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
<<<<<<< Updated upstream
// Establecer límites para cada medicamento específico y cerveza.

limit(beer, 5).

/* ----- OBJETIVOS INICIALES ----- */
// Añadimos el objetivo para que el robot empiece a revisar la pauta de medicación
!check_schedule.

=======
// Establecer límites para cada cerveza.

limit(beer, 5).

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
/* ----- ##### NUEVO: PLANES PARA REVISIÓN PROACTIVA DE PAUTA ##### ----- */

<<<<<<< Updated upstream
// Plan para revisar periódicamente la pauta de medicación
/* ----- ##### NUEVO: PLANES PARA REVISIÓN PROACTIVA DE PAUTA ##### ----- */

// Plan para revisar periódicamente la pauta de medicación (USA HORA SIMULADA) - CON LOGGING ADICIONAL
+!check_schedule : free[source(self)] <-
    .println("--- Check Schedule Cycle ---"); // Marca inicio del ciclo
    .println("Checking if free: YES");
=======
// Plan para revisar periódicamente la pauta de medicación (USA HORA SIMULADA)
+!check_schedule : free[source(self)] <-

>>>>>>> Stashed changes
    // Primero, verifica que la creencia 'clock' exista antes de intentar usarla
    if (clock(SimulatedHour)) {
        .println("Hora simulada actual percibida: ", SimulatedHour);

<<<<<<< Updated upstream
        // *** PASO DE DEPURACIÓN: Listar todas las creencias 'medician' existentes ***
        .findall(medician(Drug, Hour), medician(Drug, Hour), MedList);
        .println("Creencias 'medician' actuales en la base de creencias: ", MedList);
        // *** FIN PASO DE DEPURACIÓN ***

        // Busca si hay alguna medicación pautada para ESTA HORA SIMULADA
        // (Busca sin anotación de fuente, como discutimos)
=======
        .findall(medician(Drug, Hour), medician(Drug, Hour), MedList);

        // Busca si hay alguna medicación pautada para ESTA HORA SIMULADA
>>>>>>> Stashed changes
        if (medician(DrugToDeliver, SimulatedHour)) {
             // Si entra aquí, encontró una coincidencia
            .println("Pauta encontrada para la hora SIMULADA ", SimulatedHour, ": Entregar ", DrugToDeliver, " a owner.");
            // Verificar límite ANTES de intentar entregar
            if (not too_much(DrugToDeliver, owner)) {
                // Verificar disponibilidad ANTES de intentar entregar
                if (available(DrugToDeliver, medCab)) {
                    .println("Intentando entregar ", DrugToDeliver);
<<<<<<< Updated upstream
                    !has(owner, DrugToDeliver)[source(self)]; // El robot inicia la acción
=======
                    !has(owner, DrugToDeliver)[source(self)]; 
>>>>>>> Stashed changes
                } else {
                    .println("No se puede entregar ", DrugToDeliver, ": No disponible en ", medCab);
                    // Opcional: intentar pedirlo si no está disponible
                }
            } else {
                .println("No se puede entregar ", DrugToDeliver, ": Límite diario alcanzado.");
            }
        } else {
            // Si entra aquí, NO encontró coincidencia para la hora actual
            .println("No hay medicación pautada para la hora SIMULADA ", SimulatedHour);
        };

    } else {
         // Si entra aquí, la creencia 'clock(Hora)' no existe en este momento
    }
<<<<<<< Updated upstream
    .wait(1000); // Espera 1 segundo real (ajusta si es necesario para tu simulación)
=======
    .wait(1000); // Espera 1 segundo real 
    !check_schedule.

// Plan alternativo si no está libre 
+!check_schedule : not free[source(self)] <-
    .wait(5000); // Espera 5 segundos si está ocupado 
>>>>>>> Stashed changes
    !check_schedule.

// Plan alternativo si no está libre (también con logging para claridad)
+!check_schedule : not free[source(self)] <-
    .println("--- Check Schedule Cycle ---");
    .println("Checking if free: NO (Robot ocupado)");
    .println("--- End Check Schedule Cycle ---");
    .wait(5000); // Espera 5 segundos si está ocupado (ajusta si es necesario)
    !check_schedule.
/* ----- PLANES PARA TRAER MEDICAMENTO O CERVEZA (Modificados para ser específicos) ----- */

// Plan para traer un MEDICAMENTO ESPECÍFICO cuando se solicita (o cuando lo inicia el propio robot)
// Se activa con !has(AgenteDestino, NombreDelMedicamento)
+!has(Ag, DrugName)[source(Source)] : // Source puede ser Ag (owner) o self (robot)
    bringDrug(DrugName, Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Específico): Intentando llevar ", DrugName, " a ", Ag, " (solicitado por ", Source, ")");
    -free[source(self)]; 
    !at(enfermera, medCab); 
    open(medCab);
    obtener_medicamento(DrugName);
    close(medCab);
    !at(enfermera, Ag);
    hand_in(drug); 

