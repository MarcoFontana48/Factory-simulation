/* Initial goals */

!get(beer).   // I want beer
!check_bored. // I want to bother robot if I am bored

/* Plans Library (it's the owner's "know-how") */

+!get(beer) // How to get beer?
	: true
	<- .send(robot, achieve, has(owner, beer)). // "achieve" -> achievement-goal addition

+has(owner, beer) // As soon as I perceive to have beer, drink it
	: true
	<- !drink(beer). // sub-goal: if I have beer, drink it

-has(owner, beer) // As soon as I perceive NOT to have beer, I want it
	: true
	<- !get(beer).

/* Sub-plans */

+!drink(beer) // How to drink beer? (if I have it)
	: has(owner, beer) // while I have beer...
	<- sip(beer); !drink(beer). // ...keep drinking (notice EXTERNAL action "sip", defined in "env.HouseEnv")

+!drink(beer) // How to drink beer? (if I do NOT have it)
	: not has(owner, beer) // if I do NOT have beer...
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
