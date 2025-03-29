/* ----- CONEXIONES ENTRE HABITACIONES ----- */
//Definimos las conexiones entre las diferentes habitaciones y sus puertas
//Esto permite que el dueño pueda moverse de una habitación a otra a través de puertas específicas.

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
connect(hall,livingroom, doorSal1).                       
connect(livingroom, hall, doorSal1).
connect(hallway,livingroom, doorSal2).
connect(livingroom, hallway, doorSal2).

/* ----- OBJETIVOS INICIALES DEL DUEÑO (OWNER) ----- */
//Acciones que puede realizar el dueño

!sit.
!open.
!take_medicine.
!walk.
!wakeup.
!check_bored.


//+!init <- !sit ||| !open ||| !walk ||| !wakeup ||| !check_bored.

/* ----- OBJETIVO: Despertarse (wakeup) ----- */
//Si el dueño NO está ocupado, se despierta, informa y toma medicamentos
+!wakeup : .my_name(Ag) & not busy <-								
	+busy;																
	!check_bored;													
	.println("Owner just woke up and needs to go to the medicine shelf"); 
	.wait(3000);
	-busy;																
	!sit.

//Si el dueño ya está ocupado, espera e intenta despertarse de nuevo															
+!wakeup : .my_name(Ag) & busy <-										
	.println("Owner is doing something now, is not asleep");
	.wait(10000);
	!wakeup.
	
/* ----- OBJETIVO: Caminar (walk) ----- */
//Si el dueño NO está ocupado, se levanta y se mueve aleatoriamente
+!walk : .my_name(Ag) & not busy <- 
	+busy;  
	.println("Owner is not busy, is sit down on the sofa");
	.wait(500);
	!at(Ag,sofa);
	.wait(2000);
	//.println("Owner is walking at home"); 
	-busy;
	!open.

//Si el dueño está ocupado, espera y vuelve a intentarlo
+!walk : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not walk");
	.wait(6000);
	!walk.

/* ----- OBJETIVO: Abrir la puerta (open) ----- */
//Si el dueño NO está ocupado, va hacia la puerta y la abre
+!open : .my_name(Ag) & not busy <-
	+busy;   
	.println("Owner goes to the home door");
	.wait(200);
	!at(Ag, delivery);
	.println("Owner is opening the door"); 
	.random(X); .wait(X*7351+2000); //Toma un tiempo aleatorio en abrir
	!at(Ag, sofa);
	sit(sofa);
	.wait(5000);
	!at(Ag, medCab);
	.wait(10000);
	!at(Ag, chair3);
	sit(chair3);
	-busy.

//Si el dueño está ocupado, espera y vuelve a intentarlo
+!open : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not open the door");
	.wait(8000);
	!open.
 
/* ----- OBJETIVO: Sentarse (sit) ----- */
//Si el dueño NO está ocupado, va a la estantería de medicamentos o la nevera y toma algo
+!sit : .my_name(Ag) & not busy <- 												
	+busy; 																		
	.println("Owner goes to the medicine shelf to get a drug.");               
	//.println("Owner goes to the fridge to get a beer.");
	.wait(1000);
	!at(Ag, medCab);                                   //Elegir si se quiere ir a la estanteria de medicacion o a la nevera
	//!at(Ag, fridge);															
	.println("Owner is hungry and is at the medicine shelf getting something"); 
	//.println("He llegado al frigorifico");
	.wait(2000);
	!at(Ag, chair3);														
	sit(chair3);															
	.wait(1000);
	/*!at(Ag, chair4);                                 //Comentado para que no dure tanto el programa, luego hay que descomentarlo
	sit(chair4);
	.wait(4000);
	!at(Ag, chair2);
	sit(chair2);
	.wait(4000);
	!at(Ag, chair1);
	sit(chair1);
	.wait(4000);
	!at(Ag, sofa);
	sit(sofa);
	.wait(10000);*/
	//!get(drug);                                         //Elegir si se necesita una medicina o una cerveza
	!get(beer); 
	.wait(50000);
	-busy.	

	

//Si el dueño está ocupado, espera y vuelve a intentarlo
+!sit : .my_name(Ag) & busy <-													
	.println("Owner is doing something now and could not go to fridge");
	.wait(30000);
	!sit.



/* ----- ENTREGA PARTE 1 ----- */
/* ----- OBJETIVO: Ir al almacén de medicamentos y tomar la medicación ----- */
+!take_medicine : .my_name(Name) & not busy 
   <- 
      +busy;
      !at(Ag, medCab);
      .println("Owner is at the medicine shelf.");
      
      open(medCab); 
      get(drug); 
      close(medCab);
      
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


// ----- OBJETIVO: Verificar si el owner está en un lugar y si no esta, moverse hacia allí -----
+!at(Ag, P) : at(Ag, P) <- 														
	.println("Owner is at ",P);													
	.wait(5000).
// Si el owner no ha llegado al lugar P, se mueve hacia allí  
+!at(Ag, P) : not at(Ag, P) <- 												
	.println("Going to ", P);
	!go(P);                  													            
	.println("Checking if is at ", P);
	!at(Ag, P).            													

