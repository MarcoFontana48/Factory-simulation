!start.

truck_id("TRUCK1").           // unique identifier for this truck
truck_position(6, 0).         // coordinates of this truck
packages_handed(0).           // counter for handed packages

+!start <-
    ?truck_id(Id);
    ?truck_position(X, Y);
    .println("Truck ", Id, " started at position (", X, ", ", Y, ")").

// Handle when request_package belief is added by robot's tell message
+request_package[source(Robot)] <-
    .println("Received package request from robot ", Robot);
    !hand_package(Robot);
    // Remove the request belief after handling it
    -request_package[source(Robot)].

+!hand_package(Robot) <-
    ?packages_handed(Count);
    -packages_handed(Count);
    +packages_handed(Count + 1);
    
    PackageId = Count + 1;
    +package_handed(PackageId, Robot);
    
    .println("Handing package ", PackageId, " to robot ", Robot);
    .println("Total packages handed: ", Count + 1);
    
    // Send the response that matches what the robot expects
    .send(Robot, tell, package_received).