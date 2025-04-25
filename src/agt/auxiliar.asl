!at(Ag, medCab).


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


																