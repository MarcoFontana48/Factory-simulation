/* Initial beliefs AND RULES */

available(beer, fridge). // Let's assume there is beer ("env.HouseEnv" takes care of this)
limit(beer, 10). // Let's take care of my owner health

too_much(B) :- // What does "drinking too much" implies?
	.date(YY, MM, DD) & // that in the same day...
	.count(consumed(YY, MM, DD, _, _, _, B), QtdB) & limit(B, Limit) & QtdB > Limit. //...owner consumed more beers than allowed

/* Plans library */

/* Handle owner's orders */
+!has(owner, beer) // How to ensure owner has beer?
	: available(beer, fridge) & not too_much(beer) // if there is beer available and owner is still sober...
	<- !at(robot, fridge); // ...reach the fridge...
		open(fridge); get(beer); close(fridge); // ...get the beer (notice EXTERNAL actions)...
		!at(robot, owner); // ...reach the owner...
		hand_in(beer); // ...give beer to owner...
		?has(owner, beer); // ...ensure owner has beer
		.date(YY, MM, DD);
		.time(HH, NN, SS);
		+consumed(YY, MM, DD, HH, NN, SS, beer). // (track number of beer consumed)

@waitfor
+!has(owner, beer)
	: not available(beer, fridge) // if there is NOT beer available...
	<- .send(supermarket, achieve, order(beer, 5)); // ...order new beer stock...
		!at(robot, fridge). // ...then wait at the fridge (it is well known that beer automagically appear in the fridge)

+!has(owner, beer)
	: too_much(beer) & limit(beer, L) // if owner is no longer sober...
	<- .concat("The Department of Health does not allow me to give you more than ", L,
		" beers a day...I am very sorry about that :/", M);
		.send(owner, tell, msg(M)). // ...warn the owner
		
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
+delivered(beer, _Qtd, _OrderId)[source(supermarket)] // As soon as beer is delivered...
	: true
	<- +available(beer, fridge); // ...track the new stock...
		!has(owner, beer). // ...and re-try to satisfy owners' orders (notice we are stuck at the fridge since plan @waitfor)

/* NOTICE: "stock" and "has" beliefs are "perceptions",
 * thus they are automagically added by Jason runtime,
 * provided that YOU have correctly implemented method "updatePercepts()"
 * in the env.HouseEnv class (extending class jason.environment.Environment)
 */
+stock(beer, 0)
	: available(beer, fridge)
	<- -available(beer, fridge). // no more beer in the fridge

+stock(beer, N)
	: N > 0 & not available(beer, fridge)
	<- -+available(beer, fridge). // notice ATOMIC update of available beers

/* Plans failure handling*/

-!has(_, _)
	: true
	<- .current_intention(I); // notice INTERNAL action to retrieve the execution "context"
		.print("Failed to achieve goal '!has(_, _)'. Current intention is: ", I). // print debug info
