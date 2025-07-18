!start.

delivery_id("A").               // unique identifier for this delivery location
delivery_position(4, 2).       // coordinates of this delivery place
packages_received(0).           // counter for received packages

// Initialize the delivery place
+!start <-
    ?delivery_id(Id);
    ?delivery_position(X, Y);
    .println("Delivery place ", Id, " started at position (", X, ", ", Y, ")").

// handle when package_delivery_request belief is added by robot's tell message
+!package_delivery_request(PackageId, RobotId)[source(Robot)] <-
    .println("Received package delivery request from robot ", RobotId, " for package ", PackageId);

    ?packages_received(Count);
    -packages_received(Count);
    +packages_received(Count + 1);
    +package_received(PackageId, RobotId);
    
    .println("Package ", PackageId, " successfully received from robot ", RobotId);
    .println("Total packages received: ", Count + 1);
    
    .send(Robot, tell, delivery_confirmed(PackageId)).