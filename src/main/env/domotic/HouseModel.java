package domotic;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Area;
import jason.environment.grid.Location;
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
import java.util.concurrent.ConcurrentHashMap;
import domotic.AStar;

/**
 * Clase que representa el modelo del entorno para la simulación de un robot
 * doméstico.
 * Extiende GridWorldModel y define objetos, agentes, ubicaciones, habitaciones,
 * estado del entorno y lógica de movimiento.
 */
public class HouseModel extends GridWorldModel {

    // --- Constantes de Objetos ---

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

    // --- Constantes de Carga ---
    private static final int ROBOT_CHARGE_DURATION_MINS = 60;
    private static final int AUXILIAR_CHARGE_DURATION_MINS = 30;
    private static final int DEFAULT_CHARGE_DURATION_MINS = 60; // Un valor por si acaso
    public static final int PARTIAL_CHARGE_LIMIT = 3; // Permitidas 3, la 4ta penaliza
    public static final double MAX_ENERGY_PENALTY_FACTOR = 0.90; // Reducción del 10%

    // --- Configuración del Grid y Agentes ---

    /** Tamaño del grid en celdas */
    public static final int GSize = 15;
    /** Ancho total del grid (aumentar tamaño ventana) */
    public final int GridSize = 2000;

    // --- Definición de Agentes ---
    public static final int ROBOT_AGENT_ID = 0;
    public static final int OWNER_AGENT_ID = 1;
    public static final int AUXILIAR_AGENT_ID = 2;

    /** Número total de agentes definidos */
    private static final int nAgents = 3;

    // --- Energía de los Agentes ---
    /** Energía actual por ID de agente (Enfermera y Auxiliar) */
    private Map<Integer, Integer> agentCurrentEnergy = new HashMap<>();
    /** Energía máxima por ID de agente (Enfermera y Auxiliar) */
    private Map<Integer, Integer> agentMaxEnergy = new HashMap<>();

    // --- Estado de Carga ---
    // Mapa para contar cargas parciales por agente
    private Map<Integer, Integer> agentPartialChargeCount = new ConcurrentHashMap<>();
    // Mapa para rastrear los minutos restantes para carga completa (si está
    // cargando)
    private Map<Integer, Integer> agentRemainingChargeTimeMinutes = new ConcurrentHashMap<>();
    // Almacenamos la energía máxima original para calcular la penalización
    // correctamente
    private Map<Integer, Integer> agentOriginalMaxEnergy = new ConcurrentHashMap<>();

    // --- Estado del Modelo ---

    /** Estado de apertura de la nevera */
    boolean fridgeOpen = false;
    /** Estado de apertura del botiquín */
    boolean medCabOpen = false;
    /** Si el robot lleva medicamentos */
    boolean robotCarryingDrug = false;
    boolean robotCarryingBeer = false;

    boolean auxiliarCarryingDrug = false;

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
    Location lWasher = new Location(4, 0);
    Location lFridge = new Location(0, 0);
    Location lMedCab = new Location(4, 0);
    Location lTable = new Location(6, 9);
    Location lBed2 = new Location(14, 0);
    Location lBed3 = new Location(21, 0);
    Location lBed1 = new Location(13, 9);
    Location lCargador = new Location(23, 5);

    // --- Ubicaciones de Puertas ---

    Location lDoorHome = new Location(0, 11);
    Location lDoorKit1 = new Location(0, 6);
    Location lDoorKit2 = new Location(7, 5);
    Location lDoorSal1 = new Location(3, 11);
    Location lDoorSal2 = new Location(11, 6);
    Location lDoorBed1 = new Location(13, 6);
    Location lDoorBed2 = new Location(13, 4);
    Location lDoorBed3 = new Location(23, 4);
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
    private HouseView view;

    public void setView(HouseView view) {
        this.view = view;
    }

    /**
     * Constructor del modelo del entorno. Inicializa el grid,
     * las posiciones de los agentes, los objetos fijos (muebles, puertas),
     * las paredes y el inventario inicial de medicamentos.
     */
    public HouseModel() {
        super(2 * GSize - 5, GSize, nAgents);
        int gridWidth = getWidth(); // Ancho del grid
        int gridHeight = getHeight(); // Alto del grid
        int initialEnergy = gridWidth * gridHeight; // Energía inicial de los agentes

        // Posiciones iniciales de los agentes
        setAgPos(ROBOT_AGENT_ID, 19, 3);
        setAgPos(1, 13, 9);
        setAgPos(2, 1, 10);

        // Energía inicial y máxima de los agentes
        agentCurrentEnergy.put(ROBOT_AGENT_ID, 150);
        agentMaxEnergy.put(ROBOT_AGENT_ID, initialEnergy);
        agentCurrentEnergy.put(AUXILIAR_AGENT_ID, 150);
        agentMaxEnergy.put(AUXILIAR_AGENT_ID, initialEnergy);

        // Inicializamos los contadores de carga parcial y energía máxima
        agentPartialChargeCount.put(ROBOT_AGENT_ID, 0);
        agentPartialChargeCount.put(AUXILIAR_AGENT_ID, 0);
        agentOriginalMaxEnergy.put(ROBOT_AGENT_ID, initialEnergy);
        agentOriginalMaxEnergy.put(AUXILIAR_AGENT_ID, initialEnergy);

        // Definimos los objetos en el grid
        add(MEDCAB, lMedCab);
        add(FRIDGE, lFridge);
        add(WASHER, lWasher);
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
        add(CHARGER, lCargador);
        add(DOOR, lDoorKit2);
        add(DOOR, lDoorSal1);
        add(DOOR, lDoorBath1);
        add(DOOR, lDoorBed1);
        add(DOOR, lDoorBed2);
        add(DOOR, lDoorKit1);
        add(DOOR, lDoorSal2);
        add(DOOR, lDoorBed3);
        add(DOOR, lDoorBath2);

        // Definimos las paredes del entorno
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

        // Medicamentos iniciales
        contadorMedicamentos.put("Paracetamol", 3);
        contadorMedicamentos.put("Ibuprofeno", 4);
        contadorMedicamentos.put("Amoxicilina", 8);
        contadorMedicamentos.put("Omeprazol", 0);
        contadorMedicamentos.put("Loratadina", 4);

        // Caducidad de medicamentos
        medicamentosExpiry.put("Paracetamol", new Location(18, 00));
        medicamentosExpiry.put("Amoxicilina", new Location(1, 00));
        medicamentosExpiry.put("Omeprazol", new Location(2, 00));
        medicamentosExpiry.put("Ibuprofeno", new Location(3, 00));
        medicamentosExpiry.put("Loratadina", new Location(14, 00));

        // Contador de medicamentos
        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

        this.robotCarryingDrug = false;
        this.robotCarryingBeer = false;
        this.auxiliarCarryingDrug = false;
    }

