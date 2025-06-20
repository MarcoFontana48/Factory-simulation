!start.

carrying_package(false).                // track if robot is carrying a package
delivery_completed(false).              // track if delivery is completed
askedChargingStationLocation(false).    // track if charging station location has been asked

+!start <-
    utils.delivery_robot_init;
    .belief(current_position(X, Y));
    .belief(truck_position(TX, TY));
    .belief(delivery_position(DId, DX, DY));
    .belief(batteryLevel(BatteryLevel));
    ?batteryLevel(BatteryLevel);
    .println("started with battery level: ", BatteryLevel, "% at location (", X, ", ", Y, ")");
    .println("truck is at: (", TX, ", ", TY, ")");
    .println("delivery location is: (", DX, ", ", DY, ")");
    !step(TX, TY).

+!step(TargetX, TargetY) : not malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    
    // check if we need to redirect to help another robot (a redirect is checked parallelly, so that this 'step' can change its target if suddenly a help request arrives)
    if (redirect_to_help(HelpX, HelpY)) {
        -redirect_to_help(HelpX, HelpY);
        +saved_target_before_help(TargetX, TargetY);
        +saved_carrying_status_before_help(CarryingStatus);
        .println("redirecting step to help location: (", HelpX, ", ", HelpY, ")");
        !step(HelpX, HelpY);
    } else {
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
            } elif (helping_robot(_, HelpX, HelpY) & TargetX == HelpX & TargetY == HelpY) {
                // special case: when helping a robot, check if we're adjacent (within 1.5 distance)
                if (math.sqrt((CurrentX - TargetX) * (CurrentX - TargetX) + (CurrentY - TargetY) * (CurrentY - TargetY)) <= 1.5) {
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
        }
    }.

