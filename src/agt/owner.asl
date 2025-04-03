/* ----- CONEXIONES ENTRE HABITACIONES ----- */
// ¡¡ELIMINADAS!! La definición del entorno pertenece al Environment (Java)
// o como mucho a las creencias iniciales del robot si no hay Environment.
// El owner no necesita definir las conexiones. El robot sí para planificar rutas.


/* ----- OBJETIVOS INICIALES DEL DUEÑO (OWNER) ----- */
// Acciones que puede realizar el dueño al inicio.
// !take_medicine se activará cuando sea la hora o por otra lógica.
!take_medicine.
!sit.
!open.
!walk.
!wakeup.
!check_bored.
!medical_guides_initial.

/* ----- PLAN PARA ENVIAR PAUTAS INICIALES (Nombres específicos) ----- */
+!medical_guides_initial : not medical_guides_sent <- // Evita reenviar si ya lo hizo
    .println("Owner: Enviando pautas iniciales a la enfermera...");
    // Enviar cada pauta específica como un mensaje 'tell'
    .send(enfermera, tell, medician("Paracetamol 500mg", 1)); // ¡Usar nombres completos/correctos!
    .send(enfermera, tell, medician("Paracetamol 500mg", 9));
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 11));
    .send(enfermera, tell, medician("Amoxicilina 500mg", 15));
    .send(enfermera, tell, medician("Omeprazol 20mg", 5));
    .send(enfermera, tell, medician("Loratadina 10mg", 3));
    .send(enfermera, tell, medician("Omeprazol 20mg", 18));
    .send(enfermera, tell, medician("Paracetamol 500mg", 17));
    .send(enfermera, tell, medician("Omeprazol 20mg", 22));
    +medical_guides_sent; // Añade creencia para marcar que ya se envió
    .println("Owner: Pautas iniciales enviadas.").
// Si ya se enviaron, el objetivo se cumple sin hacer nada.
+!medical_guides_initial : medical_guides_sent <- true.

/* ----- OBJETIVO: Despertarse (wakeup) (Sin cambios lógicos) ----- */
+!wakeup : .my_name(Ag) & not busy <-
    +busy;
    !check_bored; // Empieza a comprobar aburrimiento
    .println("Owner: Acaba de despertar.");
    .wait(3000);
    -busy;
    !sit. // Se sienta después de despertar

+!wakeup : .my_name(Ag) & busy <-
    .println("Owner: Ya está despierto y haciendo algo.");
    .wait(10000);
    !wakeup. // Reintentar por si acaso

/* ----- OBJETIVO: Caminar (walk) (Sin cambios lógicos) ----- */
+!walk : .my_name(Ag) & not busy <-
    +busy;
    .println("Owner: No está ocupado, se levanta para caminar.");
    .wait(500);
    // Asumimos que empieza en algún sitio, ¿quizás ir a un sitio aleatorio?
    // !at(Ag, sofa); // Ir al sofá como punto de partida?
    .println("Owner: Caminando por casa..."); // Simulación
    .wait(5000); // Simula tiempo caminando
    -busy;
    !sit. // Después de caminar, quizás abre la puerta? (Lógica ejemplo)

+!walk : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede caminar ahora.");
    .wait(6000);
    !walk.

/* ----- OBJETIVO: Tomar Medicina (Modificado para ser específico y basado en hora) ----- */
// Este plan ahora se activa para comprobar si es hora de tomar medicina.
// Podría ser un objetivo inicial recurrente o activado por otros planes.
// Ejemplo: !check_med_time como objetivo inicial recurrente.

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
    !at(Ag, delivery); // Ir a la zona de entrega/puerta principal
    .println("Owner: Abriendo la puerta...");
    .random(X); .wait(X*7351+2000); // Simula tiempo abriendo
    .println("Owner: Puerta abierta y cerrada. Vuelve a dentro.");
    !at(Ag, sofa); // Vuelve al sofá (ejemplo)
    sit(sofa); // Necesita la acción 'sit' del entorno
    -busy.

+!open : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede abrir la puerta ahora.");
    .wait(8000);
    !open.

