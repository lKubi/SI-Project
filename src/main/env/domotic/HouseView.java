package domotic;

import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.nio.file.Paths;

import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.util.ArrayList;
import java.awt.FontMetrics; 
import javax.swing.SwingUtilities;

import domotic.SimulatedClock;

/**
 * Clase que implementa la vista gráfica de la aplicación Domestic Robot.
 * Extiende las funcionalidades de GridWorldView para representar visualmente
 * el entorno, agentes y objetos, además de incluir un reloj interno.
 */
public class HouseView extends GridWorldView {

    HouseModel hmodel;
    SimulatedClock clock;
    int viewSize;
    String currentDirectory;
    private Timer timer;
    private EnergyStatusPanel energyStatusWindow;

    /**
     * Constructor de la vista gráfica de la casa.
     * Inicializa el título de la ventana, tamaño, fuente por defecto,
     * y configura un temporizador para redibujar la vista periódicamente.
     *
     * @param model El modelo del entorno (HouseModel) que contiene el estado
     *              actual.
     */
    public HouseView(HouseModel model, SimulatedClock clock) {
        super(model, "Domestic Care Robot", model.GridSize);
        this.hmodel = model;
        this.clock = clock;
        viewSize = model.GridSize;
        setSize(viewSize, viewSize / 2);
        defaultFont = new Font("Arial", Font.BOLD, 20);

        currentDirectory = Paths.get("").toAbsolutePath().toString();

         // --- NUEVO: Crear la VENTANA de estado de energía ---
         int initialMaxER = hmodel.getMaxEnergy(HouseModel.ROBOT_AGENT_ID);
         int initialMaxEA = hmodel.getMaxEnergy(HouseModel.AUXILIAR_AGENT_ID);
         // Crear la instancia (esto crea y muestra la ventana separada)
         // Usar SwingUtilities si la creación de HouseView no está en el EDT
         SwingUtilities.invokeLater(() -> {
              energyStatusWindow = new EnergyStatusPanel(initialMaxER, initialMaxEA);
         });
  

        timer = new Timer(400, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        timer.start();

        setVisible(true);
    }

    /**
     * Dibuja los objetos del entorno (camas, sillas, sofá, mesa, puertas,
     * electrodomésticos, etc.)
     * en sus posiciones correspondientes dentro del grid.
     *
     * @param g      Contexto gráfico donde se realiza el dibujo.
     * @param x      Coordenada X de la celda a dibujar.
     * @param y      Coordenada Y de la celda a dibujar.
     * @param object Código del objeto definido en HouseModel.
     */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        Location lRobot = hmodel.getAgPos(0);
        Location lOwner = hmodel.getAgPos(1);
        Location lAuxiliar = hmodel.getAgPos(2);
        Location loc = new Location(x, y);
        String objPath = currentDirectory;

        g.setColor(Color.white);

        switch (object) {
            case HouseModel.BED:
                g.setColor(Color.lightGray);
                if (hmodel.lBed1.equals(loc)) {
                    drawMultipleScaledImage(g, x, y, "/doc/doubleBedlt.png", 2, 2, 100, 100);
                    g.setColor(Color.red);
                    super.drawString(g, x, y, defaultFont, " 1 ");
                }
                if (hmodel.lBed2.equals(loc)) {
                    drawMultipleScaledImage(g, x, y, "/doc/singleBed.png", 2, 2, 60, 90);
                    g.setColor(Color.red);
                    super.drawString(g, x, y, defaultFont, " 2 ");
                }
                if (hmodel.lBed3.equals(loc)) {
                    drawMultipleScaledImage(g, x, y, "/doc/singleBed.png", 2, 2, 60, 90);
                    g.setColor(Color.red);
                    super.drawString(g, x, y, defaultFont, " 3 ");
                }
                break;

            case HouseModel.CHAIR:
                g.setColor(Color.lightGray);
                if (hmodel.lChair1.equals(loc)) {
                    drawScaledImageMd(g, x, y, "/doc/chairL.png", 80, 80);
                }
                if (hmodel.lChair2.equals(loc)) {
                    drawScaledImageMd(g, x, y, "/doc/chairD.png", 80, 80);
                }
                if (hmodel.lChair4.equals(loc)) {
                    drawScaledImageMd(g, x, y, "/doc/chairD.png", 80, 80);
                }
                if (hmodel.lChair3.equals(loc)) {
                    drawScaledImageMd(g, x, y, "/doc/chairU.png", 80, 80);
                }
                break;

            case HouseModel.SOFA:
                g.setColor(Color.lightGray);
                drawMultipleScaledImage(g, x, y, "/doc/sofa.png", 2, 1, 90, 90);
                break;

            case HouseModel.TABLE:
                g.setColor(Color.lightGray);
                drawMultipleScaledImage(g, x, y, "/doc/table.png", 2, 1, 80, 80);
                break;

            case HouseModel.DOOR:
                g.setColor(Color.lightGray);
                if (lRobot.equals(loc) || lRobot.isNeigbour(loc) || lOwner.equals(loc) || lOwner.isNeigbour(loc)
                        || lAuxiliar.equals(loc) || lAuxiliar.isNeigbour(loc)) {
                    drawScaledImage(g, x, y, "/doc/openDoor2.png", 75, 100);
                } else {
                    drawScaledImage(g, x, y, "/doc/closeDoor2.png", 75, 100);
                }
                break;

            case HouseModel.WASHER:
                g.setColor(Color.lightGray);
                if (lRobot.equals(hmodel.lWasher)) {
                    drawScaledImage(g, x, y, "/doc/openWasher.png", 50, 60);
                } else {
                    drawImage(g, x, y, "/doc/closeWasher.png");
                }
                break;

            case HouseModel.FRIDGE:
                g.setColor(Color.lightGray);
                // Comprobar si robot, dueño O auxiliar están cerca
                boolean fridgeNear = (lRobot != null && lRobot.isNeigbour(hmodel.lFridge)) ||
                        (lOwner != null && lOwner.isNeigbour(hmodel.lFridge)) ||
                        (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lFridge)); // <-- MODIFICADO: Añadida
                                                                                     // condición auxiliar

                if (fridgeNear) {
                    drawImage(g, x, y, "/doc/openNevera.png");
                    g.setColor(Color.yellow);
                } else {
                    drawImage(g, x, y, "/doc/closeNevera.png");
                    g.setColor(Color.blue);
                }
                drawString(g, x, y, defaultFont, "Fr (" + hmodel.availableBeers + ")");
                break;

            case HouseModel.MEDCAB:
                g.setColor(Color.lightGray);
                // Comprobar si robot, dueño O auxiliar están cerca
                boolean medcabNear = (lRobot != null && lRobot.isNeigbour(hmodel.lMedCab)) ||
                        (lOwner != null && lOwner.isNeigbour(hmodel.lMedCab)) ||
                        (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lMedCab)); // <-- MODIFICADO: Añadida
                                                                                     // condición auxiliar

