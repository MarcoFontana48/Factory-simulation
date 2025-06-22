package env.agent;

import jason.environment.grid.Location;

public class ChargingStation extends AbstractAgent {
    private final int id;

    public ChargingStation(int id, String name, int x, int y) {
        super(name, new Location(x, y));
        this.id = id;
    }

    public ChargingStation(int id, String name, int battery, Location location) {
        super(name, location);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
