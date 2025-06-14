/* Initial beliefs AND RULES */

available(package, truck). // Let's assume there is beer ("env.HouseEnv" takes care of this)

/* Plans library */

/* Handle owner's orders */
+!has(deliveryA, package) // How to ensure owner has beer?
	: available(package, truck)
	<- !at(robot, truck); // ...reach the fridge...
		open(truck); get(package); close(truck); // ...get the beer (notice EXTERNAL actions)...
		!at(robot, deliveryA); // ...reach the owner...
		hand_in(package); // ...give beer to owner...
		?has(deliveryA, package); // ...ensure owner has beer
		.date(YY, MM, DD);
		.time(HH, NN, SS);
		+consumed(YY, MM, DD, HH, NN, SS, package). // (track number of beer consumed)

@waitfor
+!has(deliveryA, package)
	: not available(package, truck) // if there is NOT beer available...
	<- .send(truck, achieve, order(package, 5)); // ...order new beer stock...
		!at(robot, truck). // ...then wait at the fridge (it is well known that beer automagically appear in the fridge)

+!has(deliveryA, package)
	<- .concat("The Department of Health does not allow me to give you more than ", L,
		" packages a day...I am very sorry about that :/", M);
		.send(deliveryA, tell, msg(M)). // ...warn the owner
		
/* Handle movement */
+!at(robot, P) // if arrived at destination (P = "owner" | "fridge")...
	: at(robot, P)
	<- true. // ...that's all, do nothing, the "original" intention (the "context") can continue

+!at(robot, P) // if NOT arrived at destination (P = "owner" | "fridge")...
	: not at(robot, P)
	<- move_towards(P); !at(robot, P). // ...continue attempting to reach destination

/* Handle beer stock */
+delivered(package, _Qtd, _OrderId)[source(truck)] // As soon as beer is delivered...
	: true
	<- +available(package, truck); // ...track the new stock...
		!has(deliveryA, package). // ...and re-try to satisfy owners' orders (notice we are stuck at the fridge since plan @waitfor)

/* NOTICE: "stock" and "has" beliefs are "perceptions",
 * thus they are automagically added by Jason runtime,
 * provided that YOU have correctly implemented method "updatePercepts()"
 * in the env.HouseEnv class (extending class jason.environment.Environment)
 */
+stock(package, "")
	: available(package, truck)
	<- -available(package, truck). // no more beer in the fridge

+stock(package, N)
	: N > 0 & not available(package, truck)
	<- -+available(package, truck). // notice ATOMIC update of available beers

/* Plans failure handling*/

-!has(_, _)
	: true
	<- .current_intention(I); // notice INTERNAL action to retrieve the execution "context"
		.print("Failed to achieve goal '!has(_, _)'. Current intention is: ", I). // print debug info
