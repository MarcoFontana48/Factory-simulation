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
        this.defaultFont = new Font("Noto Sans", Font.BOLD, 16);
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
        final Location packageGeneratorLocation = copyOf(this.model.packageGeneratorLocation);
        final Location packageDeliveryLocationA = copyOf(this.model.packageDeliveryLocationA);
        final Location packageDeliveryLocationB = copyOf(this.model.packageDeliveryLocationB);
        final Location packageDeliveryLocationC = copyOf(this.model.packageDeliveryLocationC);
        final String availablePackage = this.model.availablePackage;
        final int itemCount = this.model.itemCount;
        SwingUtilities.invokeLater(() -> {
            String deliveryNameA = "A";
            String deliveryNameB = "B";
            String deliveryNameC = "C";
            super.drawAgent(graphics, x, y, Color.lightGray, -1);
            switch (item) {
                case FactoryModel.PACKAGE_GENERATOR:
                    if (robotLocation.equals(packageGeneratorLocation)) {
                        super.drawAgent(graphics, x, y, Color.yellow, -1);
                    }
                    graphics.setColor(Color.black);
                    if (availablePackage.isEmpty()) {
                        this.drawString(graphics, x, y, this.defaultFont, "Gen");
                    } else {
                        this.drawString(graphics, x, y, this.defaultFont, "Gen (" + availablePackage + ")");
                    }
                    break;
                case FactoryModel.PACKAGE_DELIVERY_A:
                    if (robotLocation.equals(packageDeliveryLocationA)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (itemCount > 0) {
                        deliveryNameA += " (" + itemCount + ")";
                    }
                    graphics.setColor(Color.black);
                    this.drawString(graphics, x, y, this.defaultFont, deliveryNameA);
                    break;
                case FactoryModel.PACKAGE_DELIVERY_B:
                    if (robotLocation.equals(packageDeliveryLocationB)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (itemCount > 0) {
                        deliveryNameB += " (" + itemCount + ")";
                    }
                    graphics.setColor(Color.black);
                    this.drawString(graphics, x, y, this.defaultFont, deliveryNameB);
                    break;
                case FactoryModel.PACKAGE_DELIVERY_C:
                    if (robotLocation.equals(packageDeliveryLocationC)) {
                        super.drawAgent(graphics, x, y, Color.red, -1);
                    }
                    if (itemCount > 0) {
                        deliveryNameC += " (" + itemCount + ")";
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
        final Location packageGeneratorLocation = copyOf(this.model.packageGeneratorLocation);
        final Location packageDeliveryLocationA = copyOf(this.model.packageDeliveryLocationA);
        final Location packageDeliveryLocationB = copyOf(this.model.packageDeliveryLocationB);
        final Location packageDeliveryLocationC = copyOf(this.model.packageDeliveryLocationC);
        final boolean isCarryingPackage = this.model.isCarryingPackage;
        SwingUtilities.invokeLater(() -> {
            if (!(robotLocation.equals(packageDeliveryLocationA) || robotLocation.equals(packageDeliveryLocationB) || robotLocation.equals(packageDeliveryLocationC) || robotLocation.equals(packageGeneratorLocation))) {
                super.drawAgent(graphics, x, y, isCarryingPackage ? Color.gray : Color.lightGray, id);    // colora il robot a seconda che abbia o meno un 'package' in mano
                graphics.setColor(Color.black);
                super.drawString(graphics, x, y, this.defaultFont, "Robot" + id);
            }
        });
    }
}
