package env;

import jason.NoValueException;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

//TODO: refactor to become FACTORY
public class FactoryEnv extends Environment {
    private static final FactoryModel FACTORY_MODEL = new FactoryModel(); 

    // action literals
    public static final Literal openTruck = Literal.parseLiteral("open(truck)");
    public static final Literal closeTruck = Literal.parseLiteral("close(truck)");
    public static final Literal getPackage = Literal.parseLiteral("get(package)");
    public static final Literal handInPackage = Literal.parseLiteral("hand_in(package)");
    public static final Literal takePackage = Literal.parseLiteral("take_item(package)");

    // belief literals
    public static final Literal hasDeliveryAPackage = Literal.parseLiteral("has(deliveryA,package)");
    public static final Literal atTruck = Literal.parseLiteral("at(robot,truck)");
    public static final Literal atDeliveryA = Literal.parseLiteral("at(robot,deliveryA)");

    @Override
    public void init(final String[] args) {

    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (HouseModel)
     */
    void updatePercepts() {

    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */
    @Override
    public boolean executeAction(final String agentNameString, final Structure action) {
        System.out.println("[" + agentNameString + "] doing: " + action);
        boolean result = false;

        if (action.getFunctor().equals("move_towards_target")) {
            result = executeMoveTowardsTarget(agentNameString, action);
        } else if (action.getFunctor().equals("update_battery_level")) {
            result = executeUpdateBatteryLevel(agentNameString, action);
        } else if (action.getFunctor().equals("compute_closest_charging_station")) {
            result = executeComputeClosestChargingStation(agentNameString, action);
        } else {
            System.err.println("Unknown action: " + action);
            return false;
        }

        return result;
    }

    /**
     * Execute move_towards_target action
     * Expected action format: move_towards_target(TargetX, TargetY)
     */
    private boolean executeMoveTowardsTarget(String agName, Structure action) {
        try {
            // Parse target coordinates from action parameters
            if (action.getArity() != 4) {
                System.err.println("move_towards_target expects 4 arguments: TargetX, TargetY, AgentLocationX, AgentLocationY");
                return false;
            }
            
            Term targetXTerm = action.getTerm(0);
            Term targetYTerm = action.getTerm(1);
            Term agLocationXTerm = action.getTerm(2);
            Term agLocationYTerm = action.getTerm(3);
            
            if (!(targetXTerm instanceof NumberTerm) || !(targetYTerm instanceof NumberTerm) 
                    || !(agLocationXTerm instanceof NumberTerm) || !(agLocationYTerm instanceof NumberTerm)) {
                System.err.println("move_towards_target arguments must be numbers");
                return false;
            }
            
            int targetX = (int) ((NumberTerm) targetXTerm).solve();
            int targetY = (int) ((NumberTerm) targetYTerm).solve();
            int agLocationX = (int) ((NumberTerm) agLocationXTerm).solve();
            int agLocationY = (int) ((NumberTerm) agLocationYTerm).solve();
            Location destination = new Location(targetX, targetY);
            Location agentLocation = new Location(agLocationX, agLocationY);

            // Get agent ID from name
            int agentId = this.getAgIdBasedOnName(agName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agName);
                return false;
            }

            // Execute one step movement using MovementManager
            boolean moveSuccess = FACTORY_MODEL.getMovementManager().moveTowards(agentId, destination, agentLocation);

            if (moveSuccess) {
                // Get new position after move
                Location newPos = FACTORY_MODEL.getAgPos(agentId);
                
                // Update agent percepts with new position
                updateAgentPosition(agName, newPos);
                
                // Simulate battery consumption
                consumeBattery(agName, 1); // 1% per move

                return true;
            } else {
                System.err.println("Move failed for " + agName + " - path might be blocked");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error executing move_towards_target for " + agName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Execute update_battery_level action
     * Expected action format: update_battery_level(NewBatteryLevel)
     */
    private boolean executeUpdateBatteryLevel(String agName, Structure action) {
        if (action.getArity() != 1) {
            System.err.println("update_battery_level expects 1 argument: NewBatteryLevel");
            return false;
        }

        Term newBatteryLevelTerm = action.getTerm(0);
        if (!(newBatteryLevelTerm instanceof NumberTerm)) {
            System.err.println("update_battery_level argument must be a number");
            return false;
        }

        int newBatteryLevel;
        try {
            newBatteryLevel = (int) ((NumberTerm) newBatteryLevelTerm).solve();
        } catch (NoValueException e) {
            e.printStackTrace();
            return false;
        }

        if (newBatteryLevel < 0 || newBatteryLevel > 100) {
            System.err.println("Battery level must be between 0 and 100");
            return false;
        }

        updateBatteryLevel(agName, newBatteryLevel);
        return true;
    }

    /**
     * Update agent's position percepts
     */
    private void updateAgentPosition(String agName, Location newPos) {
        // Remove old position percept
        removePerceptsByUnif(agName, Literal.parseLiteral("current_position(_,_)"));
        
        // Add new position percept
        addPercept(agName, Literal.parseLiteral("current_position(" + newPos.x + "," + newPos.y + ")"));
    }
    
    /**
    * Update agent's battery level percepts (can be called from external sources like charging stations)
    */
    public void updateBatteryLevel(String agName, int newBatteryLevel) {
        try {
            // Remove old battery level percept
            removePerceptsByUnif(agName, Literal.parseLiteral("batteryLevel(_)"));
            
            // Add new battery level percept
            addPercept(agName, Literal.parseLiteral("batteryLevel(" + newBatteryLevel + ")"));
        } catch (Exception e) {
            System.err.println("Error updating battery level for " + agName + ": " + e.getMessage());
        }
    }

    /**
    * Simulate battery consumption (modified to use the new update method)
    */
    private void consumeBattery(String agName, int consumption) {
        try {
            // Get current battery level from agent's percepts
            int currentBattery = getCurrentBatteryLevel(agName);
            int newBattery = Math.max(0, currentBattery - consumption);
            
            // Use the centralized update method
            updateBatteryLevel(agName, newBattery);
            
        } catch (Exception e) {
            System.err.println("Error consuming battery for " + agName + ": " + e.getMessage());
        }
    }

    /**
    * Get current battery level for agent (improved error handling)
    */
    private int getCurrentBatteryLevel(String agName) {
        try {
            for (Literal percept : getPercepts(agName)) {
                if (percept.getFunctor().equals("batteryLevel") && percept.getArity() == 1) {
                    try {
                        int batteryLevel = (int) ((NumberTerm) percept.getTerm(0)).solve();
                        return batteryLevel;
                    } catch (Exception e) {
                        System.err.println("Error parsing battery level for " + agName + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting percepts for " + agName + ": " + e.getMessage());
        }
        return 100;
    }

    private boolean executeComputeClosestChargingStation(String agName, Structure action) {
        try {
            // Get the parameters from the Jason action
            ListTerm stationList = (ListTerm) action.getTerm(0);  // StationList
            NumberTerm robotX = (NumberTerm) action.getTerm(1);   // ThisRobotX
            NumberTerm robotY = (NumberTerm) action.getTerm(2);   // ThisRobotY
            
            int currentX = (int) robotX.solve();
            int currentY = (int) robotY.solve();
            
            String closestStationName = null;
            int closestStationX = -1;
            int closestStationY = -1;
            double minDistance = Double.MAX_VALUE;
            
            // Iterate through all stations in the list
            for (Term stationTerm : stationList) {
                ListTerm station = (ListTerm) stationTerm;
                
                // Extract station data: [Station, X, Y]
                String stationName = station.get(0).toString().replace("\"", ""); // Remove quotes
                int stationX = (int) ((NumberTerm) station.get(1)).solve();
                int stationY = (int) ((NumberTerm) station.get(2)).solve();
                
                // Calculate Euclidean distance
                double distance = calculateEuclideanDistance(currentX, currentY, stationX, stationY);
                
                // Check if this is the closest station so far
                if (distance < minDistance) {
                    minDistance = distance;
                    closestStationName = stationName;
                    closestStationX = stationX;
                    closestStationY = stationY;
                }
            }
            
            // Add the closest station as a percept for the agent
            if (closestStationName != null) {
                // Remove any previous closestChargingStation percept
                removePerceptsByUnif(agName, Literal.parseLiteral("closestChargingStation(_, _, _)"));
                
                // Add the new closest station percept
                addPercept(agName, Literal.parseLiteral(
                    "closestChargingStation(\"" + closestStationName + "\", " + 
                    closestStationX + ", " + closestStationY + ")"
                ));
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error computing closest charging station for " + agName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
    * Calculate Euclidean distance between two points
    * @param x1 X coordinate of first point (robot position)
    * @param y1 Y coordinate of first point (robot position)
    * @param x2 X coordinate of second point (station position)
    * @param y2 Y coordinate of second point (station position)
    * @return Euclidean distance between the two points
    */
    private double calculateEuclideanDistance(int x1, int y1, int x2, int y2) {
        double deltaX = x2 - x1;
        double deltaY = y2 - y1;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public int getAgIdBasedOnName(String agName) {
        return switch (agName) {
            case "d_bot_1" -> 0;
            case "d_bot_2" -> 1;
            case "d_bot_3" -> 2;
            case "ch_st_1" -> 3;
            case "ch_st_2" -> 4;
            case "truck_1" -> 5;
            case "deliv_A" -> 6;
            case "deliv_B" -> 7;
            default -> -1;
        };
    }
}
