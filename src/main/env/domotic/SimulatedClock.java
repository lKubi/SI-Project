package domotic;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Clase que simula un reloj interno para la aplicación Domotic.
 * Incrementa la hora automáticamente cada cierto intervalo y notifica al entorno.
 */
public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
    private HouseEnv houseEnv;

    /**
     * Constructor del reloj simulado.
     * Inicializa el temporizador y comienza la simulación del tiempo.
     *
     * @param env Referencia al entorno de la casa que será notificado cada vez que el reloj avanza.
     */
    public SimulatedClock(HouseEnv env) {
        this.houseEnv = env;

        timer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime(env);
            }
        });
        timer.start();
    }

    /**
     * Actualiza internamente la hora simulada e informa al entorno para que actualice los perceptos.
     *
     * @param env Referencia al entorno para ejecutar la actualización de perceptos.
     */
    private synchronized void updateTime(HouseEnv env) {
        if (hours == 24) {
            hours = 0;
        } else {
            hours++;
        }

        if (houseEnv != null) {
            env.updatePercepts();
        }
    }

    /**
     * Devuelve la hora actual simulada.
     *
     * @return Hora actual entre 0 y 23.
     */
    public int getTime() {
        return hours;
    }
}
