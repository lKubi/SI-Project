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

//Si queremos añadir mas creencias caduca, hay que añadirlas tambien en HouseModel, para poder verlas visualmente
caduca("Paracetamol", 0, 30).
caduca("Amoxicilina", 1, 30).
caduca("Omeprazol", 2, 30).
caduca("Ibuprofeno",3, 30).
caduca("Loratadina", 4, 30).

// Umbral para ir a cargar Por ejemplo, ir a cargar cuando baje de 360
low_energy_threshold(360). 

free[source(self)].

!check_energy. 
!check_schedule.

// Plan para reaccionar a baja energía
// Se activa si la energía actual (CE) es menor que el umbral (T)
+!check_energy : current_energy(CE) & low_energy_threshold(T) & CE < T & free[source(self)] <-
   .print("¡Energía baja (", CE, "/", T, ")! [Bucle Check] Necesito cargar.");
   !charge_battery.       

// Plan Bucle: Energía OK, o está OCUPADO, o YA está intentando cargar
+!check_energy : current_energy(CE) & low_energy_threshold(T) & ( CE >= T | not free[source(self)] ) <-
   //.print("[ENERGÍA OK o AGENTE OCUPADO: CE=", CE, "] Esperando...");
   .wait(100);  
   !check_energy.

-!check_energy : true <- 
   //.print("⚠️ Falló el objetivo por alguna razón desconocida, reintentando...");
   .wait(100);  
   !check_energy.

// Plan para lograr el objetivo de cargar la batería
+!charge_battery : free[source(self)] & .my_name(Ag) <-
       .print("Iniciando secuencia de carga hacia el cargador...");
       .print("Navegando hacia la zona del cargador...");
       !at(Ag, cargador);
       .print("Cerca del cargador, iniciando carga...");
       start_charging; 
       .print("Carga iniciada, comenzando monitorización...");  
       !wait_for_full_charge. // Monitorizar

// Plan para monitorizar la carga y detenerla cuando esté llena
// *** MODIFICADO: Añade 'free' al terminar ***
+!wait_for_full_charge : current_energy(CE) & max_energy(ME) & CE >= ME
<-
    .print("¡Batería llena (", CE, "/", ME, ")! Deteniendo carga.");
    stop_charging;
    .print("CARGA: Carga completada y detenida. Agente libre.").


/* ----- OBJETIVO: Ir a un lugar (!at) ----- */
+!at(Ag, P) : at(Ag, P) <-
    .wait(10).

+!at(Ag, P) : not at(Ag, P) & current_energy(CE) & CE > 0 <- 
      .println("[Auxiliar] Yendo a ", P); 
      !go(P);
      !at(Ag, P).

// Plan por si intenta !at con 0 energía (para que no se quede bloqueado sin hacer nada)
+!at(Ag, P) : not at(Ag, P) & current_energy(0) <- 
      .println("[Auxiliar] No puedo ir a ", P, ", ¡sin energía!").

+!at(Ag, P) : true <- 
   .println("[Auxiliar] Fallback de !at: intentando ir a ", P, " aunque no haya contexto completo.");
   !go(P).

+at(Ag, P) <- 
   .print("[Auxiliar] Confirmado: ", Ag, " está en ", P, ".").

/* ----- OBJETIVO: Navegar a un lugar (!go) ----- */
// Plan para ir dentro de la misma habitación
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) & current_energy(CE) & CE > 0 <- 
   move_towards(P).

// Plan para ir a otra habitación, conectada directamente, estando ya en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & atDoor & current_energy(CE) & CE > 0 
   <- move_towards(P).

// Plan para ir a otra habitación, conectada directamente, SIN estar en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor & current_energy(CE) & CE > 0
   <- move_towards(Door);
      !go(P). 

// Plan para ir a otra habitación, NO conectada directamente, SIN estar en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & not atDoor & current_energy(CE) & CE > 0 
   <- move_towards(DoorR);
      !go(P). 

