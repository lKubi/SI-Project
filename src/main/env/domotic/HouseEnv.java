package domotic;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

import domotic.SimulatedClock;
import domotic.HouseModel;
import domotic.HouseView;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class HouseEnv extends Environment {

    // Costos de energía para las acciones
    private static final Map<String, Integer> ACTION_ENERGY_COSTS;
    static {
        Map<String, Integer> costs = new HashMap<>();
        costs.put("move_towards", 1);
        costs.put("sit", 1);
        costs.put("open", 3);
        costs.put("close", 1);
        costs.put("get", 2);
        costs.put("obtener_medicamento", 3);
        costs.put("hand_in", 1);
        costs.put("cargar_medicamento", 2);
        costs.put("reponer_medicamento", 5);
        costs.put("deliverdrug", 1);
        costs.put("deliverbeer", 1);
        ACTION_ENERGY_COSTS = Collections.unmodifiableMap(costs);
    }

    // Percepciones de los objetos del entorno
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
    public static final Literal ae = Literal.parseLiteral("at(auxiliar,enfermera)");
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

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    HouseModel model;
    SimulatedClock clock;
    HouseView view;

    /**
     * Inicializa el entorno, el modelo, el reloj y la vista gráfica si corresponde.
     * @param args Argumentos de inicialización ("gui" para activar la vista gráfica).
     */
    @Override
    public void init(String[] args) {
        model = new HouseModel();
        clock = new SimulatedClock(this, model);

        if (args.length == 1 && args[0].equals("gui")) {
            view = new HouseView(model, clock);
            model.setView(view);
        }

        updatePercepts();
    }

    /**
     * Actualiza las percepciones de los agentes sobre su ubicación actual dentro de la casa.
     * Añade percepciones de la habitación en la que están y si se encuentran en una puerta.
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
     * Actualiza las percepciones de la localización de los objetos fijos del entorno
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
     * Actualiza todas las percepciones relevantes para los agentes, incluyendo posiciones,
     * proximidad a objetos, estado de recursos y ubicación en muebles. También actualiza el reloj interno.
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

        if (lAuxiliar.distance(lRobot) == 1) {
            addPercept("auxiliar", ae);
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

        if (clock != null) {
            int currentHour = clock.getTime();
            int currentMinutes = clock.getMinutes();
            String timeLiteral = String.format("clock(%d, %d)", currentHour, currentMinutes);
            addPercept("enfermera", Literal.parseLiteral(timeLiteral));
            addPercept("owner", Literal.parseLiteral(timeLiteral));
            addPercept("auxiliar", Literal.parseLiteral(timeLiteral));
        }

        int robotCurrentE = model.getCurrentEnergy(HouseModel.ROBOT_AGENT_ID);
        int robotMaxE = model.getMaxEnergy(HouseModel.ROBOT_AGENT_ID);
        addPercept("enfermera", Literal.parseLiteral("current_energy(" + robotCurrentE + ")"));
        addPercept("enfermera", Literal.parseLiteral("max_energy(" + robotMaxE + ")"));

        int auxCurrentE = model.getCurrentEnergy(HouseModel.AUXILIAR_AGENT_ID);
        int auxMaxE = model.getMaxEnergy(HouseModel.AUXILIAR_AGENT_ID);
        addPercept("auxiliar", Literal.parseLiteral("current_energy(" + auxCurrentE + ")"));
        addPercept("auxiliar", Literal.parseLiteral("max_energy(" + auxMaxE + ")"));

    }

    /**
     * Ejecuta la acción especificada por un agente sobre el entorno.
     * Incluye acciones como moverse, sentarse, abrir/cerrar objetos, obtener y entregar recursos, entre otros.
     * @param ag Nombre del agente que realiza la acción.
     * @param action Acción a ejecutar (estructura).
     * @return true si la acción fue ejecutada con éxito, false en caso contrario.
     */
    @Override
    public boolean executeAction(String ag, Structure action) {

        boolean result = false;

        int agentId = -1;

        if (ag.equals("enfermera")) {
            agentId = HouseModel.ROBOT_AGENT_ID;
        } else if (ag.equals("owner")) {
            agentId = HouseModel.OWNER_AGENT_ID;
        } else if (ag.equals("auxiliar")) {
            agentId = HouseModel.AUXILIAR_AGENT_ID;
        } else {
            logger.warning("Action executed by unrecognized agent: " + ag);
            return false;
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
                case "enfermera":
                    dest = model.getAgPos(0);
                    break;
                case "auxiliar":
                    dest = model.getAgPos(2);
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

        } else if (action.getFunctor().equals("transfer_energy_to_robot")) {
            if (ag.equals("auxiliar")) {
                result = model.transferEnergyFromAuxiliarToRobot();
                if (!result) {
                    logger.info("Auxiliar tried to transfer energy but failed (likely no energy).");
                }
            } else {
                logger.warning("Agent " + ag + " (not auxiliar) cannot perform 'transfer_energy_to_robot'.");
                result = false;
            }

        } else if (action.equals(gd)) {
            result = model.getDrug();

        } else if (action.getFunctor().equals("hand_in")) {
            if (action.getArity() == 2) {
                Term drugTerm = action.getTerm(1);
                String drugNameStr = "";
                if (drugTerm instanceof StringTerm) {
                    drugNameStr = ((StringTerm) drugTerm).getString();
                } else {
                    drugNameStr = drugTerm.toString().replace("\"", "");
                }
                result = model.handInDrug(agentId, drugNameStr);
            } else if (action.getArity() == 1 && action.getTerm(0).toString().equals("beer")) {
                result = model.handInBeer();
            } else {
                logger.warning("Acción hand_in llamada con aridad o argumentos inesperados: " + action);
                result = false;
            }
        } else if (action.equals(sd)) {
            result = model.sipDrug();

        } else if (action.equals(gb)) {
            result = model.getBeer();

        } else if (action.equals(hb)) {
            result = model.handInBeer();

        } else if (action.equals(sb)) {
            result = model.sipBeer();

        } else if (action.getFunctor().equals("transferir_medicamento_enfermera")) {
            String drugName = "";
            if (action.getArity() > 0 && action.getTerm(0) instanceof StringTerm) {
                drugName = ((StringTerm) action.getTerm(0)).getString();
            } else if (action.getArity() > 0) {
                drugName = action.getTerm(0).toString().replace("\"", "");
            }

            if (ag.equals("auxiliar")) {
                result = model.transferDrugAuxToNurse(drugName);
            } else {
                logger.warning("El agente " + ag + " (no es auxiliar) no puede realizar 'transfer_drug_aux_to_nurse'.");
                result = false;
            }
        } else if (action.getFunctor().equals("watchClock")) {
            result = true;
            getClock();

        } else if (action.getFunctor().equals("obtener_medicamento")) {

            Term drugTerm = action.getTerm(0);
            String drugName = "";
            if (drugTerm instanceof StringTerm) {
                drugName = ((StringTerm) drugTerm).getString();
            } else {
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
                drugName = drugTerm.toString().replace("\"", "");
            }
            result = model.refillSingleDrug(drugName);

        } else if (action.getFunctor().equals("start_charging")) {
            System.out.println("[ENV] Recibida acción de prueba: start_charging para " + ag);
            if (agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID) {
                Location currentLoc = model.getAgPos(agentId);
                System.out.println("[ENV DEBUG] Posición actual de agente " + agentId
                        + " ANTES de llamar a startCharging: " + currentLoc);
                result = model.startCharging(agentId);
                System.out.println("[ENV] Resultado de model.startCharging: " + result);
            } else {
                logger.warning("start_charging no aplica para agente: " + ag);
                result = false;
            }
        } else if (action.getFunctor().equals("stop_charging")) {
            System.out.println("[ENV] Recibida acción de prueba: stop_charging para " + ag);
            if (agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID) {
                result = model.stopCharging(agentId);
                System.out.println("[ENV] Resultado de model.stopCharging: " + result);
            } else {
                logger.warning("stop_charging no aplica para agente: " + ag);
                result = false;
            }
        } else {
            logger.info("Agent " + ag + " tried to execute unknown or failed action: " + action);
        }

        if (result) {
            String functor = action.getFunctor();

            if ((agentId == HouseModel.ROBOT_AGENT_ID || agentId == HouseModel.AUXILIAR_AGENT_ID)
                    && !model.isAgentCharging(agentId)) {

                if (ACTION_ENERGY_COSTS.containsKey(functor)) {
                    int cost = ACTION_ENERGY_COSTS.get(functor);
                    if (cost > 0) {
                        model.decrementAgentEnergy(agentId, cost);
                    }
                }
            }

            updatePercepts();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Environment sleep interrupted.");
            } catch (Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Error during post-action sleep", e);
            }
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