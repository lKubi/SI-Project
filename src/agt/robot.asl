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

/* ----- ESTADO INICIAL DEL ROBOT ----- */
// El robot comienza libre, es decir, no tiene ninguna tarea asignada al principio.
free.

/* ----- DISPONIBILIDAD INICIAL DE PRODUCTOS (Específica) ----- */
// Estas creencias deben reflejar el estado inicial en HouseModel o ser actualizadas por percepciones.
available("Paracetamol", medCab).
available("Ibuprofeno", medCab).
available("Amoxicilina", medCab).
available("Omeprazol", medCab).
available("Loratadina", medCab).

note("Paracetamol", 3).
note("Ibuprofeno", 4).
note("Amoxicilina", 8).
note("Omeprazol", 0).
note("Loratadina", 4).

// Al inicio de enfermera.asl

// Umbral para ir a cargar
low_energy_threshold(250). // Por ejemplo, ir a cargar cuando baje de 150



available(beer, fridge).

/* ----- LÍMITES DE CONSUMO (Específicos) ----- */
// Establecer límites para cada cerveza.

limit(beer, 5).

/*
limit("Paracetamol", 4).
limit("Ibuprofeno", 3).
limit("Amoxicilina", 2).
limit("Omeprazol", 1).
limit("Loratadina", 1).
*/



checkMedCab(Drug, N) :-
	N>0 &                 // La cantidad anotada debe ser > 0
	note(Drug, N) &       // Debe existir la anotación interna de la enfermera
	stock(Drug, Quantity) & // Debe existir la creencia del stock actual (¡viene del entorno!)
	N-1 = Quantity.       // El stock actual debe ser exactamente 1 menos que el anotado
//LO QUITAMOS O NO LO QUITAMOS
/* ----- REGLA DE CONSUMO EXCESIVO (Verifica droga específica 'B') ----- */
// Esta regla verifica si el dueño ha consumido más de un tipo específico de medicamento ('B') de los permitidos en un día.
too_much(B, Ag) :-
    .date(YY, MM, DD) &
    .count(consumed(YY, MM, DD, _, _, _, B, Ag), QtdB) &
    limit(B, Limit) & 
    .println(Ag," ha consumido ", QtdB, " unidades de ", B, ". Su límite es: ", Limit) &
    QtdB >= Limit. 

/* ----- RESPUESTAS A MENSAJES (Sin cambios) ----- */
answer(Request, "It will be nice to check the weather forecast, don't?.") :-
    .substring("tiempo", Request).
answer(Request, "I don't understand what are you talking about.").

/* ----- REGLAS PARA TRAER/PEDIR (Necesitan ser específicas) ----- */
// Deberían verificar disponibilidad y límites del medicamento específico

// Verifica si un medicamento específico está disponible y no se ha superado el límite
bringDrug(DrugName, Ag) :-
    available(DrugName, medCab) & 
    not too_much(DrugName, Ag). 

// Verifica si un medicamento específico NO está disponible y no se ha superado el límite
orderDrug(DrugName, Ag) :-
    not available(DrugName, medCab) & 
    not too_much(DrugName, Ag). 

// Reglas para cerveza (sin cambios, asumiendo 'beer' es el único tipo)
bringBeer(Ag) :- available(beer, fridge) & not too_much(beer, Ag).
orderBeer(Ag) :- not available(beer, fridge) & not too_much(beer, Ag).

//!run_charge_test.
//!run_charge_stop_test.
//!run_partial_charge_penalty_test.

!check_energy. // <-- AÑADE ESTA LÍNEA (Para iniciar el bucle de chequeo)


