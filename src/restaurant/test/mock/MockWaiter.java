package restaurant.test.mock;


import restaurant.CashierAgent.Check;
import restaurant.interfaces.Waiter;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockWaiter extends Mock implements Waiter {

	String name;

	public MockWaiter(String name) {
		super(name);
		this.name = name;
	}
	
	public void msgHereIsCheck(Check check) {
		Check cpCheck = new Check(check.choice, check.customer, check.waiter, check.tableNumber);
		cpCheck.copyCheck(check);
		
		log.add(new LoggedEvent("Received msgHereIsCheck"));
		check.customer.msgHereIsYourCheck(cpCheck);		
	}	
}
