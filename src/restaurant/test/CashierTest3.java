package restaurant.test;

import java.util.HashMap;
import java.util.Map;

import restaurant.CashierAgent;
import restaurant.CashierAgent.Bill;
import restaurant.CashierAgent.Check;
import restaurant.CustomerAgent.Cash;
import restaurant.test.mock.MockCook;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockMarket;
import restaurant.test.mock.MockWaiter;
import junit.framework.*;
import restaurant.CookAgent.Food;

/** 
 * This test case is for testing extra credit scenario in milestone v2.2a
 * 
 * @author Kyu
 */

public class CashierTest3 extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockMarket market;	
	MockCook cook;
	MockCustomer customer;
	MockWaiter waiter;
	
	private Map<String, Double> menu = new HashMap<String, Double> ();
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp(); // Q
		cashier = new CashierAgent("cashier");		
		market = new MockMarket("mockMarket");
		cook = new MockCook("mockCook");
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
		
		market.cashier = cashier;	
		market.inventory.put("Pizza", new Food("Pizza"));
		market.inventory.get("Pizza").price = 4;
		cashier.cashTotal = 0;

				
		//check preconditions
		assertEquals("Cashier should have 0 bills in it, that needs to be paid to market. It doesn't.",cashier.bills.size(), 0);		
		assertEquals("CashierAgent should have an empty event log before the Cashier's askForPayment is called from the market. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
				
		//step 1 of the test : cashier receives bill from the market
		//void msgAskForPayment(String food, int orderedSize, Market market, double price)
		cashier.msgAskForPayment("Pizza", 2, market, 4);//send the message from a market to cashier to make the cashier to pay bill
				
		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgAskForPayment\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgAskForPayment"));
		
		assertEquals("MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ market.log.toString(), 0, market.log.size());

		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.bills.size(), 1);
		
		// step 2
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());

		assertTrue("Cashier should have logged \"Don't have enough money to pay\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Don't have enough money to pay"));
		
		assertFalse("Cashier's scheduler should have returned false (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.bills.size(), 1);

		// step 3
		
		cashier.checks.add(new Check("Steak", customer, waiter, 0));
		cashier.checks.get(0).setPrice(20);
		cashier.msgPayment(customer, new Cash(1,1,1,1,1));
		
		assertTrue("Cashier should have logged \"Received msgPayment\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgPayment"));
		
		assertTrue("CashierBill should contain a check with state == receivedCash. It doesn't. Instead: "
				+ cashier.checks.get(0).state.toString(), cashier.checks.get(0).state == Check.CheckState.receivedCash);
	
		// step 4		
		assertTrue("Cashier's scheduler should have returned true (needs to react to customer's msgPayment), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		assertTrue("Cashier should have logged \"Now have enough money to pay\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Now have enough money to pay"));
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.bills.size(), 1);

		// step 5		
		assertTrue("Cashier's scheduler should have returned true (needs to react to customer's msgPayment), but didn't.", 
				cashier.pickAndExecuteAnAction());
				
		assertTrue(
				"MockMarket should have logged \"Received msgPayment\". Instead, the MockMarket's event log reads: "
						+ market.log.getLastLoggedEvent().toString(), market.log.containsString("Received msgPayment"));
		
		assertTrue("CashierBill should contain a bill with state == done. It doesn't. Instead: "
				+ cashier.bills.get(0).state.toString(), cashier.bills.get(0).state == Bill.BillState.done);
		
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());
		
		assertTrue("Market should have cash = $8. It contains something else instead: $" 
				+ market.cash, market.cash == 8);

		assertTrue("Cashier should have cash = $12. It contains something else instead: $" 
				+ cashier.cashTotal, cashier.cashTotal == 12);
		
		assertEquals("Cashier should have 0 bill in it. It doesn't.", cashier.bills.size(), 0);
						
		// step 6
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
	
	}//end one normal customer scenario
	
	
}
