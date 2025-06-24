package env;

import java.util.Map;

import env.agent.DeliveryRobot;
import jason.NoValueException;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

/**
 * FactoryEnv is the main environment class for the factory simulation.
 * It manages the state of the factory, including delivery robots, charging stations,
 * and their interactions. It also handles actions performed by the robots and updates
 * the environment accordingly.
 */
public class FactoryEnv extends Environment {
    public static final Literal initDeliveryBot = Literal.parseLiteral("init_dbot(_,_,_,_)");
    public static final Literal moveTowardsTarget = Literal.parseLiteral("move_towards_target(_,_,_,_)");
    public static final Literal moveRandomly = Literal.parseLiteral("move_randomly(_,_)");
    public static final Literal updateBatteryLevel = Literal.parseLiteral("update_battery_level(_)");
    public static final Literal waitingReparationsDueToMalfunction = Literal.parseLiteral("waiting_reparations_due_to_malfunction(_)");
    public static final Literal goingTowardsChargingStation = Literal.parseLiteral("going_towards_charging_station(_)");
    public static final Literal batteryChargingUpdate = Literal.parseLiteral("battery_charging_update(_)");
    public static final Literal movingToRobotToRepairIt = Literal.parseLiteral("moving_to_robot_to_repair_it(_)");
    public static final Literal computeClosestChargingStation = Literal.parseLiteral("compute_closest_charging_station(_,_,_)");
    public static final Literal computeClosestRobot = Literal.parseLiteral("compute_closest_robot(_,_,_)");
    public static final Literal rechargingRobotAfterMalfunction = Literal.parseLiteral("recharging_robot_after_malfunction(_)");
    public static final Literal goingTowardsDeliveryLocation = Literal.parseLiteral("going_towards_delivery_location(_)");

    private FactoryModel model = new FactoryModel();
    private FactoryView view;

    /**
     * Initialize the factory environment.
     */
    @Override
    public void init(final String[] args) {
        this.model = new FactoryModel();

        // initialize GUI if requested
        if ((args.length == 1) && args[0].equals("gui")) {
            this.view = new FactoryView(this.model);
            view.setEnvironment(this);
        }
    }

