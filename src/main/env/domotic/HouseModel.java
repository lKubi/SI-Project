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

/**
 * Clase que representa el modelo del entorno para la simulación de un robot doméstico.
 * Extiende GridWorldModel y define objetos, agentes, ubicaciones, habitaciones,
 * estado del entorno y lógica de movimiento.
 */
public class HouseModel extends GridWorldModel {

    // --- Constantes de Objetos ---
    
    /** Objeto tipo columna (no utilizado en lógica actual) */
    public static final int COLUMN   =     4;
    /** Objeto tipo silla */
    public static final int CHAIR    =     8;
    /** Objeto tipo sofá */
    public static final int SOFA     =    16;
    /** Objeto tipo nevera */
    public static final int FRIDGE   =    32;
    /** Objeto tipo lavadora */
    public static final int WASHER   =    64;
    /** Objeto tipo puerta */
    public static final int DOOR     =   128;
    /** Objeto tipo cargador (no utilizado) */
    public static final int CHARGER  =   256;
    /** Objeto tipo mesa */
    public static final int TABLE    =   512;
    /** Objeto tipo cama */
    public static final int BED      =  1024;
    /** Objeto tipo pared vertical (no utilizado) */
    public static final int WALLV    =  2048;
    /** Objeto tipo botiquín (único lugar para medicamentos) */
    public static final int MEDCAB   =  4096;

    // --- Configuración del Grid y Agentes ---

    /** Tamaño del grid en celdas */
    public static final int GSize = 15;
    /** Ancho total del grid (no utilizado directamente) */
    public final int GridSize = 2000;
    /** ID del agente enfermera (robot) */
    public static final int ROBOT_AGENT_ID = 0;
    /** ID del agente dueño (owner) */
    public static final int OWNER_AGENT_ID = 1;
    /** Número total de agentes definidos */
    private static final int nAgents = 2;

    // --- Estado del Modelo ---

    /** Estado de apertura de la nevera */
    boolean fridgeOpen = false;
    /** Estado de apertura del botiquín */
    boolean medCabOpen = false;
    /** Si el robot lleva medicamentos */
    boolean carryingDrug = false;
    /** Si el robot lleva cerveza */
    boolean carryingBeer = false;
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

    Location lSofa    = new Location(6, 10);
    Location lChair1  = new Location(8, 9);
    Location lChair3  = new Location(5, 9);
    Location lChair2  = new Location(7, 8);
    Location lChair4  = new Location(6, 8);
    Location lDeliver = new Location(0, 12);
    Location lWasher  = new Location(4, 0);
    Location lFridge  = new Location(0, 0);
    Location lMedCab  = new Location(0, 2);
    Location lTable   = new Location(6, 9);
    Location lBed2    = new Location(14, 0);
    Location lBed3    = new Location(21, 0);
    Location lBed1    = new Location(13, 9);

    // --- Ubicaciones de Puertas ---

    Location lDoorHome  = new Location(0, 12);
    Location lDoorKit1  = new Location(0, 6);
    Location lDoorKit2  = new Location(7, 5);
    Location lDoorSal1  = new Location(3, 11);
    Location lDoorSal2  = new Location(11, 6);
    Location lDoorBed1  = new Location(13, 6);
    Location lDoorBed2  = new Location(13, 4);
    Location lDoorBed3  = new Location(23, 4);
    Location lDoorBath1 = new Location(11, 4);
    Location lDoorBath2 = new Location(20, 7);

    // --- Definición de Áreas/Habitaciones ---

    Area kitchen    = new Area(0, 0, 7, 6);
    Area livingroom = new Area(4, 7, 12, 12);
    Area bath1      = new Area(8, 0, 11, 4);
    Area bath2      = new Area(21, 7, 23, 12);
    Area bedroom1   = new Area(13, 7, 20, 12);
    Area bedroom2   = new Area(12, 0, 17, 4);
    Area bedroom3   = new Area(18, 0, 23, 4);
    Area hall       = new Area(0, 7, 3, 12);
    Area hallway    = new Area(8, 5, 23, 6);

    /** Mapa de direcciones de movimiento por agente */
    private Map<Integer, String> directionMap = new HashMap<>();

