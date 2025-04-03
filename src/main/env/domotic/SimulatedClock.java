package domotic;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
    private HouseEnv houseEnv; // Referencia al entorno de la casa

    public SimulatedClock(HouseEnv env) {
        this.houseEnv = env;
        
        // Timer para actualizar cada 250 ms (1 minuto simulado = 0.25 seg)
        timer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime(env);
            }
        });
        timer.start();
    }

    private void updateTime(HouseEnv env) {
            if (hours == 24) {
                hours = 0;
            } else {
                hours++;
            }
        
        
        // Llamar a updatePercepts en HouseEnv después de actualizar la hora
        if (houseEnv != null) {
            env.updatePercepts();
        }
    }
    
    // Método público para obtener la hora en formato "H:MM"
    public int getTime() {
        return hours;
    }
}