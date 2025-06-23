package env;

import java.util.Map;
import java.util.Random;

import env.agent.DeliveryRobot;
import jason.NoValueException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

//TODO: refactor to become FACTORY
public class FactoryEnv extends Environment {
    private FactoryModel model = new FactoryModel();
    private FactoryView view;

    @Override
    public void init(final String[] args) {
        this.model = new FactoryModel();

        // Initialize GUI if requested
        if ((args.length == 1) && args[0].equals("gui")) {
            this.view = new FactoryView(this.model);
            view.setEnvironment(this);
            this.model.setView(view);
        }
        
        // Initialize robot percepts
        //initializeRobotPercepts();
        
        // Boot the agents' percepts
        this.updatePercepts();
    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (FactoryModel)
     */
    void updatePercepts() {

    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */
    @Override
    public boolean executeAction(String agentName, Structure action) {
        System.out.println("[" + agentName + "] doing: " + action);
        boolean result = false;

        switch (action.getFunctor()) {
            case "init_dbot":
                result = executeInitDeliveryRobot(agentName, action);
                break;
            case "move_towards_target":
                result = executeMoveTowardsTarget(agentName, action);
                break;
            case "move_randomly":
                result = executeMoveRandomly(agentName, action);
                break;
            case "update_battery_level":
                result = executeUpdateBatteryLevel(agentName, action);
                break;
            case "update_malfunctioning_status":
                result = executeUpdateMalfunctioningStatus(agentName, action);
                break;
            case "update_seeking_charging_station":
                result = executeUpdateSeekingChargingStation(agentName, action);
                break;
            case "update_charging":
                result = executeUpdateCharging(agentName, action);
                break;
            case "update_helping_robot":
                result = executeUpdateHelpingRobot(agentName, action);
                break;
            case "compute_closest_charging_station":
                result = executeComputeClosestChargingStation(agentName, action);
                break;
            case "compute_closest_robot":
                result = executeComputeClosestRobot(agentName, action);
                break;
            case "register_charging_station":
                result = executeRegisterChargingStation(agentName, action);
                break;
            case "update_battery_sharing_active":
                result = executeUpdateBatterySharingActive(agentName, action);
                break;
            case "update_carrying_package":
                result = executeUpdateCarryingPackage(agentName, action);
                break;
            default:
                System.err.println("Unknown action: " + action);
                return false;
        }
        return result;
    }

    private boolean executeUpdateCarryingPackage(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_carrying_package expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_carrying_package argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setCarryingPackage(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating carrying package status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeUpdateHelpingRobot(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_helping_robot expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_helping_robot argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setHelpingRobot(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating about to help robot status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeUpdateBatterySharingActive(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_battery_sharing_active expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_battery_sharing_active argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setBatterySharingActive(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating battery sharing active status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeUpdateCharging(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_charging expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_charging argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setCharging(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating charging status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeUpdateSeekingChargingStation(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_seeking_charging_station expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_seeking_charging_station argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setSeekingChargingStation(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating seeking charging station status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeUpdateMalfunctioningStatus(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("update_malfunctioning_status expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("update_malfunctioning_status argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            robot.setMalfunctioning(status.equals("true"));
            view.repaint();
            return true;
        } catch (Exception e) {
            System.err.println("Error updating malfunctioning status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeInitDeliveryRobot(String agentName, Structure action) {
        try {
            if (action.getArity() != 4) {
                System.err.println("init_dbot expects 4 arguments: Name, X, Y, BatteryLevel");
                return false;
            }
            Term nameT = action.getTerm(0);
            Term xT = action.getTerm(1);
            Term yT = action.getTerm(2);
            Term batteryT = action.getTerm(3);
            if (!(xT instanceof NumberTerm) || !(yT instanceof NumberTerm) || !(batteryT instanceof NumberTerm) || !(nameT instanceof Atom)) {
                System.err.println("init_dbot arguments must be: Name (Atom), X (Number), Y (Number), BatteryLevel (Number) but got: " + (nameT.getClass().getSimpleName()) + ", " + (xT.getClass().getSimpleName()) + ", " + (yT.getClass().getSimpleName()) + ", " + (batteryT.getClass().getSimpleName()));
                return false;
            }
            String name = ((Atom) nameT).toString();
            int x = (int) ((NumberTerm) xT).solve();
            int y = (int) ((NumberTerm) yT).solve();
            int batteryLevel = (int) ((NumberTerm) batteryT).solve();

            // register in the model
            Location loc = new Location(x, y);
            DeliveryRobot robot = new DeliveryRobot(name, batteryLevel, loc);
            model.addDeliveryRobot(robot);
            System.out.println("Initialized delivery robot '" + name + "' at (" + x + "," + y + ") with battery " + batteryLevel);

            // optionally, inform the agent
            addPercept(agentName, Structure.parse("robot_initialized(" + x + "," + y + "," + batteryLevel + ")"));
            return true;
        } catch (Exception e) {
            System.err.println("Error initializing delivery robot for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
    * Execute register_charging_station action
    * Expected action format: register_charging_station(X, Y)
    */
    private boolean executeRegisterChargingStation(String agName, Structure action) {
        try {
            if (action.getArity() != 2) {
                System.err.println("register_charging_station expects 2 arguments: X, Y");
                return false;
            }
            Term xT = action.getTerm(0);
            Term yT = action.getTerm(1);
            if (!(xT instanceof NumberTerm) || !(yT instanceof NumberTerm)) {
                System.err.println("register_charging_station arguments must be numbers");
                return false;
            }
            int x = (int) ((NumberTerm) xT).solve();
            int y = (int) ((NumberTerm) yT).solve();

            // register in the model
            Location loc = new Location(x, y);
            model.addChargingStation(agName, loc);
            System.out.println("Registered charging station '" + agName + "' at (" + x + "," + y + ")");

            // optionally, inform the agent
            addPercept(agName, Structure.parse("station_registered(" + x + "," + y + ")"));
            return true;
        } catch (Exception e) {
            System.err.println("Error registering charging station for " + agName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeMoveRandomly(String agentName, Structure action) {
        try {
            // Parse target coordinates from action parameters
            if (action.getArity() != 2) {
                System.err.println("move_towards_target expects 2 arguments: AgentLocationX, AgentLocationY");
                return false;
            }

            Term agLocationXTerm = action.getTerm(0);
            Term agLocationYTerm = action.getTerm(1);

            if (!(agLocationXTerm instanceof NumberTerm) || !(agLocationYTerm instanceof NumberTerm)) {
                System.err.println("move_towards_target arguments must be numbers");
                return false;
            }

            int agLocationX = (int) ((NumberTerm) agLocationXTerm).solve();
            int agLocationY = (int) ((NumberTerm) agLocationYTerm).solve();
            Location agentLocation = new Location(agLocationX, agLocationY);

            // Get agent ID from name
            int agentId = this.getAgIdBasedOnName(agentName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agentName);
                return false;
            }

            // Execute one step movement using MovementManager
            boolean moveSuccess = model.getMovementManager().moveRandomly(agentId, agentLocation);

            if (moveSuccess) {
                // Get new position after move
                Location newPos = model.getAgPos(agentId);
                
                // Update agent percepts with new position
                updateAgentPosition(agentName, newPos);

                return true;
            } else {
                System.err.println("Move failed for " + agentName + " - path might be blocked");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error executing move_randomly for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
            
            if (!(targetXTerm instanceof NumberTerm) || !(targetYTerm instanceof NumberTerm) || !(agLocationXTerm instanceof NumberTerm) || !(agLocationYTerm instanceof NumberTerm)) {
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
            System.out.println("about to move towards target " + destination.x + " " + destination.y + " from " + agentLocation.x + " " + agentLocation.y);
            boolean moveSuccess = model.getMovementManager().moveTowards(agentId, destination, agentLocation);
            System.out.println("move towards target " + destination.x + " " + destination.y + " from " + agentLocation.x + " " + agentLocation.y + " success: " + moveSuccess);

            if (moveSuccess) {
                // Get new position after move
                Location newPos = model.getAgPos(agentId);
                
                // Update agent percepts with new position
                System.out.println("Updating position for " + agName + " from " + agentLocation.x + ", " + agentLocation.y + " to " + newPos.x + ", " + newPos.y);
                updateAgentPosition(agName, newPos);
                System.out.println("Position updated for " + agName + " from " + agentLocation.x + ", " + agentLocation.y + " to " + newPos.x + ", " + newPos.y);

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

        DeliveryRobot dbot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agName));
        if (dbot == null) {
            return;
        }

        dbot.setLocation(newPos);
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

            // Update the model's battery level
            int agentId = this.getAgIdBasedOnName(agName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agName);
                return;
            }
            DeliveryRobot dbot = model.getDeliveryRobotById(agentId);
            dbot.setBattery(newBatteryLevel);
            view.repaint();
        } catch (Exception e) {
            System.err.println("Error updating battery level for " + agName + ": " + e.getMessage());
        }
    }

    /**
    * Simulate battery consumption (modified to use the new update method)
    */
    private void consumeBattery(String agName, int consumption) {
        try {
            // TODO: THE GET CURRENT BATTERY LEVEL NEEDS A PERCEPTION THAT IS NOT THERE YET, SO IT GOES TO THE END OF METHOD "getCurrentBatteryLevel"
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
    * Get current battery level for agent
    */
    public int getCurrentBatteryLevel(String agName) {
        try {
            for (Literal percept : getPercepts(agName)) {
                if (percept.getFunctor().equals("batteryLevel") && percept.getArity() == 1) {
                    try {
                        int batteryLevel = (int) ((NumberTerm) percept.getTerm(0)).solve();
                        System.out.println("Current battery level for " + agName + ": " + batteryLevel);
                        return batteryLevel;
                    } catch (Exception e) {
                        System.err.println("Error parsing battery level for " + agName + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting percepts for " + agName + ": " + e.getMessage());
        }
        System.out.println("Returning default battery level for " + agName);
        Random random = new Random();
        return random.nextInt(21) + 80; //TODO: THIS IS TEMPORARY, REMOVE THIS ONCE FIXED
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

    private boolean executeComputeClosestRobot(String agName, Structure action) {
        try {
            // get the parameters from the Jason action
            ListTerm robotList = (ListTerm)   action.getTerm(0);  // List of [Name, X, Y]
            NumberTerm myXTerm = (NumberTerm) action.getTerm(1);  // ThisRobot’s X
            NumberTerm myYTerm = (NumberTerm) action.getTerm(2);  // ThisRobot’s Y

            int currentX = (int) myXTerm.solve();
            int currentY = (int) myYTerm.solve();

            String closestRobotName = null;
            int    closestX         = -1;
            int    closestY         = -1;
            double minDistance      = Double.MAX_VALUE;

            // iterate through all robots in the list
            for (Term robotTerm : robotList) {
                ListTerm entry = (ListTerm) robotTerm;
                String    name = entry.get(0).toString().replace("\"", "");
                int       x    = (int) ((NumberTerm) entry.get(1)).solve();
                int       y    = (int) ((NumberTerm) entry.get(2)).solve();

                double distance = calculateEuclideanDistance(currentX, currentY, x, y);

                if (distance < minDistance) {
                    minDistance         = distance;
                    closestRobotName    = name;
                    closestX            = x;
                    closestY            = y;
                }
            }

            if (closestRobotName != null) {
                // remove any previous percept
                removePerceptsByUnif(agName,
                    Literal.parseLiteral("closestRobot(_, _, _)"));
                // add the new closest-robot percept
                addPercept(agName,
                    Literal.parseLiteral(
                        "closestRobot(\"" +
                        closestRobotName + "\", " +
                        closestX + ", " +
                        closestY + ")"
                    )
                );
            }
            return true;

        } catch (Exception e) {
            System.err.println("Error computing closest robot for " + agName + ": " +
                            e.getMessage());
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

    public Location getTruckLocation() {
        return model.getTruckLocation();
    }

    public Location getDeliveryLocation() {
        return model.getDeliveryLocation();
    }

    /**
    * Register a charging station at a specific location
    * This method should be called when a charging station initializes
    */
    public void registerChargingStation(String stationName, int x, int y) {
        Location location = new Location(x, y);
        model.addChargingStation(stationName, location);
        System.out.println("Registered charging station " + stationName + " at location (" + x + ", " + y + ")");
    }

    /**
    * Unregister a charging station
    */
    public void unregisterChargingStation(String stationName) {
        model.removeChargingStation(stationName);
        System.out.println("Unregistered charging station " + stationName);
    }

    /**
    * Get all registered charging station locations
    */
    public Map<String, Location> getChargingStationLocations() {
        return model.getChargingStationLocations();
    }

    public DeliveryRobot getDeliveryRobotByLocation(Location loc) {
        return model.getDeliveryRobotByLocation(loc);
    }

    public DeliveryRobot getDeliveryRobotById(int id) {
        return model.getDeliveryRobotById(id);
    }

    public int getAgIdBasedOnName(String agName) {
        return switch (agName) {
            case "d_bot_1" -> 0;
            case "d_bot_2" -> 1;
            case "d_bot_3" -> 2;
            case "d_bot_4" -> 3;
            case "d_bot_5" -> 4;
            case "ch_st_1" -> 5;
            case "ch_st_2" -> 6;
            case "ch_st_3" -> 7;
            case "truck_1" -> 8;
            case "deliv_A" -> 9;
            case "humn_1" -> 10;
            default -> -1;
        };
    }
}
