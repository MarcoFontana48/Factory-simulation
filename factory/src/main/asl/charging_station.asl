!start.

+!start <-
    utils.charging_station_init;
    .belief(location(X, Y));
    ?location(X, Y);
    .println("Started at location: (", X, ", ", Y, ")");
    !setup_charging_station(X, Y);
    .println("Registered with environment at location: (", X, ", ", Y, ")").

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

+!start_incremental_charging(RobotName) : charging_robot(RobotName) <-
    ?batteryLevel(CurrentBattery)[source(RobotName)];
    !!charge_incrementally(RobotName, CurrentBattery).

+!charge_incrementally(RobotName, CurrentBattery) : charging_robot(RobotName) & CurrentBattery < 100 <-
    .wait(500);
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