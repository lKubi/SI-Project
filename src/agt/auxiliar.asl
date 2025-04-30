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

<<<<<<< Updated upstream
free.

caduca("Ibuprofeno 600mg", 0). 
caduca("Omeprazol 20mg", 2).  
caduca("Aspirina 100mg", 20). 
caduca("Paracetamol 500mg", 23).
caduca("Loratadina 10mg", 12).  


pauta_intervalo("Ibuprofeno 600mg", 8).  // Cada 8 horas
pauta_intervalo("Omeprazol 20mg", 24). // Cada 24 horas (1 vez al día)
pauta_intervalo("Aspirina 100mg", 24).  // Cada 24 horas
pauta_intervalo("Paracetamol 500mg", 6). // Cada 6 horas
pauta_intervalo("Loratadina 10mg", 24). // Cada 24 horas


/* ----- ##### PLANES MEJORADOS (Misma Estructura, Lógica Cambiada) ##### ----- */

// Plan para revisar periódicamente la pauta de medicación (USA HORA SIMULADA)
+clock(SimulatedHour)[source(Source)] : free[source(self)] <-
    .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Revisando pauta...");
    if (caduca(DrugToDeliver, SimulatedHour)) {
        .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Pauta encontrada: Procesar ", DrugToDeliver);
        .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Iniciando procesamiento para ", DrugToDeliver);
        +esta_caducada(DrugToDeliver); // Sigue usando esta_caducada como trigger intermedio

    } else {
        .println("PLAN REACTIVO (Clock ", SimulatedHour, "): No se ha encontrado pauta para esta hora.");
    };
.

+clock(SimulatedHour)[source(Source)] : not free[source(self)] <-
     .wait(1000);
     +clock(SimulatedHour)[source(Source)].

+esta_caducada(NombreMedicina)         
    : caduca(NombreMedicina, HoraCaducada) 
<-
    -caduca(NombreMedicina, HoraCaducada);
    .println("¡Detectada caducidad para: ", NombreMedicina, " a la hora ", HoraCaducada, "!");
    .println("Procediendo a iniciar reposición...");

    -esta_caducada(NombreMedicina);
    .println("Marca de caducidad eliminada para ", NombreMedicina);
    !reponer_medicamento(NombreMedicina, HoraCaducada);
.

// Plan para realizar la acción de reponer Y reprogramar la siguiente pauta
+!reponer_medicamento(NombreMedicina, HoraVencimiento) // <- Acepta la HoraVencimiento
     // CONTEXTO: Necesitamos el intervalo definido en las creencias estáticas
     : pauta_intervalo(NombreMedicina, IntervaloHoras)
<-
    .println("Procediendo a reponer medicamento: ", NombreMedicina);
=======
caduca("Paracetamol", 0, 30).
caduca("Amoxicilina", 1, 30).
caduca("Omeprazol", 2, 30).
caduca("Ibuprofeno",3, 30).
caduca("Loratadina", 4, 30).


free.

!stay_alert.


/* ----- OBJETIVO: Ir a un lugar (!at) ----- */
+!at(Ag, P) : at(Ag, P) <-
    .wait(10).
+!at(Ag, P) : not at(Ag, P) <-
    .println("Owner: Yendo a ", P);
    !go(P);
    !at(Ag, P). 

