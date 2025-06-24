package env.agent;

import jason.environment.grid.Location;

/**
 * Represents an abstract agent in the factory environment.
 */
public abstract class AbstractAgent {
    protected final String name;
    protected final Location location;

    /**
     * Constructs an AbstractAgent with the specified name and location.
     *
     * @param name     the name of the agent
     * @param location the initial location of the agent
     */
    public AbstractAgent(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Returns the name of the agent.
     *
     * @return the name of the agent
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the location of the agent.
     *
     * @return the location of the agent
     */
    public Location getLocation() {
        return location;
    }
}
