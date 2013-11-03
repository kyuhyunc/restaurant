package restaurant.test;

import java.util.HashMap;
import java.util.Map;

import restaurant.CashierAgent;
import restaurant.CashierAgent.Check;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockWaiter;
import junit.framework.*;

/**
 * 
 * This class is a JUnit test class to unit test the CashierAgent's basic interaction
 * with waiters, customers, and the host.
 * It is provided as an example to students in CS201 for their unit testing lab.
 *
 * @author Monroe Ekilah
 */
public class CashierTest extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockWaiter waiter;
	MockCustomer customer;
	
	private Map<String, Double> menu = new HashMap<String, Double> ();
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp(); // Q
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");		
		waiter = new MockWaiter("mockwaiter");

		menu.put("Pizza", 8.99);
	}	
	/**
	 * This tests the cashier under very simple terms: one customer is ready to pay the exact bill.
	 */
	public void testOneNormalCustomerScenario()
	{
		//setUp() runs first before this test!
		
		customer.cashier = cashier;//You can do almost anything in a unit test.			
		
		//check preconditions
		assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.checks.size(), 0);		
		assertEquals("CashierAgent should have an empty event log before the Cashier's ComputeBill is called. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
		
		//step 1 of the test : cashier receives check from waiter, and check will be calculated
		cashier.msgComputeBill("Pizza", customer, waiter, 2, menu);//send the message from a waiter to compute a bill

		//check postconditions for step 1 and preconditions for step 2
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.checks.size(), 1);
		
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a waiter), but didn't.", cashier.pickAndExecuteAnAction());
		
		assertEquals(
				"MockWaiter should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		
		// step 2 : check has been computed and will be given back to waiter
		assertTrue("CashierBill should contain a check with state == doneComputing. It doesn't.",
				cashier.checks.get(0).state == Check.CheckState.doneComputing);
		
		assertTrue("CashierBill should contain a bill of price = $8.99. It contains something else instead: $" 
				+ cashier.checks.get(0).getPrice(), cashier.checks.get(0).getPrice() == 8.99);
		
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a waiter), but didn't.", cashier.pickAndExecuteAnAction());
		
		//step 3 of the test : check has been given back to waiter, and in turn, to customer
		//check postconditions for step 3
		assertTrue(
				"MockWaiter should have logged \"Received msgHereIsCheck\". Instead, the MockCustomer's event log reads: "
						+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received msgHereIsCheck"));
		
		assertTrue("MockCustomer should have logged \"Received HereIsYourCheck from waiter\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourCheck"));
		
		//assertTrue("CashierBill should contain a check with state == waitingToBePaid. It doesn't. Instead: "
		//		+ cashier.checks.get(0).state.toString(), cashier.checks.get(0).state == Check.CheckState.waitingToBePaid);
		
		assertTrue("CashierBill should contain a bill with the right customer in it. It doesn't.", 
					cashier.checks.get(0).customer == customer);
		
		//step 4 : cashier receives msgPayment from customer and will return change to the customer
		//check preconditions for step 4 
		assertTrue("Cashier should have logged \"Received msgPayment\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgPayment"));
	
		assertTrue("CashierBill should contain a check with state == receivedCash. It doesn't. Instead: "
				+ cashier.checks.get(0).state.toString(), cashier.checks.get(0).state == Check.CheckState.receivedCash);
		
		//check postconditions for step 4
		assertTrue("Cashier's scheduler should have returned true (needs to react to customer's msgPayment), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		assertTrue("MockCustomer should have logged an event for receiving \"HereIsChange\" with the correct balance, but his last event logged reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received HereIsYourChange from cashier"));
			
		assertTrue(
				"MockCustomer should have change = $1.01. Instead, the MockCustomer has change: "
						+ customer.change.totalAmount(), customer.change.totalAmount() == 1.01);
		
		assertEquals("Cashier should have 0 bills in it. It doesn't.",cashier.checks.size(), 0);
		
		//step 5: 
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
	
	}//end one normal customer scenario
	
	
}
