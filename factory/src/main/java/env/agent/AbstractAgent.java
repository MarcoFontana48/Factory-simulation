package env.agent;

import jason.environment.grid.Location;

public abstract class AbstractAgent {
    protected final String name;
    protected final Location location;

    public AbstractAgent(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
}
