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
import java.util.Random;

import domotic.AStar;

/**
 * Clase que representa el modelo del entorno para la simulación de un robot
 * doméstico.
 * Extiende GridWorldModel y define objetos, agentes, ubicaciones, habitaciones,
 * estado del entorno y lógica de movimiento.
 */
public class HouseModel extends GridWorldModel {

    // --- Constantes de Objetos ---
<<<<<<< Updated upstream
    
    public static final int COLUMN   =     4;
    public static final int CHAIR    =     8;
    public static final int SOFA     =    16;
    public static final int FRIDGE   =    32;
    public static final int WASHER   =    64;
    public static final int DOOR     =   128;
    public static final int CHARGER  =   256;
    public static final int TABLE    =   512;
    public static final int BED      =  1024;
    public static final int WALLV    =  2048;
    public static final int MEDCAB   =  4096;
=======

    public static final int COLUMN = 4;
    public static final int CHAIR = 8;
    public static final int SOFA = 16;
    public static final int FRIDGE = 32;
    public static final int WASHER = 64;
    public static final int DOOR = 128;
    public static final int CHARGER = 256;
    public static final int TABLE = 512;
    public static final int BED = 1024;
    public static final int WALLV = 2048;
    public static final int MEDCAB = 4096;
>>>>>>> Stashed changes

    // --- Configuración del Grid y Agentes ---

    /** Tamaño del grid en celdas */
    public static final int GSize = 15;
    /** Ancho total del grid (no utilizado directamente) */
    public final int GridSize = 2000;
    /** ID del agente enfermera (robot) */
    public static final int ROBOT_AGENT_ID = 0;
    /** ID del agente dueño (owner) */
    public static final int OWNER_AGENT_ID = 1;

    public static final int AUXILIAR_AGENT_ID = 2;

    /** Número total de agentes definidos */
    private static final int nAgents = 3;

    // --- Estado del Modelo ---

    /** Estado de apertura de la nevera */
    boolean fridgeOpen = false;
    /** Estado de apertura del botiquín */
    boolean medCabOpen = false;
    /** Si el robot lleva medicamentos */
<<<<<<< Updated upstream
    boolean robotCarryingDrug = false; 
    boolean robotCarryingBeer = false;

    boolean auxiliarCarryingDrug = false; 


=======
    boolean robotCarryingDrug = false;
    boolean robotCarryingBeer = false;

    boolean auxiliarCarryingDrug = false;
>>>>>>> Stashed changes

    /** Número de sorbos de cerveza tomados */
    int sipCount = 0;
    /** Número de medicamentos consumidos */
    int drugsCount = 0;
    /** Número de cervezas disponibles */
    int availableBeers = 1;
    /** Contador por nombre de medicamentos */
    HashMap<String, Integer> contadorMedicamentos = new HashMap<>();
    /** Número total de medicamentos disponibles */
    int availableDrugs = 0;

    // --- Ubicaciones de Objetos ---

    Location lSofa = new Location(6, 10);
    Location lChair1 = new Location(8, 9);
    Location lChair3 = new Location(5, 9);
    Location lChair2 = new Location(7, 8);
    Location lChair4 = new Location(6, 8);
    Location lDeliver = new Location(0, 12);
<<<<<<< Updated upstream
    Location lWasher  = new Location(4, 0);
    Location lFridge  = new Location(0, 0);
    Location lMedCab  = new Location(8, 0); 
    Location lTable   = new Location(6, 9);
    Location lBed2    = new Location(14, 0);
    Location lBed3    = new Location(21, 0);
    Location lBed1    = new Location(13, 9);
    Location lCharger = new Location(2,7);

    // --- Ubicaciones de Puertas ---

    Location lDoorHome  = new Location(0, 11);
    Location lDoorKit1  = new Location(0, 6);
    Location lDoorKit2  = new Location(7, 5);
    Location lDoorSal1  = new Location(3, 11);
    Location lDoorSal2  = new Location(11, 6);
    Location lDoorBed1  = new Location(13, 6);
    Location lDoorBed2  = new Location(13, 4);
    Location lDoorBed3  = new Location(23, 4);
=======
    Location lWasher = new Location(4, 0);
    Location lFridge = new Location(0, 0);
    Location lMedCab = new Location(4, 0);
    Location lTable = new Location(6, 9);
    Location lBed2 = new Location(14, 0);
    Location lBed3 = new Location(21, 0);
    Location lBed1 = new Location(13, 9);
    Location lCharger = new Location(2, 7);

    // --- Ubicaciones de Puertas ---

    Location lDoorHome = new Location(0, 11);
    Location lDoorKit1 = new Location(0, 6);
    Location lDoorKit2 = new Location(7, 5);
    Location lDoorSal1 = new Location(3, 11);
    Location lDoorSal2 = new Location(11, 6);
    Location lDoorBed1 = new Location(13, 6);
    Location lDoorBed2 = new Location(13, 4);
    Location lDoorBed3 = new Location(23, 4);
>>>>>>> Stashed changes
    Location lDoorBath1 = new Location(11, 4);
    Location lDoorBath2 = new Location(20, 7);

    // --- Definición de Áreas/Habitaciones ---

    Area kitchen = new Area(0, 0, 7, 6);
    Area livingroom = new Area(4, 7, 12, 12);
    Area bath1 = new Area(8, 0, 11, 4);
    Area bath2 = new Area(21, 7, 23, 12);
    Area bedroom1 = new Area(13, 7, 20, 12);
    Area bedroom2 = new Area(12, 0, 17, 4);
    Area bedroom3 = new Area(18, 0, 23, 4);
    Area hall = new Area(0, 7, 3, 12);
    Area hallway = new Area(8, 5, 23, 6);

    /** Mapa de direcciones de movimiento por agente */
    private Map<Integer, String> directionMap = new HashMap<>();

    private Map<Integer, Integer> agentWaitCounters = new HashMap<>();
    private Random random = new Random();

    private HashMap<String, Location> medicamentosExpiry = new HashMap<>();
    private int currentSimulatedHour = 0;