                if (medcabNear) {
                    drawImage(g, x, y, "/doc/MedicalOpenR.png");
                    g.setColor(Color.green);
                } else {
                    drawImage(g, x, y, "/doc/MedicalCloseR.png");
                    g.setColor(Color.blue);
                }
                drawString(g, x, y, defaultFont, "Med(" + hmodel.availableDrugs + ")");

                // --- Dentro del método drawMedicamentos (o similar) en HouseView.java ---

                int currentHour = this.clock.getTime();
                int currentMinutes = this.clock.getMinutes(); // Obtener minutos actuales
                HashMap<String, Integer> contadorMedicamentos = hmodel.getContadorMedicamentos();
                // Correcto: expiryMap es de tipo HashMap<String, Location>
                HashMap<String, Location> expiryMap = hmodel.getMedicamentosExpiry();
                int posX = 4;
                int posY = 14;
                int lineWidth = 4; // altura entre líneas, depende del tamaño de fuente

                if (contadorMedicamentos != null) {
                    ArrayList<String> textos = new ArrayList<>();
                    ArrayList<Color> colores = new ArrayList<>();

                    int maxLength = 0;

                    // Primero preparamos los textos
                    for (String medName : contadorMedicamentos.keySet()) {
                        Integer cantidadObj = contadorMedicamentos.get(medName);
                        if (cantidadObj == null) {
                            System.err.println("Warning: Null count for " + medName + ". Skipping."); // Added warning
                            continue;
                        }
                        // Correcto: Se declara la variable cantidad
                        int cantidad = cantidadObj;

                        // ** INICIO: SECCIÓN MODIFICADA PARA MANEJAR LOCATION **
                        Location expiryLocation = null; // Variable para guardar la Location de caducidad
                        if (expiryMap != null) {
                            expiryLocation = expiryMap.get(medName); // Obtener la Location del mapa
                        }

                        Integer expiryHour = null; // Variable para la HORA de caducidad
                        Integer expiryMinute = null; // Variable para el MINUTO de caducidad

                        // Si encontramos una Location, extraemos hora (x) y minuto (y)
                        if (expiryLocation != null) {
                            expiryHour = expiryLocation.x;
                            expiryMinute = expiryLocation.y;
                        }

                        // Calcular 'caducado' usando la hora y minuto extraídos
                        boolean caducado = false;
                        if (expiryHour != null && expiryMinute != null) { // Solo si tenemos datos de caducidad
                            // Comprobar si la hora actual es posterior a la hora de caducidad
                            if (currentHour > expiryHour) {
                                caducado = true;
                            }
                            // O si es la misma hora, comprobar si el minuto actual es igual o posterior
                            else if (currentHour == expiryHour && currentMinutes >= expiryMinute) {
                                caducado = true;
                            }
                            // Si no, no está caducado
                        }
                        // ** FIN: SECCIÓN MODIFICADA **

                        String textoMostrar = medName + " (" + cantidad + ")"; // Esta línea ya funcionaba

                        if (caducado) { // Usamos la variable 'caducado' calculada correctamente
                            textoMostrar = "[EXP] " + textoMostrar;
                        }
                        textos.add(textoMostrar);
                        colores.add(caducado ? Color.ORANGE : Color.BLUE); // Usamos 'caducado'

                        if (textoMostrar.length() > maxLength) {
                            maxLength = textoMostrar.length();
                        }
                    } // Fin del for (String medName : ...)

                    // Dibujar los textos (esta parte no necesita cambios)
                    for (int i = 0; i < textos.size(); i++) {
                        g.setColor(colores.get(i));
                        // Ajuste menor: Dibuja cada texto en una nueva línea verticalmente
                        // Asumiendo que 'posY' es el inicio y 'lineWidth' es el espaciado vertical
                        drawString(g, posX + (i * lineWidth), posY, defaultFont, textos.get(i));
                        // Si querías que se dibujaran horizontalmente uno al lado del otro,
                        // tu línea original estaba bien:
                        // drawString(g, posX + (i * lineWidth), posY, defaultFont, textos.get(i));
                        // Elige la que necesites. La vertical parece más común para listas.
                    }
                }
                g.setColor(Color.BLUE);
                drawString(g, 1, 14, defaultFont, "MedCab: {");
                drawString(g, 22, 14, defaultFont, " }");

