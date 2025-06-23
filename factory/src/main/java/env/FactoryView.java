package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

import env.agent.DeliveryRobot;

public class FactoryView extends GridWorldView {
    private FactoryEnv environment;
   
    public FactoryView(final FactoryModel model) {
        super(model, "Factory Simulation", 700);
        this.defaultFont = new Font("Noto Sans", Font.BOLD, 16);
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.repaint();
        });
    }
    
    // Method to set the environment reference
    public void setEnvironment(FactoryEnv environment) {
        this.environment = environment;
    }
   
    @Override
    public void draw(final Graphics graphics, final int x, final int y, final int item) {
        switch (item) {
            case FactoryModel.OBSTACLE:
                graphics.setColor(Color.BLACK);
                super.drawObstacle(graphics, x, y);
                break;
               
            case FactoryModel.TRUCK:
                graphics.setColor(Color.GREEN.darker());
                graphics.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
                graphics.setColor(Color.WHITE);
                super.drawString(graphics, x, y, this.defaultFont, "T");
                break;
               
            case FactoryModel.DELIVERY:
                graphics.setColor(Color.RED.darker());
                graphics.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
                graphics.setColor(Color.WHITE);
                super.drawString(graphics, x, y, this.defaultFont, "D");
                break;
               
            case FactoryModel.CHARGING_STATION:
                graphics.setColor(Color.ORANGE.darker());
                graphics.fillRect(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
                graphics.setColor(Color.WHITE);
                super.drawString(graphics, x, y, this.defaultFont, "C");
                break;
        }
    }
    
    @Override
    public void drawAgent(final Graphics graphics, final int x, final int y, Color color, final int id) {
        String agentTextID = "";
        switch (id) {
            case 10:     // human
                color = Color.gray;
                agentTextID = "H";
                break;
            default:    // every other agent (delivery robots)
                DeliveryRobot dbot = getDeliveryRobotById(id);

                color = dbot.isBatterySharingActive() ? Color.magenta.darker() : dbot.isCharging() ? Color.yellow.darker() : dbot.isMalfunctioning() ? Color.red.darker() : dbot.isSeekingChargingStation() ? Color.orange.darker() : dbot.isHelpingRobot() ? Color.magenta : dbot.isCarryingPackage() ? Color.cyan : Color.cyan.darker();

                agentTextID = "R" + (id + 1) + " " + dbot.getBattery(); // +1 to the id is to match the agent ID with the display
                break;
        }
        
        super.drawAgent(graphics, x, y, color, -1);
        graphics.setColor(Color.black);
        super.drawString(graphics, x, y, this.defaultFont, agentTextID);
    }
    
    /**
     * Get agent name based on ID
     */
    private String getAgentNameById(int id) {
        return switch (id) {
            case 0 -> "d_bot_1";
            case 1 -> "d_bot_2";
            case 2 -> "d_bot_3";
            case 3 -> "d_bot_4";
            case 4 -> "d_bot_5";
            case 5 -> "ch_st_1";
            case 6 -> "ch_st_2";
            case 7 -> "ch_st_3";
            case 8 -> "truck_1";
            case 9 -> "deliv_A";
            case 10 -> "humn_1";
            default -> "unknown";
        };
    }

    /**
     * Get agent name based on ID
     */
    private int getAgentIdByName(String name) {
        return switch (name) {
            case "d_bot_1" -> 0;
            case "d_bot_2" -> 1;
            case "d_bot_3" -> 2;
            case "d_bot_4" -> 3;
            case "d_bot_5" -> 4;
            case "ch_st_1" -> 5;
            case "ch_st_2" -> 6;
            case "ch_st_3" -> 7;
            case "truck_1" -> 8;
            case "deliv_A" -> 9;
            case "humn_1" -> 10;
            default -> -1;
        };
    }

    private DeliveryRobot getDeliveryRobotById(int id) {
        return environment.getDeliveryRobotById(id);
    }
}