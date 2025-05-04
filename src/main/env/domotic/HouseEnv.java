package domotic;

import jason.asSyntax.*;
import jason.environment.Environment; // Ensure this base class is imported
import jason.environment.grid.Location;
import java.util.logging.Logger;

import domotic.SimulatedClock;
import domotic.HouseModel;
import domotic.HouseView;

import java.util.Set;      // <--- Añade esta línea si falta
import java.util.HashSet;  // <--- Añade esta línea si falta
import java.util.Arrays;   // <--- Añade esta línea si falta

public class HouseEnv extends Environment {

    // common literals
    public static final Literal omc = Literal.parseLiteral("open(medCab)");
    public static final Literal cmc = Literal.parseLiteral("close(medCab)");
    public static final Literal of = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
    public static final Literal gd = Literal.parseLiteral("get(drug)");
    public static final Literal hd = Literal.parseLiteral("hand_in(drug)");
    public static final Literal sd = Literal.parseLiteral("sip(drug)");
    public static final Literal hod = Literal.parseLiteral("has(owner,drug)");
    public static final Literal gb = Literal.parseLiteral("get(beer)");
    public static final Literal hb = Literal.parseLiteral("hand_in(beer)");
    public static final Literal sb = Literal.parseLiteral("sip(beer)");
    public static final Literal hob = Literal.parseLiteral("has(owner,beer)");

    public static final Literal emc = Literal.parseLiteral("at(enfermera, medCab)");
    public static final Literal ef = Literal.parseLiteral("at(enfermera,fridge)");
    public static final Literal eo = Literal.parseLiteral("at(enfermera,owner)");
    public static final Literal ed = Literal.parseLiteral("at(enfermera,delivery)");
    public static final Literal ec = Literal.parseLiteral("at(enfermera,cargador)");



    public static final Literal amc = Literal.parseLiteral("at(auxiliar, medCab)");
    public static final Literal af = Literal.parseLiteral("at(auxiliar,fridge)");
    public static final Literal ao = Literal.parseLiteral("at(auxiliar,owner)");
    public static final Literal ad = Literal.parseLiteral("at(auxiliar,delivery)");
    public static final Literal ac = Literal.parseLiteral("at(auxiliar,cargador)");

    public static final Literal oamc = Literal.parseLiteral("at(owner, medCab)");
    public static final Literal oaf = Literal.parseLiteral("at(owner,fridge)");
    public static final Literal oac1 = Literal.parseLiteral("at(owner,chair1)");
    public static final Literal oac2 = Literal.parseLiteral("at(owner,chair2)");
    public static final Literal oac3 = Literal.parseLiteral("at(owner,chair3)");
    public static final Literal oac4 = Literal.parseLiteral("at(owner,chair4)");
    public static final Literal oasf = Literal.parseLiteral("at(owner,sofa)");
    public static final Literal aob1 = Literal.parseLiteral("at(owner,bed1)");
    public static final Literal aob2 = Literal.parseLiteral("at(owner,bed2)");
    public static final Literal aob3 = Literal.parseLiteral("at(owner,bed3)");
    public static final Literal oad = Literal.parseLiteral("at(owner,delivery)");

    private static final Set<String> ACTION_COSTING_ENERGY = new HashSet<>(Arrays.asList(
            "move_towards", // Moverse físicamente
            "sit",          // Cambiar a estado sentado (movimiento físico)
            "open",         // Abrir nevera o botiquín (interacción física)
            "close",        // Cerrar nevera o botiquín (interacción física)
            "get",          // Coger cerveza (genérico 'get(beer)')
            "obtener_medicamento", // Coger medicamento específico
            "hand_in",      // Entregar objeto (cerveza o medicamento)
            "cargar_medicamento", // Auxiliar recoge entrega (interacción física)
            "reponer_medicamento", // Auxiliar rellena botiquín (interacción física)
            "deliverdrug",  // Simulación de colocar droga tras entrega (interacción física)
            "deliverbeer"   // Simulación de colocar cerveza tras entrega (interacción física)
    // Acciones como 'sip', 'watchClock', 'start_charging', 'stop_charging', 'test_stop_charge' NO consumen energía aquí.
    // Las acciones internas de planes (println, findall, etc.) no pasan por aquí directamente.
    ));

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    HouseModel model;
    SimulatedClock clock;
    HouseView view; // Keep a reference if needed for view's timer