    /**
     * Execute an action based on the agent's name and the action structure.
     * This method interprets the action and updates the state of the factory accordingly.
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
            case "waiting_reparations_due_to_malfunction":
                result = executeWaitingReparationsDueToMalfunction(agentName, action);
                break;
            case "going_towards_charging_station":
                result = executeMovingTowardsChargingStation(agentName, action);
                break;
            case "battery_charging_update":
                result = executeBatteryChargingUpdate(agentName, action);
                break;
            case "moving_to_robot_to_repair_it":
                result = executeMovingToRobotToRepairIt(agentName, action);
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
            case "recharging_robot_after_malfunction":
                result = executeRechargingRobotAfterMalfunction(agentName, action);
                break;
            case "going_towards_delivery_location":
                result = executeGoingTowardsDeliveryLocation(agentName, action);
                break;
            default:
                System.err.println("Unknown action: " + action);
                return false;
        }
        return result;
    }

    /**
     * Handles the action of a delivery robot going towards a delivery location.
     * It updates the model and view with the robot's carrying package status
     * based on the provided action.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the status of carrying a package
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeGoingTowardsDeliveryLocation(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("going_towards_delivery_location expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("going_towards_delivery_location argument must be: Status (Atom) but got: " + (statusT.getClass().getSimpleName()));
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousCarryingState = robot.isCarryingPackage();
            robot.setCarryingPackage(status.equals("true"));
            
            // only update view if state actually changed
            if (view != null && previousCarryingState != robot.isCarryingPackage()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating carrying package status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  Handles the action of a delivery robot moving towards a target location.
     *  It updates the robot's location in the model and view.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the target location
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeMovingToRobotToRepairIt(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("moving_to_robot_to_repair_it expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("moving_to_robot_to_repair_it argument must be: Status (Atom)");
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousHelpingState = robot.isHelpingRobot();
            robot.setHelpingRobot(status.equals("true"));
            
            // update view if helping state changed
            if (view != null && previousHelpingState != robot.isHelpingRobot()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating helping robot status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  Executes the action of recharging a robot sharing
     *  its battery with another one after a malfunction.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the status of battery sharing
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeRechargingRobotAfterMalfunction(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("recharging_robot_after_malfunction expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("recharging_robot_after_malfunction argument must be: Status (Atom)");
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousBatterySharingState = robot.isBatterySharingActive();
            robot.setBatterySharingActive(status.equals("true"));
            
            // update view if battery sharing state changed
            if (view != null && previousBatterySharingState != robot.isBatterySharingActive()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating battery sharing active status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes the action of charging a battery of a delivery robot, updating its battery level.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the status of battery charging
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeBatteryChargingUpdate(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("battery_charging_update expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("battery_charging_update argument must be: Status (Atom)");
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousChargingState = robot.isCharging();
            robot.setCharging(status.equals("true"));
            
            // update view if charging state changed
            if (view != null && previousChargingState != robot.isCharging()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating charging status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes the action of moving a delivery robot towards a target location.
     * It updates the robot's location in the model and view.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the target location
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeMovingTowardsChargingStation(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("going_towards_charging_station expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("going_towards_charging_station argument must be: Status (Atom)");
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousSeekingState = robot.isSeekingChargingStation();
            robot.setSeekingChargingStation(status.equals("true"));
            
            // update view if seeking state changed
            if (view != null && previousSeekingState != robot.isSeekingChargingStation()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating seeking charging station status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles the malfunctioning status of a delivery robot waiting for reparations.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the new battery level
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeWaitingReparationsDueToMalfunction(String agentName, Structure action) {
        try {
            if (action.getArity() != 1) {
                System.err.println("waiting_reparations_due_to_malfunction expects 1 argument: Status");
                return false;
            }
            Term statusT = action.getTerm(0);
            if (!(statusT instanceof Atom)) {
                System.err.println("waiting_reparations_due_to_malfunction argument must be: Status (Atom)");
                return false;
            }
            String status = ((Atom) statusT).toString();
            DeliveryRobot robot = model.getDeliveryRobotById(this.getAgIdBasedOnName(agentName));
            if (robot == null) {
                System.err.println("Unknown robot: " + agentName);
                return false;
            }
            
            // store previous state for comparison
            boolean previousMalfunctioningState = robot.isMalfunctioning();
            robot.setMalfunctioning(status.equals("true"));
            
            // update view if malfunction state changed
            if (view != null && previousMalfunctioningState != robot.isMalfunctioning()) {
                view.updateAgent(robot.getLocation(), this.getAgIdBasedOnName(agentName));
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating malfunctioning status for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes the initialization of a delivery robot,
     * setting its name, location, and battery level.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the new battery level
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeInitDeliveryRobot(String agentName, Structure action) {
        try {
            if (action.getArity() != 4) {
                System.err.println("init_dbot expects 4 arguments: Name, X, Y, BatteryLevel");
                return false;
            }
            Term nameT = action.getTerm(0);
            Term xT = action.getTerm(2);
            Term yT = action.getTerm(3);
            Term batteryT = action.getTerm(1);
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
            addPercept(agentName, Structure.parse("robot_initialized(" + x + "," + y + "," + batteryLevel + ")"));
            return true;
        } catch (Exception e) {
            System.err.println("Error initializing delivery robot for " + agentName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Executes the action of registering a charging station in the environment.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the charging station location
     * @return true if the action was executed successfully, false otherwise
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

    /**
     * Execute move_randomly action, moving in a 
     * completely random direction without any logic.
     * @param agentName the name of the agent performing the action
     * @param action the action structure containing the agent's current location
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeMoveRandomly(String agentName, Structure action) {
        try {
            // parse target coordinates from action parameters
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

            // get agent ID from name
            int agentId = this.getAgIdBasedOnName(agentName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agentName);
                return false;
            }

            // execute one step movement using MovementManager
            boolean moveSuccess = model.getMovementManager().moveRandomly(agentId, agentLocation);

            if (moveSuccess) {
                // get new position after move
                Location newPos = model.getAgPos(agentId);

                // update agent percepts with new position
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
     * Execute move_towards_target action, moving towards a specified target location.
     * @param agName the name of the agent performing the action
     * @param action the action structure containing the target coordinates and agent's current location
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeMoveTowardsTarget(String agName, Structure action) {
        try {
            // parse target coordinates from action parameters
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

            // get agent ID from name
            int agentId = this.getAgIdBasedOnName(agName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agName);
                return false;
            }

            // execute one step movement using MovementManager
            boolean moveSuccess = model.getMovementManager().moveTowards(agentId, destination, agentLocation);

            if (moveSuccess) {
                // get new position after move
                Location newPos = model.getAgPos(agentId);

                // update agent percepts with new position
                updateAgentPosition(agName, newPos);

                // simulate battery consumption
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
     * Execute update_battery_level action, updating the agent's battery level.
     * @param agName the name of the agent performing the action
     * @param action the action structure containing the new battery level
     * @return true if the action was executed successfully, false otherwise
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
     * Update the agent's position percepts and model state.
     * This method removes the old position percept and adds a new one based on the provided location.
     * It also updates the DeliveryRobot's location in the model.
     * @param agName the name of the agent whose position is being updated
     * @param newPos the new location of the agent
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
     * Update the battery level of a delivery robot.
     * This method removes the old battery level percept and adds a new one.
     * It also updates the DeliveryRobot's battery level in the model and view if applicable.
     * @param agName the name of the agent whose battery level is being updated
     * @param newBatteryLevel the new battery level to set
     */
    public void updateBatteryLevel(String agName, int newBatteryLevel) {
        try {
            // remove old battery level percept
            removePerceptsByUnif(agName, Literal.parseLiteral("batteryLevel(_)"));
            
            // add new battery level percept
            addPercept(agName, Literal.parseLiteral("batteryLevel(" + newBatteryLevel + ")"));

            // update the model's battery level
            int agentId = this.getAgIdBasedOnName(agName);
            if (agentId == -1) {
                System.err.println("Unknown agent: " + agName);
                return;
            }
            // update the DeliveryRobot's battery level in the model
            DeliveryRobot dbot = model.getDeliveryRobotById(agentId);
            dbot.setBattery(newBatteryLevel);

            // update the view if it exists
            if (view != null) {
                view.updateAgent(dbot.getLocation(), agentId);
            }
        } catch (Exception e) {
            System.err.println("Error updating battery level for " + agName + ": " + e.getMessage());
        }
    }

