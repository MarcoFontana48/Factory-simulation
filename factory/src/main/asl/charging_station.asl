!start.

// Charging station for robots in the factory simulation
// It handles location queries and charging requests from robots
// It initializes the charging station and registers its location
// It charges robots incrementally until their battery is full
+!start <-
    utils.charging_station_init;
    .belief(location(X, Y));
    ?location(X, Y);
    .println("Started at location: (", X, ", ", Y, ")");
    !setup_charging_station(X, Y);
    .println("Registered with environment at location: (", X, ", ", Y, ")").

// Register the charging station's location in the environment
+!setup_charging_station(X,Y)  <-
    .print("registering station at ",X,",",Y);
    register_charging_station(X,Y).

// handle location queries from robots
+?whereIsChargingStation(Requester)[source(Requester)] <-
    .my_name(ChargingStationName);
    ?location(X, Y);
    .println("Charging station ", ChargingStationName, " responding to location query from ", Requester);
    .send(Requester, tell, chargingStationLocation(X, Y)).

// handle charging requests from robots
+chargingRequest(RobotName)[source(RobotName)] <-
    .println("Received charging request from ", RobotName);
    +charging_robot(RobotName);
    .send(RobotName, askOne, batteryLevel(CurrentBattery));
    .wait(100);
    !!start_incremental_charging(RobotName).

// Start incremental charging for the robot, updating its battery level
// until it reaches 100%
+!start_incremental_charging(RobotName) : charging_robot(RobotName) <-
    ?batteryLevel(CurrentBattery)[source(RobotName)];
    !!charge_incrementally(RobotName, CurrentBattery).

// Incrementally charge the robot's battery
// This will keep charging until the battery reaches 100%
+!charge_incrementally(RobotName, CurrentBattery) : charging_robot(RobotName) & CurrentBattery < 100 <-
    .wait(100);
    NewBattery = CurrentBattery + 1;
    .println("Charging ", RobotName, " - battery: ", NewBattery, "%");
    .send(RobotName, tell, updateBatteryLevel(NewBattery));
   
    if (NewBattery >= 100) {
        .println("Charging completed for ", RobotName, " - battery: 100%");
        .send(RobotName, tell, chargingCompleted);
        -charging_robot(RobotName);
        -chargingRequest(RobotName)[source(RobotName)];
    } else {
        !!charge_incrementally(RobotName, NewBattery);
    }.

// handle battery level responses from robots
+batteryLevel(Level)[source(RobotName)] : charging_robot(RobotName) <-
    .println("Received battery level ", Level, "% from ", RobotName).