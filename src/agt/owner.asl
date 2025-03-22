/* Initial Beliefs */
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

/* Initial goals */

// Owner will simulate the behaviour of a person 
// We need to characterize their digital twin (DT)
// Owner must record the DT data periodically 
// Owner must access the historic data of such person
// Owner will act randomly according to some problems
// Owner will usually act with a behaviour normal
// Owner problems will be activated by some external actions
// Owner problems will randomly be activated on time
// Owner will dialog with the nurse robot 
// Owner will move randomly in the house by selecting places

!sit.

!open.

!walk.

!wakeup.

!check_bored.

// Initially Owner could be: sit, opening the door, waking up, walking, ...
//!sit.   			
//!check_bored. 

//+!init <- !sit ||| !open ||| !walk ||| !wakeup ||| !check_bored.

+!wakeup : .my_name(Ag) & not busy <-									// OBJETIVO: levantarse cuando NO este ocupado
	+busy;																// añadir creencia opcupado
	!check_bored;														// hacer objetivo (pedir hora)
	.println("Owner just woke up and needs to go to the fridge"); 
	.wait(3000);
	-busy;																// quitar creencia opcupado
	!sit.																// va hacer objetivo sit
+!wakeup : .my_name(Ag) & busy <-										// SI esta ocupado, comunica, espera y vuelve intentarlo
	.println("Owner is doing something now, is not asleep");
	.wait(10000);
	!wakeup.
	
+!walk : .my_name(Ag) & not busy <- 
	+busy;  
	.println("Owner is not busy, is sit down on the sofa");
	.wait(500);
	!at(Ag,sofa);
	.wait(2000);
	//.println("Owner is walking at home"); 
	-busy;
	!open.
+!walk : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not walk");
	.wait(6000);
	!walk.

+!open : .my_name(Ag) & not busy <-
	+busy;   
	.println("Owner goes to the home door");
	.wait(200);
	!at(Ag, delivery);
	.println("Owner is opening the door"); 
	.random(X); .wait(X*7351+2000); // Owner takes a random amount of time to open the door 
	!at(Ag, sofa);
	sit(sofa);
	.wait(5000);
	!at(Ag, fridge);
	.wait(10000);
	!at(Ag, chair3);
	sit(chair3);
	-busy.
+!open : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not open the door");
	.wait(8000);
	!open.
 
+!sit : .my_name(Ag) & not busy <- 												// OBJETIVO: sentarse cuando NO este ocupado
	+busy; 																		// añade creencia de que esta ocupado
	.println("Owner goes to the fridge to get a beer.");
	.wait(1000);
	!at(Ag, fridge);															// owner se mueve al fridge
	.println("Owner is hungry and is at the fridge getting something"); 
	//.println("He llegado al frigorifico");
	.wait(2000);
	!at(Ag, chair3);															// owner se mueve a chair3
	sit(chair3);																// metodo de java??
	.wait(4000);
	!at(Ag, chair4);
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
	.wait(10000);
	!get(drug); 
	.wait(50000);
	-busy.																		// quitar creencia de ocupado
+!sit : .my_name(Ag) & busy <-													// Si esta ocupado, envia mensaje, espera y velve intentarlo
	.println("Owner is doing something now and could not go to fridge");
	.wait(30000);
	!sit.

+!at(Ag, P) : at(Ag, P) <- 														// OBJETIVO: Ag esta en lugar P(P tiene que estar registrado como sitio para ir)
	.println("Owner is at ",P);													// Si ha llegado Ag a P, lo comunica y espera
	.wait(5000).
+!at(Ag, P) : not at(Ag, P) <- 													// Si Ag NO ha llegado al lugar P
	.println("Going to ", P);
	!go(P);                  													// Se mueve Ag                
	.println("Checking if is at ", P);
	!at(Ag, P).            														// Comprueba si Ag esta en lugar P
	                                                   
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <-                   					// Si Ag y P estan en la misma habitacion, Ag se mueve directamente a P          
	.println("Al estar en la misma habitación se debe mover directamente a: ", P);
	move_towards(P).  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &					// Si Ag y P estan en habitaciones contiguas, y Ag ESTA en la puerta que comunica con P, se mueve a P
		  connect(RoomAg, RoomP, Door) & atDoor <-
	.println("Al estar en la puerta ", Door, " se dirige a ", P);                        
	move_towards(P); 
	!go(P).       
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// Caso anterior pero Ag NO esta en puerta, pues que valla a la puerta 
		  connect(RoomAg, RoomP, Door) & not atDoor <-
	.println("Al estar en una habitación contigua se mueve hacia la puerta: ", Door);
	move_towards(Door); 
	!go(P).       
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// RoomAg | Room | RoomP : Ag no esta en puerta, que valla a puerta que comunica con habitacion contigua
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & not atDoor <-
	.println("Se mueve a: ", DoorR, " para ir a la habitación contigua, ", Room);
	move_towards(DoorR); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &						// Caso anterior pero Ag esta en puerta que comunica con Room, que se mueva a la puerta de Room que comunica con RoomP
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & atDoor <-
	.println("Se mueve a: ", DoorP, " para ir a la habitación ", RoomP);
	move_towards(DoorP); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <-
	.println("Owner is at ", RoomAg,", that is not a contiguous room to ", RoomP);
	move_towards(P).                                                          
-!go(P) <- .println("Something goes wrong......").
	                                                                        
	
+!get(drug) : .my_name(Name) <- 
   Time = math.ceil(math.random(4000));
   .println("I am waiting ", Time, " ms. before asking the nurse robot for my medicine.");
   .wait(Time);
   .send(enfermera, achieve, has(Name, drug)).

+has(owner,drug) : true <-
   .println("Owner take the drug.");
   !take(drug).
-has(owner,drug) : true <-
   .println("Owner ask for drug. It is time to take it.");
   !get(drug).
                                       
// while I have drug, sip
+!take(drug) : has(owner, drug) <-
   sip(drug);
   .println("Owner is siping the drug.");
   !take(drug).
+!take(drug) : not has(owner, drug) <-
   .println("Owner has finished to take the drug.").//;
   //-asked(drug).

+!check_bored : true
   <- .random(X); .wait(X*5000+2000);  // Owner get bored randomly
      .send(enfermera, askOne, time(_), R); // when bored, owner ask the robot about the time
      .print(R);
	  .send(enfermera, tell, chat("What's the weather in Ourense?"));
      !check_bored.

+msg(M)[source(Ag)] : .my_name(Name)
   <- .print(Ag, " send ", Name, " the message: ", M);
      -msg(M).

	  