    @Override
    public void init(String[] args) {
        model = new HouseModel();
        clock = new SimulatedClock(this, model); // Pasar 'this' (HouseEnv) y 'model' (HouseModel)

        if (args.length == 1 && args[0].equals("gui")) {
            view = new HouseView(model, clock); // Assign to the field
            model.setView(view);
        }

        updatePercepts();
    }

    /**
     * Actualiza las percepciones de los agentes sobre su ubicación actual dentro de
     * la casa.
     * Asigna percepciones de la habitación en la que están y si se encuentran en
     * una puerta.
     */
    void updateAgentsPlace() {
        Location lRobot = model.getAgPos(0);
        String RobotPlace = model.getRoom(lRobot);
        addPercept("enfermera", Literal.parseLiteral("atRoom(" + RobotPlace + ")"));
        addPercept("auxiliar", Literal.parseLiteral("atRoom(enfermera," + RobotPlace + ")"));
        addPercept("owner", Literal.parseLiteral("atRoom(enfermera," + RobotPlace + ")"));

        Location lOwner = model.getAgPos(1);
        String OwnerPlace = model.getRoom(lOwner);
        addPercept("owner", Literal.parseLiteral("atRoom(" + OwnerPlace + ")"));
        addPercept("enfermera", Literal.parseLiteral("atRoom(owner," + OwnerPlace + ")"));
        addPercept("auxiliar", Literal.parseLiteral("atRoom(owner," + OwnerPlace + ")"));

        Location lAuxiliar = model.getAgPos(2);
        String AuxiliarPlace = model.getRoom(lAuxiliar);
        addPercept("auxiliar", Literal.parseLiteral("atRoom(" + AuxiliarPlace + ")"));
        addPercept("enfermera", Literal.parseLiteral("atRoom(auxiliar," + AuxiliarPlace + ")"));
        addPercept("owner", Literal.parseLiteral("atRoom(auxiliar," + AuxiliarPlace + ")"));

        if (lRobot.distance(model.lDoorHome) == 0 ||
                lRobot.distance(model.lDoorKit1) == 0 ||
                lRobot.distance(model.lDoorKit2) == 0 ||
                lRobot.distance(model.lDoorSal1) == 0 ||
                lRobot.distance(model.lDoorSal2) == 0 ||
                lRobot.distance(model.lDoorBath1) == 0 ||
                lRobot.distance(model.lDoorBath2) == 0 ||
                lRobot.distance(model.lDoorBed1) == 0 ||
                lRobot.distance(model.lDoorBed2) == 0 ||
                lRobot.distance(model.lDoorBed3) == 0) {
            addPercept("enfermera", Literal.parseLiteral("atDoor"));
        }
        ;

        if (lOwner.distance(model.lDoorHome) == 0 ||
                lOwner.distance(model.lDoorKit1) == 0 ||
                lOwner.distance(model.lDoorKit2) == 0 ||
                lOwner.distance(model.lDoorSal1) == 0 ||
                lOwner.distance(model.lDoorSal2) == 0 ||
                lOwner.distance(model.lDoorBath1) == 0 ||
                lOwner.distance(model.lDoorBath2) == 0 ||
                lOwner.distance(model.lDoorBed1) == 0 ||
                lOwner.distance(model.lDoorBed2) == 0 ||
                lOwner.distance(model.lDoorBed3) == 0) {
            addPercept("owner", Literal.parseLiteral("atDoor"));
        }
        ;

        if (lAuxiliar.distance(model.lDoorHome) == 0 ||
                lAuxiliar.distance(model.lDoorKit1) == 0 ||
                lAuxiliar.distance(model.lDoorKit2) == 0 ||
                lAuxiliar.distance(model.lDoorSal1) == 0 ||
                lAuxiliar.distance(model.lDoorSal2) == 0 ||
                lAuxiliar.distance(model.lDoorBath1) == 0 ||
                lAuxiliar.distance(model.lDoorBath2) == 0 ||
                lAuxiliar.distance(model.lDoorBed1) == 0 ||
                lAuxiliar.distance(model.lDoorBed2) == 0 ||
                lAuxiliar.distance(model.lDoorBed3) == 0) {
            addPercept("auxiliar", Literal.parseLiteral("atDoor"));
        }

    }

