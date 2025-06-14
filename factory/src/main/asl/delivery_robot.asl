!start.

/* Beliefs */
available(package, truck).  //TODO: rimuovimi
batteryLevel(100).  // Initial battery level

/* Plans */

/* Main start-up plan */
+!start <-
    utils.delivery_robot_init;
    .belief(current_position(X, Y));
    .belief(truck_position(TX, TY));
    .belief(delivery_position(DId, DX, DY));
    +charging(no);
    .println("Started with battery level: ", BatteryLevel, "% at location (", X, ", ", Y, ")");
    !step(TX, TY).

/* move one step towards target */
+!step(TargetX, TargetY) : not malfunctioning(_, yes) <-
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    .println("Current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    
    if (BatteryLevel < 20) {
        .println("Battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
        //-moving_to_target(X, Y);
        !seekChargingStation;
    } else {
        if (CurrentX == TargetX & CurrentY == TargetY) {
            .println("Reached destination (", TargetX, ", ", TargetY, ")");
            //!handleArrival(TargetX, TargetY);
            -moving_to_target(TargetX, TargetY);
            !!stop_malfunction_monitoring;
        } else {
            +moving_to_target(TargetX, TargetY);
            !!start_malfunction_monitoring;
            
            // Move one step towards target using environment action (position is updated automatically)
            move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
            
            // Position will be automatically updated by environment
            .println("Movement action executed towards (", TargetX, ", ", TargetY, ")");
            ?current_position(NewX, NewY);
            .println("New position after movement: (", NewX, ", ", NewY, ")");

            //.wait(100); // Small delay for visualization/control
            !step(TargetX, TargetY);
        }
    }.

/* Handle case when robot is malfunctioning */
//+!step(X, Y) : malfunctioning(_, yes) <-
//    .println("Cannot move - robot is malfunctioning");
//    .wait(1000);
//    !step(X, Y).

/* Plan to start continuous malfunction monitoring */
+!start_malfunction_monitoring : moving_to_target(_, _) <-
    .println("Starting malfunction monitoring...");
    +monitoring_active;
    !monitor_malfunction_loop.

/* Continuous monitoring loop using custom rand_malfunction action */
+!monitor_malfunction_loop : monitoring_active & moving_to_target(_, _) & not malfunctioning(_, yes) <-
    utils.rand_malfunction(Value);  // Using custom internal action
    if (Value >= 0.95) {
        .println("Malfunction detected! (Random malfunction value: ", Value, ")");
        ?current_position(X, Y);
        .my_name(R);
        .println("Robot ", R, " is malfunctioning at (", X, ", ", Y, ").");
        +malfunctioning(R, yes);
    } else {
        .wait(500);
        // only continue monitoring if still active --> done to avoid unhandled failures due to belief deletion
        if (monitoring_active & moving_to_target(_, _)) {
            !monitor_malfunction_loop;
        }
    }.

/* Stop monitoring when movement ends */
+!stop_malfunction_monitoring <-
    .println("Stopping malfunction monitoring...");
    -monitoring_active.

/* Handle a malfunction: drop all goals and inform */
+malfunctioning(R, yes) <-
    ?current_position(X, Y);
    .println("Stopped due to malfunction at (", X, ", ", Y, ").");
    //TODO: stop the robot's movement (and remove goals?)
    // has to become a broadcast to all delivery robots and the closest one will get to this robot
    .send(R, tell, malfunctioningRobot(R, X, Y)).

/* Cleanup after successful move */
-moving_to_target(X, Y) : monitoring_active <-
    -monitoring_active;
    .println("Movement completed").

/* Seek charging station */
+!seekChargingStation <-
    .println("Searching for charging stations...");
    +found(0);
    .broadcast(askOne, whereIsChargingStation(_));
    .wait(2000);
    ?found(Count);
    if (Count > 0) {
        .println("Found ", Count, " charging station(s)!");
        !move_towards_charging_station;
    } else {
        .println("No charging stations found!");
    };
    -found(_).

/* Handle charging station location responses */  
+chargingStationLocation(X, Y)[source(Station)] : found(Count) <-
    -+found(Count + 1);
    +knownChargingStation(Station, X, Y);
    .println("Found charging station ", Station, " at (", X, ", ", Y, ")").

+!move_towards_charging_station <-
    ?knownChargingStation(Station, X, Y);
    ?current_position(CX, CY);
    .println("Moving towards charging station ", Station, " at (", X, ", ", Y, ")");
    if (CX == X & CY == Y) {
        .println("Already at charging station ", Station, ". Requesting charge.");
        !request_charge(Station, X, Y);
    } else {
        //TODO: currently simulates the robot that has already arrived.
        -+current_position(X, Y);
        .wait(1000);
        .println("Moved to charging station at (", X, ", ", Y, ")");
        !request_charge(Station, X, Y);
    }.

/* Request charging from a known station */
+!request_charge(Station, X, Y) : knownChargingStation(Station, X, Y) & current_position(X, Y) <-
    .println("Requesting charging from station ", Station, " at position (", X, ", ", Y, ")");
    .send(Station, tell, chargingRequest(_)).

/* Handle battery level queries from charging station */
+?batteryLevel(Level)[source(Station)] <-
    ?batteryLevel(CurrentLevel);
    .send(Station, tell, batteryLevel(CurrentLevel)).

/* Handle battery level updates during charging */
+updateBatteryLevel(NewLevel)[source(Station)] <-
    +charging(yes);
    -+batteryLevel(NewLevel);
    .println("Battery updated to: ", NewLevel, "%").

/* Handle charging completion */
+chargingCompleted[source(Station)] <-
    .println("Charging completed by station ", Station);
    ?batteryLevel(FinalLevel);
    .println("Final battery level: ", FinalLevel, "%");
    -+charging(no);
    // Clean up charging station knowledge
    -knownChargingStation(_, _, _);
    // After charging, continue with original mission (bring item to delivery or go to truck)
    !step(2, 2).

/* Handle busy charging station */
+chargingStationBusy[source(Station)] <-
    .println("Charging station ", Station, " is busy. Will try again later.");
    .wait(10000);
    ?current_position(X, Y);
    !request_charge(Station, X, Y).