/*
+!run_charge_stop_test
   <- .print("Agente: Iniciando prueba Start/Stop...");
      start_charging;      // Ejecuta start
      .wait(2000);            // Espera 2 segundos (tiempo real, solo para separar las acciones)
      .print("Agente: Intentando detener carga...");
      stop_charging;       // Ejecuta stop
      .print("Agente: Prueba Start/Stop completada.").

+!run_charge_test
   <- start_charging.


+!run_partial_charge_penalty_test
   <- .print("--- INICIO TEST PENALIZACIÓN ---");
      // Ciclo 1
      .print("Test: Ciclo Parcial 1");
      start_charging; .wait(1000); stop_charging; .wait(500);
      // Ciclo 2
      .print("Test: Ciclo Parcial 2");
      start_charging; .wait(1000); stop_charging; .wait(500);
      // Ciclo 3
      .print("Test: Ciclo Parcial 3");
      start_charging; .wait(1000); stop_charging; .wait(500);
      // Ciclo 4 (Aquí debería aplicarse la penalización)
      .print("Test: Ciclo Parcial 4 - Esperando penalización");
      start_charging; .wait(1000); stop_charging; .wait(500);
      // Ciclo 5 (Para ver si se mantiene penalizado)
       .print("Test: Ciclo Parcial 5 - Verificando");
      start_charging; .wait(1000);
      .print("--- FIN TEST PENALIZACIÓN ---").
*/
// Plan para reaccionar a baja energía
// Se activa si la energía actual (CE) es menor que el umbral (T)
// y si NO tenemos el objetivo de cargar activo (evita lanzarlo múltiples veces)
// Plan Bucle: Energía BAJA, está LIBRE y NO está ya intentando cargar
+!check_energy[source(self)] // <--- AÑADIDO [source(self)]
    : current_energy(CE) & low_energy_threshold(T) & CE < T & // Energía baja
      free[source(self)] // Está libre
<-
    .print("¡Energía baja (", CE, "/", T, ")! [Bucle Check] Necesito cargar.");
    !charge_battery;          // <-- Lanza el objetivo de carga
    .wait(300);               // Espera (ajusta si quieres)
    !check_energy.            // Continúa el bucle (sin source, Jason lo añadirá)

// Plan Bucle: Energía OK, o está OCUPADO, o YA está intentando cargar
+!check_energy[source(self)] // <--- AÑADIDO [source(self)]
    : current_energy(CE) & low_energy_threshold(T) & // Necesitamos saber la energía y umbral
      ( CE >= T | not free[source(self)] ) // Condición: E OK, O No libre
<-
    // .print("DEBUG: Check energía OK/Ocupado/Cargando. Esperando..."); // Log opcional
    .wait(300);  // Espera (ajusta si quieres)
    !check_energy. // Continúa el bucle (sin source, Jason lo añadirá)

// Plan para lograr el objetivo de cargar la batería
+!charge_battery : free[source(self)] // Necesitamos saber dónde está el cargador
    <- .print("Iniciando secuencia de carga hacia el cargador...");
       // PASO 1: Ir a la zona del cargador
       .print("Navegando hacia la zona del cargador...");
       !at(auxiliar, cargador); // Intentar ir a la ubicación del cargador

       // PASO 2: Una vez allí, iniciar la carga
       .print("Cerca del cargador, iniciando carga...");
       start_charging; // Ejecutar la acción en el entorno

       // *** PASO 3: INICIAR MONITORIZACIÓN *** <--- AÑADIR ESTO
       .print("Carga iniciada, comenzando monitorización...").


/* ----- PLANES PARA TRAER MEDICAMENTO O CERVEZA (Modificados para ser específicos) ----- */
// Plan para traer un MEDICAMENTO ESPECÍFICO cuando se solicita (o cuando lo inicia el propio robot)
// Se activa con !has(AgenteDestino, NombreDelMedicamento)
+!has(Ag, DrugName)[source(Source)] :
    bringDrug(DrugName, Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Específico): Intentando llevar ", DrugName, " a ", Ag, " (solicitado por ", Source, ")");
    +free[source(self)];
    if (Ag == owner) { .send(owner, tell, nurse_delivering) };
    !at(enfermera, medCab);
    open(medCab);
    obtener_medicamento(DrugName);  
    if (Ag == owner) { .send(owner, tell, medicina_recogida_robot(DrugName, SimulatedHour, SimulatedMinute)) };
    .println("ENTREGA PROGRAMADA ÉXITO: Eliminando pauta para ", DrugName, " a las ", SimulatedHour, SimulatedMinute, "h.");
    .abolish(medician(DrugName, SimulatedHour, SimulatedMinute)); // Elimina creencia local de la enfermera
    close(medCab);
    !at(enfermera, Ag);
    hand_in(Ag, DrugName);

    // Registrar consumo
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, DrugName, Ag);
    .println("Registrado consumo de ", DrugName, " por ", Ag);

    if (Ag == owner) { .send(owner, untell, nurse_delivering) };
    +free[source(self)];
    .println("Robot libre después de entregar ", DrugName).