                break;

            case HouseModel.CHARGER:
            Location chargerLocation = new Location(x, y);

            if (chargerLocation.equals(hmodel.lCargador)) {
                boolean chargerNear = (lRobot != null && lRobot.isNeigbour(hmodel.lCargador)) ||
                                              (lOwner != null && lOwner.isNeigbour(hmodel.lCargador)) ||
                                              (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lCargador));
    
                if (chargerNear) {
                    drawImage(g, x, y, "/doc/cargadorOperativo.png"); 
                    g.setColor(Color.green);    
                } else {
                    drawImage(g, x, y, "/doc/cargador.png"); 
                   
                }            
            } else {
                drawImage(g, x, y, "/doc/cargador.png");
                g.setColor(Color.lightGray); 
            }
                break;
        }
    }

    // En la clase HouseView.java

/**
 * Actualiza la visualización de energía para un agente específico en el panel.
 * Este método es público para ser llamado desde fuera (ej. HouseModel).
 *
 * @param agentId        ID del agente (0 para Robot, 2 para Auxiliar).
 * @param currentEnergy  Energía actual.
 * @param maxEnergy      Energía máxima.
 */
public void updateEnergyDisplay(int agentId, int currentEnergy, int maxEnergy) {
    // Asegúrate de que la actualización se haga en el hilo de eventos de Swing
    // si existe la posibilidad de que se llame desde otro hilo.
    // Si se llama desde decrementAgentEnergy -> executeAction (hilo del entorno),
    // normalmente es seguro, pero usar invokeLater es más robusto.
    SwingUtilities.invokeLater(() -> {
        if (energyStatusWindow != null) {
            energyStatusWindow.updateEnergy(agentId, currentEnergy, maxEnergy);
        }
    });
}

