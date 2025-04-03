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
     * @param model El modelo del entorno (HouseModel) que contiene el estado actual.
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
     * Dibuja los objetos del entorno (camas, sillas, sofá, mesa, puertas, electrodomésticos, etc.)
     * en sus posiciones correspondientes dentro del grid.
     *
     * @param g Contexto gráfico donde se realiza el dibujo.
     * @param x Coordenada X de la celda a dibujar.
     * @param y Coordenada Y de la celda a dibujar.
     * @param object Código del objeto definido en HouseModel.
     */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        Location lRobot = hmodel.getAgPos(0);
        Location lOwner = hmodel.getAgPos(1);
        Location loc = new Location(x, y);
        String objPath = currentDirectory;

        g.setColor(Color.white);
        super.drawEmpty(g, x, y);

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
                if (lRobot.equals(loc) || lRobot.isNeigbour(loc) || lOwner.equals(loc) || lOwner.isNeigbour(loc)) {
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
                if (lRobot.isNeigbour(hmodel.lFridge) || lOwner.isNeigbour(hmodel.lFridge)) {
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
                if (lRobot.isNeigbour(hmodel.lMedCab) || lOwner.isNeigbour(hmodel.lMedCab)) {
                    drawImage(g, x, y, "/doc/MedicalOpenR.png");
                    g.setColor(Color.green);
                } else {
                    drawImage(g, x, y, "/doc/MedicalCloseR.png");
                    g.setColor(Color.blue);
                }
                drawString(g, x, y, defaultFont, "Med(" + hmodel.availableDrugs + ")");
                drawString(g, 12, 14, defaultFont, "MedCab: " + hmodel.contadorMedicamentos + "");
                break;
        }
    }


    /**
     * Dibuja los agentes (robot, dueño u otros) en sus posiciones actuales dentro del entorno.
     * Utiliza imágenes distintas según el tipo de agente y su estado (por ejemplo, si está cargando objetos).
     * También muestra información visual adicional cuando el robot está cerca del dueño.
     *
     * @param g  Contexto gráfico para dibujar.
     * @param x  Coordenada X en el grid.
     * @param y  Coordenada Y en el grid.
     * @param c  Color sugerido (puede ser ignorado).
     * @param id Identificador del agente (0 = robot, 1 = dueño, >1 = otros).
     */
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Location lRobot = hmodel.getAgPos(0);
        Location lOwner = hmodel.getAgPos(1);
        String objPath = currentDirectory;

        if (id < 1) {
            if (!lRobot.equals(lOwner) && (!lRobot.equals(hmodel.lFridge) || (!lRobot.equals(hmodel.lMedCab)))) {
                c = Color.yellow;
                if (hmodel.carryingDrug || hmodel.carryingBeer) {
                    objPath = "/doc/beerBot.png";
                    drawImage(g, x, y, objPath);
                } else {
                    objPath = "/doc/bot.png";
                    drawImage(g, x, y, objPath);
                }
                g.setColor(Color.black);
                super.drawString(g, x, y, defaultFont, "Rob");
            }
        } else if (id > 1) {
            drawMan(g, x, y, "down");
        } else {
            if (lOwner.equals(hmodel.lChair1)) {
                drawMan(g, hmodel.lChair1.x, hmodel.lChair1.y, "left");
            } else if (lOwner.equals(hmodel.lChair2)) {
                drawMan(g, hmodel.lChair2.x, hmodel.lChair2.y, "down");
            } else if (lOwner.equals(hmodel.lChair4)) {
                drawMan(g, hmodel.lChair4.x, hmodel.lChair4.y, "down");
            } else if (lOwner.equals(hmodel.lChair3)) {
                drawMan(g, hmodel.lChair3.x, hmodel.lChair3.y, "right");
            } else if (lOwner.equals(hmodel.lSofa)) {
                drawMan(g, hmodel.lSofa.x, hmodel.lSofa.y, "up");
            } else if (lOwner.equals(hmodel.lDeliver)) {
                g.setColor(Color.lightGray);
                objPath = "/doc/openDoor2.png";
                drawScaledImage(g, x, y, objPath, 75, 100);
                drawMan(g, x, y, "down");
            } else {
                drawMan(g, x, y, hmodel.getLastDirection(1));
            }

            if (lRobot.isNeigbour(lOwner)) {
                String o = "S";
                if (hmodel.sipCount > 0) {
                    o += " (" + hmodel.sipCount + ")";
                }
                if (hmodel.drugsCount > 0) {
                    o += " (" + hmodel.drugsCount + ")";
                }
                g.setColor(Color.yellow);
                drawString(g, x, y, defaultFont, o);
            }
        }
    }


    /**
     * Dibuja una línea horizontal de obstáculos a partir de la posición (x, y).
     *
     * @param g       Contexto gráfico para el dibujo.
     * @param x       Coordenada X inicial.
     * @param y       Coordenada Y fija.
     * @param NCells  Número de celdas consecutivas con obstáculos a dibujar.
     */
    public void drawMultipleObstacleH(Graphics g, int x, int y, int NCells) {
        for (int i = x; i < x + NCells; i++) {
            drawObstacle(g, i, y);
        }
    }


    /**
     * Dibuja una línea vertical de NCells obstáculos empezando en (x, y).
     * @param g El contexto gráfico.
     * @param x Coordenada X.
     * @param y Coordenada Y inicial.
     * @param NCells Número de celdas de obstáculo a dibujar.
     */
    public void drawMultipleObstacleV(Graphics g, int x, int y, int NCells) {
		for (int j = y; j < y+NCells; j++) {
                drawObstacle(g,x,j);
            }    
    }

    /**
     * Dibuja una imagen que ocupa NW x NH celdas, empezando en (x, y), sin escalar.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda superior izquierda.
     * @param y Coordenada Y de la celda superior izquierda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param NW Número de celdas de ancho.
     * @param NH Número de celdas de alto.
     */
    public void drawMultipleImage(Graphics g, int x, int y, String imageAddress, int NW, int NH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else 
			Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + 2, y * cellSizeH + 2, NW*cellSizeW - 4, NH*cellSizeH - 4, null);
    }

    /**
     * Dibuja una imagen que ocupa NW x NH celdas, escalada a un porcentaje.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda superior izquierda.
     * @param y Coordenada Y de la celda superior izquierda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param NW Número de celdas de ancho base.
     * @param NH Número de celdas de alto base.
     * @param scaleW Porcentaje de escalado horizontal (100 = 100%).
     * @param scaleH Porcentaje de escalado vertical (100 = 100%).
     */
    public void drawMultipleScaledImage(Graphics g, int x, int y, String imageAddress, int NW, int NH, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + NW*cellSizeW*(100-scaleW)/200, y * cellSizeH + NH*cellSizeH*(100-scaleH)/200 + 1, NW*cellSizeW*scaleW/100, NH*scaleH*cellSizeH/100, null);
    }

    /**
     * Dibuja una imagen dentro de una sola celda, escalada a un porcentaje.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW Porcentaje de escalado horizontal.
     * @param scaleH Porcentaje de escalado vertical.
     */
    public void drawScaledImage(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image!"+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + cellSizeH*(100-scaleH)/100, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    /**
     * Dibuja una imagen escalada alineada a la parte superior de la celda.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW Porcentaje de escalado horizontal.
     * @param scaleH Porcentaje de escalado vertical.
     */
    public void drawScaledImageUp(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + 2, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    /**
     * Dibuja una imagen escalada alineada a la izquierda de la celda.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW Porcentaje de escalado horizontal.
     * @param scaleH Porcentaje de escalado vertical.
     */
	public void drawScaledImageLf(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }
    /**
     * Dibuja una imagen escalada alineada a la derecha de la celda.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW Porcentaje de escalado horizontal.
     * @param scaleH Porcentaje de escalado vertical.
     */
    public void drawScaledImageRt(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/100, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    /**
     * Dibuja una imagen escalada centrada (Middle/Md) en la celda.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     * @param scaleW Porcentaje de escalado horizontal.
     * @param scaleH Porcentaje de escalado vertical.
     */
    public void drawScaledImageMd(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.setColor(Color.lightGray);
		g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    /**
     * Dibuja una imagen ocupando casi toda la celda, sin escalar.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param imageAddress Ruta del recurso de la imagen.
     */
    public void drawImage(Graphics g, int x, int y, String imageAddress) {
		URL url = getClass().getResource(imageAddress);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+imageAddress);
		else Img = new ImageIcon(getClass().getResource(imageAddress)); 
		g.drawImage(Img.getImage(), x * cellSizeW+1, y * cellSizeH+1, cellSizeW-2, cellSizeH-2, null);
    }
	

    /**
     * Dibuja la figura de una persona según su estado/orientación.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     * @param how Cadena que indica el estado ("right", "left", "up", "down", "stand", "walkr").
     */
		public void drawMan(Graphics g, int x, int y, String how) { 
			String resource = "/doc/sitd.png"; // valor por defecto

			switch (how) {
				case "right":      resource = "/doc/sitr.png"; break;
				case "left":       resource = "/doc/sitl.png"; break;
				case "up":         resource = "/doc/situ.png"; break;
				case "down":       resource = "/doc/sitd.png"; break;
				case "stand":      resource = "/doc/sits.png"; break;
				case "walkr":      resource = "/doc/walklr.png"; break;
				case "walk_up":    resource = "/doc/walklu.png"; break;
				case "walk_down":  resource = "/doc/walkld.png"; break;
				case "walk_left":  resource = "/doc/walkll.png"; break;
				case "walk_right": resource = "/doc/walklr.png"; break;
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
     * Dibuja específicamente a la persona sentada mirando a la derecha.
     * (Posiblemente redundante si drawMan("right", ...) funciona).
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     */
    public void drawManSittingRight(Graphics g, int x, int y) {
		String objPath = "/doc/sitr.png";//currentDirectory.concat("/doc/sitr.png");
		URL url = getClass().getResource(objPath);
		ImageIcon Img = new ImageIcon();
		if (url == null)
    		System.out.println( "Could not find image! "+objPath);
		else Img = new ImageIcon(getClass().getResource(objPath)); 
		g.drawImage(Img.getImage(), x * cellSizeW - 4, y * cellSizeH + 1, cellSizeW + 2, cellSizeH - 2, null);
    }

    /**
     * Dibuja un cuadrado decorativo con doble borde dentro de una celda.
     * @param g El contexto gráfico.
     * @param x Coordenada X de la celda.
     * @param y Coordenada Y de la celda.
     */
    public void drawSquare(Graphics g, int x, int y) {
        g.setColor(Color.blue);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.setColor(Color.cyan);
        g.drawRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 3, cellSizeH - 3);   
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

