package env.agent;

import jason.environment.grid.Location;

public final class DeliveryRobot extends AbstractAgent {
    private int battery;
    private Location location;
    private boolean isCarryingPackage;
    private boolean isMalfunctioning = false;
    private boolean isCharging = false;
    private boolean isSeekingChargingStation = false;
    private boolean isBatterySharingActive = false;
    private boolean isHelpingRobot = false;

    public DeliveryRobot(String name, int battery, Location location) {
        super(name, location);
        this.battery = battery;
        this.isCarryingPackage = false; // default value
        this.location = location;
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

    public void setCarryingPackage(boolean isCarryingPackage) {
        this.isCarryingPackage = isCarryingPackage;
    }

    public boolean isCarryingPackage() {
        return isCarryingPackage;
    }

    public boolean isMalfunctioning() {
        return isMalfunctioning;
    }

    public void setMalfunctioning(boolean isMalfunctioning) {
        this.isMalfunctioning = isMalfunctioning;
    }

    public void setSeekingChargingStation(boolean isSeekingChargingStation) {
        this.isSeekingChargingStation = isSeekingChargingStation;
    }

    public boolean isSeekingChargingStation() {
        return isSeekingChargingStation;
    }

    public void setCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public boolean isBatterySharingActive() {
        return isBatterySharingActive;
    }

    public void setBatterySharingActive(boolean isBatterySharingActive) {
        this.isBatterySharingActive = isBatterySharingActive;
    }

    public int getBattery() {
        return battery;
    }

    public Location getLocation() {
        return location;
    }

    public void setHelpingRobot(boolean aboutToHelp) {
        this.isHelpingRobot = aboutToHelp;
    }

    public boolean isHelpingRobot() {
        return isHelpingRobot;
    }
}
