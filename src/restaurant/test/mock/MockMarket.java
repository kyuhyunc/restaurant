package restaurant.test.mock;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import restaurant.CookAgent.Food;
import restaurant.MarketAgent.Procure;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Market;

/**
 * A Mockmarket built to unit test a CashierAgent.
 *
 * @author kyu
 *
 */
public class MockMarket extends Mock implements Market {

	String name;

	public Timer timer = new Timer();

	public List<Procure> procures = Collections.synchronizedList(new ArrayList<Procure>());
	
	public Map<String, Food> inventory = Collections.synchronizedMap(new HashMap<String, Food> ());
		
	public Cashier cashier;
	
	public double cash = 0;	
	
	public String pattern = ".00";
	public DecimalFormat dFormat = new DecimalFormat(pattern);
		
	public MockMarket(String name) {
		super(name);
		this.name = name;
	}
	
	
	
	public void msgPayment(double cash) {
		this.cash += cash;
		
		log.add(new LoggedEvent("Received msgPayment"));
	}
}
