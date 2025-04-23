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
!start_medicine_routine. 
!check_bored.
!medical_guides_initial.
!update_schedule_later.

/* ----- PLAN PARA ENVIAR PAUTAS INICIALES Y GUARDARLAS LOCALMENTE ----- */
+!medical_guides_initial : not medical_guides_sent <-
    .println("Owner: Enviando pautas iniciales a la enfermera y guardándolas localmente...");
    // Pauta 1
    .send(enfermera, tell, medician("Paracetamol 500mg", 1));
    +medician("Paracetamol 500mg", 1); // <-- Añadido: creencia local
    // Pauta 2
    .send(enfermera, tell, medician("Paracetamol 500mg", 9));
    +medician("Paracetamol 500mg", 9); // <-- Añadido: creencia local
    // Pauta 3
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 11));
    +medician("Ibuprofeno 600mg", 11); // <-- Añadido: creencia local
    // Pauta 4
    .send(enfermera, tell, medician("Amoxicilina 500mg", 15));
    +medician("Amoxicilina 500mg", 15); // <-- Añadido: creencia local
    // Pauta 5
    .send(enfermera, tell, medician("Omeprazol 20mg", 5));
    +medician("Omeprazol 20mg", 5); // <-- Añadido: creencia local
    // Pauta 6
    .send(enfermera, tell, medician("Loratadina 10mg", 3));
    +medician("Loratadina 10mg", 3); // <-- Añadido: creencia local
    // Pauta 7
    .send(enfermera, tell, medician("Omeprazol 20mg", 18));
    +medician("Omeprazol 20mg", 18); // <-- Añadido: creencia local
    // Pauta 8
    .send(enfermera, tell, medician("Paracetamol 500mg", 20));
    +medician("Paracetamol 500mg", 20); // <-- Añadido: creencia local
    // Pauta 9
    .send(enfermera, tell, medician("Omeprazol 20mg", 23));
    +medician("Omeprazol 20mg", 23); // <-- Añadido: creencia local

    +medical_guides_sent; // Marca que ya se envió/guardó
    .println("Owner: Pautas iniciales enviadas y guardadas localmente.");

    .println("Owner: Pautas cargadas. Intentando tomar la primera medicina de la lista...").

+!medical_guides_initial : medical_guides_sent <- true.


/* ----- PLAN PARA ACTUALIZAR PAUTAS (CON BORRADO Y NUEVAS CREENCIAS) ----- */
// (Plan !update_schedule_later sin cambios)
+!update_schedule_later : true <-
    .println("Owner: Esperando para actualizar pautas...");
    .wait(480000); // 5 minutos
    .println("Owner: ¡Tiempo de actualizar pautas!");
    !do_schedule_update.

// **MODIFICADO**: Borra también las creencias locales del owner
+!do_schedule_update : true <-
    .println("Owner: Iniciando actualización de pautas...");
    // 1. Pedir a la enfermera que borre las pautas antiguas
    .println("Owner: Solicitando a enfermera borrar pautas antiguas.");
    .send(enfermera, achieve, clear_schedule);
    // 2. Borrar las creencias locales de pautas antiguas del owner
    .println("Owner: Borrando pautas antiguas locales...");
    .abolish(medician(_, _)); // Elimina todas las creencias 'medician'
    // 3. Esperar un poco
    .wait(1500);
    // 4. Enviar y guardar las nuevas pautas
    !send_new_schedule.

// **MODIFICADO**: Añade las nuevas pautas como creencias locales
+!send_new_schedule : true <-
    .println("Owner: Enviando y guardando NUEVAS pautas...");
    // Nueva Pauta 1
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 8));
    +medician("Ibuprofeno 600mg", 8); // <-- Añadido
    // Nueva Pauta 2
    .send(enfermera, tell, medician("Omeprazol 20mg", 14));
    +medician("Omeprazol 20mg", 14); // <-- Añadido
    // Nueva Pauta 3
    .send(enfermera, tell, medician("Aspirina 100mg", 19));
    +medician("Aspirina 100mg", 19); // <-- Añadido
    // Nueva Pauta 4
    .send(enfermera, tell, medician("Paracetamol 500mg", 22));
    +medician("Paracetamol 500mg", 22); // <-- Añadido

    .println("Owner: NUEVAS pautas enviadas y guardadas.").
    // Podrías querer volver a marcar como enviadas si usas la misma bandera
    // +medical_guides_sent; // O usar una nueva bandera si es necesario


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

/* ----- NUEVO: PLAN PARA INICIAR Y CONTINUAR LA RUTINA DE MEDICACIÓN ----- */
+!start_medicine_routine : true <-
    .println("[Owner] Esperando 15 segundos de tiempo para iniciar rutina de medicación.");
    .wait(15000); 
    !check_and_take_medicine. // Llama al plan que realmente comprueba y actúa

