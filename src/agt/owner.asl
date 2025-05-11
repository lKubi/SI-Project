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

// Medicamentos disponibles en el botiquín
available("Paracetamol", medCab).
available("Ibuprofeno", medCab).
available("Amoxicilina", medCab).
available("Omeprazol", medCab).
available("Loratadina", medCab).

// Cervezas disponibles en la nevera
available(beer, fridge).  

free.

// Regla para indicar si hay o no cervezas
orderBeer :- not available(beer, fridge).

/* ----- OBJETIVOS INICIALES DEL DUEÑO (OWNER) ----- */

!do_something.
!check_bored.
!medical_guides_initial.
!update_schedule_later.
!check_schedule.


/* ----- PLANES PARA GESTIONAR ESTADO DE LA ENFERMERA/ROBOT ----- */

// Este plan ahora espera DrugName y SimulatedHour, SimulatedMinute (si es posible enviarlos)
+medicina_recogida_robot(DrugName, SimulatedHour, SimulatedMinute)[source(enfermera)]<-
    .print("La medicina ha sido recogida por el robot.");
    .drop_all_intentions;
    .abolish(medician(DrugName, SimulatedHour, SimulatedMinute)); 
    .println("Owner: Pauta para ", DrugName, " (", SimulatedHour, SimulatedMinute,"h) eliminada localmente (enfermera fue más rápida).");
    -medicina_recogida_robot(DrugName, SimulatedHour, SimulatedMinute)[source(enfermera)]; 
    +nurse_delivering;
    +free[source(self)];
    !check_schedule.

/* ----- PLAN PARA ENVIAR PAUTAS INICIALES Y GUARDARLAS LOCALMENTE ----- */
+!medical_guides_initial : not medical_guides_sent <-
    .println("Owner: Enviando pautas iniciales a la enfermera y guardándolas localmente...");
    .send(enfermera, tell, medician("Paracetamol", 0, 40)); +medician("Paracetamol", 0, 40);
    .send(enfermera, tell, medician("Amoxicilina", 1,40)); +medician("Amoxicilina", 1, 40);
    .send(enfermera, tell, medician("Ibuprofeno", 2, 40)); +medician("Ibuprofeno", 2,40);
    .send(enfermera, tell, medician("Amoxicilina", 3, 40)); +medician("Amoxicilina", 3,40);
    .send(enfermera, tell, medician("Omeprazol", 4, 40)); +medician("Omeprazol", 4,40);
    .send(enfermera, tell, medician("Loratadina", 6, 40)); +medician("Loratadina 10mg", 6, 40);
    .send(enfermera, tell, medician("Omeprazol", 8, 40)); +medician("Omeprazol", 8, 40);
    .send(enfermera, tell, medician("Omeprazol", 12, 40)); +medician("Omeprazol", 12, 40);
    .send(enfermera, tell, medician("Loratadina", 16, 40)); +medician("Loratadina 10mg", 16, 40);
    .send(enfermera, tell, medician("Paracetamol", 20, 40)); +medician("Paracetamol", 20, 40);
    .send(enfermera, tell, medician("Omeprazol", 23, 40)); +medician("Omeprazol", 23, 40);
    +medical_guides_sent;
    .println("Owner: Pautas iniciales enviadas y guardadas localmente.").

//Evitar que se envien las pautas iniciales varias veces
+!medical_guides_initial : medical_guides_sent <- true.

// Plan para actualizar pautas (ej. después de X tiempo)
+!update_schedule_later : true  <- 
    .println("Owner: Esperando para actualizar pautas...");
    .wait(3600000); //Espera 1 dia para cambiar las pautas
    .println("Owner: ¡Tiempo de actualizar pautas!");
    !do_schedule_update.

//Borrado de pautas antiguas si las hubiese y envio de nuevas
+!do_schedule_update : true <-
    .println("Owner: Iniciando actualización de pautas...");
    .send(enfermera, achieve, clear_schedule);
    .println("Owner: Borrando pautas antiguas locales...");
    .abolish(medician(_, _, _)); // Borra todas las pautas locales si las hubiera
    !send_new_schedule_random. // Enviar y guardar las nuevas

