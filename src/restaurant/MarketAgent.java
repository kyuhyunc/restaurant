package restaurant;

import agent.Agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import restaurant.CookAgent.Food;
import restaurant.gui.FoodGui;

/**
 * Restaurant market agent.
 */
public class MarketAgent extends Agent {	
	private String name;
	private int marketNumber;
	Timer timer = new Timer();
		
	//private List<Order> orders = new ArrayList<Order>();
	private List<Procure> procures = Collections.synchronizedList(new ArrayList<Procure>());
	
	private Map<String, Food> inventory = new HashMap<String, Food> ();
	private List<String> food_list;
	
	private CookAgent cook;
	private HostAgent host;
	
	private Semaphore atCook = new Semaphore(0,true);
	
	FoodGui deliveryFood;
	
	private int deliveryTime = 4000;
		
	/**
	 * Constructor for MarketAgent class
	 *
	 * @param name name of the customer
	 */
	public MarketAgent(String name){
		super();
		this.name = name;
	}
	

	/**
	 * hack to establish connection to Host agent.
	 */
	
	// Messages

	public boolean msgBuyFood(Procure procure) {
		// check availability for the procure order
		if(inventory.get(procure.food).amount < procure.batchSize) {
			return false;
		}
		else {
			print("received an procure order from cook");
			procures.add(procure);
			// minus stock level in advance
			inventory.get(procure.food).amount -= procure.batchSize;
			//print("stock level : " + inventory.get(procure.food).amount);
			stateChanged();
			return true;
		}
	}
	
	public void msgDeliveredToCook() {
		atCook.release();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		synchronized (procures) {
			if (!procures.isEmpty()) {
				for(int i=0;i<procures.size();i++){
					if(procures.get(i).state == Procure.ProcureState.Pending) {
						procures.get(i).state = Procure.ProcureState.Delivering;
						DeliverOrder(procures.get(i));
						return true;
					}
					else if(procures.get(i).state == Procure.ProcureState.Done) {
						cook.msgOrderFulfillment(procures.get(i));
						Do("Done delivering " + procures.get(i).food + " to the cook");
						procures.remove(i);
						return true;
					}
				}
				//return true; // return true when state is Delivering, so that market can wait
			}
		}
		return false; // return false when there is no procure orders
	}

	// Actions
	void DeliverOrder(Procure procure) {
		print("Start Delivering");
		
		DoDeliver(procure);
		//inventory.get(procure.food).amount -= procure.batchSize;
		//print("stock level : " + inventory.get(procure.food).amount);
	}
	
	public void DoDeliver(Procure procure) {
		Timer timer = new Timer();
		final Procure p = procure;
		
		deliveryFood = new FoodGui(marketNumber, inventory.get(procure.food));
		host.gui.animationPanel.addGui(deliveryFood);
			
		timer.schedule(new TimerTask() {
			public void run() {
				//p.state = Procure.ProcureState.Done;
				//stateChanged();
				DoDeliverGui(p);
			}
		},
		(int) deliveryTime); // delivery will be done in deliveryTime		
	}
	
	public void DoDeliverGui(Procure p) {
		deliveryFood.state = FoodGui.State.procurement;
		deliveryFood.DoGoToCook(this);
		try {
			atCook.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		p.state = Procure.ProcureState.Done;
		stateChanged();		
	}
		
	// Accessors, etc.
	public void setCook(CookAgent cook) {
		this.cook = cook; 
	}	
	
	public void setHost(HostAgent host) {
		this.host = host; 
	}	
	
	public void setMenuList(List<String> menu_list) {
		food_list = menu_list;
		
		// setting up the inventory and stock level
		for(String s : food_list) {
			inventory.put(s, new Food(s));
			inventory.get(s).setAmount(2);
			inventory.get(s).setBatchSize(2);			
		}	
	}
	
	public void setMarketNumber(int marketNumber) {
		this.marketNumber = marketNumber;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean chkProcureInProcess(String food) {
		for(Procure p : procures) {
			if(p.food.equals(food)) {
				return true;		
			}
		}
		return false;		
	}
	
	public String toString() {
		return "cook " + getName();
	}
	
	public static class Procure {
		private String food;
		int batchSize;
		
		Procure (String food, int batchSize) {
			this.food = food;
			this.batchSize = batchSize;
		}
		
		public enum ProcureState
		{Pending, Delivering, Done, outOfStock};
		ProcureState state = ProcureState.Pending;
		
		public String getFood() {
			return food;
		}
	}
}

