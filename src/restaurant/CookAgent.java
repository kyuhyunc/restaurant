package restaurant;

import agent.Agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

/**
 * Restaurant customer agent.
 */
public class CookAgent extends Agent {
	private String name;
	Timer timer = new Timer();
		
	//private List<Order> orders = new ArrayList<Order>();
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	
	/**
	 * Constructor for CookrAgent class
	 *
	 * @param name name of the customer
	 */
	public CookAgent(String name){
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	
	// Messages
	// 7: HereIsAnOrder(order);
	public void msgHereIsAnOrder(Order order) {
		print("received an order");
		orders.add(order);
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if (!orders.isEmpty()) {
			synchronized (orders) {
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
				return true; // return true when state is cooking, so that cook can wait
			}
		}
		return false;
	}


	// Actions
	void CookOrder(Order order) {
		print("Start cooking");
		order.DoCooking();
		//stateChanged();
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
					System.out.println("Cook: Done cooking, " + choice.name + " for " + customer.getName());
					state = Order.OrderState.Cooked;
					waiter.getCook().stateChanged();
				}
			},
			(int) (choice.time * choice.cookingTimeMultiplier));//getHungerLevel() * 1000);//how long to wait before running task
		}
		
	}
	
	public static class Food {
		String name;
		
		int time; // for setting timer differently
		double cookingTimeMultiplier = 2.5;
		double eatingTimeMultiplier = 4;
		
		private ImageIcon foodImage;
		
		Food(String name) {
			this.name = name;
			
			if (name == "Stake") {
				time = (int) (1000 * cookingTimeMultiplier);
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/stake.jpg");
			}
			else if (name == "Chicken") {
				time = (int) (800 * cookingTimeMultiplier);
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/chicken.jpg");
			}
			else if (name == "Salad") {
				time = (int) (600 * cookingTimeMultiplier);
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/salad.jpg");
			}
			else if (name == "Pizza") {
				time = (int) (300 * cookingTimeMultiplier);
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/pizza.jpg");
			}
			else {
				time = 0;
			}
		}
		
		public int getEatingTime(String choice) {
			if (name == "Stake") {
				return (int) (1000 * eatingTimeMultiplier);
			}
			else if (name == "Chiken") {
				return (int) (800 * eatingTimeMultiplier);
			}
			else if (name == "Salad") {
				return (int) (600 * eatingTimeMultiplier);
			}
			else if (name == "Pizza") {
				return (int) (300 * eatingTimeMultiplier);
			}
			else {
				return 0;
			}
		}
		
		public ImageIcon getImageIcon() {
			return foodImage;
		}
	}
}

