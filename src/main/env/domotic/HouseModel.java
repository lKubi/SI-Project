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
    public static final int GSize = 15;      // Cells
    public final int GridSize = 2000;      // Width (No usado directamente en la lógica del grid, pero lo dejamos)
    public static final int ROBOT_AGENT_ID = 0; // enfermera (robot)
    public static final int OWNER_AGENT_ID = 1; // owner
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
    Location lDeliver   = new Location(0, 12);
    Location lWasher    = new Location(4, 0);
    Location lFridge    = new Location(0, 0);
    Location lMedCab    = new Location(0, 2);
    Location lTable     = new Location(6, 9);
    Location lBed2      = new Location(14, 0);
    Location lBed3      = new Location(21,0);
    Location lBed1      = new Location(13, 9);



    // --- Ubicaciones de Puertas (sin cambios) ---
    Location lDoorHome  = new Location(0, 12);  // La puerta de casa coincide con lDeliver
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
    Area livingroom = new Area(4, 7, 12, 12);
    Area bath1      = new Area(8, 0, 11, 4);
    Area bath2      = new Area(21, 7, 23, 12);
    Area bedroom1   = new Area(13, 7, 20, 12);
    Area bedroom2   = new Area(12, 0, 17, 4);
    Area bedroom3   = new Area(18, 0, 23, 4);
    Area hall       = new Area(0, 7, 3, 12);
    Area hallway    = new Area(8, 5, 23, 6);

    private Map<Integer, String> directionMap = new HashMap<>();


    /**
     * Constructor del modelo
     */
    public HouseModel() {
        // create a 2*GSize x GSize grid with nAgents mobile agent
        super(2*GSize-5, GSize, nAgents); // Grid de 24x12

        // Posiciones iniciales de los agentes
        setAgPos(0, 19, 10);   // Posicion partida enfermera (robot)
        setAgPos(1, 13, 9);    // Posicion partida owner

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
        add(DOOR, lDoorBath1);
        add(DOOR, lDoorBed1);
        add(DOOR, lDoorBed2);
        add(DOOR, lDoorKit1);
        add(DOOR, lDoorSal2);
        add(DOOR, lDoorBed3);
        add(DOOR, lDoorBath2);

        // Añadir paredes al modelo
        addWall(1, 12, 24, 12);
        addWall(24, 0, 24, 11);


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
		contadorMedicamentos.put("Paracetamol 500mg", 1); // Añade Paracetamol 
		contadorMedicamentos.put("Ibuprofeno 600mg", 1);   // Añade Ibuprofeno
		contadorMedicamentos.put("Amoxicilina 500mg", 1);  // Añade Amoxicilina
		contadorMedicamentos.put("Omeprazol 20mg", 1);   // Añade Omeprazol 
		contadorMedicamentos.put("Loratadina 10mg", 1);   // Añade Loratadina
	 
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
   /**
     * Verifica si el agente Ag puede moverse a la casilla (x, y).
     * VERSIÓN CON BLOQUEO DE COORDENADAS DIRECTO.
     */
    boolean canMoveTo (int Ag, int x, int y) {
        // 1. Verifica límites del grid
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return false;
        }

        // 2. Verifica OBSTACLES (paredes físicas)
        if (hasObject(OBSTACLE, x, y)) {
             return false;
        }

        // 3. Verifica si hay OTRO agente en la celda
        int agentInCell = getAgAtPos(x,y);
        if (agentInCell != -1 && agentInCell != Ag) {
             return false;
        }
        // (Opcional: usar isFree si maneja agentes y obstáculos)
        // if (!isFree(x, y)) return false;


        // 4. Aplica restricciones específicas según el agente y la celda
        if (Ag == ROBOT_AGENT_ID || Ag == OWNER_AGENT_ID) { // Agente 0 es el robot/enfermera

            // --- INICIO: Bloqueo de coordenadas específicas para el robot ---
            // Comprueba si la celda (x,y) es una de las prohibidas
            // (Coordenadas tomadas de tus definiciones linvisibleWallX válidas)
            if ( (x == 7 && y == 9) ||   
                 (x == 7 && y == 10) ||  
                 (x == 14 && y == 1) ||  
                 (x == 15 && y == 1) ||  
                 (x == 15 && y == 0) ||  
                 (x == 21 && y == 1) ||  
                 (x == 22 && y == 1) ||  
                 (x == 22 && y == 0) ||  
                 (x == 13 && y == 10) || 
                 (x == 14 && y == 9) || 
                 (x == 14 && y == 10) )  
            {
                // System.out.println("Debug: Robot blocked by specific coordinate rule at " + x + "," + y); // Opcional para depurar
                return false; // El robot NO puede entrar en estas celdas específicas
            }
            // --- FIN: Bloqueo de coordenadas específicas ---

            // Si no está bloqueado por coordenadas específicas,
            // aplicar bloqueo por objetos intransitables para el robot.

            return !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) &&
                   !hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) &&
                   !hasObject(BED, x, y) && !hasObject(FRIDGE, x, y) &&
                   !hasObject(MEDCAB, x, y); 

        } else {
             // Comportamiento para otros agentes
             return true; // Por defecto, permitir movimiento
        }
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

    

     boolean addDrug(int n) {
        if (n <= 0) {
            System.out.println("Error: Cannot add zero or negative quantity of drugs.");
            return false;
        }

        System.out.println("Attempting to restock: Adding " + n + " units to EACH drug type.");

        // Iterar sobre todas las claves (nombres de medicamentos) en el HashMap
        for (String drugName : contadorMedicamentos.keySet()) {
            // Obtener la cantidad actual o 0 si no existe (aunque debería existir por el constructor)
            int currentCount = contadorMedicamentos.getOrDefault(drugName, 0);
        
            // Actualizar la cantidad en el HashMap
            contadorMedicamentos.put(drugName, currentCount + n);
            System.out.println("  - Added " + n + " units to '" + drugName + "'. New count: " + contadorMedicamentos.get(drugName));
            
        }

        // MUY IMPORTANTE: Recalcular el total de availableDrugs basado en el HashMap actualizado
        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

        System.out.println("Restock complete. Total available drugs now: " + availableDrugs);

        // Opcional: Notificar a la vista para actualizar el estado visual del MedCab si existe
        // if (view != null) {
        //     view.update(lMedCab.x, lMedCab.y);
        // }

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


/**
     * Acción para que un AGENTE ESPECÍFICO (Robot o Owner) coja un medicamento
     * ESPECÍFICO del MedCab.
     * Reemplaza a robotGetSpecificDrug y ownerGetSpecificDrug.
     *
     * @param agentId El ID del agente que intenta coger el medicamento (0 para Robot, 1 para Owner).
     * @param nombreMedicamento El nombre exacto del medicamento.
     * @return true si el agente cogió el medicamento, false en caso contrario.
     */
    boolean agenteGetSpecificDrug(int agentId, String nombreMedicamento) {
        // Determinar el nombre del agente para los logs
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot" : (agentId == OWNER_AGENT_ID ? "Owner" : "Unknown Agent " + agentId);

        System.out.println("\n" + agentName + " trying to get: " + nombreMedicamento);

        // --- Precondiciones Comunes ---
        if (!medCabOpen) {
            System.out.println("Error (" + agentName + "): MedCab is closed.");
            return false;
        }

        // Verificar proximidad del AGENTE al MedCab
        Location agentPos = getAgPos(agentId);
        if (agentPos == null) { // Comprobación extra por si el ID es inválido
             System.out.println("Error (" + agentName + "): Invalid agent ID " + agentId);
             return false;
        }
        if (!agentPos.isNeigbour(lMedCab)) {
            System.out.println("Error (" + agentName + "): Not near MedCab at " + lMedCab + " (Agent at " + agentPos + ")");
            return false;
        }

        // --- Precondiciones Específicas del Agente ---
        if (agentId == ROBOT_AGENT_ID) {
            if (carryingDrug) {
                System.out.println("Error (" + agentName + "): Already carrying a drug.");
                return false;
            }
        } else if (agentId == OWNER_AGENT_ID) {
            if (drugsCount > 0) {
                // Cambiado a >= 1 para claridad, aunque > 0 es equivalente para int
                System.out.println("Error (" + agentName + "): Already has a drug ready (drugsCount=" + drugsCount + "). Must take it first.");
                return false;
            }
             // No necesitamos comprobar carryingDrug para el owner
        } else {
            System.out.println("Error (" + agentName + "): Unhandled agent ID for preconditions.");
            return false; // Agente no soportado por esta acción específica
        }


        // --- Comprobación del Medicamento (Común) ---
        if (contadorMedicamentos.containsKey(nombreMedicamento)) {
            int cantidadEspecifica = contadorMedicamentos.get(nombreMedicamento);

            if (cantidadEspecifica > 0) {
                // Comprobación de consistencia (Común)
                if (availableDrugs > 0) {
                    // --- Éxito ---
                    // Actualizaciones comunes
                    contadorMedicamentos.put(nombreMedicamento, cantidadEspecifica - 1); // Decrementar stock específico
                    availableDrugs--;                                                    // Decrementar stock general

                    // Actualizaciones específicas del agente tras éxito
                    if (agentId == ROBOT_AGENT_ID) {
                        carryingDrug = true; // Robot ahora lleva el medicamento
                    } else if (agentId == OWNER_AGENT_ID) {
                        drugsCount = 1;      // Owner ahora tiene 1 unidad/dosis lista para tomar
                                             // (Podría ser > 1 si una "toma" consume más de una unidad del stock)
                    }

                    // Mensajes de éxito (Común con detalle del agente)
                    System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + ".");
                    System.out.println("  Specific units left: " + contadorMedicamentos.get(nombreMedicamento));
                    System.out.println("  Total units left: " + availableDrugs);

                    // view?.update... (Si se necesita actualizar la vista)

                    return true; // Éxito al coger el medicamento

                } else {
                    // Inconsistencia: HashMap dice que hay, pero contador total es 0. (Común)
                    System.out.println("Error/Inconsistency (" + agentName + "): Specific drug found ('" + nombreMedicamento +"'=" + cantidadEspecifica + "), but availableDrugs is 0.");
                    // Opcional: Recalcular para intentar corregir
                    // int realTotal = calcularTotalMedicamentos(contadorMedicamentos);
                    // System.out.println("Recalculated total: " + realTotal);
                    // if (realTotal > 0) { this.availableDrugs = realTotal; /* reintentar? */ }
                    return false;
                }
            } else {
                // --- Fallo: No quedan unidades específicas --- (Común)
                System.out.println("Error (" + agentName + "): No units of " + nombreMedicamento + " left.");
                return false;
            }
        } else {
            // --- Fallo: Medicamento no encontrado --- (Común)
            System.out.println("Error (" + agentName + "): Drug '" + nombreMedicamento + "' not found in inventory.");
            return false;
        }
    }
     /** (Opcional) Método genérico para añadir medicamentos (podría usarse para reponer stock) */
     boolean addSpecificDrug(String nombreMedicamento, int cantidad) {
        if (cantidad <= 0) return false;
        int cantidadActual = contadorMedicamentos.getOrDefault(nombreMedicamento, 0);
        contadorMedicamentos.put(nombreMedicamento, cantidadActual + cantidad);
        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos); // Recalcular total
        System.out.println("Added " + cantidad + " units of " + nombreMedicamento + ". New count: " + contadorMedicamentos.get(nombreMedicamento) + ". Total drugs: " + availableDrugs);
        // view?.update...
        return true;
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