// Plan para traer CERVEZA de la nevera
+!has(Ag, beer)[source(Source)] : // <-- Añadido Source para consistencia si lo pide el robot
    bringBeer(Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Cerveza): Intentando llevar cerveza a ", Ag);
    -free[source(self)];
    // <<< ===== NUEVO: AVISO INICIO ENTREGA ===== >>>
    if (Ag == owner) { .send(owner, untell, nurse_delivering) };
    // <<< ====================================== >>>
    !at(enfermera, fridge);
    open(fridge);
    get(beer); // Asume que 'get' añade 'beer' al inventario de la enfermera
    close(fridge);
    !at(enfermera, Ag);
    hand_in(beer);
    // ?has(Ag, beer); // Esta línea parece ser una consulta interna, puede ser innecesaria aquí
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, beer, Ag);

    // <<< ===== NUEVO: AVISO FIN ENTREGA ===== >>>
    if (Ag == owner) { .send(owner, untell, nurse_delivering) };
    // <<< ==================================== >>>
    +free[source(self)].

/* ----- PLANES PARA PEDIR AL REPARTIDOR UN MEDICAMENTO O UNA CERVEZA ----- */
// Si el medicamento no está disponible, el robot lo pide al repartidor.
// Después de que el reparto se realice, el robot recoge el medicamento y lo pone en el estante.
+!has(Ag, DrugName)[source(Ag)] :
	orderDrug(Ag) & free[source(self)] <- 
		.println("SECOND RULE ====================================");
		.wait(1000);
		-free[source(self)]; 
		!at(enfermera, medCab);
		.send(repartidor, achieve, order(DrugName, 5)); 
		!at(enfermera, delivery);   
		.wait(delivered);
		!at(enfermera, medCab);     
		deliverdrug(Product,5); 	
		+available(DrugName, medCab); 
		+free[source(self)];
		.println("Trying to bring drug after order it");
		!has(Ag, DrugName)[source(Ag)]. 

// Si la cerveza no está disponible, el robot lo pide al repartidor.
// Después de que el reparto se realice, el robot recoge la cerveza y lo pone en la nevera.
+!has(Ag, beer)[source(Ag)] :
	orderBeer(Ag) & free[source(self)] <- 
		.println("SECOND RULE ====================================");
		.wait(1000);
		-free[source(self)]; 
		!at(enfermera, fridge);
		.send(repartidor, achieve, order(beer, 5)); 
		!at(enfermera, delivery);     // go to deliver area and wait there.
		.wait(delivered);
		!at(enfermera, fridge);      
		deliverbeer(Product,5);
		+available(beer, fridge); 
		+free[source(self)];
		.println("Trying to bring beer after order it");
		!has(Ag, beer)[source(Ag)].                

/* ----- MANEJO DE CONFLICTOS DE PEDIDOS ----- */
// Si el robot está ocupado y no puede atender la solicitud, informa al dueño.
+!has(Ag, DrugName)[source(Ag)] :
	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, DrugName).   

+!has(Ag, beer)[source(Ag)] :
	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, beer).   

