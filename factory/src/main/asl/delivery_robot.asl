!start.

batteryLevel(100).                      // initial battery level
carrying_package(false).                // track if robot is carrying a package
delivery_completed(false).              // track if delivery is completed
askedChargingStationLocation(false).    // track if charging station location has been asked

+!start <-
    utils.delivery_robot_init;
    .belief(current_position(X, Y));
    .belief(truck_position(TX, TY));
    .belief(delivery_position(DId, DX, DY));
    ?batteryLevel(BatteryLevel);
    .println("started with battery level: ", BatteryLevel, "% at location (", X, ", ", Y, ")");
    .println("truck is at: (", TX, ", ", TY, ")");
    .println("delivery location is: (", DX, ", ", DY, ")");
    !step(TX, TY).

+!step(TargetX, TargetY) : not malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    
    if(BatteryLevel <= 0) {
        +malfunctioning;
        !step(TargetX, TargetY);
    }

    if (BatteryLevel < 20 & not seekingChargingStation & not charging) {
        .println("battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
        
        // save current state before seeking charging station
        +saved_target(TargetX, TargetY);
        ?carrying_package(CarryingStatus);
        +saved_carrying_status(CarryingStatus);
        .println("saved state - target: (", TargetX, ", ", TargetY, "), carrying: ", CarryingStatus);
        
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

            !step(TargetX, TargetY);
        }
    }.

/* handle case when robot is malfunctioning */
+!step(TargetX, TargetY) : malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    .println("cannot move due to malfunctions, stopped at (", CurrentX, ", ", CurrentY, ") and willing to move to target (", TargetX, ", ", TargetY, "), waiting for repair to arrive");
    .wait(1000);
    !step(TargetX, TargetY).

/* plan to start continuous malfunction monitoring */
+!start_malfunction_monitoring : moving_to_target(_, _) & not monitoring_active <-
    .println("starting malfunction monitoring...");
    +monitoring_active;
    !monitor_malfunction_loop.

+!start_malfunction_monitoring : not (moving_to_target(_, _) & not monitoring_active) <-
    .println("malfunction monitoring already active, skipping new monitoring.").

/* continuous monitoring loop using custom rand_malfunction action */
+!monitor_malfunction_loop : monitoring_active & moving_to_target(_, _) & not malfunctioning <-
    utils.rand_malfunction(Value);  // using custom internal action
    if (Value >= 0.9999999999) {
        .println("malfunction detected! (Random malfunction value: ", Value, ")");
        +malfunctioning;
    } else {
        .wait(500);
        !monitor_malfunction_loop;
    }.

+!monitor_malfunction_loop : not ( monitoring_active & moving_to_target(_,_) & not malfunctioning) <-
    !!stop_malfunction_monitoring.

/* stop monitoring when movement ends */
+!stop_malfunction_monitoring <-
    .println("stopping malfunction monitoring...");
    -monitoring_active.

/* handle a malfunction */
+malfunctioning <-
    !stop_malfunction_monitoring;
    ?current_position(X, Y);
    .println("stopped due to malfunction at (", X, ", ", Y, ").").

+!handleArrival(TargetX, TargetY) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(X, Y);
    .wait(500);
   
    if (seekingChargingStation) {
        .println("arrived at charging station location (", X, ", ", Y, ")");
        -seekingChargingStation;

        ?knownChargingStation(Station, X, Y);
        !request_charge(Station, X, Y); //TODO: 4
    }
    // handle arrival at truck position
    if (truck_position(TargetX, TargetY) & not carrying_package(true)) {
        .println("arrived at truck location. Attempting to pick up package...");
        !pickup_package_from_truck;
    }
    // handle arrival at delivery position
    if (delivery_position(_, TargetX, TargetY) & carrying_package(true)) {
        .println("arrived at delivery location. delivering package...");
        !deliver_package;
    }.

/* plan to pick up package from truck with better debugging */
+!pickup_package_from_truck <-
    ?truck_position(TX, TY);
    ?current_position(X, Y);
    
    if (X == TX & Y == TY) {
        .send(truck, tell, request_package);
        .println("message sent to truck, waiting for response...");
        .wait(2000);

        if (package_received) {
            .println("received package_received message");
            -package_received;
            -carrying_package(false);
            +carrying_package(true);
            .println("package successfully picked up from truck!");
            
            ?delivery_position(DId, DX, DY);
            .println("now heading to delivery location (", DX, ", ", DY, ")");
            !step(DX, DY);
        } else {
            .println("ERROR: did not receive package_received message from truck");
        }
    }.

