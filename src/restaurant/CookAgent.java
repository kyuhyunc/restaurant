package restaurant;

import agent.Agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	public Map<String, Food> menu = new HashMap<String, Food> ();
	public List<String> menu_list = new ArrayList<String> ();
	
	/**
	 * Constructor for CookrAgent class
	 *
	 * @param name name of the customer
	 */
	public CookAgent(String name){
		super();
		this.name = name;
		
		menu_list.addAll(Arrays.asList("Stake","Chicken","Salad","Pizza"));
		
		// setting up the menu
		for(String s : menu_list) {
			menu.put(s, new Food(s));
		}		
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
						Do("Order for customer " + orders.get(i).customer + " is ready : " + orders.get(i).choice);
						orders.remove(i);
						return true;
					}
					else if(orders.get(i).state == Order.OrderState.outOfStock) {
						orders.get(i).waiter.msgOrderIsOutOfStock(orders.get(i));
						Do("Order for customer " + orders.get(i).customer + " is out of stock : " + orders.get(i).choice);
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
		if(menu.get(order.choice).amount > 0) {
			DoCooking(order);
			//order.choice.amount --; // decreasing stock by 1
			menu.get(order.choice).amount --;
			if (menu.get(order.choice).amount == 1) {
				Do("There is only 1 stock left for the food " + order.choice);
			}
		} 
		else {
			// tell waiter there is no food
			Do(order.choice + " is out of stock right now");			
			order.state = Order.OrderState.outOfStock;
			stateChanged();
		}
	}
	
	// Accessors, etc.

	public String getName() {
		return name;
	}
	
	public Map<String, Food> getMenu() {
		return menu;
	}

	public String toString() {
		return "cook " + getName();
	}
	
	public void DoCooking(Order order) {
		Timer timer = new Timer();
		final Order o = order;
		
		timer.schedule(new TimerTask() {
			public void run() {
				System.out.println("Cook: Done cooking, " + o.choice + " for " + o.customer.getName());
				o.state = Order.OrderState.Cooked;
				o.waiter.getCook().stateChanged();
			}
		},
		(int) (menu.get(o.choice).time * menu.get(o.choice).cookingTimeMultiplier));//getHungerLevel() * 1000);//how long to wait before running task
	}
	
	public static class Order {
		WaiterAgent waiter;
		CustomerAgent customer;
		String choice;
		
		Order (WaiterAgent waiter, CustomerAgent customer, String choice) {
			this.waiter = waiter;
			this.customer = customer;
			this.choice = choice;
		}
		
		public enum OrderState
		{Pending, Cooking, Cooked, outOfStock};
		OrderState state = OrderState.Pending;
	}
	
	public static class Food {
		String name;
		
		int time; // for setting timer differently
		int amount;
		double cookingTimeMultiplier = 2.5;
		double eatingTimeMultiplier = 4;
		
		private ImageIcon foodImage;
		
		Food(String name) {
			this.name = name;
			amount = 2; // can set initial amount  depending on foods later
			
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