/* ----- CONTROL DE LÍMITE DE PRODUCTOS ----- */
// Si el dueño ha alcanzado el límite de medicamentos diarios, el robot informa que no puede dar más.
+!has(Ag, DrugName)[source(Ag)] 
:  too_much(DrugName, Ag) & limit(DrugName, L) <-
		.println("FOURTH RULE ====================================");
		.wait(1000);
		.concat("The Department of Health does not allow me to give you more than ", L,
				" drugs a day! I am very sorry about that!", M);
		.send(Ag, tell, msg(M)).

// Si el dueño ha alcanzado el límite de cervezas diarias, el robot informa que no puede dar más.
+!has(Ag, beer)[source(Ag)] 
:  too_much(beer, Ag) & limit(beer, L) <-
		.println("FOURTH RULE ====================================");
		.wait(1000);
		.concat("The Department of Health does not allow me to give you more than ", L,
				" beers a day! I am very sorry about that!", M);
		.send(Ag, tell, msg(M)).

/* ----- GESTIÓN DE FALLOS EN OBJETIVOS ----- */
// Si alguna intención falla, se informa del error y de la intención actual.
-!has(Name, P)[error(E)] <-
    .println("FIFTH RULE ====================================");
    // .wait(1000); // Espera opcional
    .current_intention(I);
    .println("Failed to achieve goal: !has(", Name, " , ", P, "). Error: ", E);
    .println("Current intention is: ", I);
    // Asegurarse de notificar al owner que la enfermera ya no está ocupada (si lo estaba)
    if (Name == owner) { .send(owner, untell, nurse_delivering) };
    +free[source(self)]. // Asegurarse de que la enfermera queda libre


/* ----- ACTUALIZACIÓN DE LA LOCALIZACIÓN DEL ROBOT ----- */ // (O OBJETIVO: Ir a un lugar (!at))

// Plan si ya está en el lugar
+!at(Ag, P) : at(Ag, P) <-
    //.println(Ag, " is at ",P); // Mensaje opcional
    .wait(10). // Espera mínima

// Plan si no está en el lugar Y TIENE energía
+!at(Ag, P) : not at(Ag, P) & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- .println("[Enfermera] Yendo a ", P, "..."); // <-- Corregido/Aclarado
      !go(P); // Lanza el sub-objetivo de navegación
      !at(Ag, P). // Vuelve a intentar !at para confirmar llegada

// Plan si intenta !at con 0 energía
+!at(Ag, P) : not at(Ag, P) & current_energy(0)
   <- .println("[Enfermera] No puedo ir a ", P, ", ¡sin energía!").


/* ----- OBJETIVO: Navegar a un lugar (!go) ----- */

// Plan para ir dentro de la misma habitación
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(P).

// Plan para ir a otra habitación, conectada directamente, estando ya en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & atDoor & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(P).

// Plan para ir a otra habitación, conectada directamente, SIN estar en la puerta
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(Door);
      !go(P).

// Plan para ir a otra habitación, NO conectada directamente, SIN estar en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & not atDoor & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(DoorR);
      !go(P).

// Plan para ir a otra habitación, NO conectada directamente, ESTANDO en la puerta intermedia
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
          connect(Room, RoomP, DoorP) & atDoor & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(DoorP);
      !go(P).

// Plan Fallback (si los otros fallan pero las habitaciones son distintas)
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP & current_energy(CE) & CE > 0 // <-- AÑADIDO chequeo energía
   <- move_towards(P).

// Plan de fallo si no puede moverse por falta de energía
-!go(P)[error(E)] : current_energy(0)
   <- .println("[Enfermera] Fallo al intentar ir a ", P, " por falta de energía (0). Error: ", E).

// Plan de llegada
-!go(P) <-
   .println("[Enfermera] He llegado a ", P, "."). // <-- Corregido/Aclarado
   																		
/* ----- MANEJO DE ENTREGA ----- */
// Cuando el repartidor realiza una entrega, el robot actualiza su estado y vuelve a intentar cumplir la tarea.
+delivered(DrugName, _Qtd, _OrderId)[source(repartidor)]
:  true
<- +delivered;
	.wait(2000). 

+delivered(beer, _Qtd, _OrderId)[source(repartidor)]
:  true
<- +delivered;
	.wait(2000).	

