package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.SwingUtilities;

//TODO: refactor to become FACTORY
public class FactoryView extends GridWorldView {
    FactoryModel model;

    public FactoryView(final FactoryModel model) {
        super(model, "Factory Simulation", 700);
        this.model = model;
        this.defaultFont = new Font("Noto Sans", Font.BOLD, 16); // change default font
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.repaint();
        });
    }

    private static final Location copyOf(final Location location) {
        return new Location(location.x, location.y);
    }

    @Override
    public void draw(final Graphics graphics, final int x, final int y, final int item) {
        // Handle obstacles FIRST, outside of SwingUtilities block
        if (item == FactoryModel.OBSTACLE) {
            graphics.setColor(Color.black);
            super.drawObstacle(graphics, x, y);
            return; // Exit early for obstacles
        }
        
        // Rest of your original code stays the same
        final Location robotLocation = copyOf(this.model.getAgPos(0));
        final Location ItemGeneratorLocation = copyOf(this.model.itemGeneratorLocation);
        final Location deliveryLocationA = copyOf(this.model.itemDeliveryLocationA);
        final Location deliveryLocationB = copyOf(this.model.itemDeliveryLocationB);
        final Location deliveryLocationC = copyOf(this.model.itemDeliveryLocationC);
        final String availableItem = this.model.availableItem;
        final int sipCount = this.model.sipCount;
        SwingUtilities.invokeLater(() -> {
            String deliveryNameA = "A";
            String deliveryNameB = "B";
            String deliveryNameC = "C";
            super.drawAgent(graphics, x, y, Color.lightGray, -1);
            switch (item) {
                case FactoryModel.ITEM_GENERATOR:
                    if (robotLocation.equals(ItemGeneratorLocation)) {
                        super.drawAgent(graphics, x, y, Color.yellow, -1);
                    }
                    graphics.setColor(Color.black);
                    if (availableItem.isEmpty()) {
                        this.drawString(graphics, x, y, this.defaultFont, "Gen");
                    } else {
                        this.drawString(graphics, x, y, this.defaultFont, "Gen (" + availableItem + ")");
                    }
                    break;
                case FactoryModel.ITEM_DELIVERY_A:
                    if (robotLocation.equals(deliveryLocationA)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (sipCount > 0) {
                        deliveryNameA += " (" + sipCount + ")";
                    }
                    graphics.setColor(Color.black);
                    this.drawString(graphics, x, y, this.defaultFont, deliveryNameA);
                    break;
                case FactoryModel.ITEM_DELIVERY_B:
                    if (robotLocation.equals(deliveryLocationB)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (sipCount > 0) {
                        deliveryNameB += " (" + sipCount + ")";
                    }
                    graphics.setColor(Color.black);
                    this.drawString(graphics, x, y, this.defaultFont, deliveryNameB);
                    break;
                case FactoryModel.ITEM_DELIVERY_C:
                    if (robotLocation.equals(deliveryLocationC)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (sipCount > 0) {
                        deliveryNameC += " (" + sipCount + ")";
                    }
                    graphics.setColor(Color.black);
                    this.drawString(graphics, x, y, this.defaultFont, deliveryNameC);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void drawAgent(final Graphics graphics, final int x, final int y, Color color, final int id) {
        final Location robotLocation = copyOf(this.model.getAgPos(0));
        final Location itemGeneratorLocation = copyOf(this.model.itemGeneratorLocation);
        final Location deliveryLocationA = copyOf(this.model.itemDeliveryLocationA);
        final Location deliveryLocationB = copyOf(this.model.itemDeliveryLocationB);
        final Location deliveryLocationC = copyOf(this.model.itemDeliveryLocationC);
        final boolean isCarryingItem = this.model.isCarryingItem;
        SwingUtilities.invokeLater(() -> {
            if (!(robotLocation.equals(deliveryLocationA) || robotLocation.equals(deliveryLocationB) || robotLocation.equals(deliveryLocationC) || robotLocation.equals(itemGeneratorLocation))) {
                super.drawAgent(graphics, x, y, isCarryingItem ? Color.gray : Color.lightGray, id);    // colora il robot a seconda che abbia o meno un oggetto in mano
                graphics.setColor(Color.black);
                super.drawString(graphics, x, y, this.defaultFont, "Robot" + id);
            }
        });
    }
}
