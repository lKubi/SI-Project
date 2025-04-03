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
// Ejemplo: Si Paracetamol empieza con 1 unidad. Los demás con 0.
available("Paracetamol 500mg", medCab).
available("Ibuprofeno 600mg", medCab).
available("Amoxicilina 500mg", medCab).
available("Omeprazol 20mg", medCab).
available("Loratadina 10mg", medCab).

// available("Ibuprofeno 600mg", medCab). // Comentado si empieza en 0
// available("Amoxicilina 500mg", medCab). // Comentado si empieza en 0
// ... añadir el resto según el estado inicial del HashMap en HouseModel ...
available(beer, fridge). // Asumiendo que empieza con 1 cerveza

/* ----- LÍMITES DE CONSUMO (Específicos) ----- */
// Establecer límites para cada medicamento específico y cerveza.
limit("Paracetamol 500mg", 1). // Ejemplo: Máximo 2 paracetamoles al día
limit("Ibuprofeno 600mg", 1). // Ejemplo
limit("Amoxicilina 500mg", 1). // Ejemplo
limit("Omeprazol 20mg", 1).    // Ejemplo
limit("Loratadina 10mg", 1).    // Ejemplo
limit(beer, 5).

/* ----- OBJETIVOS INICIALES ----- */
// Añadimos el objetivo para que el robot empiece a revisar la pauta de medicación
!check_schedule.

/* ----- REGLA DE CONSUMO EXCESIVO (Verifica droga específica 'B') ----- */
// Esta regla verifica si el dueño ha consumido más de un tipo específico de medicamento ('B') de los permitidos en un día.
too_much(B, Ag) :-
    .date(YY, MM, DD) &
    // Cuenta cuántas veces se ha consumido el item específico B hoy
    .count(consumed(YY, MM, DD, _, _, _, B, Ag), QtdB) &
    limit(B, Limit) & // Obtiene el límite para ese item específico B
    .println(Ag," ha consumido ", QtdB, " unidades de ", B, ". Su límite es: ", Limit) &
    QtdB >= Limit. // Si la cantidad consumida es MAYOR O IGUAL al límite

/* ----- RESPUESTAS A MENSAJES (Sin cambios) ----- */
answer(Request, "It will be nice to check the weather forecast, don't?.") :-
    .substring("tiempo", Request).
answer(Request, "I don't understand what are you talking about.").

/* ----- REGLAS PARA TRAER/PEDIR (Necesitan ser específicas) ----- */
// Deberían verificar disponibilidad y límites del medicamento específico

// Verifica si un medicamento específico está disponible y no se ha superado el límite
bringDrug(DrugName, Ag) :-
    available(DrugName, medCab) & // Verifica disponibilidad específica
    not too_much(DrugName, Ag). // Verifica límite específico

// Verifica si un medicamento específico NO está disponible y no se ha superado el límite
orderDrug(DrugName, Ag) :-
    not available(DrugName, medCab) & // Verifica disponibilidad específica
    not too_much(DrugName, Ag). // Verifica límite específico

// Reglas para cerveza (sin cambios, asumiendo 'beer' es el único tipo)
bringBeer(Ag) :- available(beer, fridge) & not too_much(beer, Ag).
orderBeer(Ag) :- not available(beer, fridge) & not too_much(beer, Ag).

/* ----- ##### NUEVO: PLANES PARA REVISIÓN PROACTIVA DE PAUTA ##### ----- */

