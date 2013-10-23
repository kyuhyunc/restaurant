package restaurant; 

import agent.Agent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import restaurant.CustomerAgent.Cash;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import restaurant.test.mock.LoggedEvent;

/**
 * Restaurant cashier agent.
 */
public class CashierAgent extends Agent implements Cashier {
	private String name;

	// changed from private to public for unit testing
	public List<Check> checks = Collections.synchronizedList(new ArrayList<Check>());

	private Map<String, Double> menu;
	
	public String pattern = ".00";
	public DecimalFormat dFormat = new DecimalFormat(pattern);
	
	public enum AgentState
	{Waiting, Busy};
	public AgentState state = AgentState.Waiting;//The start state
	
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
	// Cashier 1a: ComputeBill
	//public void msgComputeBill(String choice, CustomerAgent c, WaiterAgent w, int tableNumber, Map<String, Double> menu) {
	public void msgComputeBill(String choice, Customer c, Waiter w, int tableNumber, Map<String, Double> menu) {
		print("Calculating bill");
		
		checks.add(new Check(choice, c, w, tableNumber));
		this.menu = menu;
	
		stateChanged();
	}
	
	// Cashier 4: Payment
	public void msgPayment(Customer customer, Cash cash) {		
		synchronized(checks) {
			for(Check c : checks) {
				if(c.customer == customer) {
					c.cash = new Cash(cash.twentyDollar, cash.tenDollar, cash.fiveDollar, cash.oneDollar, cash.coins);
					c.state = Check.CheckState.receivedCash;
					log.add(new LoggedEvent("Received msgPayment"));
					break;
				}
			}
		}
		stateChanged();	
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		synchronized (checks) {
			if (!checks.isEmpty()) {
				for(int i=0;i<checks.size();i++){
					if(checks.get(i).state == Check.CheckState.nothing) {
						state = AgentState.Busy;
						checks.get(i).state = Check.CheckState.computing;
						ComputeBill(checks.get(i));
						return true;
					}
					else if(checks.get(i).state == Check.CheckState.doneComputing) {
						state = AgentState.Busy;
						giveCheckToWaiter(checks.get(i));
						return true;
					}
					else if(checks.get(i).state == Check.CheckState.receivedCash) {						
						state = AgentState.Busy;
						checks.get(i).state = Check.CheckState.paid;
						returnChange(checks.get(i));
						//checks.remove(i);
						return true;
					}	
				}
			}
		}
		return false;
	}

	// Actions
	public void ComputeBill(Check c) {
		/**synchronized(checks) {
			for(Check check : checks) {
				if(check == c) {
					check.setPrice(menu.get(c.choice));
				}
			}
		}*/
		
		// should here have timer for setting the price..?
		c.setPrice(menu.get(c.choice));
		c.state = Check.CheckState.doneComputing;
		
		Do("Price is " + c.price);
		
		state = AgentState.Waiting;	
	}
	
	// private to public for testing
	public void giveCheckToWaiter(Check check) {
		// Deep copying check to cpCheck
		Check cpCheck = new Check(check.choice, check.customer, check.waiter, check.tableNumber);
		cpCheck.copyCheck(check);
		
		//if(check.waiter.state == WaiterAgent.AgentState.Waiting) {
			check.state = Check.CheckState.waitingToBePaid;	
			check.waiter.msgHereIsCheck(cpCheck);
		//}
		state = AgentState.Waiting;
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
		
		checks.remove(c);
		c.customer.msgChange(Change);		
		state = AgentState.Waiting;
	}
	
	// Accessors, etc.

	public String getName() {
		return name;
	}

	public String toString() {
		return "cook " + getName();
	}
	
	public static class Check {
		// changed to public for testing
		public String choice;
		//CustomerAgent customer;
		public Customer customer;
		public Waiter waiter;
		public int tableNumber;
		public double price;		
		
		Cash cash; // from customer
	
		//Check (String choice, CustomerAgent customer, WaiterAgent waiter, int tableNumber) {
		public Check (String choice, Customer customer, Waiter waiter, int tableNumber) {
			this.choice = choice;
			this.customer = customer;
			this.waiter = waiter;
			this.tableNumber = tableNumber;
		}
		
		public enum CheckState
		{nothing, computing, doneComputing, waitingToBePaid, receivedCash, paid};
		public CheckState state = CheckState.nothing;
		
		public void setPrice(double price) {
			this.price = price;
		}
		
		public double getPrice() {
			return price;
		}
		
		public Cash getCash() {
			return cash;
		}
		
		public void copyCheck(Check check) {
			this.price = check.price;
			Cash c = check.cash;
			if(c != null) {
				this.cash = new Cash(c.twentyDollar, c.tenDollar, c.fiveDollar, c.oneDollar, c.coins);
			}
		}
	}
}

