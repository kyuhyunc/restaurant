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

	private List<Procure> procures = Collections.synchronizedList(new ArrayList<Procure>());
	
	private Map<String, Food> inventory = Collections.synchronizedMap(new HashMap<String, Food> ());
	private List<String> food_list;
	
	private CookAgent cook;
	private HostAgent host;
	
	private Semaphore atCook = new Semaphore(0,true);
	

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
		
	// Messages
	// TheMarketAndCook 2: BuyFood()
	public boolean msgBuyFood(Procure procure) {
		// check availability for the procure order
		if(inventory.get(procure.food).amount < procure.batchSize) {
			return false;
		}
		else {
			print("received an procure order for " + procure.food + " from cook ");
			procures.add(procure);
			inventory.get(procure.food).amount -= procure.batchSize; // minus stock level in advance
			stateChanged();
			return true;
		}
	}
	
	// TheMarketAndCook 0: message from gui when food arrived to cook
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
						OrderFulfillment(procures.get(i));
						return true;
					}
				}
				//return true; // return true when state is Delivering, so that market can wait
			}
		}
		return false; // return false when there is no procure orders
	}

	// Actions
	private void DeliverOrder(Procure procure) {
		print("Start Delivering");
		
		DoDeliver(procure);
	}
	
	private void DoDeliver(Procure procure) {
		Timer timer = new Timer();
		FoodGui deliveryFood = new FoodGui(marketNumber, inventory.get(procure.food));
		host.gui.animationPanel.addGui(deliveryFood);
		
		class MyTimerTask extends TimerTask {
			Procure procure;
			FoodGui deliveryFood;
			MyTimerTask (Procure procure, FoodGui deliveryFood) {
				this.procure = procure;
				this.deliveryFood = deliveryFood;
			}
			
			public void run() {
				DoDeliveryGui(procure, deliveryFood);
			}
			
		}
			
		// delivery will be done in deliveryTime
		timer.schedule(new MyTimerTask(procure, deliveryFood),(int) deliveryTime); 		
	}
	
	private void DoDeliveryGui(Procure p, FoodGui f) {
		f.state = FoodGui.State.procurement;
		f.DoGoToCook(this);
		try {
			atCook.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		p.state = Procure.ProcureState.Done;
		stateChanged();		
	}
	
	private void OrderFulfillment(Procure procure) {
		cook.msgOrderFulfillment(procure);
		Do("Done delivering " + procure.food + " to the cook");
		procures.remove(procure);
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
	
	public Map<String, Food> getInventory() {
		return inventory;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean chkProcureInProcess(String food) {
		synchronized(procures) {
			for(Procure p : procures) {
				if(p.food.equals(food)) {
					return true;		
				}
			}
			return false;
		}
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