    /**
     * simulates battery consumption
     */
    private void consumeBattery(String agName, int consumption) {
        try {
            int currentBattery = getCurrentBatteryLevel(agName);
            int newBattery = Math.max(0, currentBattery - consumption);

            updateBatteryLevel(agName, newBattery);
        } catch (Exception e) {
            System.err.println("Error consuming battery for " + agName + ": " + e.getMessage());
        }
    }

    /**
    * get current battery level for agent
    */
    public int getCurrentBatteryLevel(String agName) {
        return model.getDeliveryRobotById(this.getAgIdBasedOnName(agName)).getBattery();
    }

    /**
     * Execute compute_closest_charging_station action, evaluating the closest charging station
     * based on the agent's current location and a list of available stations.
     * This method calculates the Euclidean distance to each station and updates the agent's percepts
     * with the closest station's name and coordinates.
     * It does not take into account obstacles or other agents, focusing solely on the distance
     * from the agent's current position to the charging stations.
     * @param agName the name of the agent performing the action
     * @param action the action structure containing the station list and agent's current location
     * @return true if the action was executed successfully, false otherwise
     */
    private boolean executeComputeClosestChargingStation(String agName, Structure action) {
        try {
            // get the parameters from the Jason action
            ListTerm stationList = (ListTerm) action.getTerm(0);  // StationList
            NumberTerm robotX = (NumberTerm) action.getTerm(1);   // ThisRobotX
            NumberTerm robotY = (NumberTerm) action.getTerm(2);   // ThisRobotY
            
            int currentX = (int) robotX.solve();
            int currentY = (int) robotY.solve();
            
            String closestStationName = null;
            int closestStationX = -1;
            int closestStationY = -1;
            double minDistance = Double.MAX_VALUE;
            
            // iterate through all stations in the list
            for (Term stationTerm : stationList) {
                ListTerm station = (ListTerm) stationTerm;
                
                // extract station data: [Station, X, Y]
                String stationName = station.get(0).toString().replace("\"", ""); // Remove quotes
                int stationX = (int) ((NumberTerm) station.get(1)).solve();
                int stationY = (int) ((NumberTerm) station.get(2)).solve();
                
                // calculate Euclidean distance
                double distance = calculateEuclideanDistance(currentX, currentY, stationX, stationY);
                
                // check if this is the closest station so far
                if (distance < minDistance) {
                    minDistance = distance;
                    closestStationName = stationName;
                    closestStationX = stationX;
                    closestStationY = stationY;
                }
            }
            
            // add the closest station as a percept for the agent
            if (closestStationName != null) {
                // remove any previous closestChargingStation percept
                removePerceptsByUnif(agName, Literal.parseLiteral("closestChargingStation(_, _, _)"));

                // add the new closest station percept
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
     * Execute compute_closest_robot action, evaluating the closest robot
     * based on the agent's current location and a list of available robots.
     * This method calculates the Euclidean distance to each robot and updates the agent's percepts
     * with the closest robot's name and coordinates.
     * It does not take into account obstacles or other agents, focusing solely on the distance
     * from the agent's current position to the robots.
     * @param agName the name of the agent performing the action
     * @param action the action structure containing the robot list and agent's current location
     * @return true if the action was executed successfully, false otherwise
     */
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

    /**
     *  Get a delivery robot by its location.
     *  This method retrieves the DeliveryRobot instance that is currently at the specified location.
     * @param loc
     * @return
     */
    public DeliveryRobot getDeliveryRobotByLocation(Location loc) {
        return model.getDeliveryRobotByLocation(loc);
    }

    /**
     * Get a delivery robot by its ID.
     * This method retrieves the DeliveryRobot instance that has the specified ID.
     * @param id the ID of the delivery robot
     * @return the DeliveryRobot instance with the specified ID, or null if not found
     */
    public DeliveryRobot getDeliveryRobotById(int id) {
        return model.getDeliveryRobotById(id);
    }

    /**
     * Get the agent ID based on its name.
     * @param agName the name of the agent
     * @return the ID of the agent, or -1 if not found or null
     */
    public int getAgIdBasedOnName(String agName) {
        if (agName == null || agName.isEmpty()) {
            return -1;
        }
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
