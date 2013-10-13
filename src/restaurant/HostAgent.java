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
	
	public List<WaiterAgent> waiters = Collections.synchronizedList(new ArrayList<WaiterAgent>());
	public List<CustomerAgent> waitingCustomers	= Collections.synchronizedList(new ArrayList<CustomerAgent>());
	public CookAgent cook;
		
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
	// 0:
	public void msgAddWaiter(WaiterAgent waiter) {
		Do("New waiter " + waiter.getName() + " is added");				
	
		waiters.add(waiter);
		stateChanged();
	}
	
	// 1: IWantFood(customer)
	public void msgIWantFood(CustomerAgent cust) {

		waitingCustomers.add(cust);
		Do(cust + " is added to the waiting list");
		stateChanged();
	}
	
	public void msgDecision(CustomerAgent customer) {
		if(!customer.waitWhenTableFull) {
			waitingCustomers.remove(customer);
		}
	}

	// 11: TableIsCleared(table)
	public void msgTableIsCleared(Table table) {
		Do("table #" + table.tableNumber + " is cleared");
		table.setUnoccupied();
		stateChanged(); // so that when a customer leaves, host will check availability of tables again
	}
	
	// msg from waiter on break
	public void msgCanIBreak(WaiterAgent w) {
		Do("Negotiation on break is going on for " + w.getName());
		
		stateChanged();		
	}
	
	public void msgOffBreak() {
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
		if (!waitingCustomers.isEmpty()) {
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
		
		for(CustomerAgent c : waitingCustomers) {
			if(!c.waitWhenTableFull)	c.msgWhetherLeave();
		}
	}
	
	private void tellWaiter(CustomerAgent customer, Table table) {

		WaiterAgent w;
		
		w = getTheMostFreeWaiter();
		
		if(w!=null) {
			table.setOccupant(customer);
			w.msgSitAtTable(customer, table);
			customer.setWaiter(w);
			waitingCustomers.remove(customer);
		}
		/**else
			Do("No waiter available");*/
	}
	
	public void chkIfWaiterCanBreak(WaiterAgent w) {
		boolean breakPermission;
		
		// when the last non break waiter is clicked, count will be zero --> default = 1
		int numberOfNoBreakWaiters = 1; 
		
		synchronized(waiters) {
			for(WaiterAgent wait : waiters){
				if(!wait.getGui().isBreak()){
					numberOfNoBreakWaiters ++;
				}
			}
		}
		
		if( (waitingCustomers.isEmpty()) && (numberOfNoBreakWaiters > 1)) {
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

