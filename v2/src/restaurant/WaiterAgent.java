package restaurant;

import restaurant.HostAgent.Table;
import restaurant.gui.WaiterGui;
import agent.Agent;
import restaurant.CookAgent.Order;
import restaurant.HostAgent;
import restaurant.CookAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class WaiterAgent extends Agent {
	private String name;
	
	// agent correspondents
	private HostAgent host;
	private CookAgent cook;
	
	// Q: can this be replaced by Semaphore?
	public enum AgentState
	{Waiting, Serving};
	public AgentState state = AgentState.Waiting;//The start state

	private List<MyCustomer> MyCustomers = new ArrayList<MyCustomer>();
	
	public WaiterGui waiterGui = null;
	public int CurrentTableNumber = 0;
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private Semaphore atHost = new Semaphore(0,true);
		
	public Set<String> menu = new TreeSet<String>();
	
	/**
	 * Constructor for WaiterAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public WaiterAgent(String name){
		super();
		this.name = name;
		
		menu.addAll(Arrays.asList("Stake","Chicke","Salad","Pizza"));
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}

	// Messages

	// 2:
	public void msgSitAtTable(CustomerAgent customer, Table table) {
		//state = AgentState.Serving;
		state = AgentState.Waiting;
		print("Received msgSitAtTable from the host");
		MyCustomers.add(new MyCustomer(customer, table));
		stateChanged();
	}
	
	public void msgAtTable() {
		atTable.release();// = true;
	}
	
	public void msgAtHost() {
		atHost.release();
	}
	/**
	public void msgReadyToServe() {
		state = AgentState.Waiting;
		stateChanged();
	}*/
	
	public void msgReadyToOrder(CustomerAgent customer) {
		for(int i=0; i < MyCustomers.size() ; i++) {
			if(MyCustomers.get(i).c == customer) {
				MyCustomers.get(i).state = MyCustomer.CustState.readyToOrder;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgHereIsMyChoice(CustomerAgent customer, String choice) {
		for(int i=0; i < MyCustomers.size() ; i++) {
			if(MyCustomers.get(i).c == customer) {
				MyCustomers.get(i).state = MyCustomer.CustState.waitingFood1;
				MyCustomers.get(i).choice = choice;
				state = AgentState.Waiting;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgArrivedToCook() {
		atCook.release();
	}
	
	public void msgOrderIsReady(Order order) {
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == order.customer) {
				cust.state = MyCustomer.CustState.foodIsReady;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgLeavingTable(CustomerAgent customer) {
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == customer) {
				cust.state = MyCustomer.CustState.doneEating;
				stateChanged();
				break;
			}
		}
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine
		
		if (state == AgentState.Waiting) {
		//if (state == AgentState.Serving) {
			for (MyCustomer customer : MyCustomers) {
				if (customer.state == MyCustomer.CustState.Waiting) {
					state = AgentState.Serving;
					SitAtTable(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.readyToOrder) {
					state = AgentState.Serving;
					WhatWouldYouLike(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.waitingFood1) {
					state = AgentState.Serving;
					HereIsAnOrder(this, customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.foodIsReady) {
					state = AgentState.Serving;
					HereIsYourOrder(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.doneEating) {
					state = AgentState.Serving;
					TableIsCleared(customer);
					return true;
				}
			}
		}
		return false;
	}

	// Actions
	void SitAtTable(MyCustomer customer) {
		// set up the initial position to the host
		waiterGui.DoGoBackToHost();
		try {
			atHost.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		customer.c.msgFollowMe(menu);
		DoSeatCustomer(customer);
		//customer.t.setOccupant(customer.c);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		customer.state = MyCustomer.CustState.seated;
		
		waiterGui.DoGoBackToHost();
		state = AgentState.Waiting;
	}
	
	void DoSeatCustomer(MyCustomer customer) {
		Do("Seating " + customer.c + " at the table #" + customer.t.tableNumber);
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber); 
	}
	
	void WhatWouldYouLike(MyCustomer customer) {
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		Do("asking what would you like to customer " + customer.c);
		customer.c.msgWhatWouldYouLike();
	}
		
	void HereIsAnOrder(WaiterAgent waiter, MyCustomer customer) {
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		Do("Here is an order " + customer.choice + " from customer " + customer.c);
		cook.msgHereIsAnOrder(waiter, customer.c, customer.choice);
		
		customer.state = MyCustomer.CustState.waitingFood2;
		
		waiterGui.DoGoBackToHost();
		state = AgentState.Waiting;
		
	}
	
	void DoGoToCook() {
		Do("Going to cook");
		waiterGui.GoToCook();
	}
	
	void HereIsYourOrder(MyCustomer customer) {
		// going to cook to pick up the order
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		Do("Here is an order for you, " + customer.c);
		customer.c.msgHereIsYourOrder();
		customer.state = MyCustomer.CustState.eating;

		waiterGui.DoGoBackToHost();
		state = AgentState.Waiting;
	}

	void TableIsCleared(MyCustomer customer) {
		
		MyCustomers.remove(customer);
		
		host.msgTableIsCleared(customer.t);
		//state = AgentState.Waiting;
	}
	
	
	// Accessors, etc.
	public List<MyCustomer> getMyCustomers() {
		return MyCustomers;
	}
		
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "waiter " + getName();
	}
	
	public static class MyCustomer {
		CustomerAgent c;
		Table t;
		String choice;

		public enum CustState
		{Waiting, seated, readyToOrder, waitingFood1, waitingFood2, foodIsReady, eating, doneEating};
		CustState state = CustState.Waiting;//The start state
		
		MyCustomer(CustomerAgent customer, Table table) {
			c = customer;
			t = table;
			choice = "";
			state = CustState.Waiting;
		}
	}
		
	public static class Food {
		String name;
		
		int time; // for setting timer differently
		double cookingTimeMultiplier = 3;
		double eatingTimeMultiplier = 2.5;
		
		Food(String name) {
			this.name = name;
			
			if (name == "Stake") {
				time = (int) (1000 * cookingTimeMultiplier);
			}
			else if (name == "Chiken") {
				time = (int) (800 * cookingTimeMultiplier);
			}
			else if (name == "Salad") {
				time = (int) (600 * cookingTimeMultiplier);
			}
			else if (name == "Pizza") {
				time = (int) (300 * cookingTimeMultiplier);
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
	}
}

