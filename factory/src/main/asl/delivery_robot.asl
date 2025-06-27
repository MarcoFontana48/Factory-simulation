!start.

delivery_completed(false).              // track if delivery is completed

/* initialization plan */
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
    .my_name(RobotName);
    register_dbot(RobotName, BatteryLevel, X, Y);
    !step(TX, TY).

/* 'step' contains the main logic of the robot and recursively calls itself,
    it contains multiple plans to handle different possible scenarios */
// Plan 1: Handle redirect to help another robot
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(HelpX, HelpY) & not seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel > 20 <-
    .println("DEBUG step P1");
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    -redirect_to_help(HelpX, HelpY);
    +saved_target_before_help(TargetX, TargetY);
    if (carrying_package) {
        +saved_carrying_status_before_help(true);
    } else {
        +saved_carrying_status_before_help(false);
    }
    .println("redirecting step to help location: (", HelpX, ", ", HelpY, ")");
    !step(HelpX, HelpY).

// Plan 1a: Handle redirect to help another robot if seeking a charging station when asked to redirect to the malfunctioning robot (go to the charging station first)
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & (CurrentX \== TargetX | CurrentY \== TargetY) & not helping_robot(_, TargetX, TargetY) & seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel > 0 & BatteryLevel < 20 <-
    .println("DEBUG step P1a");
    ?current_position(CurrentX, CurrentY);
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(750);
    !step(TargetX, TargetY).

// Plan 2: Handle battery depletion (malfunction case)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & batteryLevel(BatteryLevel) & BatteryLevel <= 0 <-
    .println("Debug step P2");
    ?current_position(CurrentX, CurrentY);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +malfunctioning;
    !step(TargetX, TargetY).

// Plan 3: Handle low battery (seek charging station)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & batteryLevel(BatteryLevel) & BatteryLevel < 20 & not seekingChargingStation & not charging <-
    .println("Debug step P3");
    ?current_position(CurrentX, CurrentY);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    .println("battery level is low (", BatteryLevel, "%). Going to nearest charging station.");
    // save current state before seeking charging station
    +saved_target(TargetX, TargetY);
    if (carrying_package) {
        +saved_carrying_status(true);
    } else {
        +saved_carrying_status(false);
    }
    .println("saved status - target: (", TargetX, ", ", TargetY, ")");
    -moving_to_target(X, Y);
    !seekChargingStation.

// Plan 4: Handle arrival at regular target
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & CurrentX == TargetX & CurrentY == TargetY & not helping_robot(_, TargetX, TargetY) <-
    .println("Debug step P4");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY).

// Plan 5: Handle arrival when helping a robot (adjacent check)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & helping_robot(_, HelpX, HelpY) & TargetX == HelpX & TargetY == HelpY & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - TargetX) * (CurrentX - TargetX) + (CurrentY - TargetY) * (CurrentY - TargetY)) <= 1.5 <-
    .println("Debug step P5");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY).

// Plan 6: Continue moving when helping a robot (not yet adjacent)
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & helping_robot(_, HelpX, HelpY) & TargetX == HelpX & TargetY == HelpY & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - TargetX) * (CurrentX - TargetX) + (CurrentY - TargetY) * (CurrentY - TargetY)) > 1.5 <-
    .println("Debug step P6");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(750);
    !step(TargetX, TargetY).

// Plan 7: Continue moving towards regular target
+!step(TargetX, TargetY) : not malfunctioning & not redirect_to_help(_, _) & current_position(CurrentX, CurrentY) & (CurrentX \== TargetX | CurrentY \== TargetY) & not helping_robot(_, TargetX, TargetY) <-
    .println("Debug step P7");
    ?batteryLevel(BatteryLevel);
    .println("current position: (", CurrentX, ", ", CurrentY, "), Target: (", TargetX, ", ", TargetY, "), Battery: ", BatteryLevel, "%");
    +moving_to_target(TargetX, TargetY);
    if (not monitoring_active) {
        !!start_malfunction_monitoring;
    }
    // moves one step towards target avoiding obstacles and updating battery level
    move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
    .wait(750);
    !step(TargetX, TargetY).

