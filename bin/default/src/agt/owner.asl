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

available("Paracetamol 500mg", medCab).
available("Ibuprofeno 600mg", medCab).
available("Amoxicilina 500mg", medCab).
available("Omeprazol 20mg", medCab).
available("Loratadina 10mg", medCab).
available(beer, fridge).       

// Regla para indicar si hay o no cervezas
orderBeer :- not available(beer, fridge).

/* ----- OBJETIVOS INICIALES DEL DUEÑO (OWNER) ----- */

!do_something. 
!check_bored.
!medical_guides_initial.
!update_schedule_later.


/* ----- NUEVO: PLANES PARA GESTIONAR ESTADO DE LA ENFERMERA/ROBOT ----- */

+nurse_is_delivering[source(enfermera)] : not nurse_delivering <- // Verifica fuente y estado actual
    +nurse_delivering; // Modifica la creencia LOCAL del owner
    .println("Owner: Recibido aviso de que el robot está entregando. Esperaré.").

// Se activa cuando el robot envía el mensaje 'nurse_finished_delivering'
+nurse_finished_delivering[source(enfermera)] : nurse_delivering <- // Verifica fuente y estado actual
    -nurse_delivering; // Modifica la creencia LOCAL del owner
    .println("Owner: Recibido aviso de que el robot terminó la entrega. Puedo continuar.").

// Opcional: Manejar mensaje de fin si ya no creía que estaba entregando (por si acaso)
+nurse_finished_delivering[source(enfermera)] : not nurse_delivering <-
    .println("Owner: Recibido aviso de fin de entrega, pero no creía que estuviera entregando.").

/* ----- PLAN PARA ENVIAR PAUTAS INICIALES Y GUARDARLAS LOCALMENTE ----- */
+!medical_guides_initial : not medical_guides_sent <-
    .println("Owner: Enviando pautas iniciales a la enfermera y guardándolas localmente...");
    .send(enfermera, tell, medician("Paracetamol 500mg", 1)); +medician("Paracetamol 500mg", 1);
    .send(enfermera, tell, medician("Paracetamol 500mg", 9)); +medician("Paracetamol 500mg", 9);
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 11)); +medician("Ibuprofeno 600mg", 11);
    .send(enfermera, tell, medician("Amoxicilina 500mg", 15)); +medician("Amoxicilina 500mg", 15);
    .send(enfermera, tell, medician("Omeprazol 20mg", 5)); +medician("Omeprazol 20mg", 5);
    .send(enfermera, tell, medician("Loratadina 10mg", 3)); +medician("Loratadina 10mg", 3);
    .send(enfermera, tell, medician("Omeprazol 20mg", 18)); +medician("Omeprazol 20mg", 18);
    .send(enfermera, tell, medician("Paracetamol 500mg", 20)); +medician("Paracetamol 500mg", 20);
    .send(enfermera, tell, medician("Omeprazol 20mg", 23)); +medician("Omeprazol 20mg", 23);

    +medical_guides_sent;
    .println("Owner: Pautas iniciales enviadas y guardadas localmente.");
.

+!medical_guides_initial : medical_guides_sent <- true.


// Plan para actualizar pautas (ej. después de X tiempo)
+!update_schedule_later : true  <- 
    .println("Owner: Esperando para actualizar pautas...");
    .wait(480000); //Espera 1 dia para cambiar las pautas
    .println("Owner: ¡Tiempo de actualizar pautas!");
    !do_schedule_update.

+!do_schedule_update : true <-
    .println("Owner: Iniciando actualización de pautas...");
    .send(enfermera, achieve, clear_schedule);
    .println("Owner: Borrando pautas antiguas locales...");
    .abolish(medician(_, _)); // Borra todas las pautas locales (con 2 argumentos)
    .wait(1500);
    !send_new_schedule_random. // Enviar y guardar las nuevas

//Añadir nuevas pautas manualmente, no lo utilizamos actualmente pero lo deje por si se quiere probar.
+!send_new_schedule : true <-
    .println("Owner: Enviando y guardando NUEVAS pautas...");
    .send(enfermera, tell, medician("Ibuprofeno 600mg", 8)); +medician("Ibuprofeno 600mg", 8);
    .send(enfermera, tell, medician("Omeprazol 20mg", 14)); +medician("Omeprazol 20mg", 14);
    .send(enfermera, tell, medician("Aspirina 100mg", 19)); +medician("Aspirina 100mg", 19);
    .send(enfermera, tell, medician("Paracetamol 500mg", 22)); +medician("Paracetamol 500mg", 22);
    .println("Owner: NUEVAS pautas enviadas y guardadas.");
.

//Añadir nuevas pautas de forma aleatoria
+!send_new_schedule_random : true <-
    .println("Owner: Enviando y guardando NUEVAS pautas ALEATORIAS...");

    Medications = ["Ibuprofeno 600mg", "Omeprazol 20mg", "Aspirina 100mg", "Paracetamol 500mg"];

    for (.member(MedName, Medications)) {
        // Genera una hora aleatoria (0-23) para este medicamento
        .random([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23], RandomHour);

        .print("Owner: Creando pauta aleatoria: [", MedName, ", ", RandomHour, "]");

        .send(enfermera, tell, medician(MedName, RandomHour));
        +medician(MedName, RandomHour);
    }

    .println("Owner: NUEVAS pautas ALEATORIAS enviadas y guardadas.");
