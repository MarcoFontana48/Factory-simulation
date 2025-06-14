!start.

/* Beliefs */
available(package, truck).

/* Plans */

/* Main start-up plan */
+!start <-
    utils.delivery_robot_init;
    .belief(batteryLevel(BatteryLevel));
    .belief(current_position(X, Y));
    +charging(no);
    .println("Started with battery level: ", BatteryLevel, "% at location (", X, ", ", Y, ")");
    !step.

+!step <-
    //tmp placeholder for initial step
    !step(2, 5).

/* Move towards target (unless already malfunctioning) */
//TODO: creare una funzione STEP per muovere il robot un passo alla volta e ogni volta controllare se la batteria è sufficiente (la logica che è qui)
+!step(X, Y) : not malfunctioning(_, yes) <-
    ?current_position(X0, Y0);
    ?batteryLevel(BatteryLevel);
    .println("Moving towards target (", X, ", ", Y, ") from (", X0, ", ", Y0, ") with battery level ", BatteryLevel, "%");
    .my_name(R);
    if (BatteryLevel < 20) {
        .println("Battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
        !seekChargingStation;
    } else {
        +moving_to_target(X, Y);              
        !!start_malfunction_monitoring;       
        .wait(2000);
        .println("Reached destination (", X, ", ", Y, ")");
        -moving_to_target(X, Y);              
        !!stop_malfunction_monitoring;        
    }.

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
    // After charging, continue with original mission
    !step(2, 2).

/* Handle busy charging station */
+chargingStationBusy[source(Station)] <-
    .println("Charging station ", Station, " is busy. Will try again later.");
    .wait(10000);
    ?current_position(X, Y);
    .println("Retrying charging request at station ", Station, " at position (", X, ", ", Y, ")");
    !request_charge(Station, X, Y).