// Plan 8: Handle arrival at charging station
+!step(TargetX, TargetY) : not malfunctioning & seekingChargingStation & current_position(CurrentX, CurrentY) & CurrentX == TargetX & CurrentY == TargetY <-
    .println("Debug step P8");
    ?batteryLevel(BatteryLevel);
    .println("arrived at charging station at (", CurrentX, ", ", CurrentY, "), Battery: ", BatteryLevel, "%");
    !!stop_malfunction_monitoring;
    !handleArrival(TargetX, TargetY, CurrentX, CurrentY).

// Plan 9: Handle redirect while seeking charging station but should prioritize charging
+!step(TargetX, TargetY) : not malfunctioning & redirect_to_help(_, _) & seekingChargingStation & batteryLevel(BatteryLevel) & BatteryLevel <= 20 <-
    .println("DEBUG step P9");
    ?batteryLevel(BatteryLevel);
    ?current_position(CurrentX, CurrentY);
    .println("ignoring redirect request - battery too low (", BatteryLevel, "%), continuing to charging station at: (", TargetX, ", ", TargetY, ")");
    if (BatteryLevel == 0) {
        +malfunctioning;
        !step(TargetX, TargetY);
    } else {
        .abolish(moving_to_target(_, _));  
        +moving_to_target(TargetX, TargetY);
        move_towards_target(TargetX, TargetY, CurrentX, CurrentY);
        .wait(750);
        !step(TargetX, TargetY);
    }.

/* handle case when robot is malfunctioning, this is the only plan that deals with it */
+!step(TargetX, TargetY) : malfunctioning <-
    ?current_position(CurrentX, CurrentY);
        .println("cannot move due to malfunctioning at (", CurrentX, ", ", CurrentY, "), waiting for repair");
        
        // Increment the else counter
        if (rebroadcast_counter(Count)) {
            -rebroadcast_counter(Count);
            +rebroadcast_counter(Count + 1);
            .println("rebroadcast_counter incremented to: ", Count + 1);

            /* 
            Check if counter exceeds 120 (1 minute), then rebroadcast malfunction (this was made to recover after a 
            situation where all robots are malfunctioning simultaneously, the human has repaired one, and the others
            are still waiting for repair and ask the newly repaired robot to help them; also, a robot may try to help
            this one but its battery is lower than 30, so it cannot help it and this robot may remain malfunctioning 
            for a long time, so this was made to accelerate the recovery process)
            */
            if (Count + 1 > 120) {
                .println("rebroadcast_counter exceeded 120, broadcasting malfunction");
                .abolish(malfunction_ack(_,_,_)); // ensure not to have previous confirmations before asking for a new one
                ?current_position(X, Y);
                .my_name(RobotName);
                !broadcast_malfunction(X, Y, RobotName);
                .abolish(rebroadcast_counter(_)); // reset counter after broadcasting
                +rebroadcast_counter(0);
            }
        } else {
            // Initialize counter if it doesn't exist
            .println("rebroadcast_counter not found, initializing to 1");
            +rebroadcast_counter(1);
        }
    .wait(750);
    !step(TargetX, TargetY).

/* backup plan */
+!step(X, Y) : not malfunctioning <-
    .println("DEBUG: reached an unexpected state at (", X, ", ", Y, ")");
    !reboot_robot.

/* carry a package */
+carrying_package <-
    .println("picked up package, now carrying it");
    ?current_position(X, Y);
    ?delivery_position(DId, DX, DY);
    .println("starting to carry package from (", X, ", ", Y, ") to (", DX, ", ", DY, ")");
    going_towards_delivery_location(true).

-carrying_package <-
    .println("dropped package to delivery location, no longer carrying it...");
    going_towards_delivery_location(false).

/* plan to start continuous malfunction monitoring */
+!start_malfunction_monitoring : moving_to_target(_, _) & not monitoring_active <-
    .println("starting malfunction monitoring...");
    +monitoring_active;
    !monitor_malfunction_loop.

+!start_malfunction_monitoring : not (moving_to_target(_, _) & not monitoring_active) <-
    .println("malfunction monitoring already active, skipping new monitoring.").