/* ----- OBJETIVO: Navegar a un lugar (!go) ----- */
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <-
    move_towards(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & atDoor <- // Ya en puerta
    move_towards(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor <- // No en puerta
    move_towards(Door);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & not atDoor <-
    move_towards(DoorR);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & atDoor <-
    move_towards(DoorP);
    !go(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <- // Fallback
    move_towards(P).
-!go(P) <- 
.println("Owner: He llegado a ", P, ".").


// Plan para mantenerse mínimamente activo
+!stay_alert : true <-
    .wait(500); // Espera un tiempo corto (ajusta si es necesario)
    // .print("Auxiliar check..."); // Mensaje de depuración opcional
    !stay_alert. // Vuelve a lanzar el objetivo para crear un ciclo controlado



/* ----- OBJETIVO: Recibir y mostrar mensajes (Sin cambios lógicos) ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   


+clock(H, M)[source(S)] : free[source(self)] <-
    .findall(Drug, caduca(Drug, H, M), ExpiringDrugs); // Encuentra todas las coincidencias
    if (.length(ExpiringDrugs) > 0) {
        .print("Auxiliar: [Clock ", H, ":", M, "] Caducaciones encontradas: ", ExpiringDrugs);
        for ( .member(DrugToReponer, ExpiringDrugs) ) {
            // Para cada una, añade la creencia intermedia o directamente el objetivo
             .print("Iniciando proceso para: ", DrugToReponer);
             +esta_caducada(DrugToReponer, H, M);
             // OJO: El plan +esta_caducada elimina caduca(). Esto podría ser problemático
             // si se ejecutan concurrentemente. Podría ser mejor eliminar caduca()
             // DESPUÉS de reponer exitosamente.
        }
    } else {
        .println("Auxiliar: [Clock ", H, ":", M, "] No hay caducacion programada para mí a esta hora.");
    }.
// Similar change for the 'not free' plan trigger
+clock(SimulatedHour, SimulatedMinute)[source(Source)] : not free[source(self)] <-
     .wait(1000);
     .println("Auxiliar: [Clock ", SimulatedHour, ":", SimulatedMinute, "] Reintentando procesamiento de tick (estaba ocupado).");
     +clock(SimulatedHour, SimulatedMinute)[source(Source)].




+esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado)  
    : caduca(NombreMedicina, HoraDetectada, MinutoDetectado) 
<-
    -caduca(NombreMedicina, HoraDetectada, MinutoDetectado);
    .println("¡Detectada caducidad para: ", NombreMedicina, " a la hora ", HoraDetectada, MinutoDetectado, "!");
    .println("Procediendo a iniciar reposición...");

    -esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado);
    .println("Marca de caducidad eliminada para ", NombreMedicina);
    !reponer_medicamento(NombreMedicina, HoraDetectada, MinutoDetectado);
.

// Plan para realizar la acción de reponer Y reprogramar la siguiente pauta
+!reponer_medicamento(NombreMedicina, HoraVencimiento, MinutoVencimiento) // <- Acepta la HoraVencimiento
     // CONTEXTO: Necesitamos el intervalo definido en las creencias estáticas
<-
    .println("Procediendo a reponer medicamento: ", NombreMedicina);
	.my_name(Ag);
>>>>>>> Stashed changes
	!at(Ag, delivery);
	cargar_medicamento; // Cargar el medicamento en el agente
	.println("Agente ", Ag, " recogiendo la medicacion de entrega.");
	.wait(1000);
    !at(Ag, medCab);
    open(medCab);
<<<<<<< Updated upstream
	reponer_medicamento;
    close(medCab);
    .wait(1000); 
    .println("Medicamento ", NombreMedicina, " repuesto.");

    // --- Reprogramación Periódica (LA MEJORA CLAVE) ---
    // Calcula la hora de la *siguiente* dosis usando el intervalo
    NuevaHoraProximaDosis = (HoraVencimiento + IntervaloHoras); // Modulo 24 para reloj 0-23h

    +caduca(NombreMedicina, NuevaHoraProximaDosis);
    .println("Pauta para ", NombreMedicina, " reprogramada periódicamente. Próxima hora: ", NuevaHoraProximaDosis);
.



/* ----- ACTUALIZACIÓN DE LA LOCALIZACIÓN DEL AUXILIAR ----- */
// El robot actualiza su ubicación y se mueve entre habitaciones o hacia puertas dependiendo de su situación.
+!at(Ag, P) : at(Ag, P) <- 
	//.println(Ag, " is at ",P);
	.wait(500).
+!at(Ag, P) : not at(Ag, P) <- 
	.println("Going to ", P, " <=======================");  
	.wait(200);
	!go(P);                                        
	//.println("Checking if is at ", P, " ============>");
	!at(Ag, P).            
														
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <- 
		.println("<================== 1 =====================>");
		.println("Al estar en la misma habitación se debe mover directamente a: ", P);
		move_towards(P).  
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
			connect(RoomAg, RoomP, Door) & not atDoor <-
		.println("<================== 3 =====================>");
		.println("Al estar en una habitación contigua se mueve hacia la puerta: ", Door);
		move_towards(Door); 
		!go(P).                     
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
			connect(RoomAg, RoomP, Door) <- //& not atDoor <-
		.println("<================== 3 =====================>");
		.println("Al estar en la puerta de la habitación contigua se mueve hacia ", P);
		move_towards(P); 
		!go(P).       
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
			not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
			connect(Room, RoomP, DoorP) & not atDoor <-
		.println("<================== 4 =====================>");
		.println("Se mueve a: ", DoorR, " para ir a la habitación contigua, ", Room);
		move_towards(DoorR); 
		!go(P). 
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
			not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
			connect(Room, RoomP, DoorP) & atDoor <-
		.println("<================== 4 BIS =====================>");
		.println("Se mueve a: ", DoorP, " para acceder a la habitación ", RoomP);
		move_towards(DoorP); 
		!go(P). 
	+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <- //& not atDoor <-
		.println("Owner is at ", RoomAg,", that is not a contiguous room to ", RoomP);
		.println("<================== 5 =====================>");
		move_towards(P).                                                          
	-!go(P) <- 
		.println("¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿ WHAT A FUCK !!!!!!!!!!!!!!!!!!!!");
		.println("..........SOMETHING GOES WRONG......").                                        
																						

/* ----- GESTIÓN DE CHAT ----- */
// El robot responde a los mensajes de chat enviados por cualquier agente.
+chat(Msg)[source(Ag)] : answer(Msg, Answ) <-  
	.println("El agente ", Ag, " me ha chateado: ", Msg);
	.send(Ag, tell, msg(Answ)). 

/* ----- ACTUALIZACIÓN DE LA HORA ----- */
// El robot puede verificar la hora actual.                  
+?time : true
	<-  watchClock.
	
=======
	reponer_medicamento(NombreMedicina);
    close(medCab);
    .wait(1000); 
    .println("Medicamento ", NombreMedicina, " repuesto.");
.


+?time : true
	<-  watchClock.
>>>>>>> Stashed changes