=======
/* ----- ##### NUEVO: PLANES PARA REVISIÓN PROACTIVA DE PAUTA (MODIFICADO) ##### ----- */

// Plan para revisar periódicamente la pauta de medicación (USA HORA y MINUTO SIMULADOS)
+clock(SimulatedHour)[source(Source)] : free[source(self)] <- // Solo actúa si está libre
    .println("PLAN REACTIVO: Hora simulada actualizada a ", SimulatedHour, ". Revisando pauta...");

    // --- Lógica de comprobación (la misma que tenías dentro de !check_schedule) ---
    // Busca si hay alguna medicación pautada para ESTA HORA SIMULADA
    if (medician(DrugToDeliver, SimulatedHour)) {
        // Si entra aquí, encontró una coincidencia
       .println("PLAN REACTIVO: Pauta encontrada para la hora ", SimulatedHour, ": Entregar ", DrugToDeliver, " a owner.");
       // Verificar límite ANTES de intentar entregar
       if (not too_much(DrugToDeliver, owner)) {
           // Verificar disponibilidad ANTES de intentar entregar
           if (available(DrugToDeliver, medCab)) {
               .println("PLAN REACTIVO: Intentando entregar ", DrugToDeliver);
               !has(owner, DrugToDeliver)[source(self)]; // Lanza el objetivo de entrega
               .abolish(medician(DrugToDeliver, SimulatedHour)); // Elimina la pauta procesada
            .send(owner, achieve, remove_my_medician(DrugToDeliver, SimulatedHour));
               .println("PLAN REACTIVO: Pauta para ", DrugToDeliver, " a las ", SimulatedHour, "h eliminada del horario.");
           } else {
               .println("PLAN REACTIVO: No se puede entregar ", DrugToDeliver, ": No disponible en ", medCab);
               // Opcional: intentar pedirlo si no está disponible
           }
       } else {
           .println("PLAN REACTIVO: No se puede entregar ", DrugToDeliver, ": Límite diario alcanzado.");
       }
    } else {
        // Si entra aquí, NO encontró coincidencia para la hora actual
        .println("PLAN REACTIVO: No hay medicación pautada para la hora SIMULADA ", SimulatedHour);
    }.
// Plan alternativo si no está libre (sin cambios necesarios aquí)
+!check_schedule : not free[source(self)] <-
    .wait(5000); // Espera 5 segundos si está ocupado
    !check_schedule.

/* ----- PLANES PARA TRAER MEDICAMENTO O CERVEZA (Modificados para ser específicos) ----- */

// Plan para traer un MEDICAMENTO ESPECÍFICO cuando se solicita (o cuando lo inicia el propio robot)
// Se activa con !has(AgenteDestino, NombreDelMedicamento)
+!has(Ag, DrugName)[source(Source)] : // Source puede ser Ag (owner) o self (robot)
    bringDrug(DrugName, Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Específico): Intentando llevar ", DrugName, " a ", Ag, " (solicitado por ", Source, ")");
    -free[source(self)]; 
    !at(enfermera, medCab); 
    open(medCab);
    obtener_medicamento(DrugName);
    close(medCab);
    !at(enfermera, Ag);
    hand_in(drug); 

>>>>>>> Stashed changes
    // Registrar consumo específico DESPUÉS de entregar
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, DrugName, Ag); 
    .println("Registrado consumo de ", DrugName, " por ", Ag);
    +free[source(self)]; // Marcar como libre
    .println("Robot libre después de entregar ", DrugName).

// Plan para traer CERVEZA de la nevera
+!has(Ag, beer)[source(Ag)] :
    bringBeer(Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Cerveza): Intentando llevar cerveza a ", Ag);
    -free[source(self)];
    !at(enfermera, fridge);
    open(fridge);
    get(beer);
    close(fridge);
    !at(enfermera, Ag);
    hand_in(beer); 
    // ?has(Ag, beer);
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, beer, Ag); 
    +free[source(self)].

/* ----- PLANES PARA PEDIR AL REPARTIDOR UN MEDICAMENTO O UNA CERVEZA ----- */
// Si el medicamento no está disponible, el robot lo pide al repartidor.
// Después de que el reparto se realice, el robot recoge el medicamento y lo pone en el estante.
+!has(Ag, drug)[source(Ag)] :
	orderDrug(Ag) & free[source(self)] <- 
		.println("SECOND RULE ====================================");
		.wait(1000);
		-free[source(self)]; 
		!at(enfermera, medCab);
		.send(repartidor, achieve, order(drug, 5)); 
		!at(enfermera, delivery);   
		.wait(delivered);
		!at(enfermera, medCab);     
		deliverdrug(Product,5); 	
		+available(drug, medCab); 
		+free[source(self)];
		.println("Trying to bring drug after order it");
		!has(Ag, drug)[source(Ag)]. 

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
+!has(Ag, drug)[source(Ag)] :
	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, drug).   