/* malfunction monitoring loop that continually checks for random malfunctions */
+!monitor_malfunction_loop : monitoring_active & moving_to_target(_, _) & not malfunctioning <-
    utils.rand_malfunction(Value);  // using custom internal action
    ?current_position(X, Y);
    /* slim chance of malfunctioning, even though it seems low, it is checked frequently (and 
       the number of agents is high), so it still happens quite frequently */
    if (Value >= 0.9999) {
        .println("random malfunction check currently disabled, skipping it for now...");
        ?delivery_position(DId, DX, DY);
        ?truck_position(TX, TY);
        if ((X == DX & Y == DY) | (X == TX & Y == TY)) {
            .println("skipping malfunctioning at delivery/truck position (", X, ", ", Y, ")");
            .wait(750);
            !monitor_malfunction_loop;
        } else {
            .println("malfunction detected at (", X, ", ", Y, ")");
            +malfunctioning;
        }
    } else {
        .wait(750);
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
    waiting_reparations_due_to_malfunction(true);
    ?current_position(X, Y);
    .my_name(RobotName);
    .println("stopped due to malfunction at (", X, ", ", Y, ")");
    .abolish(malfunction_ack(_,_,_));  // ensure not to have previous confirmations before asking for a new one
    !broadcast_malfunction(X, Y, RobotName).

/* broadcast malfunction to other robots */
+!broadcast_malfunction(X, Y, RobotName) : not malfunction_ack(_, _, _) & malfunctioning <-
    .println("broadcasting malfunction at (", X, ", ", Y, ")");
    .broadcast(askOne, robotMalfunctioning(RobotName, X, Y));
    .wait(2000);  // wait N second before retrying if no acknowledgment is received, otherwise it will evaluate the distances from the other robots and choose the closest one
    !broadcast_malfunction(X, Y, RobotName).

+!broadcast_malfunction(ThisRobotX, ThisRobotY, _) : malfunction_ack(_, _, _) & malfunctioning<-
    .findall([ThatRobotName, ThatRobotX, ThatRobotY], malfunction_ack(ThatRobotName, ThatRobotX, ThatRobotY), RobotList);
    .length(RobotList, Count);
    .println("received ", Count, " malfunction acknowledgments from other robots: ", RobotList);
    /* it is possible to comment out the line below, to let all robots that 
       responded with an acknowledgment to help this one after they have
       completed their current task. Leaving it on means that those robots 
       will keep doing this, but also one robot (the closest one to this)
       will be chosen to immediately go to this one, not waiting for task
       completition, requiring it to adapt to this new situation. By default,
       this line is left uncommented to see this more complex behaviour. */
    !find_closest_robot_to_help(RobotList, ThisRobotX, ThisRobotY).

// every other possible case (where the robot is not malfunctioning)
+!broadcast_malfunction(_, _, _) <-
    .println("no longer required to broadcast malfunction, resuming normal operation.").

/* computes who the closest robot is to help */
+!find_closest_robot_to_help(RobotList, ThisRobotX, ThisRobotY) <-
    compute_closest_robot(RobotList, ThisRobotX, ThisRobotY);   //the list is composed of: [["name1", X, Y], ["name2", X2, Y2], ...]
    ?closestRobot(ClosestName, ThatRobotX, ThatRobotY);
    .println("closest robot to me, is ", ClosestName, " at (", ThatRobotX, ", ", ThatRobotY, "), sending help request to it to speed up the repair process...");
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

/* confirming acknowledgment of malfunction report from another robot */
+malfunction_ack(ThatRobotName, ThatRobotX, ThatRobotY)[source(ThatRobotName)] <-
    .println("acknowledged malfunction report from robot ", ThatRobotName, " at (", ThatRobotX, ", ", ThatRobotY, ")").

/* redirect to help another robot */
+!redirect_to_help(R, X, Y) <-
    .println("the robot ", R, " is malfunctioning, redirecting to it at (", X, ", ", Y, ") in order to repair and recharge it...");

    // save current state before going to help
    ?current_position(CurrentX, CurrentY);
    
    // check if we're currently moving to a target and save that information
    if (moving_to_target(TargetX, TargetY)) {
        +saved_target_before_help(TargetX, TargetY);
        .println("saved current target: (", TargetX, ", ", TargetY, ")");
        -moving_to_target(TargetX, TargetY);
    } else {
        // if not moving to a specific target, save current task context
        if (carrying_package) {
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
    if (carrying_package) {
        +saved_carrying_status_before_help(true);
    } else {
        +saved_carrying_status_before_help(false);
    }
    
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

/* currently seeking a charging station */
+seekingChargingStation <-
    ?batteryLevel(Level);
    ?current_position(X, Y);
    .println("seeking charging station starting from: (", X, ", ", Y, "), with battery level: ", Level, "%");
    going_towards_charging_station(true).

-seekingChargingStation <-
    ?batteryLevel(Level);
    ?current_position(X, Y);
    .println("seeking charging station completed at: (", X, ", ", Y, "), with battery level: ", Level, "%");
    going_towards_charging_station(false).

// Sub-plan for handling robot help when it's not ourselves
+!handle_robot_help(RobotName, MyName, MalfunctionX, MalfunctionY) : RobotName \== MyName <-
    // check if robot is still malfunctioning
    .send(RobotName, askOne, malfunctioning, Reply);
    // check position and malfunction status
    !process_malfunction_check(RobotName, Reply, MalfunctionX, MalfunctionY).

// process the reply from the malfunctioning check - robot is still malfunctioning
+!process_malfunction_check(RobotName, malfunctioning, MalfunctionX, MalfunctionY) <-
    // now check if the robot is still at the expected position
    .send(RobotName, askOne, current_position(X, Y), PositionReply);
    !verify_position_and_help(RobotName, PositionReply, MalfunctionX, MalfunctionY).

// Robot is at the expected position and still malfunctioning - proceed with help
+!verify_position_and_help(RobotName, current_position(X, Y), MalfunctionX, MalfunctionY) : X == MalfunctionX & Y == MalfunctionY <-
    .println("Robot ", RobotName, " is still malfunctioning at position (", X, ",", Y, "). Starting battery sharing.");
    !start_battery_sharing(RobotName);
    .println("Finished helping robot ", RobotName, ". Returning to previous task.");
    -about_to_help_robot(RobotName, MalfunctionX, MalfunctionY);
    .abolish(helping_robot(_, _, _)); // remove the helping robot belief
    ?saved_target_before_help(SavedX, SavedY);
    !restore_carrying_status;
    -saved_target_before_help(SavedX, SavedY);
    !step(SavedX, SavedY).

// Robot has moved from the expected position - no help needed
+!verify_position_and_help(RobotName, current_position(X, Y), MalfunctionX, MalfunctionY) : (X \== MalfunctionX | Y \== MalfunctionY) <-
    .println("Robot ", RobotName, " has moved from position (", MalfunctionX, ",", MalfunctionY, ") to (", X, ",", Y, "). No help needed.");
    -about_to_help_robot(RobotName, MalfunctionX, MalfunctionY);
    .abolish(helping_robot(_, _, _)); // remove the helping robot belief
    ?saved_target_before_help(SavedX, SavedY);
    !restore_carrying_status;
    -saved_target_before_help(SavedX, SavedY);
    !step(SavedX, SavedY).

// Handle case when position query fails (robot might be completely down)
+!verify_position_and_help(RobotName, PositionReply, MalfunctionX, MalfunctionY) : PositionReply \== current_position(_, _) <-
    .println("Could not get position from robot ", RobotName, ". Assuming it's still at malfunction location and proceeding with help.");
    !start_battery_sharing(RobotName);
    .println("Finished helping robot ", RobotName, ". Returning to previous task.");
    -about_to_help_robot(RobotName, MalfunctionX, MalfunctionY);
    .abolish(helping_robot(_, _, _)); // remove the helping robot belief
    ?saved_target_before_help(SavedX, SavedY);
    !restore_carrying_status;
    -saved_target_before_help(SavedX, SavedY);
    !step(SavedX, SavedY).

// Handle case when robot is no longer malfunctioning even though we were about to help it
+!process_malfunction_check(RobotName, Reply, MalfunctionX, MalfunctionY) : Reply \== malfunctioning <-
    .println("Robot ", RobotName, " is no longer malfunctioning. No help needed.");
    -about_to_help_robot(RobotName, MalfunctionX, MalfunctionY);
    .abolish(helping_robot(_, _, _)); // remove the helping robot belief
    ?saved_target_before_help(SavedX, SavedY);
    !restore_carrying_status;
    -saved_target_before_help(SavedX, SavedY);
    !step(SavedX, SavedY).

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

/* battery sharing with another robot */
+battery_sharing_active(RobotName) <-
    .println("battery sharing is now active with robot ", RobotName);
    ?batteryLevel(MyLevel);
    .println("my current battery level before sharing: ", MyLevel, "%");
    recharging_robot_after_malfunction(true).

-battery_sharing_active(RobotName) <-
    .println("battery sharing with robot ", RobotName, " is no longer active");
    ?batteryLevel(MyLevel);
    .println("my current battery level after sharing: ", MyLevel, "%");
    recharging_robot_after_malfunction(false).

// Plan for handling arrival at charging station
+!handleArrival(TargetX, TargetY) : seekingChargingStation <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    .println("arrived at charging station location (", CurrentX, ", ", CurrentY, ")");
    -seekingChargingStation;
    ?knownChargingStation(Station, TargetX, TargetY);
    !request_charge(Station, TargetX, TargetY).

// Plan for handling arrival when helping a robot and close enough
+!handleArrival(TargetX, TargetY) : helping_robot(RobotName, MalfunctionX, MalfunctionY) & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY)) <= 1.5 <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    EuclideanDistanceFromMalfunctioningRobot = math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY));
    if (.count(about_to_help_robot(_, MalfunctionX, MalfunctionY)) == 0) {
        .println("arrived at target, but it's not present: redirecting to help robot ", RobotName, " at (", MalfunctionX, ", ", MalfunctionY, ")");
        .abolish(helping_robot(_, _, _)); // remove the helping robot belief
        ?saved_target_before_help(SavedX, SavedY);
        !restore_carrying_status;
        -saved_target_before_help(SavedX, SavedY);
        !step(SavedX, SavedY)
    } else {
        .println("not about to help any robot, proceeding with help for robot ", RobotName);
    }
    .println("arrived near malfunctioning robot ", RobotName, " (adjacent to it)");
    .println("current position: (", CurrentX, ", ", CurrentY, "), robot to help at: (", MalfunctionX, ", ", MalfunctionY, ")");
    .my_name(MyName);
    !handle_robot_help(RobotName, MyName, MalfunctionX, MalfunctionY).

