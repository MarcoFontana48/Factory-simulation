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

/* multiple plans to handle different possible scenarios */
// Plan 1: Handle redirect to help another robot
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(HelpX, HelpY) & not seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel > 20 <-
    .println("DEBUG: P1");
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    -redirect_to_help(HelpX, HelpY);
    +saved_target_before_help(TargetX, TargetY);
    ?carrying_package(CarryingStatus);
    +saved_carrying_status_before_help(CarryingStatus);
    .println("redirecting step to help location: (", HelpX, ", ", HelpY, ")");
    !step(HelpX, HelpY).

// Plan 1a: Handle redirect to help another robot if seeking a charging station when asked to redirect to the malfunctioning robot (go to the charging station first)
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & (CurrentX \== TargetX | CurrentY \== TargetY) & not helping_robot(_, TargetX, TargetY) & seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel > 0 & BatteryLevel < 20 <-
    .println("DEBUG: P1a");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(500);
    !step(TargetX, TargetY).

// Plan 2: Handle battery depletion (malfunction case)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & batteryLevel(BatteryLevel) & BatteryLevel <= 0 <-
    .println("DEBUG: P2");
    ?current_position(CurrentX, CurrentY);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +malfunctioning;
    !step(TargetX, TargetY).

// Plan 3: Handle low battery (seek charging station)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & batteryLevel(BatteryLevel) & BatteryLevel < 20 & not seekingChargingStation & not charging <-
    .println("DEBUG: P3");
    ?current_position(CurrentX, CurrentY);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    .println("battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
    // save current state before seeking charging station
    +saved_target(TargetX, TargetY);
    ?carrying_package(CarryingStatus);
    +saved_carrying_status(CarryingStatus);
    .println("saved state - target: (", TargetX, ", ", TargetY, "), carrying: ", CarryingStatus);
    -moving_to_target(X, Y);
    !seekChargingStation.

// Plan 4: Handle arrival at regular target
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & CurrentX == TargetX & CurrentY == TargetY & not helping_robot(_, TargetX, TargetY) <-
    .println("DEBUG: P4");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY).

// Plan 5: Handle arrival when helping a robot (adjacent check)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & helping_robot(_, HelpX, HelpY) & TargetX == HelpX & TargetY == HelpY & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - TargetX) * (CurrentX - TargetX) + (CurrentY - TargetY) * (CurrentY - TargetY)) <= 1.5 <-
    .println("DEBUG: P5");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY).

// Plan 6: Continue moving when helping a robot (not yet adjacent)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & helping_robot(_, HelpX, HelpY) & TargetX == HelpX & TargetY == HelpY & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - TargetX) * (CurrentX - TargetX) + (CurrentY - TargetY) * (CurrentY - TargetY)) > 1.5 <-
    .println("DEBUG: P6");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(500);
    !step(TargetX, TargetY).

// Plan 7: Continue moving towards regular target
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & (CurrentX \== TargetX | CurrentY \== TargetY) & not helping_robot(_, TargetX, TargetY) <-
    .println("DEBUG: P7");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(500);
    !step(TargetX, TargetY).

// Plan 8: Continue moving towards charging station when seeking charging station
//+!step(TargetX, TargetY) : not malfunctioning & seekingChargingStation <-
//    .println("DEBUG: P8");
//    ?batteryLevel(BatteryLevel);
//    ?current_position(CurrentX, CurrentY);
//    .println("current position: (", CurrentX, ", ", CurrentY, "), moving to charging station at: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
//    +moving_to_target(TargetX, TargetY);
//    if (not monitoring_active) {
//        !!start_malfunction_monitoring;
//    }
//    // moves one step towards target avoiding obstacles and updating battery level
//    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
//    .wait(500);
//    !step(TargetX, TargetY).

// Plan 9: Handle arrival at charging station
+!step(TargetX, TargetY) : not malfunctioning & seekingChargingStation & current_position(CurrentX, CurrentY) & CurrentX == TargetX & CurrentY == TargetY <-
    .println("DEBUG: P9");
    ?batteryLevel(BatteryLevel);
    .println("arrived at charging station at (", CurrentX, ", ", CurrentY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY, CurrentX, CurrentY).

