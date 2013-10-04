package restaurant;

import agent.Agent;
import restaurant.gui.RestaurantGui;
import restaurant.WaiterAgent;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Host Agent
 */
public class HostAgent extends Agent {
	static public int NTABLES = 3;//a global for the number of tables.
	//static public int NWAITERS = 1;
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	
	public List<WaiterAgent> waiters = new ArrayList<WaiterAgent>();
	public List<CustomerAgent> waitingCustomers	= new ArrayList<CustomerAgent>();
	public CookAgent cook;
	
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented	
	public Collection<Table> tables;
		
	public RestaurantGui gui;
	
	private String name;
	
	private Semaphore waiterAdd = new Semaphore(0,true);
	//private Semaphore waiterBreak = new Semaphore(0,true);
	
	boolean flag = true;
	
	public HostAgent(String name) {
		super();

		this.name = name;
		
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix)); //how you add to a collections			
		}		
	}

	// Messages
	// 0:
	public void msgAddWaiter(WaiterAgent waiter) {
		Do("New waiter " + waiter.getName() + " is added");				
		
		waiters.add(waiter);	
		waiterAdd.release();
		//waiterBreak.release();
	}
	
	// 1: IWantFood(customer)
	public void msgIWantFood(CustomerAgent cust) {
		waitingCustomers.add(cust);
		Do(cust + " is added to the waiting list");
		flag = true;
		stateChanged();
	}

	// 11: TableIsCleared(table)
	public void msgTableIsCleared(Table table) {
		print("table #" + table.tableNumber + " is cleared");
		table.setUnoccupied();
		stateChanged(); // so that when a customer leaves, host will check availability of tables again
	}

	// msg from waiter
	public void msgCanIBreak(WaiterAgent w) {
		chkIfWaiterCanBreak(w);		
	}
	
	public void msgOffBreak() {
		//waiterBreak.release();
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
		int waiterNumber = -1;
		int customerSize = -1;

		
		if(waiters.size() > 0){
			// this for loop is for saving the first customersize and waiternumber to compare with others
			for(int i=0;i<waiters.size();i++) {
				if(!waiters.get(i).getGui().isBreak()) {
					waiterNumber = -2;
					if(waiters.get(i).state == WaiterAgent.AgentState.Waiting) {
						customerSize = waiters.get(i).getMyCustomers().size();
						waiterNumber = i;
						break;
					}
				}
			}
			
			// choosing a waiter that has the least number of customers in the list
			for(int i=0;i<waiters.size();i++) {
				if(!waiters.get(i).getGui().isBreak()) {
					if(waiters.get(i).state == WaiterAgent.AgentState.Waiting) {
						// customerSize == -1 is for when state changes to waiting after the first loop above, due to race condition
						// customerSize == -2 is for when isBreak changes after the first loop above, due to race condition
						if(customerSize >= waiters.get(i).getMyCustomers().size() || customerSize == -1 || customerSize == -2) {
							customerSize = waiters.get(i).getMyCustomers().size();
							waiterNumber = i;
						}
					}
				}
			}
			
			if(waiterNumber == -1) { // when all waiter are on break 
				//Do("aaaaaaaaaaaa");
				if(flag) {
					Do("There is no waiter available; they are all on break");
					flag = false;
				}	
			}
			else if(waiterNumber == -2){ // when all waiter serving, although  there are waiters not on break
				// using flag?
				if(flag) {
					Do("There is no waiter available; they are all serving customers");
					flag = false;
				}
			}
			else {
				flag = true;
				table.setOccupant(customer);
				waiters.get(waiterNumber).msgSitAtTable(customer, table);
				customer.setWaiter(waiters.get(waiterNumber));
				waitingCustomers.remove(customer);
			}
		}
		else {
			Do("There is no waiter!");
			try{
				waiterAdd.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	public void chkIfWaiterCanBreak(WaiterAgent w) {
		boolean breakPermission;
		
		if( waitingCustomers.isEmpty() ) {
			breakPermission = true;
			/**try{
				waiterBreak.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
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

