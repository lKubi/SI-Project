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
available("Paracetamol 500mg", medCab).
available("Ibuprofeno 600mg", medCab).
available("Amoxicilina 500mg", medCab).
available("Omeprazol 20mg", medCab).
available("Loratadina 10mg", medCab).

available(beer, fridge).

/* ----- LÍMITES DE CONSUMO (Específicos) ----- */
// Establecer límites para cada cerveza.

limit(beer, 5).

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

/* ----- ##### NUEVO: PLANES PARA REVISIÓN PROACTIVA DE PAUTA (MODIFICADO) ##### ----- */

// Plan para revisar periódicamente la pauta de medicación (USA HORA y MINUTO SIMULADOS)
// Plan para revisar periódicamente la pauta de medicación (USA HORA SIMULADA)
+clock(SimulatedHour)[source(Source)] : free[source(self)] <-
    .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Revisando pauta...");
    if (medician(DrugToDeliver, SimulatedHour)) {
        .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Pauta encontrada: Entregar ", DrugToDeliver, " a owner.");
        if (not too_much(DrugToDeliver, owner)) {
            if (available(DrugToDeliver, medCab)) {
                .println("PLAN REACTIVO (Clock ", SimulatedHour, "): Intentando iniciar entrega de ", DrugToDeliver);
                // *** NUEVO: Añadir creencia temporal ANTES de lanzar !has ***
                // Esta creencia marca que la siguiente ejecución de !has es para una entrega programada.
                +is_scheduled_delivery(DrugToDeliver, owner, SimulatedHour);
                // Lanzar el objetivo de entrega estándar. Los planes +!has / -!has se encargarán del resto.
                !has(owner, DrugToDeliver)[source(self)];
                // <<< YA NO SE ABOLISH NI SEND DESDE AQUÍ >>>
            } else {
                .println("PLAN REACTIVO (Clock ", SimulatedHour, "): No se puede entregar ", DrugToDeliver, ": No disponible en ", medCab);
                // Opcional: intentar pedirlo si no está disponible? !orderDrug(...) ?
            }
        } else {
            .println("PLAN REACTIVO (Clock ", SimulatedHour, "): No se puede entregar ", DrugToDeliver, ": Límite diario alcanzado.");
        }
    } else {
        .println("PLAN REACTIVO (Clock ", SimulatedHour, "): No hay medicación pautada para esta hora.");
    }.

// Plan alternativo si no está libre (SIN CAMBIOS)
+clock(SimulatedHour)[source(Source)] : not free[source(self)] <-
     .wait(1000); // Espera un poco si está ocupado
     +clock(SimulatedHour)[source(Source)]. // Reintenta revisar el reloj (o +!check_schedule si usas ese patrón)


/* ----- PLANES PARA TRAER MEDICAMENTO O CERVEZA (Modificados para ser específicos) ----- */

// Plan para traer un MEDICAMENTO ESPECÍFICO cuando se solicita (o cuando lo inicia el propio robot)
// Se activa con !has(AgenteDestino, NombreDelMedicamento)
+!has(Ag, DrugName)[source(Source)] :
    bringDrug(DrugName, Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Específico): Intentando llevar ", DrugName, " a ", Ag, " (solicitado por ", Source, ")");
    -free[source(self)];
    if (Ag == owner) { .send(owner, tell, nurse_is_delivering) };
    !at(enfermera, medCab);
    open(medCab);
    obtener_medicamento(DrugName); // Acción que puede fallar
    close(medCab);
    !at(enfermera, Ag);
    hand_in(DrugName);

    // Registrar consumo
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, DrugName, Ag);
    .println("Registrado consumo de ", DrugName, " por ", Ag);

    // *** NUEVO: Limpieza de pauta SOLO SI era programada Y tuvo éxito ***
    if (is_scheduled_delivery(DrugName, Ag, Hour)) {
        .println("ENTREGA PROGRAMADA ÉXITO: Eliminando pauta para ", DrugName, " a las ", Hour, "h.");
        .abolish(medician(DrugName, Hour)); // Elimina creencia local de la enfermera
        .send(Ag, achieve, remove_my_medician(DrugName, Hour)); // Pide al owner eliminar la suya
        // Eliminar la creencia temporal de la enfermera
        -is_scheduled_delivery(DrugName, Ag, Hour);
    };

    if (Ag == owner) { .send(owner, tell, nurse_finished_delivering) };
    +free[source(self)];
    .println("Robot libre después de entregar ", DrugName).

// Plan para traer CERVEZA de la nevera
+!has(Ag, beer)[source(Source)] : // <-- Añadido Source para consistencia si lo pide el robot
    bringBeer(Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Cerveza): Intentando llevar cerveza a ", Ag);
    -free[source(self)];
    // <<< ===== NUEVO: AVISO INICIO ENTREGA ===== >>>
    if (Ag == owner) { .send(owner, tell, nurse_is_delivering) };
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
    if (Ag == owner) { .send(owner, tell, nurse_finished_delivering) };
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

    // *** NUEVO: Limpieza de creencia temporal SI era programada Y falló ***
    // Crucial: NO eliminar la pauta 'medician' si la entrega falló.
    if (is_scheduled_delivery(P, Name, Hour)) {
         .println("ENTREGA PROGRAMADA FALLO: NO se elimina pauta para ", P, " a las ", Hour, "h.");
         // Solo eliminar la creencia temporal
         -is_scheduled_delivery(P, Name, Hour);
    };

    // Asegurarse de notificar al owner que la enfermera ya no está ocupada (si lo estaba)
    if (Name == owner) { .send(owner, tell, nurse_finished_delivering) };
    +free[source(self)]. // Asegurarse de que la enfermera queda libre


