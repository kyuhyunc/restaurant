package restaurant;

import agent.Agent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import restaurant.CustomerAgent.Cash;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Market;
import restaurant.interfaces.Waiter;
import restaurant.test.mock.LoggedEvent;

/**
 * Restaurant cashier agent.
 */
public class CashierAgent extends Agent implements Cashier {
	private String name;

	// changed from private to public for unit testing
	public List<Check> checks = Collections
			.synchronizedList(new ArrayList<Check>());
	public List<Check> pendingChecks = Collections
			.synchronizedList(new ArrayList<Check>());

	// this is for checks for markets
	public List<Bill> bills = Collections
			.synchronizedList(new ArrayList<Bill>());
	
	private Map<String, Double> menu;
	public double cashTotal = 30;

	public String pattern = ".00";
	public DecimalFormat dFormat = new DecimalFormat(pattern);

	/**
	 * Constructor for CashierAgent class
	 * 
	 * @param name
	 *            name of the customer
	 */
	public CashierAgent(String name) {
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */

	// Messages	
	// Cashier 1a: ComputeBill
	// public void msgComputeBill(String choice, CustomerAgent c, WaiterAgent w,
	// int tableNumber, Map<String, Double> menu) {
	public void msgComputeBill(String choice, Customer c, Waiter w,
			int tableNumber, Map<String, Double> menu) {
		print("Calculating bill");

		boolean askWaiterToPickUpCheck = false;

		for (Check chk : checks) {
			if (chk.state != Check.CheckState.nothing) {
				askWaiterToPickUpCheck = true;
			}
		}

		if (askWaiterToPickUpCheck) {
			pendingChecks.add(new Check(choice, c, w, tableNumber));
		} else {
			checks.add(new Check(choice, c, w, tableNumber));
		}
		this.menu = menu;

		stateChanged();
	}

	// Cashier 4: Payment
	public void msgPayment(Customer customer, Cash cash) {
		synchronized (checks) {
			for (Check c : checks) {
				if (c.customer == customer) {
					c.cash = new Cash(cash.twentyDollar, cash.tenDollar,
							cash.fiveDollar, cash.oneDollar, cash.coins);
					c.state = Check.CheckState.receivedCash;
					if(cash.totalAmount() >= c.price) {
						log.add(new LoggedEvent("Received msgPayment - earn full"));
					}
					else if(cash.totalAmount() == 0) {
						log.add(new LoggedEvent("Received msgPayment - earn 0"));
					}
					else {
						log.add(new LoggedEvent("Received msgPayment - earn partial"));	
					}
					break;
				}
			}
		}
		stateChanged();
	}
	
	// message from market to ask for payment
	public void msgAskForPayment(String food, int orderedSize, Market market, double price) {
		bills.add(new Bill(food, orderedSize, market, price));

		stateChanged();
		
		log.add(new LoggedEvent("Received msgAskForPayment"));
	}

	/**
	 * Scheduler. Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		synchronized (checks) {
			if (!checks.isEmpty()) {
				for (int i = 0; i < checks.size(); i++) {
					if (checks.get(i).state == Check.CheckState.nothing) {
						checks.get(i).state = Check.CheckState.computing;
						ComputeBill(checks.get(i));
						return true;
					} else if (checks.get(i).state == Check.CheckState.doneComputing) {
						giveCheckToWaiter(checks.get(i));
						return true;
					} else if (checks.get(i).state == Check.CheckState.receivedCash) {
						checks.get(i).state = Check.CheckState.paid;
						returnChange(checks.get(i));
						// checks.remove(i);
						return true;
					}
				}
			}
		}

		synchronized (pendingChecks) {
			if (!pendingChecks.isEmpty()) {
				for (Check chk : pendingChecks) {
					checks.add(chk);
				}
				pendingChecks.clear();
				return true;
			}
		}

		synchronized (bills) {
			if (!bills.isEmpty()) {
				for (Bill b : bills) {
					if (b.state == Bill.BillState.nothing) {
						b.state = Bill.BillState.inProcess;
						PayBill(b);
						return true;
					} else if (b.state == Bill.BillState.done) {
						bills.remove(b);
						return true;
					}
				}
			}
		}
		return false;
	}

	// Actions
	public void ComputeBill(Check c) {
		// should here have timer for setting the price..?
		c.setPrice(menu.get(c.choice));
		c.state = Check.CheckState.doneComputing;

		Do("Price is " + c.price);
	}

	// private to public for testing
	public void giveCheckToWaiter(Check check) {
		// Deep copying check to cpCheck
		Check cpCheck = new Check(check.choice, check.customer, check.waiter,
				check.tableNumber);
		cpCheck.copyCheck(check);

		check.state = Check.CheckState.waitingToBePaid;
		check.waiter.msgHereIsCheck(cpCheck);
	}

	private void returnChange(Check c) {
		int twentyDollar = 0;
		int tenDollar = 0;
		int fiveDollar = 0;
		int oneDollar = 0;
		int coins = 0;

		if(c.cash.totalAmount() >= c.price) {
			cashTotal += c.price;

			double change = c.cash.totalAmount() - c.price;
			Cash Change;
	
			twentyDollar = (int) change / 20;
			change -= 20 * twentyDollar;
	
			tenDollar = (int) change / 10;
			change -= 10 * tenDollar;
	
			fiveDollar = (int) change / 5;
			change -= 5 * fiveDollar;
	
			oneDollar = (int) change / 1;
			change -= 1 * oneDollar;
	
			coins = (int) (100 * ((double) (change) + 0.0001));
	
			Change = new Cash(twentyDollar, tenDollar, fiveDollar, oneDollar, coins);
	
			Do("Change is " + dFormat.format(Change.totalAmount()));

			c.customer.msgChange(Change);
		}
		else {
			cashTotal += c.cash.totalAmount();
		}
			
		checks.remove(c);
		
		// check bills if cashier earns money
		double sum = 0;
		for(Bill b : bills) {
			if(b.state == Bill.BillState.unPaid) {
				sum += (b.price*b.orderedSize);
				if(cashTotal >= sum) {
					log.add(new LoggedEvent("Now have enough money to pay"));
					Do("I can now pay to the " + b.market + " for the order " + b.food + " [extra creidt]");
					b.state = Bill.BillState.nothing;
				}
				else {
					break;
				}
			}
		}
		
	}

	private void PayBill(Bill b) {
		//Bill b = bills.get(0);
		if(cashTotal >= (b.price*b.orderedSize)) {
			cashTotal -= b.price * b.orderedSize;

			b.market.msgPayment(b.price * b.orderedSize);

			b.state = Bill.BillState.done;
		}
		else {
			log.add(new LoggedEvent("Don't have enough money to pay"));
			Do("I don't have enough money to pay to the " + b.market + " for the order " + b.food + " [extra creidt]");
			b.state = Bill.BillState.unPaid;
		}
	}

	// Accessors, etc.

	public String getName() {
		return name;
	}

	public String toString() {
		return "cook " + getName();
	}

	public String getCash() {
		return dFormat.format(cashTotal);
	}

	public String getTotalDebt() {
		double totalDebt = 0;
		
		for(Bill b : bills) {
			totalDebt += b.price;
		}
		
		return dFormat.format(totalDebt);
	}
	
	
	public static class Check {
		// changed to public for testing
		public String choice;
		// CustomerAgent customer;
		public Customer customer;
		public Waiter waiter;
		public int tableNumber;
		public double price;

		Cash cash; // from customer

		// Check (String choice, CustomerAgent customer, WaiterAgent waiter, int
		// tableNumber) {
		public Check(String choice, Customer customer, Waiter waiter,
				int tableNumber) {
			this.choice = choice;
			this.customer = customer;
			this.waiter = waiter;
			this.tableNumber = tableNumber;
		}

		public enum CheckState {
			nothing, computing, doneComputing, waitingToBePaid, receivedCash, paid
		};

		public CheckState state = CheckState.nothing;

		public void setPrice(double price) {
			this.price = price;
		}

		public double getPrice() {
			return price;
		}

		public Cash getCash() {
			return cash;
		}

		public void copyCheck(Check check) {
			this.price = check.price;
			Cash c = check.cash;
			if (c != null) {
				this.cash = new Cash(c.twentyDollar, c.tenDollar, c.fiveDollar,
						c.oneDollar, c.coins);
			}
		}
	}

	public static class Bill {
		private String food;
		private int orderedSize;
		private Market market;
		private double price;

		//private double totalPrice;

		public enum BillState {
			nothing, inProcess, unPaid, done
		};

		public BillState state = BillState.nothing;

		public Bill(String food, int orderedSize, Market market, double price) {
			this.food = food;
			this.orderedSize = orderedSize;
			this.market = market;
			this.price = price;

			//totalPrice = (price / 2) * orderedSize;
		}
	}
}
