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
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;

import restaurant.MarketAgent.Procure;
import restaurant.gui.CookGui;
import restaurant.gui.FoodGui;
import restaurant.interfaces.Cook;

/**
 * Restaurant cook agent.
 */
public class CookAgent extends Agent implements Cook {
	static public int NMARKETS = 3;//a global for the number of markets
	static public int NPLAT = 5;
	static public int NGRILL = 5;
	
	private String name;
	Timer timer = new Timer();
		
	//private List<Order> orders = new ArrayList<Order>();
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private List<Order> pendingOrders = Collections.synchronizedList(new ArrayList<Order>());

	private List<MarketAgent> markets = Collections.synchronizedList(new ArrayList<MarketAgent> ());
	
	private List<Grill> grills = Collections.synchronizedList(new ArrayList<Grill> ());
	private List<Plat> plats = Collections.synchronizedList(new ArrayList<Plat> ());
	
	private List<ProcureContract> contracts = Collections.synchronizedList(new ArrayList<ProcureContract> ());
		
	private HostAgent host;
	private CashierAgent cashier;
	
	private CookGui cookGui;
	
	/**
	 * If I need to change foods list, all places I need to modify is here
	 * food (foods) in cook is current food information for cook
	 * food (inventory) in market is current food information for markets
	 */
	public Map<String, Food> foods = Collections.synchronizedMap(new HashMap<String, Food> ());
	public List<String> menu_list = Collections.synchronizedList(new ArrayList<String> ());
	
	public int defaultBatchSize = 3;
	
