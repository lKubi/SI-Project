package domotic;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;

/**
 * Clase que simula un reloj interno para la aplicación Domotic.
 * Incrementa la hora (horas, minutos) automáticamente.
 * La velocidad de simulación está ajustada para que 1 hora simulada = 30 
 * segundos reales.
 * Los minutos avanzan de uno en uno en la pantalla (la pantalla se actualiza
 * cada medio segundo real).
 * Notifica al entorno cuando la HORA simulada cambia.
 * Muestra la hora (HH:MM) en una ventana con un diseño mejorado y tamaño
 * ajustado.
 */
public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
    private int minutes = 0;
    private HouseEnv houseEnv;
    private JLabel timeLabel;
    private JFrame clockFrame;
    private HouseModel model;

    // --- AJUSTE DE VELOCIDAD ---
    // El Timer se dispara cada TICK_INTERVAL_MS milisegundos reales.
    private static final int TICK_INTERVAL_MS = 500;

    // Segundos simulados que avanzan en cada tick del Timer.
    private static final int SIMULATED_SECONDS_PER_TICK = 60; 

    /**
     * Constructor del reloj simulado.
     * Inicializa el temporizador, la ventana gráfica y comienza la simulación del
     * tiempo.
     *
     * @param env Referencia al entorno de la casa que será notificado cada vez que
     *            la HORA simulada avanza.
     */
    public SimulatedClock(HouseEnv env, HouseModel model) {
        this.houseEnv = env;
        this.model = model;

        initClockWindow();

        timer = new Timer(TICK_INTERVAL_MS, new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime();
                if (model != null) {
                    model.applyChargeEnergy();
                }
                if (houseEnv != null) {
                     houseEnv.updatePercepts();
                }
            }
        });
        timer.start();
    }

    /**
     * Crea e inicializa la ventana gráfica que muestra la hora (HH:MM) con un
     * diseño mejorado
     * y un tamaño más grande.
     */
    private void initClockWindow() {
        clockFrame = new JFrame("Reloj Simulado Domotic (1h = 30s)");
        clockFrame.setSize(400, 200);
        clockFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clockFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.DARK_GRAY);

        // Formato inicial HH:MM
        String initialTime = String.format("%02d:%02d", hours, minutes);
        timeLabel = new JLabel(initialTime, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 60));
        timeLabel.setForeground(Color.BLUE);
        timeLabel.setOpaque(true);
        timeLabel.setBackground(Color.BLACK);

        panel.add(timeLabel, BorderLayout.CENTER);
        clockFrame.add(panel);
        clockFrame.setVisible(true);
    }

    /**
     * Actualiza internamente la hora simulada avanzando SIMULATED_SECONDS_PER_TICK
     * (60)
     * segundos simulados en cada llamada (cada 500ms). Actualiza la pantalla del 
     * reloj (HH:MM).
     * Notifica al entorno SÓLO cuando la hora (el valor de 'hours') cambia.
     */
    private synchronized void updateTime() {
        int previousHour = this.hours;

        int totalSecondsToday = this.hours * 3600 + this.minutes * 60;

        // Añadimos los segundos simulados que deben pasar en este tick (60)
        totalSecondsToday += SIMULATED_SECONDS_PER_TICK;

        totalSecondsToday %= 86400; // Módulo 24h

        // Recalculamos horas, minutos y segundos
        this.hours = totalSecondsToday / 3600;
        int remainingSecondsAfterHours = totalSecondsToday % 3600;
        this.minutes = remainingSecondsAfterHours / 60;

        String currentTime = String.format("%02d:%02d", this.hours, this.minutes);
        SwingUtilities.invokeLater(() -> timeLabel.setText(currentTime));

        // Notifica al entorno SIEMPRE (en cada tick/minuto simulado)
        if (houseEnv != null) {
            houseEnv.updatePercepts();
        }
    }

    // Métodos getTime (para obtener la hora) y getMinutes
    public synchronized int getTime() {
        return hours;
    }

    public synchronized int getMinutes() {
        return minutes;
    }

    /**
     * Detiene el reloj simulado.
     * No se puede detener en el MAS ya que no tenemos al codigo de esa interfaz, si
     * se puede parar con una accion.
     */
    public void stopClock() {
        if (timer != null) {
            timer.stop();
            System.out.println("SimulatedClock detenido.");
        }
    }

}