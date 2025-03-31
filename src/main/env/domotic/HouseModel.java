package domotic;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Area;
import jason.environment.grid.Location;
//import jason.asSyntax.*;

// Imports necesarios para A*
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    // --- Constantes de Objetos (sin cambios) ---
    public static final int COLUMN   =     4; // No usado en el código proporcionado, pero lo dejamos
    public static final int CHAIR    =     8;
    public static final int SOFA     =    16;
    public static final int FRIDGE   =    32;
    public static final int WASHER   =    64;
    public static final int DOOR     =   128;
    public static final int CHARGER  =   256; // No usado en el código proporcionado, pero lo dejamos
    public static final int TABLE    =   512;
    public static final int BED      =  1024;
    public static final int WALLV    =  2048; // No usado en el código proporcionado, pero lo dejamos
    public static final int MEDCAB   =  4096; // Medician Cabinet :-> Lugar (unico por ahora) donde se guardan las medicinas

    // --- Configuración del Grid y Agentes (sin cambios) ---
    public static final int GSize = 12;      // Cells
    public final int GridSize = 1080;      // Width (No usado directamente en la lógica del grid, pero lo dejamos)
    private static final int nAgents = 2;      // AGENTES ENTORNO: 0 -> enfermera (robot) , 1 -> owner , 2-> supermarket (no implementado)

    // --- Estado del Modelo (sin cambios) ---
    boolean fridgeOpen   = false;          // comprueba si el fridge esta abierto o no
    boolean medCabOpen   = false;          // comprueba si el medCab está abierto o no
    boolean carryingDrug = false;          // si el robot lleva o no medicina
    boolean carryingBeer = false;          // si el robot lleva o no cerveza (era medicina en el comment)
    int sipCount       = 0;              // (se podria usar para implementar bebidas en el fridge)
    int drugsCount       = 0;              // Contador para el consumo de medicamentos
    int availableBeers   = 1;              // numero de cervezas disponibles (era medicamentos en el comment)

	HashMap<String, Integer> contadorMedicamentos = new HashMap<>();
    int availableDrugs = 0; // Se inicializa en constructor DESPUÉS de llenar el map

    // --- Ubicaciones de Objetos (sin cambios) ---
    Location lSofa      = new Location(6, 10);
    Location lChair1    = new Location(8, 9);
    Location lChair3    = new Location(5, 9);
    Location lChair2    = new Location(7, 8);
    Location lChair4    = new Location(6, 8);
    Location lDeliver   = new Location(0, 11);
    Location lWasher    = new Location(4, 0);
    Location lFridge    = new Location(0, 0);
    Location lMedCab    = new Location(0, 2);
    Location lTable     = new Location(6, 9);
    Location lBed2      = new Location(14, 0);
    Location lBed3      = new Location(21,0);
    Location lBed1      = new Location(13, 9);


    // --- Ubicaciones de Puertas (sin cambios) ---
    Location lDoorHome  = new Location(0, 11);
    Location lDoorKit1  = new Location(0, 6);
    Location lDoorKit2  = new Location(7, 5);
    Location lDoorSal1  = new Location(3, 11);
    Location lDoorSal2  = new Location(11, 6);
    Location lDoorBed1  = new Location(13, 6);
    Location lDoorBed2  = new Location(13, 4);
    Location lDoorBed3  = new Location(23, 4);
    Location lDoorBath1 = new Location(11, 4);
    Location lDoorBath2 = new Location(20, 7);

    // --- Definición de Áreas/Habitaciones (sin cambios) ---
    Area kitchen    = new Area(0, 0, 7, 6);
    Area livingroom = new Area(4, 7, 12, 11);
    Area bath1      = new Area(8, 0, 11, 4);
    Area bath2      = new Area(21, 7, 23, 11);
    Area bedroom1   = new Area(13, 7, 20, 11);
    Area bedroom2   = new Area(12, 0, 17, 4);
    Area bedroom3   = new Area(18, 0, 23, 4);
    Area hall       = new Area(0, 7, 3, 11);
    Area hallway    = new Area(8, 5, 23, 6);

    private Map<Integer, String> directionMap = new HashMap<>();


    /**
     * Constructor del modelo
     */
    public HouseModel() {
        // create a 2*GSize x GSize grid with nAgents mobile agent
        super(2*GSize, GSize, nAgents); // Grid de 24x12

        // Posiciones iniciales de los agentes
        setAgPos(0, 19, 10);   // Posicion partida enfermera (robot)
        setAgPos(1, 23, 8);    // Posicion partida owner

        // Añadir objetos fijos al modelo
        add(MEDCAB, lMedCab);
        add(FRIDGE, lFridge);
        add(WASHER, lWasher);
        add(DOOR,   lDeliver); // La puerta de casa coincide con lDeliver
        add(SOFA,   lSofa);
        add(CHAIR,  lChair2);
        add(CHAIR,  lChair3);
        add(CHAIR,  lChair4);
        add(CHAIR,  lChair1);
        add(TABLE,  lTable);
        add(BED,    lBed1);
        add(BED,    lBed2);
        add(BED,    lBed3);

        // Añadir puertas al modelo (como obstáculos tipo DOOR)
        add(DOOR, lDoorKit2);
        add(DOOR, lDoorSal1);
        // add(DOOR, lDoorBath1); // Duplicado en el original
        add(DOOR, lDoorBath1);
        add(DOOR, lDoorBed1);
        add(DOOR, lDoorBed2);
        add(DOOR, lDoorKit1);
        add(DOOR, lDoorSal2);
        add(DOOR, lDoorBed3);
        add(DOOR, lDoorBath2);

        // Añadir paredes al modelo
        addWall(7, 0, 7, 4);
        addWall(8, 4, 10, 4);
        addWall(14, 4, 22, 4);
        addWall(18, 0, 18, 3);
        addWall(12, 0, 12, 4);
        addWall(1, 6, 10, 6); // Ajuste posible: Quizás debería ser (0,6) o (1,6)? Basado en lDoorKit1 en (0,6)
        addWall(3, 7, 3, 10);
        addWall(12, 6, 12, 11);
        addWall(20, 8, 20, 11);
        addWall(14, 6, 23, 6); // Intersecta con lDoorBed3(23,4)? No, la puerta está fuera de esta pared.
                               // Intersecta con lDoorBath2(20,7)? No, la puerta está fuera de esta pared.

	    // 2. Añadir 5 medicamentos diferentes con sus cantidades iniciales
		contadorMedicamentos.put("Paracetamol 500mg", 3); // Añade Paracetamol 
		contadorMedicamentos.put("Ibuprofeno 600mg", 2);   // Añade Ibuprofeno
		contadorMedicamentos.put("Amoxicilina 500mg", 1);  // Añade Amoxicilina
		contadorMedicamentos.put("Omeprazol 20mg", 2);   // Añade Omeprazol 
		contadorMedicamentos.put("Loratadina 10mg", 2);   // Añade Loratadina
	 
		this.availableDrugs  = calcularTotalMedicamentos(contadorMedicamentos); 					// numero de medicamentos disponibles
   }

    /**
     * Metodo que devuelve la habitacion de la localización 'thing'
     * @param thing Objeto a localizar
     */
    String getRoom (Location thing){

        if (kitchen.contains(thing)) return "kitchen"; // Comprobación más eficiente
        if (livingroom.contains(thing)) return "livingroom";
        if (bath1.contains(thing)) return "bath1";
        if (bath2.contains(thing)) return "bath2";
        if (bedroom1.contains(thing)) return "bedroom1";
        if (bedroom2.contains(thing)) return "bedroom2";
        if (bedroom3.contains(thing)) return "bedroom3";
        if (hall.contains(thing)) return "hall";
        if (hallway.contains(thing)) return "hallway";

        // Si no está en ninguna habitación definida, podría estar fuera o en un borde
        // Devolver un valor por defecto o lanzar una excepción podría ser apropiado
        // Mantenemos el comportamiento original por ahora:
        System.err.println("Warning: Location " + thing + " not found in any defined room. Defaulting to kitchen.");
        return "kitchen"; // Valor por defecto del código original
    }


    /**
     * Metodo para sentar agentes (sin cambios)
     * @param Ag agente a localizar
     * @param dest destino donde quiere sentarse
     */
    boolean sit(int Ag, Location dest) {
        Location loc = getAgPos(Ag);

        if (loc.isNeigbour(dest)) {      // Si esta cerca del objeto
            setAgPos(Ag, dest);          // Lo sienta (mueve al agente a la ubicación del objeto)
            return true; // Indicar éxito
        }
        // Podría ser útil devolver false si no está cerca
        // System.out.println("Agent " + Ag + " is not neighbour of " + dest + " to sit.");
        return false; // Indicar fallo (no estaba cerca)
    }

    // --- Métodos para abrir/cerrar MedCab y Fridge (sin cambios) ---
    boolean openMedCab() {
        if (!medCabOpen) {
            medCabOpen = true;
            return true;
        } else {
            System.out.println("MedCab already open."); // Mensaje informativo
            return false;
        }
    }

    boolean closeMedCab() {
        if (medCabOpen) {
            medCabOpen = false;
            return true;
        } else {
            System.out.println("MedCab already closed."); // Mensaje informativo
            return false;
        }
    }

    boolean openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;
            return true;
        } else {
            System.out.println("Fridge already open."); // Mensaje informativo
            return false;
        }
    }

    boolean closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false;
            return true;
        } else {
            System.out.println("Fridge already closed."); // Mensaje informativo
            return false;
        }
    }

    /**
     * Verifica si el agente Ag puede moverse a la casilla (x, y).
     * Esta función es usada por el algoritmo A* para determinar los vecinos válidos.
     * (Mantenemos la lógica original)
     */
    boolean canMoveTo (int Ag, int x, int y) {
        // Verifica límites del grid
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
             return false;
        }

        // Verifica si la casilla está libre (sin paredes u otros agentes)
        boolean isCellFree = isFree(x, y);
        if (!isCellFree) {
            return false;
        }

        // Aplica restricciones específicas de objetos según el tipo de agente
        if (Ag == 0) { // Agente 0 es el robot/enfermera
            return !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) &&
                   !hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) &&
                   !hasObject(BED, x, y) && !hasObject(FRIDGE, x, y) && // Añadido por si acaso
                   !hasObject(MEDCAB, x, y); // Añadido por si acaso
                   // Nota: Las puertas (DOOR) no se consideran obstáculos intransitables aquí
        } else { // Otros agentes (ej. owner)
            // El owner puede tener menos restricciones, pero mantenemos las originales:
             return !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y);
             // Podríamos permitir al owner moverse sobre sillas o sofás si tuviera sentido
        }
        // Devolver true por defecto si no es Ag==0 y no hay WASHER/TABLE
        // return true; // Esta línea es alcanzada si Ag != 0 y no hay WASHER/TABLE
    }


    /**
     * Calcula el camino óptimo usando A* y mueve el agente un paso hacia el destino.
     * @param Ag El índice del agente a mover.
     * @param dest La localización destino.
     * @return true si el movimiento (o el intento) se realizó, false si hubo un error.
     */
    boolean moveTowards(int Ag, Location dest) {
        Location start = getAgPos(Ag);

        if (start.equals(dest)) {
            return true;
        }

        Map<Location, Location> cameFrom = new HashMap<>();
        Map<Location, Integer> gScore = new HashMap<>();
        Map<Location, Integer> fScore = new HashMap<>();

        Comparator<Location> fScoreComparator = Comparator.comparingInt(loc -> fScore.getOrDefault(loc, Integer.MAX_VALUE));
        PriorityQueue<Location> openSet = new PriorityQueue<>(fScoreComparator);
        Set<Location> closedSet = new HashSet<>();

        gScore.put(start, 0);
        fScore.put(start, manhattanDistance(start, dest));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Location current = openSet.poll();

            if (current.equals(dest)) {
                List<Location> path = reconstructPath(cameFrom, current);
                if (path.size() > 1) {
                    Location nextStep = path.get(1);

                    // Calcular dirección
                    int dx = nextStep.x - start.x;
                    int dy = nextStep.y - start.y;
                    String dir = "walkr";

                    if (Math.abs(dx) > Math.abs(dy)) {
                        dir = (dx > 0) ? "walk_right" : "walk_left";
                    } else if (Math.abs(dy) > 0) {
                        dir = (dy > 0) ? "walk_down" : "walk_up";
                    }

                    directionMap.put(Ag, dir);
                    setAgPos(Ag, nextStep);
                    return true;
                } else {
                    System.err.println("A* found a path with < 2 steps, but start != dest.");
                    return true;
                }
            }

            closedSet.add(current);

            int[] dx = {0, 0, 1, -1};
            int[] dy = {1, -1, 0, 0};

            for (int i = 0; i < 4; i++) {
                int nextX = current.x + dx[i];
                int nextY = current.y + dy[i];
                Location neighbor = new Location(nextX, nextY);

                if (!neighbor.equals(dest)) {
                    if (!canMoveTo(Ag, nextX, nextY)) continue;
                } else {
                    if (nextX < 0 || nextX >= getWidth() || nextY < 0 || nextY >= getHeight()) continue;
                    if (hasObject(OBSTACLE, nextX, nextY)) continue;
                }

                if (closedSet.contains(neighbor)) continue;

                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;

                if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + manhattanDistance(neighbor, dest));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    } else {
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }

        System.out.println("Agent " + Ag + " could not find a path from " + start + " to " + dest);
        return true;
    }

    public String getLastDirection(int ag) {
        return directionMap.getOrDefault(ag, "walkr");
    }


    /**
     * Calcula la distancia Manhattan entre dos localizaciones.
     */
    private int manhattanDistance(Location a, Location b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Reconstruye el camino desde el nodo final hasta el inicio usando el mapa cameFrom.
     * @param cameFrom Mapa que indica de qué nodo se llegó a cada nodo.
     * @param current El nodo final del camino encontrado.
     * @return Lista de localizaciones que forman el camino (inicio -> fin).
     */
    private List<Location> reconstructPath(Map<Location, Location> cameFrom, Location current) {
        List<Location> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        Collections.reverse(totalPath); // Poner el camino en orden: start -> end
        return totalPath;
    }


    /* Metodos cerveza (sin cambios) */

    boolean getBeer() {
        if (fridgeOpen && availableBeers > 0 && !carryingBeer) {
            availableBeers--;
            carryingBeer = true;
            // view?.update(lFridge.x,lFridge.y) // Assuming view needs update
            // view?.update(getAgPos(0).x, getAgPos(0).y) // Update agent carrying status visually?
            System.out.println("Robot got a beer. Beers left: " + availableBeers);
            return true;
        } else {
            // Mensajes de depuración más específicos
            if (!fridgeOpen) System.out.println("Failed to get beer: Fridge is closed.");
            if (availableBeers <= 0) System.out.println("Failed to get beer: No beers available.");
            if (carryingBeer) System.out.println("Failed to get beer: Robot already carrying a beer.");
            return false;
        }
    }

    boolean addBeer(int n) {
        if (n <= 0) return false; // No añadir cantidad negativa o cero
        availableBeers += n;
        System.out.println("Added " + n + " beers. Total beers: " + availableBeers);
        // view?.update(lFridge.x, lFridge.y); // Update fridge visually
        return true;
    }

    boolean handInBeer() {
        // Asumimos que "handIn" significa dársela al owner (Agente 1)
        // Necesitamos verificar proximidad entre robot (Ag 0) y owner (Ag 1)
        Location robotPos = getAgPos(0);
        Location ownerPos = getAgPos(1);

        if (carryingBeer && robotPos.isNeigbour(ownerPos)) {
            sipCount = 10; // Owner ahora tiene la cerveza para beberla
            carryingBeer = false; // Robot ya no la lleva
            System.out.println("Robot handed beer to owner.");
            // view?.update(ownerPos.x, ownerPos.y); // Update owner visually
            // view?.update(robotPos.x, robotPos.y); // Update robot visually
            return true;
        } else {
             if (!carryingBeer) System.out.println("Failed to hand in beer: Robot not carrying one.");
             if (!robotPos.isNeigbour(ownerPos)) System.out.println("Failed to hand in beer: Robot not near owner.");
            return false;
        }
    }

    boolean sipBeer() {
        // Asumimos que es el owner (Agente 1) quien bebe
        if (sipCount > 0) {
            sipCount--;
            System.out.println("Owner sips beer. Sips left: " + sipCount);
            // view?.update(getAgPos(1).x, getAgPos(1).y); // Update owner visually
            return true;
        } else {
            System.out.println("Owner has no beer to sip.");
            return false;
        }
    }

    /* Metodos medicamentos (sin cambios, pero con mejoras en mensajes y lógica similar a cerveza) */

    boolean getDrug() {
        if (medCabOpen && availableDrugs > 0 && !carryingDrug) {
            availableDrugs--;
            carryingDrug = true;
            System.out.println("Robot got a drug. Drugs left: " + availableDrugs);
            // view?.update(lMedCab.x, lMedCab.y);
            // view?.update(getAgPos(0).x, getAgPos(0).y);
            return true;
        } else {
            if (!medCabOpen) System.out.println("Failed to get drug: MedCab is closed.");
            if (availableDrugs <= 0) System.out.println("Failed to get drug: No drugs available.");
            if (carryingDrug) System.out.println("Failed to get drug: Robot already carrying a drug.");
            return false;
        }
    }

    boolean addDrug(int n) {
         if (n <= 0) return false;
        availableDrugs += n;
        System.out.println("Added " + n + " drugs. Total drugs: " + availableDrugs);
        // view?.update(lMedCab.x, lMedCab.y);
        return true;
    }

    boolean handInDrug() {
        // Asumimos dársela al owner (Ag 1) y verificamos proximidad
        Location robotPos = getAgPos(0);
        Location ownerPos = getAgPos(1);

        if (carryingDrug && robotPos.isNeigbour(ownerPos)) {
            drugsCount = 10; // Owner tiene la medicina para tomarla (o usarla)
            carryingDrug = false;
             System.out.println("Robot handed drug to owner.");
            // view?.update(ownerPos.x, ownerPos.y);
            // view?.update(robotPos.x, robotPos.y);
            return true;
        } else {
            if (!carryingDrug) System.out.println("Failed to hand in drug: Robot not carrying one.");
            if (!robotPos.isNeigbour(ownerPos)) System.out.println("Failed to hand in drug: Robot not near owner.");
            return false;
        }
    }

    boolean sipDrug() { // Renombrado a takeDrug sería más apropiado
        // Asumimos que es el owner (Ag 1) quien la toma
        if (drugsCount > 0) {
            drugsCount--; // Podría representar dosis o tiempo de efecto
            System.out.println("Owner takes drug. Doses/Effect left: " + drugsCount);
            // view?.update(getAgPos(1).x, getAgPos(1).y);
            return true;
        } else {
             System.out.println("Owner has no drug to take.");
            return false;
        }
	}


	boolean obtener_medicamento(String nombreMedicamento) {
			System.out.println("\nIntentando coger: " + nombreMedicamento); // Mensaje de acción
		
			// --- Comprobaciones de Precondiciones ---
			if (!medCabOpen) {
				System.out.println("Error: El armario de medicinas está cerrado.");
				return false;
			}
			if (carryingDrug) {
				System.out.println("Error: El robot ya está llevando un medicamento.");
				return false;
			}
		
			// --- Comprobación del Medicamento Específico y Disponibilidad General ---
			// Primero verificamos si el medicamento específico existe y tiene stock
			if (contadorMedicamentos.containsKey(nombreMedicamento)) {
				int cantidadEspecifica = contadorMedicamentos.get(nombreMedicamento);
		
				if (cantidadEspecifica > 0) {
					// Ahora, aunque no es estrictamente necesario si el HashMap es la fuente de verdad,
					// comprobamos también el contador general como solicitaste,
					// aunque lo lógico sería que availableDrugs refleje la suma del HashMap.
					// Si availableDrugs es 0 pero el HashMap dice que hay, hay una inconsistencia.
					// Vamos a priorizar el HashMap y decrementar availableDrugs si cogemos uno.
					if (availableDrugs > 0) { // Comprobamos si el contador general indica que hay algo
						// --- Éxito: Coger el medicamento ---
						contadorMedicamentos.put(nombreMedicamento, cantidadEspecifica - 1); // Decrementar stock específico
						availableDrugs--;         // Decrementar stock general <<<--- LÍNEA AÑADIDA/MODIFICADA
						carryingDrug = true;      // Actualizar estado del robot
						drugsCount++;             // Incrementar contador total de medicamentos cogidos
						System.out.println("Éxito: Robot ha cogido " + nombreMedicamento + ".");
						System.out.println("Quedan " + contadorMedicamentos.get(nombreMedicamento) + " unidades específicas.");
						System.out.println("Quedan " + availableDrugs + " unidades totales (aprox)."); // Informar del total
						return true;
					} else {
						 // Esto indica una inconsistencia: el HashMap dice que hay, pero el contador general no.
						 System.out.println("Error/Inconsistencia: El inventario específico indica stock, pero el contador general availableDrugs es 0.");
						 return false;
					}
		
				} else {
					// --- Fallo: No quedan unidades específicas ---
					System.out.println("Error: No quedan unidades de " + nombreMedicamento + ".");
					return false;
				}
			} else {
				// --- Fallo: Medicamento no encontrado ---
				System.out.println("Error: El medicamento '" + nombreMedicamento + "' no existe en el inventario.");
				return false;
			}
	}
	
	
		private int calcularTotalMedicamentos(HashMap<String, Integer> inventario) {
			int total = 0;
			// values() devuelve una Collection con todas las cantidades (los valores Integer)
			for (Integer cantidad : inventario.values()) {
				if (cantidad != null) { // Buena práctica asegurarse de que no sea null
				   total += cantidad; // Suma la cantidad de cada medicamento al total
				}
			}
			return total;
		}
    
}