+package_received[source(truck)] <-
    +package_received.

+!deliver_package <-
    ?delivery_position(DId, DX, DY);
    ?current_position(X, Y);
    
    if (X == DX & Y == DY & carrying_package(true)) {
        .my_name(RobotName);
        .println("at delivery location - requesting package delivery");
        .send(delivery_place, tell, package_delivery_request("PKG001", RobotName));
        .wait(1000);
        -carrying_package(true);
        +carrying_package(false);
        +delivery_completed(true);
        .println("package delivery process completed");
        
        // Return to truck after successful delivery for next package
        ?truck_position(TX, TY);
        .println("delivery completed! Now returning to truck at (", TX, ", ", TY, ") for next package");
        !step(TX, TY);
    } else {
        .println("ERROR: cannot deliver package - not at delivery location or not carrying package");
    }.

/* handle delivery confirmation */
+delivery_confirmed(PackageId)[source(DeliveryPlace)] <-
    .println("delivery confirmed by delivery place for package: ", PackageId).

/* seek charging station */
+!seekChargingStation <-
    +seekingChargingStation;
    .println("searching for charging stations...");
    // there's no need to ask for charging station location if we already know it, so this is asked only once
    if (askedChargingStationLocation(false)) {
        +askedChargingStationLocation(true);
        .broadcast(askOne, whereIsChargingStation(_));
        .wait(2000);
    }
    .findall([Station, X, Y], knownChargingStation(Station, X, Y), StationList);
    .length(StationList, Count);
    if (Count > 0) {
        .println("found ", Count, " charging station(s): ", StationList);
        ?current_position(ThisRobotX, ThisRobotY);
        compute_closest_charging_station(StationList, ThisRobotX, ThisRobotY);
        ?closestChargingStation(ClosestStation, ClosestStationX, ClosestStationY);
        .println("closest charging station is ", ClosestStation, " at (", ClosestStationX, ", ", ClosestStationY, ")");
        !move_towards_charging_station(ClosestStation, ClosestStationX, ClosestStationY);
    } else {
        .println("no charging stations found!");
    }.

+chargingStationLocation(X, Y)[source(Station)] <-
    +knownChargingStation(Station, X, Y);
    .println("found charging station ", Station, " at (", X, ", ", Y, ")").

+!move_towards_charging_station(Station, X, Y) <-
    .println("moving towards charging station ", Station, " at (", X, ", ", Y, ")");
    !step(X, Y).

+!request_charge(Station, X, Y) : knownChargingStation(Station, X, Y) & current_position(X, Y) <-
    .my_name(RobotName);
    .println("Requesting charging from station ", Station);
    .send(Station, tell, chargingRequest(RobotName)).

+?batteryLevel(Level)[source(Station)] <-
    .println("charging station ", Station, " is asking for battery level");
    ?batteryLevel(CurrentLevel);
    .println("sending battery level ", CurrentLevel, "% to ", Station);
    .send(Station, tell, batteryLevel(CurrentLevel)).

+updateBatteryLevel(NewLevel)[source(Station)] <-
    -updateBatteryLevel(NewLevel)[source(Station)];
    +charging;
    -batteryLevel(_);
    +batteryLevel(NewLevel);
    ?batteryLevel(CurrentLevel);
    update_battery_level(NewLevel);
    .println("battery updated to: ", NewLevel, "%").

+chargingCompleted[source(Station)] <-
    -chargingCompleted[source(Station)];
    .println("charging completed by station ", Station);
    ?batteryLevel(FinalLevel);
    .println("final battery level: ", FinalLevel, "%");

    // clean up charging state
    -charging;
    
    // debug saved state
    .findall([X, Y], saved_target(X, Y), SavedTargets);
    .findall(Status, saved_carrying_status(Status), SavedStatuses);
    
    ?saved_carrying_status(WasCarrying);
    .println("restoring previous state - target: (", SavedX, ", ", SavedY, "), was carrying: ", WasCarrying);
    
    // clean up saved state
    -saved_target(SavedX, SavedY);
    -saved_carrying_status(WasCarrying);
    
    // start moving again to previous target
    !step(SavedX, SavedY).
