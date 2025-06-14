!start.

/* Beliefs */
location(10, 15).  // Charging station location coordinates
available(true).   // Charging station availability

/* Plans */

/* Main start-up plan */
+!start <-
    .my_name(ChargingStationName);
    ?location(X, Y);
    .println("Started at location: (", X, ", ", Y, ")").

/* Respond to queries about charging station location, a station replies only if available (if no robots are currently charging).
If multiple robots are going to the same station, because they concurrently asked for its position, they will all go the station,
but the station will not be available to charge all and the others have to wait */
+?whereIsChargingStation(Requester)[source(Requester)] : available(true) <-
    .my_name(ChargingStationName);
    ?location(X, Y);
    .println("Charging station ", ChargingStationName, " responding to location query from ", Requester);
    .send(Requester, tell, chargingStationLocation(X, Y)).

/* Handle charging requests */
+chargingRequest(RobotName)[source(RobotName)] : available(true) <-
    -+available(false);  // Mark as busy
    .println("Received charging request from ", RobotName);
    .println("Starting charging process for ", RobotName);
    +charging_robot(RobotName);
    // Request current battery level from robot
    .send(RobotName, askOne, batteryLevel(CurrentBattery));
    .wait(100);  // Small delay to receive response
    // Start charging as a separate asynchronous goal
    !!start_incremental_charging(RobotName).

/* Start incremental charging process */
+!start_incremental_charging(RobotName) : charging_robot(RobotName) <-
    ?batteryLevel(CurrentBattery)[source(RobotName)];
    .println("Robot ", RobotName, " current battery: ", CurrentBattery, "%");
    !!charge_incrementally(RobotName, CurrentBattery).

/* Incremental charging loop - now with proper yielding */
+!charge_incrementally(RobotName, CurrentBattery) :
    charging_robot(RobotName) & CurrentBattery < 100 <-
    .wait(500);  // Wait 500ms
    NewBattery = CurrentBattery + 1;
    .println("Charging ", RobotName, " - Battery: ", NewBattery, "%");
    // Send updated battery level to robot
    .send(RobotName, tell, updateBatteryLevel(NewBattery));
    if (NewBattery >= 100) {
        .println("Charging completed for ", RobotName, " - Battery: 100%");
        .send(RobotName, tell, chargingCompleted);
        -charging_robot(RobotName);
        -+available(true);  // Mark as available again
    } else {
        !!charge_incrementally(RobotName, NewBattery);
    }.

/* Handle case where battery level is not received */
+!start_incremental_charging(RobotName) :
    charging_robot(RobotName) & not batteryLevel(_)[source(RobotName)] <-
    .println("Could not get battery level from ", RobotName, ". Assuming 0%");
    !!charge_incrementally(RobotName, 0).

/* Handle charging requests when busy - THIS NOW WORKS! */
+chargingRequest(RobotName)[source(RobotName)] : available(false) <-
    .println("Charging station busy, cannot serve ", RobotName, " right now");
    .send(RobotName, tell, chargingStationBusy).

/* Status inquiry */
+?stationStatus(Requester)[source(Requester)] <-
    ?available(Status);
    ?location(X, Y);
    .send(Requester, tell, stationInfo(X, Y, Status)).

/* Handle robot battery level responses */
+batteryLevel(Level)[source(RobotName)] : charging_robot(RobotName) <-
    .println("Received battery level ", Level, "% from ", RobotName).