//Añadir nuevas pautas de forma aleatoria
+!send_new_schedule_random : true <-
    .println("Owner: Enviando y guardando NUEVAS pautas ALEATORIAS (con minutos)...");
    Medications = ["Ibuprofeno", "Omeprazol", "Aspirina", "Paracetamol"];
    for (.member(MedName, Medications)) {
        // Genera una hora aleatoria (0-23) para este medicamento
        .random([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23], RandomHour);
        // Genera un minuto aleatorio (0-59) para este medicamento
        .random([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59], RandomMinute);

        // Imprime la pauta con hora y minuto (formato HH:MM podría necesitar ajuste dependiendo de la salida deseada para un solo dígito)
        .print("Owner: Creando pauta aleatoria: [", MedName, ", ", RandomHour, ":", RandomMinute, "]");

        // Envía y guarda la creencia con ambos, hora y minuto
        .send(enfermera, tell, medician(MedName, RandomHour, RandomMinute));
        +medician(MedName, RandomHour, RandomMinute); 
    }
    .println("Owner: NUEVAS pautas ALEATORIAS (con minutos) enviadas y guardadas.").

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
          connect(RoomAg, RoomP, Door) & atDoor <-
    move_towards(P).
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
          connect(RoomAg, RoomP, Door) & not atDoor <-
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
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <-
    move_towards(P).
-!go(P) <- 
.println("Owner: He llegado a ", P, ".").

/* ----- OBJETIVO: Pedir medicamento o cerveza ----- */
+!get(ItemName) : .my_name(Name) <- 
    Time = math.ceil(math.random(4000));
    .println("Owner: Esperando ", Time, " ms antes de pedir ", ItemName, " al robot.");
    .wait(Time);
    // *** ENVÍA PETICIÓN ESPECÍFICA ***
    .send(enfermera, achieve, has(Name, ItemName)).

/* ----- OBJETIVO: Recibir y procesar medicamento o cerveza ----- */
+has(owner, ItemName) : true <-
    !take(ItemName). 

/* ----- OBJETIVO: Tomar/Consumir medicamento o cerveza ----- */
+!take(ItemName) : has(owner, ItemName) <- 
    sip(ItemName); 
    .println("Owner: Consumiendo ...");
    .wait(100); // Simular tiempo de consumo
    -has(owner, ItemName); // Eliminar creencia de que tiene el item
    .abolish(nurse_delivering);
    !take(ItemName).


// Condición de parada: ya no tiene el item (sip lo consumió o se usó para otra cosa)
+!take(ItemName) : not has(owner, ItemName) <-
    .println("Owner: Terminado de consumir");
    !do_something;
    -free[source(self)].

/* ----- OBJETIVO: Comprobar aburrimiento (Sin cambios lógicos) ----- */
+!check_bored : true <-
    .wait(100);
    .send(enfermera, askOne, time, R);
    .send(enfermera, tell, chat("¿Qué tiempo hace en Ourense?")); 
    !check_bored. 

/* ----- OBJETIVO: Recibir y mostrar mensajes (Sin cambios lógicos) ----- */
+msg(M)[source(Ag)] : .my_name(Name) <-
    .print(Ag, " envió a ", Name, " el mensaje: '", M, "'").
   


// Triggered by the new percept format
+!check_schedule
   : clock(SimulatedHour, SimulatedMinute) 
<-
    if (not ha_caducado(DrugToDeliver)) {

    if (medician(DrugToTake, SimulatedHour, SimulatedMinute)) { // Ahora busca HORA y MINUTO
         .println("Owner: [Clock ", SimulatedHour, ":", SimulatedMinute, "] ¡Pauta encontrada para esta hora exacta!: ", DrugToTake);
         .drop_all_intentions;
         !check_and_take_medicine(DrugToTake, SimulatedHour, SimulatedMinute);
    } else {
         //.println("Owner: [Clock ", SimulatedHour, ":", SimulatedMinute, "] No hay medicina programada para mí a esta hora.");
    };
    }else{
        .println("Owner: La medicacion esta caducada.");
    }
    .wait(100);
    !check_schedule.


// Plan Bucle: Si está OCUPADO o la Energía está BAJA, simplemente esperar
+!check_schedule
   : ( not free[source(self)]) <-
   .wait(100); 
   !check_schedule.

// Plan Bucle: Si falta la hora 
+!check_schedule : not clock(_, _) <-
  // .print("WARN: [Bucle Check Schedule] No se encontró la creencia clock(H,M). Esperando...");
   .wait(100); 
   !check_schedule.

-!check_schedule : true <-
    //.print("La ejecución del cuerpo de un plan para !check_schedule falló. Reintentando...");
    .wait(200); // Pequeña espera
    !check_schedule.


// ----- OWNER: Planes para lograr el Objetivo !check_and_take_medicine -----