// Plan 10: Handle redirect while seeking charging station but should prioritize charging
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(_, _) & seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel <= 20 <-
    .println("DEBUG: P10");
    ?batteryLevel(BatteryLevel);
    ?current_position(CurrentX, CurrentY);
    .println("ignoring redirect request - battery too low (", BatteryLevel, "%), continuing to charging station at: (", TargetX, ", ", TargetY, ")");
    if (BatteryLevel == 0) {
        +malfunctioning;
    } else {
        .abolish(moving_to_target(_, _));  
        +moving_to_target(TargetX, TargetY);
        move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
        .wait(500);
        !step(TargetX, TargetY);
    }.

/* handle case when robot is malfunctioning */
+!step(TargetX, TargetY) : malfunctioning <-
    ?current_position(CurrentX, CurrentY);
    if (waiting_to_retry_asking_for_help_after_unsuccessful_repair) {
        .println("cannot move due to malfunctioning at (", CurrentX, ", ", CurrentY, "), will soon retry asking for help after previous unsuccessful repair");
    } else {
        .println("cannot move due to malfunctioning at (", CurrentX, ", ", CurrentY, "), waiting for repair");
        
        // Increment the else counter
        if (rebroadcast_counter(Count)) {
            -rebroadcast_counter(Count);
            +rebroadcast_counter(Count + 1);
            .println("rebroadcast_counter incremented to: ", Count + 1);

            // Check if counter exceeds 240 (2 minutes)
            if (Count + 1 > 240) {
                .println("rebroadcast_counter exceeded 240, broadcasting malfunction");
                .abolish(malfunction_ack(_,_,_)); // ensure not to have previous confirmations before asking for a new one
                ?current_position(X, Y);
                .my_name(RobotName);
                !broadcast_malfunction(X, Y, RobotName);
                -rebroadcast_counter(_); // Reset counter after broadcasting
                +rebroadcast_counter(0);
            }
        } else {
            // Initialize counter if it doesn't exist
            .println("rebroadcast_counter not found, initializing to 1");
            +rebroadcast_counter(1);
        }
    }
    .wait(500);
    !step(TargetX, TargetY).

/* plan to start continuous malfunction monitoring */
+!start_malfunction_monitoring : moving_to_target(_, _) & not monitoring_active <-
    .println("starting malfunction monitoring...");
    +monitoring_active;
    !monitor_malfunction_loop.

+!start_malfunction_monitoring : not (moving_to_target(_, _) & not monitoring_active) <-
    .println("malfunction monitoring already active, skipping new monitoring.").

