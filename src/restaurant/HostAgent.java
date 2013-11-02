package restaurant;

import agent.Agent;
import restaurant.gui.RestaurantGui;
import restaurant.WaiterAgent;

import java.util.*;

/**
 * Restaurant Host Agent
 */
public class HostAgent extends Agent {
	static public int NTABLES = 3;//a global for the number of tables.
	
	public enum AgentState
	{Waiting, Serving};
	public AgentState state = AgentState.Waiting;//The start state
	
	private List<WaiterAgent> waiters = Collections.synchronizedList(new ArrayList<WaiterAgent>());
	private List<CustomerAgent> waitingCustomers = Collections.synchronizedList(new ArrayList<CustomerAgent>());
	private List<CustomerAgent> customersInLine = Collections.synchronizedList(new ArrayList<CustomerAgent>());
	private CookAgent cook;
	
	private int numberOfWaiters = 0;
	private int numberOfOnBreakWaiters = 0;
	
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented	
	public Collection<Table> tables;
		
	public RestaurantGui gui;
	
	private String name;
	
	boolean tableAvailable = true;
	
	public HostAgent(String name) {
		super();

		this.name = name;
		
		// make some tables
		tables = new ArrayList<Table>();
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix)); //how you add to a collections			
		}		
	}

	// Messages
	// message from gui to add waiter
	public void msgAddWaiterByGui(WaiterAgent waiter) {
		Do("New waiter " + waiter.getName() + " is added");				
		numberOfWaiters ++;
		waiters.add(waiter);
		stateChanged();
	}
	
	// 1: IWantFood(customer)
	public void msgIWantFood(CustomerAgent cust) {

		waitingCustomers.add(cust);
		customersInLine.add(cust);
		
		//cust.waitingNumber = waitingCustomers.size();
		
		Do(cust + " is added to the waiting list");
		stateChanged();
	}
	
	// message when tables are full from customer
	public void msgDecision(CustomerAgent customer) {
		if(!customer.waitWhenTableFull) {
			waitingCustomers.remove(customer);
			customersInLine.remove(customer);
		}
	}

	// 11: TableIsCleared(table)
	public void msgTableIsCleared(Table table) {
		Do("table #" + table.tableNumber + " is cleared");
		table.setUnoccupied();
		stateChanged(); // so that when a customer leaves, host will check availability of tables again
	}
	
	// WaiterGoOnBreak 0: CanIHaveBreak
	public void msgCanIBreak(WaiterAgent w) {
		Do("Negotiation on break is going on for " + w.getName());
		
		stateChanged();		
	}
	
	// WaiterGoOnBreak 2: OffBreak
	public void msgOffBreak() {
		numberOfOnBreakWaiters --;
		stateChanged();
	}
	
	public void msgPickedCust(CustomerAgent c) {
		// update waiting number for customers
		synchronized(customersInLine) {
			customersInLine.remove(c);
			for(int i=0;i<customersInLine.size();i++) {
				//waitingCustomers.get(i).msgGoToLine(i+1);
				customersInLine.get(i).waitingNumber = i+1;
			}
		}
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		synchronized(waiters) {
			for(WaiterAgent w : waiters) {
				if(w.Break == WaiterAgent.AgentBreak.waitingForAnswer) {
					chkIfWaiterCanBreak(w);
					return true;
				}
			}
		}		
		
		synchronized(customersInLine) {
			for(int i=0;i<customersInLine.size();i++) {
				WaitingInLine(customersInLine.get(i), i+1);
			}
		}
		
		if (!waitingCustomers.isEmpty()) {
			//WaitingInLine(waitingCustomers.get(waitingCustomers.size()-1), waitingCustomers.size());
			
			if(waiters.size() > 0) {
				for (Table table : tables) {
					tableAvailable = true;
					if (!table.isOccupied()) {
						tellWaiter(waitingCustomers.get(0), table);
						return true;//return true to the abstract agent to reinvoke the scheduler.
					}					
				}
				Do("There is no table available");
				tableAvailable = false;
			}
			else {
				Do("There is no waiter");
				return false;
			}
			// if tables are full, ask customer whether they would wait or leave
			if (!tableAvailable) {
				askCustomerWhenFull();
				return false;
			}
		}
		
		return false;
	}

	// Actions
	
	private void askCustomerWhenFull() {
		Do("Tables are full, ask customer whether wait or leave");
		synchronized(waitingCustomers) {
			for(CustomerAgent c : waitingCustomers) {
				if(!c.waitWhenTableFull)	c.msgWhetherLeave();
			}
		}
	}
	
	private void WaitingInLine(CustomerAgent cust, int waitingNumber) {
		cust.msgGoToLine(waitingNumber);
	}
	
	private void tellWaiter(CustomerAgent customer, Table table) {

		WaiterAgent w;
		
		w = getTheMostFreeWaiter();
		
		if(w!=null) {
			table.setOccupant(customer);
			w.msgSitAtTable(customer, table);
			customer.setWaiter(w);
			//customer.waitingNumber = 0;	
			waitingCustomers.remove(customer);
			

		}

		/**else
		Do("No waiter available");*/
	}
	
	public void chkIfWaiterCanBreak(WaiterAgent w) {
		boolean breakPermission;
		int numberOfOffBreakWaiters;
		
		// when the last non break waiter is clicked, count will be zero --> default = 1
		/**int numberOfOffBreakWaiters = 1; 
		synchronized(waiters) {
			for(WaiterAgent wait : waiters){
				if(!wait.getGui().isBreak()){
					numberOfNoBreakWaiters ++;
				}
			}
		}*/
		
		numberOfOffBreakWaiters = numberOfWaiters - numberOfOnBreakWaiters;
		
				
		if( (waitingCustomers.isEmpty()) && (numberOfOffBreakWaiters > 1)) {
			numberOfOnBreakWaiters ++;
			breakPermission = true;
		}
		else {
			breakPermission = false;
		}
		w.msgReplyBreak(breakPermission);		
	}

	//utilities
	public void setRestaurantGui(RestaurantGui gui) {
		this.gui = gui;
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
	public CookAgent getCook() {
		return cook;
	}
	
	public List<WaiterAgent> getWaiters() {
		return waiters;
	}
	
	public String getName() {
		return name;
	}
		
	public void addTableByGui() {
		tables.add(new Table(NTABLES));//how you add to a collections
	}
	
	public WaiterAgent getTheMostFreeWaiter() {
		List<WaiterAgent> availableWaiters = Collections.synchronizedList(new ArrayList<WaiterAgent>()); 
		int numberOfCustomers = -1;
		int waiterNumber = -1;
		
		// this for loop is for saving the first customersize and waiternumber to compare with others
		synchronized(waiters) {
			for(int i=0;i<waiters.size();i++) {
				if(!waiters.get(i).getGui().isBreak()) {
					availableWaiters.add(waiters.get(i));
				}
			}
		}
		
		synchronized(availableWaiters) {
			if(!availableWaiters.isEmpty()) {
				numberOfCustomers = availableWaiters.get(0).getNumberOfCustomers();
				waiterNumber = 0;
				for(int i=0; i < availableWaiters.size() ; i++) {
					if( numberOfCustomers >= availableWaiters.get(i).getNumberOfCustomers()) {
						numberOfCustomers = availableWaiters.get(i).getNumberOfCustomers();
						waiterNumber = i; 
					}				
				}
				return availableWaiters.get(waiterNumber);
			}
			return null;
		}				
	}
	
	public class Table {
		CustomerAgent occupiedBy;
		int tableNumber;

		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupant(CustomerAgent cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		CustomerAgent getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}
	}
	
}