    /**
     * Constructor del modelo del entorno. Inicializa el grid,
     * las posiciones de los agentes, los objetos fijos (muebles, puertas),
     * las paredes y el inventario inicial de medicamentos.
     */
    public HouseModel() {
        super(2*GSize-5, GSize, nAgents); // Grid de 24x12

        // Posiciones iniciales de los agentes
        setAgPos(0, 19, 10);   // enfermera
        setAgPos(1, 13, 9);    // owner

        // Objetos fijos en el entorno
        add(MEDCAB, lMedCab);
        add(FRIDGE, lFridge);
        add(WASHER, lWasher);
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
        contadorMedicamentos.put("Paracetamol 500mg", 3);
        contadorMedicamentos.put("Ibuprofeno 600mg", 4);
        contadorMedicamentos.put("Amoxicilina 500mg", 8);
        contadorMedicamentos.put("Omeprazol 20mg", 1);
        contadorMedicamentos.put("Loratadina 10mg", 0);

        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
    }


    /**
     * Devuelve el nombre de la habitación en la que se encuentra la localización especificada.
     * Si la ubicación no pertenece a ninguna habitación conocida, retorna "kitchen" por defecto
     * y muestra una advertencia en consola.
     *
     * @param thing Localización a identificar dentro del entorno.
     * @return Nombre de la habitación correspondiente.
     */
    String getRoom(Location thing) {
        if (kitchen.contains(thing))    return "kitchen";
        if (livingroom.contains(thing)) return "livingroom";
        if (bath1.contains(thing))      return "bath1";
        if (bath2.contains(thing))      return "bath2";
        if (bedroom1.contains(thing))   return "bedroom1";
        if (bedroom2.contains(thing))   return "bedroom2";
        if (bedroom3.contains(thing))   return "bedroom3";
        if (hall.contains(thing))       return "hall";
        if (hallway.contains(thing))    return "hallway";

        System.err.println("Warning: Location " + thing + " not found in any defined room. Defaulting to kitchen.");
        return "kitchen";
    }


    /**
     * Mueve al agente a la ubicación especificada si está adyacente a ella,
     * simulando que se sienta en ese lugar.
     *
     * @param Ag ID del agente que desea sentarse.
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
     * Verifica si el agente especificado puede moverse a la celda (x, y).
     * Evalúa límites del grid, obstáculos físicos, colisiones con otros agentes
     * y restricciones específicas como celdas bloqueadas o muebles intransitables.
     *
     * @param Ag ID del agente que desea moverse.
     * @param x Coordenada X de la celda destino.
     * @param y Coordenada Y de la celda destino.
     * @return true si el movimiento es válido, false si está bloqueado.
     */
    boolean canMoveTo(int Ag, int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return false;
        }

        if (hasObject(OBSTACLE, x, y)) {
            return false;
        }

        int agentInCell = getAgAtPos(x, y);
        if (agentInCell != -1 && agentInCell != Ag) {
            return false;
        }

