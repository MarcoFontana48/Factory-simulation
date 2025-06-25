package env;

import env.agent.DeliveryRobot;
import env.agent.HumanTechnician;
import env.agent.AbstractAgent;
import env.behaviour.MovementManager;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * FactoryModel represents the environment model for a factory simulation.
 * It extends GridWorldModel to manage agents, obstacles, and key locations.
 * The model supports dynamic charging stations and delivery robots with observer notifications.
 */
public class FactoryModel extends GridWorldModel {
    private Map<String, AbstractAgent> agentsMap = new HashMap<>();
    private List<ModelObserver> observers = new ArrayList<>();
    public static final int GSize = 13;
    public static final int OBSTACLE = 4;
    public static final int TRUCK = 16;
    public static final int DELIVERY = 32;
    public static final int CHARGING_STATION = 64;
    private final Location truckLocation = new Location(8, 10);
    private final Location deliveryLocation = new Location(4, 2);
    private final int deliveryLocationId = 1;
    private final int truckId = 11;
    private final MovementManager movementManager = new MovementManager(this);
    private final Map<String, Location> chargingStationLocations = new HashMap<>();
    
    /**
     * ModelObserver interface for observing changes in the FactoryModel.
     * Observers can implement this interface to receive updates on agent movements and cell updates.
     */
    public interface ModelObserver {
        void onAgentUpdated(Location location, int agentId);
        void onAgentMoved(Location oldLocation, Location newLocation, int agentId);
        void onCellUpdated(Location location);
    }

    /**
     * FactoryModel constructor initializes the grid and adds static objects.
     */
    public FactoryModel() {
        super(FactoryModel.GSize, FactoryModel.GSize, 25);
        this.addWallsRandomly();
        
        // Add static objects to the grid
        this.add(TRUCK, truckLocation);
        this.add(DELIVERY, deliveryLocation);
    }

    /**
     * Adds an observer to the model.
     * Observers will be notified of agent updates, movements, and cell updates.
     * @param observer the observer to add
     */
    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer from the model.
     * The observer will no longer receive notifications of changes.
     * @param observer the observer to remove
     */
    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that an agent has been updated.
     * This method is called when an agent's location or state changes.
     * @param location the new location of the agent
     * @param agentId the ID of the agent
     */
    private void notifyAgentUpdated(Location location, int agentId) {
        for (ModelObserver observer : observers) {
            observer.onAgentUpdated(location, agentId);
        }
    }

    /**
     * Notifies observers that an agent has moved from one location to another.
     * This method is called when an agent's position changes.
     * @param oldLocation the previous location of the agent
     * @param newLocation the new location of the agent
     * @param agentId the ID of the agent
     */
    private void notifyAgentMoved(Location oldLocation, Location newLocation, int agentId) {
        for (ModelObserver observer : observers) {
            observer.onAgentMoved(oldLocation, newLocation, agentId);
        }
    }

    /**
     * Notifies observers that a cell has been updated.
     * This method is called when an obstacle or charging station is added or removed.
     * @param location the location of the updated cell
     */
    private void notifyCellUpdated(Location location) {
        for (ModelObserver observer : observers) {
            observer.onCellUpdated(location);
        }
    }