// Plan principal: Manos libres, se activa cuando se adopta el objetivo específico
+!check_and_take_medicine(DrugToTake, SimulatedHour, SimulatedMinute) : .my_name(Ag) & not has(Ag, _) <-
    .println("",DrugToTake,",",SimulatedHour, SimulatedMinute,")] Iniciando gestión. Manos libres.");
    
    // --- Decisión Aleatoria ---
    .random([0,1,2], Decision); // 0 = Ir a por ella y robot al mismo tiempo, 1 = Esperar al robot, 2 = Ir a por ella

    if (Decision == 0) {
        // --- DECISIÓN: Ir a por la medicina y el robot al mismo tiempo carrera ---
        .println("Decidí ir yo mismo a por ", DrugToTake, ".");
        .send(enfermera, tell, ir_medicina);
        -free[source(self)];

        !at(Ag, medCab);
        .println("He llegado al botiquín (medCab).");

        if (available(DrugToTake, medCab)) {
            .println("", DrugToTake, " parece disponible. Intentando cogerlo...");
            open(medCab);
            obtener_medicamento(DrugToTake);
            .send(enfermera, tell, medicina_recogida_owner(DrugToTake, SimulatedHour, SimulatedMinute)); 
            close(medCab);
            .abolish(medician(DrugToTake, SimulatedHour, SimulatedMinute));
            .println("Pauta para ", DrugToTake, " (", SimulatedHour, SimulatedMinute,"h) eliminada localmente.");
            .println("Obtenido ", DrugToTake, " del botiquín.");
            .wait(1000);
            .println("omando ", DrugToTake, "...");
            .println("Terminé de tomar ", DrugToTake, ".");
            .send(enfermera, tell, medication_consumed(DrugToTake, SimulatedHour, SimulatedMinute));
            .println("Notificado a 'enfermera de que consumi la medicacion.");
            .wait(1000);
            .println("Acción completada para ", DrugToTake, ".");
            -has(Ag, DrugToTake); // Eliminar creencia de que tiene el medicamento
        }
        +free[source(self)];
    }
    
    if(Decision == 1) {
        // --- DECISIÓN: Esperar al robot ---
        .send(enfermera, tell, esperar_medicina);
        !at(Ag, chair2);
        .println("Decidí esperar a que la enfermera me traiga ", DrugToTake, ".");
        .println("Esperando a la enfermera en el salón...");
    }
    
    if(Decision == 2) {
        // --- DECISIÓN: Ir a por la medicina ---
       .println("Decidí ir yo mismo a por ", DrugToTake, ".");
        -free[source(self)];

        !at(Ag, medCab);
        .println("He llegado al botiquín (medCab).");

        if (available(DrugToTake, medCab)) {
            .println("", DrugToTake, " parece disponible. Intentando cogerlo...");
            open(medCab);
            obtener_medicamento(DrugToTake);
            .send(enfermera, tell, medicina_recogida_owner(DrugToTake, SimulatedHour, SimulatedMinute)); 
            close(medCab);
            .abolish(medician(DrugToTake, SimulatedHour, SimulatedMinute));
            .println("Pauta para ", DrugToTake, " (", SimulatedHour, SimulatedMinute,"h) eliminada localmente.");

            .println("Obtenido ", DrugToTake, " del botiquín.");
            .wait(1000);
            .println("Tomando ", DrugToTake, "...");
            .println("Terminé de tomar ", DrugToTake, ".");
            .send(enfermera, tell, medication_consumed(DrugToTake, SimulatedHour, SimulatedMinute));
            .println("Notificado a 'enfermera de que consumi la medicacion.");
            .wait(1000);
            .println("Acción completada para ", DrugToTake, ".");
            -has(Ag, DrugToTake); // Eliminar creencia de que tiene el medicamento
        }
        +free[source(self)];
    };
    !check_schedule; // Revisa el horario después de tomar la medicina
.

+!do_something: not nurse_delivering & free[source(self)] <-
    .wait(500);
    .println("Owner: [do_something] Libre y enfermera no entrega. Decidiendo qué hacer...");
    .random([0,1,2,3,4],Tarea);

    if (Tarea == 0) {
        .println("Owner: [do_something] Decidí ir a abrir la puerta (simulado).");
        !open; // Este plan marcará -busy al finalizar implícitamente o explícitamente si lo modificas
    } elif (Tarea == 1) {
        .println("Owner: [do_something] Decidí ir a sentarme.");
        !sit;
    } elif (Tarea == 2) {
        .println("Owner: [do_something] Decidí ir a dormir.");
        !sleep_bed;
    } else {
        .println("Owner: [do_something] Decidí pedir/tomar una cerveza.");
        !drunk; // Este plan debe manejar su propio estado busy si es complejo
    };
    .wait(100); 
    !do_something. 