/* ----- OBJETIVO: Sentarse (sit) (Sin cambios lógicos importantes) ----- */
// Este plan parece más una rutina de moverse y sentarse en varios sitios.
// Podría simplificarse o hacerse más dirigida.
// La parte de !get(drug) o !get(beer) parece fuera de lugar aquí.
// Se debería decidir si pedir algo *antes* de empezar la rutina de sentarse.
+!sit : .my_name(Ag) & not busy <-
    +busy;
    .println("Owner: Iniciando rutina de sentarse en varios sitios.");
    // Quitado: !get(drug) / !get(beer) - Debería ser una decisión separada.

    // Rutina de moverse y sentarse:
    !at(Ag, chair3); sit(chair3); .wait(4000);
    !at(Ag, chair4); sit(chair4); .wait(10000);
    !at(Ag, chair2); sit(chair2); .wait(4000);
    !at(Ag, chair1); sit(chair1); .wait(4000);
    !at(Ag, sofa); sit(sofa); .wait(10000);

    -busy;
    // Después de sentarse mucho, ¿quizás !walk?
    !walk.

+!sit : .my_name(Ag) & busy <-
    .println("Owner: Ocupado, no puede iniciar rutina de sentarse.");
    .wait(30000);
    !sit.

/* ----- OBJETIVO: Ir a un lugar (!at) (Sin cambios lógicos) ----- */
// Verifica si ya está en P, si no, lanza !go(P)
+!at(Ag, P) : at(Ag, P) <-
    //.println("Owner ya está en ",P); // Opcional, mucho log
    .wait(10).
+!at(Ag, P) : not at(Ag, P) <-
    .println("Owner: Yendo a ", P);
    !go(P); // Lanza navegación
    //.println("Owner: Comprobando si llegó a ", P); // Opcional
    !at(Ag, P). // Verifica de nuevo

/* ----- OBJETIVO: Navegar a un lugar (!go) (Sin cambios lógicos) ----- */
// Lógica de movimiento basada en habitaciones y puertas (usa move_towards)
// Estos planes dependen de las percepciones atRoom y atDoor y las creencias connect.
// Asume que el entorno proporciona atRoom(X) y atDoor correctamente.
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

/* ----- OBJETIVO: Pedir medicamento o cerveza (Modificado para ser específico) ----- */
// Se activa con !get("NombreDelMedicamento") o !get(beer)
+!get(ItemName) : .my_name(Name) <- // ItemName es el nombre específico
    Time = math.ceil(math.random(4000));
    .println("Owner: Esperando ", Time, " ms antes de pedir ", ItemName, " a la enfermera.");
    .wait(Time);
    // *** ENVÍA PETICIÓN ESPECÍFICA ***
    .send(enfermera, achieve, has(Name, ItemName)).

/* ----- OBJETIVO: Recibir y procesar medicamento o cerveza (Modificado para ser específico) ----- */
// Se activa por una creencia +has(owner, ItemName) añadida (normalmente por el robot via hand_in)
+has(owner, ItemName) : true <- // ItemName es el nombre específico
    .println("Owner: He recibido ", ItemName, ".");
    !take(ItemName). // Inicia el plan para tomar/consumir el item específico

/* ----- OBJETIVO: Tomar/Consumir medicamento o cerveza (Modificado para ser específico) ----- */
// Se activa con !take(ItemName)
+!take(ItemName) : has(owner, ItemName) <- // Verifica si aún tiene el item específico
    // La acción 'sip' del entorno es genérica, pero el log es específico.
    sip(drug); // Asume que 'sip(drug)' funciona para cualquier medicamento. Si no, se necesita sip(ItemName).
    //sip(beer) // Si fuera cerveza
    .println("Owner: Consumiendo ", ItemName, "...");
    .wait(1000); // Simula tiempo de consumo
    !take(ItemName). // Llamada recursiva mientras siga teniendo el item (¿o debería parar?)

// Condición de parada: ya no tiene el item (sip lo consumió o se usó para otra cosa)
+!take(ItemName) : not has(owner, ItemName) <-
    .println("Owner: Terminado de consumir ", ItemName, ".").

/* ----- OBJETIVO: Comprobar aburrimiento (Sin cambios lógicos) ----- */
+!check_bored : true <-
    .random(X);
    WaitTime = math.round(X*20000 + 10000); // Espera entre 10 y 30 segundos
    .println("Owner: Esperando ", WaitTime, " ms para aburrirse...");
    .wait(WaitTime);
    .println("Owner: Aburrido. Preguntando la hora y el tiempo.");
    .send(enfermera, askOne, time(_), R); // Pregunta la hora
    .print("Respuesta de hora: ", R);
    .send(enfermera, tell, chat("¿Qué tiempo hace en Ourense?")); // Pregunta el tiempo
    !check_bored. // Vuelve a empezar el ciclo de aburrimiento

/* ----- OBJETIVO: Recibir y mostrar mensajes (Sin cambios lógicos) ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
    // -msg(M). // No eliminar la creencia, podría ser útil para historial