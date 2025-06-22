// HUMAN AGENT
!start.

+!start <-
    utils.human_init;
    .belief(current_position(X, Y));
    ?current_position(X, Y);
    .println("Started at position (", X, ", ", Y, ")");
    !!periodic_status_check;
    !step(X, Y).

// main movement loop
+!step(GoToX, GoToY) <-
    ?current_position(X, Y);
    .println("Current position: (", X, ", ", Y, ")");
    move_randomly(X, Y);    // may move randomly or not move at all at each step
    .wait(1500);
    !step(X, Y).

// periodic status checking
+!periodic_status_check <-
    .println("About to request status from all robots...");
    .abolish(robot_status(_, _, _, _, _, _, _, _, _, _, _, _));  // clear previous status
    .broadcast(askOne, request_status);
    .wait(10000);  // wait n seconds for responses
    !check_all_robots_malfunction;
    !periodic_status_check.

// Check if all robots are malfunctioning
+!check_all_robots_malfunction <-
    .findall(RobotName, robot_status(RobotName, _, _, _, _, true, _, _, _, _, _, _), MalfunctioningRobots);
    .findall(RobotName, robot_status(RobotName, _, _, _, _, _, _, _, _, _, _, _), AllRobots);
    .length(MalfunctioningRobots, MalfunctionCount);
    .length(AllRobots, TotalCount);
    
    .println("status check: ", MalfunctionCount, " out of ", TotalCount, " robots are malfunctioning");
    
    if (MalfunctionCount > 0 & MalfunctionCount == TotalCount & TotalCount > 0) {
        .println("all robots are malfunctioning, selecting one for emergency repair...");
        !select_random_robot_for_repair(MalfunctioningRobots);
    } else {
        if (MalfunctionCount > 0) {
            .println("some robots are malfunctioning, not all of them");
        } else {
            .println("all robots are operational");
        }
    }.

// Select a random robot from the malfunctioning list for repair
+!select_random_robot_for_repair(MalfunctioningRobots) <-
    .length(MalfunctioningRobots, Count);
    if (Count > 0) {
        utils.rand_int(RandomNumber, 0, Count - 1);
        .nth(RandomNumber, MalfunctioningRobots, SelectedRobot);

        ?robot_status(SelectedRobot, X, Y, Battery, Carrying, true, _, _, _, _, _, _);
        .println("Randomly selected robot ", SelectedRobot, " at position (", X, ", ", Y, ") with ", Battery, "% battery for emergency repair");
        
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

+!repair_loop(RobotName) <-
    .wait(500);
    
    // Check if we still have status for this robot
    if (robot_status(RobotName, X, Y, Battery, Carrying, Malfunctioning, _, _, _, _, _, _)) {
        .println("Current status of ", RobotName, ": Battery ", Battery, "%, Malfunctioning: ", Malfunctioning);
        
        // if robot's battery is above 50%, repair it remotely and send confirmation
        if (Battery > 50) {
            .println("Robot ", RobotName, " has sufficient battery (", Battery, "%). Sending repair confirmation...");
            .send(RobotName, tell, remotely_repaired);
            .println("Emergency repair completed for robot ", RobotName);
        // else if battery is below 50%, send a battery unit remotely, and repeat repair loop
        } else {
            .println("Robot ", RobotName, " battery too low (", Battery, "%). Sending remote battery unit...");
            .send(RobotName, achieve, receive_battery_unit(1));
            .wait(250);  // wait a bit for the battery update to process
            
            // Request updated status before continuing
            .send(RobotName, askOne, request_status);
            .wait(250);  // wait for status response
            
            !repair_loop(RobotName);
        }
    } else {
        .println("ERROR: No status available for robot ", RobotName, ". Cannot continue repair.");
    }.