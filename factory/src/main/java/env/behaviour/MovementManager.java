package env.behaviour;

import env.FactoryModel;
import jason.environment.grid.Location;

public class MovementManager {
    private FactoryModel model;

    public MovementManager(FactoryModel model) {
        this.model = model;
    }

    public boolean moveTowards(int agentId, Location destination, Location agentLocation) {
        final Location originalAgentPos = new Location(agentLocation.x, agentLocation.y); // Store original position
        java.util.Random random = new java.util.Random();
        
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

    private boolean tryMove(int agentId, Location move) {
        this.model.setAgPos(agentId, move);
        //if (this.model.getView() != null) {
        //    this.model.getView().update(this.model.truckLocation.x, this.model.truckLocation.y);
        //    this.model.getView().update(this.model.deliveryLocation.x, this.model.deliveryLocation.y);
        //}
        return true;
    }

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

    private boolean shouldPrioritizeVertical(Location dest, final Location r1) {
        // calculate initial distances to determine priority
        int verticalDistance = Math.abs(r1.y - dest.y);
        int horizontalDistance = Math.abs(r1.x - dest.x);
        boolean prioritizeVertical = verticalDistance >= horizontalDistance;
        return prioritizeVertical;
    }
}