    // --- Getters para Energía ---

    /**
     * Obtiene la energía actual de un agente.
     * 
     * @param agentId ID del agente (0 para Robot, 2 para Auxiliar).
     * @return La energía actual, o 0 si el agente no tiene sistema de energía.
     */
    public synchronized int getCurrentEnergy(int agentId) {
        int toret = 0;
        if (agentId == ROBOT_AGENT_ID || agentId == AUXILIAR_AGENT_ID) {
            toret = agentCurrentEnergy.getOrDefault(agentId, 0);
        }
        return toret;
    }

    /**
     * Obtiene la energía máxima de un agente.
     * 
     * @param agentId ID del agente (0 para Robot, 2 para Auxiliar).
     * @return La energía máxima, o 0 si el agente no tiene sistema de energía.
     */
    public synchronized int getMaxEnergy(int agentId) {
        int toret = 0;
        if (agentId == ROBOT_AGENT_ID || agentId == AUXILIAR_AGENT_ID) {
            toret = agentMaxEnergy.getOrDefault(agentId, 0);
        }
        return toret;
    }

    // --- Getters para Medicamentos ---

    public HashMap<String, Location> getMedicamentosExpiry() {
        return this.medicamentosExpiry;
    }

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
        String toret = "kitchen";
        if (kitchen.contains(thing))
            toret = "kitchen";
        if (livingroom.contains(thing))
            toret = "livingroom";
        if (bath1.contains(thing))
            toret = "bath1";
        if (bath2.contains(thing))
            toret = "bath2";
        if (bedroom1.contains(thing))
            toret = "bedroom1";
        if (bedroom2.contains(thing))
            toret = "bedroom2";
        if (bedroom3.contains(thing))
            toret = "bedroom3";
        if (hall.contains(thing))
            toret = "hall";
        if (hallway.contains(thing))
            toret = "hallway";
        return toret;
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
        boolean toret = false;
        if (loc.isNeigbour(dest)) {
            setAgPos(Ag, dest);
            toret = true;
        }
        return toret;
    }

    /**
     * Abre el botiquín si no está ya abierto.
     *
     * @return true si el botiquín fue abierto, false si ya estaba abierto.
     */
    boolean openMedCab() {
        boolean toret = false;
        if (!medCabOpen) {
            medCabOpen = true;
            System.out.println("MedCab se ha abierto.");
            toret = true;
        } else {
            System.out.println("MedCab ya estaba abierto.");
            toret = true;
        }
        return toret;
    }

    /**
     * Cierra el botiquín si está abierto.
     *
     * @return true si el botiquín fue cerrado, false si ya estaba cerrado.
     */
    boolean closeMedCab() {
        boolean toret = false;
        if (medCabOpen) {
            medCabOpen = false;
            toret = true;
        }
        return toret;
    }

    /**
     * Abre la nevera si no está ya abierta.
     *
     * @return true si la nevera fue abierta, false si ya estaba abierta.
     */
    boolean openFridge() {
        boolean toret = false;
        if (!fridgeOpen) {
            fridgeOpen = true;
            toret = true;
        }
        return toret;
    }

    /**
     * Cierra la nevera si está abierta.
     *
     * @return true si la nevera fue cerrada, false si ya estaba cerrada.
     */
    boolean closeFridge() {
        boolean toret = false;
        if (fridgeOpen) {
            fridgeOpen = false;
            toret = true;
        }
        return toret;
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
        boolean toret = true;

        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            toret = false;
        } else if (hasObject(OBSTACLE, x, y)) {
            toret = false;
        } else {
            int agentInCell = getAgAtPos(x, y);
            if (agentInCell != -1 && agentInCell != Ag) {
                toret = false;
            } else if (Ag == ROBOT_AGENT_ID || Ag == OWNER_AGENT_ID || Ag == AUXILIAR_AGENT_ID) {
                if ((x == 7 && y == 9) || (x == 7 && y == 10) || (x == 14 && y == 1) ||
                        (x == 15 && y == 1) || (x == 15 && y == 0) || (x == 21 && y == 1) ||
                        (x == 22 && y == 1) || (x == 22 && y == 0) || (x == 13 && y == 10) ||
                        (x == 14 && y == 9) || (x == 14 && y == 10)) {
                    toret = false;
                } else {
                    toret = !(hasObject(WASHER, x, y) || hasObject(TABLE, x, y) ||
                            hasObject(SOFA, x, y) || hasObject(CHAIR, x, y) ||
                            hasObject(BED, x, y) || hasObject(FRIDGE, x, y) ||
                            hasObject(MEDCAB, x, y) || hasObject(CHARGER, x, y));
                }
            }
        }

        return toret;
    }

    private Location findClearAdjacentCell(int ag, Location currentLoc, Location avoidTarget) {
        Location toret = null;
        List<Location> possibleSteps = new ArrayList<>();
        int[] dx = { 0, 0, 1, -1 };
        int[] dy = { 1, -1, 0, 0 };

        for (int i = 0; i < 4; i++) {
            possibleSteps.add(new Location(currentLoc.x + dx[i], currentLoc.y + dy[i]));
        }
        Collections.shuffle(possibleSteps);
        for (Location potentialStep : possibleSteps) {
            boolean valid = potentialStep.x >= 0 && potentialStep.x < getWidth() &&
                    potentialStep.y >= 0 && potentialStep.y < getHeight() &&
                    canMoveTo(ag, potentialStep.x, potentialStep.y) &&
                    getAgAtPos(potentialStep.x, potentialStep.y) == -1 &&
                    !potentialStep.equals(avoidTarget);
            if (valid) {
                toret = potentialStep;
                break;
            }
        }
        return toret;
    }

    /**
     * Intenta "empujar" a un agente (blockerAgentId) que está en targetCell
     * a la celda directamente "detrás" de él, según la dirección de movimiento
     * desde pushingAgentStartCell hacia targetCell.
     *
     * @param pushingAgentId        ID del agente que realiza el empuje (ej.
     *                              ROBOT_AGENT_ID).
     * @param pushingAgentStartCell Ubicación actual del agente que empuja.
     * @param targetCell            La celda a la que el pushingAgent quiere moverse
     *                              y que está
     *                              ocupada por el blockerAgentId.
     * @param blockerAgentId        ID del agente que está siendo empujado (ej.
     *                              OWNER_AGENT_ID).
     * @return true si el empuje fue exitoso y ambos agentes se movieron, false en
     *         caso contrario.
     */
    private synchronized boolean attemptPushAgent(int pushingAgentId, Location pushingAgentStartCell,
            Location targetCell, int blockerAgentId) {
        int dx_push = targetCell.x - pushingAgentStartCell.x;
        int dy_push = targetCell.y - pushingAgentStartCell.y;

        Location pushToCell = new Location(targetCell.x + dx_push, targetCell.y + dy_push);

        System.out.println("[PUSH] Agente " + pushingAgentId + " en " + pushingAgentStartCell +
                " intenta empujar a Agente " + blockerAgentId + " de " + targetCell +
                " hacia " + pushToCell);

        if (!(pushToCell.x >= 0 && pushToCell.x < getWidth() &&
                pushToCell.y >= 0 && pushToCell.y < getHeight())) {
            System.out.println("  [PUSH FAILED] " + pushToCell + " está fuera de los límites.");
            return false;
        }

        if (!canMoveTo(blockerAgentId, pushToCell.x, pushToCell.y) || getAgAtPos(pushToCell.x, pushToCell.y) != -1) {
            if (getAgAtPos(pushToCell.x, pushToCell.y) != -1) {
                System.out.println("  [PUSH FAILED] La celda de destino del empuje " + pushToCell
                        + " está ocupada por el agente: " + getAgAtPos(pushToCell.x, pushToCell.y));
            } else {
                System.out.println("  [PUSH FAILED] Agente " + blockerAgentId
                        + " no puede moverse (obstáculo/regla) a la celda de destino del empuje " + pushToCell);
            }
            return false;
        }

        System.out.println("  [PUSH SUCCESS] Agente " + blockerAgentId + " es empujado a " + pushToCell +
                ". Agente " + pushingAgentId + " se mueve a " + targetCell);

        updateAgentDirection(blockerAgentId, targetCell, pushToCell);
        setAgPos(blockerAgentId, pushToCell);

        updateAgentDirection(pushingAgentId, pushingAgentStartCell, targetCell);
        setAgPos(pushingAgentId, targetCell);

        agentWaitCounters.remove(pushingAgentId);
        agentWaitCounters.remove(blockerAgentId);

        return true;
    }

    private void updateAgentDirection(int agentId, Location from, Location to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        String dir = directionMap.getOrDefault(agentId, "walk_down");

        if (dx == 0 && dy == 0) {
        } else if (Math.abs(dx) > Math.abs(dy)) {
            dir = (dx > 0) ? "walk_right" : "walk_left";
        } else {
            dir = (dy > 0) ? "walk_down" : "walk_up";
        }
        directionMap.put(agentId, dir);
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
        if (agentWaitCounters.getOrDefault(Ag, 0) > 0) {
            int remainingCycles = agentWaitCounters.get(Ag) - 1;
            agentWaitCounters.put(Ag, remainingCycles);
            System.out.println("Agente " + Ag + " esperando (ciclos restantes: " + remainingCycles + ")");
            if (remainingCycles == 0) {
                agentWaitCounters.remove(Ag);
                System.out.println("Agente " + Ag + " terminó de esperar.");
            }
            return true;
        }

        Location start = getAgPos(Ag);

        if (start.equals(dest)) {
            agentWaitCounters.remove(Ag);
            return true;
        }

        List<Location> path = AStar.findPath(this, Ag, start, dest);

        if (path != null && path.size() > 1) {
            Location nextStep = path.get(1);

            int agentInNextCell = getAgAtPos(nextStep.x, nextStep.y);

            if (agentInNextCell == -1 || agentInNextCell == Ag) {
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
                agentWaitCounters.remove(Ag);

            } else {
                System.out.println(
                        "Agente " + Ag + " - Intento a " + nextStep + " bloqueado por Agente " + agentInNextCell);

                boolean PUSH_OR_SIDESTEP_ACTION_TAKEN = false;

                if (Ag == ROBOT_AGENT_ID && agentInNextCell == OWNER_AGENT_ID) {
                    System.out.println(
                            "  Robot (" + Ag + ") evaluando empujar a Owner (" + agentInNextCell + ") de " + nextStep);
                    if (attemptPushAgent(Ag, start, nextStep, agentInNextCell)) {
                        PUSH_OR_SIDESTEP_ACTION_TAKEN = true;
                    } else {
                        System.out.println("  Empuje de Owner por Robot falló o no fue posible.");
                    }
                }

                if (!PUSH_OR_SIDESTEP_ACTION_TAKEN) {
                    Location sideStepLocation = findClearAdjacentCell(Ag, start, nextStep);
                    if (sideStepLocation != null) {
                        System.out.println("Agente " + Ag + " realizando maniobra lateral a " + sideStepLocation);
                        updateAgentDirection(Ag, start, sideStepLocation);
                        setAgPos(Ag, sideStepLocation);
                        agentWaitCounters.remove(Ag);
                        PUSH_OR_SIDESTEP_ACTION_TAKEN = true;
                    }
                }

                if (!PUSH_OR_SIDESTEP_ACTION_TAKEN) {
                    int waitTime = 1 + random.nextInt(3);
                    agentWaitCounters.put(Ag, waitTime);
                    System.out.println(
                            "Agente " + Ag + " esperando: No se pudo empujar ni hacer side-step. Iniciando espera de "
                                    + waitTime + " ciclos.");
                }
            }

        } else {
            System.out.println(
                    "Agente " + Ag + " - No se encontró ruta por A* a " + dest + " o ya estaba allí/muy cerca.");
            agentWaitCounters.remove(Ag);
        }

        return true;
    }

    public String getLastDirection(int ag) {
        return directionMap.getOrDefault(ag, "walkr");
    }

    /**
     * Permite al robot tomar una cerveza de la nevera si está abierta,
     * hay stock disponible y no está cargando una actualmente.
     * (Método actualizado para usar robotCarryingBeer)
     * 
     * @return true si la acción se realizó con éxito, false si falló por alguna
     *         condición.
     */
    boolean getBeer() {
        boolean toret = false;
        if (!fridgeOpen) {
            System.out.println("Failed to get beer: Fridge is closed.");
        }

        if (availableBeers > 0) {
            availableBeers--;
            System.out.println("Robot got a beer. Beers left: " + availableBeers);

            if (availableBeers == 0) {
                System.out.println("Fridge empty! Automatically refilling beers...");
                availableBeers = 10;
                System.out.println("Fridge refilled. Beers available now: " + availableBeers);
            }

            toret = true;

        } else {
            System.out.println("Failed to get beer: No beers available (fridge is open).");
        }
        return toret;
    }

    /**
     * Añade una cantidad de cervezas al inventario del frigorífico.
     *
     * @param n Cantidad de cervezas a añadir.
     * @return true si se añadieron correctamente, false si la cantidad es inválida.
     */
    boolean addBeer(int n) {
        boolean toret = true;
        if (n <= 0) {
            toret = false;
        } else {
            availableBeers += n;
            System.out.println("Added " + n + " beers. Total beers: " + availableBeers);
        }
        return toret;
    }

    /**
     * Entrega la cerveza al dueño si el robot la está cargando y está cerca de él.
     * La acción reinicia el contador de sorbos y libera la carga del robot.
     *
     * @return true si la entrega fue exitosa, false si falló por distancia o falta
     *         de carga.
     */
    boolean handInBeer() {
        Location robotPos = getAgPos(ROBOT_AGENT_ID);
        Location ownerPos = getAgPos(OWNER_AGENT_ID);
        boolean toret = false;

        if (robotPos == null || ownerPos == null) {
            System.out.println("Failed to hand in beer: Agent position not found.");
        }

        if (robotCarryingBeer && robotPos.isNeigbour(ownerPos)) {
            sipCount = 1;
            robotCarryingBeer = false;
            System.out.println("Robot handed beer to owner.");
            toret = true;
        } else {
            if (!robotCarryingBeer)
                System.out.println("Failed to hand in beer: Robot not carrying one.");
            if (!robotPos.isNeigbour(ownerPos))
                System.out.println("Failed to hand in beer: Robot not near owner.");
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
        boolean toret = false;
        if (sipCount > 0) {
            sipCount--;
            System.out.println("Owner sips beer. Sips left: " + sipCount);
            toret = true;
        } else {
            System.out.println("Owner has no beer to sip.");
        }
        return toret;
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
        boolean toret = false;
        if (n <= 0) {
            System.out.println("Error: Cannot add zero or negative quantity of drugs.");
        } else {

            System.out.println("Attempting to restock: Adding " + n + " units to EACH drug type.");

            for (String drugName : contadorMedicamentos.keySet()) {
                int currentCount = contadorMedicamentos.getOrDefault(drugName, 0);
                contadorMedicamentos.put(drugName, currentCount + n);
                System.out.println("  - Added " + n + " units to '" + drugName + "'. New count: "
                        + contadorMedicamentos.get(drugName));
            }

            this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

            System.out.println("Restock complete. Total available drugs now: " + availableDrugs);
            toret = true;
        }
        return toret;
    }

    /**
     * Entrega un medicamento específico al dueño si el agente especificado (robot o
     * auxiliar)
     * lo está cargando y se encuentra cerca del dueño.
     * Actualiza el estado del dueño y libera la carga del agente que entrega.
     *
     * @param agentId  ID del agente que realiza la entrega (0 para robot, 2 para
     *                 auxiliar).
     * @param drugName El nombre del medicamento que se está entregando (usado para
     *                 log).
     * @return true si la entrega fue exitosa, false si no se cumple alguna
     *         condición.
     */
    boolean handInDrug(int agentId, String drugName) {
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot"
                : (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId);
        Location agentPos = getAgPos(agentId);
        Location ownerPos = getAgPos(OWNER_AGENT_ID);

        if (agentPos == null) {
            return false;
        }
        if (ownerPos == null) {
            return false;
        }
        if (agentId != ROBOT_AGENT_ID && agentId != AUXILIAR_AGENT_ID) {
            return false;
        }

        boolean canDeliver = false;
        boolean isCarrying = false;

        if (agentId == ROBOT_AGENT_ID) {
            isCarrying = robotCarryingDrug;
            if (isCarrying && agentPos.isNeigbour(ownerPos)) {
                canDeliver = true;
            }
        } else if (agentId == AUXILIAR_AGENT_ID) {
            isCarrying = auxiliarCarryingDrug;
            if (isCarrying && agentPos.isNeigbour(ownerPos)) {
                canDeliver = true;
            }
        }

        if (canDeliver) {
            drugsCount = 10;

            if (agentId == ROBOT_AGENT_ID) {
                robotCarryingDrug = false;
            } else if (agentId == AUXILIAR_AGENT_ID) {
                auxiliarCarryingDrug = false;
            }

            System.out.println(agentName + " handed '" + drugName + "' to owner.");
            return true;

        } else {
            if (!isCarrying) {
                System.out.println("Failed to hand in drug: " + agentName + " not carrying an item ("
                        + (agentId == ROBOT_AGENT_ID ? "robotCarryingDrug=false" : "auxiliarCarryingDrug=false")
                        + ").");
            } else if (!agentPos.isNeigbour(ownerPos)) {
                System.out.println("Failed to hand in drug: " + agentName + " not near owner (Agent at " + agentPos
                        + ", Owner at " + ownerPos + ").");
            } else {
                System.out.println("Failed to hand in drug (" + agentName + "): Unknown reason (carrying=" + isCarrying
                        + ", near=" + agentPos.isNeigbour(ownerPos) + ").");
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
        boolean toret = false;
        if (drugsCount > 0) {
            drugsCount--;
            System.out.println("Owner takes drug. Doses/Effect left: " + drugsCount);
            toret = true;
        } else {
            System.out.println("Owner has no drug to take.");
        }
        return toret;
    }

    /**
     * Permite al robot tomar un medicamento del botiquín si está abierto,
     * hay unidades disponibles y no está cargando otro medicamento.
     *
     * @return true si la acción fue exitosa, false si no se cumplen las condiciones
     *         necesarias.
     */
    boolean getDrug() {
        boolean toret = false;
        if (medCabOpen && availableDrugs > 0 && !robotCarryingDrug) {
            availableDrugs--;
            robotCarryingDrug = true;
            System.out.println("Robot got a generic drug. Drugs left (total): " + availableDrugs);
            toret = true;
        } else {
            if (!medCabOpen)
                System.out.println("Failed to get drug: MedCab is closed.");
            if (availableDrugs <= 0)
                System.out.println("Failed to get drug: No drugs available.");
            if (robotCarryingDrug)
                System.out.println("Failed to get drug: Robot already carrying a drug.");
        }
        return toret;
    }

    /**
     * Permite que un agente (robot, dueño o auxiliar) tome un medicamento
     * específico del botiquín.
     * Verifica precondiciones como cercanía al botiquín, disponibilidad del
     * medicamento,
     * y si el agente ya lleva un objeto (drug para robot, box para auxiliar) o
     * tiene dosis pendiente (dueño).
     * Si el medicamento está agotado, lo repone automáticamente.
     *
     * @param agentId           ID del agente (0 para robot, 1 para dueño, 2 para
     *                          auxiliar).
     * @param nombreMedicamento Nombre exacto del medicamento que se desea obtener.
     * @return true si el agente obtuvo el medicamento, false en caso contrario.
     */
    boolean agenteGetSpecificDrug(int agentId, String nombreMedicamento) {
        boolean toret = true;
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot"
                : (agentId == OWNER_AGENT_ID ? "Owner"
                        : (agentId == AUXILIAR_AGENT_ID ? "Auxiliar" : "Unknown Agent " + agentId));

        System.out.println("\n" + agentName + " trying to get: " + nombreMedicamento);

        if (!medCabOpen) {
            toret = false;
        }
        Location agentPos = getAgPos(agentId);
        if (agentPos == null) {
            toret = false;
        }
        if (!agentPos.isNeigbour(lMedCab)) {
            toret = false;
        }

        if (agentId == ROBOT_AGENT_ID && robotCarryingDrug) {
            System.out.println("Error (" + agentName + "): Already carrying a drug.");
            toret = false;
        }
        if (agentId == OWNER_AGENT_ID && drugsCount > 0) {
            System.out.println("Error (" + agentName + "): Already has a drug ready (drugsCount=" + drugsCount
                    + "). Must take it first.");
            toret = false;
        }
        if (agentId == AUXILIAR_AGENT_ID && auxiliarCarryingDrug) {
            System.out.println("Error (" + agentName + "): Already carrying a box/item.");
            toret = false;
        }

        if (contadorMedicamentos.containsKey(nombreMedicamento) && toret) {
            int cantidadEspecifica = contadorMedicamentos.get(nombreMedicamento);
            boolean justRefilledThis = false;

            if (cantidadEspecifica <= 0) {
                int refillAmount = 5;
                contadorMedicamentos.put(nombreMedicamento, refillAmount);
                justRefilledThis = true;
                this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
                cantidadEspecifica = refillAmount;
                System.out.println("* Rellenado específico completado. Nueva cantidad de '" + nombreMedicamento
                        + "': " + refillAmount + ". *");

            }

            contadorMedicamentos.put(nombreMedicamento, cantidadEspecifica - 1);
            availableDrugs--;

            if (agentId == ROBOT_AGENT_ID) {
                robotCarryingDrug = true;
            } else if (agentId == OWNER_AGENT_ID) {
                drugsCount = 10;
            } else if (agentId == AUXILIAR_AGENT_ID) {
                auxiliarCarryingDrug = true;
            }

            System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + ".");
            System.out.println("   Specific units left: " + contadorMedicamentos.get(nombreMedicamento));
            System.out.println("   Total units left: " + availableDrugs);
        } else {
            System.out.println("Error (" + agentName + "): Drug '" + nombreMedicamento + "' not found in inventory.");
            toret = false;
        }
        return toret;
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
        boolean toret = false;
        if (cantidad <= 0) {
            toret = false;
        } else {
            int cantidadActual = contadorMedicamentos.getOrDefault(nombreMedicamento, 0);
            contadorMedicamentos.put(nombreMedicamento, cantidadActual + cantidad);
            this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
            System.out.println("Added " + cantidad + " units of " + nombreMedicamento + ". New count: "
                    + contadorMedicamentos.get(nombreMedicamento) + ". Total drugs: " + availableDrugs);
            toret = true;
        }
        return toret;
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
        boolean toret = true;
        if (agentId != AUXILIAR_AGENT_ID) {
            System.out.println("Action 'auxiliarPickUpDelivery' is only for the Auxiliar Agent (ID " + AUXILIAR_AGENT_ID
                    + "). Agent " + agentId + " cannot perform it.");
            toret = false;
        }

        Location agentPos = getAgPos(agentId);
        if (agentPos == null) {
            System.out
                    .println("Failed to get position for Auxiliar Agent (ID " + agentId + "). Cannot perform pickup.");
            toret = false;
        }

        if (auxiliarCarryingDrug) {
            System.out.println("Auxiliar Agent is already carrying a drug/item. Cannot pick up another.");
            toret = false;
        }

        if(toret){
        auxiliarCarryingDrug = true;
        System.out.println("Auxiliar Agent has picked up the delivery at the door (lDoorHome). Now carrying item (auxiliarCarryingDrug = true).");
        }

        return toret;
    }

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
        auxiliarCarryingDrug = false;
        final int TARGET_QUANTITY = 50;

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
            return false;
        }

        System.out.println("\n--- Attempting to refill specific drug: '" + drugNameToRefill + "' to " + TARGET_QUANTITY
                + " units ---");

        if (this.contadorMedicamentos.containsKey(drugNameToRefill)) {

            int oldCount = this.contadorMedicamentos.getOrDefault(drugNameToRefill, 0);

            this.contadorMedicamentos.put(drugNameToRefill, TARGET_QUANTITY);
            System.out.println("   - Refilled '" + drugNameToRefill + "': " + oldCount + " -> " + TARGET_QUANTITY);

            Location oldExpiry = this.medicamentosExpiry.remove(drugNameToRefill);

            if (oldExpiry != null) {
                System.out.println("     - Reset expiry status for '" + drugNameToRefill
                        + "' (removed entry, was expiring at "
                        + String.format("%02d:%02d", oldExpiry.x, oldExpiry.y) + ")");
            } else {
                System.out.println("     - No previous expiry status found for '" + drugNameToRefill + "' to remove.");
            }

            this.availableDrugs = calcularTotalMedicamentos(this.contadorMedicamentos);
            System.out.println("Refill complete for '" + drugNameToRefill + "'. Total available drugs now: "
                    + this.availableDrugs);
            System.out.println("-----------------------------------------------------------\n");

            return true;

        } else {
            System.err.println("ERROR: Cannot refill '" + drugNameToRefill
                    + "'. Drug not found in the inventory (contadorMedicamentos).");
            System.out.println("-----------------------------------------------------------\n");
            return false;
        }
    }

    /**
     * Decrementa la energía del agente especificado en una cantidad dada.
     * No permite que la energía baje de 0.
     *
     * @param agentId El ID del agente (0 o 2).
     * @param amount  La cantidad a decrementar (debería ser positivo).
     */
    public synchronized void decrementAgentEnergy(int agentId, int amount) {
        if (amount <= 0) {
            System.err.println("Warning: Attempted to decrement energy by non-positive amount: " + amount);
        }else{

        if (agentId == ROBOT_AGENT_ID || agentId == AUXILIAR_AGENT_ID) {
            int currentEnergy = agentCurrentEnergy.getOrDefault(agentId, 0);
            int newEnergy = Math.max(0, currentEnergy - amount);
            agentCurrentEnergy.put(agentId, newEnergy);

            if (view != null) {
                view.updateEnergyDisplay(agentId, newEnergy, getMaxEnergy(agentId));
            }

        } else {
            System.err.println("Warning: Attempted to decrement energy for agent without energy system: " + agentId);
        }
        }
    }

    // --- NUEVOS MÉTODOS AUXILIARES PARA CARGA ---

    /**
     * Devuelve la ubicación del cargador designado para un agente específico.
     * 
     * @param agentId ID del agente (0 o 2).
     * @return La Location del cargador o null si el agente no tiene uno.
     */
    public Location getChargerLocationForAgent(int agentId) {
        Location toret = null;
        if (agentId == ROBOT_AGENT_ID || agentId == AUXILIAR_AGENT_ID) {
            toret = lCargador;
        }
        return toret;
    }

    /**
     * Verifica si un agente está actualmente en proceso de carga.
     * 
     * @param agentId ID del agente.
     * @return true si está cargando (tiene tiempo restante en el mapa), false si
     *         no.
     */
    public boolean isAgentCharging(int agentId) {
        return agentRemainingChargeTimeMinutes.containsKey(agentId);
    }

    /**
     * Obtiene el conjunto de IDs de los agentes que están actualmente cargando.
     * Útil para que el reloj sepa a quién aplicar la carga.
     * 
     * @return Un Set con los IDs de los agentes en carga.
     */
    public Set<Integer> getChargingAgents() {
        return new HashSet<>(agentRemainingChargeTimeMinutes.keySet());
    }

    /**
     * Inicia el proceso de carga si el agente está cerca del ÚNICO cargador.
     * PERMITE USO SIMULTÁNEO (no hay chequeo de ocupación).
     * 
     * @param agentId ID del agente (0 o 2).
     * @return true si la carga se inició, false si no está cerca o ya estaba
     *         cargando.
     */
    public synchronized boolean startCharging(int agentId) {
        int chargeDuration;
        if (agentId == ROBOT_AGENT_ID) {
            chargeDuration = ROBOT_CHARGE_DURATION_MINS;
        } else if (agentId == AUXILIAR_AGENT_ID) {
            chargeDuration = AUXILIAR_CHARGE_DURATION_MINS;
        } else {
            System.err.println(
                    "Advertencia: Agente inesperado " + agentId + " intentando cargar. Usando duración por defecto.");
            chargeDuration = DEFAULT_CHARGE_DURATION_MINS;
        }

        agentRemainingChargeTimeMinutes.put(agentId, chargeDuration);
        System.out.println(
                "Agente " + agentId + " ha iniciado la carga. Tiempo restante inicial: " + chargeDuration + " mins.");

        if (view != null) {
            view.updateEnergyDisplay(agentId, getCurrentEnergy(agentId), getMaxEnergy(agentId));
        }
        return true;
    }

    /**
     * Detiene el proceso de carga para un agente.
     * Gestiona carga parcial y penalización.
     * (Ya no necesita liberar cargador, permite uso simultáneo).
     * 
     * @param agentId ID del agente (0 o 2).
     * @return true si estaba cargando y se detuvo, false si no.
     */
    public synchronized boolean stopCharging(int agentId) {
        if (agentId != ROBOT_AGENT_ID && agentId != AUXILIAR_AGENT_ID) {
            System.err.println("Error: stopCharging llamado para ID de agente inválido: " + agentId);
            return false;
        }
        if (!agentRemainingChargeTimeMinutes.containsKey(agentId)) {
            System.out.println("[MODELO] Agente " + agentId
                    + ": stopCharging llamado, pero el agente no estaba en el mapa de tiempo restante de carga (quizás ya se detuvo o nunca empezó).");
            if (view != null) {
                view.updateEnergyDisplay(agentId, getCurrentEnergy(agentId), getMaxEnergy(agentId));
            }
            return false;
        }

        int remainingTimeInMap = agentRemainingChargeTimeMinutes.getOrDefault(agentId, 0);
        int currentEnergy = getCurrentEnergy(agentId);
        int maxEnergy = getMaxEnergy(agentId);

        boolean isEffectivelyFull = (currentEnergy >= maxEnergy);

        boolean isConsideredPenaltyPartialStop = !isEffectivelyFull && (remainingTimeInMap > 0);

        if (isConsideredPenaltyPartialStop) {
            System.out.println(
                    "[MODELO] Agente " + agentId + ": DETECTADA PARADA PARCIAL (CON POSIBLE PENALIZACIÓN). Energía: "
                            + currentEnergy + "/" + maxEnergy + ", Tiempo restante en mapa: " + remainingTimeInMap);

            int currentPartialCount = agentPartialChargeCount.getOrDefault(agentId, 0) + 1;
            agentPartialChargeCount.put(agentId, currentPartialCount);
            System.out.println("Agente " + agentId + ": Contador de cargas parciales incrementado a: "
                    + currentPartialCount + "/" + PARTIAL_CHARGE_LIMIT);

            if (currentPartialCount > PARTIAL_CHARGE_LIMIT) {
                System.out.println("[MODELO] Agente " + agentId + ": ¡PENALIZACIÓN APLICADA! (" + currentPartialCount
                        + " cargas parciales > límite de " + PARTIAL_CHARGE_LIMIT + ")");

                int originalMaxEnergyVal = agentOriginalMaxEnergy.getOrDefault(agentId, maxEnergy);
                int newMaxEnergy = (int) Math.round(originalMaxEnergyVal * MAX_ENERGY_PENALTY_FACTOR);
                agentMaxEnergy.put(agentId, newMaxEnergy);

                if (getCurrentEnergy(agentId) > newMaxEnergy) {
                    agentCurrentEnergy.put(agentId, newMaxEnergy);
                }

                agentPartialChargeCount.put(agentId, 0);
                System.out.println("Energía máxima del Agente " + agentId + " reducida a " + newMaxEnergy
                        + ". Contador de cargas parciales reseteado.");
            }
        } else {
            if (isEffectivelyFull) {
                System.out.println("[MODELO] Agente " + agentId + ": DETENIDA CARGA COMPLETA (Energía: " + currentEnergy
                        + "/" + maxEnergy + "). Tiempo restante en mapa era: " + remainingTimeInMap + ".");
            } else {
                System.out.println(
                        "[MODELO] Agente " + agentId + ": DETENIDA CARGA POR FIN DE DURACIÓN (Energía: " + currentEnergy
                                + "/" + maxEnergy + "). Tiempo restante en mapa era: " + remainingTimeInMap + ".");
            }
            if (agentPartialChargeCount.getOrDefault(agentId, 0) > 0) {
                System.out.println("[MODELO] Agente " + agentId + ": Reseteando contador de cargas parciales (actual: "
                        + agentPartialChargeCount.get(agentId) + ") debido a parada no penalizada.");
                agentPartialChargeCount.put(agentId, 0);
            }
        }

        agentRemainingChargeTimeMinutes.remove(agentId);
        System.out.println("Agente " + agentId + " ha detenido la carga formalmente. Energía final: "
                + getCurrentEnergy(agentId) + "/" + getMaxEnergy(agentId));

        if (view != null) {
            view.updateEnergyDisplay(agentId, getCurrentEnergy(agentId), getMaxEnergy(agentId));
        }
        return true;
    }

    /**
     * Aplica un 'tick' de carga. Verifica si el agente sigue cerca del cargador
     * único. PERMITE USO SIMULTÁNEO.
     */
    public synchronized void applyChargeEnergy() {
        if (agentRemainingChargeTimeMinutes.isEmpty()) {
            return;
        }

        Set<Integer> chargingAgentIds = new HashSet<>(agentRemainingChargeTimeMinutes.keySet());

        for (int agentId : chargingAgentIds) {
            if (!agentRemainingChargeTimeMinutes.containsKey(agentId)) {
                continue;
            }

            Location agentLoc = getAgPos(agentId);
            Location chargerLoc = getChargerLocationForAgent(agentId);

            if (agentLoc == null || chargerLoc == null ||
                    !(agentLoc.equals(chargerLoc) || agentLoc.isNeigbour(chargerLoc))) {
                System.out.println("[MODEL] Agente " + agentId + " ya no está cerca del cargador (" + chargerLoc
                        + "). Pos: " + agentLoc + ". Deteniendo carga automáticamente.");
                stopCharging(agentId);
                continue;
            }

            int remainingTime = agentRemainingChargeTimeMinutes.getOrDefault(agentId, 0);

            if (remainingTime <= 0 || !agentRemainingChargeTimeMinutes.containsKey(agentId)) {
                if (agentRemainingChargeTimeMinutes.containsKey(agentId)) {
                    System.out.println("[MODEL DEBUG] Carga de Agente " + agentId
                            + " con remainingTime <=0 al inicio del tick. Esperando a que el ASL detenga.");
                }
                if (view != null) {
                    view.updateEnergyDisplay(agentId, getCurrentEnergy(agentId), getMaxEnergy(agentId));
                }
                continue;
            }

            remainingTime--;
            agentRemainingChargeTimeMinutes.put(agentId, remainingTime);

            int maxEnergy = getMaxEnergy(agentId);
            int originalMaxEnergy = agentOriginalMaxEnergy.getOrDefault(agentId, maxEnergy);

            int totalChargeDuration;
            if (agentId == ROBOT_AGENT_ID) {
                totalChargeDuration = ROBOT_CHARGE_DURATION_MINS;
            } else if (agentId == AUXILIAR_AGENT_ID) {
                totalChargeDuration = AUXILIAR_CHARGE_DURATION_MINS;
            } else {
                totalChargeDuration = DEFAULT_CHARGE_DURATION_MINS;
            }

            if (totalChargeDuration <= 0) {
                System.err.println("Error: Duración de carga total para agente " + agentId + " es inválida ("
                        + totalChargeDuration + "). No se añadirá energía.");
                continue;
            }

            double energyPerMinute = (double) originalMaxEnergy / totalChargeDuration;
            int currentEnergy = getCurrentEnergy(agentId);
            int energyToAdd = (int) Math.round(energyPerMinute);
            int newEnergy = Math.min(maxEnergy, currentEnergy + energyToAdd);
            agentCurrentEnergy.put(agentId, newEnergy);

            if (remainingTime <= 0) {
                System.out.println("[MODEL] Tiempo de carga agotado para Agente " + agentId + ". Energía actual: "
                        + newEnergy + "/" + maxEnergy + ". El agente ASL debería detener la carga pronto.");
            }
            if (view != null) {
                view.updateEnergyDisplay(agentId, newEnergy, maxEnergy);
            }
        }
    }

    public synchronized boolean transferEnergyFromAuxiliarToRobot() {
        boolean toret = true;
        int auxiliarAgentId = AUXILIAR_AGENT_ID;
        int robotAgentId = ROBOT_AGENT_ID;

        int auxiliarEnergy = getCurrentEnergy(auxiliarAgentId);
        int robotEnergy = getCurrentEnergy(robotAgentId);
        int robotMaxEnergy = getMaxEnergy(robotAgentId);

        if (auxiliarEnergy <= 0) {
            System.out.println("[MODEL] Auxiliar has no energy to transfer.");
            return false;
        }

        int energyToTransfer = auxiliarEnergy / 2;
        if (energyToTransfer <= 0 && auxiliarEnergy > 0) {
            energyToTransfer = 1;
        }
        if (energyToTransfer == 0) {
            System.out.println(
                    "[MODEL] Auxiliar energy too low (" + auxiliarEnergy + ") to transfer a meaningful amount.");
            return false;
        }

        int newAuxiliarEnergy = auxiliarEnergy - energyToTransfer;
        agentCurrentEnergy.put(auxiliarAgentId, Math.max(0, newAuxiliarEnergy));
        System.out.println("[MODEL] Auxiliar transferred " + energyToTransfer + " energy. Auxiliar new energy: "
                + newAuxiliarEnergy);

        int newRobotEnergy = robotEnergy + energyToTransfer;
        agentCurrentEnergy.put(robotAgentId, Math.min(newRobotEnergy, robotMaxEnergy));
        System.out.println("[MODEL] Robot received " + energyToTransfer + " energy. Robot new energy: "
                + agentCurrentEnergy.get(robotAgentId) + "/" + robotMaxEnergy);

        if (view != null) {
            view.updateEnergyDisplay(auxiliarAgentId, getCurrentEnergy(auxiliarAgentId), getMaxEnergy(auxiliarAgentId));
            view.updateEnergyDisplay(robotAgentId, getCurrentEnergy(robotAgentId), getMaxEnergy(robotAgentId));
        }
        return true;
    }

    public synchronized boolean transferDrugAuxToNurse(String drugName) {
        boolean toret = false;
        if (auxiliarCarryingDrug) {
            auxiliarCarryingDrug = false;
            robotCarryingDrug = true;
            System.out.println(
                    "[MODELO] Medicamento (" + drugName + ") transferido del Auxiliar a la Enfermera (Robot).");
            if (view != null) {
                view.update();
            }
            toret = true;
        } else {
            System.out.println(
                    "[MODELO] Falló la transferencia de medicamento del Auxiliar a la Enfermera: Auxiliar no lleva medicamento.");
        }
        return toret;
    }

}
