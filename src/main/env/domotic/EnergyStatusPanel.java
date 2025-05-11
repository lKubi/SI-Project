package domotic;

import javax.swing.*;
import java.awt.*;

/**
 * CREA UNA VENTANA (JFrame) que muestra el estado de energía (texto y barra de progreso)
 * para el Robot (Enfermera) y el Auxiliar.
 */
public class EnergyStatusPanel {

    private JFrame energyFrame;
    private JLabel robotEnergyLabel;
    private JProgressBar robotEnergyBar;
    private JLabel auxiliarEnergyLabel;
    private JProgressBar auxiliarEnergyBar;
    private Font statusFont = new Font("Arial", Font.BOLD, 12);

    /**
     * Constructor que crea la ventana de estado de energía.
     * @param initialMaxEnergyRobot La energía máxima inicial del robot.
     * @param initialMaxEnergyAuxiliar La energía máxima inicial del auxiliar.
     */
    public EnergyStatusPanel(int initialMaxEnergyRobot, int initialMaxEnergyAuxiliar) {
        energyFrame = new JFrame("Estado Energía Agentes");
        energyFrame.setSize(350, 120);
        energyFrame.setLocationRelativeTo(null);
        energyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel internalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        internalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        robotEnergyLabel = new JLabel("Robot E: ---/---", JLabel.CENTER);
        robotEnergyLabel.setFont(statusFont);
        robotEnergyBar = new JProgressBar(0, Math.max(1, initialMaxEnergyRobot));
        robotEnergyBar.setStringPainted(true);
        robotEnergyBar.setFont(statusFont);
        robotEnergyBar.setForeground(Color.GREEN.darker());
        robotEnergyBar.setValue(initialMaxEnergyRobot);

        auxiliarEnergyLabel = new JLabel("Aux E: ---/---", JLabel.CENTER);
        auxiliarEnergyLabel.setFont(statusFont);
        auxiliarEnergyBar = new JProgressBar(0, Math.max(1, initialMaxEnergyAuxiliar));
        auxiliarEnergyBar.setStringPainted(true);
        auxiliarEnergyBar.setFont(statusFont);
        auxiliarEnergyBar.setForeground(Color.CYAN.darker());
        auxiliarEnergyBar.setValue(initialMaxEnergyAuxiliar);

        internalPanel.add(robotEnergyLabel);
        internalPanel.add(robotEnergyBar);
        internalPanel.add(auxiliarEnergyLabel);
        internalPanel.add(auxiliarEnergyBar);

        energyFrame.getContentPane().add(internalPanel, BorderLayout.CENTER);

        updateEnergy(HouseModel.ROBOT_AGENT_ID, initialMaxEnergyRobot, initialMaxEnergyRobot);
        updateEnergy(HouseModel.AUXILIAR_AGENT_ID, initialMaxEnergyAuxiliar, initialMaxEnergyAuxiliar);

        energyFrame.setVisible(true);
    }

    /**
     * Actualiza la visualización de energía para un agente específico.
     * @param agentId ID del agente (0 para Robot, 2 para Auxiliar).
     * @param currentEnergy Energía actual.
     * @param maxEnergy Energía máxima.
     */
    public void updateEnergy(int agentId, int currentEnergy, int maxEnergy) {
         if (agentId == HouseModel.ROBOT_AGENT_ID) {
             if (maxEnergy > 0) {
                 robotEnergyLabel.setText(String.format("Robot E: %d/%d", currentEnergy, maxEnergy));
                 robotEnergyBar.setMaximum(maxEnergy);
                 robotEnergyBar.setValue(currentEnergy);
             } else {
                 robotEnergyLabel.setText("Robot E: N/A");
                 robotEnergyBar.setValue(0);
                 robotEnergyBar.setMaximum(1);
             }
         } else if (agentId == HouseModel.AUXILIAR_AGENT_ID) {
             if (maxEnergy > 0) {
                 auxiliarEnergyLabel.setText(String.format("Aux E: %d/%d", currentEnergy, maxEnergy));
                 auxiliarEnergyBar.setMaximum(maxEnergy);
                 auxiliarEnergyBar.setValue(currentEnergy);
             } else {
                 auxiliarEnergyLabel.setText("Aux E: N/A");
                 auxiliarEnergyBar.setValue(0);
                 auxiliarEnergyBar.setMaximum(1);
             }
         }
    }

    /**
     * Actualiza toda la ventana leyendo directamente del modelo.
     * @param model El modelo de la casa.
     */
     public void updatePanelFromModel(HouseModel model) {
         if (model == null) return;
         updateEnergy(HouseModel.ROBOT_AGENT_ID,
                      model.getCurrentEnergy(HouseModel.ROBOT_AGENT_ID),
                      model.getMaxEnergy(HouseModel.ROBOT_AGENT_ID));
         updateEnergy(HouseModel.AUXILIAR_AGENT_ID,
                      model.getCurrentEnergy(HouseModel.AUXILIAR_AGENT_ID),
                      model.getMaxEnergy(HouseModel.AUXILIAR_AGENT_ID));
     }

     /**
      * Cierra y libera los recursos de esta ventana de energía.
      */
     public void dispose() {
         if (energyFrame != null) {
             energyFrame.dispose();
         }
     }
}