// Plan for handling arrival when helping a robot but not close enough
+!handleArrival(TargetX, TargetY) : helping_robot(RobotName, MalfunctionX, MalfunctionY) & current_position(CurrentX, CurrentY) & math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY)) > 1.5 <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    EuclideanDistanceFromMalfunctioningRobot = math.sqrt((CurrentX - MalfunctionX) * (CurrentX - MalfunctionX) + (CurrentY - MalfunctionY) * (CurrentY - MalfunctionY));
    .println("not close enough to robot ", RobotName, " (distance: ", EuclideanDistanceFromMalfunctioningRobot, "), i will be continuing to approach it...");
    !step(TargetX, TargetY).

// Plan for handling arrival at truck when not carrying package
+!handleArrival(TargetX, TargetY) : truck_position(TargetX, TargetY) & not carrying_package <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    if (about_to_help_robot(_, _, _)) {
        ?about_to_help_robot(RobotName, HelpX, HelpY);
        !redirect_to_help(RobotName, HelpX, HelpY);
    }
    .println("arrived at truck location. Attempting to pick up package...");
    !pickup_package_from_truck.

// Plan for handling arrival at delivery location when carrying package
+!handleArrival(TargetX, TargetY) : delivery_position(_, TargetX, TargetY) & carrying_package <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    if (about_to_help_robot(_, _, _)) {
        ?about_to_help_robot(RobotName, HelpX, HelpY);
        !redirect_to_help(RobotName, HelpX, HelpY);
    }
    .println("arrived at delivery location. delivering package...");
    !deliver_package.

