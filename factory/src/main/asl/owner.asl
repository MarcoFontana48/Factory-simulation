/* Initial goals */

!get(package).   // I want beer
!check_bored. // I want to bother robot if I am bored

/* Plans Library (it's the owner's "know-how") */

+!get(package) // How to get beer?
	: true
	<- .send(robot, achieve, has(owner, package)). // "achieve" -> achievement-goal addition

+has(owner, package) // As soon as I perceive to have beer, drink it
	: true
	<- !drink(package). // sub-goal: if I have beer, drink it

-has(owner, package) // As soon as I perceive NOT to have beer, I want it
	: true
	<- !get(package).

/* Sub-plans */

+!drink(package) // How to drink beer? (if I have it)
	: has(owner, package) // while I have beer...
	<- sip(package); !drink(package). // ...keep drinking (notice EXTERNAL action "sip", defined in "env.HouseEnv")

+!drink(package) // How to drink beer? (if I do NOT have it)
	: not has(owner, package) // if I do NOT have beer...
	<- true. // ...stop drinking (simply drop recursion)
 
+!check_bored
	: true
	<- .random(X); .wait(X*5000+2000); // From time to time, I get bored...
		.send(robot, askOne, time(_), R); // ...so I ask robot about the time (notice request is SYNCHRONOUS)
		.print(R); !!check_bored. // notice "parallel" intention execution

+msg(M)[source(Ag)] // How to handle incoming messages? (notice annotation)
	: true
	<- .print("Message from ", Ag, ": ", M);
		-msg(M). // notice belief deletion: what happens if we drop this?
