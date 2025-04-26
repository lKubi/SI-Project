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

free.

caduca("Ibuprofeno 600mg", 0). 
caduca("Omeprazol 20mg", 15).  
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
	
	.println("Agente ", Ag, " recogiendo la medicacion de entrega.");
	.wait(1000);
    !at(Ag, medCab);
    open(medCab);
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
	