/* ----- ACTUALIZACIÓN DE DISPONIBILIDAD DE PRODUCTOS ----- */
// Cuando el stock de medicamentos cambia, el robot actualiza su disponibilidad.
+stock(DrugName, 0)
:  available(DrugName, medCab)
<- -available(DrugName, medCab). 
	
+stock(DrugName, N)
:  N > 0 & not available(DrugName, medCab)
<- +available(DrugName, medCab).     
	
+stock(beer, 0)
:  available(beer, fridge)
<- -available(beer, fridge). 
	
+stock(beer, N)
:  N > 0 & not available(beer, fridge)
<- +available(beer, fridge).   

/* ----- GESTIÓN DE CHAT ----- */
// El robot responde a los mensajes de chat enviados por el dueño.
+chat(Msg)[source(Ag)] : answer(Msg, Answ) <-  
	.println("El agente ", Ag, " me ha chateado: ", Msg);
	.send(Ag, tell, msg(Answ)). 


	
/* ----- ##### GESTIÓN DE NOTIFICACIÓN DE CONSUMO (MODIFICADO CON VERIFICACIÓN SIMULADA) ##### ----- */

//OBJETIVOS PARA COMPROBAR VERACIDAD OWNER TOMA MEDICAMENTOS

// Objetivo para comprobar medicamentos, en caso de que owner hubiera consumido uno
+medication_consumed(DrugName, SimulatedHour, SimulatedMinute)[source(Ag)] : free[source(self)] <-
    -free;     
	.println("I received a message from owner tell me he take '", DrugName, "', so let´s comprobate");
    .abolish(medician(DrugName, SimulatedHour, SimulatedMinute)); // Eliminar pauta local de la enfermera
    !at(enfermera, medCab); 
	open(medCab);
	!checkVericity(DrugName, SimulatedHour, SimulatedMinute);
    +free. 

// En caso de que robot este ocupado, lo intenta de nuevo mas adelante
+medication_consumed(DrugName, SimulatedHour, SimulatedMinute)[source(owner)]: not free[source(self)] <-
    .println("I received a message from owner who tell me he consumed '", DrugName, "' but I´m busy... I will try later");
	.wait(500);
	!medication_consumed(DrugName, SimulatedHour, SimulatedMinute).

// POSIBLES OPCIONES DE COMPROBACION
//1.-VERACIDAD

// Este objetivo actualiza su libreta en caso de que se haya DECREMENTADO las dosis
+!checkVericity(DrugName, SimulatedHour, SimulatedMinute): note(DrugName, N) & checkMedCab(DrugName, N) <-
	.println("Ummm, looks like the owner is telling the truth and he take the drug. I´m going to update me!!!");
	-note(DrugName, N);
	+note(DrugName, N - 1);
	.concat("Looks like you tell me the truth and take '",DrugName,"' of ", SimulatedHour, SimulatedMinute,"... Thanks for help me!!! :)", M);
	.send("owner", tell, message(M));
	close(medCab).


//2.-FALSEDAD
// Este objetivo, al no haber suministrado owner el medicamento, lo suministra robot
+!checkVericity(DrugName, SimulatedHour, SimulatedMinute): note(DrugName, N) & not checkMedCab(DrugName, N) <-
	.println("HE LIES ME!!!! He didn´t take '", DrugName, "'.... okay, let's bring the medician");
    obtener_medicamento(DrugName);
    close(medCab);
    .send(owner, tell, medicina_recogida_robot(DrugName, SimulatedHour, SimulatedMinute));
    !at(enfermera, owner);
    hand_in(owner, DrugName);
	.concat("Let this be the last time you lie. Are you understand??", M);
	.send("owner", tell, message(M)).

