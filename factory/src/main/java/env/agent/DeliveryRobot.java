package env.agent;

import jason.environment.grid.Location;

public final class DeliveryRobot extends AbstractAgent {
    private final int id;
    private int battery;
    private Location location;
    private boolean isCarryingPackage;

    public DeliveryRobot(int id, String name, int battery, Location location) {
        super(name, location);
        this.id = id;
        this.battery = battery;
        this.isCarryingPackage = false; // default value
        this.location = location;
    }

    public DeliveryRobot(int id, String name, int battery, int x, int y) {
        this(id, name, battery, new Location(x, y));
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocation(int x, int y) {
        this.location = new Location(x, y);
    }

    public void decreaseBattery(int amount) {
        this.battery -= amount;
        if (this.battery < 0) {
            this.battery = 0; // ensure battery does not go below zero
        }
    }

    public void increaseBattery(int amount) {
        this.battery += amount;
        if (this.battery > 100) {
            this.battery = 100; // ensure battery does not exceed 100
        }
    }

    public boolean isCarryingPackage() {
        return isCarryingPackage;
    }

    public int getBattery() {
        return battery;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }
}