        if (Ag == ROBOT_AGENT_ID || Ag == OWNER_AGENT_ID) {
            if ((x == 7 && y == 9)  || (x == 7 && y == 10) || (x == 14 && y == 1) ||
                (x == 15 && y == 1) || (x == 15 && y == 0) || (x == 21 && y == 1) ||
                (x == 22 && y == 1) || (x == 22 && y == 0) || (x == 13 && y == 10) ||
                (x == 14 && y == 9) || (x == 14 && y == 10)) {
                return false;
            }

            return !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) &&
                   !hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) &&
                   !hasObject(BED, x, y) && !hasObject(FRIDGE, x, y) &&
                   !hasObject(MEDCAB, x, y);
        } else {
            return true;
        }
    }

    /**
     * Calcula el camino óptimo utilizando el algoritmo A* y mueve el agente un paso hacia el destino.
     * Si el movimiento es válido, también actualiza la dirección en la que se mueve el agente.
     *
     * @param Ag ID del agente que se desea mover.
     * @param dest Ubicación destino a la que debe dirigirse el agente.
     * @return true si el movimiento se realizó (o se intentó sin necesidad), false si no hay camino.
     */
    boolean moveTowards(int Ag, Location dest) {
        Location start = getAgPos(Ag);

        // Si ya está en el destino, no hacer nada
        if (start.equals(dest)) {
            return true;
        }

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
    }

    /**
     * Devuelve la última dirección registrada en la que se movió el agente.
     * Si no hay dirección registrada, retorna "walkr" por defecto.
     *
     * @param ag ID del agente.
     * @return Cadena con la dirección de movimiento (por ejemplo: "walk_up", "walk_left", etc.).
     */
    public String getLastDirection(int ag) {
        return directionMap.getOrDefault(ag, "walkr");
    }

    /**
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
     *
     * @return true si la acción se realizó con éxito, false si falló por alguna condición.
     */
    boolean getBeer() {
        if (fridgeOpen && availableBeers > 0 && !carryingBeer) {
            availableBeers--;
            carryingBeer = true;
            System.out.println("Robot got a beer. Beers left: " + availableBeers);
            return true;
        } else {
            if (!fridgeOpen) System.out.println("Failed to get beer: Fridge is closed.");
            if (availableBeers <= 0) System.out.println("Failed to get beer: No beers available.");
            if (carryingBeer) System.out.println("Failed to get beer: Robot already carrying a beer.");
            return false;
        }
    }

    /**
     * Añade una cantidad de cervezas al inventario del frigorífico.
     *
     * @param n Cantidad de cervezas a añadir.
     * @return true si se añadieron correctamente, false si la cantidad es inválida.
     */
    boolean addBeer(int n) {
        if (n <= 0) return false;
        availableBeers += n;
        System.out.println("Added " + n + " beers. Total beers: " + availableBeers);
        return true;
    }

    /**
     * Entrega la cerveza al dueño si el robot la está cargando y está cerca de él.
     * La acción reinicia el contador de sorbos y libera la carga del robot.
     *
     * @return true si la entrega fue exitosa, false si falló por distancia o falta de carga.
     */
    boolean handInBeer() {
        Location robotPos = getAgPos(0);
        Location ownerPos = getAgPos(1);

        if (carryingBeer && robotPos.isNeigbour(ownerPos)) {
            sipCount = 10;
            carryingBeer = false;
            System.out.println("Robot handed beer to owner.");
            return true;
        } else {
            if (!carryingBeer) System.out.println("Failed to hand in beer: Robot not carrying one.");
            if (!robotPos.isNeigbour(ownerPos)) System.out.println("Failed to hand in beer: Robot not near owner.");
            return false;
        }
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
     * Añade una cantidad de unidades a cada tipo de medicamento disponible en el botiquín.
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
            System.out.println("  - Added " + n + " units to '" + drugName + "'. New count: " + contadorMedicamentos.get(drugName));
        }

        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

        System.out.println("Restock complete. Total available drugs now: " + availableDrugs);
        return true;
    }

    /**
     * Entrega un medicamento al dueño si el robot lo está cargando y se encuentra cerca.
     * Inicia el contador de uso de medicamentos y libera la carga del robot.
     *
     * @return true si la entrega fue exitosa, false si no se cumple alguna condición.
     */
    boolean handInDrug() {
        Location robotPos = getAgPos(0);
        Location ownerPos = getAgPos(1);

        if (carryingDrug && robotPos.isNeigbour(ownerPos)) {
            drugsCount = 10;
            carryingDrug = false;
            System.out.println("Robot handed drug to owner.");
            return true;
        } else {
            if (!carryingDrug) System.out.println("Failed to hand in drug: Robot not carrying one.");
            if (!robotPos.isNeigbour(ownerPos)) System.out.println("Failed to hand in drug: Robot not near owner.");
            return false;
        }
    }

    /**
     * Simula que el dueño toma una dosis del medicamento si está disponible.
     * Disminuye el contador de dosis disponibles.
     *
     * @return true si se consumió una dosis, false si no hay medicamento disponible.
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
     * @return true si la acción fue exitosa, false si no se cumplen las condiciones necesarias.
     */
    boolean getDrug() {
        if (medCabOpen && availableDrugs > 0 && !carryingDrug) {
            availableDrugs--;
            carryingDrug = true;
            System.out.println("Robot got a drug. Drugs left: " + availableDrugs);
            return true;
        } else {
            if (!medCabOpen) System.out.println("Failed to get drug: MedCab is closed.");
            if (availableDrugs <= 0) System.out.println("Failed to get drug: No drugs available.");
            if (carryingDrug) System.out.println("Failed to get drug: Robot already carrying a drug.");
            return false;
        }
    }

    /**
     * Permite que un agente (robot o dueño) tome un medicamento específico del botiquín.
     * Verifica precondiciones como cercanía al botiquín, disponibilidad del medicamento,
     * y si el agente ya lleva uno. Si el medicamento está agotado, lo repone automáticamente.
     *
     * @param agentId ID del agente (0 para robot, 1 para dueño).
     * @param nombreMedicamento Nombre exacto del medicamento que se desea obtener.
     * @return true si el agente obtuvo el medicamento, false en caso contrario.
     */
    boolean agenteGetSpecificDrug(int agentId, String nombreMedicamento) {
        String agentName = (agentId == ROBOT_AGENT_ID) ? "Robot" : (agentId == OWNER_AGENT_ID ? "Owner" : "Unknown Agent " + agentId);
        System.out.println("\n" + agentName + " trying to get: " + nombreMedicamento);

        if (!medCabOpen) {
            System.out.println("Error (" + agentName + "): MedCab is closed.");
            return false;
        }

        Location agentPos = getAgPos(agentId);
        if (agentPos == null) {
            System.out.println("Error (" + agentName + "): Invalid agent ID " + agentId);
            return false;
        }

        if (!agentPos.isNeigbour(lMedCab)) {
            System.out.println("Error (" + agentName + "): Not near MedCab at " + lMedCab + " (Agent at " + agentPos + ")");
            return false;
        }

        if (agentId == ROBOT_AGENT_ID && carryingDrug) {
            System.out.println("Error (" + agentName + "): Already carrying a drug.");
            return false;
        }

        if (agentId == OWNER_AGENT_ID && drugsCount > 0) {
            System.out.println("Error (" + agentName + "): Already has a drug ready (drugsCount=" + drugsCount + "). Must take it first.");
            return false;
        }

        if (contadorMedicamentos.containsKey(nombreMedicamento)) {
            int cantidadEspecifica = contadorMedicamentos.get(nombreMedicamento);
            boolean justRefilledThis = false;

            if (cantidadEspecifica <= 0) {
                System.out.println("\n**");
                System.out.println("*** Medicamento específico '" + nombreMedicamento + "' AGOTADO! ***");
                System.out.println("*** Auto-rellenando SOLO '" + nombreMedicamento + "' a 5 unidades... ***");

                int refillAmount = 5;
                contadorMedicamentos.put(nombreMedicamento, refillAmount);
                justRefilledThis = true;
                this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);

                System.out.println("*** Rellenado específico completado. Nueva cantidad de '" + nombreMedicamento + "': " + refillAmount + ". ***");
                System.out.println("*** Total de medicamentos disponibles ahora: " + this.availableDrugs + " ***");

                cantidadEspecifica = refillAmount;

                if (view != null) {
                    view.update(lMedCab.x, lMedCab.y);
                }
            }

            contadorMedicamentos.put(nombreMedicamento, cantidadEspecifica - 1);
            availableDrugs--;

            if (agentId == ROBOT_AGENT_ID) {
                carryingDrug = true;
            } else if (agentId == OWNER_AGENT_ID) {
                drugsCount = 1;
            }

            if (justRefilledThis) {
                System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + " (after auto-refill).");
            } else {
                System.out.println("Success (" + agentName + "): Got " + nombreMedicamento + ".");
            }

            System.out.println("  Specific units left: " + contadorMedicamentos.get(nombreMedicamento));
            System.out.println("  Total units left: " + availableDrugs);
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
     * @param cantidad Número de unidades a añadir.
     * @return true si la operación fue exitosa, false si la cantidad es inválida.
     */
    boolean addSpecificDrug(String nombreMedicamento, int cantidad) {
        if (cantidad <= 0) return false;
        int cantidadActual = contadorMedicamentos.getOrDefault(nombreMedicamento, 0);
        contadorMedicamentos.put(nombreMedicamento, cantidadActual + cantidad);
        this.availableDrugs = calcularTotalMedicamentos(contadorMedicamentos);
        System.out.println("Added " + cantidad + " units of " + nombreMedicamento + ". New count: " + contadorMedicamentos.get(nombreMedicamento) + ". Total drugs: " + availableDrugs);
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
}