// Este plan ahora espera DrugName y SimulatedHour, SimulatedMinute enviados por el owner
+medicina_recogida_owner(DrugName, SimulatedHour, SimulatedMinute)[source(owner)] <-
    .print("DEBUG: Creencia medicina_recogida_owner(", DrugName, ",", SimulatedHour, SimulatedMinute, ") RECIBIDA de ", owner);
    .println("El owner ha sido mas rapido que yo con ", DrugName, ", asi que procedo a retirar mi accion");
    .drop_all_intentions;
    .abolish(medician(DrugName, SimulatedHour, SimulatedMinute)); // <-- ¡¡ELIMINAR LA PAUTA LOCAL DE LA ENFERMERA!!
    .println("Enfermera: Pauta para ", DrugName, " a las ", SimulatedHour, SimulatedMinute, "h eliminada de mi horario (owner fue más rápido).");
    -medicina_recogida_owner(DrugName, SimulatedHour, SimulatedMinute)[source(owner)]; // Eliminar creencia específica
    +free[source(self)]. // Marcarse como libre

/* ----- RECEPCIÓN DE PAUTAS DE MEDICACIÓN ----- */
// Al recibir una pauta del owner, la añade a sus creencias.
+medician(M, H)[source(owner)] <-
    +medician(M, H);
    .println("Pauta recibida y almacenada: Tomar ", M, " a las ", H, "h.").

+!clear_schedule // Forma más simple si no necesitas saber quién lo envió
    : true // Condición de contexto: siempre aplicable cuando se recibe el objetivo
<-
    .println("Enfermera: Recibida orden para borrar el horario de medicación.");

    .abolish(medician(_, _));

    .println("Enfermera: Todas las pautas de medicación (creencias 'medician') han sido eliminadas.").


// Plan para revisar periódicamente la pauta de medicación (USA HORA SIMULADA)
+clock(SimulatedHour, SimulatedMinute)[source(Source)] : free[source(self)] <-
    //.println("PLAN REACTIVO (Clock ", SimulatedHour, SimulatedMinute, "): Revisando pauta...");

 if (not ha_caducado(DrugToDeliver)) {
    if (medician(DrugToDeliver, SimulatedHour, SimulatedMinute)) {
       // .println("PLAN REACTIVO (Clock ", SimulatedHour, SimulatedMinute, "): Pauta encontrada: Entregar ", DrugToDeliver, " a owner.");
    

        if (not too_much(DrugToDeliver, owner)) {
                .println("PLAN REACTIVO (Clock ", SimulatedHour, SimulatedMinute, "): Intentando iniciar entrega de ", DrugToDeliver);
                // Lanzar el objetivo de entrega estándar. Los planes +!has / -!has se encargarán del resto.
                !has(owner, DrugToDeliver)[source(self)];
                // <<< YA NO SE ABOLISH NI SEND DESDE AQUÍ >>>
        }else{
            .println("PLAN REACTIVO (Clock ", SimulatedHour, SimulatedMinute, "): No se puede entregar ", DrugToDeliver, ": Límite de consumo alcanzado.");
        }
     
    } else {
        //.println("PLAN REACTIVO (Clock ", SimulatedHour, SimulatedMinute, "): No hay medicación pautada para esta hora.");
    };
    } else {
        //.println("Enfermera: La medicacion esta caducada.");
    }.
    

// Plan alternativo si no está libre (SIN CAMBIOS)
+clock(SimulatedHour, SimulatedMinute)[source(Source)] : not free[source(self)] <-
     .wait(1000); // Espera un poco si está ocupado
     +clock(SimulatedHour, SimulatedMinute)[source(Source)]. // Reintenta revisar el reloj (o +!check_schedule si usas ese patrón)


/* ----- ACTUALIZACIÓN DE LA HORA ----- */
// El robot puede verificar la hora actual.                  
+?time : true
	<-  watchClock.


+orden_eliminar_caducaciones[source(auxiliar)] <-
    .println("Agente B: Recibida señal de ", auxiliar, " para eliminar la caducacion.");
    .abolish(ha_caducado(_));
    .println("Agente B: Caducacion eliminada.");
    -orden_eliminar_caducaciones[source(auxiliar)]; // Limpia la creencia señal
    .