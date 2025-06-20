package env;

import java.util.Map;

import env.agent.DeliveryRobot;
import env.behaviour.MovementManager;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

public class FactoryModel extends GridWorldModel {
    public static final int GSize = 13;
    private final Location truckLocation = new Location(GSize / 2, 0);
    private final Location delivery1Location = new Location(GSize / 2, GSize - 1);
    private final int deliveryLocationId = 1;
    private final int truckId = 11;
    private final MovementManager movementManager = new MovementManager(this);

    public FactoryModel() {
        super(FactoryModel.GSize, FactoryModel.GSize, 8);
    }

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