// Plan para ir a otra habitación, NO conectada directamente, ESTANDO en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & atDoor & current_energy(CE) & CE > 0 
   <- move_towards(DoorP); 
      !go(P). 

// Plan Fallback (si los otros fallan pero las habitaciones son distintas)
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP & current_energy(CE) & CE > 0
   <- move_towards(P).

// Plan de fallo si no puede moverse por falta de energía
-!go(P)[error(E)] : current_energy(0) 
   <- .println("[Auxiliar] Fallo al intentar ir a ", P, " por falta de energía (0). Error: ", E).

// Plan de llegada 
-!go(P) <-
   .println("[Auxiliar] He llegado a ", P, "."). 

/* ----- OBJETIVO: Recibir y mostrar mensajes ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   
// Plan Bucle: Comprobar horario/caducidades si está LIBRE y la Energía está OK
+!check_schedule
   : clock(H, M) & free[source(self)] & 
     current_energy(CE) & low_energy_threshold(T) & CE >= T 
<-
   // .print("DEBUG: [Bucle Check Schedule] Hora ", H, ":", M, ". Energía OK. Comprobando caducidades..."); 
   .findall(Drug, caduca(Drug, H, M), ExpiringDrugs); // Busca caducidades
   if (.length(ExpiringDrugs) > 0) {
       for ( .member(DrugToReponer, ExpiringDrugs) ) {
           .print("Iniciando proceso para caducidad de: ", DrugToReponer);
           .send(enfermera, tell, ha_caducado(DrugToReponer));
           .send(owner, tell, ha_caducado(DrugToReponer));
           .println("Enviando mensaje de caducidad a los otros agentes.");
           +esta_caducada(DrugToReponer, H, M);
       }
   } else {
       // .println("No hay caducidad programada."); 
   };
   .wait(100);
   !check_schedule. 

// Plan Bucle: Si está OCUPADO o la Energía está BAJA, simplemente esperar
+!check_schedule
   : ( not free[source(self)] | (current_energy(CE) & low_energy_threshold(T) & CE < T) ) <-
   // .print("DEBUG: [Bucle Check Schedule] Ocupado o Energía Baja, esperando..."); // Log opcional
   .wait(100); 
   !check_schedule.

// Plan Bucle: Si falta la hora 
+!check_schedule : not clock(_, _) <-
   .print("WARN: [Bucle Check Schedule] No se encontró la creencia clock(H,M). Esperando...");
   .wait(100); 
   !check_schedule.

+esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado)  
    : caduca(NombreMedicina, HoraDetectada, MinutoDetectado) <-
    .println("¡Detectada caducidad para: ", NombreMedicina, " a la hora ", HoraDetectada, MinutoDetectado, "!");
    .println("Procediendo a iniciar reposición...");
    -caduca(NombreMedicina, HoraDetectada, MinutoDetectado);
    -esta_caducada(NombreMedicina, HoraDetectada, MinutoDetectado);
    !reponer_medicamento(NombreMedicina, HoraDetectada, MinutoDetectado).

// Plan para realizar la acción de reponer Y reprogramar la siguiente pauta
+!reponer_medicamento(NombreMedicina, HoraVencimiento, MinutoVencimiento) <-
   -free[source(self)];
   .my_name(Ag);
   .println("Procediendo a reponer medicamento: ", NombreMedicina);
	!at(Ag, delivery);
	cargar_medicamento; 
	.println("Agente ", Ag, " recogiendo la medicacion de entrega.");
	.wait(1000);
   !at(Ag, medCab);
   open(medCab);
	reponer_medicamento(NombreMedicina);
   close(medCab);
   .wait(1000); 
   .println("Medicamento ", NombreMedicina, " repuesto.");
   .send(enfermera, tell, orden_eliminar_caducaciones);
   .send(owner, tell, orden_eliminar_caducaciones); 
   +free[source(self)].
