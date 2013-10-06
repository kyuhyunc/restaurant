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
	//static public int NWAITERS = 1;
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	
	public enum AgentState
	{Waiting, Serving};
	public AgentState state = AgentState.Waiting;//The start state
	
	public List<WaiterAgent> waiters = new ArrayList<WaiterAgent>();
	public List<CustomerAgent> waitingCustomers	= new ArrayList<CustomerAgent>();
	public CookAgent cook;
	
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented	
	public Collection<Table> tables;
		
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
	}

	// Messages
	// 0:
	public void msgAddWaiter(WaiterAgent waiter) {
		Do("New waiter " + waiter.getName() + " is added");				
		
		waiters.add(waiter);	
		//waiterAdd.release();
		//waiterBreak.release();
		stateChanged();
	}
	
	// 1: IWantFood(customer)
	public void msgIWantFood(CustomerAgent cust) {
		waitingCustomers.add(cust);
		Do(cust + " is added to the waiting list");
		//flag = true;
		stateChanged();
	}

	// 11: TableIsCleared(table)
	public void msgTableIsCleared(Table table) {
		print("table #" + table.tableNumber + " is cleared");
		table.setUnoccupied();
		stateChanged(); // so that when a customer leaves, host will check availability of tables again
	}
	
	// receiving msg when waiter's state becomes waiting
	public void msgReadyToServe() {
		stateChanged();
	}

	// msg from waiter
	public void msgCanIBreak(WaiterAgent w) {
		chkIfWaiterCanBreak(w);		
	}
	
	public void msgOffBreak() {
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		for (Table table : tables) {
			if (!table.isOccupied()) {
				if (!waitingCustomers.isEmpty()) {
					if(waiters.size() > 0) {
						for(WaiterAgent w : waiters) {
							if(!w.getGui().isBreak()) {
								if(w.state == WaiterAgent.AgentState.Waiting){
									tellWaiter(waitingCustomers.get(0), table);
									return true;//return true to the abstract agent to reinvoke the scheduler.
								}
							}	
						}
						//Do("All waiters are serving customers");
						return false;
					}
					else {
						Do("There is no waiter");
						return false;
					}					
				}
			}
		}
		return false;
	}

	// Actions
	private void tellWaiter(CustomerAgent customer, Table table) {

		WaiterAgent w;
		
		w = getTheMostFreeWaiter();
		
		if(w!=null) {
			table.setOccupant(customer);
			w.msgSitAtTable(customer, table);
			customer.setWaiter(w);
			waitingCustomers.remove(customer);
		}
		else
			Do("No waiter available");
	}
	
	public void chkIfWaiterCanBreak(WaiterAgent w) {
		boolean breakPermission;
		
		int numberOfNoBreakWaiters = 1;
		
		for(WaiterAgent wait : waiters){
			if(!wait.getGui().isBreak()){
				numberOfNoBreakWaiters ++;
			}
		}
		
		if( (waitingCustomers.isEmpty()) && (numberOfNoBreakWaiters > 1)) {
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
	
	public WaiterAgent getTheMostFreeWaiter() {
		List<WaiterAgent> availableWaiters = new ArrayList<WaiterAgent>(); 
		int numberOfCustomers = -1;
		int waiterNumber = -1;
		
		// this for loop is for saving the first customersize and waiternumber to compare with others
		for(int i=0;i<waiters.size();i++) {
			if(!waiters.get(i).getGui().isBreak()) {
				if(waiters.get(i).state == WaiterAgent.AgentState.Waiting) {
					availableWaiters.add(waiters.get(i));
				}
			}
		}
		
		if(!availableWaiters.isEmpty()) {
			numberOfCustomers = availableWaiters.get(0).getMyCustomers().size();
			waiterNumber = 0;
			for(int i=0; i < availableWaiters.size() ; i++) {
				if( numberOfCustomers >= availableWaiters.get(i).getMyCustomers().size()) {
					waiterNumber = i; 
				}				
			}
			return availableWaiters.get(waiterNumber);
		}
		
		return null;		
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

