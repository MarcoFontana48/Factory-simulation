package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.SwingUtilities;

public class FactoryView extends GridWorldView {
    FactoryModel model;
    
    public FactoryView(final FactoryModel model) {
        super(model, "Factory Simulation", 700);
        this.model = model;
        this.defaultFont = new Font("Noto Sans", Font.BOLD, 16);
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.repaint();
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
        String agentText = "";

        switch (id) {
            case 7:     // human
                color = Color.gray;
                agentText = "H";
                break;
            default:    // every other agent (delivery robots)
                color = Color.cyan.darker();
                agentText = "R" + (id + 1); // +1 to match the agent ID with the display
                break;
        }

        super.drawAgent(graphics, x, y, color, -1);

        graphics.setColor(Color.black);
        super.drawString(graphics, x, y, this.defaultFont, agentText);
    }
}