+!monitor_malfunction_loop : monitoring_active & moving_to_target(_, _) & not malfunctioning <-
    utils.rand_malfunction(Value);  // using custom internal action
    ?current_position(X, Y);
    // slim chance of malfunctioning
    if (Value >= 0.999) {
        .println("random malfunction check currently disabled, skipping it for now...");
        if ((X == DX & Y == DY) | (X == TX & Y == TY)) {
            .println("skipping malfunctioning at delivery/truck position (", X, ", ", Y, ")");
            .wait(500);
            !monitor_malfunction_loop;
        } else {
            .println("malfunction detected at (", X, ", ", Y, ")");
            +malfunctioning;
        }
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
    .abolish(malfunction_ack(_,_,_));  // ensure not to have previous confirmations before asking for a new one
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
+?robotMalfunctioning(RobotName, X, Y) : not malfunctioning & not helping_robot & not seekingChargingStation & not about_to_help_robot(_, _, _) <-
    .my_name(MyName);
    if (RobotName \== MyName) {
        ?current_position(MyX, MyY);
        .println("received malfunction report from robot ", RobotName, " at (", X, ", ", Y, "), my position is (", MyX, ", ", MyY, "), evaluating distance before responding with acknowledgment...");
        // a distance is evaluated to prevent a single robot from trying to help >1 malfunctioning robot simultaneously if all of them are all adjacent w.r.t. each other at the same time
        if (math.sqrt((MyX - X) * (MyX - X) + (MyY - Y) * (MyY - Y)) > 1.5) {
            +about_to_help_robot(RobotName, X, Y);   // this is helpful to prevent the robot from trying to help multiple robots while i.e. charging and has already planned to help another robot
            .println("i'm not adjacent to the malfunctioning robot: responding with acknowledgment...");
            .send(RobotName, tell, malfunction_ack(MyName, MyX, MyY));
        } else {
            .println("i'm adjacent to the malfunctioning robot: the robot is too close to me and i cannot help it, ignoring the report...");
        }
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
+?robotMalfunctioning(RobotName, X, Y) : malfunctioning | helping_robot | seekingChargingStation | about_to_help_robot(_, _, _) <-
    if (malfunctioning) {
        .println("ignoring malfunction report from robot ", RobotName, " at (", X, ", ", Y, ") because I'm already malfunctioning");
    } elif (helping_robot(RobotName, _, _)) {
        .println("ignoring malfunction report from robot ", RobotName, " because I'm already helping another robot");
    } elif (seekingChargingStation) {
        .println("ignoring malfunction report from robot ", RobotName, " because I'm currently seeking a charging station to recharge");
    } elif (about_to_help_robot(_, _, _)) {
        .println("ignoring malfunction report from robot ", RobotName, " because I'm about to go helping another robot");
    } else {
        .println("ignoring malfunction report from robot ", RobotName, " at (", X, ", ", Y, ") because I'm currently busy");
    }.

// Plan for handling arrival at charging station
+!handleArrival(TargetX, TargetY) : seekingChargingStation & knownChargingStation(Station, TargetX, TargetY) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    .println("arrived at charging station location (", CurrentX, ", ", CurrentY, ")");
    -seekingChargingStation;
    ?knownChargingStation(Station, CurrentX, CurrentY);
    !request_charge(Station, CurrentX, CurrentY).

// Plan for handling arrival when helping a robot and close enough
+!handleArrival(TargetX, TargetY) : helping_robot(RobotName, MalfunctionX, MalfunctionY) & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY)) <= 1.5 <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    EuclideanDistanceFromMalfunctioningRobot = math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY));
    .println("arrived near malfunctioning robot ", RobotName, " (adjacent to it)");
    .println("current position: (", CurrentX, ", ", CurrentY, "), robot to help at: (", MalfunctionX, ", ", MalfunctionY, ")");
    .my_name(MyName);
    !handle_robot_help(RobotName, MyName, MalfunctionX, MalfunctionY).

// Sub-plan for handling robot help when it's not ourselves
+!handle_robot_help(RobotName, MyName, MalfunctionX, MalfunctionY) : RobotName \== MyName <-
    !start_battery_sharing(RobotName);
    .println("finished helping robot ", RobotName, ". Returning to previous task.");
    -about_to_help_robot(RobotName, MalfunctionX, MalfunctionY);
    -helping_robot(RobotName, MalfunctionX, MalfunctionY);
    ?saved_target_before_help(SavedX, SavedY);
    !restore_carrying_status;
    -saved_target_before_help(SavedX, SavedY);
    !step(SavedX, SavedY).

// Sub-plan for handling robot help when it's ourselves (error case)
+!handle_robot_help(RobotName, MyName, MalfunctionX, MalfunctionY) : RobotName == MyName <-
    .println("ERROR: cannot help myself! Something went wrong with robot identification.").

// Sub-plan for restoring carrying status when it was saved
+!restore_carrying_status : saved_carrying_status_before_help(WasCarrying) <-
    .println("restoring previous carrying status: ", WasCarrying);
    -saved_carrying_status_before_help(WasCarrying).

// Sub-plan for restoring carrying status when it wasn't saved
+!restore_carrying_status : not saved_carrying_status_before_help(_) <-
    +saved_carrying_status_before_help(false);
    ?saved_carrying_status_before_help(WasCarrying);
    .println("restoring previous state - was carrying: ", WasCarrying);
    -saved_carrying_status_before_help(WasCarrying).

// Plan for handling arrival when helping a robot but not close enough
+!handleArrival(TargetX, TargetY) : helping_robot(RobotName, MalfunctionX, MalfunctionY) & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY)) > 1.5 <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    EuclideanDistanceFromMalfunctioningRobot = math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY));
    .println("not close enough to robot ", RobotName, " (distance: ", EuclideanDistanceFromMalfunctioningRobot, "), i will be continuing to approach it...");
    !step(TargetX, TargetY).

