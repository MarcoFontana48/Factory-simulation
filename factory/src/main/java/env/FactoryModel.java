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
    private final Location delivery2Location = new Location(GSize / 2 - 2, GSize - 1);
    private final Location delivery3Location = new Location(GSize / 2 + 2, GSize - 1);
    private final int deliveryLocation1Id = 1;
    private final int deliveryLocation2Id = 2;
    private final int deliveryLocation3Id = 3;

    public FactoryModel() {
        super(FactoryModel.GSize, FactoryModel.GSize, 0);
    }

    public Location getTruckLocation() {
        return truckLocation;
    }

    public Location getDelivery1Location() {
        return delivery1Location;
    }

    public Location getDelivery2Location() {
        return delivery2Location;
    }

    public Location getDelivery3Location() {
        return delivery3Location;
    }

    public int getDelivery1Id() {
        return deliveryLocation1Id;
    }

    public int getDelivery2Id() {
        return deliveryLocation2Id;
    }

    public int getDelivery3Id() {
        return deliveryLocation3Id;
    }
}