/* backup plan for handling arrival at a target when not carrying a package */
+!handleArrival(TargetX, TargetY) <-
    .println("reached destination (", TargetX, ", ", TargetY, ")");
    -moving_to_target(TargetX, TargetY);
    ?current_position(CurrentX, CurrentY);
    .wait(750);
    .println("DEBUG: reached target (", TargetX, ", ", TargetY, ") but not carrying a package and not at truck or delivery location.");
    !reboot_robot.

// Battery sharing plan
+!start_battery_sharing(RobotName) <-
    .println("starting battery sharing with robot ", RobotName);
    ?batteryLevel(MyBattery);
    .println("my current battery level: ", MyBattery, "%");
    +battery_sharing_active(RobotName);
    +battery_shared_amount(0);
    !battery_sharing_loop(RobotName).

// Battery sharing loop to keep charging until conditions are met
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
            
            .wait(100); // wait before next sharing cycle
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

    if (not receiving_battery(Helper)) {
        +receiving_battery(Helper);
        .println("started receiving battery from ", Helper);
    }

    update_battery_level(NewBattery);
    .println("received ", Amount, " unit of battery from ", Helper, ". Battery now: ", NewBattery, "%").

// Handle battery sharing completion notification
+battery_sharing_completed(TotalReceived)[source(Helper)] <-
    -battery_sharing_completed(TotalReceived)[source(Helper)];
    -receiving_battery(Helper);
    .println("battery sharing completed. Received total of ", TotalReceived, " units from ", Helper);
    ?batteryLevel(FinalBattery);
    .println("final battery level after sharing: ", FinalBattery, "%");
    
    if (.count(receiving_battery(_), N) & N > 0) {
        .println("still receiving battery from other robot(s), waiting for more units before restoring normal operation...");
    // check if we have enough battery to resume operation. It may happen that the robot has not enough battery to resume operation even after receiving battery from another robot or the other robot has not enough battery to share
    } elif (FinalBattery > 0 & malfunctioning) { 
        -malfunctioning;
        .println("malfunction status cleared. Ready to resume normal operation");
    // this may happen when the charger has not enough battery to fully recharge this robot (it recharges only if its battery is higher than 30%, if it's lower than 30 and higher than 20 it tries to charge but only shares 0 battery, so this robot does not receive any battery and has to wait for the human to help it) 
    } else {
        .println("still not enough battery to resume operation... waiting for further assistance");
    }.