/* ----- ACTUALIZACIÓN DE LA LOCALIZACIÓN DEL ROBOT ----- */
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

/* ----- ACTUALIZACIÓN DE LA HORA ----- */
// El robot puede verificar la hora actual.                  
+?time : true
	<-  watchClock.
	
/* ----- ##### GESTIÓN DE NOTIFICACIÓN DE CONSUMO (MODIFICADO CON VERIFICACIÓN SIMULADA) ##### ----- */

// Cuando el dueño informa que ha consumido un medicamento ESPECÍFICO y el robot está libre:
// Robot actualiza pauta y lanza la verificación (simulada) para ese medicamento.
+medication_consumed(DrugName, Hour)[source(Ag)] : free[source(self)] <-
    .println("Notificación recibida: ", Ag, " dice haber tomado ", DrugName, " (pauta de las ", Hour, "h).");
    -free[source(self)]; // <-- Robot se ocupa para ir a verificar
    // Acción Inmediata: Eliminar la pauta correspondiente de las creencias del robot
    .abolish(medician(DrugName, Hour));
    .println("Robot: Pauta para ", DrugName, " a las ", Hour, "h eliminada de mi horario.");
    .println("Robot: Iniciando plan para verificar el consumo de '", DrugName, "' en medCab."); // Log usa nombre específico
    // Disparar la verificación específica para DrugName
    !verify_consumption(Ag, DrugName). // <-- Pasa DrugName específico al plan de verificación

// Si el robot está ocupado cuando recibe la notificación ESPECÍFICA:
// Actualiza la pauta inmediatamente pero informa al dueño que verificará más tarde.
+medication_consumed(DrugName, Hour)[source(Ag)] : not free[source(self)] <-
    .println("Recibí notificación de consumo de ", DrugName, " (pauta de las ", Hour, "h) por ", Ag, ", pero estoy ocupado con otra tarea.");
    // Acción Inmediata: Eliminar la pauta de las creencias del robot, incluso estando ocupado
    .abolish(medician(DrugName, Hour));
    .println("Robot: Pauta para ", DrugName, " a las ", Hour, "h eliminada de mi horario (mientras estaba ocupado).");
    // Informar al dueño que se recibió y actualizó, pero la verificación será más tarde.
    .send(Ag, tell, msg("Recibí tu notificación sobre ", DrugName, " de las ", Hour, "h y actualicé mi horario. Verificaré el consumo en el botiquín cuando termine mi tarea actual.")).
    // NOTA: No llamamos a !verify_consumption aquí porque está ocupado.
    // Se podría añadir opcionalmente !!verify_consumption(Ag, DrugName) para ponerlo en cola si se desea.

/* ----- ##### PLAN PARA VERIFICAR EL CONSUMO (MODIFICADO) ##### ----- */
// Plan AHORA recibe el nombre específico del medicamento (DrugName)
+!verify_consumption(Ag, DrugName) <- // <-- Trigger modificado, recibe DrugName
    .println("Verificando consumo de '", DrugName, "' en ", medCab, " solicitado por ", Ag); // <-- Log actualizado
    .println("Llegué a ", medCab, ". Realizando verificación de stock de '", DrugName,"'."); // <-- Log actualizado

    // --- Inicio: Simulación/Acción de Verificación ---
    // ESTA PARTE SIGUE SIENDO UNA SIMULACIÓN. NO COMPRUEBA REALMENTE EL STOCK.
    // Para una verificación real, necesitarías interactuar con el entorno aquí.
    .println("Robot: [Simulación] Buscando/Contando unidades de ", DrugName, "...");
    .wait(3000); // Simula tiempo de chequeo
    .println("Verificación de stock simulada para '", DrugName, "' completada."); // <-- Log actualizado
    // --- Fin: Simulación/Acción de Verificación ---

    .println("Verificación finalizada para ", DrugName, ". Enviando confirmación a ", Ag);
    // Mensaje de confirmación final (ahora menciona el medicamento verificado)
    .send(Ag, tell, msg("He verificado en el estante de medicación respecto a ", DrugName, ". ¡Gracias por informarme!")); // <-- Mensaje actualizado
    +free[source(self)]; // <-- Libera robot DESPUÉS de verificar
    .println("Robot libre después de verificar consumo de ", DrugName, ".").

// Plan de fallo para la verificación (MODIFICADO)
// Ahora también usa DrugName
-!verify_consumption(Ag, DrugName)[error(E)] <- // <-- Trigger modificado
    .println("¡ERROR al verificar el consumo de ", DrugName, " para ", Ag, "! Error: ", E); // <-- Log actualizado
    .send(Ag, tell, msg("Tuve un problema al intentar verificar el consumo de ", DrugName, ". Por favor, revisa manualmente.")); // <-- Mensaje actualizado
    +free[source(self)]. // <-- Asegura liberar robot en caso de error


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

