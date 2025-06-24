package env.behaviour;

import env.FactoryModel;
import jason.environment.grid.Location;

/**
 * MovementManager handles the movement logic for agents in the factory environment.
 * It provides methods to move agents randomly or towards a specific destination,
 * while ensuring that movements are valid and do not collide with obstacles.
 */
public class MovementManager {
    private FactoryModel model;
    private java.util.Random random = new java.util.Random();

    /**
     *  Constructs a MovementManager with the specified FactoryModel.
     *  This model is used to check the validity of movements and to update agent positions.
     *
     * @param model the FactoryModel instance that represents the model of the agents.
     */
    public MovementManager(FactoryModel model) {
        this.model = model;
    }

    /**
     * Moves an agent randomly within the factory model.
     * The agent will attempt to move in a random direction, ensuring that the new location is free.
     *
     * @param agentId the ID of the agent to move
     * @param agentLocation the current location of the agent
     * @return true if the move was successful, false otherwise
     */
    public boolean moveRandomly(int agentId, Location agentLocation) {
        int dx = 0, dy = 0;
        // Ensure at least one coordinate moves
        while (dx == 0 && dy == 0) {
            dx = random.nextBoolean() ? 1 : -1;
            dy = random.nextBoolean() ? 1 : -1;
            // Randomly decide to move only in one direction sometimes
            if (random.nextBoolean()) dx = 0;
            else dy = 0;
        }
        Location randomMove = new Location(agentLocation.x + dx, agentLocation.y + dy);
        if (this.model.isFree(randomMove.x, randomMove.y)) {
            return tryMove(agentId, randomMove);
        }
        return true;
    }

    /**
     * Moves an agent towards a specified destination while considering the agent's current location.
     * The movement is prioritized vertically or horizontally based on the relative distances to the destination.
     * If the preferred direction is blocked, it attempts random movements in the other direction.
     *
     * @param agentId the ID of the agent to move
     * @param destination the target location to move towards
     * @param agentLocation the current location of the agent
     * @return true if the move was successful, false otherwise
     */
    public boolean moveTowards(int agentId, Location destination, Location agentLocation) {
        final Location originalAgentPos = new Location(agentLocation.x, agentLocation.y); // Store original position
        
        boolean prioritizeVertical = shouldPrioritizeVertical(destination, agentLocation);
        
        // decide probabilistically whether to move towards or away from the target
        // chance to move towards the target (in order to make the robot eventually reach the target and not get stuck)
        boolean moveTowardsTarget = random.nextDouble() < 0.9;
        
        if (prioritizeVertical) {
            Location verticalMove = computeVerticalMove(destination, agentLocation, moveTowardsTarget);
            if (this.model.isFree(verticalMove.x, verticalMove.y)) {
                return tryMove(agentId, verticalMove);
            }
            
            // if vertical movement is blocked, try random horizontal movements
            int attempts = 0;
            int maxAttempts = this.model.getWidth(); // prevent infinite loop
            while (attempts < maxAttempts) {
                Location horizontalMove = computeRandomHorizontalMove(originalAgentPos, random);
                
                // if this horizontal position is free, move there
                if (this.model.isFree(horizontalMove.x, horizontalMove.y)) {
                    return tryMove(agentId, horizontalMove);
                }
                attempts++;
            }
        } else {
            // prioritize horizontal movement
            Location horizontalMove = computeHorizontalMove(destination, agentLocation, moveTowardsTarget);

            // if horizontal movement is possible, do it
            if (this.model.isFree(horizontalMove.x, horizontalMove.y)) {
                return tryMove(agentId, horizontalMove);
            }

            // if horizontal movement is blocked, try random vertical movements
            int attempts = 0;
            int maxAttempts = this.model.getHeight(); // prevent infinite loop
            while (attempts < maxAttempts) {
                Location verticalMove = computeRandomVerticalMove(originalAgentPos, random);

                // if this vertical position is free, move there
                if (this.model.isFree(verticalMove.x, verticalMove.y)) {
                    return tryMove(agentId, verticalMove);
                }
                attempts++;
            }
        }
        
        return true;
    }