/* ----- MODIFICADO: OBJETIVO Comprobar y Tomar Medicamento (!check_and_take_medicine) ----- */
// Plan principal: Hay medicación pendiente y no está ocupado
+!check_and_take_medicine : .my_name(Ag) & not busy & medician(DrugToTake, Hour) <-
    +busy; // Marcar como ocupado para esta acción específica
    .println("Owner [", Ag, "]: Comprobando medicación... ¡Sí! Debo tomar: ", DrugToTake, " (programada para ", Hour, "h). Iniciando acción.");

    // Ir al botiquín
    !at(Ag, medCab);
    .println("Owner [", Ag, "]: He llegado al botiquín (medCab).");

    // Simular obtención y toma del medicamento ESPECÍFICO
    open(medCab);
    obtener_medicamento(DrugToTake); // Usa la variable con el nombre del medicamento encontrado
    close(medCab);
    .println("Owner [", Ag, "]: Obtenido ", DrugToTake, " del botiquín.");
    .wait(1000); // Simular tiempo de preparación/toma

    .println("Owner [", Ag, "]: Tomando ", DrugToTake, "...");

    // --- ACCIONES CLAVE ---
    // 1. Eliminar la pauta específica de las creencias LOCALES del owner
    .abolish(medician(DrugToTake, Hour));
    .println("Owner [", Ag, "]: Pauta para ", DrugToTake, " a las ", Hour, "h eliminada de mis creencias locales.");

    // 2. Notificar a la enfermera que se ha tomado esta medicación específica Y SU HORA PROGRAMADA
    .send(enfermera, tell, medication_consumed(DrugToTake, Hour)); // Envía Drug y Hour
    .println("Owner [", Ag, "]: Notificado a 'enfermera' que he tomado ", DrugToTake, " (de la pauta de las ", Hour, "h).");
    // --- FIN ACCIONES CLAVE ---

    // Volver a un lugar y liberar al owner
    !at(Ag, sofa);
    .println("Owner [", Ag, "]: Volviendo al sofá después de tomar la medicación.");
    .wait(2000); // Espera breve post-acción
    -busy; // Marcar como libre
    .println("Owner [", Ag, "]: Acción de tomar ", DrugToTake, " completada. Reiniciando ciclo de comprobación de medicación.");
    !start_medicine_routine. // <-- REINICIA EL CICLO

// Plan alternativo: No hay medicación pendiente y no está ocupado
+!check_and_take_medicine : .my_name(Ag) & not busy & not medician(_,_) <-
    .println("Owner [", Ag, "]: Comprobando medicación... No hay nada programado en mi lista ahora. Reiniciando ciclo.");
    !start_medicine_routine. // <-- REINICIA EL CICLO (no hay nada que hacer, pero el ciclo continúa)

// Plan por si está ocupado cuando intenta comprobar/tomar medicación
+!check_and_take_medicine : .my_name(Ag) & busy <-
    .println("Owner [", Ag, "]: Quería comprobar medicación, pero estoy ocupado. Lo haré después. Reiniciando ciclo.");
    // No necesita esperar aquí, la espera ocurrió en !start_medicine_routine
    !start_medicine_routine. // <-- REINICIA EL CICLO (intentará de nuevo después de la próxima espera aleatoria)


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
    !at(Ag, chair4); sit(chair4); .wait(5000);
    !at(Ag, chair2); sit(chair2); .wait(4000);
    !at(Ag, chair1); sit(chair1); .wait(4000);
    !at(Ag, sofa); sit(sofa); .wait(10000);
    !at(Ag, bed3); sit(bed3); .wait(2000);

    -busy;
    !walk.

+!sit : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede iniciar rutina de sentarse.");
    .wait(30000);
    !sit.

/* ----- OBJETIVO: Ir a un lugar (!at) ----- */
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
    .println("Owner: Esperando ", Time, " ms antes de pedir ", ItemName, " al robot.");
    .wait(Time);
    // *** ENVÍA PETICIÓN ESPECÍFICA ***
    .send(enfermera, achieve, has(Name, ItemName)).

/* ----- OBJETIVO: Recibir y procesar medicamento o cerveza ----- */
+has(owner, ItemName) : true <-
    .println("Owner: He recibido ", ItemName, ".");
    !take(ItemName). 

/* ----- OBJETIVO: Tomar/Consumir medicamento o cerveza ----- */
+!take(ItemName) : has(owner, ItemName) <- 
    +busy;
    sip(ItemName); 
    .println("Owner: Consumiendo ", ItemName, "...");
    .wait(1000); // Simular tiempo de consumo
    !take(ItemName);
    -busy.

// Condición de parada: ya no tiene el item (sip lo consumió o se usó para otra cosa)
+!take(ItemName) : not has(owner, ItemName) <-
    .println("Owner: Terminado de consumir ", ItemName, ".").

/* ----- OBJETIVO: Comprobar aburrimiento (Sin cambios lógicos) ----- */
+!check_bored : true <-
    .wait(100);
    .send(enfermera, askOne, time, R);
    .send(enfermera, tell, chat("¿Qué tiempo hace en Ourense?")); 
    !check_bored. 

/* ----- OBJETIVO: Recibir y mostrar mensajes (Sin cambios lógicos) ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   

// Plan para manejar la solicitud de la enfermera de eliminar una pauta
+!remove_my_medician(Drug, Hour)[source(enfermera)] : medician(Drug, Hour) <- // Solo si aún la tiene
    .println("Owner: Recibida petición de enfermera para eliminar pauta ", Drug, " (", Hour, "h).");
    .abolish(medician(Drug, Hour));
    .println("Owner: Pauta eliminada por petición.").

+!remove_my_medician(Drug, Hour)[source(enfermera)] : not medician(Drug, Hour) <-
    .println("Owner: Recibida petición de enfermera para eliminar pauta ", Drug, " (", Hour, "h), pero ya no la tenía.").