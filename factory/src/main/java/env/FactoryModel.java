package env;

import java.util.List;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

//TODO: refactor to become FACTORY
public class FactoryModel extends GridWorldModel {
    public static final int PACKAGE_GENERATOR = 16;
    public static final int PACKAGE_DELIVERY_A = 32;
    public static final int PACKAGE_DELIVERY_B = 33;
    public static final int PACKAGE_DELIVERY_C = 34;
    public static final int OBSTACLE = 4;
    public static final int GSize = 11;
    // whether the fridge is open
    boolean fridgeOpen = false;
    // whether the robot is carrying beer
    boolean isCarryingPackage = false;
    // how many sips the owner did
    int itemCount = 0;
    // package available
    String availablePackage = "a";
    String carriedPackageType = "";

    // where the environment objects are
    Location packageGeneratorLocation = new Location(FactoryModel.GSize / 2, 0);
    Location packageDeliveryLocationA = new Location(FactoryModel.GSize / 2 + 2, FactoryModel.GSize - 1);
    Location packageDeliveryLocationB = new Location(FactoryModel.GSize / 2, FactoryModel.GSize - 1);
    Location packageDeliveryLocationC = new Location(FactoryModel.GSize / 2 - 2, FactoryModel.GSize - 1);
    List<Location> obstacles = List.of(
        // first row
        new Location(0, 0), new Location(1, 0), new Location(2, 0), new Location(3, 0), new Location(4, 0), new Location(6, 0), new Location(7, 0), new Location(8, 0), new Location(9, 0), new Location(10, 0),
        // second row (empty)
        // third row
        new Location(2, 2), new Location(3, 2), new Location(4, 2), new Location(8, 2),
        // fourth row
        new Location(4, 3), new Location(6, 3), new Location(7, 3), new Location(8, 3),
        // fifth row
        new Location(0, 4), new Location(1, 4), new Location(6, 4), new Location(10, 4),
        // sixth row
        new Location(3, 5), new Location(4, 5), new Location(5, 5), new Location(6, 5), new Location(8, 5),
        // seventh row
        new Location(8, 6), new Location(9, 6), new Location(10, 6),
        // eighth row
        new Location(0, 7), new Location(1, 7), new Location(2, 7), new Location(4, 7), new Location(5, 7), new Location(10, 7),
        // ninth row
        new Location(2, 8), new Location(5, 8), new Location(6, 8), new Location(7, 8), new Location(8, 8),
        // tenth row (empty)
        // eleventh row
        new Location(0, 10), new Location(1, 10), new Location(2, 10), new Location(4, 10), new Location(6, 10), new Location(8, 10), new Location(9, 10), new Location(10, 10)
    );

    public FactoryModel() {
        // create a grid with mobile agents (/*super(FactoryModel.GSize, FactoryModel.GSize, 3); */)
        super(FactoryModel.GSize, FactoryModel.GSize, 3);
        // set the agent's initial position
        this.setAgPos(0, 5, 4);
        this.setAgPos(1, 0, 1);
        this.setAgPos(2, 10, 1);
        // initial location of package generator and delivery
        this.add(FactoryModel.PACKAGE_GENERATOR, this.packageGeneratorLocation);
        this.add(FactoryModel.PACKAGE_DELIVERY_A, this.packageDeliveryLocationA);
        this.add(FactoryModel.PACKAGE_DELIVERY_B, this.packageDeliveryLocationB);
        this.add(FactoryModel.PACKAGE_DELIVERY_C, this.packageDeliveryLocationC);
        // add obstacles
        obstacles.forEach(loc -> this.add(FactoryModel.OBSTACLE, loc));
    }

    /*
     * All the following methods are invoked by the environment controller (HouseEnv)
     * so as to model changes in the environment, either spontaneous or due to agents
     * interaction. As such, they first check actions pre-conditions, then carry out
     * actions post-conditions.
     */
    boolean openFridge() {
        if (!this.fridgeOpen) {
            this.fridgeOpen = true;
            return true;
        }
        return false;
    }

    boolean closeFridge() {
        if (this.fridgeOpen) {
            this.fridgeOpen = false;
            return true;
        }
        return false;
    }

    boolean moveTowards(final Location dest) {
        final Location r1 = this.getAgPos(0);
        // compute where to move
        if (r1.x < dest.x) {
            r1.x++;
        } else if (r1.x > dest.x) {
            r1.x--;
        }
        if (r1.y < dest.y) {
            r1.y++;
        } else if (r1.y > dest.y) {
            r1.y--;
        }
        this.setAgPos(0, r1); // actually move the robot in the grid
        // repaint fridge and owner locations (to repaint colors)
        if (this.view != null) {
            this.view.update(this.packageGeneratorLocation.x, this.packageGeneratorLocation.y);
            this.view.update(this.packageDeliveryLocationA.x, this.packageDeliveryLocationA.y);
            this.view.update(this.packageDeliveryLocationB.x, this.packageDeliveryLocationB.y);
            this.view.update(this.packageDeliveryLocationC.x, this.packageDeliveryLocationC.y);
        }
        return true;
    }

    boolean getPackage() {
        if (this.fridgeOpen && (!this.availablePackage.isEmpty()) && !this.isCarryingPackage) {
            this.carriedPackageType = this.availablePackage; // Remember what type we picked up
            this.availablePackage = "";
            this.isCarryingPackage = true;
            if (this.view != null) {
                this.view.update(this.packageGeneratorLocation.x, this.packageGeneratorLocation.y);
            }
            return true;
        }
        return false;
    }

    boolean addPackage(final String name) {
        this.availablePackage = name;
        System.out.println("[" + this.getAgPos(0) + "] added package: " + this.availablePackage);
        if (this.view != null) {
            this.view.update(this.packageGeneratorLocation.x, this.packageGeneratorLocation.y);
        }
        return true;
    }

    boolean deliverPackage() {
        if (this.isCarryingPackage) {
            this.itemCount = new java.util.Random().nextInt(20) + 5; // randomly generate the number of items in the package
            this.isCarryingPackage = false;
            if (this.view != null) {
                this.view.update(this.packageDeliveryLocationA.x, this.packageDeliveryLocationA.y);
            }
            return true;
        }
        return false;
    }

    boolean takeItem() {
        if (this.itemCount > 0) {
            this.itemCount--;
            if (this.view != null) {
                this.view.update(this.packageDeliveryLocationA.x, this.packageDeliveryLocationA.y);
            }
            return true;
        }
        return false;
    }
}