// Opcionalmente, podrías tener un método general si prefieres
// public void refreshEnergyDisplay() {
//     SwingUtilities.invokeLater(() -> {
//         if (energyStatusWindow != null && hmodel != null) {
//              energyStatusWindow.updatePanelFromModel(hmodel);
//         }
//     });
// }

    /**
     * Dibuja los agentes (robot, dueño, auxiliar u otros) en sus posiciones
     * actuales dentro
     * del entorno.
     * Utiliza imágenes distintas según el tipo de agente y su estado (por ejemplo,
     * si está cargando objetos).
     * También muestra información visual adicional cuando el robot está cerca del
     * dueño.
     *
     * @param g  Contexto gráfico para dibujar.
     * @param x  Coordenada X en el grid.
     * @param y  Coordenada Y en el grid.
     * @param c  Color sugerido (puede ser ignorado).
     * @param id Identificador del agente (0 = robot, 1 = dueño, 2 = auxiliar, >2 =
     *           otros).
     */
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Location lRobot = hmodel.getAgPos(0);
        Location lOwner = hmodel.getAgPos(1);
        Location lAuxiliar = hmodel.getAgPos(2);

        if (id == 0) { // Robot
            if (!lRobot.equals(lOwner) && !lRobot.equals(hmodel.lFridge) && !lRobot.equals(hmodel.lMedCab)) {

                String objPath = "/doc/bot.png";

                if (hmodel.robotCarryingBeer) {
                    objPath = "/doc/beerBot.png";
                } else if (hmodel.robotCarryingDrug) {
                    objPath = "/doc/drugBot.png";
                }

                drawImage(g, x, y, objPath);
                super.drawString(g, x, y, defaultFont, "Rob");

                // --- NUEVO: Dibujar Energía Robot ---
                int currentE = hmodel.getCurrentEnergy(id);
                int maxE = hmodel.getMaxEnergy(id);
                if (maxE > 0) { // Solo dibujar si hay energía máxima definida
                    String energyStr = String.format("E:%d/%d", currentE, maxE);
                    g.setColor(Color.ORANGE); // O un color que cambie según el nivel
                    // Dibujar debajo del texto "Rob" o de la imagen
                    FontMetrics fm = g.getFontMetrics(defaultFont);
                    int energyY = y * cellSizeH + cellSizeH - fm.getDescent(); // Posición Y cerca del fondo de la celda
                    g.drawString(energyStr, x * cellSizeW + 5, energyY);
                }
                // --- FIN NUEVO ---
            }
        } else if (id == 1) { // Owner
            Location lAgent = hmodel.getAgPos(id);
            if (lAgent.equals(hmodel.lChair1)) {
                drawMan(g, lAgent.x, lAgent.y, "left");
            } else if (lAgent.equals(hmodel.lChair2)) {
                drawMan(g, lAgent.x, lAgent.y, "down");
            } else if (lAgent.equals(hmodel.lChair4)) {
                drawMan(g, lAgent.x, lAgent.y, "down");
            } else if (lAgent.equals(hmodel.lChair3)) {
                drawMan(g, lAgent.x, lAgent.y, "right");
            } else if (lAgent.equals(hmodel.lSofa)) {
                drawMan(g, lAgent.x, lAgent.y, "up");
            } else if (lAgent.equals(hmodel.lDeliver)) {
                g.setColor(Color.lightGray);
                String objPath = "/doc/openDoor2.png";
                drawScaledImage(g, lAgent.x, lAgent.y, objPath, 75, 100);
                drawMan(g, lAgent.x, lAgent.y, "down");
            } else {
                drawMan(g, lAgent.x, lAgent.y, hmodel.getLastDirection(id));
            }

            if (lRobot != null && lRobot.isNeigbour(lAgent)) {
                String o = "S";
                if (hmodel.sipCount > 0) {
                    o += " (" + hmodel.sipCount + ")";
                }
                if (hmodel.drugsCount > 0) {
                    o += " (" + hmodel.drugsCount + ")";
                }
                g.setColor(Color.yellow);
                drawString(g, lAgent.x, lAgent.y, defaultFont, o);
            }

        } else if (id == 2) { // Auxiliar
            if (!lAuxiliar.equals(lOwner) && !lAuxiliar.equals(hmodel.lFridge) && !lAuxiliar.equals(hmodel.lMedCab)) {
                String objPath;
                boolean isCarrying = hmodel.auxiliarCarryingDrug;

                if (isCarrying) {
                    objPath = "/doc/auxiliarCaja.png";
                } else {
                    objPath = "/doc/auxiliar.png";
                }
                drawImage(g, x, y, objPath);
                g.setColor(Color.blue);

                // --- NUEVO: Dibujar Energía Auxiliar ---
               int currentE = hmodel.getCurrentEnergy(id);
                int maxE = hmodel.getMaxEnergy(id);
                if (maxE > 0) { // Solo dibujar si hay energía máxima definida
                    String energyStr = String.format("E:%d/%d", currentE, maxE);
                    g.setColor(Color.ORANGE); // O un color que cambie según el nivel
                    // Dibujar debajo del texto "Rob" o de la imagen
                    FontMetrics fm = g.getFontMetrics(defaultFont);
                    int energyY = y * cellSizeH + cellSizeH - fm.getDescent(); // Posición Y cerca del fondo de la celda
                    g.drawString(energyStr, x * cellSizeW + 5, energyY);
                }
                // --- FIN NUEVO ---
            }

        } else {
            drawMan(g, x, y, "stand");
            g.setColor(Color.darkGray);
            super.drawString(g, x, y, defaultFont, "Ag" + id);
        }
    }

    /**
     * Dibuja una imagen que ocupa NW x NH celdas, escalada a un porcentaje.
     * 
     * @param g            El contexto gráfico.
     * @param x            Coordenada X de la celda superior izquierda.
     * @param y            Coordenada Y de la celda superior izquierda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param NW           Número de celdas de ancho base.
     * @param NH           Número de celdas de alto base.
     * @param scaleW       Porcentaje de escalado horizontal (100 = 100%).
     * @param scaleH       Porcentaje de escalado vertical (100 = 100%).
     */
    public void drawMultipleScaledImage(Graphics g, int x, int y, String imageAddress, int NW, int NH, int scaleW,
            int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! " + imageAddress);
        else
            Img = new ImageIcon(getClass().getResource(imageAddress));
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + NW * cellSizeW * (100 - scaleW) / 200,
                y * cellSizeH + NH * cellSizeH * (100 - scaleH) / 200 + 1, NW * cellSizeW * scaleW / 100,
                NH * scaleH * cellSizeH / 100, null);
    }

    /**
     * Dibuja una imagen dentro de una sola celda, escalada a un porcentaje.
     * 
     * @param g            El contexto gráfico.
     * @param x            Coordenada X de la celda.
     * @param y            Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW       Porcentaje de escalado horizontal.
     * @param scaleH       Porcentaje de escalado vertical.
     */
    public void drawScaledImage(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image!" + imageAddress);
        else
            Img = new ImageIcon(getClass().getResource(imageAddress));
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW * (100 - scaleW) / 200,
                y * cellSizeH + cellSizeH * (100 - scaleH) / 100, cellSizeW * scaleW / 100, scaleH * cellSizeH / 100,
                null);
    }

    /**
     * Dibuja una imagen escalada centrada (Middle/Md) en la celda.
     * 
     * @param g            El contexto gráfico.
     * @param x            Coordenada X de la celda.
     * @param y            Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW       Porcentaje de escalado horizontal.
     * @param scaleH       Porcentaje de escalado vertical.
     */
    public void drawScaledImageMd(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! " + imageAddress);
        else
            Img = new ImageIcon(getClass().getResource(imageAddress));
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW * (100 - scaleW) / 200,
                y * cellSizeH + cellSizeH * (100 - scaleH) / 200 + 1, cellSizeW * scaleW / 100,
                scaleH * cellSizeH / 100, null);
    }

    /**
     * Dibuja una imagen ocupando casi toda la celda, sin escalar.
     * 
     * @param g            El contexto gráfico.
     * @param x            Coordenada X de la celda.
     * @param y            Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     */
    public void drawImage(Graphics g, int x, int y, String imageAddress) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! " + imageAddress);
        else
            Img = new ImageIcon(getClass().getResource(imageAddress));
        g.drawImage(Img.getImage(), x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2, null);
    }

    /**
     * Dibuja la figura de una persona según su estado/orientación.
     * 
     * @param g   El contexto gráfico.
     * @param x   Coordenada X de la celda.
     * @param y   Coordenada Y de la celda.
     * @param how Cadena que indica el estado ("right", "left", "up", "down",
     *            "stand", "walkr").
     */
    public void drawMan(Graphics g, int x, int y, String how) {
        String resource = "/doc/sitd.png"; // valor por defecto

        switch (how) {
            case "right":
                resource = "/doc/sitr.png";
                break;
            case "left":
                resource = "/doc/sitl.png";
                break;
            case "up":
                resource = "/doc/situ.png";
                break;
            case "down":
                resource = "/doc/sitd.png";
                break;
            case "walkr":
                resource = "/doc/walklr.png";
                break;
            case "walk_up":
                resource = "/doc/walklu.png";
                break;
            case "walk_down":
                resource = "/doc/walkld.png";
                break;
            case "walk_left":
                resource = "/doc/walkll.png";
                break;
            case "walk_right":
                resource = "/doc/walklr.png";
                break;
        }

        URL url = getClass().getResource(resource);
        ImageIcon Img = new ImageIcon();

        if (url == null) {
            System.out.println("Could not find image! " + resource);
        } else {
            Img = new ImageIcon(url);
        }

        g.drawImage(Img.getImage(), x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 3, cellSizeH - 3, null);
    }

    /**
     * Detiene el temporizador de redibujado si se encuentra en ejecución.
     * Este método puede ser llamado al cerrar la ventana o al pausar la simulación.
     */
      /**
     * Detiene el temporizador de redibujado Y cierra la ventana de energía.
     */
    public void stopTimer() { // Sobrescribimos para añadir dispose
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        // Añadido: cerrar también la ventana de energía
        if (energyStatusWindow != null) {
             // Asegurarse de que se llama desde el EDT si es necesario
             SwingUtilities.invokeLater(() -> {
                  energyStatusWindow.dispose();
             });
        }
    }

}