// ----- OBJETIVO: Mover al owner a un lugar específico -----
//Si el owner ya está en la misma habitación que el destino, se mueve directamente allí                   
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <-                   					        
	.println("Al estar en la misma habitación se debe mover directamente a: ", P);
	move_towards(P).  

//Si el owner y el destino están en habitaciones contiguas y el owner está en la puerta, se mueve al destino  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &				
		  connect(RoomAg, RoomP, Door) & atDoor <-
	.println("Al estar en la puerta ", Door, " se dirige a ", P);                        
	move_towards(P); 
	!go(P).       
//Si el owner y el destino están en habitaciones contiguas, pero NO está en la puerta, se mueve primero a la puerta  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// Caso anterior pero Ag NO esta en puerta, pues que valla a la puerta 
		  connect(RoomAg, RoomP, Door) & not atDoor <-
	.println("Al estar en una habitación contigua se mueve hacia la puerta: ", Door);
	move_towards(Door); 
	!go(P).
//Si el destino está en una habitación más alejada, primero se mueve a la puerta de una habitación intermedia         
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// RoomAg | Room | RoomP : Ag no esta en puerta, que valla a puerta que comunica con habitacion contigua
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & not atDoor <-
	.println("Se mueve a: ", DoorR, " para ir a la habitación contigua, ", Room);
	move_towards(DoorR); 
	!go(P). 
//Si el owner ya está en la puerta de la habitación intermedia, se mueve a la puerta de la siguiente habitación  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// Caso anterior pero Ag esta en puerta que comunica con Room, que se mueva a la puerta de Room que comunica con RoomP
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & atDoor <-
	.println("Se mueve a: ", DoorP, " para ir a la habitación ", RoomP);
	move_towards(DoorP); 
	!go(P). 
// Si el owner está en una habitación pero no es contigua al destino, se mueve directamente  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <-
	.println("Owner is at ", RoomAg,", that is not a contiguous room to ", RoomP);
	move_towards(P).  
//Manejo de errores si algo falla en el proceso de movimiento                                                      
-!go(P) <- .println("Something goes wrong......").
	                                                                        
/* ----- OBJETIVO: Obtener medicamento o cerveza ----- */
//El owner espera un tiempo aleatorio antes de solicitar el medicamento o la cerveza al robot enfermera.
//Esto simula un comportamiento más realista, evitando solicitudes instantáneas y repetitivas.
+!get(drug) : .my_name(Name) <- 
   Time = math.ceil(math.random(4000));
   .println("I am waiting ", Time, " ms. before asking the nurse robot for my drug.");
   .wait(Time);
   .send(enfermera, achieve, has(Name, drug)).

+!get(beer) : .my_name(Name) <- 
   Time = math.ceil(math.random(4000));
   .println("I am waiting ", Time, " ms. before asking the nurse robot for my beer.");
   .wait(Time);
   .send(enfermera, achieve, has(Name, beer)).

/* ----- OBJETIVO: El owner recibe y toma el medicamento o la cerveza ----- */
//Cuando el owner recibe el medicamento o la cerveza, lo toma inmediatamente.
//Se asume que el robot enfermera ha entregado el objeto correctamente.
+has(owner,drug) : true <-
   .println("Owner take the drug.");
   !take(drug).

+has(owner,beer) : true <-
   .println("Owner take the beer.");
   !take(beer).

/* ----- OBJETIVO: El owner solicita el medicamento o la cerveza ----- */
//Si el owner no tiene el medicamento o la cerveza, solicita que el robot enfermera se lo traiga.
-has(owner,drug) : true <-
   .println("Owner ask for drug. It is time to take it.");
   !get(drug).

-has(owner,beer) : true <-
   .println("Owner ask for beer. It is time to take it.");
   !get(beer).

/* ----- OBJETIVO: Tomar medicamento o cerveza ----- */
+!take(drug) : has(owner, drug) <-
   sip(drug);
   .println("Owner is siping the drug.");
   !take(drug).
+!take(drug) : not has(owner, drug) <-                         //Finaliza de tomar el medicamento
   .println("Owner has finished to take the drug.").//;
   //-asked(drug).

+!take(beer) : has(owner, beer) <-
   sip(beer);
   .println("Owner is siping the beer.");
   !take(beer).
+!take(beer) : not has(owner, beer) <-                          //Finaliza de tomar la cerveza
   .println("Owner has finished to take the beer.").//;
   //-asked(beer)

/* ----- OBJETIVO: Comprobar aburrimiento ----- */
+!check_bored : true
   <- .random(X); .wait(X*5000+2000);  // Owner get bored randomly
      .send(enfermera, askOne, time(_), R); // when bored, owner ask the robot about the time
      .print(R);
	  .send(enfermera, tell, chat("What's the weather in Ourense?"));
      !check_bored.

/* ----- OBJETIVO: Recibir y mostrar mensajes ----- */
+msg(M)[source(Ag)] : .my_name(Name)
   <- .print(Ag, " send ", Name, " the message: ", M);
      -msg(M).

	  
