package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import javax.swing.SwingUtilities;

import env.agent.DeliveryRobot;

public class FactoryView extends GridWorldView implements FactoryModel.ModelObserver {
    private FactoryEnv environment;
    private Set<Location> dirtyRegions = new HashSet<>();
    private boolean isUpdating = false;
   
    public FactoryView(final FactoryModel model) {
        super(model, "Factory Simulation", 700);
        this.defaultFont = new Font("Noto Sans", Font.BOLD, 16);
        
        // Register as observer
        if (model instanceof FactoryModel) {
            ((FactoryModel) model).addObserver(this);
        }
        
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.repaint();
        });
    }
    
    public void setEnvironment(FactoryEnv environment) {
        this.environment = environment;
    }

    // Observer pattern implementations
    @Override
    public void onAgentUpdated(Location location, int agentId) {
        updateAgent(location, agentId);
    }

    @Override
    public void onAgentMoved(Location oldLocation, Location newLocation, int agentId) {
        updateAgentMovement(oldLocation, newLocation, agentId);
    }

    @Override
    public void onCellUpdated(Location location) {
        updateCell(location);
    }

    // Selective update methods
    public void updateAgent(Location location, int agentId) {
        SwingUtilities.invokeLater(() -> {
            repaintCell(location);
        });
    }

    public void updateAgentMovement(Location oldLocation, Location newLocation, int agentId) {
        SwingUtilities.invokeLater(() -> {
            // Repaint both old and new locations
            repaintCell(oldLocation);
            repaintCell(newLocation);
        });
    }

    public void updateCell(Location location) {
        SwingUtilities.invokeLater(() -> {
            repaintCell(location);
        });
    }

    // Batch update method for multiple changes
    public void updateCells(List<Location> locations) {
        SwingUtilities.invokeLater(() -> {
            if (locations.size() > 5) {
                // If too many locations, just repaint everything
                repaint();
            } else {
                // Repaint individual cells
                for (Location location : locations) {
                    repaintCell(location);
                }
            }
        });
    }

    // Helper method to repaint a specific cell
    private void repaintCell(Location location) {
        if (location == null || location.x < 0 || location.y < 0 || 
            location.x >= getModel().getWidth() || location.y >= getModel().getHeight()) {
            return;
        }
        
        int x = location.x * cellSizeW;
        int y = location.y * cellSizeH;
        repaint(x, y, cellSizeW, cellSizeH);
    }



    // Force full repaint when needed
    public void forceFullRepaint() {
        SwingUtilities.invokeLater(() -> {
            repaint();
        });
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
                if (dbot != null) {
                    color = dbot.isBatterySharingActive() ? Color.magenta.darker() : 
                           dbot.isCharging() ? Color.yellow.darker() : 
                           dbot.isMalfunctioning() ? Color.red.darker() : 
                           dbot.isSeekingChargingStation() ? Color.orange : 
                           dbot.isHelpingRobot() ? Color.magenta : 
                           dbot.isCarryingPackage() ? Color.cyan : Color.cyan.darker();

                    agentTextID = "R" + (id + 1) + " " + dbot.getBattery();
                } else {
                    color = Color.gray;
                    agentTextID = "R" + (id + 1);
                }
                break;
        }
        
        super.drawAgent(graphics, x, y, color, -1);
        graphics.setColor(Color.black);
        super.drawString(graphics, x, y, this.defaultFont, agentTextID);
    }

    private DeliveryRobot getDeliveryRobotById(int id) {
        return environment != null ? environment.getDeliveryRobotById(id) : null;
    }
}