/* ----- CONEXIONES ENTRE HABITACIONES ----- */
// Definimos las conexiones entre las diferentes habitaciones y sus puertas
// Esto permite que el robot pueda moverse de una habitación a otra a través de puertas específicas.
connect(kitchen, hall, doorKit1).
connect(kitchen, hallway, doorKit2).
connect(hall, kitchen, doorKit1).
connect(hallway, kitchen, doorKit2).
connect(bath1, hallway, doorBath1).
connect(bath2, bedroom1, doorBath2).
connect(hallway, bath1, doorBath1).
connect(bedroom1, bath2, doorBath2).
connect(bedroom1, hallway, doorBed1).
connect(hallway, bedroom1, doorBed1).
connect(bedroom2, hallway, doorBed2).
connect(hallway, bedroom2, doorBed2).
connect(bedroom3, hallway, doorBed3).
connect(hallway, bedroom3, doorBed3).
connect(hall, livingroom, doorSal1).
connect(livingroom, hall, doorSal1).
connect(hallway, livingroom, doorSal2).
connect(livingroom, hallway, doorSal2).


/* ----- OBJETIVOS INICIALES DEL DUEÑO (OWNER) ----- */
!take_medicine.
!sit.
!open.
!walk.
!wakeup.
!check_bored.
!medical_guides_initial.
!update_schedule_later.

/* ----- PLAN PARA ENVIAR PAUTAS INICIALES (Nombres específicos) ----- */
+!medical_guides_initial : not medical_guides_sent <- // Evita reenviar si ya lo hizo
    .println("Owner: Enviando pautas iniciales a la enfermera...");
    .send(enfermera, tell, medician("Paracetamol 500mg", 1)); 
    .send(enfermera, tell, medician("Paracetamol 500mg", 9));
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 11));
    .send(enfermera, tell, medician("Amoxicilina 500mg", 15));
    .send(enfermera, tell, medician("Omeprazol 20mg", 5));
    .send(enfermera, tell, medician("Loratadina 10mg", 3));
    .send(enfermera, tell, medician("Omeprazol 20mg", 18));
    .send(enfermera, tell, medician("Paracetamol 500mg", 20));
    .send(enfermera, tell, medician("Omeprazol 20mg", 23));
    +medical_guides_sent; // Añade creencia para marcar que ya se envió
    .println("Owner: Pautas iniciales enviadas.").
// Si ya se enviaron, el objetivo se cumple sin hacer nada.
+!medical_guides_initial : medical_guides_sent <- true.


// Plan para esperar un tiempo definido y luego iniciar la actualización
// Este plan se ejecutará una sola vez gracias al objetivo inicial !update_schedule_later
+!update_schedule_later : true <-
    // Espera un tiempo en milisegundos (ej: 5 minutos = 300000 ms)
    // Ajusta este valor según cuánto tiempo quieres esperar
    .println("Owner: Esperando para actualizar pautas...");
    .wait(300000); // Espera 5 minutos (ajusta el tiempo aquí)
    .println("Owner: ¡Tiempo de actualizar pautas!");
    !do_schedule_update.

// Plan principal para realizar la actualización
+!do_schedule_update : true <-
    .println("Owner: Iniciando actualización de pautas...");
    // 1. Pedir a la enfermera que borre las pautas antiguas
    .println("Owner: Solicitando a enfermera borrar pautas antiguas.");
    .send(enfermera, achieve, clear_schedule); 
    // 2. Esperar un poco para asegurar que la enfermera procesó el borrado
    //    (Una solución más robusta usaría confirmación, pero esto es más simple)
    .wait(1500);
    // 3. Enviar las nuevas pautas
    !send_new_schedule.

// Plan para enviar el NUEVO conjunto de pautas
+!send_new_schedule : true <-
    .println("Owner: Enviando NUEVAS pautas a la enfermera...");
    // *** DEFINE AQUÍ TUS NUEVAS PAUTAS ***
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 8)); // Nueva pauta/hora
    .send(enfermera, tell, medician("Omeprazol 20mg", 14));  // Nueva pauta/hora
    .send(enfermera, tell, medician("Aspirina 100mg", 19)); // Nueva pauta/hora (medicamento nuevo)
    .send(enfermera, tell, medician("Paracetamol 500mg", 22)); // Paracetamol a otra hora
    .println("Owner: NUEVAS pautas enviadas.").

/* ----- OBJETIVO: Despertarse (wakeup) ----- */
+!wakeup : .my_name(Ag) & not busy <-
    +busy;
    !check_bored; 
    .println("Owner: Acaba de despertar.");
    .wait(3000);
    -busy;
    !sit.

+!wakeup : .my_name(Ag) & busy <-
    .println("Owner: Ya está despierto y haciendo algo.");
    .wait(10000);
    !wakeup.

/* ----- OBJETIVO: Caminar (walk) ----- */
+!walk : .my_name(Ag) & not busy <-
    +busy;
    .println("Owner: No está ocupado, se levanta para caminar.");
    .wait(500);
    .println("Owner: Caminando por casa..."); 
    .wait(5000); 
    -busy;
    !sit. 

+!walk : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede caminar ahora.");
    .wait(6000);
    !walk.

