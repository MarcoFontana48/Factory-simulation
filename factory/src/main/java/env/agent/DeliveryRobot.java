package env.agent;

import jason.environment.grid.Location;

/**
 * Represents a delivery robot in the factory environment.
 */
public class DeliveryRobot extends AbstractAgent {
    private int battery;
    private Location location;
    private boolean isCarryingPackage;
    private boolean isMalfunctioning = false;
    private boolean isCharging = false;
    private boolean isSeekingChargingStation = false;
    private boolean isBatterySharingActive = false;
    private boolean isHelpingRobot = false;

    /**
     * Constructs a DeliveryRobot with the specified name, battery level, and location.
     *
     * @param name     the name of the robot
     * @param battery  the initial battery level (0-100)
     * @param location the initial location of the robot
     */
    public DeliveryRobot(String name, int battery, Location location) {
        super(name, location);
        this.battery = battery;
        this.isCarryingPackage = false; // default value
        this.location = location;
    }

    /**
     * Sets the battery level of the robot.
     */
    public void setBattery(int battery) {
        this.battery = battery;
    }

    /**
     * Sets the location of the robot.
     *
     * @param location the new location of the robot
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Sets the location of the robot using x and y coordinates.
     *
     * @param x the x-coordinate of the new location
     * @param y the y-coordinate of the new location
     */
    public void setLocation(int x, int y) {
        this.location = new Location(x, y);
    }

    /**
     * Decreases the battery level of the robot.
     *
     * @param amount the amount to decrease the battery by
     */
    public void decreaseBattery(int amount) {
        this.battery -= amount;
        if (this.battery < 0) {
            this.battery = 0; // ensure battery does not go below zero
        }
    }

    /**
     * Increases the battery level of the robot.
     *
     * @param amount the amount to increase the battery by
     */
    public void increaseBattery(int amount) {
        this.battery += amount;
        if (this.battery > 100) {
            this.battery = 100; // ensure battery does not exceed 100
        }
    }

    /**
     * Checks if the robot is carrying a package.
     *
     * @return true if the robot is carrying a package, false otherwise
     */
    public void setCarryingPackage(boolean isCarryingPackage) {
        this.isCarryingPackage = isCarryingPackage;
    }

    /**
     * Returns whether the robot is currently carrying a package.
     *
     * @return true if the robot is carrying a package, false otherwise
     */
    public boolean isCarryingPackage() {
        return isCarryingPackage;
    }

    /**
     * Checks if the robot is malfunctioning.
     *
     * @return true if the robot is malfunctioning, false otherwise
     */
    public boolean isMalfunctioning() {
        return isMalfunctioning;
    }

    /**
     * Sets the malfunctioning status of the robot.
     *
     * @param isMalfunctioning true if the robot is malfunctioning, false otherwise
     */
    public void setMalfunctioning(boolean isMalfunctioning) {
        this.isMalfunctioning = isMalfunctioning;
    }

    /**
     * Checks if the robot is seeking a charging station.
     *
     * @return true if the robot is seeking a charging station, false otherwise
     */
    public void setSeekingChargingStation(boolean isSeekingChargingStation) {
        this.isSeekingChargingStation = isSeekingChargingStation;
    }

    /**
     * Returns whether the robot is currently seeking a charging station.
     *
     * @return true if the robot is seeking a charging station, false otherwise
     */
    public boolean isSeekingChargingStation() {
        return isSeekingChargingStation;
    }

    /**
     * Sets the charging status of the robot.
     *
     * @param isCharging true if the robot is charging, false otherwise
     */
    public void setCharging(boolean isCharging) {
        this.isCharging = isCharging;
    }

    /**
     * Returns whether the robot is currently charging.
     *
     * @return true if the robot is charging, false otherwise
     */
    public boolean isCharging() {
        return isCharging;
    }

    /**
     * Sets the battery sharing status of the robot.
     *
     * @param isBatterySharingActive true if battery sharing is active, false otherwise
     */
    public boolean isBatterySharingActive() {
        return isBatterySharingActive;
    }

    /**
     * Sets the battery sharing status of the robot.
     *
     * @param isBatterySharingActive true if battery sharing is active, false otherwise
     */
    public void setBatterySharingActive(boolean isBatterySharingActive) {
        this.isBatterySharingActive = isBatterySharingActive;
    }

    /**
     * Returns the current battery level of the robot.
     *
     * @return the battery level
     */
    public int getBattery() {
        return battery;
    }

    /**
     * Returns the current location of the robot.
     *
     * @return the location of the robot
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets whether the robot is about to help another robot.
     *
     * @param aboutToHelp true if the robot is about to help, false otherwise
     */
    public void setHelpingRobot(boolean aboutToHelp) {
        this.isHelpingRobot = aboutToHelp;
    }

    /**
     * Returns whether the robot is about to help another robot.
     *
     * @return true if the robot is helping, false otherwise
     */
    public boolean isHelpingRobot() {
        return isHelpingRobot;
    }
}
