<<<<<<< HEAD
=======

>>>>>>> 8c1763acc831b8971e14c667803ae08f255c9718
package domotic;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SimulatedClock {
    private Timer timer;
    private int hours = 0;
<<<<<<< HEAD
=======
    private int minutes = 0;
>>>>>>> 8c1763acc831b8971e14c667803ae08f255c9718
    private HouseEnv houseEnv; // Referencia al entorno de la casa

    public SimulatedClock(HouseEnv env) {
        this.houseEnv = env;
        
        // Timer para actualizar cada 250 ms (1 minuto simulado = 0.25 seg)
<<<<<<< HEAD
        timer = new Timer(20000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime(env);
=======
        timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime();
>>>>>>> 8c1763acc831b8971e14c667803ae08f255c9718
            }
        });
        timer.start();
    }

<<<<<<< HEAD
    private void updateTime(HouseEnv env) {
            if (hours == 24) {
                hours = 0;
            } else {
                hours++;
            }
        
        
        // Llamar a updatePercepts en HouseEnv después de actualizar la hora
        if (houseEnv != null) {
            env.updatePercepts();
=======
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
>>>>>>> 8c1763acc831b8971e14c667803ae08f255c9718
        }
    }
    
    // Método público para obtener la hora en formato "H:MM"
<<<<<<< HEAD
    public int getTime() {
        return hours;
=======
    public String getTime() {
        return String.format("%d:%02d", hours, minutes);
>>>>>>> 8c1763acc831b8971e14c667803ae08f255c9718
    }
}