/* in case all robots fail simultaneously, the human operator can remotely repair the robot and reboot it */
+remotely_repaired[source(Helper)] <-
    -remotely_repaired[source(Helper)];
    -receiving_battery(Helper);
    -malfunctioning;
    !reboot_robot;
    .println("robot has been remotely repaired by ", Helper, ". Resuming normal operation.").

-malfunctioning <-
    waiting_reparations_due_to_malfunction(false).

/* handle helping another robot */
+helping_robot(RobotName, X, Y) <-
    .println("helping robot ", RobotName, " at (", X, ", ", Y, ")");
    ?current_position(MyX, MyY);
    .println("moving from (", MyX, ", ", MyY, ") to help location");
    moving_to_robot_to_repair_it(true).

-helping_robot(RobotName, X, Y) <-
    .println("no longer helping robot ", RobotName, " at (", X, ", ", Y, ")");
    ?batteryLevel(Level);
    .println("my battery level after helping: ", Level, "%");
    moving_to_robot_to_repair_it(false).

/* 
Plan to reboot the robot when requested by the human operator or when it has been remotely repaired
this is useful to reset all states related to malfunctioning and resume normal operation as if it 
were starting the simulation from scratch
*/
+!reboot_robot <-
    .println("requested reboot...");
    // reset all states related to malfunctioning
    .abolish(battery_sharing_active(_));
    .abolish(battery_shared_amount(_));
    .abolish(about_to_help_robot(_, _, _));
    .abolish(helping_robot(_, _, _));
    .abolish(seekingChargingStation);
    .abolish(saved_target_before_help(_, _));
    -saved_carrying_status_before_help(_);
    -delivery_completed(false);              // track if delivery is completed

    .drop_all_desires;
    .drop_all_intentions;

    ?current_position(X, Y);
    if (not carrying_package) {
        ?truck_position(TX, TY);
        .println("robot rebooted successfully. Currently not carrying package, so i'll start moving towards the truck...");
        !step(TX, TY);
    } else {
        ?delivery_position(DId, DX, DY);
        .println("robot rebooted successfully. Currently carrying package, so i'll start moving towards the delivery location...");
        !step(DX, DY);
    }.

// handle battery level queries from other robots
+?batteryLevel(Level)[source(RequesterRobot)] <-
    ?batteryLevel(CurrentLevel);
    .println("robot ", RequesterRobot, " is asking for my battery level: ", CurrentLevel, "%");
    .send(RequesterRobot, tell, batteryLevel(CurrentLevel)).

/* plan to pick up package from truck */
+!pickup_package_from_truck <-
    ?truck_position(TX, TY);
    ?current_position(X, Y);
    
    if (X == TX & Y == TY) {
        .send(truck, achieve, request_package);
        .println("message sent to truck, waiting for response...");
        .wait(750);

        if (package_received) {
            .println("received package_received message");
            -package_received;
            +carrying_package;
            .println("package successfully picked up from truck!");
            
            ?delivery_position(DId, DX, DY);
            .println("now heading to delivery location (", DX, ", ", DY, ")");
            !step(DX, DY);
        } else {
            .println("DEBUG: did not receive package_received message from truck");
        }
    }.

