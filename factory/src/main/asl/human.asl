!start.

// Human agent in the factory simulation
// It initializes the human's starting position, moves randomly,
// checks the status of all robots periodically, and repairs a random
// malfunctioning robots only if all robots are malfunctioning (this
// edge case may happen because all robots have a random
// chance of malfunctioning every 1/2 seconds and may simultaneously
// malfunction, even though there's a slim chance of happening).
+!start <-
    utils.human_init;
    .belief(current_position(X, Y));
    ?current_position(X, Y);
    .println("Started at position (", X, ", ", Y, ")");
    .my_name(HumanName);
    register_human(HumanName, X, Y);
    !periodic_status_check.

// periodic status checking
+!periodic_status_check <-
    .wait(30000);  // wait n seconds for responses
    .println("About to request status from all robots...");
    .abolish(robot_status(_, _, _, _, _, _, _, _, _, _, _, _));  // clear previous status
    .broadcast(askOne, request_status);
    .wait(500);  // wait n seconds for responses
    !check_malfunctioning_robots;
    !periodic_status_check.

// Check for any malfunctioning robots and repair one randomly
+!check_malfunctioning_robots <-
    .findall(RobotName, robot_status(RobotName, _, _, _, _, true, _, _, _, _, _, _), MalfunctioningRobots);
    .findall(RobotName, robot_status(RobotName, _, _, _, _, _, _, _, _, _, _, _), AllRobots);
    .length(MalfunctioningRobots, MalfunctionCount);
    .length(AllRobots, TotalCount);
    
    .println("status check: ", MalfunctionCount, " out of ", TotalCount, " robots are malfunctioning");
    
    /*
    if all robots are malfunctioning simultaneously (it may happen because all robots have a random 
    change of malfunctioning every 1/2 seconds), in order not to stop the simulation, choose one
    randomly for repair, recharge it partially, and let it resume working. This robot will then be
    able to repair other robots that will eventually resume the simulation entirely as if it were
    starting again from the beginning
    */
    if (MalfunctionCount == TotalCount) {
        .println("all robots are malfunctioning, selecting one for repair...");
        !select_random_robot_for_repair(MalfunctioningRobots);
    } else {
        .println("not all robots are malfunctioning, skipping repair selection...");
    }.

// Select a random robot from the malfunctioning list for repair
+!select_random_robot_for_repair(MalfunctioningRobots) <-
    .length(MalfunctioningRobots, Count);
    if (Count > 0) {
        utils.rand_int(RandomNumber, 0, Count - 1);
        .nth(RandomNumber, MalfunctioningRobots, SelectedRobot);

        ?robot_status(SelectedRobot, X, Y, Battery, Carrying, true, _, _, _, _, _, _);
        .println("Randomly selected robot ", SelectedRobot, " at position (", X, ", ", Y, ") with ", Battery, "% battery for repair");
        
        !remotely_recharge_robot(SelectedRobot, X, Y);
    } else {
        .println("ERROR: No malfunctioning robots found in the list!");
    }.

// Handle status responses from robots
+robot_status(RobotName, X, Y, Battery, Carrying, Malfunctioning, YY, MM, DD, HH, NN, SS)[source(RobotId)] <-
    .println("Robot ", RobotId, " status at ", YY, ":", MM, ":", DD, " - ", HH, ":", NN, ":", SS, ": Position(", X, ",", Y, "), Battery:", Battery, "%, Package:", Carrying, ", Malfunction:", Malfunctioning).

// Remotely recharge and repair a robot
+!remotely_recharge_robot(R, X, Y) <-
    .println("Starting to remotely repair and recharge robot ", R, " at position (", X, ", ", Y, ")");
    !repair_loop(R).

// Repair loop for a robot
// This will check the robot's battery and repair it if necessary
+!repair_loop(RobotName) <-
    .wait(100);
    
    // Check if we still have status for this robot
    if (robot_status(RobotName, X, Y, Battery, Carrying, Malfunctioning, _, _, _, _, _, _)) {
        .println("Current status of ", RobotName, ": Battery ", Battery, "%, Malfunctioning: ", Malfunctioning);
        
        // if robot's battery is above 99%, repair it remotely and send confirmation
        if (Battery > 99) {
            .println("Robot ", RobotName, " has sufficient battery (", Battery, "%). Sending repair confirmation...");
            .send(RobotName, tell, remotely_repaired);
            .println("Repair completed for robot ", RobotName);
        // else if battery is below 99%, send a battery unit remotely, and repeat repair loop
        } else {
            .println("Robot ", RobotName, " battery too low (", Battery, "%). Sending remote battery unit...");
            .send(RobotName, achieve, receive_battery_unit(1));
            .wait(100);  // wait a bit for the battery update to process
            
            // Request updated status before continuing
            .send(RobotName, askOne, request_status);
            .wait(100);  // wait for status response

            !repair_loop(RobotName);
        }
    } else {
        .println("ERROR: No status available for robot ", RobotName, ". Cannot continue repair.");
    }.
