/* Initial beliefs */

last_order_id(1). // It's supermarket's "knowledge base"

/* Plans library */

+!order(Product,Qtd)[source(Ag)] // How to handle orders?
	: true
	<- ?last_order_id(N); // test-goal
		OrderId = N + 1;
		-+last_order_id(OrderId); // notice ATOMIC belief update
		deliver(Product,Qtd); // notice EXTERNAL action "deliver", defined in "env.HouseEnv"
		.send(Ag, tell, delivered(Product,Qtd,OrderId)). // "tell -> belief addition"
