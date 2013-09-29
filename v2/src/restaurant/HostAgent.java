package restaurant;

import agent.Agent;
import restaurant.gui.RestaurantGui;
import restaurant.gui.WaiterGui;
import restaurant.WaiterAgent;

import java.util.*;

/**
 * Restaurant Host Agent
 */
public class HostAgent extends Agent {
	static public int NTABLES = 3;//a global for the number of tables.
	static public int NWAITERS = 1;
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	
	public List<CustomerAgent> waitingCustomers	= new ArrayList<CustomerAgent>();
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented
	public List<WaiterAgent> waiters = new ArrayList<WaiterAgent>();
	
	private CookAgent cook = new CookAgent("Cook");
	
	public RestaurantGui gui;
	
	private String name;

	public HostAgent(String name) {
		super();

		this.name = name;
		
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix)); //how you add to a collections
		}		
		
		for (int i=0;i<NWAITERS;i++) {
			WaiterAgent w = new WaiterAgent("waiter #"+i);
			w.setHost(this);
			w.setCook(cook);
			
			WaiterGui g = new WaiterGui(w);
			w.setGui(g);
			
			waiters.add(w);
			w.startThread();
		}
		
		cook.startThread();
	}

	// Messages

	// 1: IWantFood(customer)
	public void msgIWantFood(CustomerAgent cust) {
		waitingCustomers.add(cust);
		Do(cust + " is added to the waiting list");
		stateChanged();
	}

	// 11: TableIsCleared(table)
	public void msgTableIsCleared(Table table) {
		print("table #" + table.tableNumber + " is cleared");
		table.setUnoccupied();
		stateChanged(); // so that when a customer leaves, host will check availability of tables again
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Table table : tables) {
			if (!table.isOccupied()) {
				if (!waitingCustomers.isEmpty()) {
					tellWaiter(waitingCustomers.get(0), table);
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
			}
		}
		return false;
	}

	// Actions
	private void tellWaiter(CustomerAgent customer, Table table) {
		int waiterNumber = 0;
		int customerSize= 0;
		
		if(waiters.size() > 0){
			customerSize = waiters.get(waiterNumber).getMyCustomers().size();
			
			// choosing a waiter that has the least number of customers in the list
			for(int i=0;i<waiters.size();i++) {
				if(waiters.get(i).state == WaiterAgent.AgentState.Waiting) {
					if(customerSize > waiters.get(i).getMyCustomers().size()) {
						waiterNumber = i;
					}
				}
			}

			table.setOccupant(customer);
			waiters.get(waiterNumber).msgSitAtTable(customer, table);
			customer.setWaiter(waiters.get(waiterNumber));
			waitingCustomers.remove(customer);		
		}
		else {
			Do("There is no waiter!");
		}
	}
	

	//utilities
	public void setRestaurantGui(RestaurantGui gui) {
		this.gui = gui;
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
	
	public void addWaiterByGui() {
		WaiterAgent w = new WaiterAgent("waiter #"+ (NWAITERS-1));
		w.setHost(this);		
		w.setCook(cook);
				
		WaiterGui g = new WaiterGui(w);
		w.setGui(g);
				
		waiters.add(w);
		
		w.startThread();		
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

