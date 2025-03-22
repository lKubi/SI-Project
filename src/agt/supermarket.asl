last_order_id(1). // initial belief

// plan to achieve the goal "order" for agent Ag
+!order(Product,Qtd)[source(Ag)] : true
  <- ?last_order_id(N);
     OrderId = N + 1;
     -+last_order_id(OrderId);  
     //deliver(Product,Qtd);  
	 .wait(3000);
     .send(Ag, tell, delivered(Product,Qtd,OrderId)).

