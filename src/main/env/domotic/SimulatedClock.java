
package domotic;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
    private int minutes = 0;
    private HouseEnv houseEnv; // Referencia al entorno de la casa

    public SimulatedClock(HouseEnv env) {
        this.houseEnv = env;
        
        // Timer para actualizar cada 250 ms (1 minuto simulado = 0.25 seg)
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime();
            }
        });
        timer.start();
    }

    private void updateTime() {
        minutes++;
        if (minutes == 60) {
            minutes = 0;
            hours++;
            if (hours == 24) {
                hours = 0;
            }
        }
        
        // Llamar a updatePercepts en HouseEnv después de actualizar la hora
        if (houseEnv != null) {
            houseEnv.updatePercepts();
        }
    }
    
    // Método público para obtener la hora en formato "H:MM"
    public String getTime() {
        return String.format("%d:%02d", hours, minutes);
    }
}