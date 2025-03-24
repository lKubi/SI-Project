package domotic;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Area;
import jason.environment.grid.Location;
//import jason.asSyntax.*;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    // constants for the grid objects

    public static final int COLUMN  =    4;
    public static final int CHAIR  	=    8;
    public static final int SOFA  	=   16;
    public static final int FRIDGE 	=   32;
    public static final int WASHER 	=   64;
	public static final int DOOR 	=  128;                                       
	public static final int CHARGER =  256;
    public static final int TABLE  	=  512;
    public static final int BED	   	= 1024;
    public static final int WALLV   = 2048;

    // the grid size                                                     
    public static final int GSize = 12;        //Cells
	public final int GridSize = 1080;    //Width
                          
    boolean fridgeOpen   = false; // whether the fridge is open                                   
    boolean carryingDrug = false; // whether the robot is carrying drug
    int sipCount        = 0; // how many sip the owner did
    int availableDrugs  = 2; // how many drugs are available
    
	// Initialization of the objects Location on the domotic home scene 
    Location lSofa	 	= new Location(GSize/2, GSize-2); //(6,10)
    Location lChair1  	= new Location(GSize/2+2, GSize-3); //(8,9)
    Location lChair3 	= new Location(GSize/2-1, GSize-3); //(5,9)
    Location lChair2 	= new Location(GSize/2+1, GSize-4); //(7,8)
    Location lChair4 	= new Location(GSize/2, GSize-4); //(6,8)
    Location lDeliver 	= new Location(0, GSize-1); //(0,11)
    Location lWasher 	= new Location(GSize/3, 0);	//(4,0)
    Location lFridge 	= new Location(2, 0); //(2,0)
    Location lTable  	= new Location(GSize/2, GSize-3); //(6,9)
	Location lBed2		= new Location(GSize+2, 0); //(14,0)
	Location lBed3		= new Location(GSize*2-3,0); //(21,0)
	Location lBed1		= new Location(GSize+1, GSize*3/4); //(13,9)

	// Initialization of the doors location on the domotic home scene 
	Location lDoorHome 	= new Location(0, GSize-1); //(0,11)
	Location lDoorKit1	= new Location(0, GSize/2); //(0,10)
	Location lDoorKit2	= new Location(GSize/2+1, GSize/2-1); //(7,5)
	Location lDoorSal1	= new Location(GSize/4, GSize-1); //(3,11)
	Location lDoorSal2	= new Location(GSize+1, GSize/2); //(13,6)
	Location lDoorBed1	= new Location(GSize-1, GSize/2); //(11,6)
	Location lDoorBath1	= new Location(GSize-1, GSize/4+1); //(11,4)
	Location lDoorBed3	= new Location(GSize*2-1, GSize/4+1); //(23,4)
	Location lDoorBed2	= new Location(GSize+1, GSize/4+1); //(13,4)
	Location lDoorBath2	= new Location(GSize*2-4, GSize/2+1); //(20,7)
	
	// Initialization of the area modeling the home rooms      
	Area kitchen 	= new Area(0, 0, GSize/2+1, GSize/2-1); //(0,0,7,5)
	Area livingroom	= new Area(GSize/3, GSize/2+1, GSize, GSize-1); //(4,7,12,11)
	Area bath1	 	= new Area(GSize/2+2, 0, GSize-1, GSize/3); //(8,0,11,4)
	Area bath2	 	= new Area(GSize*2-3, GSize/2+1, GSize*2-1, GSize-1); //(21,7,23,11)
	Area bedroom1	= new Area(GSize+1, GSize/2+1, GSize*2-4, GSize-1); //(13,7,20,11)
	Area bedroom2	= new Area(GSize, 0, GSize*3/4-1, GSize/3); //(12,0,8,4)
	Area bedroom3	= new Area(GSize*3/4, 0, GSize*2-1, GSize/3); //(9,0,23,4)
	Area hall		= new Area(0, GSize/2+1, GSize/4, GSize-1); //(0,7,3,11)
	Area hallway	= new Area(GSize/2+2, GSize/2-1, GSize*2-1, GSize/2); //(8,5,23,6)
	/*
	Modificar el modelo para que la casa sea un conjunto de habitaciones
	Dar un codigo a cada habitación y vincular un Area a cada habitación
	Identificar los objetos de manera local a la habitación en que estén
	Crear un método para la identificación del tipo de agente existente
	Identificar objetos globales que precisen de un único identificador
	*/
	
    public HouseModel() {
        // create a GSize x 2GSize grid with 3 mobile agent
        super(2*GSize, GSize, 2);
                                                                           
        // initial location of robot (column 3, line 3)
        // ag code 0 means the robot
        setAgPos(0, 19, 10);  
		setAgPos(1, 23, 8);
		//setAgPos(2, GSize*2-1, GSize*3/5);

		// Do a new method to create literals for each object placed on
		// the model indicating their nature to inform agents their existence
		
        // initial location of fridge and owner
        add(FRIDGE, lFridge); 
		add(WASHER, lWasher); 
		add(DOOR,   lDeliver); 
		add(SOFA,   lSofa);
		add(CHAIR,  lChair2);
		add(CHAIR,  lChair3);
		add(CHAIR,  lChair4);
        add(CHAIR,  lChair1);  
        add(TABLE,  lTable);  
		add(BED,	lBed1);
		add(BED,	lBed2);
		add(BED,	lBed3);
		
		addWall(GSize/2+1, 0, GSize/2+1, GSize/2-2);  		
		add(DOOR, lDoorKit2);                              
		//addWall(GSize/2+1, GSize/2-1, GSize/2+1, GSize-2);  
		add(DOOR, lDoorSal1); 

		addWall(GSize/2+1, GSize/4+1, GSize-2, GSize/4+1);   
		//addWall(GSize+1, GSize/4+1, GSize*2-1, GSize/4+1);   
 		add(DOOR, lDoorBath1); 
		//addWall(GSize+1, GSize*2/5+1, GSize*2-2, GSize*2/5+1);   
		addWall(GSize+2, GSize/4+1, GSize*2-2, GSize/4+1);   
		addWall(GSize*2-6, 0, GSize*2-6, GSize/4);
		add(DOOR, lDoorBed1); 
		
		addWall(GSize, 0, GSize, GSize/4+1);  
		//addWall(GSize+1, GSize/4+1, GSize, GSize/4+1);  
 		add(DOOR, lDoorBed2); 
		
		addWall(1, GSize/2, GSize-1, GSize/2);            
		add(DOOR, lDoorKit1);                
		add(DOOR, lDoorSal2);

		addWall(GSize/4, GSize/2+1, GSize/4, GSize-2);            
		
		addWall(GSize, GSize/2, GSize, GSize-1);  
		addWall(GSize*2-4, GSize/2+2, GSize*2-4, GSize-1);  
		addWall(GSize+2, GSize/2, GSize*2-1, GSize/2);  
 		add(DOOR, lDoorBed3);  
 		add(DOOR, lDoorBath2);  
		
     }
	
	/**
	 * Metodo que devuelve la habitacion del objeto thing
	 */
	 String getRoom (Location thing){  
		
		String byDefault = "kitchen";	// Si no entra en ningun if, por defecto sera kitchen

		if (bath1.contains(thing)){		// Si la habitacion contiene el objeto
			byDefault = "bath1";		// actualiza respuesta
		};
		if (bath2.contains(thing)){
			byDefault = "bath2";
		};
		if (bedroom1.contains(thing)){
			byDefault = "bedroom1";
		};
		if (bedroom2.contains(thing)){
			byDefault = "bedroom2";
		};
		if (bedroom3.contains(thing)){
			byDefault = "bedroom3";
		};
		if (hallway.contains(thing)){
			byDefault = "hallway";
		};
		if (livingroom.contains(thing)){
			byDefault = "livingroom";
		};
		if (hall.contains(thing)){
			byDefault = "hall";
		};
		return byDefault;
	}

	boolean sit(int Ag, Location dest) { 
		Location loc = getAgPos(Ag);
		if (loc.isNeigbour(dest)) {
			setAgPos(Ag, dest);
		};
		return true;
	}

	boolean openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;
            return true;
        } else {
            return false;
        }
    }

    boolean closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false;
            return true;
        } else {
            return false;
        }
    }   

	boolean canMoveTo (int Ag, int x, int y) {
		if (Ag < 1) {
			return (isFree(x,y) && !hasObject(WASHER,x,y) && !hasObject(TABLE,x,y) &&
		           !hasObject(SOFA,x,y) && !hasObject(CHAIR,x,y));
		} else { 
			return (isFree(x,y) && !hasObject(WASHER,x,y) && !hasObject(TABLE,x,y));
		}
	}
	
	boolean moveTowards(int Ag, Location dest) {
        Location r1 = getAgPos(Ag); 
        Location r2 = getAgPos(Ag); 
		
		if (r1.distance(dest)>0) {
			if (r1.x < dest.x && canMoveTo(Ag,r1.x+1,r1.y)) {
				r1.x++;
			}
			else if (r1.x > dest.x && canMoveTo(Ag,r1.x-1,r1.y)) {
				r1.x--;
			};
			
			if (r1.y < dest.y && r1.distance(dest)>0 && canMoveTo(Ag,r1.x,r1.y+1)) {
				r1.y++;
			}
			else if (r1.y > dest.y && r1.distance(dest)>0 && canMoveTo(Ag,r1.x,r1.y-1)) {  
				r1.y--;
			};
        };
		
		if (r1 == r2 && r1.distance(dest)>0) { // could not move the agent
			if (r1.x == dest.x && canMoveTo(Ag,r1.x+1,r1.y)) {
				r1.x++;
			}; 
			if (r1.x == dest.x && canMoveTo(Ag,r1.x-1,r1.y)) {
				r1.x--;
			};
			if (r1.y == dest.y && canMoveTo(Ag,r1.x,r1.y+1)) {
				r1.y++;
			};   
			if (r1.y == dest.y && canMoveTo(Ag,r1.x,r1.y-1)) { 
				r1.y--;
			};			
		};  
		
		setAgPos(Ag, r1); // move the agent in the grid 
		
        return true;        
    }                                                                                 

    boolean getDrug() {
        if (fridgeOpen && availableDrugs > 0 && !carryingDrug) {
            availableDrugs--;
            carryingDrug = true;
            return true;
        } else {  
			if (fridgeOpen) {
				System.out.println("The fridge is opened. ");
			};
			if (availableDrugs > 0){ 
				System.out.println("The fridge has drugs enough. ");
			};
			if (!carryingDrug){ 
				System.out.println("The robot is not bringing a drug. ");
			};
            return false;
        }
    }

    boolean addDrug(int n) {
        availableDrugs += n;
        //if (view != null)
        //    view.update(lFridge.x,lFridge.y);
        return true;
    }

    boolean handInDrug() {
        if (carryingDrug) {
            sipCount = 10;
            carryingDrug = false;
            //if (view != null)
                //view.update(lOwner.x,lOwner.y);
            return true;
        } else {
            return false;
        }
    }

    boolean sipDrug() {
        if (sipCount > 0) {
            sipCount--;
            //if (view != null)
                //view.update(lOwner.x,lOwner.y);
            return true;
        } else {
            return false;
        }
    }
}
