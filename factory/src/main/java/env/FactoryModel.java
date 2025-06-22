package env;

import env.behaviour.MovementManager;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.HashMap;
import java.util.Map;

public class FactoryModel extends GridWorldModel {
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
    
    // Dynamic charging station storage
    private final Map<String, Location> chargingStationLocations = new HashMap<>();
    
    public FactoryModel() {
        super(FactoryModel.GSize, FactoryModel.GSize, 25);
        this.addWallsRandomly();
        
        // Add static objects to the grid
        this.add(TRUCK, truckLocation);
        this.add(DELIVERY, deliveryLocation);
    }

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
    
    // Methods to manage dynamic charging stations
    public void addChargingStation(String stationName, Location location) {
        chargingStationLocations.put(stationName, location);
        this.add(CHARGING_STATION, location);
        
        // Update view if available
        if (this.view != null) {
            this.view.repaint();
        }
    }
    
    public void removeChargingStation(String stationName) {
        Location location = chargingStationLocations.remove(stationName);
        if (location != null) {
            this.remove(CHARGING_STATION, location);
            
            // Update view if available
            if (this.view != null) {
                this.view.repaint();
            }
        }
    }
    
    public Map<String, Location> getChargingStationLocations() {
        return new HashMap<>(chargingStationLocations);
    }
    
    public boolean hasChargingStationAt(Location location) {
        return chargingStationLocations.containsValue(location);
    }

    public boolean isAdjacentToKeyLocation(int x, int y) {
        Location loc = new Location(x, y);
        return (euclideanDistance(loc, truckLocation) < 1.5) || (euclideanDistance(loc, deliveryLocation) < 1.5);
    }

    private double euclideanDistance(Location a, Location b) {
        int dx = a.x - b.x;
        int dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // Existing methods
    public Location getTruckLocation() {
        return truckLocation;
    }
    
    public int getTruckId() {
        return truckId;
    }
    
    public Location getDeliveryLocation() {
        return deliveryLocation;
    }
    
    public int getDeliveryId() {
        return deliveryLocationId;
    }
    
    public MovementManager getMovementManager() {
        return movementManager;
    }
}
