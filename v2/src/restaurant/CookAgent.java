package restaurant;

import agent.Agent;
import restaurant.WaiterAgent.Food;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Restaurant customer agent.
 */
public class CookAgent extends Agent {
	private String name;
	Timer timer = new Timer();
	// agent correspondents
	
	//    private boolean isHungry = false; //hack for gui
	private List<Order> orders = new ArrayList<Order>();

	
	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public CookAgent(String name){
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	
	// Messages
	public void msgHereIsAnOrder(WaiterAgent waiter, CustomerAgent customer, String choice) {
		print("received an order");
		orders.add(new Order(waiter, customer, choice));
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if (!orders.isEmpty()) {
			for(int i=0;i<orders.size();i++){
				if(orders.get(i).state == Order.OrderState.Pending) {
					orders.get(i).state = Order.OrderState.Cooking;
					CookOrder(orders.get(i));
					return true;
				}
				else if(orders.get(i).state == Order.OrderState.Cooked) {
					orders.get(i).waiter.msgOrderIsReady(orders.get(i));
					Do("Order for customer " + orders.get(i).customer + " is ready : " + orders.get(i).choice.name);
					orders.remove(i);
					return true;
				}
			}
			return true;
		}
		return false;
	}


	// Actions
	void CookOrder(Order order) {
		print("Start cooking");
		order.DoCooking();
		//print("aldskjfdsaf");
		stateChanged();
	}
	
	// Accessors, etc.

	public String getName() {
		return name;
	}

	public String toString() {
		return "cook " + getName();
	}
	
	public static class Order {
		WaiterAgent waiter;
		CustomerAgent customer;
		Food choice;
		Timer timer = new Timer();
		
		Order (WaiterAgent waiter, CustomerAgent customer, String choice) {
			this.waiter = waiter;
			this.customer = customer;
			this.choice = new Food(choice);
		}
		
		public enum OrderState
		{Pending, Cooking, Cooked};
		OrderState state = OrderState.Pending;
		
		void DoCooking() {
			timer.schedule(new TimerTask() {
				public void run() {
					System.out.println("Done cooking, " + choice);
					state = Order.OrderState.Cooked;
				}
			},
			choice.cookingTime);//getHungerLevel() * 1000);//how long to wait before running task
		}
		
	}
	
	/**
	public static class Order {
		WaiterAgent waiter;
		CustomerAgent customer;
		String choice;
		Timer timer = new Timer();
		
		Order (WaiterAgent waiter, CustomerAgent customer, String choice) {
			this.waiter = waiter;
			this.customer = customer;
			this.choice = choice;
		}
		
		public enum OrderState
		{Pending, Cooking, Cooked};
		OrderState state = OrderState.Pending;
		
		void DoCooking() {
			timer.schedule(new TimerTask() {
				public void run() {
					System.out.println("Done cooking, " + choice);
					state = Order.OrderState.Cooked;
				}
			},
			2000);//getHungerLevel() * 1000);//how long to wait before running task
		}
		
	}*/

}