/* ----- ENTREGA PARTE 1 ----- */
/* ----- OBJETIVO: Ir al almacén de medicamentos y tomar la medicación ----- */
+!take_medicine : .my_name(Name) & not busy 
   <- 
      +busy;
      !at(Ag, medCab);
      .println("Owner is at the medicine shelf.");
      
      open(medCab); 
      obtener_medicamento("Paracetamol 500mg"); 
      close(medCab);
      .wait(4000);
      
	  .println("Owner is taking the drug.");
      // Notificar al robot que ha tomado la medicación
      .send(enfermera, tell, medication_consumed(drug));
            
      !at(Ag, sofa);
      .wait(5000);
      -busy.
 
+!take_medicine : .my_name(Name) & busy
   <- 
      .println("Owner is doing something else and cannot take medicine now.");
      .wait(5000);
      !take_medicine.

/* ----- OBJETIVO: Abrir la puerta (open) (Sin cambios lógicos) ----- */
+!open : .my_name(Ag) & not busy <-
    +busy;
    .println("Owner: Va hacia la puerta principal.");
    .wait(200);
    !at(Ag, delivery);
    .println("Owner: Abriendo la puerta...");
    .random(X); .wait(X*7351+2000); 
    .println("Owner: Puerta abierta y cerrada. Vuelve a dentro.");
    !at(Ag, sofa); 
    sit(sofa); // 
    -busy.

+!open : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede abrir la puerta ahora.");
    .wait(8000);
    !open.

/* ----- OBJETIVO: Sentarse (sit) ----- */
+!sit : .my_name(Ag) & not busy <-
    +busy;
    .println("Owner: Iniciando rutina de sentarse en varios sitios.");

    // Rutina de moverse y sentarse:
    !at(Ag, chair3); sit(chair3); .wait(4000);
    !at(Ag, chair4); sit(chair4); .wait(10000);
    !at(Ag, chair2); sit(chair2); .wait(4000);
    !at(Ag, chair1); sit(chair1); .wait(4000);
    !at(Ag, sofa); sit(sofa); .wait(10000);

    -busy;
    !walk.

+!sit : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede iniciar rutina de sentarse.");
    .wait(30000);
    !sit.

/* ----- OBJETIVO: Ir a un lugar (!at) (Sin cambios lógicos) ----- */
+!at(Ag, P) : at(Ag, P) <-
    .wait(10).
+!at(Ag, P) : not at(Ag, P) <-
    .println("Owner: Yendo a ", P);
    !go(P);
    !at(Ag, P). 

/* ----- OBJETIVO: Navegar a un lugar (!go) ----- */
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <-
    .println("Owner: Moviendo dentro de ", RoomAg, " hacia ", P);
    move_towards(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & atDoor <- // Ya en puerta
     .println("Owner: En puerta ", Door, ", yendo a ", P, " en ", RoomP);
    move_towards(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor <- // No en puerta
    .println("Owner: En ", RoomAg, ", necesita ir a ", RoomP, ". Yendo a puerta ", Door);
    move_towards(Door);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & not atDoor <-
    .println("Owner: En ", RoomAg, ", necesita ir a ", RoomP, ". Yendo a puerta ", DoorR, " (hacia ", Room, ")");
    move_towards(DoorR);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & atDoor <-
    .println("Owner: En puerta ", DoorR, ", necesita ir a ", RoomP, ". Yendo a puerta ", DoorP);
    move_towards(DoorP);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <- // Fallback
    .println("Owner: En ", RoomAg,", no contiguo a ", RoomP, ". Yendo directo a ", P);
    move_towards(P).
-!go(P) <- .println("Owner: ¡ERROR DE NAVEGACIÓN! Algo falló en !go(", P, ")").

/* ----- OBJETIVO: Pedir medicamento o cerveza ----- */
+!get(ItemName) : .my_name(Name) <- // ItemName es el nombre específico
    Time = math.ceil(math.random(4000));
    .println("Owner: Esperando ", Time, " ms antes de pedir ", ItemName, " a la enfermera.");
    .wait(Time);
    // *** ENVÍA PETICIÓN ESPECÍFICA ***
    .send(enfermera, achieve, has(Name, ItemName)).

/* ----- OBJETIVO: Recibir y procesar medicamento o cerveza ----- */
+has(owner, ItemName) : true <-
    .println("Owner: He recibido ", ItemName, ".");
    !take(ItemName). 

/* ----- OBJETIVO: Tomar/Consumir medicamento o cerveza ----- */
+!take(ItemName) : has(owner, ItemName) <- 
    sip(drug); 
    .println("Owner: Consumiendo ", ItemName, "...");
    .wait(1000); // Simular tiempo de consumo
    !take(ItemName). 

// Condición de parada: ya no tiene el item (sip lo consumió o se usó para otra cosa)
+!take(ItemName) : not has(owner, ItemName) <-
    .println("Owner: Terminado de consumir ", ItemName, ".").

/* ----- OBJETIVO: Comprobar aburrimiento (Sin cambios lógicos) ----- */
+!check_bored : true <-
    .wait(100);
    .println("Owner: Aburrido. Preguntando la hora y el tiempo.");
    .send(enfermera, askOne, time, R);
    .print("Respuesta de hora: ", R);    
    .send(enfermera, tell, chat("¿Qué tiempo hace en Ourense?")); 
    !check_bored. 

/* ----- OBJETIVO: Recibir y mostrar mensajes (Sin cambios lógicos) ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   

    