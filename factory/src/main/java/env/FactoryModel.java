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
    
    private final Location truckLocation = new Location(8, 12);
    private final Location delivery1Location = new Location(4, 0);
    private final int deliveryLocationId = 1;
    private final int truckId = 11;
    private final MovementManager movementManager = new MovementManager(this);
    
    // Dynamic charging station storage
    private final Map<String, Location> chargingStationLocations = new HashMap<>();
    
    public FactoryModel() {
        super(FactoryModel.GSize, FactoryModel.GSize, 8);
        //this.addWalls();
        this.addWallsRandomly();
        
        // Add static objects to the grid
        this.add(TRUCK, truckLocation);
        this.add(DELIVERY, delivery1Location);
    }

    private void addWallsRandomly() {
        // Randomly place obstacles in the grid
        for (int i = 0; i < 10; i++) {
            int x = (int) (Math.random() * GSize);
            int y = (int) (Math.random() * GSize);
            if (this.isFree(x, y)) {
                this.add(OBSTACLE, new Location(x, y));
            }
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
        this.addWall(0, 3, 0, 3); this.addWall(3, 3, 5, 3); this.addWall(7, 3, 9, 3); this.addWall(12,3,12,3);
        // fifth row
        this.addWall(0, 4, 0, 4); this.addWall(12, 4, 12, 4);
        // sixth row
        this.addWall(0, 5, 0, 5); this.addWall(12, 5, 12, 5);
        // seventh row
        this.addWall(0, 6, 0, 6); this.addWall(3, 6, 5, 6); this.addWall(7, 6, 9, 6); this.addWall(12, 6, 12, 6);
        // eighth row
        this.addWall(0, 7, 0, 7); this.addWall(12, 7, 12, 7);
        // ninth row
        this.addWall(0, 8, 0, 8); this.addWall(12, 8, 12, 8);
        // tenth row
        this.addWall(0, 9, 0, 9); this.addWall(3, 9, 5, 9); this.addWall(7, 9, 9, 9); this.addWall(12, 9, 12, 9);
        // eleventh row
        this.addWall(0, 10, 0, 10); this.addWall(12, 10, 12, 10);
        // twelfth row
        this.addWall(0, 11, 0, 11); this.addWall(12, 11, 12, 11);
        // thirteenth row
        //this.addWall(0, 12, 5, 12); this.addWall(7, 12, 12, 12);
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
    
    // Existing methods
    public Location getTruckLocation() {
        return truckLocation;
    }
    
    public int getTruckId() {
        return truckId;
    }
    
    public Location getDeliveryLocation() {
        return delivery1Location;
    }
    
    public int getDeliveryId() {
        return deliveryLocationId;
    }
    
    public MovementManager getMovementManager() {
        return movementManager;
    }
}