+!has(Ag, beer)[source(Ag)] :
	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, beer).   

/* ----- CONTROL DE LÍMITE DE PRODUCTOS ----- */
// Si el dueño ha alcanzado el límite de medicamentos diarios, el robot informa que no puede dar más.
+!has(Ag, drug)[source(Ag)] 
:  too_much(drug, Ag) & limit(drug, L) <-
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
-!has(Name, P) <-
	//: true
	.println("FIFTH RULE ====================================");
	.wait(1000);
	.current_intention(I);
	.println("Failed to achieve goal: !has(", Name, " , ", P, ").");
	.println("Current intention is: ", I).

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
+delivered(drug, _Qtd, _OrderId)[source(repartidor)]
:  true
<- +delivered;
	.wait(2000). 

+delivered(beer, _Qtd, _OrderId)[source(repartidor)]
:  true
<- +delivered;
	.wait(2000).	

/* ----- ACTUALIZACIÓN DE DISPONIBILIDAD DE PRODUCTOS ----- */
// Cuando el stock de medicamentos cambia, el robot actualiza su disponibilidad.
+stock(drug, 0)
:  available(drug, medCab)
<- -available(drug, medCab). 
	
+stock(drug, N)
:  N > 0 & not available(drug, medCab)
<- +available(drug, medCab).     
	
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

<<<<<<< Updated upstream
<<<<<<< Updated upstream
	/* ----- ACTUALIZACIÓN DE LA HORA ----- */
	// El robot puede verificar la hora actual.                  
    +?time : true
=======
/* ----- ACTUALIZACIÓN DE LA HORA ----- */
// El robot puede verificar la hora actual.                  
+?time : true
>>>>>>> Stashed changes
	<-  watchClock.

/* ----- ##### GESTIÓN DE NOTIFICACIÓN DE CONSUMO ##### ----- */

// Cuando el dueño informa que ha consumido un medicamento y el robot está libre:
+medication_consumed(drug)[source(Ag)] : free[source(self)] <-
    .println("Notificación recibida: ", Ag, " dice haber tomado ", drug);
    -free[source(self)]; // Marca al robot como ocupado
    .println("Iniciando plan para verificar el consumo en medCab.");
    !verify_consumption(Ag, drug). // Inicia el objetivo de verificación

// Si el robot está ocupado cuando recibe la notificación:
+medication_consumed(drug)[source(Ag)] : not free[source(self)] <-
    .println("Recibí notificación de consumo de ", drug, " por ", Ag, ", pero estoy ocupado. Lo verificaré más tarde.");
    .send(Ag, tell, msg("Recibí tu notificación sobre ", drug, ", pero estoy ocupado. Lo verificaré en cuanto pueda.")).

/* ----- ##### PLAN PARA VERIFICAR EL CONSUMO ##### ----- */
+!verify_consumption(Ag, drug) <-
    .println("Verificando consumo de ", drug, " en ", medCab, " solicitado por ", Ag);
    !at(enfermera, medCab); // Paso 1: Ir a la ubicación del drug
    .println("Llegué a ", medCab, ". Realizando verificación de stock de ", drug);

    // --- Inicio: Simulación/Acción de Verificación ---
	
    .wait(3000); 
    .println("Verificación de stock para ", drug, " completada (simulada).");

    // --- Fin: Simulación/Acción de Verificación ---

    .println("Verificación finalizada. Enviando confirmación a ", Ag);
    .send(Ag, tell, msg("He verificado en el estante de medicacion que has tomado ", drug, ". ¡Gracias por informarme!")); // Mensaje de confirmación final
    +free[source(self)]; // Libera robot *tras* disparar chequeo
    .println("Robot libre después de verificar consumo.").

// Plan de fallo para la verificación
-!verify_consumption(Ag, drug)[error(E)] <-
    .println("¡ERROR al verificar el consumo de ", drug, " para ", Ag, "! Error: ", E);
    .send(Ag, tell, msg("Tuve un problema al intentar verificar el consumo de ", drug, ". Por favor, revisa manualmente."));
    +free[source(self)].
=======
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

>>>>>>> Stashed changes

/* ----- RECEPCIÓN DE PAUTAS DE MEDICACIÓN ----- */
// Al recibir una pauta del owner, la añade a sus creencias.
+medician(M, H)[source(owner)] <-
    +medician(M, H);
<<<<<<< Updated upstream
    .println("Pauta recibida y almacenada: Tomar ", M, " a las ", H, "h.").
=======
    .println("Pauta recibida y almacenada: Tomar ", M, " a las ", H, "h.").

+!clear_schedule // Forma más simple si no necesitas saber quién lo envió
    : true // Condición de contexto: siempre aplicable cuando se recibe el objetivo
<-
    .println("Enfermera: Recibida orden para borrar el horario de medicación.");

    .abolish(medician(_, _));

    .println("Enfermera: Todas las pautas de medicación (creencias 'medician') han sido eliminadas.").
>>>>>>> Stashed changes
