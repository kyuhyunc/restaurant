package restaurant;

import restaurant.HostAgent.Table;
import restaurant.gui.CustomerGui;
import restaurant.gui.RestaurantGui;
import agent.Agent;
import restaurant.CookAgent.Order;
import restaurant.HostAgent;
import restaurant.CookAgent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
	
	public Set<Food> menu;
	/**
	 * Constructor for WaiterAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public WaiterAgent(String name){
		super();
		this.name = name;
		
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
		state = AgentState.Serving;
		print("Received msgSitAtTable from the host");
		MyCustomers.add(new MyCustomer(customer, table));
		stateChanged();
	}
	
	public void msgReadyToOrder(CustomerAgent customer) {
		state = AgentState.Serving;
		/**for(MyCustomer cust : MyCustomers) {
			if(cust.c == customer) {
				cust.state = MyCustomer.CustState.readyToOrder;
				stateChanged();
				break;
			}
		}*/
		for(int i=0; i < MyCustomers.size() ; i++) {
			if(MyCustomers.get(i).c == customer) {
				MyCustomers.get(i).state = MyCustomer.CustState.readyToOrder;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgHereIsMyChoice(CustomerAgent customer, String choice) {
		/**for(MyCustomer cust : MyCustomers) {
			if(cust.c == customer) {
				cust.state = MyCustomer.CustState.waitingFood;
				cust.choice = choice;
				stateChanged();
				break;
			}
		}*/
		state = AgentState.Serving;
		for(int i=0; i < MyCustomers.size() ; i++) {
			if(MyCustomers.get(i).c == customer) {
				MyCustomers.get(i).state = MyCustomer.CustState.waitingFood;
				MyCustomers.get(i).choice = choice;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgOrderIsReady(Order order) {
		state = AgentState.Serving;
		print("Order Is ready from cook");
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == order.customer) {
				cust.state = MyCustomer.CustState.foodIsReady;
				stateChanged();
				break;
			}
		}
	}
	
	public void msgLeavingTable(CustomerAgent customer) {
		state = AgentState.Serving;
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
		
		if (state == AgentState.Serving) {
			for (MyCustomer customer : MyCustomers) {
				if (customer.state == MyCustomer.CustState.Waiting) {
					SitAtTable(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.readyToOrder) {
					WhatWouldYouLike(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.waitingFood) {
					HereIsAnOrder(this, customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.foodIsReady) {
					HereIsYourOrder(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.doneEating) {
					TableIsCleared(customer);
					return true;
				}
			}
		}
		return false;
	}

	// Actions
	void SitAtTable(MyCustomer customer) {
		Do("SitAtTable in Actions");
		state = AgentState.Serving;
		customer.c.msgFollowMe(menu);
		DoSeatCustomer(customer);
	}
	
	void DoSeatCustomer(MyCustomer customer) {
		customer.state = MyCustomer.CustState.seated;
		customer.t.setOccupant(customer.c);
		state = AgentState.Waiting; 
	}
	
	void WhatWouldYouLike(MyCustomer customer) {
		Do("asking what would you like to customer " + customer.c);
		customer.c.msgWhatWouldYouLike();
		state = AgentState.Waiting; 
	}
		
	void HereIsAnOrder(WaiterAgent waiter, MyCustomer customer) {
		Do("Here is an order " + customer.choice + " from customer " + customer.c);
		cook.msgHereIsAnOrder(waiter, customer.c, customer.choice);
		state = AgentState.Waiting;
	}
	
	void HereIsYourOrder(MyCustomer customer) {
		Do("Here is an order for you, " + customer.c);
		customer.c.msgHereIsYourOrder();
		customer.state = MyCustomer.CustState.eating;
		state = AgentState.Waiting;
	}

	void TableIsCleared(MyCustomer customer) {
		
		MyCustomers.remove(customer);
		
		host.msgTableIsCleared(customer.t);
		state = AgentState.Waiting; 
	}
	// Accessors, etc.

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "waiter " + getName();
	}
	
	private static class MyCustomer {
		CustomerAgent c;
		Table t;
		String choice;

		public enum CustState
		{Waiting, seated, readyToOrder, waitingFood, foodIsReady, eating, doneEating};
		CustState state = CustState.Waiting;//The start state
		
		MyCustomer(CustomerAgent customer, Table table) {
			c = customer;
			t = table;
			choice = "";
			state = CustState.Waiting;
		}
	}
	
	public class Food {
		String name;
		int foodNumber;
		int cookingTime; // for setting timer differently
	}
}