	private Semaphore atGrill = new Semaphore(0,true);
	private Semaphore atPlat = new Semaphore(0,true);
	private Semaphore atRefrig = new Semaphore(0,true);
	
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
			foods.get(s).setBatchSize(defaultBatchSize);
			foods.get(s).setAmount(2);
		}		
	}
		
	// Messages
	// OutOFFood 2: HereIsAnOrder(order);
	// TheMarketAndCook 0: HereIsAnOrder
	public void msgHereIsAnOrder(Order order) {
		boolean callWaiterToPickUpOrder = false;
		
		print("received an order");
		for(Order o : orders){
			if(o.state != Order.OrderState.Pending) {
				callWaiterToPickUpOrder = true;
			}
		}
		
		if(callWaiterToPickUpOrder) {
			pendingOrders.add(order);
		}
		else {
			orders.add(order);
		}
		stateChanged();
	}
	
	// TheMarketAndCook 3: OrderFulfillment
	public void msgOrderFulfillment(Procure procure) {
		// better to make another method to do this
		foods.get(procure.getFood()).amount += procure.orderedSize; 
	}
	
	public void msgTellOrderSize(MarketAgent m, String food, int orderedSize) {
		//synchronized(contracts) {
			for(ProcureContract cp : contracts) {
				if(cp.food == food) {
					cp.orderedSize += orderedSize;
					cp.orders.put(m, orderedSize);
					if(cp.orderSize == cp.orderedSize) {
						cp.state = ProcureContract.ContractState.Complete;
					}
					else {
						cp.state = ProcureContract.ContractState.Pending;
					}
					break;
				}
			}
		//}
		stateChanged();
	}
	
	public void msgAtRefrig() {
		atRefrig.release();
	}
	
	public void msgAtGrill() {
		atGrill.release();
	}
	
	public void msgAtPlat() {
		atPlat.release();
	}
	
	public void msgArrivedToPick(Order order) {
	//public void msgArrivedToPick(CustomerAgent customer) {
		/**synchronized (orders) {
			for(Order o : orders) {
				if( o.customer == customer ) {
					o.foodGui.state = FoodGui.State.noCommand;
					o.plat.setUnoccupied();
					orders.remove(o);
					break;
				}
			}
		}*/
		//customer.c.foodGui.state = FoodGui.State.noCommand;
		order.plat.setUnoccupied();
		order.foodGui.state = FoodGui.State.noCommand;
		//orders.remove(order);
		order.state = Order.OrderState.done;
		stateChanged();
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
						orders.get(i).state = Order.OrderState.waitingToBePicked;
						OrderIsReady(orders.get(i));
						return true;
					}
					else if(orders.get(i).state == Order.OrderState.done) {
						orders.remove(orders.get(i));
					}
					else if(orders.get(i).state == Order.OrderState.outOfStock) {
						OrderIsOutOfStock(orders.get(i));
						return true;
					}
				}
				//return true; // return true when state is cooking
			}
		}		
		synchronized (pendingOrders) {
			if(!pendingOrders.isEmpty()) {
				for(Order po : pendingOrders) {
					orders.add(po);
				}		
				pendingOrders.clear();
				return true;
			}
		}
		
		synchronized (contracts) {
			if(!contracts.isEmpty()) {
				for(ProcureContract cp : contracts) {
					if(cp.state == ProcureContract.ContractState.Pending) {
						OrderFood(cp);
						return true;
					}
					else if (cp.state == ProcureContract.ContractState.Complete) {
						contracts.remove(cp);
						return true;						
					}
				}
			}
		}
		
		return false;
	}

	// Actions
	private void CookOrder(Order order) {

		cookGui.DoGoToRefrig();
		try {
			atRefrig.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		if(foods.get(order.choice).amount > 0) {
			Do("Go to grill to Start cooking");
			foods.get(order.choice).amount --;
			DoCooking(order);
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
		synchronized(grills) {
			for(Grill g : grills) {
				if(!g.isOccupied()) {
					order.setGrill(g);
					g.setOccupied();
					FoodGui ingredient = new FoodGui(g.grillNumber, foods.get(order.choice), 1);
					ingredient.setCook(this);
					order.foodGui = ingredient;
					host.gui.animationPanel.addGui(ingredient);
					break;
				}
			}
		}
		
		order.foodGui.state = FoodGui.State.refrigToGill;
		order.foodGui.DoGoToGrill(order.grill.grillNumber);
		
		cookGui.DoGoToGrill();
		try {
			atGrill.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
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
		
		cookGui.DoGoToDefault();		
	}
	
	
	private void OrderIsReady(Order order) {
		
		cookGui.DoGoToGrill();
		try {
			atGrill.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		for(Plat p : plats) {
			if(!p.isOccupied()) {
				order.setPlat(p);
				order.grill.setUnoccupied();
				p.setOccupied();
				order.foodGui.tableNumber = p.platNumber;
				break;
			}
		}
		
		order.foodGui.state = FoodGui.State.grillToPlat;
		order.foodGui.DoGoToPlat(order.plat.platNumber);
		
		cookGui.DoGoToPlat(order.plat.platNumber);
		try {
			atPlat.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
				
		Do("Order for " + order.customer + " is ready : " + order.choice);
		order.waiter.msgOrderIsReady(order);

	}
	
	private void OrderIsOutOfStock(Order order) {
		order.waiter.msgOrderIsOutOfStock(order);
		Do("Order for " + order.customer + " is out of stock : " + order.choice);
		orders.remove(order);
	}
	
	void BuyFood(String food, int batchSize) {
		
		boolean alreadyOrdered = false;
		
		synchronized(contracts) {
			for(ProcureContract pc : contracts) {
				if(pc.food == food) {
					alreadyOrdered = true;
				}
			}
		}
		
		if(!alreadyOrdered) {
			contracts.add(new ProcureContract(food, batchSize));
		}
		else {
			Do(food + " has been ordered already");
		}
	}
	
	public void OrderFood(ProcureContract pc) {
		boolean alreadyOrderedToAllMarkets = true;
		pc.state = ProcureContract.ContractState.Ordering;
		
		MarketAgent market = null;
		
		synchronized(markets) {
			for(MarketAgent m : markets) {
				if(!pc.orders.containsKey(m)) {
					market = m;
					//m.msgBuyFood(pc.food, pc.orderSize-pc.orderedSize);
					alreadyOrderedToAllMarkets = false;
					break;
				}
			}
		}	
		
		if(alreadyOrderedToAllMarkets == true) {
			Do("Cannot order " + (pc.orderSize-pc.orderedSize) + " number of " + pc.food + " (No Stock)");
			contracts.remove(pc);
		}
		else if(market != null) {
			market.msgBuyFood(pc.food, pc.orderSize-pc.orderedSize);
			Do("Order " + pc.food +"(" + (pc.orderSize-pc.orderedSize) + ")"+ " to " + market.getName());
		}
	}

	// Accessors, etc.
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}
	
	public void setGui(CookGui cookGui) {
		this.cookGui = cookGui;
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
			m.setCashier(cashier);
			m.setMarketNumber(i+1);
			markets.add(m);
			m.startThread();	
		}
	}
	
	public void setDefaultPlatsAndGrills() {
		for(int i=0;i<NPLAT;i++){
			Plat p = new Plat(i+1);
			plats.add(p);				
		}
		for(int i=0;i<NGRILL;i++){
			Grill g = new Grill(i+1);
			grills.add(g);				
		}
	}
	
	public void addMarketByGui() {
		MarketAgent m = new MarketAgent("Market #" + NMARKETS);
		m.setCook(this);
		m.setHost(host);
		m.setMenuList(menu_list); // this will set up the initial inventory level of the market
		m.setMarketNumber(NMARKETS);
		m.setCashier(cashier);
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
		FoodGui foodGui;
		
		Grill grill;
		Plat plat;
		
		Order (WaiterAgent waiter, CustomerAgent customer, String choice) {
			this.waiter = waiter;
			this.customer = customer;
			this.choice = choice;
		}
		
		public enum OrderState
		{Pending, Cooking, Cooked, waitingToBePicked, outOfStock, done};
		OrderState state = OrderState.Pending;
		
		public void setFoodGui(int platingNumber, Food food, int cookSize) {
			foodGui = new FoodGui(platingNumber, food, cookSize);
		}
		
		public void setGrill(Grill grill) {
			this.grill = grill;
		}
		
		public void setPlat(Plat plat) {
			this.plat = plat;
		}
		
		public Grill getGrill() {
			return grill;
		}
		
		public Plat getPlat() {
			return plat;
		}
	}
	
	public static class ProcureContract {
		String food;
		int orderSize;
		int orderedSize;
		
		//List<MarketAgent> subContractors = Collections.synchronizedList(new ArrayList<MarketAgent>());
	
		public Map<MarketAgent, Integer> orders = Collections.synchronizedMap(new HashMap<MarketAgent, Integer> ());  
		
		public enum ContractState
		{Pending, Ordering, Complete};
		ContractState state = ContractState.Pending;
		
		ProcureContract(String food, int orderSize) {
			this.food = food;
			this.orderSize = orderSize;
			orderedSize = 0;
		}
	}
	
	public static class Food {
		public String name;
		
		public int amount; // stock level
		public int batchSize; // amount of order
		
		public int time; // for setting timer differently	
		public double price;
		
		double cookingTimeMultiplier = 7;
		double eatingTimeMultiplier = 5;
		
		private ImageIcon foodImage;
		
		public Food(String name) {
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
	
	public class Plat {
		boolean occupied;
		int platNumber;

		Plat(int platNumber) {
			this.platNumber = platNumber;
			occupied = false;
		}

		void setOccupied() {
			occupied = true;
		}

		void setUnoccupied() {
			occupied = false;
		}

		boolean isOccupied() {
			return occupied;
		}

		public String toString() {
			return "table " + platNumber;
		}
	}
	
	public class Grill {
		boolean occupied;
		int grillNumber;

		Grill(int grillNumber) {
			this.grillNumber = grillNumber;
			occupied = false;
		}

		void setOccupied() {
			occupied = true;
		}

		void setUnoccupied() {
			occupied = false;
		}

		boolean isOccupied() {
			return occupied;
		}

		public String toString() {
			return "table " + grillNumber;
		}
	}
	
}