    /**
     * Moves an agent away from a specified destination while considering the agent's current location.
     * The movement is prioritized vertically or horizontally based on the relative distances to the destination.
     * If the preferred direction is blocked, it attempts random movements in the other direction.
     *
     * @param agentId the ID of the agent to move
     * @param destination the target location to move away from
     * @param agentLocation the current location of the agent
     * @return true if the move was successful, false otherwise
     */
    private Location computeRandomVerticalMove(final Location originalAgentPos, java.util.Random random) {
        Location verticalMove = new Location(originalAgentPos.x, originalAgentPos.y);
        // randomly choose up or down
        if (random.nextBoolean()) {
            verticalMove.y++;
        } else {
            verticalMove.y--;
        }
        return verticalMove;
    }

    /**
     * Computes the horizontal movement for an agent.
     * This method calculates the new horizontal position of the agent based on the destination
     * and the current agent location. It takes into account whether the agent is moving towards
     * or away from the target.
     *
     * @param destination the target location to move towards or away from
     * @param agentLocation the current location of the agent
     * @param moveTowardsTarget true if the agent should move towards the target, false otherwise
     * @return the new horizontal location for the agent
     */
    private Location computeHorizontalMove(Location destination, final Location agentLocation, boolean moveTowardsTarget) {
        Location horizontalMove = new Location(agentLocation.x, agentLocation.y);
        if (moveTowardsTarget) {
            // move towards target
            if (horizontalMove.x < destination.x) {
                horizontalMove.x++;
            } else if (horizontalMove.x > destination.x) {
                horizontalMove.x--;
            }
        } else {
            // move away from target
            if (horizontalMove.x < destination.x) {
                horizontalMove.x--;
            } else if (horizontalMove.x > destination.x) {
                horizontalMove.x++;
            }
        }
        
        horizontalMove.x = (horizontalMove.x + this.model.getWidth()) % this.model.getWidth();
        return horizontalMove;
    }

    /**
     * Computes a random horizontal movement for an agent.
     * This method randomly chooses to move the agent left or right from its current position.
     *
     * @param originalAgentPos the original position of the agent
     * @param random a Random instance to generate random numbers
     * @return a new Location representing the horizontal move
     */
    private Location computeRandomHorizontalMove(final Location originalAgentPos, java.util.Random random) {
        Location horizontalMove = new Location(originalAgentPos.x, originalAgentPos.y);
        // randomly choose left or right
        if (random.nextBoolean()) {
            horizontalMove.x = (horizontalMove.x + 1) % this.model.getWidth();
        } else {
            horizontalMove.x = (horizontalMove.x - 1 + this.model.getWidth()) % this.model.getWidth();
        }
        return horizontalMove;
    }

    /**
     * Attempts to move an agent to a new location.
     * This method updates the agent's position in the model and can be extended to update the view if necessary.
     *
     * @param agentId the ID of the agent to move
     * @param move the new location to move the agent to
     * @return true if the move was successful, false otherwise
     */
    private boolean tryMove(int agentId, Location move) {
        this.model.setAgPos(agentId, move);
        return true;
    }

    /**
     * Computes a vertical movement for an agent.
     * This method calculates the new vertical position of the agent based on the destination
     * and the current agent location. It takes into account whether the agent is moving towards
     * or away from the target.
     *
     * @param destination the target location to move towards or away from
     * @param agentLocation the current location of the agent
     * @param moveTowardsTarget true if the agent should move towards the target, false otherwise
     * @return the new vertical location for the agent
     */
    private Location computeVerticalMove(Location destination, final Location agentLocation, boolean moveTowardsTarget) {
        // try vertical movement
        Location verticalMove = new Location(agentLocation.x, agentLocation.y);
        if (moveTowardsTarget) {
            // move towards target
            if (verticalMove.y < destination.y) {
                verticalMove.y++;
            } else if (verticalMove.y > destination.y) {
                verticalMove.y--;
            }
        } else {
            // move away from target
            if (verticalMove.y < destination.y) {
                verticalMove.y--;
            } else if (verticalMove.y > destination.y) {
                verticalMove.y++;
            }
        }
        return verticalMove;
    }

    /**
     * Determines whether to prioritize vertical movement based on the destination and the agent's current location.
     * This method compares the vertical and horizontal distances to the destination to decide the movement priority.
     *
     * @param dest the target location to move towards
     * @param r1 the current location of the agent
     * @return true if vertical movement should be prioritized, false otherwise
     */
    private boolean shouldPrioritizeVertical(Location dest, final Location r1) {
        // calculate initial distances to determine priority
        int verticalDistance = Math.abs(r1.y - dest.y);
        int horizontalDistance = Math.abs(r1.x - dest.x);
        boolean prioritizeVertical = verticalDistance >= horizontalDistance;
        return prioritizeVertical;
    }
}
