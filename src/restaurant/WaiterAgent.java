package restaurant;

import restaurant.HostAgent.Table;
import restaurant.gui.FoodGui;
import restaurant.gui.WaiterGui;
import agent.Agent;
import restaurant.CookAgent.Food;
import restaurant.CookAgent.Order;
import restaurant.HostAgent;
import restaurant.CookAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class WaiterAgent extends Agent {
	private String name;
	
	// agent correspondents
	private HostAgent host;
	private CookAgent cook;
	
	public enum AgentState
	{Waiting, Serving};
	public AgentState state = AgentState.Waiting;//The start state

	private List<MyCustomer> MyCustomers = new ArrayList<MyCustomer>();
	
	public WaiterGui waiterGui = null;
	public int CurrentTableNumber = 0;
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private Semaphore atHost = new Semaphore(0,true);
		
	//public Map<String, Food> menu = new HashMap<String, Food> ();
	//public List<String> menu_list = new ArrayList<String> ();
	private List<String> menu_list = new ArrayList<String> ();
	private int numberOfMenu;
	
	/**
	 * Constructor for WaiterAgent class
	 *
	 * @param name name of the customer
	 */
	public WaiterAgent(String name){
		super();
		this.name = name;
	}

	// Messages
	
	// 2: SitAtTable(customer, table)
	public void msgSitAtTable(CustomerAgent customer, Table table) {
		//state = AgentState.Serving;
		//state = AgentState.Waiting;
		print("Received msgSitAtTable from the host");
		MyCustomers.add(new MyCustomer(customer, table));
		stateChanged();
	}
	
	// msg from gui
	public void msgAtTable() {
		atTable.release();// = true;
	}
	
	// msg from gui
	public void msgAtHost() {
		atHost.release();
	}
	
	// 4: ReadyToOrder(customer); 
	public void msgReadyToOrder(CustomerAgent customer) {
		for(int i=0; i < MyCustomers.size() ; i++) {
			if(MyCustomers.get(i).c == customer) {
				MyCustomers.get(i).state = MyCustomer.CustState.readyToOrder;
				stateChanged();
				break;
			}
		}
	}
	
	// 6: HereIsMyChoice(customer, choice)
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
	
	// msg from gui
	public void msgArrivedToCook() {
		atCook.release();
	}

	public void msgOrderIsOutOfStock(Order order) {
		//menu.remove(order.choice);
		menu_list.remove(order.choice);
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == order.customer) {
				cust.state = MyCustomer.CustState.reOrder;
				stateChanged();
				break;
			}
		}
	}
	
	// 8: OrderIsReady(order)
	public void msgOrderIsReady(Order order) {
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == order.customer) {
				cust.state = MyCustomer.CustState.foodIsReady;
				stateChanged();
				break;
			}
		}
	}
	
	// 10: IAmDone(customer)
	public void msgLeavingTable(CustomerAgent customer) {
		for(MyCustomer cust : MyCustomers) {
			if(cust.c == customer) {
				cust.state = MyCustomer.CustState.doneEating;
				stateChanged();
				break;
			}
		}
	}
	
	// msg from gui
	public void msgOnBreak() {
		Do("Can I have a break?");
		//host.msgOffBreak();
		// need to ask for permssion to Host
		host.msgCanIBreak(this);
	}
	
	public void msgReplyBreak(boolean breakPermission) {
		
		waiterGui.getReplyBreak(breakPermission);
		
		if(breakPermission) {
			Do("I am on break");
		}
		else
			Do("I can't be on break T.T...");
	}
	
	public void msgOffBreak() {
		Do("I am off break");
		host.msgOffBreak();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	WaiterAgent is a finite state machine
		if (state == AgentState.Waiting) {
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
				else if (customer.state == MyCustomer.CustState.reOrder) {
					state = AgentState.Serving;
					WhatWouldYouLikeAgain(customer);
					return true;
				}
				else if (customer.state == MyCustomer.CustState.foodIsReady) {
					state = AgentState.Serving;
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
		// set up the initial position to the host
		waiterGui.DoGoBackToHost();
		try {
			atHost.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		// initializing menu list
		//menu_list = cook.getMenuList(); 
		menu_list.addAll(cook.getMenuList());
		numberOfMenu = menu_list.size();
		
		customer.c.msgFollowMe(menu_list);
		DoSeatCustomer(customer);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		customer.state = MyCustomer.CustState.seated;
		
		state = AgentState.Waiting;
		host.msgReadyToServe();
		waiterGui.DoGoBackToHost2();
		stateChanged();
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
				
		Do("asking what would you like to " + customer.c);
		customer.c.msgWhatWouldYouLike();
	}
		
	void HereIsAnOrder(WaiterAgent waiter, MyCustomer customer) {
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		Do("Here is an order " + customer.choice + " from " + customer.c);
		cook.msgHereIsAnOrder(new Order(waiter, customer.c, customer.choice));
		
		customer.state = MyCustomer.CustState.waitingFood2;
		
		state = AgentState.Waiting;
		host.msgReadyToServe();
		waiterGui.DoGoBackToHost2();
		stateChanged();		
	}
	
	void DoGoToCook() {
		Do("Going to cook");
		waiterGui.GoToCook();
	}
	
	void WhatWouldYouLikeAgain(MyCustomer customer) {
		Do("Ask for the order again");
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// update the menu of customer;
		customer.c.msgAskForOrderAgain(menu_list);
				
		state = AgentState.Waiting;
		//host.msgReadyToServe();
		waiterGui.DoGoBackToHost2();
		stateChanged();
	}
	
	void HereIsYourOrder(MyCustomer customer) {
		// going to cook to pick up the order
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		customer.c.getFoodGui().state = FoodGui.State.delivering;
		customer.c.getFoodGui().DoGoToTable();
				
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		Do("Here is an order " + customer.choice + " for you, " + customer.c);
		customer.c.msgHereIsYourOrder();
		customer.state = MyCustomer.CustState.eating;
		
		//customer.c.getFoodGui().state = FoodGui.State.delivered;

		state = AgentState.Waiting;
		host.msgReadyToServe();
		waiterGui.DoGoBackToHost2();
		stateChanged();
	}

	void TableIsCleared(MyCustomer customer) {
		
		MyCustomers.remove(customer);
		
		host.msgTableIsCleared(customer.t);
		//state = AgentState.Waiting;
	}
	
	// Accessors, etc.
	
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
	public CookAgent getCook() {
		return cook;
	}
	
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}
	
	public List<MyCustomer> getMyCustomers() {
		return MyCustomers;
	}
	
	public String getName() {
		return name;
	}
	
	public Food getFood(String choice){
		return cook.getMenu().get(choice);
	}
	
	public int getNumberOfMenu() {
		return numberOfMenu;
	}
	
	
	public String toString() {
		return "waiter " + getName();
	}
	
	public static class MyCustomer {
		CustomerAgent c;
		Table t;
		String choice;

		public enum CustState
		{Waiting, seated, readyToOrder, waitingFood1, waitingFood2, foodIsReady, 
			eating, doneEating, reOrder};
		CustState state = CustState.Waiting;//The start state
		
		MyCustomer(CustomerAgent customer, Table table) {
			c = customer;
			t = table;
			choice = "";
			state = CustState.Waiting;
		}
	}
	
}

