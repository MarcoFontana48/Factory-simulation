/* Initial beliefs AND RULES */

available(package, packageGen). // Let's assume there is beer ("env.HouseEnv" takes care of this)
limit(package, 10). // Let's take care of my owner health

too_much(B) :- // What does "drinking too much" implies?
	.date(YY, MM, DD) & // that in the same day...
	.count(consumed(YY, MM, DD, _, _, _, B), QtdB) & limit(B, Limit) & QtdB > Limit. //...owner consumed more beers than allowed

/* Plans library */

/* Handle owner's orders */
+!has(deliveryA, package) // How to ensure owner has beer?
	: available(package, packageGen) & not too_much(package) // if there is beer available and owner is still sober...
	<- !at(robot, packageGen); // ...reach the fridge...
		open(packageGen); get(package); close(packageGen); // ...get the beer (notice EXTERNAL actions)...
		!at(robot, deliveryA); // ...reach the owner...
		hand_in(package); // ...give beer to owner...
		?has(deliveryA, package); // ...ensure owner has beer
		.date(YY, MM, DD);
		.time(HH, NN, SS);
		+consumed(YY, MM, DD, HH, NN, SS, package). // (track number of beer consumed)

@waitfor
+!has(deliveryA, package)
	: not available(package, packageGen) // if there is NOT beer available...
	<- .send(supermarket, achieve, order(package, 5)); // ...order new beer stock...
		!at(robot, packageGen). // ...then wait at the fridge (it is well known that beer automagically appear in the fridge)

+!has(deliveryA, package)
	: too_much(package) & limit(package, L) // if owner is no longer sober...
	<- .concat("The Department of Health does not allow me to give you more than ", L,
		" packages a day...I am very sorry about that :/", M);
		.send(deliveryA, tell, msg(M)). // ...warn the owner
		
+?time(T)
	: true
	<- time.check(T). // notice USER-DEFINED INTERNAL action ("package.class" notation)

/* Handle movement */
+!at(robot, P) // if arrived at destination (P = "owner" | "fridge")...
	: at(robot, P)
	<- true. // ...that's all, do nothing, the "original" intention (the "context") can continue

+!at(robot, P) // if NOT arrived at destination (P = "owner" | "fridge")...
	: not at(robot, P)
	<- move_towards(P); !at(robot, P). // ...continue attempting to reach destination

/* Handle beer stock */
+delivered(package, _Qtd, _OrderId)[source(supermarket)] // As soon as beer is delivered...
	: true
	<- +available(package, packageGen); // ...track the new stock...
		!has(deliveryA, package). // ...and re-try to satisfy owners' orders (notice we are stuck at the fridge since plan @waitfor)

/* NOTICE: "stock" and "has" beliefs are "perceptions",
 * thus they are automagically added by Jason runtime,
 * provided that YOU have correctly implemented method "updatePercepts()"
 * in the env.HouseEnv class (extending class jason.environment.Environment)
 */
+stock(package, "")
	: available(package, packageGen)
	<- -available(package, packageGen). // no more beer in the fridge

+stock(package, N)
	: N > 0 & not available(package, packageGen)
	<- -+available(package, packageGen). // notice ATOMIC update of available beers

/* Plans failure handling*/

-!has(_, _)
	: true
	<- .current_intention(I); // notice INTERNAL action to retrieve the execution "context"
		.print("Failed to achieve goal '!has(_, _)'. Current intention is: ", I). // print debug info