    /**
     * Constructor del modelo del entorno. Inicializa el grid,
     * las posiciones de los agentes, los objetos fijos (muebles, puertas),
     * las paredes y el inventario inicial de medicamentos.
     */
    public HouseModel() {
        super(2 * GSize - 5, GSize, nAgents); // Grid de 24x12

        // Posiciones iniciales de los agentes
<<<<<<< Updated upstream
        setAgPos(0, 19, 10);   // enfermera
        setAgPos(1, 13, 9);    // owner
        setAgPos(2, 1, 10); // auxiliar

=======
        setAgPos(0, 19, 10); // enfermera
        setAgPos(1, 13, 9); // owner
        setAgPos(2, 1, 10); // auxiliar
>>>>>>> Stashed changes

        // Objetos fijos en el entorno
        add(MEDCAB, lMedCab);
        add(FRIDGE, lFridge);
        add(WASHER, lWasher);
<<<<<<< Updated upstream
        add(DOOR,   lDeliver);
        add(SOFA,   lSofa);
        add(CHAIR,  lChair2);
        add(CHAIR,  lChair3);
        add(CHAIR,  lChair4);
        add(CHAIR,  lChair1);
        add(TABLE,  lTable);
        add(BED,    lBed1);
        add(BED,    lBed2);
        add(BED,    lBed3);
=======
        add(DOOR, lDeliver);
        add(SOFA, lSofa);
        add(CHAIR, lChair2);
        add(CHAIR, lChair3);
        add(CHAIR, lChair4);
        add(CHAIR, lChair1);
        add(TABLE, lTable);
        add(BED, lBed1);
        add(BED, lBed2);
        add(BED, lBed3);
>>>>>>> Stashed changes
        add(CHARGER, lCharger);

        // Puertas
        add(DOOR, lDoorKit2);
        add(DOOR, lDoorSal1);
        add(DOOR, lDoorBath1);
        add(DOOR, lDoorBed1);
        add(DOOR, lDoorBed2);
        add(DOOR, lDoorKit1);
        add(DOOR, lDoorSal2);
        add(DOOR, lDoorBed3);
        add(DOOR, lDoorBath2);

        // Paredes
        addWall(1, 12, 24, 12);
        addWall(24, 0, 24, 11);
        addWall(7, 0, 7, 4);
        addWall(8, 4, 10, 4);
        addWall(14, 4, 22, 4);
        addWall(18, 0, 18, 3);
        addWall(12, 0, 12, 4);
        addWall(1, 6, 10, 6);
        addWall(3, 7, 3, 10);
        addWall(12, 6, 12, 11);
        addWall(20, 8, 20, 11);
        addWall(14, 6, 23, 6);

        // Medicamentos iniciales disponibles
<<<<<<< Updated upstream
        contadorMedicamentos.put("Paracetamol 500mg", 3);
        contadorMedicamentos.put("Ibuprofeno 600mg", 4);
        contadorMedicamentos.put("Amoxicilina 500mg", 8);
        contadorMedicamentos.put("Omeprazol 20mg", 1);
        contadorMedicamentos.put("Loratadina 10mg", 4);
=======
        contadorMedicamentos.put("Paracetamol", 3);
        contadorMedicamentos.put("Ibuprofeno", 4);
        contadorMedicamentos.put("Amoxicilina", 8);
        contadorMedicamentos.put("Omeprazol", 0);
        contadorMedicamentos.put("Loratadina", 4);

        medicamentosExpiry.put("Paracetamol", new Location(0, 30));
        medicamentosExpiry.put("Amoxicilina", new Location(1, 30));
        medicamentosExpiry.put("Omeprazol", new Location(2, 30));
        medicamentosExpiry.put("Ibuprofeno", new Location(3, 30));
        medicamentosExpiry.put("Loratadina", new Location(4, 30));
>>>>>>> Stashed changes

        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
        this.robotCarryingDrug = false;
        this.robotCarryingBeer = false;
        this.auxiliarCarryingDrug = false;
    }

    // *** NUEVO: Método para obtener el mapa de caducidades ***
    public HashMap<String, Location> getMedicamentosExpiry() {
        // El nombre del método puede seguir siendo el mismo si ya no usas la versión
        // antigua
        return this.medicamentosExpiry;
    }

    // *** NUEVO: Getter para el contador de medicamentos si no existía
    // explícitamente ***
    public HashMap<String, Integer> getContadorMedicamentos() {
        return contadorMedicamentos;
    }

    /**
     * Devuelve el nombre de la habitación en la que se encuentra la localización
     * especificada.
     * Si la ubicación no pertenece a ninguna habitación conocida, retorna "kitchen"
     * por defecto
     * y muestra una advertencia en consola.
     *
     * @param thing Localización a identificar dentro del entorno.
     * @return Nombre de la habitación correspondiente.
     */
    String getRoom(Location thing) {
        if (kitchen.contains(thing))
            return "kitchen";
        if (livingroom.contains(thing))
            return "livingroom";
        if (bath1.contains(thing))
            return "bath1";
        if (bath2.contains(thing))
            return "bath2";
        if (bedroom1.contains(thing))
            return "bedroom1";
        if (bedroom2.contains(thing))
            return "bedroom2";
        if (bedroom3.contains(thing))
            return "bedroom3";
        if (hall.contains(thing))
            return "hall";
        if (hallway.contains(thing))
            return "hallway";

        System.err.println("Warning: Location " + thing + " not found in any defined room. Defaulting to kitchen.");
        return "kitchen";
    }

    /**
     * Mueve al agente a la ubicación especificada si está adyacente a ella,
     * simulando que se sienta en ese lugar.
     *
     * @param Ag   ID del agente que desea sentarse.
     * @param dest Ubicación destino (silla, sofá, etc.).
     * @return true si el agente se pudo sentar, false si no estaba cerca.
     */
    boolean sit(int Ag, Location dest) {
        Location loc = getAgPos(Ag);

        if (loc.isNeigbour(dest)) {
            setAgPos(Ag, dest);
            return true;
        }

        return false;
    }

    /**
     * Abre el botiquín si no está ya abierto.
     *
     * @return true si el botiquín fue abierto, false si ya estaba abierto.
     */
    boolean openMedCab() {
        if (!medCabOpen) {
            medCabOpen = true;
            return true;
        } else {
            System.out.println("MedCab already open.");
            return false;
        }
    }

    /**
     * Cierra el botiquín si está abierto.
     *
     * @return true si el botiquín fue cerrado, false si ya estaba cerrado.
     */
    boolean closeMedCab() {
        if (medCabOpen) {
            medCabOpen = false;
            return true;
        } else {
            System.out.println("MedCab already closed.");
            return false;
        }
    }

    /**
     * Abre la nevera si no está ya abierta.
     *
     * @return true si la nevera fue abierta, false si ya estaba abierta.
     */
    boolean openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;
            return true;
        } else {
            System.out.println("Fridge already open.");
            return false;
        }
    }

    /**
     * Cierra la nevera si está abierta.
     *
     * @return true si la nevera fue cerrada, false si ya estaba cerrada.
     */
    boolean closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false;
            return true;
        } else {
            System.out.println("Fridge already closed.");
            return false;
        }
    }

    /**
     * Checks if the agent specified can move to the cell (x, y).
     * Evaluates grid limits, obstacles, collisions, and specific restrictions.
     * (This method remains unchanged and is used by AStar.findPath)
     * 
     * @param Ag ID of the agent.
     * @param x  Target X coordinate.
     * @param y  Target Y coordinate.
     * @return true if movement is valid, false otherwise.
     */
    boolean canMoveTo(int Ag, int x, int y) {
        // ... (Keep your existing implementation of canMoveTo) ...
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return false;
        }
        // Use GridWorldModel.OBSTACLE if that's your wall constant
        if (hasObject(OBSTACLE, x, y)) {
            return false;
        }
        int agentInCell = getAgAtPos(x, y);
        if (agentInCell != -1 && agentInCell != Ag) {
            return false;
        }
<<<<<<< Updated upstream

        if (Ag == ROBOT_AGENT_ID || Ag == OWNER_AGENT_ID || Ag == AUXILIAR_AGENT_ID) { 
            if ((x == 7 && y == 9)  || (x == 7 && y == 10) || (x == 14 && y == 1) ||
                (x == 15 && y == 1) || (x == 15 && y == 0) || (x == 21 && y == 1) ||
                (x == 22 && y == 1) || (x == 22 && y == 0) || (x == 13 && y == 10) ||
                (x == 14 && y == 9) || (x == 14 && y == 10)) {
=======
        // ... rest of your canMoveTo logic ...
        if (Ag == ROBOT_AGENT_ID || Ag == OWNER_AGENT_ID || Ag == AUXILIAR_AGENT_ID) {
            if ((x == 7 && y == 9) || (x == 7 && y == 10) || (x == 14 && y == 1) ||
                    (x == 15 && y == 1) || (x == 15 && y == 0) || (x == 21 && y == 1) ||
                    (x == 22 && y == 1) || (x == 22 && y == 0) || (x == 13 && y == 10) ||
                    (x == 14 && y == 9) || (x == 14 && y == 10)) {
>>>>>>> Stashed changes
                return false;
            }

            return !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) &&
<<<<<<< Updated upstream
                   !hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) &&
                   !hasObject(BED, x, y) && !hasObject(FRIDGE, x, y) &&
                   !hasObject(MEDCAB, x, y) && !hasObject(CHARGER, x, y);
=======
                    !hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) &&
                    !hasObject(BED, x, y) && !hasObject(FRIDGE, x, y) &&
                    !hasObject(MEDCAB, x, y) && !hasObject(CHARGER, x, y);