    /**
     * Actualiza las percepciones de la localización de los objetos fijos del
     * entorno
     * como el botiquín, la nevera, el sofá, sillas, camas y punto de entrega.
     */
    void updateThingsPlace() {

        String cargadorPlace = model.getRoom(model.lCargador);
        addPercept(Literal.parseLiteral("atRoom(cargador, " + cargadorPlace + ")"));

        String medCabPlace = model.getRoom(model.lMedCab);
        addPercept(Literal.parseLiteral("atRoom(medCab, " + medCabPlace + ")"));

        String fridgePlace = model.getRoom(model.lFridge);
        addPercept(Literal.parseLiteral("atRoom(fridge, " + fridgePlace + ")"));

        String sofaPlace = model.getRoom(model.lSofa);
        addPercept(Literal.parseLiteral("atRoom(sofa, " + sofaPlace + ")"));

        String chair1Place = model.getRoom(model.lChair1);
        addPercept(Literal.parseLiteral("atRoom(chair1, " + chair1Place + ")"));

        String chair2Place = model.getRoom(model.lChair2);
        addPercept(Literal.parseLiteral("atRoom(chair2, " + chair2Place + ")"));

        String chair3Place = model.getRoom(model.lChair3);
        addPercept(Literal.parseLiteral("atRoom(chair3, " + chair3Place + ")"));

        String chair4Place = model.getRoom(model.lChair4);
        addPercept(Literal.parseLiteral("atRoom(chair4, " + chair4Place + ")"));

        String deliveryPlace = model.getRoom(model.lDeliver);
        addPercept(Literal.parseLiteral("atRoom(delivery, " + deliveryPlace + ")"));

        String bed1Place = model.getRoom(model.lBed1);
        addPercept(Literal.parseLiteral("atRoom(bed1, " + bed1Place + ")"));

        String bed2Place = model.getRoom(model.lBed2);
        addPercept(Literal.parseLiteral("atRoom(bed2, " + bed2Place + ")"));

        String bed3Place = model.getRoom(model.lBed3);
        addPercept(Literal.parseLiteral("atRoom(bed3, " + bed3Place + ")"));
    }

