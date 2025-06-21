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
    move_randomly(X, Y);    //may move randomly or not move at all at each step
    .wait(500);
    !step(X, Y).

// periodic status checking
+!periodic_status_check <-
    .println("About to request status from all robots...");
    -robot_status(_, _, _, _, _, _);
    .broadcast(askOne, request_status);
    .wait(10000);  // wait n seconds before next check
    !periodic_status_check.

// Handle status responses from robots
+robot_status(RobotName, X, Y, Battery, Carrying, Malfunctioning, YY, MM, DD, HH, NN, SS)[source(RobotId)] <-
    .println("Robot ", RobotId, " status at ", YY, ":", MM, ":", DD, " - ", HH, ":", NN, ":", SS, ": Position(", X, ",", Y, "), Battery:", Battery, "%, Package:", Carrying, ", Malfunction:", Malfunctioning).

/* handle malfunction reports from robots */
//+?robotMalfunctioning(RobotName, X, Y) <-
//    .my_name(MyName);
//    ?current_position(MyX, MyY);
//    .println("received malfunction report from robot ", RobotName, " at (", X, ", ", Y, "), my position is (", MyX, ", ", MyY, "), responding with acknowledgment");
//
//    .send(RobotName, tell, malfunction_ack(MyName, MyX, MyY)).
//
//+!redirect_to_help(R, X, Y) <-
//    .println("i have been requested to help robot ", R, " at (", X, ", ", Y, ")");
//    .println("starting to remotely repair and recharge robot ", RobotName);
//    !repair_loop(RobotName).
//
//+!repair_loop(RobotName) <-
//    .wait(500);
//    ?robot_status(RobotName, X, Y, Battery, Carrying, Completed, Malfunctioning)[source(RobotName)];
//    // if robot's battery is above 50%, repair it remotely and send confirmation
//    if (Battery > 50) {
//        .println("robot repaired and recharged, sending confirmation to ", RobotName);
//        .send(RobotName, tell, remotely_repaired);
//    // else if battery is below 50%, send a battery unit remotely, and repeat repair loop until battery is above 50% then repair it 
//    } else {
//        .println("sending ", RobotName, " a remote unit of battery");
//        .send(RobotName, achieve, receive_battery_unit(1));
//        !repair_loop(RobotName);
//    }.