>>>>>>> Stashed changes
        } else {
            return true;
        }
    }

    /**
     * Encuentra una celda adyacente a currentLoc que esté libre, sea válida para
     * moverse
     * por el agente 'ag', y no sea la celda 'avoidTarget'.
     * Intenta buscar en un orden aleatorio para evitar sesgos.
     *
     * @param ag          ID del agente que se mueve.
     * @param currentLoc  Ubicación actual del agente.
     * @param avoidTarget Celda que se debe evitar (normalmente, la celda bloqueada
     *                    original).
     * @return Una Location válida para apartarse, o null si no se encuentra
     *         ninguna.
     */
    private Location findClearAdjacentCell(int ag, Location currentLoc, Location avoidTarget) {
        // Define los desplazamientos a celdas adyacentes (arriba, abajo, izq, der)
        List<Location> possibleSteps = new ArrayList<>();
        int[] dx = { 0, 0, 1, -1 };
        int[] dy = { 1, -1, 0, 0 }; // abajo, arriba, derecha, izquierda

        // Crea una lista de posibles ubicaciones adyacentes
        for (int i = 0; i < 4; i++) {
            possibleSteps.add(new Location(currentLoc.x + dx[i], currentLoc.y + dy[i]));
        }

        // Baraja la lista para comprobar en orden aleatorio y evitar que siempre
        // elija la misma dirección si hay múltiples opciones.
        Collections.shuffle(possibleSteps);

        // Busca la primera celda válida en la lista barajada
        for (Location potentialStep : possibleSteps) {
            // Comprobaciones:
            // 1. Dentro de los límites del grid (ya incluido en canMoveTo generalmente)
            // 2. Es un movimiento válido según las reglas (canMoveTo)
            // 3. La celda está actualmente vacía (getAgAtPos == -1)
            // 4. No es la celda que originalmente queríamos evitar (avoidTarget)
            if (potentialStep.x >= 0 && potentialStep.x < getWidth() &&
                    potentialStep.y >= 0 && potentialStep.y < getHeight() && // Comprobación básica de límites
                    canMoveTo(ag, potentialStep.x, potentialStep.y) && // ¿Puede el agente moverse AQUI en general?
                    getAgAtPos(potentialStep.x, potentialStep.y) == -1 && // ¿Está VACIA AHORA MISMO?
                    !potentialStep.equals(avoidTarget) // ¿No es la celda que causó el conflicto?
            ) {
                // Encontró una celda válida para apartarse
                return potentialStep;
            }
        }

        // No se encontró ninguna celda adyacente válida y libre
        return null;
    }

    /**
     * Calcula el camino óptimo utilizando A* e intenta mover al agente un paso.
     * Si el siguiente paso está bloqueado por otro agente y no es posible una
     * maniobra lateral, el agente esperará un número aleatorio de ciclos.
     * Si el movimiento ocurre, actualiza la dirección para la animación.
     *
     * @param Ag   ID del agente que se desea mover.
     * @param dest Ubicación destino a la que debe dirigirse el agente.
     * @return true, indicando que el intento de movimiento se procesó (movimiento,
     *         side-step, espera o sin ruta).
     */
    boolean moveTowards(int Ag, Location dest) {
        // --- INICIO: Comprobar contador de espera ---
        // Si el agente tiene ciclos de espera pendientes, decrementa y termina.
        if (agentWaitCounters.getOrDefault(Ag, 0) > 0) {
            int remainingCycles = agentWaitCounters.get(Ag) - 1;
            agentWaitCounters.put(Ag, remainingCycles);
            System.out.println("Agente " + Ag + " esperando (ciclos restantes: " + remainingCycles + ")");
            if (remainingCycles == 0) {
                agentWaitCounters.remove(Ag); // Limpiar si terminó la espera
                System.out.println("Agente " + Ag + " terminó de esperar.");
            }
            // No intentar mover este ciclo, la acción es esperar.
            // directionMap.put(Ag, "idle"); // Opcional: Actualizar animación a 'idle'
            return true; // La acción del ciclo es esperar
        }
        // --- FIN: Comprobar contador de espera ---

<<<<<<< Updated upstream
        // Si ya está en el destino, no hacer nada
=======
        Location start = getAgPos(Ag); // Posición actual

        // 1. Comprobar si ya está en el destino
>>>>>>> Stashed changes
        if (start.equals(dest)) {
            // Ya está en el destino, no necesita moverse ni esperar.
            agentWaitCounters.remove(Ag); // Asegurarse de limpiar cualquier espera residual
            return true;
        }

<<<<<<< Updated upstream
        Map<Location, Location> cameFrom = new HashMap<>();
        Map<Location, Integer> gScore = new HashMap<>();
        Map<Location, Integer> fScore = new HashMap<>();
        Comparator<Location> fScoreComparator = Comparator.comparingInt(loc -> fScore.getOrDefault(loc, Integer.MAX_VALUE));
        PriorityQueue<Location> openSet = new PriorityQueue<>(fScoreComparator); // Nodos por evaluar

        Set<Location> closedSet = new HashSet<>(); // Nodos ya evaluados

        // Inicializar valores para el nodo de inicio
        gScore.put(start, 0);
        fScore.put(start, manhattanDistance(start, dest));
        openSet.add(start);

        // 2. Bucle principal de A*
        while (!openSet.isEmpty()) {
            Location current = openSet.poll(); // Obtener nodo con menor fScore

            // Si hemos llegado al destino
            if (current.equals(dest)) {
                // Reconstruir el camino y mover un paso
                List<Location> path = reconstructPath(cameFrom, current);
                if (path.size() > 1) {
                    Location nextStep = path.get(1);
=======
        // 2. Calcular la ruta con A*
        List<Location> path = AStar.findPath(this, Ag, start, dest);

        // 3. Procesar el resultado de la ruta
        if (path != null && path.size() > 1) {
            // Ruta válida encontrada, obtener el siguiente paso intencionado
            Location nextStep = path.get(1);

            // 4. Comprobar si el siguiente paso está bloqueado AHORA MISMO por OTRO agente
            int agentInNextCell = getAgAtPos(nextStep.x, nextStep.y);

            if (agentInNextCell == -1 || agentInNextCell == Ag) {
                // --- CASO NORMAL: El siguiente paso está libre o soy yo mismo (raro, pero
                // seguro) ---
                // Es seguro moverse al nextStep planeado.
>>>>>>> Stashed changes

                int dx = nextStep.x - start.x;
                int dy = nextStep.y - start.y;
                String dir = "walkr"; // Dirección por defecto
                if (Math.abs(dx) > Math.abs(dy)) {
                    dir = (dx > 0) ? "walk_right" : "walk_left";
                } else if (Math.abs(dy) > 0) {
                    dir = (dy > 0) ? "walk_down" : "walk_up";
                }
                // Si dx=0 y dy=0 (no debería pasar si start!=dest), se queda walkr

                directionMap.put(Ag, dir);
                setAgPos(Ag, nextStep); // Realizar el movimiento
                agentWaitCounters.remove(Ag); // Limpiar contador de espera si se movió
                // System.out.println("Agente " + Ag + " se movió a " + nextStep);

            } else {
                // --- CASO BLOQUEO: El nextStep está ocupado por agentInNextCell ---
                System.out.println(
                        "Agente " + Ag + " - Intento a " + nextStep + " bloqueado por Agente " + agentInNextCell);

                // 5. Intentar Maniobra Lateral (Side Step)
                Location sideStepLocation = findClearAdjacentCell(Ag, start, nextStep);

                if (sideStepLocation != null) {
                    // Encontró una celda libre adyacente para apartarse
                    System.out.println("Agente " + Ag + " realizando maniobra lateral a " + sideStepLocation);

                    // Calcula la dirección para el paso lateral
                    int dx = sideStepLocation.x - start.x;
                    int dy = sideStepLocation.y - start.y;
                    String dir = "walkr"; // Dirección por defecto
                    if (Math.abs(dx) > Math.abs(dy)) {
                        dir = (dx > 0) ? "walk_right" : "walk_left";
                    } else if (Math.abs(dy) > 0) {
                        dir = (dy > 0) ? "walk_down" : "walk_up";
                    }

                    // Mover al agente a la celda lateral
                    directionMap.put(Ag, dir);
                    setAgPos(Ag, sideStepLocation);
                    agentWaitCounters.remove(Ag); // Limpiar contador de espera si se movió (side-step)

                } else {
<<<<<<< Updated upstream
                    // El camino solo tiene el nodo inicial (o está vacío), ¿quizás start == dest?
                    // Ya se manejó al principio. Si llegamos aquí, es un caso raro.
                    System.err.println("A* found a path with < 2 steps, but start != dest.");
                    return true; // Consideramos que no hay movimiento necesario o posible
                }
            }

            closedSet.add(current); // Marcar como evaluado

             // 3. Explorar vecinos (arriba, abajo, izquierda, derecha)
			 int[] dx = {0, 0, 1, -1};
			 int[] dy = {1, -1, 0, 0};
 
			 for (int i = 0; i < 4; i++) {
				 int nextX = current.x + dx[i];
				 int nextY = current.y + dy[i];
				 Location neighbor = new Location(nextX, nextY);
 
				 // --- INICIO DE LA MODIFICACIÓN ---
				 // Validar vecino: Aplicar canMoveTo SÓLO si el vecino NO es el destino final.
				 // Si es el destino final, permitimos que A* lo considere para completar la ruta,
				 // aunque no podamos "pisarlo" según canMoveTo. El movimiento real se detendrá antes.
				 if (!neighbor.equals(dest)) {
					 if (!canMoveTo(Ag, nextX, nextY)) {
						 continue; // Ignorar si no es transitable (pared, obstáculo, fuera de límites)
					 }
				 } else {
					 // Si es el destino, al menos verificamos que esté dentro de los límites del grid
					 // (aunque canMoveTo ya lo haría si se llamase)
					  if (nextX < 0 || nextX >= getWidth() || nextY < 0 || nextY >= getHeight()) {
						 continue;
					  }
					  // También podríamos verificar si es un OBSTACLE (pared), ya que eso sí sería infranqueable.
					  if (hasObject(OBSTACLE, nextX, nextY)){
						 continue;
					  }
					  // No aplicamos el resto de chequeos de canMoveTo (FRIDGE, CHAIR, etc.) aquí.
				 }
				 // --- FIN DE LA MODIFICACIÓN ---
 
 
				 if (closedSet.contains(neighbor)) {
					 continue; // Ignorar si ya fue evaluado
				 }
 
				 // Costo de moverse al vecino (asumimos costo 1 para movimientos ortogonales)
				 int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
 
				 // Si esta ruta hacia el vecino es mejor que cualquier ruta anterior
				 if (tentativeGScore < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
					 // Actualizar información del camino
					 cameFrom.put(neighbor, current);
					 gScore.put(neighbor, tentativeGScore);
					 fScore.put(neighbor, tentativeGScore + manhattanDistance(neighbor, dest));
 
					 // Añadir a openSet si no está ya, o actualizar su prioridad
					 // (Java PriorityQueue maneja esto al re-añadir o si contains es falso)
					 if (!openSet.contains(neighbor)) {
						  openSet.add(neighbor);
					 } else {
						 // Para forzar la actualización de prioridad en Java PQ si ya existe:
						 openSet.remove(neighbor);
						 openSet.add(neighbor);
					 }
				 }
			 }
		 } // Fin del while (!openSet.isEmpty())

        // Si el bucle termina y no se encontró el destino, no hay camino posible
        System.out.println("Agent " + Ag + " could not find a path from " + start + " to " + dest);
        return true; // Indicar que el intento se hizo, aunque no hubo movimiento.
                     // Podría devolverse false si se quiere indicar fallo en encontrar ruta.
=======
                    // No hay espacio para apartarse, inicia espera aleatoria (Deadlock situation)
                    int waitTime = 1 + random.nextInt(3); // Espera entre 1 y 3 ciclos (inclusive)
                    agentWaitCounters.put(Ag, waitTime);
                    System.out
                            .println("Agente " + Ag + " esperando: No hay espacio adyacente libre. Iniciando espera de "
                                    + waitTime + " ciclos.");
                    // No se llama a setAgPos. El agente se queda en 'start'.
                    // directionMap.put(Ag, "idle"); // Opcional: indicar espera en la animación
                }
            }

        } else {
            // No se encontró ruta por A* o el camino solo tenía el punto de inicio.
            // AStar ya debería haber impreso una advertencia si no encontró ruta.
            System.out.println(
                    "Agente " + Ag + " - No se encontró ruta por A* a " + dest + " o ya estaba allí/muy cerca.");
            agentWaitCounters.remove(Ag); // Limpiar espera por si acaso
        }

        // Siempre devuelve true, la acción se procesó (movimiento, side-step, espera o
        // sin ruta).
        return true;
>>>>>>> Stashed changes
    }

    public String getLastDirection(int ag) { // <--- It exists and is public!
        return directionMap.getOrDefault(ag, "walkr");
    }

    /**
<<<<<<< Updated upstream
     * Calcula la distancia de Manhattan entre dos ubicaciones del grid.
     * 
     * @param a Primera ubicación.
     * @param b Segunda ubicación.
     * @return Distancia Manhattan entre ambas ubicaciones.
     */
    private int manhattanDistance(Location a, Location b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Reconstruye el camino desde el nodo final hasta el inicio utilizando el mapa de procedencia.
     *
     * @param cameFrom Mapa que indica para cada nodo desde qué nodo se llegó.
     * @param current Nodo final del camino.
     * @return Lista de ubicaciones que representan el camino desde el inicio hasta el destino.
     */
    private List<Location> reconstructPath(Map<Location, Location> cameFrom, Location current) {
        List<Location> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(current);
        }
        Collections.reverse(totalPath);
        return totalPath;
    }

     /**
     * Permite al robot tomar una cerveza de la nevera si está abierta,
     * hay stock disponible y no está cargando una actualmente.
     * (Método actualizado para usar robotCarryingBeer)
     * @return true si la acción se realizó con éxito, false si falló por alguna condición.
     */
    boolean getBeer() {
        if (fridgeOpen && availableBeers > 0) { // <-- MODIFICADO
            availableBeers--;
=======
     * Permite al robot tomar una cerveza de la nevera si está abierta,
     * hay stock disponible y no está cargando una actualmente.
     * (Método actualizado para usar robotCarryingBeer)
     * 
     * @return true si la acción se realizó con éxito, false si falló por alguna
     *         condición.
     */
    boolean getBeer() {
        // Primero, comprueba si el frigorífico está cerrado. Es la condición más
        // restrictiva.
        if (!fridgeOpen) {
            System.out.println("Failed to get beer: Fridge is closed.");
            return false; // No se puede coger si está cerrado
        }

        // Si llegamos aquí, el frigorífico está abierto. Ahora comprobamos si hay
        // cervezas.
        if (availableBeers > 0) {
            // Hay cervezas y el frigo está abierto: coger una.
            availableBeers--; // Decrementa el contador
>>>>>>> Stashed changes
            System.out.println("Robot got a beer. Beers left: " + availableBeers);

            // *** INICIO DE LA LÓGICA DE AUTO-RELLENADO ***
            // Comprueba SI DESPUÉS de coger la cerveza, el contador ha llegado a 0.
            if (availableBeers == 0) {
                System.out.println("Fridge empty! Automatically refilling beers...");
                availableBeers = 10; // Rellena automáticamente a 10
                System.out.println("Fridge refilled. Beers available now: " + availableBeers);
            }
            // *** FIN DE LA LÓGICA DE AUTO-RELLENADO ***

            return true; // La acción de coger la cerveza fue exitosa

        } else {
            // El frigorífico está abierto, pero no quedaban cervezas ANTES de intentar
            // coger.
            System.out.println("Failed to get beer: No beers available (fridge is open).");
            return false; // No se pudo coger porque no había
        }
    }

    /**
     * Añade una cantidad de cervezas al inventario del frigorífico.
     *
     * @param n Cantidad de cervezas a añadir.
     * @return true si se añadieron correctamente, false si la cantidad es inválida.
     */
    boolean addBeer(int n) {
        if (n <= 0)
            return false;
        availableBeers += n;
        System.out.println("Added " + n + " beers. Total beers: " + availableBeers);
        return true;
    }

    /**
     * Entrega la cerveza al dueño si el robot la está cargando y está cerca de él.
     * La acción reinicia el contador de sorbos y libera la carga del robot.
     *
     * @return true si la entrega fue exitosa, false si falló por distancia o falta
     *         de carga.
     */
    boolean handInBeer() {
        Location robotPos = getAgPos(ROBOT_AGENT_ID); // Usar constante
        Location ownerPos = getAgPos(OWNER_AGENT_ID); // Usar constante
        boolean toret;

        // Comprobar si las posiciones son válidas
        if (robotPos == null || ownerPos == null) {
<<<<<<< Updated upstream
             System.out.println("Failed to hand in beer: Agent position not found.");
             return false;
        }

        if (robotCarryingBeer && robotPos.isNeigbour(ownerPos)) { // <-- MODIFICADO
            sipCount = 10;       // Owner ahora tiene la cerveza
=======
            System.out.println("Failed to hand in beer: Agent position not found.");
            return false;
        }

        if (robotCarryingBeer && robotPos.isNeigbour(ownerPos)) { // <-- MODIFICADO
            sipCount = 1; // Owner ahora tiene la cerveza
>>>>>>> Stashed changes
            robotCarryingBeer = false; // Robot ya no la lleva <-- MODIFICADO
            System.out.println("Robot handed beer to owner.");
            toret = true;
        } else {
<<<<<<< Updated upstream
            if (!robotCarryingBeer) System.out.println("Failed to hand in beer: Robot not carrying one."); // <-- MODIFICADO
            if (!robotPos.isNeigbour(ownerPos)) System.out.println("Failed to hand in beer: Robot not near owner.");
=======
            if (!robotCarryingBeer)
                System.out.println("Failed to hand in beer: Robot not carrying one."); // <-- MODIFICADO
            if (!robotPos.isNeigbour(ownerPos))
                System.out.println("Failed to hand in beer: Robot not near owner.");
>>>>>>> Stashed changes
            toret = false;
        }
        return toret;
    }

    /**
     * Simula que el dueño toma un sorbo de la cerveza si tiene disponible.
     * Decrementa el contador de sorbos disponibles.
     *
     * @return true si el sorbo fue posible, false si no hay cerveza.
     */
    boolean sipBeer() {
        if (sipCount > 0) {
            sipCount--;
            System.out.println("Owner sips beer. Sips left: " + sipCount);
            return true;
        } else {
            System.out.println("Owner has no beer to sip.");
            return false;
        }
    }

    /**
     * Añade una cantidad de unidades a cada tipo de medicamento disponible en el
     * botiquín.
     * Recalcula el total de medicamentos disponibles tras la actualización.
     *
     * @param n Cantidad a añadir por cada tipo de medicamento.
     * @return true si la operación fue exitosa, false si la cantidad es inválida.
     */
    boolean addDrug(int n) {
        if (n <= 0) {
            System.out.println("Error: Cannot add zero or negative quantity of drugs.");
            return false;
        }

        System.out.println("Attempting to restock: Adding " + n + " units to EACH drug type.");

        for (String drugName : contadorMedicamentos.keySet()) {
            int currentCount = contadorMedicamentos.getOrDefault(drugName, 0);
            contadorMedicamentos.put(drugName, currentCount + n);
            System.out.println("  - Added " + n + " units to '" + drugName + "'. New count: "
                    + contadorMedicamentos.get(drugName));
        }

        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

        System.out.println("Restock complete. Total available drugs now: " + availableDrugs);
        return true;
    }

<<<<<<< Updated upstream
   
   /**
     * Entrega un medicamento específico al dueño si el agente especificado (robot o auxiliar)
     * lo está cargando y se encuentra cerca del dueño.
     * Actualiza el estado del dueño y libera la carga del agente que entrega.
     *
     * @param agentId  ID del agente que realiza la entrega (0 para robot, 2 para auxiliar). // <-- Parámetro añadido
     * @param drugName El nombre del medicamento que se está entregando (usado para log).
     * @return true si la entrega fue exitosa, false si no se cumple alguna condición.
     */
    boolean handInDrug(int agentId, String drugName) {
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot" :
                           (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId);
=======
    /**
     * Entrega un medicamento específico al dueño si el agente especificado (robot o
     * auxiliar)
     * lo está cargando y se encuentra cerca del dueño.
     * Actualiza el estado del dueño y libera la carga del agente que entrega.
     *
     * @param agentId  ID del agente que realiza la entrega (0 para robot, 2 para
     *                 auxiliar). // <-- Parámetro añadido
     * @param drugName El nombre del medicamento que se está entregando (usado para
     *                 log).
     * @return true si la entrega fue exitosa, false si no se cumple alguna
     *         condición.
     */
    boolean handInDrug(int agentId, String drugName) {
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot"
                : (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId);
>>>>>>> Stashed changes
        Location agentPos = getAgPos(agentId);
        Location ownerPos = getAgPos(OWNER_AGENT_ID);

        // --- Verificaciones iniciales (sin cambios) ---
<<<<<<< Updated upstream
        if (agentPos == null) { /* ... */ return false; }
        if (ownerPos == null) { /* ... */ return false; }
        if (agentId != ROBOT_AGENT_ID && agentId != AUXILIAR_AGENT_ID) { /* ... */ return false; }
=======
        if (agentPos == null) {
            /* ... */ return false;
        }
        if (ownerPos == null) {
            /* ... */ return false;
        }
        if (agentId != ROBOT_AGENT_ID && agentId != AUXILIAR_AGENT_ID) {
            /* ... */ return false;
        }
>>>>>>> Stashed changes

        boolean canDeliver = false;
        boolean isCarrying = false;

        if (agentId == ROBOT_AGENT_ID) {
            isCarrying = robotCarryingDrug; // <-- MODIFICADO
            if (isCarrying && agentPos.isNeigbour(ownerPos)) {
                canDeliver = true;
            }
        } else if (agentId == AUXILIAR_AGENT_ID) {
            isCarrying = auxiliarCarryingDrug; // Sin cambios para auxiliar
            if (isCarrying && agentPos.isNeigbour(ownerPos)) {
                canDeliver = true;
            }
        }

        if (canDeliver) {
            drugsCount = 10; // Owner ahora tiene el medicamento listo

            // Libera la carga del agente que entregó
            if (agentId == ROBOT_AGENT_ID) {
                robotCarryingDrug = false; // <-- MODIFICADO
            } else if (agentId == AUXILIAR_AGENT_ID) {
                auxiliarCarryingDrug = false; // Sin cambios para auxiliar
            }

            System.out.println(agentName + " handed '" + drugName + "' to owner.");
            return true;

        } else {
            // Mensajes de error (actualizados para reflejar la variable correcta)
            if (!isCarrying) {
<<<<<<< Updated upstream
                 System.out.println("Failed to hand in drug: " + agentName + " not carrying an item (" + (agentId == ROBOT_AGENT_ID ? "robotCarryingDrug=false" : "auxiliarCarryingDrug=false") + ")."); // <-- MODIFICADO (texto)
            } else if (!agentPos.isNeigbour(ownerPos)) {
                 System.out.println("Failed to hand in drug: " + agentName + " not near owner (Agent at " + agentPos + ", Owner at " + ownerPos + ").");
            } else {
                 System.out.println("Failed to hand in drug ("+agentName+"): Unknown reason (carrying=" + isCarrying + ", near=" + agentPos.isNeigbour(ownerPos) + ").");
=======
                System.out.println("Failed to hand in drug: " + agentName + " not carrying an item ("
                        + (agentId == ROBOT_AGENT_ID ? "robotCarryingDrug=false" : "auxiliarCarryingDrug=false")
                        + ")."); // <-- MODIFICADO (texto)
            } else if (!agentPos.isNeigbour(ownerPos)) {
                System.out.println("Failed to hand in drug: " + agentName + " not near owner (Agent at " + agentPos
                        + ", Owner at " + ownerPos + ").");
            } else {
                System.out.println("Failed to hand in drug (" + agentName + "): Unknown reason (carrying=" + isCarrying
                        + ", near=" + agentPos.isNeigbour(ownerPos) + ").");
>>>>>>> Stashed changes
            }
            return false;
        }
    }

    /**
     * Simula que el dueño toma una dosis del medicamento si está disponible.
     * Disminuye el contador de dosis disponibles.
     *
     * @return true si se consumió una dosis, false si no hay medicamento
     *         disponible.
     */
    boolean sipDrug() {
        if (drugsCount > 0) {
            drugsCount--;
            System.out.println("Owner takes drug. Doses/Effect left: " + drugsCount);
            return true;
        } else {
            System.out.println("Owner has no drug to take.");
            return false;
        }
    }

    /**
     * Permite al robot tomar un medicamento del botiquín si está abierto,
     * hay unidades disponibles y no está cargando otro medicamento.
     *
<<<<<<< Updated upstream
     * @return true si la acción fue exitosa, false si no se cumplen las condiciones necesarias.
=======
     * @return true si la acción fue exitosa, false si no se cumplen las condiciones
     *         necesarias.
>>>>>>> Stashed changes
     */
    boolean getDrug() {
        // Esta acción implícitamente la hace el robot
        if (medCabOpen && availableDrugs > 0 && !robotCarryingDrug) { // <-- MODIFICADO
            availableDrugs--; // Reduce el total general
            // Nota: No se reduce el contador específico aquí, ¿es intencional?
<<<<<<< Updated upstream
            // La acción 'agenteGetSpecificDrug' sí lo hace. Quizás esta acción 'getDrug' es genérica.
=======
            // La acción 'agenteGetSpecificDrug' sí lo hace. Quizás esta acción 'getDrug' es
            // genérica.
>>>>>>> Stashed changes
            robotCarryingDrug = true; // <-- MODIFICADO
            System.out.println("Robot got a generic drug. Drugs left (total): " + availableDrugs);
            return true;
        } else {
<<<<<<< Updated upstream
            if (!medCabOpen) System.out.println("Failed to get drug: MedCab is closed.");
            if (availableDrugs <= 0) System.out.println("Failed to get drug: No drugs available.");
            if (robotCarryingDrug) System.out.println("Failed to get drug: Robot already carrying a drug."); // <-- MODIFICADO
=======
            if (!medCabOpen)
                System.out.println("Failed to get drug: MedCab is closed.");
            if (availableDrugs <= 0)
                System.out.println("Failed to get drug: No drugs available.");
            if (robotCarryingDrug)
                System.out.println("Failed to get drug: Robot already carrying a drug."); // <-- MODIFICADO
>>>>>>> Stashed changes
            return false;
        }
    }

    /**
<<<<<<< Updated upstream
     * Permite que un agente (robot, dueño o auxiliar) tome un medicamento específico del botiquín.
     * Verifica precondiciones como cercanía al botiquín, disponibilidad del medicamento,
     * y si el agente ya lleva un objeto (drug para robot, box para auxiliar) o tiene dosis pendiente (dueño).
     * Si el medicamento está agotado, lo repone automáticamente.
     *
     * @param agentId ID del agente (0 para robot, 1 para dueño, 2 para auxiliar). // <-- Actualizada descripción
=======
     * Permite que un agente (robot, dueño o auxiliar) tome un medicamento
     * específico del botiquín.
     * Verifica precondiciones como cercanía al botiquín, disponibilidad del
     * medicamento,
     * y si el agente ya lleva un objeto (drug para robot, box para auxiliar) o
     * tiene dosis pendiente (dueño).
     * Si el medicamento está agotado, lo repone automáticamente.
     *
     * @param agentId           ID del agente (0 para robot, 1 para dueño, 2 para
     *                          auxiliar). // <-- Actualizada descripción
>>>>>>> Stashed changes
     * @param nombreMedicamento Nombre exacto del medicamento que se desea obtener.
     * @return true si el agente obtuvo el medicamento, false en caso contrario.
     */
    boolean agenteGetSpecificDrug(int agentId, String nombreMedicamento) {
<<<<<<< Updated upstream
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot" :
                           (agentId == OWNER_AGENT_ID ? "Owner" :
                           (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId));
=======
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot"
                : (agentId == OWNER_AGENT_ID ? "Owner"
                        : (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId));
>>>>>>> Stashed changes

        System.out.println("\n" + agentName + " trying to get: " + nombreMedicamento);

        // --- Precondiciones (sin cambios) ---
<<<<<<< Updated upstream
        if (!medCabOpen) { /* ... */ return false; }
        Location agentPos = getAgPos(agentId);
        if (agentPos == null) { /* ... */ return false; }
        if (!agentPos.isNeigbour(lMedCab)) { /* ... */ return false; }
=======
        if (!medCabOpen) {
            /* ... */ return false;
        }
        Location agentPos = getAgPos(agentId);
        if (agentPos == null) {
            /* ... */ return false;
        }
        if (!agentPos.isNeigbour(lMedCab)) {
            /* ... */ return false;
        }
>>>>>>> Stashed changes

        // --- Precondiciones específicas por agente (actualizadas) ---
        if (agentId == ROBOT_AGENT_ID && robotCarryingDrug) { // <-- MODIFICADO
            System.out.println("Error (" + agentName + "): Already carrying a drug.");
            return false;
        }
        if (agentId == OWNER_AGENT_ID && drugsCount > 0) { // Sin cambios para owner
<<<<<<< Updated upstream
            System.out.println("Error (" + agentName + "): Already has a drug ready (drugsCount=" + drugsCount + "). Must take it first.");
=======
            System.out.println("Error (" + agentName + "): Already has a drug ready (drugsCount=" + drugsCount
                    + "). Must take it first.");
            return false;
        }
        if (agentId == AUXILIAR_AGENT_ID && auxiliarCarryingDrug) { // Sin cambios para auxiliar
            System.out.println("Error (" + agentName + "): Already carrying a box/item.");
>>>>>>> Stashed changes
            return false;
        }
        if (agentId == AUXILIAR_AGENT_ID && auxiliarCarryingDrug) { // Sin cambios para auxiliar
            System.out.println("Error (" + agentName + "): Already carrying a box/item.");
            return false;
        }

        // --- Lógica para obtener el medicamento (actualizada) ---
        if (contadorMedicamentos.containsKey(nombreMedicamento)) {
            int cantidadEspecifica = contadorMedicamentos.get(nombreMedicamento);
            boolean justRefilledThis = false;

            if (cantidadEspecifica <= 0) {
                // ... (lógica de auto-rellenado sin cambios) ...
<<<<<<< Updated upstream
                 int refillAmount = 5; // Ejemplo
                 contadorMedicamentos.put(nombreMedicamento, refillAmount);
                 justRefilledThis = true;
                 this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
                 cantidadEspecifica = refillAmount;
                 // ... (Mensajes y update view sin cambios) ...
                 System.out.println("*** Rellenado específico completado. Nueva cantidad de '" + nombreMedicamento + "': " + refillAmount + ". ***");
=======
                int refillAmount = 5; // Ejemplo
                contadorMedicamentos.put(nombreMedicamento, refillAmount);
                justRefilledThis = true;
                this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
                cantidadEspecifica = refillAmount;
                // ... (Mensajes y update view sin cambios) ...
                System.out.println("*** Rellenado específico completado. Nueva cantidad de '" + nombreMedicamento
                        + "': " + refillAmount + ". ***");
>>>>>>> Stashed changes

            }

            // Decrementar contadores
            contadorMedicamentos.put(nombreMedicamento, cantidadEspecifica - 1);
            availableDrugs--;

            // --- MODIFICADO: Actualizar estado del agente correcto ---
            if (agentId == ROBOT_AGENT_ID) {
                robotCarryingDrug = true; // <-- MODIFICADO
            } else if (agentId == OWNER_AGENT_ID) {
                drugsCount = 10; // Owner tiene 1 dosis lista
            } else if (agentId == AUXILIAR_AGENT_ID) {
                auxiliarCarryingDrug = true; // Auxiliar ahora lleva algo
            }

            // --- Mensajes de éxito (sin cambios) ---
            // ...
<<<<<<< Updated upstream
             System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + ".");
             System.out.println("   Specific units left: " + contadorMedicamentos.get(nombreMedicamento));
             System.out.println("   Total units left: " + availableDrugs);
=======
            System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + ".");
            System.out.println("   Specific units left: " + contadorMedicamentos.get(nombreMedicamento));
            System.out.println("   Total units left: " + availableDrugs);
>>>>>>> Stashed changes
            return true;

        } else {
            System.out.println("Error (" + agentName + "): Drug '" + nombreMedicamento + "' not found in inventory.");
            return false;
        }
    }


    /**
     * Añade una cantidad específica de un medicamento al inventario.
     * Si el medicamento no existía previamente, lo agrega al mapa.
     * 
     * @param nombreMedicamento Nombre del medicamento a añadir.
     * @param cantidad          Número de unidades a añadir.
     * @return true si la operación fue exitosa, false si la cantidad es inválida.
     */
    boolean addSpecificDrug(String nombreMedicamento, int cantidad) {
        if (cantidad <= 0)
            return false;
        int cantidadActual = contadorMedicamentos.getOrDefault(nombreMedicamento, 0);
        contadorMedicamentos.put(nombreMedicamento, cantidadActual + cantidad);
        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
        System.out.println("Added " + cantidad + " units of " + nombreMedicamento + ". New count: "
                + contadorMedicamentos.get(nombreMedicamento) + ". Total drugs: " + availableDrugs);
        return true;
    }

    /**
     * Calcula el total de unidades de medicamentos disponibles sumando
     * todas las cantidades en el inventario proporcionado.
     *
     * @param inventario Mapa con los nombres de medicamentos y sus cantidades.
     * @return Total de unidades disponibles en el inventario.
     */
    private int calcularTotalMedicamentos(HashMap<String, Integer> inventario) {
        int total = 0;
        for (Integer cantidad : inventario.values()) {
            if (cantidad != null) {
                total += cantidad;
            }
        }
        return total;
    }

<<<<<<< Updated upstream


    /**
 * Permite al agente auxiliar (ID 2) simular la recogida de una entrega (medicamento/objeto)
 * cuando se encuentra en la puerta de casa (lDoorHome). Establece auxiliarCarryingDrug a true.
 * Solo funciona si el auxiliar no está ya cargando algo.
 *
 * @param agentId El ID del agente que intenta realizar la acción. Debe ser AUXILIAR_AGENT_ID.
 * @return true si el agente auxiliar estaba en la puerta, no llevaba nada y recogió
 * exitosamente la entrega (flag establecido); false en caso contrario.
 */
boolean auxiliarPickUpDelivery(int agentId) {
    // 1. Verificar si es el agente auxiliar
    if (agentId != AUXILIAR_AGENT_ID) {
        System.out.println("Action 'auxiliarPickUpDelivery' is only for the Auxiliar Agent (ID " + AUXILIAR_AGENT_ID + "). Agent " + agentId + " cannot perform it.");
        return false;
    }

    // 2. Obtener la posición del agente
    Location agentPos = getAgPos(agentId);
    if (agentPos == null) {
        System.out.println("Failed to get position for Auxiliar Agent (ID " + agentId + "). Cannot perform pickup.");
        return false;
    }

    // 3. Verificar si está en la ubicación correcta (lDoorHome)
    if (!agentPos.equals(lDoorHome)) {
        System.out.println("Auxiliar Agent is not at the home door (lDoorHome: " + lDoorHome + "). Current position: " + agentPos + ". Cannot pick up delivery.");
        return false;
    }

    // 4. Verificar si ya está cargando algo
    if (auxiliarCarryingDrug) {
        System.out.println("Auxiliar Agent is already carrying a drug/item. Cannot pick up another.");
        return false;
    }

    // 5. Realizar la acción: establecer la bandera
    auxiliarCarryingDrug = true;
    System.out.println("Auxiliar Agent has picked up the delivery at the door (lDoorHome). Now carrying item (auxiliarCarryingDrug = true).");

    // Opcional: Actualizar la vista si es necesario
    // if (view != null) view.update(lDoorHome.x, lDoorHome.y); // Update door location appearance?
    // if (view != null) view.update(agentPos.x, agentPos.y); // Update agent appearance?

    return true;
}

/**
 * Rellena el botiquín estableciendo la cantidad de cada tipo de medicamento existente
 * a un valor fijo (100 unidades). Actualiza el recuento total de medicamentos disponibles.
 * Si no hay medicamentos definidos en el inventario, simplemente informa y actualiza el total (a 0).
 *
 * @return true si el relleno se completó (incluso si no había nada que rellenar),
 * false si ocurre un error grave (ej. el mapa de inventario es null).
 */
boolean refillMedCabFull() {
    final int TARGET_QUANTITY = 100; // La cantidad objetivo fija para cada medicamento

    System.out.println("\n--- Refilling Medicine Cabinet to Full (" + TARGET_QUANTITY + " units each) ---");

    auxiliarCarryingDrug = false;

    
    // Comprobación de seguridad por si el mapa fuera null
    if (contadorMedicamentos == null) {
        System.err.println("CRITICAL ERROR: Medicine inventory map (contadorMedicamentos) is null! Cannot refill.");
        return false; // Indica un fallo grave
    }

    if (contadorMedicamentos.isEmpty()) {
        System.out.println("Warning: Medicine inventory (contadorMedicamentos) is empty. No specific drug types exist to refill to " + TARGET_QUANTITY + ".");
        // No hay nada que rellenar, pero la operación se considera completada lógicamente.
    } else {
        System.out.println("Setting quantity of each existing drug type to: " + TARGET_QUANTITY);
        // Iterar sobre todas las claves existentes (nombres de medicamentos)
        // y establecer su valor a TARGET_QUANTITY en el mapa.
        // Usamos keySet() para obtener los nombres y luego put() para actualizar.
        for (String drugName : contadorMedicamentos.keySet()) {
            int oldCount = contadorMedicamentos.getOrDefault(drugName, 0); // Obtener el valor anterior para el log
            contadorMedicamentos.put(drugName, TARGET_QUANTITY);
            System.out.println("  - Refilled '" + drugName + "': " + oldCount + " -> " + TARGET_QUANTITY);
        }
    }

    // Recalcular el número total de medicamentos disponibles basado en el mapa actualizado
    // Se llama incluso si el mapa estaba vacío, para asegurar que availableDrugs sea 0.
    this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

    System.out.println("Medicine Cabinet refill complete. Total available drugs now: " + this.availableDrugs);
    System.out.println("-----------------------------------------------------------\n");

    // Opcional: Actualizar la vista del botiquín si es necesario
    // if (view != null) view.update(lMedCab.x, lMedCab.y);

    return true; // La operación se considera exitosa
}
=======
    /**
     * Permite al agente auxiliar (ID 2) simular la recogida de una entrega
     * (medicamento/objeto)
     * cuando se encuentra en la puerta de casa (lDoorHome). Establece
     * auxiliarCarryingDrug a true.
     * Solo funciona si el auxiliar no está ya cargando algo.
     *
     * @param agentId El ID del agente que intenta realizar la acción. Debe ser
     *                AUXILIAR_AGENT_ID.
     * @return true si el agente auxiliar estaba en la puerta, no llevaba nada y
     *         recogió
     *         exitosamente la entrega (flag establecido); false en caso contrario.
     */
    boolean auxiliarPickUpDelivery(int agentId) {
        // 1. Verificar si es el agente auxiliar
        if (agentId != AUXILIAR_AGENT_ID) {
            System.out.println("Action 'auxiliarPickUpDelivery' is only for the Auxiliar Agent (ID " + AUXILIAR_AGENT_ID
                    + "). Agent " + agentId + " cannot perform it.");
            return false;
        }

        // 2. Obtener la posición del agente
        Location agentPos = getAgPos(agentId);
        if (agentPos == null) {
            System.out
                    .println("Failed to get position for Auxiliar Agent (ID " + agentId + "). Cannot perform pickup.");
            return false;
        }

        // 3. Verificar si está en la ubicación correcta (lDoorHome)
        if (!agentPos.equals(lDoorHome)) {
            System.out.println("Auxiliar Agent is not at the home door (lDoorHome: " + lDoorHome
                    + "). Current position: " + agentPos + ". Cannot pick up delivery.");
            return false;
        }

        // 4. Verificar si ya está cargando algo
        if (auxiliarCarryingDrug) {
            System.out.println("Auxiliar Agent is already carrying a drug/item. Cannot pick up another.");
            return false;
        }

        // 5. Realizar la acción: establecer la bandera
        auxiliarCarryingDrug = true;
        System.out.println(
                "Auxiliar Agent has picked up the delivery at the door (lDoorHome). Now carrying item (auxiliarCarryingDrug = true).");

        // Opcional: Actualizar la vista si es necesario
        // if (view != null) view.update(lDoorHome.x, lDoorHome.y); // Update door
        // location appearance?
        // if (view != null) view.update(agentPos.x, agentPos.y); // Update agent
        // appearance?

        return true;
    }

    /**
     * Rellena el botiquín poniendo una cantidad fija para cada tipo de medicamento
     * existente
     * y elimina su estado de caducidad anterior.
     *
     * @return true si la operación se completó (incluso si no había nada que
     *         rellenar),
     *         false si ocurrió un error crítico (mapas nulos).
     */
    /**
     * Rellena un medicamento específico a la cantidad objetivo y elimina su
     * registro de caducidad.
     *
     * @param drugNameToRefill El nombre exacto del medicamento a rellenar.
     * @return true si el medicamento se encontró y se rellenó/reseteó
     *         correctamente,
     *         false si el medicamento no existe, los mapas son null o el nombre es
     *         inválido.
     */
    boolean refillSingleDrug(String drugNameToRefill) {
        auxiliarCarryingDrug = false; // Reiniciar el estado de carga del auxiliar
        final int TARGET_QUANTITY = 50; // Puedes mantener la misma cantidad objetivo

        // --- Validación de Entradas ---
        if (drugNameToRefill == null || drugNameToRefill.isEmpty()) {
            System.err.println("ERROR: Invalid drug name provided for refill (null or empty).");
            return false;
        }
        if (this.contadorMedicamentos == null) {
            System.err.println("CRITICAL ERROR: Medicine inventory map (contadorMedicamentos) is null! Cannot refill '"
                    + drugNameToRefill + "'.");
            return false;
        }
        if (this.medicamentosExpiry == null) {
            System.err.println(
                    "CRITICAL ERROR: Medicine expiry map (medicamentosExpiry) is null! Cannot reset expiry for '"
                            + drugNameToRefill + "'.");
            // Podrías decidir continuar solo rellenando cantidad si esto no es crítico,
            // pero por seguridad, es mejor fallar si falta un mapa esperado.
            return false;
        }

        System.out.println("\n--- Attempting to refill specific drug: '" + drugNameToRefill + "' to " + TARGET_QUANTITY
                + " units ---");

        // --- Comprobar si el medicamento existe en el inventario ---
        if (this.contadorMedicamentos.containsKey(drugNameToRefill)) {

            int oldCount = this.contadorMedicamentos.getOrDefault(drugNameToRefill, 0);

            // 1. Actualizar la cantidad SOLO para este medicamento
            this.contadorMedicamentos.put(drugNameToRefill, TARGET_QUANTITY);
            System.out.println("   - Refilled '" + drugNameToRefill + "': " + oldCount + " -> " + TARGET_QUANTITY);

            // 2. Eliminar la entrada de caducidad SOLO para este medicamento
            // ----- LÍNEA CORREGIDA: Cambiar el tipo de la variable -----
            Location oldExpiry = this.medicamentosExpiry.remove(drugNameToRefill); // Ahora oldExpiry es de tipo
                                                                                   // Location

            if (oldExpiry != null) {
                // ----- LÍNEA CORREGIDA: Acceder a .x (hora) y .y (minuto) -----
                System.out.println("     - Reset expiry status for '" + drugNameToRefill
                        + "' (removed entry, was expiring at "
                        + String.format("%02d:%02d", oldExpiry.x, oldExpiry.y) + ")"); // Formateado como HH:MM
            } else {
                System.out.println("     - No previous expiry status found for '" + drugNameToRefill + "' to remove.");
            }

            // 3. Recalcular el total de medicamentos (importante porque la cantidad cambió)
            this.availableDrugs = calcularTotalMedicamentos(this.contadorMedicamentos);
            System.out.println("Refill complete for '" + drugNameToRefill + "'. Total available drugs now: "
                    + this.availableDrugs);
            System.out.println("-----------------------------------------------------------\n");

            // ... (resto del código) ...

            return true; // Éxito

        } else {
            // El medicamento no existe en el inventario
            System.err.println("ERROR: Cannot refill '" + drugNameToRefill
                    + "'. Drug not found in the inventory (contadorMedicamentos).");
            System.out.println("-----------------------------------------------------------\n");
            return false; // Fallo porque no se encontró el medicamento
        }
    }

>>>>>>> Stashed changes
}