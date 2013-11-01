package restaurant.test;

import java.util.HashMap;
import java.util.Map;

import restaurant.CashierAgent;
import restaurant.CashierAgent.Bill;
import restaurant.test.mock.MockCook;
import restaurant.test.mock.MockMarket;
import junit.framework.*;
import restaurant.CookAgent.Food;

/** 
 * This test case is for testing the second scenario in milestone v2.2a
 * 
 * @author Kyu
 */

public class CashierTest2 extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockMarket market1;
	MockMarket market2;
	MockCook cook;
	
	private Map<String, Double> menu = new HashMap<String, Double> ();
	
	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp(); // Q
		cashier = new CashierAgent("cashier");		
		market1 = new MockMarket("mockMarket1");
		market2 = new MockMarket("mockMarket2");
		cook = new MockCook("mockCook");
		
		menu.put("Pizza", 8.99);
	}	
	/**
	 * This tests the cashier under very simple terms: one customer is ready to pay the exact bill.
	 */
	public void testOneNormalCustomerScenario()
	{
		//setUp() runs first before this test!
		
		market1.cashier = cashier;	
		market1.inventory.put("Pizza", new Food("Pizza"));
		market1.inventory.get("Pizza").price = 4;
		
		market2.cashier = cashier;	
		market2.inventory.put("Pizza", new Food("Pizza"));
		market2.inventory.get("Pizza").price = 4;
				
		//check preconditions
		assertEquals("Cashier should have 0 bills in it, that needs to be paid to market. It doesn't.",cashier.bills.size(), 0);		
		assertEquals("CashierAgent should have an empty event log before the Cashier's askForPayment is called from the market. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
				
		//step 1 of the test : cashier receives bill from the market
		//void msgAskForPayment(String food, int orderedSize, Market market, double price)
		cashier.msgAskForPayment("Pizza", 2, market1, 4);//send the message from a market to cashier to make the cashier to pay bill
		cashier.msgAskForPayment("Pizza", 1, market2, 4);//send the message from a market to cashier to make the cashier to pay bill
				
		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgAskForPayment\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgAskForPayment"));
		
		assertEquals("MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ market1.log.toString(), 0, market1.log.size());
		assertEquals("MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ market2.log.toString(), 0, market2.log.size());

		assertEquals("Cashier should have 2 bill in it. It doesn't.", cashier.bills.size(), 2);
		
		// step 2 - 1
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());

		assertTrue(
				"MockMarket should have logged \"Received msgPayment\". Instead, the MockMarket's event log reads: "
						+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgPayment"));
		
		assertTrue("CashierBill should contain a bill with state == done. It doesn't. Instead: "
				+ cashier.bills.get(0).state.toString(), cashier.bills.get(0).state == Bill.BillState.done);
		
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());
		
		assertTrue("Market1 should have cash = $8. It contains something else instead: $" 
				+ market1.cash, market1.cash == 8);
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", cashier.bills.size(), 1);
		
		// step 2 - 2
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());
		
		assertTrue(
				"MockMarket should have logged \"Received msgPayment\". Instead, the MockMarket's event log reads: "
						+ market2.log.getLastLoggedEvent().toString(), market2.log.containsString("Received msgPayment"));
		
		assertTrue("CashierBill should contain a bill with state == done. It doesn't. Instead: "
				+ cashier.bills.get(0).state.toString(), cashier.bills.get(0).state == Bill.BillState.done);
		
		assertTrue("Cashier's scheduler should have returned true (no actions to do on a bill from a market), but didn't.", cashier.pickAndExecuteAnAction());

		assertTrue("Market2 should have cash = $4. It contains something else instead: $" 
				+ market2.cash, market2.cash == 4);
		
		assertEquals("Cashier should have 0 bill in it. It doesn't.", cashier.bills.size(), 0);
				
		// step 4
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
	
	}//end one normal customer scenario
	
	
}
