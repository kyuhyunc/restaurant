package restaurant.test.mock;


import restaurant.interfaces.Cook;

/**
 * A MockCook built to unit test a CashierAgent.
 *
 * @author kyu
 *
 */
public class MockCook extends Mock implements Cook {

	String name;
	public MockCook(String name) {
		super(name);
		this.name = name;
	}
}
