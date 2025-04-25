package domotic;

import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.Timer;

/**
 * Clase que implementa la vista gráfica de la aplicación Domestic Robot.
 * Extiende las funcionalidades de GridWorldView para representar visualmente
 * el entorno, agentes y objetos, además de incluir un reloj interno.
 */
public class HouseView extends GridWorldView {

    HouseModel hmodel;
    int viewSize;
    String currentDirectory;
    private Timer timer;

    /**
     * Constructor de la vista gráfica de la casa.
     * Inicializa el título de la ventana, tamaño, fuente por defecto,
     * y configura un temporizador para redibujar la vista periódicamente.
     *
     * @param model El modelo del entorno (HouseModel) que contiene el estado
     *              actual.
     */
    public HouseView(HouseModel model) {
        super(model, "Domestic Care Robot", model.GridSize);
        hmodel = model;
        viewSize = model.GridSize;
        setSize(viewSize, viewSize / 2);
        defaultFont = new Font("Arial", Font.BOLD, 20);

        currentDirectory = Paths.get("").toAbsolutePath().toString();

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
                if (lRobot.equals(loc) || lRobot.isNeigbour(loc) || lOwner.equals(loc) || lOwner.isNeigbour(loc) || lAuxiliar.equals(loc) || lAuxiliar.isNeigbour(loc)) {
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
                                     (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lFridge)); // <-- MODIFICADO: Añadida condición auxiliar

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
                                     (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lMedCab)); // <-- MODIFICADO: Añadida condición auxiliar

                if (medcabNear) {
                    drawImage(g, x, y, "/doc/MedicalOpenR.png");
                    g.setColor(Color.green);
                } else {
                    drawImage(g, x, y, "/doc/MedicalCloseR.png");
                    g.setColor(Color.blue);
                }
                drawString(g, x, y, defaultFont, "Med(" + hmodel.availableDrugs + ")");
                drawString(g, 12, 14, defaultFont, "MedCab: " + hmodel.contadorMedicamentos + "");
                break;

            case HouseModel.CHARGER:
                g.setColor(Color.lightGray);
                 // Comprobar si robot, dueño O auxiliar están cerca
                 // (Quizás para el cargador solo debería depender del robot?)
                boolean chargerNear = (lRobot != null && lRobot.isNeigbour(hmodel.lCharger)) ||
                                      (lOwner != null && lOwner.isNeigbour(hmodel.lCharger)) || // ¿Debería el dueño activar el cargador?
                                      (lAuxiliar != null && lAuxiliar.isNeigbour(hmodel.lCharger)); // <-- MODIFICADO: Añadida condición auxiliar

