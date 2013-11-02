package restaurant;

import restaurant.HostAgent.Table;
import restaurant.gui.FoodGui;
import restaurant.gui.WaiterGui;
import restaurant.interfaces.Waiter;
import agent.Agent;
import restaurant.CashierAgent.Check;
import restaurant.CookAgent.Food;
import restaurant.CookAgent.Order;
import restaurant.HostAgent;
import restaurant.CookAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class WaiterAgent extends Agent implements Waiter{
	private String name;
	
	// agent correspondents
	private HostAgent host;
	private CookAgent cook;
	private CashierAgent cashier;
	
	public enum AgentState
	{Waiting, Serving};
	public AgentState state = AgentState.Waiting;//The start state
	
	public enum AgentBreak
	{none, askForBreak, waitingForAnswer, onBreak, offBreak};
	public AgentBreak Break = AgentBreak.none;//The start state

	private List<MyCustomer> MyCustomers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	int numberOfCustomers = 0;
	
	//private List<Check> checkToPick = Collections.synchronizedList(new ArrayList<Check>());
	//private List<Order> orderToPick = Collections.synchronizedList(new ArrayList<Order>());
	
	public WaiterGui waiterGui = null;
	private Semaphore atTable = new Semaphore(0,true);
	private Semaphore atCook = new Semaphore(0,true);
	private Semaphore atHost = new Semaphore(0,true);
	private Semaphore atCashier = new Semaphore(0,true);
	private Semaphore atLine = new Semaphore(0,true);
		
	private List<String> menu_list = Collections.synchronizedList(new ArrayList<String> ());
	private Map<String, Double> menu = Collections.synchronizedMap(new HashMap<String, Double> ());
	
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
		print("Received msgSitAtTable from the host");
		numberOfCustomers ++;
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
	
	public void msgAtLine() {
		atLine.release();
	}
	
	// 4: ReadyToOrder(customer); 
	public void msgReadyToOrder(CustomerAgent customer) {
		synchronized (MyCustomers) {
			for(int i=0; i < MyCustomers.size() ; i++) {
				if(MyCustomers.get(i).c == customer) {
					MyCustomers.get(i).state = MyCustomer.CustState.readyToOrder;
					//state = AgentState.Waiting;
					stateChanged();
					break;
				}
			}
		}
	}
	
	// 6: HereIsMyChoice(customer, choice)
	// OutOfFood 1: HereIsMyChoice
	public void msgHereIsMyChoice(CustomerAgent customer, String choice) {
		synchronized (MyCustomers) {
			for(int i=0; i < MyCustomers.size() ; i++) {
				if(MyCustomers.get(i).c == customer) {
					MyCustomers.get(i).state = MyCustomer.CustState.waitingFood1;
					MyCustomers.get(i).choice = choice;
					//state = AgentState.Waiting;
					stateChanged();
					break;
				}
			}
		}
	}
	
	// msg from gui
	public void msgArrivedToCook() {
		atCook.release();
	}

	// OutOfFood 3: OutOfFood()
	// TheMarketAndCook 1: OutOfFood()
	public void msgOrderIsOutOfStock(Order order) {
		// the food that is out of stock will be erased from the menu list 
		// and will be passed to the customer when the waiter ask the customer again.
		// However, this menu list will be reseted when the waiter sits the customer down.
				
		menu_list.remove(order.choice);

		synchronized (MyCustomers) {
			for(MyCustomer cust : MyCustomers) {
				if(cust.c == order.customer) {
					cust.state = MyCustomer.CustState.reOrder;
					stateChanged();
					break;
				}
			}
		}
	}
		
	// 8: OrderIsReady(order)
	public void msgOrderIsReady(Order order) {
		synchronized (MyCustomers) {
			for(MyCustomer cust : MyCustomers) {
				if(cust.c == order.customer) {
					//int tableNumber = cust.c.foodGui.tableNumber; 
					//cust.c.foodGui.state = FoodGui.State.noCommand;
					cust.fg = order.foodGui;
					//cust.c.foodGui = order.foodGui;
					//cust.c.foodGui.tableNumber = tableNumber;
					cust.state = MyCustomer.CustState.foodIsReady;
					cust.order = order;
					//orderToPick.add(order);
					stateChanged();
					break;
				}
			}
		}
	}
	
	// Cashier 0: ReadyForcheck
	public void msgReadyForCheck(CustomerAgent customer) {
		synchronized (MyCustomers) {
			for(MyCustomer cust : MyCustomers) {
				if(cust.c == customer) {
					cust.state = MyCustomer.CustState.askingForCheck;
					stateChanged();
					break;
				}
			}
		}
	}

	// Cashier : message from gui when waiter arrives at cashier
	public void msgArrivedToCashier() {
		atCashier.release();
	}
	
	// Cashier 1b: HereIsCheck (from cashier)
	public void msgHereIsCheck(Check check) {
		synchronized (MyCustomers) {
			for(MyCustomer cust : MyCustomers) {
				if(cust.c == check.customer) {
					cust.state = MyCustomer.CustState.checkIsReady;
					cust.check = check;
					//checkToPick.add(check);
					//state = AgentState.Waiting;
					stateChanged();
					break;
				}
			}
		}
	}
	
	// 10: IAmDone(customer)
	// Cashier 3: LeaveTable
	public void msgLeavingTable(CustomerAgent customer) {
		synchronized (MyCustomers) {
			for(MyCustomer cust : MyCustomers) {
				if(cust.c == customer) {
					cust.state = MyCustomer.CustState.leaving;
					stateChanged();
					break;
				}
			}
		}
	}
	
	// WaiterOnBreak 0: message from gui when break box is checked (on break)
	public void msgOnBreak() {
		Break = AgentBreak.askForBreak;
		stateChanged();
	}
	
	// WaiterOnBreak 2: ReplyBreak
	public void msgReplyBreak(boolean breakPermission) {
		
		// if waiter can break, then break permission is true
		waiterGui.getReplyBreak(breakPermission);
	
		if(breakPermission) {
			Break = AgentBreak.onBreak;
			Do("I am on break");
		}
		else {
			Break = AgentBreak.none;
			Do("I can't be on break T.T...");
		}
			
		stateChanged();
	}
	
	// WaiterOnBreak 0: message from gui when break box is unchecked (off break)
	public void msgOffBreak() {
		Break = AgentBreak.offBreak;
		Do("I am off break");
		
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	WaiterAgent is a finite state machine
		synchronized (MyCustomers) {
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
					//else if (orderToPick.isEmpty() && customer.state == MyCustomer.CustState.waitingFood1) {
					else if (customer.state == MyCustomer.CustState.waitingFood1) {
						state = AgentState.Serving;
						HereIsAnOrder(customer);
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
					//else if (checkToPick.isEmpty() && customer.state == MyCustomer.CustState.askingForCheck) {
					else if (customer.state == MyCustomer.CustState.askingForCheck) {
						state = AgentState.Serving;
						AskForCheck(customer);
						return true;
					}					
					else if (customer.state == MyCustomer.CustState.checkIsReady) {
						state = AgentState.Serving;
						HereIsCheck(customer);
						return true;
					}
				}
			}
			if(Break == AgentBreak.askForBreak) {
				AskForBreak();
				return true;
			}
			else if(Break == AgentBreak.offBreak) {
				OffBreak();
				return true;
			}
			for (MyCustomer customer : MyCustomers) {
				if (customer.state == MyCustomer.CustState.leaving) {
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
		
		waiterGui.DoGoToLine();
		try {
			atLine.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		host.msgPickedCust(customer.c);
		
		// initializing menu list
		menu_list.clear();
		menu.clear();
		
		menu_list.addAll(cook.getMenuList());
		
		for(int i=0;i<cook.getMenuList().size();i++) {
			menu.put(menu_list.get(i), cook.getPrice(menu_list.get(i)));
		}
		
		// giving a full menu to customer
		customer.c.msgFollowMe(menu_list, menu, customer.t.tableNumber);
		DoSeatCustomer(customer);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		customer.state = MyCustomer.CustState.seated;
		
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
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
		state = AgentState.Waiting;
		customer.state = MyCustomer.CustState.askedForOrder;
		customer.c.msgWhatWouldYouLike();
	}
		
	void HereIsAnOrder(MyCustomer customer) {
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		customer.state = MyCustomer.CustState.waitingFood2;
		
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
		
		Do("Here is an order " + customer.choice + " from " + customer.c);
		cook.msgHereIsAnOrder(new Order(this, customer.c, customer.choice));
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
		
		customer.state = MyCustomer.CustState.reOrdering;
		
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
		
		// update the menu of customer;
		customer.c.msgAskForOrderAgain(menu_list);
	}
	
	void HereIsYourOrder(MyCustomer customer) {
		// going to cook to pick up the order
		DoGoToCook();
		try {
			atCook.acquire();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		//cook.msgArrivedToPick(customer.c);
		cook.msgArrivedToPick(customer.order);
	
		/**customer.c.foodGui.state = FoodGui.State.noCommand;
		customer.c.foodGui = customer.fg;
		customer.c.foodGui.tableNumber = customer.t.tableNumber;
		*/
		
		customer.c.getFoodGui().state = FoodGui.State.delivering;
		customer.c.getFoodGui().DoGoToTable();
				
		waiterGui.DoGoToTable(customer.c, customer.t.tableNumber);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//orderToPick.remove(customer.order);
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
		
		Do("Here is an order " + customer.choice + " for you, " + customer.c);
		customer.c.msgHereIsYourOrder();
		customer.state = MyCustomer.CustState.eating;
	}
	
	void AskForCheck(MyCustomer c) {
		waiterGui.DoGoToTable(c.c, c.t.tableNumber);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		c.c.getFoodGui().state = FoodGui.State.waitingCheck;
		c.state = MyCustomer.CustState.waitingForCheck;		
		
		waiterGui.DoGoToCashier();
		try {
			atCashier.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
				
		cashier.msgComputeBill(c.choice, c.c, this, c.t.tableNumber, menu);		
		
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
	}
	


	void HereIsCheck(MyCustomer c) {
		waiterGui.DoGoToCashier();
		try {
			atCashier.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		c.c.getFoodGui().state = FoodGui.State.deliveringCheck;
		c.c.getFoodGui().DoGoToTable();
		
		waiterGui.DoGoToTable(c.c, c.t.tableNumber);
		try {
			atTable.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		c.state = MyCustomer.CustState.getTheCheck;
		
		Check chk = c.check;
		Check cpCheck = new Check(chk.choice, chk.customer, chk.waiter, chk.tableNumber);
		cpCheck.copyCheck(chk);
		
		//c.c.msgHereIsYourCheck(c.check);
		c.c.msgHereIsYourCheck(cpCheck);
		//checkToPick.remove(c.check);
		
		state = AgentState.Waiting;
		waiterGui.DoGoBackToHost2();
	}
	
	void TableIsCleared(MyCustomer customer) {
		
		host.msgTableIsCleared(customer.t);
		MyCustomers.remove(customer);
		
		numberOfCustomers --;
	}
	
	void AskForBreak() {
		Do("Can I have a break?");
		// need to ask for permission to Host
		Break = AgentBreak.waitingForAnswer;
		host.msgCanIBreak(this);
	}
	
	void OffBreak() {
		Break = AgentBreak.none;
		host.msgOffBreak();		
	}
	
	// Accessors, etc.
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
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
	
	public int getNumberOfCustomers() {
		return numberOfCustomers;		
	}
	
	public String getName() {
		return name;
	}
	
	public Food getFood(String choice){
		return cook.getFoods().get(choice);
	}
	
	public String toString() {
		return "waiter " + getName();
	}
	
	public static class MyCustomer {
		CustomerAgent c;
		Table t;
		String choice;
		Check check;
		Order order;
		FoodGui fg;

		public enum CustState
		{Waiting, seated, readyToOrder, askedForOrder, waitingFood1, waitingFood2, foodIsReady, 
		eating, askingForCheck, waitingForCheck, checkIsReady, getTheCheck, leaving,
		reOrder, reOrdering};
		CustState state = CustState.Waiting;//The start state
		
		MyCustomer(CustomerAgent customer, Table table) {
			c = customer;
			t = table;
			choice = "";
			state = CustState.Waiting;
		}
	}
	
}