+package_received[source(truck)] <-
    +package_received.

/* plan to deliver package to delivery location */
+!deliver_package <-
    ?delivery_position(DId, DX, DY);
    ?current_position(X, Y);
    
    if (X == DX & Y == DY & carrying_package) {
        .my_name(RobotName);
        .println("at delivery location - requesting package delivery");
        .send(delivery_place, achieve, package_delivery_request("A", RobotName));
        .wait(750);
        -carrying_package;
        +delivery_completed(true);
        .println("package delivery process completed");
        
        // return to truck after successful delivery for next package
        ?truck_position(TX, TY);
        .println("delivery completed! Now returning to truck at (", TX, ", ", TY, ") for next package");
        !step(TX, TY);
    } else {
        .println("DEBUG: cannot deliver package - not at delivery location or not carrying package");
        !reboot_robot;
    }.

/* handle delivery confirmation */
+delivery_confirmed(PackageId)[source(DeliveryPlace)] <-
    .println("delivery confirmed by delivery place for package: ", PackageId).

/* seek charging station, computing closest one */
+!seekChargingStation <-
    +seekingChargingStation;
    .println("searching for charging stations...");
    .broadcast(askOne, whereIsChargingStation(_));
    .wait(750);
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

/* handle known charging station locations */
+chargingStationLocation(X, Y)[source(Station)] <-
    -chargingStationLocation(X, Y)[source(Station)];
    +knownChargingStation(Station, X, Y);
    .println("found charging station ", Station, " at (", X, ", ", Y, ")").

/* move towards the closest charging station */
+!move_towards_charging_station(Station, X, Y) <-
    .println("moving towards charging station ", Station, " at (", X, ", ", Y, ")");
    !step(X, Y).

/* request charging from a known charging station */
+!request_charge(Station, X, Y) : knownChargingStation(Station, X, Y) & current_position(X, Y) <-
    .my_name(RobotName);
    .println("Requesting charging from station ", Station);
    .send(Station, tell, chargingRequest(RobotName)).

+!request_charge(Station, X, Y) : not (knownChargingStation(Station, X, Y) & current_position(X, Y)) <-
    if (not carrying_package) {
        ?truck_position(TX, TY);
        !step(TX, TY);
    } else {
        ?delivery_position(DId, DX, DY);
        !step(DX, DY);
    }.

/* handle battery level requests */
+?batteryLevel(Level)[source(Station)] <-
    .println("charging station ", Station, " is asking for battery level");
    ?batteryLevel(CurrentLevel);
    .println("sending battery level ", CurrentLevel, "% to ", Station);
    .send(Station, tell, batteryLevel(CurrentLevel)).

/* handle battery level updates from charging station */
+updateBatteryLevel(NewLevel)[source(Station)] <-
    -updateBatteryLevel(NewLevel)[source(Station)];
    +charging;
    -batteryLevel(_);
    +batteryLevel(NewLevel);
    ?batteryLevel(CurrentLevel);
    update_battery_level(NewLevel);
    .println("battery updated to: ", NewLevel, "%").

/* handle charging completion from charging station */
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

/* handle charging state */
+charging <-
    .println("currently starting to charge...");
    ?batteryLevel(Level);
    .println("charging started at ", Level, "% battery");
    battery_charging_update(true).

-charging <-
    .println("no longer charging");
    ?batteryLevel(Level);
    .println("charging completed - battery level: ", Level, "%");
    battery_charging_update(false).

// Handle status request from agents
+?request_status[source(AgentId)] <-
    .println("received status request from agent ", AgentId);
    ?current_position(X, Y);
    ?batteryLevel(BatteryLevel);
    .my_name(RobotName);
    .date(YY, MM, DD);
    .time(HH, NN, SS);
    
    if (malfunctioning & carrying_package) {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, true, true, YY, MM, DD, HH, NN, SS));
    } elif (malfunctioning & not carrying_package) {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, false, true, YY, MM, DD, HH, NN, SS));
    } elif (not malfunctioning & carrying_package) {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, true, false, YY, MM, DD, HH, NN, SS));
    } else {
        .send(AgentId, tell, robot_status(RobotName, X, Y, BatteryLevel, false, false, YY, MM, DD, HH, NN, SS));
    }.