    /**
     * Actualiza todas las percepciones relevantes para los agentes, incluyendo
     * posiciones,
     * proximidad a objetos, estado de recursos (cervezas, medicamentos), y
     * ubicación en muebles.
     * También actualiza el reloj interno del entorno.
     */
    void updatePercepts() {
        clearPercepts("enfermera");
        clearPercepts("owner");
        clearPercepts("auxiliar");

        updateAgentsPlace();
        updateThingsPlace();

        Location lRobot = model.getAgPos(0);
        Location lOwner = model.getAgPos(1);
        Location lAuxiliar = model.getAgPos(2);

        if (lRobot.distance(model.lMedCab) < 2) {
            addPercept("enfermera", emc);
        }

        if (lOwner.distance(model.lMedCab) < 2) {
            addPercept("owner", oamc);
        }

        if (lAuxiliar.distance(model.lMedCab) < 2) {
            addPercept("auxiliar", amc);
        }

        if (lRobot.distance(model.lFridge) < 2) {
            addPercept("enfermera", ef);
        }

        if (lRobot.distance(model.lCargador) < 2) {
            addPercept("enfermera", ec);
        }



        if (lAuxiliar.distance(model.lCargador) < 2) {
            addPercept("auxiliar", ac);
        }


        if (lOwner.distance(model.lFridge) < 2) {
            addPercept("owner", oaf);
        }

        if (lAuxiliar.distance(model.lFridge) < 2) {
            addPercept("auxiliar", af);
        }

        if (lRobot.distance(lOwner) == 1) {
            addPercept("enfermera", eo);
        }

        if (lAuxiliar.distance(lOwner) == 1) {
            addPercept("auxiliar", ao);
        }

        if (lRobot.distance(model.lDeliver) == 1) {
            addPercept("enfermera", ed);
        }

        if (lAuxiliar.distance(model.lDeliver) == 1) {
            addPercept("auxiliar", ad);
        }

        if (lOwner.distance(model.lChair1) == 0) {
            addPercept("owner", oac1);
        }

        if (lOwner.distance(model.lChair2) == 0) {
            addPercept("owner", oac2);
        }

        if (lOwner.distance(model.lChair3) == 0) {
            addPercept("owner", oac3);
        }

        if (lOwner.distance(model.lChair4) == 0) {
            addPercept("owner", oac4);
        }

        if (lOwner.distance(model.lSofa) == 0) {
            addPercept("owner", oasf);
        }

        if (lOwner.distance(model.lBed1) == 0) {
            addPercept("owner", aob1);
        }

        if (lOwner.distance(model.lBed2) == 0) {
            addPercept("owner", aob2);
        }

        if (lOwner.distance(model.lBed3) == 0) {
            addPercept("owner", aob3);
        }

        if (lOwner.distance(model.lDeliver) == 0) {
            addPercept("owner", oad);
        }

        if (model.fridgeOpen) {
            addPercept("enfermera", Literal.parseLiteral("stock(beer," + model.availableBeers + ")"));
            addPercept("auxiliar", Literal.parseLiteral("stock(beer," + model.availableBeers + ")"));

        }

        if (model.sipCount > 0) {
            addPercept("enfermera", hob);
            addPercept("owner", hob);
            addPercept("auxiliar", hob);

        }

        if (model.medCabOpen) {
            addPercept("enfermera", Literal
                    .parseLiteral("stock(\"Paracetamol\", " + model.contadorMedicamentos.get("Paracetamol") + ")"));
            addPercept("enfermera", Literal
                    .parseLiteral("stock(\"Ibuprofeno\", " + model.contadorMedicamentos.get("Ibuprofeno") + ")"));
            addPercept("enfermera", Literal
                    .parseLiteral("stock(\"Amoxicilina\", " + model.contadorMedicamentos.get("Amoxicilina") + ")"));
            addPercept("enfermera",
                    Literal.parseLiteral("stock(\"Omeprazol\", " + model.contadorMedicamentos.get("Omeprazol") + ")"));
            addPercept("enfermera", Literal
                    .parseLiteral("stock(\"Loratadina\", " + model.contadorMedicamentos.get("Loratadina") + ")"));

            addPercept("owner", Literal
                    .parseLiteral("stock(\"Paracetamol\", " + model.contadorMedicamentos.get("Paracetamol") + ")"));
            addPercept("owner", Literal
                    .parseLiteral("stock(\"Ibuprofeno\", " + model.contadorMedicamentos.get("Ibuprofeno") + ")"));
            addPercept("owner", Literal
                    .parseLiteral("stock(\"Amoxicilina\", " + model.contadorMedicamentos.get("Amoxicilina") + ")"));
            addPercept("owner",
                    Literal.parseLiteral("stock(\"Omeprazol\", " + model.contadorMedicamentos.get("Omeprazol") + ")"));
            addPercept("owner", Literal
                    .parseLiteral("stock(\"Loratadina\", " + model.contadorMedicamentos.get("Loratadina") + ")"));

        }

        if (model.drugsCount > 0) {
            addPercept("enfermera", hod);
            addPercept("owner", hod);
            addPercept("auxiliar", hod);
        }

        // In HouseEnv, when updating percepts:
        if (clock != null) {
            int currentHour = clock.getTime();
            int currentMinutes = clock.getMinutes(); // Get minutes from the clock
            String timeLiteral = String.format("clock(%d, %d)", currentHour, currentMinutes);
            addPercept("enfermera", Literal.parseLiteral(timeLiteral));
            addPercept("owner", Literal.parseLiteral(timeLiteral));
            addPercept("auxiliar", Literal.parseLiteral(timeLiteral));
        }

        // Para Enfermera (Robot)
        int robotCurrentE = model.getCurrentEnergy(HouseModel.ROBOT_AGENT_ID);
        int robotMaxE = model.getMaxEnergy(HouseModel.ROBOT_AGENT_ID);
        addPercept("enfermera", Literal.parseLiteral("current_energy(" + robotCurrentE + ")"));
        addPercept("enfermera", Literal.parseLiteral("max_energy(" + robotMaxE + ")"));
        // También útil para que otros agentes sepan su energía (opcional)
        // addPercept("owner", Literal.parseLiteral("energy(enfermera," + robotCurrentE
        // + "," + robotMaxE + ")"));
        // addPercept("auxiliar", Literal.parseLiteral("energy(enfermera," +
        // robotCurrentE + "," + robotMaxE + ")"));

        // Para Auxiliar
        int auxCurrentE = model.getCurrentEnergy(HouseModel.AUXILIAR_AGENT_ID);
        int auxMaxE = model.getMaxEnergy(HouseModel.AUXILIAR_AGENT_ID);
        addPercept("auxiliar", Literal.parseLiteral("current_energy(" + auxCurrentE + ")"));
        addPercept("auxiliar", Literal.parseLiteral("max_energy(" + auxMaxE + ")"));
        // También útil para que otros agentes sepan su energía (opcional)
        // addPercept("owner", Literal.parseLiteral("energy(auxiliar," + auxCurrentE +
        // "," + auxMaxE + ")"));
        // addPercept("enfermera", Literal.parseLiteral("energy(auxiliar," + auxCurrentE
        // + "," + auxMaxE + ")"));

    }

