/* Initial goals */

!get(package).   // I want beer

/* Plans Library (it's the owner's "know-how") */

+!get(package) // How to get beer?
	: true
	<- .send(robot, achieve, has(delivery, package)). // "achieve" -> achievement-goal addition

+has(delivery, package) // As soon as I perceive to have beer, drink it
	: true
	<- !deliverPackage(package). // sub-goal: if I have beer, drink it

-has(delivery, package) // As soon as I perceive NOT to have beer, I want it
	: true
	<- !get(package).

/* Sub-plans */

+!deliverPackage(package) // How to drink beer? (if I have it)
	: has(delivery, package) // while I have beer...
	<- take_item(package); !deliverPackage(package). // ...keep drinking (notice EXTERNAL action "sip", defined in "env.HouseEnv")

+!deliverPackage(package) // How to drink beer? (if I do NOT have it)
	: not has(delivery, package) // if I do NOT have beer...
	<- true. // ...stop drinking (simply drop recursion)
 
+msg(M)[source(Ag)] // How to handle incoming messages? (notice annotation)
	: true
	<- .print("Message from ", Ag, ": ", M);
		-msg(M). // notice belief deletion: what happens if we drop this?
