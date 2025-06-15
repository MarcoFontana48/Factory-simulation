!start.

/* Beliefs */
available(package, truck).  //TODO: rimuovimi
batteryLevel(100).  // initial battery level

/* Plans */

/* Main start-up plan */
+!start <-
    utils.delivery_robot_init;
    .belief(current_position(X, Y));
    .belief(truck_position(TX, TY));
    .belief(delivery_position(DId, DX, DY));
    ?batteryLevel(BatteryLevel);
    .println("Started with battery level: ", BatteryLevel, "% at location (", X, ", ", Y, ")");
    !step(TX, TY).

/* move one step towards target
Possible targets are:
    - delivery location
    - truck location
    - charging station
    - this robot's current position (in this case the robot has reached its target)
    - another robot's position (if it is malfunctioning and asking for help)
 */
+!step(TargetX, TargetY) : not malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    .println("Current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    
    if(BatteryLevel <= 0) {
        +malfunctioning;
        !step(TargetX, TargetY);
    }

    if (BatteryLevel < 20 & not seekingChargingStation & not charging) {
        .println("Battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
        -moving_to_target(X, Y);
        !seekChargingStation;
    } else {
        if (CurrentX == TargetX & CurrentY == TargetY) {
            !handleArrival(TargetX, TargetY);
            !!stop_malfunction_monitoring;
        } else {
            +moving_to_target(TargetX, TargetY);

            if (not monitoring_active) {
                !!start_malfunction_monitoring;
            }
            
            // moves one step towards target avoiding obstacles and updating battery level
            move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
            .wait(500);
            ?current_position(NewX, NewY);

            !step(TargetX, TargetY);
        }
    }.

/* Handle case when robot is malfunctioning */
+!step(TargetX, TargetY) : malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    .println("Cannot move due to malfunctions, stopped at (", CurrentX, ", ", CurrentY, ") and willing to move to target (", TargetX, ", ", TargetY, "), waiting for repair to arrive");
    .wait(1000);
    !step(TargetX, TargetY).

/* Plan to start continuous malfunction monitoring */
+!start_malfunction_monitoring : moving_to_target(_, _) & not monitoring_active <-
    .println("Starting malfunction monitoring...");
    +monitoring_active;
    !monitor_malfunction_loop.

+!start_malfunction_monitoring : not (moving_to_target(_, _) & not monitoring_active) <-
    .println("Malfunction monitoring already active, skipping new monitoring.").

/* continuous monitoring loop using custom rand_malfunction action */
+!monitor_malfunction_loop : monitoring_active & moving_to_target(_, _) & not malfunctioning <-
    utils.rand_malfunction(Value);  // using custom internal action
    if (Value >= 0.99) {
        .println("Malfunction detected! (Random malfunction value: ", Value, ")");
        +malfunctioning;
    } else {
        .wait(500);
        !monitor_malfunction_loop;
    }.

/* stops monitoring each time the robot stops moving towards the target
there can be multiple reasons for this behaviour to occur:
    - malfunctioning
    - waiting for charging stations to reply with their locations before moving towards them
    - reached target destination (see the list of possible targets for better understanding)
*/
+!monitor_malfunction_loop : not ( monitoring_active & moving_to_target(_,_) & not malfunctioning) <-
    !!stop_malfunction_monitoring.

/* Stop monitoring when movement ends */
+!stop_malfunction_monitoring <-
    .println("Stopping malfunction monitoring...");
    -monitoring_active.

/* Handle a malfunction: the 'step' plan is not executed when this is active */
+malfunctioning <-
    !stop_malfunction_monitoring;
    ?current_position(X, Y);
    .println("Stopped due to malfunction at (", X, ", ", Y, ")."). //TODO: remember to remove malfunctioning belief after maintenance

    // has to become a broadcast to all delivery robots and the closest one will get to this robot
    //.send(R, tell, malfunctioningRobot(R, X, Y)).

+!handleArrival(TargetX, TargetY) <-
    .println("Reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(X, Y);
    .wait(500); // this wait is not necessary, it only helps in visualization
    if (seekingChargingStation) {
        .println("Arrived at charging station location (", X, ", ", Y, ")");
        -seekingChargingStation;
        !request_charge(Station, X, Y);
    }.

/* Seek charging station */
+!seekChargingStation <-
    +seekingChargingStation;
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
    !step(X, Y).

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
    +charging;
    -+batteryLevel(NewLevel);
    update_battery_level(NewLevel);
    .println("Battery updated to: ", NewLevel, "%").

/* Handle charging completion */
+chargingCompleted[source(Station)] <-
    .println("Charging completed by station ", Station);
    ?batteryLevel(FinalLevel);
    .println("Final battery level: ", FinalLevel, "%");
    -charging;
    // Clean up charging station knowledge
    -knownChargingStation(_, _, _);
    // After charging, continue with original mission (bring item to delivery or go to truck)
    //TODO: currently temporarily moves towards truck again, but have to change to delivery or truck based on the fact that the robot has a package or not (check the facts and move accordingly, es: current_target(malfunctioningRobot, 9, 8), or current_target(delivery, 5, 6) --> based on unification (to be done like: (malfunctioningRobot, X, Y) unifies with (malfunctioningRobot, X, Y) ??? )) move towards the respective target)
    ?truck_position(TX, TY);
    ?delivery_position(DId, DX, DY);
    !step(TX, TY).

/* Handle busy charging station */
+chargingStationBusy[source(Station)] <-
    .println("Charging station ", Station, " is busy. Will try again later.");
    .wait(10000);
    ?current_position(X, Y);
    !request_charge(Station, X, Y).