.


+!check_and_take_medicine : .my_name(Ag) & medician(DrugToTake, Hour) & not has(Ag, _) <-
    // Solo entra aquí si hay medicina programada Y no está consumiendo/teniendo otra cosa.
    .println("Owner [", Ag, "]: [Random Do] Comprobando medicación... ¡Sí! Debo tomar: ", DrugToTake, " (", Hour, "h) y tengo las manos libres.");

    // --- El resto de la lógica para ir, obtener y tomar ---
    !at(Ag, medCab);
    .println("Owner [", Ag, "]: [Random Do] He llegado al botiquín (medCab).");
    open(medCab);
    obtener_medicamento(DrugToTake); // Ahora debería funcionar si no tiene nada
    close(medCab);
    .println("Owner [", Ag, "]: [Random Do] Obtenido ", DrugToTake, " del botiquín.");
    .wait(1000);
    .println("Owner [", Ag, "]: [Random Do] Tomando ", DrugToTake, "...");
    .abolish(medician(DrugToTake, Hour));
    .println("Owner [", Ag, "]: [Random Do] Pauta para ", DrugToTake, " eliminada localmente.");
    .send(enfermera, tell, medication_consumed(DrugToTake, Hour));
    .println("Owner [", Ag, "]: [Random Do] Notificado a 'enfermera'.");
    !at(Ag, sofa);
    .println("Owner [", Ag, "]: [Random Do] Volviendo al sofá.");
    .wait(1000);
    .println("Owner [", Ag, "]: [Random Do] Acción de tomar ", DrugToTake, " completada.");
    // FIN DEL PLAN
.

// NUEVO Plan: Hay medicación pendiente PERO el owner ESTÁ OCUPADO CON OTRA COSA.
+!check_and_take_medicine : .my_name(Ag) & medician(DrugToTake, Hour) & has(Ag, CurrentItem) <-
    // Se activa si hay medicina programada pero ya tiene 'CurrentItem'.
    .println("Owner [", Ag, "]: [Random Do] Debo tomar ", DrugToTake, ", pero todavía estoy con ", CurrentItem, ". Mejor espero a terminar.");
    // No hace nada más, termina inmediatamente. La medicina sigue programada.
    // El control vuelve a !do_something.
.
// Plan alternativo: No hay medicación pendiente (sin cambios respecto a la versión anterior).
+!check_and_take_medicine : .my_name(Ag) & not medician(_,_) <-
    .println("Owner [", Ag, "]: [Random Do] Comprobando medicación... Nada pendiente ahora.");
    // FIN DEL PLAN
.

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

+!do_something: not busy & not nurse_delivering <-
    +busy; // Marcarse como ocupado PARA ESTA ACCIÓN CONCRETA
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
    } elif (Tarea == 3) {
        .println("Owner: [do_something] Decidí comprobar si debo tomar medicina.");
        !check_and_take_medicine; // Este plan debe manejar su propio estado busy si es complejo
    } else {
        .println("Owner: [do_something] Decidí pedir/tomar una cerveza.");
        !drunk; // Este plan debe manejar su propio estado busy si es complejo
    };
    // IMPORTANTE: Asumiendo que los sub-objetivos (!open, !sit, etc.) liberan 'busy' al terminar.
    // Si no lo hacen, necesitas -busy aquí. Pero es mejor que cada sub-objetivo maneje su busy.
    // Si los sub-objetivos son rápidos y no necesitan busy, puedes quitar +busy al inicio y -busy al final aquí.
    // PERO, si !check_and_take_medicine o !drunk INICIAN una acción larga, necesitan +busy/-busy dentro de ellos.
    // Para simplificar, asumamos que los subplanes son atómicos para !do_something
    -busy; // Liberar busy después de completar la acción elegida
    .wait(100); // Pequeña pausa antes de volver a evaluar
    !do_something. // Volver a intentar hacer algo

// NUEVO Plan: Esperar si la enfermera está ocupada entregando (aunque el owner esté libre)
+!do_something: not busy & nurse_delivering <-
    .println("Owner: [do_something] Quiero hacer algo, pero la enfermera viene hacia mí. Esperaré...");
    .wait(2000); // Espera un poco (ajusta el tiempo si es necesario)
    !do_something. // Vuelve a intentar hacer algo (revisará de nuevo nurse_delivering)

// Plan cuando owner YA ESTÁ OCUPADO con una acción previa (de !do_something o !take)
+!do_something: busy <-
    .println("Owner: [do_something] Estoy ocupado con otra cosa. Esperando terminar...");
    .wait(1000); // Espera mientras está ocupado
    !do_something. // Re-evalúa después de esperar
    
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
    .random([0,1,2], Bed);      // Elige aleatoriamente un valor, y dependiendo de este, selecciona una cama
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
    .println("Whoaaa, I dreamed I was an helecopter").

// Este objetivo trata de obtener y beber una cerveza, SABIENDO QUE HAY EXISTENCIAS 
+!drunk: .my_name(Ag) & available(beer, fridge) & not(orderBeer) <- // Añadido available(beer, fridge)
    .println("Ummmm I'm thirsty... I want a beer :)");
    !at(Ag, fridge);        
    open(fridge);
    get(beer);          // Obtenemos una cerveza
    close(fridge);
    !at(Ag, sofa);      // Nos vememos a un sitio para sentarnos y beber
    !take(beer). 

//
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
