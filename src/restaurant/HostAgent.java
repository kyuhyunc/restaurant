package restaurant;

import agent.Agent;
import restaurant.gui.HostGui;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostAgent extends Agent {
	static public int NTABLES = 3;//a global for the number of tables.
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	public List<CustomerAgent> waitingCustomers
	= new ArrayList<CustomerAgent>();
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented

	private String name;
	private Semaphore atTable = new Semaphore(0,true);

	public HostGui hostGui = null;
	
	public int CurrentTableNumber = 0;
	public int CurrentSeatNumber = 0;
	
	// made agent status enum for distinguishing whether host is ready or not
	public enum agentStatus { serving, waiting };
	agentStatus hostStatus = agentStatus.waiting; 
	
	public int tableSize = 2;

	public HostAgent(String name) {
		super();

		this.name = name;
		// make some tables
		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}

	public List getWaitingCustomers() {
		return waitingCustomers;
	}

	public Collection getTables() {
		return tables;
	}
	// Messages

	public void msgIWantFood(CustomerAgent cust) {
		// here is where host status changes to serving
		waitingCustomers.add(cust);
		stateChanged();
	}
	
	public void msgReadyToServe() {
		// here is where host status changes to waiting
		hostStatus = agentStatus.waiting;
		stateChanged();
	}

	public void msgLeavingTable(CustomerAgent cust) {
		for (Table table : tables) {
			//if(cust.getTableNumber() == table.tableNumber) {
				for(int z=0 ; z < tableSize ; z++) {
					if(table.getOccupant(z) == cust) {
						print(cust + " leaving " + table);
						//print("[msgLeavingTable]seat number :" + z);
						table.setUnoccupied(z);
						stateChanged();
						break;
					}
					
				}
			//}
		}
			
		
		/**for (Table table : tables) {
			if (table.getOccupant() == cust) {
				print(cust + " leaving " + table);
				table.setUnoccupied();
				stateChanged();
			}
		}*/
	}

	public void msgAtTable() {//from animation
		//print("msgAtTable() called");
		atTable.release();// = true;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
		
		// seatCustomer will not be executed unless host status is waiting
		// if another customer comes into the restaurant, this will not be executed
		// and when host status changes to waiting by message from animation, 
		// this will be executed since stateChanged() has been run, which will in turn 
		// call this method. At this moment, host will get a customer from the list.
		if( hostStatus == agentStatus.waiting ) {
			for (Table table : tables) {
				if (!table.isOccupied()) {
					if (!waitingCustomers.isEmpty()) {
						hostStatus = agentStatus.serving; // change host's status to serving
						seatCustomer(waitingCustomers.get(0), table);//the action
						return true;//return true to the abstract agent to reinvoke the scheduler.
					}
				}
			}
		}
		
		else if ( hostStatus == agentStatus.serving ) { 
			// make some method to wait until HostGui returns the message that host is ready to serve
			// just do nothing
			// return true;
			return false; // this is for making the host to be waiting; sleep until status changes
		}
			
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void seatCustomer(CustomerAgent customer, Table table) {
		
		/**while(!(hostGui.getXPos() == -20 && hostGui.getYPos() == -20)) {
			print("in the while");
		}*/
				
		CurrentTableNumber = table.tableNumber;
		
		for(int i=0;i<tableSize;i++) {
			if( table.getOccupant(i) == null ) {
				CurrentSeatNumber = i;
				break;
			}
		}		
	
		customer.msgSitAtTable();
		DoSeatCustomer(customer, table);
		//table.setOccupant(customer); <-- should have current seat parameter
		try {
			atTable.acquire(); // QQQ two semaphores? in Agent and HostAgent?
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		table.setOccupant(customer);
		waitingCustomers.remove(customer);
		hostGui.DoLeaveCustomer();
	}
	
	

	// The animation DoXYZ() routines
	private void DoSeatCustomer(CustomerAgent customer, Table table) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		print("Seating " + customer + " at the seat #" + (CurrentSeatNumber+1) + " of " + table);
		hostGui.DoBringToTable(customer, table.tableNumber); 
	}

	//utilities

	public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public HostGui getGui() {
		return hostGui;
	}
	
	public void addTableByGui() {
		tables.add(new Table(NTABLES));//how you add to a collections
	}
	

	public class Table {
		private CustomerAgent[] occupiedBy = new CustomerAgent[tableSize];
		//List<CustomerAgent> occupiedBy = new ArrayList<CustomerAgent>();
		//CustomerAgent occupiedBy;
		int tableNumber;
				
		Table(int tableNumber) {
			this.tableNumber = tableNumber;
			for(int q=0;q<tableSize;q++) {
				occupiedBy[q] = null;
				//occupiedBy[i].equals(null);
			}
		}

		void setOccupant(CustomerAgent cust) {
			//occupiedBy = cust;
			//occupiedBy.add(cust);
			/**for(int p=0;p<tableSize;p++) {
				//if(occupiedBy[i].equals(null)) {
				if(occupiedBy[p] == null) {
					occupiedBy[p] = cust;
					break;
				}
			}*/
			occupiedBy[CurrentSeatNumber] = cust;
			//print("[table.setOccupant]table number: " + tableNumber + " & by " + cust);
		}

		void setUnoccupied(int index) {
			//occupiedBy = null;
			occupiedBy[index] = null;
						
			if(occupiedBy[index] == null) {
				//print("[table.setUnoccupied]table number: " + tableNumber +" & seat number: " + index);
			}			
		}

		//List<CustomerAgent> getOccupant() {
		CustomerAgent getOccupant(int index) {
			//print("[table.getOccupant]table number: " + tableNumber +" & seat number: " + index);
			
			return occupiedBy[index];
		}
		
		int occupantsNumber() {
			int OccupantsNumber = 0;			
			
			for(int i=0;i<tableSize;i++) {
				if( occupiedBy[i] != null ) {
				//if( !occupiedBy[i].equals(null) ) {
					OccupantsNumber++;
				}
			}
			
			//print("[occupantsNumber]occupants' number: " + OccupantsNumber);
			return OccupantsNumber;
		}
		
		boolean isOccupied() {
			//return occupiedBy != null;
			if(occupantsNumber() == tableSize) {
				//print("[occupantsNumber]occupants' number: " + occupantsNumber());
				return true;
			}
			else {
				return false;
			}
		}

		public String toString() {
			return "table " + tableNumber;
		}
	}
}