// Plan para revisar periódicamente la pauta de medicación
+!check_schedule : free[source(self)] <- // Solo revisa si está libre
    .println("Revisando pauta de medicación...");
    .time(HH, _, _); // Obtiene la hora actual
    // Busca si hay alguna medicación pautada para esta hora (usando creencias recibidas del owner)
    if (medician(DrugToDeliver, HH)[source(owner)]) {
        .println("Pauta encontrada para la hora ", HH, ": Entregar ", DrugToDeliver, " a owner.");
        // Verificar límite ANTES de intentar entregar
        if (not too_much(DrugToDeliver, owner)) {
             // Verificar disponibilidad ANTES de intentar entregar
             if (available(DrugToDeliver, medCab)) {
                  .println("Intentando entregar ", DrugToDeliver);
                  // Intenta lograr el objetivo de que el owner tenga el medicamento específico
                  // Esto activará los planes +!has(owner, DrugToDeliver)
                  !has(owner, DrugToDeliver)[source(self)]; // El robot inicia la acción
             } else {
                  .println("No se puede entregar ", DrugToDeliver, ": No disponible en ", medCab);
                  // Opcional: intentar pedirlo si no está disponible
                  // if (orderDrug(DrugToDeliver, owner)) { !order_item(DrugToDeliver); }
             }
        } else {
            .println("No se puede entregar ", DrugToDeliver, ": Límite diario alcanzado.");
        }
    } else {
        .println("No hay medicación pautada para la hora ", HH);
    };
    .wait(20000); // Espera 1 minuto (60000 ms) antes de volver a revisar
    !check_schedule. // Llama recursivamente para seguir revisando

// Si está ocupado, espera menos tiempo y vuelve a intentarlo
+!check_schedule : not free[source(self)] <-
    .println("Robot ocupado, posponiendo revisión de pauta.");
    .wait(30000); // Espera 30 segundos si está ocupado
    !check_schedule.

/* ----- PLANES PARA TRAER MEDICAMENTO O CERVEZA (Modificados para ser específicos) ----- */

// Plan para traer un MEDICAMENTO ESPECÍFICO cuando se solicita (o cuando lo inicia el propio robot)
// Se activa con !has(AgenteDestino, NombreDelMedicamento)
+!has(Ag, DrugName)[source(Source)] : // Source puede ser Ag (owner) o self (robot)
    bringDrug(DrugName, Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Específico): Intentando llevar ", DrugName, " a ", Ag, " (solicitado por ", Source, ")");
    -free[source(self)]; // Marcar como ocupado
    !at(enfermera, medCab); // Ir al armario
    open(medCab);
    // *** USA EL PARÁMETRO DrugName ***
    obtener_medicamento(DrugName); // Llama a la acción con el nombre específico
    close(medCab);
    !at(enfermera, Ag); // Ir hacia el agente
    // hand_in sigue siendo genérico en el modelo, pero podría ser específico si fuera necesario
    hand_in(drug); // Entregar (acción genérica en el modelo actual)
    // ?has(Ag, DrugName); // Opcional: Verificar si el agente actualiza su creencia (puede que no sea inmediato)

    // Registrar consumo específico DESPUÉS de entregar
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, DrugName, Ag); // Registrar consumo del medicamento específico
    .println("Registrado consumo de ", DrugName, " por ", Ag);
    +free[source(self)]; // Marcar como libre
    .println("Robot libre después de entregar ", DrugName).

// Plan para traer CERVEZA (sin cambios, asume 'beer' como tipo único)
+!has(Ag, beer)[source(Ag)] :
    bringBeer(Ag) & free[source(self)] <-
    .println("REGLA 1 (Traer Cerveza): Intentando llevar cerveza a ", Ag);
    -free[source(self)];
    !at(enfermera, fridge);
    open(fridge);
    get(beer); // Acción genérica para cerveza
    close(fridge);
    !at(enfermera, Ag);
    hand_in(beer); // Acción genérica para cerveza
    // ?has(Ag, beer);
    .date(YY, MM, DD); .time(HH, NN, SS);
    +consumed(YY, MM, DD, HH, NN, SS, beer, Ag); // Registrar consumo de cerveza
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
			!at(enfermera, delivery);     // go to deliver area and wait there.
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

	// A different rule provided to not block the agent with contradictory petitions

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
		.println(Ag, " is at ",P);
		.wait(500).
	+!at(Ag, P) : not at(Ag, P) <- 
		.println("Going to ", P, " <=======================");  
		.wait(200);
		!go(P);                                        
		.println("Checking if is at ", P, " ============>");
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

	/* ----- ACTUALIZACIÓN DE LA HORA ----- */
	// El robot puede verificar la hora actual.                  
	+?time : true
	<-  wacthClock.


