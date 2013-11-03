package restaurant.test.mock;


import restaurant.CashierAgent.Check;
import restaurant.CustomerAgent.Cash;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockCustomer extends Mock implements Customer {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	public Cashier cashier;
		
	String name;
	
	public Cash cash;
	public Cash payment;
	public Cash change;
	
	
	public MockCustomer(String name) {
		super(name);
		this.name = name;
		
		// hack if you want to change budget of customer
	}

	@Override
	public void msgHereIsYourCheck(Check check) {
		double total = check.getPrice();

		//Check cpCheck = new Check(check.choice, check.customer, check.waiter, check.tableNumber);
		
		log.add(new LoggedEvent("Received HereIsYourCheck from waiter. Total = "+ total));
			
		if(this.name.toLowerCase().contains("thief")){
			//test the non-normative scenario where the customer has no money if their name contains the string "theif"
			//cashier.IAmShort(this, 0);
			cashier.msgPayment(this, new Cash(0,0,0,0,0));
		}
		else if(this.name.toLowerCase().contains("poor")){
			//test the non-normative scenario where the customer has no money if their name contains the string "theif"
			//cashier.IAmShort(this, 0);
			cashier.msgPayment(this, new Cash(0,0,1,0,0));
		}
		else {
			//test the normative scenario
			payment = cash.payCash(total);
			cashier.msgPayment(this, payment);
		}
	}

	@Override
	//public void HereIsYourChange(double total) {
	public void msgChange(Cash cash) {
		change = cash;
		log.add(new LoggedEvent("Received HereIsYourChange from cashier. Change = "+ cash.totalAmount()));
	}
}
