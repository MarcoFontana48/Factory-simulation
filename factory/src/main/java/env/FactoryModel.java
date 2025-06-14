package env;

import java.util.Map;

import env.agent.DeliveryRobot;
import env.behaviour.MovementManager;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

//TODO: refactor to become FACTORY
public class FactoryModel extends GridWorldModel {
    public static final int PACKAGE_GENERATOR = 16;
    public static final int PACKAGE_DELIVERY = 32;
    public static final int OBSTACLE = 4;
    public static final int GSize = 13;
    public final MovementManager navigationManager = new MovementManager(this);
    public Location truckLocation = new Location(FactoryModel.GSize / 2, 0);
    public Location deliveryLocation = new Location(FactoryModel.GSize / 2, FactoryModel.GSize - 1);
    boolean truckOpen = false;
    boolean isCarryingPackage = false;
    int itemCount = 0;
    String availablePackage = "a";
    String carriedPackageType = "";
    Map<Integer, DeliveryRobot> robots = new java.util.HashMap<>();

    public FactoryModel() {
        // create a grid with mobile agents (/*super(FactoryModel.GSize, FactoryModel.GSize, 3); */)
        super(FactoryModel.GSize, FactoryModel.GSize, 3);
        this.addRobotsPosition();
        this.addTruckPosition();
        this.addDeliveryPosition();
        this.addWalls();
    }

    private void addDeliveryPosition() {
        this.add(FactoryModel.PACKAGE_DELIVERY, this.deliveryLocation);
    }

    private void addTruckPosition() {
        this.add(FactoryModel.PACKAGE_GENERATOR, this.truckLocation);
    }

    private void addRobotsPosition() {
        int x, y;
        do {
            x = (int) (Math.random() * GSize);
            y = (int) (Math.random() * GSize);
        } while (!isFree(x, y));
        for (int i = 0; i < 3; i++) {
            DeliveryRobot robot = new DeliveryRobot(i);
            this.robots.put(i, robot);
            this.setAgPos(i, new Location(x, y));
        }
    }

    private void addWalls() {
        // first row 
        this.addWall(0, 0, 5, 0); this.addWall(7, 0, 12, 0);
        // second row
        this.addWall(0, 1, 0, 1); this.addWall(12, 1, 12, 1);
        // third row
        this.addWall(0, 2, 0, 2); this.addWall(12, 2, 12, 2);
        // fourth row
        this.addWall(0, 3, 0, 3); this.addWall(3, 3, 9, 3); this.addWall(12,3,12,3);
        // fifth row
        this.addWall(0, 4, 0, 4); this.addWall(6, 4, 6, 4); this.addWall(12, 4, 12, 4);
        // sixth row
        this.addWall(0, 5, 0, 5); this.addWall(6, 5, 6, 5); this.addWall(12, 5, 12, 5);
        // seventh row
        this.addWall(0, 6, 0, 6); this.addWall(3, 6, 9, 6); this.addWall(12, 6, 12, 6);
        // eighth row
        this.addWall(0, 7, 0, 7); this.addWall(6, 7, 6, 7); this.addWall(12, 7, 12, 7);
        // ninth row
        this.addWall(0, 8, 0, 8); this.addWall(6, 8, 6, 8); this.addWall(12, 8, 12, 8);
        // tenth row
        this.addWall(0, 9, 0, 9); this.addWall(3, 9, 9, 9); this.addWall(12, 9, 12, 9);
        // eleventh row
        this.addWall(0, 10, 0, 10); this.addWall(12, 10, 12, 10);
        // twelfth row
        this.addWall(0, 11, 0, 11); this.addWall(12, 11, 12, 11);
        // thirteenth row
        this.addWall(0, 12, 5, 12); this.addWall(7, 12, 12, 12);
    }

    /*
     * All the following methods are invoked by the environment controller (HouseEnv)
     * so as to model changes in the environment, either spontaneous or due to agents
     * interaction. As such, they first check actions pre-conditions, then carry out
     * actions post-conditions.
     */
    boolean openTruck() {
        if (!this.truckOpen) {
            this.truckOpen = true;
            return true;
        }
        return false;
    }

    boolean closeTruck() {
        if (this.truckOpen) {
            this.truckOpen = false;
            return true;
        }
        return false;
    }

    boolean moveTowards(final int agentId, final Location dest) {
        return navigationManager.moveTowards(agentId, dest);
    }

    boolean getPackage() {
        if (this.truckOpen && (!this.availablePackage.isEmpty()) && !this.isCarryingPackage) {
            this.carriedPackageType = this.availablePackage; // Remember what type we picked up
            this.availablePackage = "";
            this.isCarryingPackage = true;
            if (this.view != null) {
                this.view.update(this.truckLocation.x, this.truckLocation.y);
            }
            return true;
        }
        return false;
    }

    boolean addPackage(final String name) {
        this.availablePackage = name;
        if (this.view != null) {
            this.view.update(this.truckLocation.x, this.truckLocation.y);
        }
        return true;
    }

    boolean deliverPackage() {
        if (this.isCarryingPackage) {
            this.itemCount = new java.util.Random().nextInt(20) + 5; // randomly generate the number of items in the package
            this.isCarryingPackage = false;
            if (this.view != null) {
                this.view.update(this.deliveryLocation.x, this.deliveryLocation.y);
            }
            return true;
        }
        return false;
    }

    boolean takeItem() {
        if (this.itemCount > 0) {
            this.itemCount--;
            if (this.view != null) {
                this.view.update(this.deliveryLocation.x, this.deliveryLocation.y);
            }
            return true;
        }
        return false;
    }

    public void decreaseEnergy(final int agentId) {
        DeliveryRobot robot = this.getRobotById(agentId);
        if (robot == null) {
            throw new IllegalArgumentException("Robot with ID " + agentId + " does not exist.");
        }

        int decreaseEnergyAmount = 1;
        robot.decreaseEnergy(decreaseEnergyAmount);
    }

    public DeliveryRobot getRobotById(int agentId) {
        return this.robots.get(agentId);
    }

    public GridWorldView getView() {
        return this.view;
    }
}