/* ----- ##### NUEVO: GESTIÓN DE NOTIFICACIÓN DE CONSUMO ##### ----- */

// Cuando el dueño informa que ha consumido un medicamento y el robot está libre:
+medication_consumed(drug)[source(Ag)] : free[source(self)] & available(drug, medCab) <-
    .println("Notificación recibida: ", Ag, " dice haber tomado ", drug);
    -free[source(self)]; // Marca al robot como ocupado
    .println("Iniciando plan para verificar el consumo en medCab.");
    !verify_consumption(Ag, drug). // Inicia el objetivo de verificación

// Si el robot está ocupado cuando recibe la notificación:
+medication_consumed(drug)[source(Ag)] : not free[source(self)] <-
    .println("Recibí notificación de consumo de ", drug, " por ", Ag, ", pero estoy ocupado. Lo verificaré más tarde.");
    // Opcional: Enviar mensaje al Ag indicando que está ocupado
    .send(Ag, tell, msg("Recibí tu notificación sobre ", drug, ", pero estoy ocupado. Lo verificaré en cuanto pueda.")).
    // Opcional: Añadir a una cola de tareas pendientes
    // +pending_verification(Ag, drug, medCab).

/* ----- ##### NUEVO: PLAN PARA VERIFICAR EL CONSUMO ##### ----- */

+!verify_consumption(Ag, drug) <-
    .println("Verificando consumo de ", drug, " en ", medCab, " solicitado por ", Ag);
    !at(enfermera, medCab); // Paso 1: Ir a la ubicación del drug
    .println("Llegué a ", medCab, ". Realizando verificación de stock de ", drug);

    // --- Inicio: Simulación/Acción de Verificación ---
    // Aquí iría la lógica real para verificar el stock.
    // Podría ser consultar un sensor, interactuar con el entorno, etc.
    // Por ahora, simulamos que la verificación toma tiempo y es exitosa.
    .wait(3000); // Simula el tiempo de verificación
    .println("Verificación de stock para ", drug, " completada (simulada).");

    // Aquí podrías actualizar la creencia del stock si usaras un contador.
    // Ejemplo: Si tuvieras stock(drug, 10)
    // ?stock(drug, N);
    // -stock(drug, N);
    // +stock(drug, N-1).
    // También deberías manejar el caso N=0 -> -available(drug, medCab).

    // --- Fin: Simulación/Acción de Verificación ---

    .println("Verificación finalizada. Enviando confirmación a ", Ag);
    .send(Ag, tell, msg("He verificado en el estante de medicacion que has tomado ", drug, ". ¡Gracias por informarme!")); // Mensaje de confirmación final
    +free[source(self)]; // Libera robot *tras* disparar chequeo
    .println("Robot libre después de verificar consumo.").

// Plan de fallo para la verificación
-!verify_consumption(Ag, drug)[error(E)] <-
    .println("¡ERROR al verificar el consumo de ", drug, " para ", Ag, "! Error: ", E);
    .send(Ag, tell, msg("Tuve un problema al intentar verificar el consumo de ", drug, ". Por favor, revisa manualmente."));
    +free[source(self)]. // Asegura liberar al robot incluso si falla


/* ----- RECEPCIÓN DE PAUTAS DE MEDICACIÓN ----- */
// Al recibir una pauta del owner, la añade a sus creencias.
+medician(M, H)[source(owner)] <-
    +medician(M, H);
    .println("Pauta recibida y almacenada: Tomar ", M, " a las ", H, "h.").