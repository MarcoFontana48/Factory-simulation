!start.

truck_position(8, 10).         // coordinates of this truck
packages_handed(0).           // counter for handed packages

+!start <-
    ?truck_position(X, Y);
    .println("Started at position (", X, ", ", Y, ")").

// handle when request_package belief is added by robot's tell message
+!request_package[source(Robot)] <-
    .println("Received package request from robot ", Robot);

    ?packages_handed(Count);
    -packages_handed(Count);
    +packages_handed(Count + 1);
    
    PackageId = Count + 1;
    +package_handed(PackageId, Robot);
    
    .println("Handing package ", PackageId, " to robot ", Robot);
    .println("Total packages handed: ", Count + 1);
    
    // send the response that matches what the robot expects
    .send(Robot, tell, package_received).