    /**
     * Ejecuta la acción especificada por un agente sobre el entorno.
     * Incluye acciones como moverse, sentarse, abrir/cerrar objetos, obtener y
     * entregar
     * recursos (cerveza o medicamentos), mirar el reloj, entre otros.
     *
     * @param ag     Nombre del agente que realiza la acción.
     * @param action Acción a ejecutar (estructura).
     * @return true si la acción fue ejecutada con éxito, false en caso contrario.
     */
    @Override
    public boolean executeAction(String ag, Structure action) {

        boolean result = false;

        int agentId = -1; // Determinar el ID basado en el nombre

        if (ag.equals("enfermera")) {
            agentId = HouseModel.ROBOT_AGENT_ID; // 0
        } else if (ag.equals("owner")) {
            agentId = HouseModel.OWNER_AGENT_ID; // 1
        } else if (ag.equals("auxiliar")) { // <-- AÑADIDO
            agentId = HouseModel.AUXILIAR_AGENT_ID; // 2
        } else {
            logger.warning("Action executed by unrecognized agent: " + ag);
            return false; // Agente desconocido
        }

        if (action.getFunctor().equals("sit")) {
            String l = action.getTerm(0).toString();
            Location dest = null;
            switch (l) {
                case "chair1":
                    dest = model.lChair1;
                    break;
                case "chair2":
                    dest = model.lChair2;
                    break;
                case "chair3":
                    dest = model.lChair3;
                    break;
                case "chair4":
                    dest = model.lChair4;
                    break;
                case "sofa":
                    dest = model.lSofa;
                    break;
            }
            ;
            try {
                if (ag.equals("enfermera")) {
                    System.out.println("[enfermera] is sitting");
                    result = model.sit(0, dest);
                } else if (ag.equals("owner")) {
                    System.out.println("[owner] is sitting");
                    result = model.sit(1, dest);
                } else if (ag.equals("auxiliar")) {
                    System.out.println("[auxiliar] is sitting");
                    result = model.sit(2, dest);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (action.equals(omc)) {
            result = model.openMedCab();

        } else if (action.equals(cmc)) {
            result = model.closeMedCab();

        } else if (action.equals(of)) {
            result = model.openFridge();

        } else if (action.equals(clf)) {
            result = model.closeFridge();

        } else if (action.getFunctor().equals("move_towards")) {
            String l = action.getTerm(0).toString();
            Location dest = null;
            switch (l) {
                case "cargador":
                    dest = model.lCargador;
                    break;

                case "medCab":
                    dest = model.lMedCab;
                    break;
                case "fridge":
                    dest = model.lFridge;
                    break;
                case "owner":
                    dest = model.getAgPos(1);
                    break;
                case "delivery":
                    dest = model.lDeliver;
                    break;
                case "chair1":
                    dest = model.lChair1;
                    break;
                case "chair2":
                    dest = model.lChair2;
                    break;
                case "chair3":
                    dest = model.lChair3;
                    break;
                case "chair4":
                    dest = model.lChair4;
                    break;
                case "sofa":
                    dest = model.lSofa;
                    break;
                case "bed1":
                    dest = model.lBed1;
                    break;
                case "bed2":
                    dest = model.lBed2;
                    break;
                case "bed3":
                    dest = model.lBed3;
                    break;
                case "washer":
                    dest = model.lWasher;
                    break;
                case "table":
                    dest = model.lTable;
                    break;
                case "doorBed1":
                    dest = model.lDoorBed1;
                    break;
                case "doorBed2":
                    dest = model.lDoorBed2;
                    break;
                case "doorBed3":
                    dest = model.lDoorBed3;
                    break;
                case "doorKit1":
                    dest = model.lDoorKit1;
                    break;
                case "doorKit2":
                    dest = model.lDoorKit2;
                    break;
                case "doorSal1":
                    dest = model.lDoorSal1;
                    break;
                case "doorSal2":
                    dest = model.lDoorSal2;
                    break;
                case "doorBath1":
                    dest = model.lDoorBath1;
                    break;
                case "doorBath2":
                    dest = model.lDoorBath2;
                    break;
            }
            try {
                if (ag.equals("enfermera")) {
                    result = model.moveTowards(0, dest);
                } else if (ag.equals("auxiliar")) {
                    result = model.moveTowards(2, dest);
                } else {
                    result = model.moveTowards(1, dest);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (action.equals(gd)) {
            result = model.getDrug();

        } else if (action.getFunctor().equals("hand_in")) { // Compara el nombre/functor de la acción

            Term drugTerm = action.getTerm(0);
            String drugName = "";
            if (drugTerm instanceof StringTerm) {
                drugName = ((StringTerm) drugTerm).getString();
            } else {
                // Remove quotes if it's parsed as an atom containing quotes
                drugName = drugTerm.toString().replace("\"", "");
            }

            result = model.handInDrug(agentId, drugName);

        } else if (action.equals(sd)) {
            result = model.sipDrug();

        } else if (action.equals(gb)) {
            result = model.getBeer();

        } else if (action.equals(hb)) {
            result = model.handInBeer();

        } else if (action.equals(sb)) {
            result = model.sipBeer();

        } else if (action.getFunctor().equals("watchClock")) {
            result = true;
            getClock(); // Prints the time, doesn't change state

        } else if (action.getFunctor().equals("obtener_medicamento")) {

            Term drugTerm = action.getTerm(0);
            String drugName = "";
            if (drugTerm instanceof StringTerm) {
                drugName = ((StringTerm) drugTerm).getString();
            } else {
                // Remove quotes if it's parsed as an atom containing quotes
                drugName = drugTerm.toString().replace("\"", "");
            }

            result = model.agenteGetSpecificDrug(agentId, drugName);

        } else if (action.getFunctor().equals("deliverdrug")) {
            try {
                result = model.addDrug((int) ((NumberTerm) action.getTerm(1)).solve());
                Thread.sleep(4000);
            } catch (Exception e) {
                logger.info("Failed to execute action deliverdrug!" + e);
            }

        } else if (action.getFunctor().equals("deliverbeer")) {
            try {
                result = model.addBeer((int) ((NumberTerm) action.getTerm(1)).solve());
                Thread.sleep(4000);
            } catch (Exception e) {
                logger.info("Failed to execute action deliverbeer!" + e);
            }

        } else if (action.getFunctor().equals("cargar_medicamento")) {
            result = model.auxiliarPickUpDelivery(agentId);
        } else if (action.getFunctor().equals("reponer_medicamento")) {

            Term drugTerm = action.getTerm(0);
            String drugName = "";
            if (drugTerm instanceof StringTerm) {
                drugName = ((StringTerm) drugTerm).getString();
            } else {
                // Remove quotes if it's parsed as an atom containing quotes
                drugName = drugTerm.toString().replace("\"", "");
            }
            result = model.refillSingleDrug(drugName);

        } else if (action.getFunctor().equals("start_charging")) {
            System.out.println("[ENV] Recibida acción de prueba: start_charging para " + ag);
            if (agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID) {
                // --- Añadir DEBUG de ubicación AHORA ---
                Location currentLoc = model.getAgPos(agentId); // Obtiene la posición JUSTO AHORA
                System.out.println("[ENV DEBUG] Posición actual de agente " + agentId
                        + " ANTES de llamar a startCharging: " + currentLoc);
                // --- Fin DEBUG ---
                result = model.startCharging(agentId); // Llama al método del modelo
                System.out.println("[ENV] Resultado de model.startCharging: " + result);
            } else {
                logger.warning("start_charging no aplica para agente: " + ag);
                result = false;
            }
        } else if (action.getFunctor().equals("stop_charging")) {
            System.out.println("[ENV] Recibida acción de prueba: stop_charging para " + ag);
            if (agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID) {
                result = model.stopCharging(agentId); // Llama al método stop que acabamos de crear
                System.out.println("[ENV] Resultado de model.stopCharging: " + result);
            } else {
                logger.warning("stop_charging no aplica para agente: " + ag);
                result = false;
            }
        } else {
            logger.info("Agent " + ag + " tried to execute unknown or failed action: " + action);
        }

      // REEMPLAZA EL BLOQUE ANTERIOR CON ESTE
      if (result) { // Si la acción se ejecutó con éxito (sintaxis, método del modelo OK)
        String functor = action.getFunctor(); // <-- OBTENER NOMBRE DE LA ACCIÓN

        // --- INICIO: DECREMENTAR ENERGÍA (MODIFICADO) ---
        // Comprueba ID agente, si NO está cargando, Y si la acción está en la lista de coste
        if ((agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID)
                && !model.isAgentCharging(agentId)
                && ACTION_COSTING_ENERGY.contains(functor)) { // <-- *** ESTA ES LA CONDICIÓN MODIFICADA ***

            // Opcional: Añadir un log para depurar cuándo se resta energía
            // logger.fine("[ENV] Action '" + functor + "' costs 1 energy for agent " + agentId);
            model.decrementAgentEnergy(agentId, 1); // Resta 1 de energía
        }
        // --- FIN: DECREMENTAR ENERGÍA (MODIFICADO) ---

        updatePercepts(); // Actualiza percepciones para todos los agentes
        try {
            Thread.sleep(250); // Pausa corta para simular tiempo de acción y visualización
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt(); // Restablecer estado interrumpido
             logger.warning("Environment sleep interrupted.");
        } catch (Exception e) {
             logger.log(java.util.logging.Level.SEVERE,"Error during post-action sleep", e);
        }
    } else {
         // Log opcional si la acción falló
         // logger.warning("Action " + action + " for agent " + ag + " failed or returned false.");
    }

        return result;
    }

    /**
     * Imprime en consola la hora actual del reloj del entorno.
     */
    private void getClock() {
        if (clock != null) {
            clock.getTime();
        } else {
        }
    }
}