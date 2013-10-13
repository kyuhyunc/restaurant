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

import restaurant.MarketAgent.Procure;

/**
 * Restaurant cook agent.
 */
public class CookAgent extends Agent {
	static public int NMARKETS = 0;//a global for the number of markets
	
	private String name;
	Timer timer = new Timer();
		
	//private List<Order> orders = new ArrayList<Order>();
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());

	private List<MarketAgent> markets = Collections.synchronizedList(new ArrayList<MarketAgent> ());
	
	private HostAgent host;
	
	/**
	 * If I need to change foods list, all places I need to modify is here
	 * food (foods) in cook is current food information for cook
	 * food (inventory) in market is current food information for markets
	 */
	private Map<String, Food> foods = new HashMap<String, Food> ();
	private List<String> menu_list = new ArrayList<String> ();
	
	/**
	 * Constructor for CookrAgent class
	 *
	 * @param name name of the customer
	 */
	public CookAgent(String name){
		super();
		this.name = name;
		
		menu_list.addAll(Arrays.asList("Steak","Chicken","Salad","Pizza"));
		
		// setting up the menu
		for(String s : menu_list) {
			foods.put(s, new Food(s));
			foods.get(s).setBatchSize(2);
			foods.get(s).setAmount(2);
		}		
	}
		
	// Messages
	// OutOFFood 2: HereIsAnOrder(order);
	// TheMarketAndCook 0: HereIsAnOrder
	public void msgHereIsAnOrder(Order order) {
		print("received an order");
		orders.add(order);
		stateChanged();
	}
	
	// TheMarketAndCook 3: OrderFulfillment
	public void msgOrderFulfillment(Procure procure) {
		foods.get(procure.getFood()).amount += foods.get(procure.getFood()).batchSize; 
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		synchronized (orders) {
			if (!orders.isEmpty()) {			
				for(int i=0;i<orders.size();i++){
					if(orders.get(i).state == Order.OrderState.Pending) {
						orders.get(i).state = Order.OrderState.Cooking;
						CookOrder(orders.get(i));
						return true;
					}
					else if(orders.get(i).state == Order.OrderState.Cooked) {
						OrderIsReady(orders.get(i));
						return true;
					}
					else if(orders.get(i).state == Order.OrderState.outOfStock) {
						OrderIsOutOfStock(orders.get(i));
						return true;
					}
				}
				//return true; // return true when state is cooking
			}
		}	
		return false;
	}

	// Actions
	private void CookOrder(Order order) {
		if(foods.get(order.choice).amount > 0) {
			Do("Start cooking");
			DoCooking(order);
			foods.get(order.choice).amount --;
			if (foods.get(order.choice).amount == 1 || foods.get(order.choice).amount == 0) {
				Do("There is only " + foods.get(order.choice).amount + " stock left for the food " + order.choice);
				// BuyFood
				BuyFood(order.choice, foods.get(order.choice).batchSize);
			}
		} 
		else {
			// tell waiter there is no food
			Do(order.choice + " is out of stock right now");			
			order.state = Order.OrderState.outOfStock;
			BuyFood(order.choice, foods.get(order.choice).batchSize);
			stateChanged();
		}
	}
	
	private void DoCooking(Order order) {
		Timer timer = new Timer();
		
		class MyTimerTask extends TimerTask {
			Order order;
			
			MyTimerTask(Order order) {
				this.order = order;
			}
			// Override
			public void run() {
				Do("Done cooking, " + order.choice + " for " + order.customer.getName());
				order.state = Order.OrderState.Cooked;
				stateChanged();
			}
		}
		
		timer.schedule(new MyTimerTask(order), (int) (foods.get(order.choice).getCookingTime()));	
	}
	
	
	private void OrderIsReady(Order order) {
		order.waiter.msgOrderIsReady(order);
		Do("Order for " + order.customer + " is ready : " + order.choice);
		orders.remove(order);
	}
	
	private void OrderIsOutOfStock(Order order) {
		order.waiter.msgOrderIsOutOfStock(order);
		Do("Order for " + order.customer + " is out of stock : " + order.choice);
		orders.remove(order);
	}
	
	void BuyFood(String food, int batchSize) {
		boolean marketAvailable = false;
		boolean alreadyOrdered = false;
		
		synchronized(markets) {
			for(MarketAgent m : markets) {
				// return true if the food has already been ordered to the market
				if(m.chkProcureInProcess(food)) {
					alreadyOrdered = true;
					break;
				}
			}

			if(!alreadyOrdered) {
				for(MarketAgent m : markets) {
					// msgBuyFood will return true if the market has a stock for the choice
					if(m.msgBuyFood(new Procure(food, batchSize))) {
						marketAvailable = true;
						Do("Ordered " + food + " to " + m.getName());
						break;
					}
				}
				if(!marketAvailable) {
					Do("There is no market that has a stock for " + food);
				}
			}	
			else {
				Do(food + " has been ordered already");
			}
		}
	}
	
	// Accessors, etc.
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, Food> getFoods() {
		return foods;
	}
	
	public double getPrice(String food) {
		return foods.get(food).price;
	}
	
	public List<String> getMenuList() {
		return menu_list;
	}

	public String toString() {
		return "cook " + getName();
	}
	
	public void setDefaultMarkets() {
		for(int i=0;i<NMARKETS;i++){
			MarketAgent m = new MarketAgent("Market #" + (i+1));
			m.setCook(this);
			m.setHost(host);
			m.setMenuList(menu_list); // this will set up the initial inventory level of the market
			m.setMarketNumber(i+1);
			markets.add(m);
			m.startThread();	
		}
	}
	
	public void addMarketByGui() {
		MarketAgent m = new MarketAgent("Market #" + NMARKETS);
		m.setCook(this);
		m.setHost(host);
		m.setMenuList(menu_list); // this will set up the initial inventory level of the market
		m.setMarketNumber(NMARKETS);
		markets.add(m);
		m.startThread();
	}
	
	public List<MarketAgent> getMarkets() {
		return markets;
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
		
		int amount; // stock level
		int batchSize; // amount of order
		
		int time; // for setting timer differently	
		double price;
		
		double cookingTimeMultiplier = 7;
		double eatingTimeMultiplier = 5;
		
		private ImageIcon foodImage;
		
		Food(String name) {
			this.name = name;
			//amount = 3; // can set initial amount  depending on foods later
			
			if (name == "Steak") {
				time = (int) 800;
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/steak.jpg");
				price = 15.99;
			}
			else if (name == "Chicken") {
				time = (int) 600;
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/chicken.jpg");
				price = 10.99;
			}
			else if (name == "Salad") {
				time = (int) 400;
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/salad.jpg");
				price = 5.99;
			}
			else if (name == "Pizza") {
				time = (int) 500;
				foodImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/pizza.jpg");
				price = 8.99;
			}
			else {
				time = 0;
				foodImage = null;
			}
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
		}
		
		public void setBatchSize(int batchSize) {
			this.batchSize = batchSize;
		}
		
		public double getPrice() {
			return price;
		}
		
		public int getBatchSize() {
			return batchSize;
		}
		
		public int getEatingTime() {
			return (int) (time * eatingTimeMultiplier);
		}
		
		public int getCookingTime() {
			return (int) (time * cookingTimeMultiplier);
		}
				
		public ImageIcon getImageIcon() {
			return foodImage;
		}	
		
		public int getStock() {
			return amount;
		}
	}
}