// Plan for handling arrival at truck when not carrying package
+!handleArrival(TargetX, TargetY) : truck_position(TargetX, TargetY) & not carrying_package(true) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    .println("arrived at truck location. Attempting to pick up package...");
    !pickup_package_from_truck.

// Plan for handling arrival at delivery location when carrying package
+!handleArrival(TargetX, TargetY) : delivery_position(_, TargetX, TargetY) & carrying_package(true) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    .println("arrived at delivery location. delivering package...");
    !deliver_package.

// Plan for handling arrival when not carrying package (go to truck)
+!handleArrival(TargetX, TargetY) : carrying_package(false) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    ?truck_position(TX, TY);
    if (about_to_help_robot(_, _, _)) {
        ?about_to_help_robot(RobotName, HelpX, HelpY);
        !redirect_to_help(HelpX, HelpY);
    }
    !step(TX, TY).

// Plan for handling arrival when carrying package (go to delivery)
+!handleArrival(TargetX, TargetY) : carrying_package(true) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(500);
    ?delivery_position(DId, DX, DY);
    if (about_to_help_robot(_, _, _)) {
        ?about_to_help_robot(RobotName, HelpX, HelpY);
        !redirect_to_help(HelpX, HelpY);
    }
    !step(DX, DY).

// Battery sharing plan
+!start_battery_sharing(RobotName) <-
    .println("starting battery sharing with robot ", RobotName);
    ?batteryLevel(MyBattery);
    .println("my current battery level: ", MyBattery, "%");
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
    PotentialNewBattery = CurrentBattery + Amount;
    
    if (PotentialNewBattery > 100) {
        NewBattery = 100;
        .println("battery would exceed 100%, capping at maximum level");
    } else {
        NewBattery = PotentialNewBattery;
    }
    
    -batteryLevel(CurrentBattery);
    +batteryLevel(NewBattery);
    update_battery_level(NewBattery);
    .println("received ", Amount, " unit of battery from ", Helper, ". Battery now: ", NewBattery, "%").

// Handle battery sharing completion notification
+battery_sharing_completed(TotalReceived)[source(Helper)] <-
    -battery_sharing_completed(TotalReceived)[source(Helper)];
    .println("battery sharing completed. Received total of ", TotalReceived, " units from ", Helper);
    ?batteryLevel(FinalBattery);
    .println("final battery level after sharing: ", FinalBattery, "%");
    
    // check if we have enough battery to resume operation. It may happen that the robot has not enough battery to resume operation even after receiving battery from another robot or the other robot has not enough battery to share
    if (FinalBattery > 0 & malfunctioning) {
        -malfunctioning;
        .println("malfunction status cleared. Ready to resume normal operation");
    } else {
        .println("still not enough battery to resume operation, about to ask again for help in 30 seconds...");
        +waiting_to_retry_asking_for_help_after_unsuccessful_repair;
        !!countdown(30, 5);  // start countdown of 30 seconds to retry asking for help
        -waiting_to_retry_asking_for_help_after_unsuccessful_repair;
        .abolish(malfunction_ack(_,_,_));  // ensure not to have previous confirmations before asking for a new one
        ?current_position(X, Y);
        !broadcast_malfunction(X, Y, RobotName);
    }.

+!countdown(T, Step) : T > 0 & Step <= T <-
     .println("countdown: ", T, " seconds remaining");
    .wait(Step * 1000);  // wait for Step seconds
    T1 = T - Step;
    !countdown(T1, Step).

+!countdown(T, Step) : T <= 0 <-
    .println("countdown completed").

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

// Handle status request from agent
+?request_status[source(AgentId)] <-
    .println("received status request from agent ", AgentId);
    ?current_position(X, Y);
    ?batteryLevel(BatteryLevel);
    ?carrying_package(CarryingStatus);
    .my_name(RobotName);
    .date(YY, MM, DD);
    .time(HH, NN, SS);
    
    if (malfunctioning) {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, CarryingStatus, true, YY, MM, DD, HH, NN, SS));
    } else {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, CarryingStatus, false, YY, MM, DD, HH, NN, SS));
    }.