/* handle case when robot is malfunctioning */
+!step(TargetX, TargetY) : malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    .println("cannot move due to malfunctioning at (", CurrentX, ", ", CurrentY, ")");
    .wait(500);
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
    // slim chance of malfunctioning
    if (Value >= 0.999) {
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
    .my_name(RobotName);
    .println("stopped due to malfunction at (", X, ", ", Y, ")");
    -malfunction_ack(_, _, _);  // ensure not to have a previous confirmation before asking for a new one
    !broadcast_malfunction(X, Y, RobotName).

/* broadcast malfunction to other robots */
+!broadcast_malfunction(X, Y, RobotName) : not malfunction_ack(_, _, _) <-
    .println("broadcasting malfunction at (", X, ", ", Y, ")");
    .broadcast(askOne, robotMalfunctioning(RobotName, X, Y));
    .wait(5000);  // wait N second before retrying if no acknowledgment is received, otherwise it will evaluate the distances from the other robots and choose the closest one
    !broadcast_malfunction(X, Y, RobotName).

+!broadcast_malfunction(ThisRobotX, ThisRobotY, _) : malfunction_ack(_, _, _) <-
    .findall([ThatRobotName, ThatRobotX, ThatRobotY], malfunction_ack(ThatRobotName, ThatRobotX, ThatRobotY), RobotList);
    .length(RobotList, Count);
    .println("received ", Count, " malfunction acknowledgments from other robots: ", RobotList);
    !find_closest_robot_to_help(RobotList, ThisRobotX, ThisRobotY).

+!find_closest_robot_to_help(RobotList, ThisRobotX, ThisRobotY) <-
    compute_closest_robot(RobotList, ThisRobotX, ThisRobotY);   //the list is composed of: [["name1", X, Y], ["name2", X2, Y2], ...]
    ?closestRobot(ClosestName, CX, CY);
    .println("closest robot to me, is ", ClosestName, " at (", CX, ", ", CY, "), sending help request to it");
    .my_name(MyName);
    .send(ClosestName, achieve, redirect_to_help(MyName, ThisRobotX, ThisRobotY)).

/* handle malfunction reports from other robots */
+?robotMalfunctioning(RobotName, X, Y) : not malfunctioning & not helping_robot & not seekingChargingStation <-
    .my_name(MyName);
    if (RobotName \== MyName) {
        ?current_position(MyX, MyY);
        .println("received malfunction report from robot ", RobotName, " at (", X, ", ", Y, "), my position is (", MyX, ", ", MyY, "), responding with acknowledgment");

        .send(RobotName, tell, malfunction_ack(MyName, MyX, MyY));
    }.

+malfunction_ack(ThatRobotName, ThatRobotX, ThatRobotY)[source(ThatRobotName)] <-
    .println("acknowledged malfunction report from robot ", ThatRobotName, " at (", ThatRobotX, ", ", ThatRobotY, ")").

+!redirect_to_help(R, X, Y) <-
    .println("i have been requested to help robot ", R, " at (", X, ", ", Y, ")");

    // save current state before going to help
    ?current_position(CurrentX, CurrentY);
    
    // check if we're currently moving to a target and save that information
    if (moving_to_target(TargetX, TargetY)) {
        +saved_target_before_help(TargetX, TargetY);
        .println("saved current target: (", TargetX, ", ", TargetY, ")");
        -moving_to_target(TargetX, TargetY);
    } else {
        // if not moving to a specific target, save current task context
        if (carrying_package(true)) {
            ?delivery_position(DId, DX, DY);
            +saved_target_before_help(DX, DY);
            .println("saved delivery target: (", DX, ", ", DY, ")");
        } else {
            ?truck_position(TX, TY);
            +saved_target_before_help(TX, TY);
            .println("saved truck target: (", TX, ", ", TY, ")");
        }
    }
    
    // save current carrying status
    ?carrying_package(CarryingStatus);
    +saved_carrying_status_before_help(CarryingStatus);
    
    // mark that we're helping another robot and redirect current step execution
    +helping_robot(R, X, Y);
    +redirect_to_help(X, Y);
    
    .println("redirecting to help robot ", R, " at (", X, ", ", Y, ")").

/* ignore malfunction reports when we're already malfunctioning or helping someone */
+?robotMalfunctioning(RobotName, X, Y) : malfunctioning | helping_robot | seekingChargingStation <-
    if (malfunctioning) {
        .println("ignoring malfunction report from robot ", RobotName, " at (", X, ", ", Y, ") because I'm already malfunctioning");
    } elif (helping_robot(RobotName, _, _)) {
        .println("ignoring malfunction report from robot ", RobotName, " because I'm already helping another robot");
    } elif (seekingChargingStation) {
        .println("ignoring malfunction report from robot ", RobotName, " because I'm currently seeking a charging station to recharge");
    } else {
        .println("ignoring malfunction report from robot ", RobotName, " at (", X, ", ", Y, ") because I'm currently busy");
    }.

// Modified section for handling arrival near malfunctioning robot with battery sharing
+!handleArrival(TargetX, TargetY) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(X, Y);
    .wait(500);
   
    // handle arrival at charging station
    if (seekingChargingStation & knownChargingStation(Station, X, Y)) {
        .println("arrived at charging station location (", X, ", ", Y, ")");
        -seekingChargingStation;

        ?knownChargingStation(Station, X, Y);
        !request_charge(Station, X, Y);
    } elif (helping_robot(RobotName, MalfunctionX, MalfunctionY)) {
        // check if we're within 1 position of the malfunctioning robot
        ?current_position(MyX, MyY);
        EuclideanDistanceFromMalfunctioningRobot = math.sqrt((MyX - MalfunctionX) * (MyX - MalfunctionX) + (MyY - MalfunctionY) * (MyY - MalfunctionY));
        if (EuclideanDistanceFromMalfunctioningRobot <= 1.5) { // allowing for diagonal adjacency (sqrt(2) == 1.41)
            .println("arrived near malfunctioning robot ", RobotName, " (adjacent to it)");
            .println("current position: (", MyX, ", ", MyY, "), robot to help at: (", MalfunctionX, ", ", MalfunctionY, ")");
            
            // Verify we're not trying to help ourselves
            .my_name(MyName);
            if (RobotName \== MyName) {
                // Start battery sharing process with the correct robot
                !start_battery_sharing(RobotName);
            } else {
                .println("ERROR: cannot help myself! Something went wrong with robot identification.");
            }
            
            .println("finished helping robot ", RobotName, ". Returning to previous task.");
            
            // clean up helping state
            -helping_robot(RobotName, MalfunctionX, MalfunctionY);
            
            // restore previous state
            ?saved_target_before_help(SavedX, SavedY);
            ?saved_carrying_status_before_help(WasCarrying);
            
            .println("restoring previous state - target: (", SavedX, ", ", SavedY, "), was carrying: ", WasCarrying);
            
            // clean up saved state
            -saved_target_before_help(SavedX, SavedY);
            -saved_carrying_status_before_help(WasCarrying);
            
            // resume previous task
            !step(SavedX, SavedY);
        } else {
            .println("not close enough to robot ", RobotName, " (distance: ", EuclideanDistanceFromMalfunctioningRobot, "), i will be continuing to approach it...");
            !step(MalfunctionX, MalfunctionY);
        }
    } elif (truck_position(TargetX, TargetY) & not carrying_package(true)) {
        .println("arrived at truck location. Attempting to pick up package...");
        !pickup_package_from_truck;
    } elif (delivery_position(_, TargetX, TargetY) & carrying_package(true)) {
        .println("arrived at delivery location. delivering package...");
        !deliver_package;
    } else {
        if (carrying_package(false)) {
            ?truck_position(TX, TY);
            !step(TX, TY);
        } else {
            ?delivery_position(DId, DX, DY);
            !step(DX, DY);
        }
    }.

// Battery sharing plan
+!start_battery_sharing(RobotName) <-
    .println("starting battery sharing with robot ", RobotName);
    ?batteryLevel(MyBattery);
    .println("my current battery level: ", MyBattery, "%");
    
    // Request the malfunctioning robot's battery level
    //.send(RobotName, askOne, batteryLevel(Level));
    //.wait(500);
    
    +battery_sharing_active(RobotName);
    +battery_shared_amount(0);
    !battery_sharing_loop(RobotName).

// Battery sharing loop
+!battery_sharing_loop(RobotName) : battery_sharing_active(RobotName) <-
    ?batteryLevel(MyBattery);
    ?battery_shared_amount(SharedSoFar);
    
    .println("battery sharing status - my battery: ", MyBattery, "%, shared so far: ", SharedSoFar, " units");
    
    // Check if we can continue sharing (my battery > N and haven't shared U units yet)
    if (MyBattery > 30 & SharedSoFar < 25) {
        // Check if the target robot actually needs more battery (ask for its current level)
            .println("sharing 1 unit of battery with robot ", RobotName);
            
            // Send 1 unit of battery
            .send(RobotName, achieve, receive_battery_unit(1));
            
            // Update our battery level
            NewMyBattery = MyBattery - 1;
            -batteryLevel(MyBattery);
            +batteryLevel(NewMyBattery);
            update_battery_level(NewMyBattery);
            
            // Update shared amount
            NewSharedAmount = SharedSoFar + 1;
            -battery_shared_amount(SharedSoFar);
            +battery_shared_amount(NewSharedAmount);
            
            .println("updated my battery to: ", NewMyBattery, "%, total shared: ", NewSharedAmount, " units");
            
            .wait(500); // wait before next sharing cycle
            !battery_sharing_loop(RobotName);
    } else {
        if (MyBattery <= 30) {
            .println("stopping battery sharing, my battery level is too low to be shared (", MyBattery, "%)");
        } elif (SharedSoFar >= 25) {
            .println("stopping battery sharing, maximum sharing limit reached (", SharedSoFar, " units)");
        }
        !stop_battery_sharing(RobotName);
    }.

// Stop battery sharing
+!stop_battery_sharing(RobotName) <-
    ?battery_shared_amount(TotalShared);
    .println("battery sharing completed with robot ", RobotName, ". Total shared: ", TotalShared, " units");
    
    // Notify the other robot that sharing is complete
    .send(RobotName, tell, battery_sharing_completed(TotalShared));
    
    // Clean up battery sharing state
    -battery_sharing_active(RobotName);
    -battery_shared_amount(TotalShared).

// Handle receiving battery from another robot (for the malfunctioning robot)
+!receive_battery_unit(Amount)[source(Helper)] <-
    ?batteryLevel(CurrentBattery);
    NewBattery = CurrentBattery + Amount;
    
    // Ensure battery doesn't exceed maximum of 100%
    if (NewBattery > 100) {
        NewBattery = 100;
        .println("battery would exceed 100%, capping at maximum level");
    }
    
    -batteryLevel(CurrentBattery);
    +batteryLevel(NewBattery);
    update_battery_level(NewBattery);
    
    .println("received ", Amount, " unit of battery from ", Helper, ". Battery now: ", NewBattery, "%").

// Handle battery sharing completion notification
+battery_sharing_completed(TotalReceived)[source(Helper)] <-
    .println("battery sharing completed. Received total of ", TotalReceived, " units from ", Helper);
    ?batteryLevel(FinalBattery);
    .println("final battery level after sharing: ", FinalBattery, "%");
    
    // Check if we have enough battery to resume operation
    if (FinalBattery > 0 & malfunctioning) {
        -malfunctioning;
        .println("malfunction status cleared. Ready to resume normal operation");
    }.

+remotely_repaired[source(Helper)] <-
    -malfunctioning;
    -remotely_repaired[source(Helper)];
    .println("robot has been remotely repaired by ", Helper, ". Resuming normal operation.").

// Handle battery level queries from other robots
+?batteryLevel(Level)[source(RequesterRobot)] <-
    ?batteryLevel(CurrentLevel);
    .println("robot ", RequesterRobot, " is asking for my battery level: ", CurrentLevel, "%");
    .send(RequesterRobot, tell, batteryLevel(CurrentLevel)).

/* plan to pick up package from truck with better debugging */
+!pickup_package_from_truck <-
    ?truck_position(TX, TY);
    ?current_position(X, Y);
    
    if (X == TX & Y == TY) {
        .send(truck, achieve, request_package);
        .println("message sent to truck, waiting for response...");
        .wait(5000);

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
        .send(delivery_place, achieve, package_delivery_request("PKG001", RobotName));
        .wait(2000);
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
    // there's no need to ask for charging station location if we already know it, since they won't move, so this is asked only once
    if (askedChargingStationLocation(false)) {
        +askedChargingStationLocation(true);
        .broadcast(askOne, whereIsChargingStation(_));
        .wait(5000);
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

+!request_charge(Station, X, Y) : not (knownChargingStation(Station, X, Y) & current_position(X, Y)) <-
    if (carrying_package(false)) {
        ?truck_position(TX, TY);
        !step(TX, TY);
    } else {
        ?delivery_position(DId, DX, DY);
        !step(DX, DY);
    }.

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
    
    ?saved_target(SavedX, SavedY);
    ?saved_carrying_status(WasCarrying);
    .println("restoring previous state - target: (", SavedX, ", ", SavedY, "), was carrying: ", WasCarrying);
    
    // clean up saved state
    -saved_target(SavedX, SavedY);
    -saved_carrying_status(WasCarrying);
    
    // start moving again to previous target
    !step(SavedX, SavedY).

// Handle status request from human agent
+?request_status[source(HumanId)] <-
    ?current_position(X, Y);
    ?batteryLevel(BatteryLevel);
    ?carrying_package(CarryingStatus);
    ?delivery_completed(DeliveryCompleted);
    .my_name(RobotName);
    if (malfunctioning) {
        .send(HumanId, tell, robot_status(RobotName, X, Y, BatteryLevel, CarryingStatus, DeliveryCompleted, true));
    } else {
        .send(HumanId, tell, robot_status(RobotName, X, Y, BatteryLevel, CarryingStatus, DeliveryCompleted, false));
    }.
