package restaurant; 

import agent.Agent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import restaurant.CookAgent.Food;
import restaurant.CustomerAgent.Cash;

/**
 * Restaurant cashier agent.
 */
public class CashierAgent extends Agent {
	private String name;
	
	private CookAgent cook;
			
	//private List<Order> orders = new ArrayList<Order>();
	private List<Check> checks = Collections.synchronizedList(new ArrayList<Check>());
	
	private Map<String, Food> foods;
	
	public String pattern = ".00";
	public DecimalFormat dFormat = new DecimalFormat(pattern);
	
	/**
	 * Constructor for CashierAgent class
	 *
	 * @param name name of the customer
	 */
	public CashierAgent(String name){
		super();
		this.name = name;
	}
	
	/**
	 * hack to establish connection to Host agent.
	 */
	
	// Messages
	// 
	public void msgComputeBill(String choice, CustomerAgent c, WaiterAgent w, int tableNumber) {
		print("Calculating bill");
		
		checks.add(new Check(choice, c, w, tableNumber));
		
		stateChanged();
	}
	
	//
	public void msgPayment(Check check, Cash cash) {
		for(Check c : checks) {
			if(c == check) {
				c.cash = new Cash(cash.twentyDollar, cash.tenDollar, cash.fiveDollar, cash.oneDollar, cash.coins);
				//c.cash = cash;
				c.state = Check.CheckState.receivedCash;
				break;
			}
		}
		stateChanged();	
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if (!checks.isEmpty()) {
			synchronized (checks) {
				for(int i=0;i<checks.size();i++){
					if(checks.get(i).state == Check.CheckState.nothing) {
						checks.get(i).state = Check.CheckState.waitingToBePaid;
						ComputeBill(checks.get(i));
						return true;
					}
					else if(checks.get(i).state == Check.CheckState.receivedCash) {						
						checks.get(i).state = Check.CheckState.paid;
						returnChange(checks.get(i));
						checks.remove(i);
						return true;
					}
				}
			}
		}
		return false;
	}

	// Actions
	private void ComputeBill(Check c) {
		for(Check check : checks) {
			if(check == c) {
				check.setPrice(foods.get(c.choice).price);
			}
		}
		
		Do("Price is " + c.price);
		
		c.waiter.msgHereIsCheck(c);		
	}
	
	private void returnChange(Check c) {
		int twentyDollar = 0;
		int tenDollar = 0;
		int fiveDollar = 0;
		int oneDollar = 0;
		int coins = 0;
		
		double change = c.cash.totalAmount() - c.price;
		Cash Change;
				
		twentyDollar = (int) change/20;
		change -= 20*twentyDollar;
		
		tenDollar = (int) change/10;
		change -= 10*tenDollar;
		
		fiveDollar = (int) change/5;
		change -= 5*fiveDollar;
		
		oneDollar = (int) change/1;
		change -= 1*oneDollar;
				
		coins = (int) (100*((double)(change)+0.0001));
		
		Change = new Cash(twentyDollar, tenDollar, fiveDollar, oneDollar, coins);
			
		Do("Change is " + dFormat.format(Change.totalAmount()));
		
		c.customer.msgChange(Change);		
	}
	
	// Accessors, etc.

	public String getName() {
		return name;
	}

	public String toString() {
		return "cook " + getName();
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
		
		foods = cook.getFoods();
	}
	
	public static class Check {
		String choice;
		CustomerAgent customer;
		WaiterAgent waiter;
		int tableNumber;
		double price;		
		
		Cash cash; // from customer
		
		Check (String choice, CustomerAgent customer, WaiterAgent waiter, int tableNumber) {
			this.choice = choice;
			this.customer = customer;
			this.waiter = waiter;
			this.tableNumber = tableNumber;
		}
		
		public enum CheckState
		{nothing, waitingToBePaid, receivedCash, paid};
		CheckState state = CheckState.nothing;
		
		public void setPrice(double price) {
			this.price = price;
		}
	}
}

