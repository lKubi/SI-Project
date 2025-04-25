package domotic;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;

/**
 * Clase que simula un reloj interno para la aplicación Domotic.
 * Incrementa la hora (horas, minutos) automáticamente.
 * La velocidad de simulación está ajustada para que 1 hora simulada = 20 segundos reales.
 * Los minutos avanzan de uno en uno en la pantalla (la pantalla se actualiza cada 1/3 de segundo real).
 * Notifica al entorno cuando la HORA simulada cambia.
 * Muestra la hora (HH:MM) en una ventana con un diseño mejorado y tamaño ajustado.
 */
public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0; // Los segundos se calculan internamente pero no se muestran
    private HouseEnv houseEnv; // Referencia al entorno a notificar
    private JLabel timeLabel; // Etiqueta para mostrar la hora
    private JFrame clockFrame; // Ventana del reloj

    // --- AJUSTE DE VELOCIDAD ---
    // El Timer se dispara cada TICK_INTERVAL_MS milisegundos reales.
    // CAMBIO: Para que 1 min sim avance por tick y 1h sim sea 20s real, el tick es cada 1000/3 ms.
    private static final int TICK_INTERVAL_MS = 333; // Aprox. 1/3 de segundo real por tick

    // Segundos simulados que avanzan en cada tick del Timer.
    // CAMBIO: Avanzamos 60 segundos (1 minuto) simulado por tick.
    private static final int SIMULATED_SECONDS_PER_TICK = 60;

    /**
     * Constructor del reloj simulado.
     * Inicializa el temporizador, la ventana gráfica y comienza la simulación del tiempo.
     *
     * @param env Referencia al entorno de la casa que será notificado cada vez que la HORA simulada avanza.
     */
    public SimulatedClock(HouseEnv env) {
        this.houseEnv = env;
        initClockWindow(); // Inicializa la ventana primero

        // Configura el Timer para que se active cada TICK_INTERVAL_MS milisegundos
        timer = new Timer(TICK_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime(); // Llama al método para actualizar el tiempo
            }
        });
        timer.start(); // Inicia el temporizador
    }

    /**
     * Crea e inicializa la ventana gráfica que muestra la hora (HH:MM) con un diseño mejorado
     * y un tamaño más grande.
     */
    private void initClockWindow() {
        // El título sigue siendo válido porque la velocidad general no cambia
        clockFrame = new JFrame("Reloj Simulado Domotic (1h = 20s)");
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
     * Actualiza internamente la hora simulada avanzando SIMULATED_SECONDS_PER_TICK (60)
     * segundos simulados en cada llamada (cada 333ms). Actualiza la pantalla del reloj (HH:MM).
     * Notifica al entorno SÓLO cuando la hora (el valor de 'hours') cambia.
     */
    private synchronized void updateTime() {
        int previousHour = this.hours;

        int totalSecondsToday = this.hours * 3600 + this.minutes * 60 + this.seconds;

        // Añadimos los segundos simulados que deben pasar en este tick (60)
        totalSecondsToday += SIMULATED_SECONDS_PER_TICK;

        totalSecondsToday %= 86400; // Módulo 24h

        // Recalculamos horas, minutos y segundos
        this.hours = totalSecondsToday / 3600;
        int remainingSecondsAfterHours = totalSecondsToday % 3600;
        this.minutes = remainingSecondsAfterHours / 60;
        this.seconds = remainingSecondsAfterHours % 60; // Segundos internos

        boolean hourChanged = (this.hours != previousHour);

        // Actualiza la etiqueta en la ventana del reloj con formato HH:MM
        String currentTime = String.format("%02d:%02d", this.hours, this.minutes);
        // Usar SwingUtilities.invokeLater para asegurar la actualización en el hilo de UI
        SwingUtilities.invokeLater(() -> timeLabel.setText(currentTime));


        // Notifica al entorno SI la hora cambió en este tick
        if (hourChanged && houseEnv != null) {
            houseEnv.updatePercepts();
        }
    }

    // Métodos getTime y getMinutes sin cambios
    public synchronized int getTime() {
        return hours;
    }

    public synchronized int getMinutes() {
        return minutes;
    }

    /**
    * Detiene el reloj simulado.
    * No se puede detener en el MAS ya que no tenemos al codigo de esa interfaz, si se puede parar con una accion.
    */
    public void stopClock() {
    if (timer != null) {
        timer.stop();
        System.out.println("SimulatedClock detenido.");
    }
    }

}