    /**
     * Checks if a cell is free (not occupied by an obstacle or agent).
     * @param x the x-coordinate of the cell
     * @param y the y-coordinate of the cell
     * @return true if the cell is free, false otherwise
     */
    private void addWallsRandomly() {
        // Randomly place obstacles in the grid
        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * GSize);
            int y = (int) (Math.random() * GSize);
            if (this.isFree(x, y) && !this.isAdjacentToKeyLocation(x, y)) {
                this.add(OBSTACLE, new Location(x, y));
            }
        }
    }

    /**
     * Adds a charging station to the model.
     * @param stationName the name of the charging station
     * @param location the location of the charging station
     */
    public void addChargingStation(String stationName, Location location) {
        chargingStationLocations.put(stationName, location);
        this.add(CHARGING_STATION, location);
        notifyCellUpdated(location);
    }
    
    /**
     * Removes a charging station from the model.
     * @param stationName the name of the charging station to remove
     */
    public void removeChargingStation(String stationName) {
        Location location = chargingStationLocations.remove(stationName);
        if (location != null) {
            this.remove(CHARGING_STATION, location);
            notifyCellUpdated(location);
        }
    }
    
    /**
     * Retrieves the locations of all charging stations.
     * @return a map of charging station names to their locations
     */
    public Map<String, Location> getChargingStationLocations() {
        return new HashMap<>(chargingStationLocations);
    }
    
    /**
     * Checks if a specific location has a charging station.
     * @param location the location to check
     * @return true if there is a charging station at the specified location, false otherwise
     */
    public boolean hasChargingStationAt(Location location) {
        return chargingStationLocations.containsValue(location);
    }

    /**
     * Checks if a specific location is adjacent to a key location.
     * This is used to prevent obstacles from being placed too close to key locations.
     * @param x the x-coordinate of the location
     * @param y the y-coordinate of the location
     * @return true if the location is adjacent to a key location, false otherwise
     */
    public boolean isAdjacentToKeyLocation(int x, int y) {
        Location loc = new Location(x, y);
        return (euclideanDistance(loc, truckLocation) < 1.5) || (euclideanDistance(loc, deliveryLocation) < 1.5);
    }

    /**
     * Calculates the Euclidean distance between two locations.
     * @param a the first location
     * @param b the second location
     * @return the Euclidean distance between the two locations
     */
    private double euclideanDistance(Location a, Location b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Adds a new delivery robot to the model.
     * The robot is added to the deliveryRobots map and observers are notified of its initial location.
     * @param robot the DeliveryRobot to add
     */
    public void addDeliveryRobot(DeliveryRobot robot) {
        agentsMap.put(robot.getName(), robot);
        // Notify observers of new robot
        notifyAgentUpdated(robot.getLocation(), FactoryUtils.getAgIdBasedOnName(robot.getName()));
    }

    /**
     * Adds a new human technician to the model.
     * The robot is added to the agentsMap and observers are notified of its initial location.
     * @param human the HumanTechnician to add
     */
    public void addHumanTechnician(HumanTechnician human) {
        agentsMap.put(human.getName(), human);
        // Notify observers of new robot
        notifyAgentUpdated(human.getLocation(), FactoryUtils.getAgIdBasedOnName(human.getName()));
    }

    /**
     * Removes a delivery robot from the model.
     * The robot is removed from the deliveryRobots map and observers are notified of its removal.
     * @param robotName the name of the DeliveryRobot to remove
     */
    public void updateDeliveryRobotLocation(String robotName, Location oldLocation, Location newLocation) {
        AbstractAgent robot = agentsMap.get(robotName);
        if (robot != null) {
            robot.setLocation(newLocation);
            notifyAgentMoved(oldLocation, newLocation, FactoryUtils.getAgIdBasedOnName(robotName));
        }
    }

    /**
     * Updates the state of a delivery robot in the model.
     * This method is called when the robot's state changes, such as its location or battery level.
     * @param robotName the name of the DeliveryRobot to update
     */
    public void updateDeliveryRobotState(String robotName) {
        AbstractAgent robot = agentsMap.get(robotName);
        if (robot != null) {
            notifyAgentUpdated(robot.getLocation(), FactoryUtils.getAgIdBasedOnName(robotName));
        }
    }

    /**
     * Retrieves the truck location.
     * @return the location of the truck
     */
    public Location getTruckLocation() {
        return truckLocation;
    }
    
    /**
     * Retrieves the truck id.
     * @return the id of the truck
     */
    public int getTruckId() {
        return truckId;
    }
    
    /**
     * Retrieves the delivery location.
     * @return the location where deliveries are made
     */
    public Location getDeliveryLocation() {
        return deliveryLocation;
    }
    
    /**
     * Retrieves the delivery location id.
     * @return the id of the delivery location
     */
    public int getDeliveryId() {
        return deliveryLocationId;
    }
    
    /**
     * Returns the movement manager that contains the logic for 
     * movements the delivery robots managed by this model.
     * @return the movement manager for the delivery robots
     */
    public MovementManager getMovementManager() {
        return movementManager;
    }

    /**
     * Retrieves a human technician by its agent ID.
     * @param agentId the ID of the human technician
     * @return the HumanTechnician with the specified ID, or null if not found
     */
    public AbstractAgent getAgentById(int agentId) {
        return agentsMap.get(FactoryUtils.getAgNameBasedOnId(agentId));
    }

    /**
     * Retrieves a delivery robot by its location.
     * @param location the location of the delivery robot
     * @return the DeliveryRobot at the specified location, or null if not found
     */
    public AbstractAgent getDeliveryRobotByLocation(Location location) {
        for (AbstractAgent robot : agentsMap.values()) {
            if (robot.getLocation().equals(location) && robot instanceof DeliveryRobot) {
                return robot;
            }
        }
        return null;
    }
}