                if (chargerNear) {
                    drawImage(g, x, y, "/doc/cargadorOperativo.png");
                    g.setColor(Color.green);
                } else {
                    drawImage(g, x, y, "/doc/cargador.png");
                    g.setColor(Color.blue);
                }
                // No se dibuja texto en el cargador por defecto
                break;
        }


    }

       /**
     * Dibuja los agentes (robot, dueño u otros) en sus posiciones actuales dentro
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
     * @param id Identificador del agente (0 = robot, 1 = dueño, 2 = auxiliar, >2 = otros).
     */
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Location lRobot = hmodel.getAgPos(0);
        Location lOwner = hmodel.getAgPos(1);
        // Location lAgent = hmodel.getAgPos(id); // Get location based on id if needed

        if (id == 0) { // Robot
            // Condición original para no dibujar en ciertos lugares.
            // Considera si realmente quieres este comportamiento. Si el robot debe
            // ser visible incluso en la ubicación del MedCab, elimina o ajusta este 'if'.
            if (!lRobot.equals(lOwner) && !lRobot.equals(hmodel.lFridge) && !lRobot.equals(hmodel.lMedCab)) {

                String objPath = "/doc/bot.png"; // Imagen por defecto

                // --- LÓGICA CORREGIDA ---
                // Prioriza la imagen según lo que lleve.
                // Si puede llevar ambas cosas (poco probable), decide cuál tiene prioridad.
                // Aquí asumimos que cerveza tiene prioridad visual sobre medicamento si ambas fueran true.
                if (hmodel.robotCarryingBeer) {
                    objPath = "/doc/beerBot.png";
                } else if (hmodel.robotCarryingDrug) { // Solo comprueba 'carryingDrug' si NO lleva cerveza
                    objPath = "/doc/drugBot.png";
                }
                // No se necesita un 'else' final aquí, ya que 'objPath' tiene un valor por defecto.
                // --- FIN LÓGICA CORREGIDA ---

                drawImage(g, x, y, objPath);
                super.drawString(g, x, y, defaultFont, "Rob");
            }
            // Si eliminaste el 'if' de arriba, la lógica de selección de imagen
            // y el drawImage/drawString irían aquí directamente.

        } else if (id == 1) { // Owner
            Location lAgent = hmodel.getAgPos(id); // Agent's location
            if (lAgent.equals(hmodel.lChair1)) {
                drawMan(g, lAgent.x, lAgent.y, "left"); // Use agent's actual coords
            } else if (lAgent.equals(hmodel.lChair2)) {
                drawMan(g, lAgent.x, lAgent.y, "down");
            } else if (lAgent.equals(hmodel.lChair4)) {
                drawMan(g, lAgent.x, lAgent.y, "down");
            } else if (lAgent.equals(hmodel.lChair3)) {
                drawMan(g, lAgent.x, lAgent.y, "right");
            } else if (lAgent.equals(hmodel.lSofa)) {
                drawMan(g, lAgent.x, lAgent.y, "up");
            } else if (lAgent.equals(hmodel.lDeliver)) {
                // Draw door behind the man at delivery location
                g.setColor(Color.lightGray);
                String objPath = "/doc/openDoor2.png";
                drawScaledImage(g, lAgent.x, lAgent.y, objPath, 75, 100); // Draw door at owner's location
                drawMan(g, lAgent.x, lAgent.y, "down"); // Draw man on top
            } else {
                // Draw the owner walking or standing based on last direction
                drawMan(g, lAgent.x, lAgent.y, hmodel.getLastDirection(id));
            }

            // Draw interaction hint if robot is near owner
            if (lRobot != null && lRobot.isNeigbour(lAgent)) {
                String o = "S"; // Interaction symbol or text
                if (hmodel.sipCount > 0) { // Assumes sipCount relates to owner interaction state
                    o += " (" + hmodel.sipCount + ")";
                }
                if (hmodel.drugsCount > 0) { // Assumes drugsCount relates to owner interaction state
                    o += " (" + hmodel.drugsCount + ")";
                }
                g.setColor(Color.yellow); // Highlight color for text
                // Draw string near the owner (agent id 1)
                drawString(g, lAgent.x, lAgent.y, defaultFont, o);
            }

        } else if (id == 2) { // **** NEW AGENT ****
            String objPath;
            // *** IMPORTANT: Replace hmodel.auxCarryingBox with your actual condition ***
            // *** OR add boolean auxCarryingBox to HouseModel ***
            boolean isCarrying = hmodel.carryingBox; // Placeholder condition

            if (isCarrying) {
                objPath = "/doc/auxiliarCaja.png"; // Image when carrying
            } else {
                objPath = "/doc/auxiliar.png"; // Default image
            }
            drawImage(g, x, y, objPath); // Draw the agent's image at its location (x,y)
            g.setColor(Color.blue); // Color for text label

        } else { // Other agents (id > 2), if any
            // Default representation for other agents
            drawMan(g, x, y, "stand"); // Or a generic image like a circle
            g.setColor(Color.darkGray); // Different color for label
            super.drawString(g, x, y, defaultFont, "Ag" + id); // Label as "Ag3", "Ag4", etc.
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
            case "stand":
                resource = "/doc/sits.png";
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
    public void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

}