// NUEVO Plan: Esperar si la enfermera está ocupada entregando (aunque el owner esté libre)
+!do_something: nurse_delivering <-
    .println("Owner: [do_something] Quiero hacer algo, pero la enfermera viene hacia mí. Esperaré...");
    .wait(2000). 

// Plan cuando owner YA ESTÁ OCUPADO con una acción previa (de !do_something o !take)
+!do_something <-
    .println("Owner: [do_something] Estoy ocupado con otra cosa. Esperando terminar...");
    .wait(1000);
    !do_something. 
    
// Este objetivo se centra en abrir la puerta de la casa (simular visitas)
+!open: .my_name(Ag) <-
    .println("¡¡¡Ohh I have a visit!!! I´m going to open the door, give me a second...");
    .wait(200);
    !at(Ag, delivery);
    .println("Opening the door...");
    .random(X); 
    .wait(X*7351+2000); 
    .println("¡¡Bye, thank you for the visit!!").

// Este objetivo se centra en sentarse en algun sitio disponible (sofa o sillas)
+!sit: .my_name(Ag) <-
    .println("I´m need to sit...");
    .random([0,1,2,3,4], Sitio);        // Elegimos aleatoriamente un lugar para sentarse
    
    if (Sitio == 0) {
        .println("I´m going to sit in chair1");
        !at(Ag, chair1);
        sit(chair1);
        .wait(4000);
    } elif (Sitio == 1) {
        .println("I´m going to sit in chair2");
        !at(Ag, chair2);
        sit(chair2);
        .wait(4000);
    } elif (Sitio == 2) {
        .println("I´m going to sit in chair3");
        !at(Ag, chair3);
        sit(chair3);
        .wait(4000);
    } elif (Sitio == 3) {
        .println("I´m going to sit in chair4");
        !at(Ag, chair4);
        sit(chair4);
        .wait(4000);
    } else {
        .println("I´m going to sit in sofa");
        !at(Ag, sofa);
        sit(sofa);
        .wait(10000);
    };
    .println("Yep, the walls are so clean").


// Este objetivo trata de que el owner duerma en una de las camas X tiempo
+!sleep_bed : .my_name(Ag) <-
    .println("I´m tired...");
    .random([0,1,2], Bed);      
    if (Bed == 0) {
        .println("I´m going to sleep in bed1");
        !at(Ag, bed1);
    } elif (Bed == 1) {
        .println("I´m going to sleep in bed2");
        !at(Ag, bed2);
    } else {
        .println("I´m going to sleep in bed3");
        !at(Ag, bed3);
    }
    .random(X);    
    .wait(X*2+1000);               // Duerme
    .println("Whoaaa, I dreamed I was an helicopter").

// Este objetivo trata de obtener y beber una cerveza, SABIENDO QUE HAY EXISTENCIAS 
+!drunk: .my_name(Ag) & available(beer, fridge) & not(orderBeer) <-
    .println("Ummmm I'm thirsty... I want a beer :)");
    !at(Ag, fridge);        
    open(fridge);
    get(beer);         
    close(fridge);
    !at(Ag, sofa);      
    !take(beer). 

+!drunk: .my_name(Ag) & orderBeer <-
    .println("Ummmm I'm thirsty... I want a beer :)");
    !at(Ag, fridge);
    .println("Oh noo!!! We've run out of beers.... let´s ask for more!!!!");
    .wait(500);        
    .send(repartidor, achieve, order(beer, 5));                                 // como no hay cervezas, las pedimos
    .println("Okay, I made the order. Now, let´s move to the delivery"); 
	!at(Ag, delivery);                                                          // nos movemos a la zona de entrega para recojer pedido    
	.wait(delivered);                                                           // esperamos a que lo entreguen
    .wait(1000);
    .println("Thanks for the beers repartidor!!!! Let´s repose that, I´m thrirsty");
	!at(Ag, fridge);
    open(fridge);
	deliverbeer(beer,5);                                                        // reponemos unidades de cerveza
	+available(beer, fridge);
    .println("Now I can take a beer");
    get(beer);
    close(fridge);
    !at(Ag, sofa);
    !take(beer).   

//Se recibe la señal de auxiliar que la caducacion ha sido eliminada y ya se puede operar con tranquilidad
+orden_eliminar_caducaciones[source(auxiliar)] <-
    .println("Recibida señal de ", auxiliar, " para eliminar la caducacion.");
    .abolish(ha_caducado(_));
    .println("Caducacion eliminada.");
    -orden_eliminar_caducaciones[source(auxiliar)].