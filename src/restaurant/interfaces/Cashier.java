package restaurant.interfaces;

import java.util.Map;

import restaurant.CustomerAgent.Cash;
import restaurant.MarketAgent;

/**
 * A sample Cashier interface built to unit test a CashierAgent.
 *
 */
public interface Cashier {
	
	// Sent from waiter to computer a bill for a customer
	public abstract void msgComputeBill(String choice, Customer c, Waiter w, int tableNumber, Map<String, Double> menu);
	
	// Sent from a customer when making a payment
	public abstract void msgPayment(Customer customer, Cash cash);

	public abstract void msgAskForPayment(String food, int batchSize, MarketAgent marketAgent, double price);

	public abstract String getCash();	
}