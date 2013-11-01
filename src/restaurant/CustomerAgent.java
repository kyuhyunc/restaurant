package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.FoodGui;
import restaurant.interfaces.Customer;
import agent.Agent;
import restaurant.CashierAgent.Check;
import restaurant.WaiterAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent implements Customer {
	private String name;
	private int hungerLevel = 1;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	private FoodGui foodGui;

	// agent correspondents
	private HostAgent host;
	private WaiterAgent wait;
	private CashierAgent cashier;
	private int tableNumber;
	
	//Map<String, Food> menu;
	private List<String> menu_list = new ArrayList<String> ();
	private Map<String, Double> menu = new HashMap<String, Double> ();
	private String choice;
	private Check check;
	private Cash cash;
	
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, TableFull, BeingSeated, Seated, 
		WaitingFood, Eating, DoneEating, LeavingTable, DonePayment, LeavingRestaurant};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, tableFull, decidedToWait, followHost, seated, callWaiterToOrder, 
	makeOrder, getFood, doneEating, askForCheck, getCheck, payment, leaveRestaurant
	, doneLeaving, reOrder};
	AgentEvent event = AgentEvent.none;

	boolean waitWhenTableFull = false;;
	int orderCount;
	
	String pattern = ".00";
	DecimalFormat dFormat = new DecimalFormat(pattern);
	
	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
	public CustomerAgent(String name){
		super();
		this.name = name;
	
		// hack if you want to change budget of customer
		cash = new Cash(1,1,1,1,0);
	}

	// Messages

	// 0: IamHungry()
	public void gotHungry() { //from animation
		orderCount = 0;
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}

	// message when tables are full from host
	public void msgWhetherLeave() {
		event = AgentEvent.tableFull;
		stateChanged();
	}
		
	// 3: FollowMe(menu)
	public void msgFollowMe(List<String> menu_list, Map<String, Double> menu, int tableNumber) {
		this.menu_list.clear();
		this.menu.clear();		
		this.menu_list.addAll(menu_list);
		this.menu.putAll(menu);
		this.tableNumber = tableNumber;
		event = AgentEvent.followHost;
		stateChanged();		
	}

	// messages from gui
	public void msgAnimationFinishedGoToSeat() {
		//from animation
		event = AgentEvent.seated;
		stateChanged();
	}
	
	// 5: WhatWouldYouLike()
	// OutOfFood 0: WhatWouldYouLike
	public void msgWhatWouldYouLike() {
		event = AgentEvent.makeOrder;
		stateChanged();
	}
	
	// OutOfFood 4:WhatWouldYouLikeAgain
	public void msgAskForOrderAgain(List<String> menu_list) {
		// update menu that out of stocked food is not included
		this.menu_list.clear();
		this.menu_list.addAll(menu_list); // deep copy
		
		// check menu list
		event = AgentEvent.reOrder;
		stateChanged();
	}
	
	// 9: HereIsYourOrder() 
	public void msgHereIsYourOrder() {
		event = AgentEvent.getFood;
		stateChanged();
	}
	
	// Cashier 2: HereIsCheck
	public void msgHereIsYourCheck(Check check) {
		event = AgentEvent.getCheck;
		this.check = check;
		foodGui.setPrice(check.price);
		stateChanged();
	}
	
	// message from gui
	public void msgArrivedAtCashier() {
		event = AgentEvent.payment;
		stateChanged();
	}
	
	// Cashier 5: Change
	public void msgChange(Cash cash) {
		this.cash.addChanges(cash);		
		event = AgentEvent.leaveRestaurant;
		stateChanged();
	}
	
	// messages from gui
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
		waitWhenTableFull = false;
		state = AgentState.DoingNothing;
		event = AgentEvent.doneLeaving;
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine
		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry ){
			state = AgentState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		else if (state == AgentState.WaitingInRestaurant && event == AgentEvent.tableFull ){
			state = AgentState.TableFull;
			thinkWhetherLeave();
			return true;			
		}		
		else if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followHost ){
			state = AgentState.BeingSeated;
			SitDown();
			return true;
		}
		else if (state == AgentState.BeingSeated && event == AgentEvent.seated){
			state = AgentState.Seated;
			ChooseMenu();
			return true;
		}
		else if (state == AgentState.Seated && event == AgentEvent.makeOrder){
			state = AgentState.WaitingFood;
			HereIsMyChoice(choice);
			return true;
		}
		else if (state == AgentState.WaitingFood && event == AgentEvent.reOrder){
			// Go back to previous step to go over the process again
			state = AgentState.Seated;
			ChooseMenu();
			return true;
		}
		else if (state == AgentState.WaitingFood && event == AgentEvent.getFood){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		else if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.DoneEating;
			ReadyForCheck();
			return true;
		}
		else if (state == AgentState.DoneEating && event == AgentEvent.getCheck){
			state = AgentState.LeavingTable;
			leaveTable();
			return true;
		}
		else if (state == AgentState.LeavingTable && event == AgentEvent.payment) {
			state = AgentState.DonePayment;
			Payment();
			return true;
		}
		else if (state == AgentState.DonePayment && event == AgentEvent.leaveRestaurant) {
			state = AgentState.LeavingRestaurant;
			exitRestaurant();
			return true;
		}		
		return false;
	}

	// Actions
	private void goToRestaurant() {
		Do("Going to restaurant");
		host.msgIWantFood(this); //send our instance, so he can respond to us
	}

	private void thinkWhetherLeave() {
		Random oRandom = new Random();
		int randomNum;
		
		// non-norm #3: 
		// Customer comes to restaurant and restaurant is full, customer is told and waits.
		// Customer comes to restaurant and restaurant is full, customer is told and leaves.
		// 40% chance to leave the restaurant 
		randomNum = oRandom.nextInt(5);
		if(randomNum == 0 || randomNum == 2) {
			Do("Full? I will come next time then!");
			exitRestaurant();
			waitWhenTableFull = false;
		}
		else {
			Do("Full? I can wait =) ");
			waitWhenTableFull = true;
			state = AgentState.WaitingInRestaurant;
			event = AgentEvent.decidedToWait;
		}
		host.msgDecision(this);
	}
	
	private void SitDown() {
		Do("Being seated. Going to table");
	
		customerGui.DoGoToSeat(tableNumber);		
	}
	
	private void ChooseMenu() {
		// this is for reset the foodGui
		if(!(foodGui == null)){
			foodGui.state = FoodGui.State.reOrdering;
		}
		
		Random oRandom = new Random();
		int randomNum;
		
		orderCount ++;
				
		// Customers who have only enough money to order the cheapest item will leave if that item is out of stock
		if ( orderCount > 1) {
			if(menu_list.size() >= 1) {
				if((!menu_list.contains(choice))) {
					if((cash.totalAmount() < FirstCheapestFood()) && cash.totalAmount() >= menu.get(choice)) {
						Do("I have only enough money to order the cheapest food, but if there is no stock I will leave");
						wait.msgLeavingTable(this);
						state = AgentState.DoingNothing;
						exitRestaurant();
						
						return;
					}
				}
			}			
		}	
			
		Do("Choosing menu");
		
		if(menu_list.size() == 0) {
			Do("There is nothing available");
			
			wait.msgLeavingTable(this);
			state = AgentState.DoingNothing;
			exitRestaurant();		
		}
		else {						
			// non-norm #1: customer leaves if all food is too expensive 
			// 250 chance to leave the restaurant --> disabled as it is not a requirement according to the rubric 
			randomNum = oRandom.nextInt(5);
			if(randomNum == -1) {
				Do("All food is too expensive, I will come later again.");
				wait.msgLeavingTable(this);
				state = AgentState.DoingNothing;
				exitRestaurant();						
			}
			else { // 80% chance to order food
				// Customers who have no money to order anything:
				if((cash.totalAmount() < FirstCheapestFood())) {
					randomNum = oRandom.nextInt(5);
					// sometimes choose to leave. (40%)
					if((randomNum == 0 || randomNum == orderCount%5)) {
						Do("I don't have enough money to buy anything");
						
						wait.msgLeavingTable(this);
						state = AgentState.DoingNothing;
						exitRestaurant();
					}
					// sometimes just order. (60%)
					else {
						// algorithm for choose what to order
						randomNum = oRandom.nextInt(menu_list.size());				
						choice = menu_list.get(randomNum);
						
						// hack!! for testing.
						// customer's name will be his/her choice in case name matches with menu_list
						if(menu_list.contains(name)) {
							System.out.println("Hack =) by Kyu");
							choice = name;
						}
						
						ReadyToOrder();
					}
				}
				// if customers have enough money, order food.
				else {					
					do {
						// algorithm for choose what to order
						randomNum = oRandom.nextInt(menu_list.size());	
						choice = menu_list.get(randomNum);
					// Customers who have only enough money to order the cheapest item will order the cheapest food
					} while( cash.totalAmount() < menu.get(choice) );
					
					// hack!! for testing.
					// customer's name will be his/her choice in case name matches with menu_list
					if(menu_list.contains(name)) {
						if(cash.totalAmount() >= menu.get(name)) {
							System.out.println("***** Hack!!! (by Q)");
							choice = name;
						}
					}
					
					// Customers who have only enough money to order the cheapest item will order the cheapest food
					if(menu_list.size() == 1) {
						Do(choice + " is the cheapest food I can order out of " + menu_list.size() + " foods");
					}
					else {
						if(cash.totalAmount() >= FirstCheapestFood() && cash.totalAmount() < SecondCheapestFood()) {
							Do(choice + " is the cheapest food I can order out of " + menu_list.size() + " foods");
						}
					}
										
					ReadyToOrder();
				}
			}
		}
	}
	
	private void ReadyToOrder() {
		Do("Ready To Order");
		wait.msgReadyToOrder(this);
	}
	
	private void HereIsMyChoice(String choice) {
		Do("Here Is My Choice : " + choice);
		
		foodGui = new FoodGui(tableNumber, wait.getFood(choice), 1);
		host.gui.animationPanel.addGui(foodGui);
		foodGui.state = FoodGui.State.waiting;
		
		wait.msgHereIsMyChoice(this, choice);
	}

	private void EatFood() {
		Do("Eating Food");
		//This next complicated line creates and starts a timer thread.
		//We schedule a deadline of getHungerLevel()*1000 milliseconds.
		//When that time elapses, it will call back to the run routine
		//located in the anonymous class created right there inline:
		//TimerTask is an interface that we implement right there inline.
		//Since Java does not all us to pass functions, only objects.
		//So, we use Java syntactic mechanism to create an
		//anonymous inner class that has the public method run() in it.
			
		timer.schedule(new TimerTask() {
			public void run() {
				print("Done eating, " + choice);
				event = AgentEvent.doneEating;
				foodGui.state = FoodGui.State.doneEating;
				stateChanged();
			}
		},
		wait.getFood(choice).getEatingTime());
	}
	
	private void ReadyForCheck() {
		Do("Ask for check to waiter");
		wait.msgReadyForCheck(this);
	}

	private void leaveTable() {
		foodGui.state = FoodGui.State.goToCashier;
		foodGui.DoGoToCashier();
		customerGui.DoGoToCashier();
		
		Do("Leaving.");
		wait.msgLeavingTable(this);
	}
	
	private void Payment() {
		Cash payment;
		//Check cpCheck = new Check(check.choice, check.customer, check.waiter, check.tableNumber);
		
		if(cash.totalAmount() >= check.price) {
			payment = cash.payCash(check.price);
			Do("Here is cash " + dFormat.format(payment.totalAmount()));
			cashier.msgPayment(this, payment);
		}
		else {
			Do("I am sorry, but I don't have enough money this time. Chao!");
			state = AgentState.DoingNothing;
			exitRestaurant();			
		}
		
		foodGui.state = FoodGui.State.done;
	}
	
	private void exitRestaurant() {
		customerGui.DoExitRestaurant();
	}
	
	// Accessors, etc.
	public void setHost(HostAgent host) {
		this.host = host;
	}

	public void setWaiter(WaiterAgent waiter) {
		this.wait = waiter;
	}
	
	public WaiterAgent getWaiter() {
		return wait;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}
	
	public String getName() {
		return name;
	}
	
	public int getHungerLevel() {
		return hungerLevel;
	}

	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
		//could be a state change. Maybe you don't
		//need to eat until hunger lever is > 5?
	}
	
	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	public CustomerGui getGui() {
		return customerGui;
	}
	
	public FoodGui getFoodGui() {
		return foodGui;
	}

	public String toString() {
		return "customer " + getName();
	}

	public String getCurrentCash() {
		return dFormat.format(cash.totalAmount());
	}
	
	public double FirstCheapestFood() {
		double first;
		double second;
		
		first = menu.get(menu_list.get(0));
		second = menu.get(menu_list.get(0));
	
		for(String food : menu_list) {
			if (first > menu.get(food)) {
				second = first;
				first = menu.get(food);
			}
			else if ( first < menu.get(food) && second > menu.get(food) ) {
				second = menu.get(food);
			}
		}
		
		return first;
	}
	
	public double SecondCheapestFood() {
		double first;
		double second;
		
		first = menu.get(menu_list.get(0));
		second = menu.get(menu_list.get(0));
	
		for(String food : menu_list) {
			if (first > menu.get(food)) {
				second = first;
				first = menu.get(food);
			}
			else if ( first < menu.get(food) && second > menu.get(food) ) {
				second = menu.get(food);
			}
		}
		
		return second;
	}
	
	public static class Cash {
		int twentyDollar;
		int tenDollar;
		int fiveDollar;
		int oneDollar;
		int coins;
		
		public Cash(int twenty, int ten, int five, int one, int coins) {
			twentyDollar = twenty;
			tenDollar = ten;
			fiveDollar = five;
			oneDollar = one;
			this.coins = coins;
		}
		
		public Cash payCash(double price) {
			int twenty = 0;
			int ten = 0;
			int five = 0;
			int one = 0;
			int coins = 0;
						
			double sum = 0;
				
			while(sum < price) {
				if(twenty < twentyDollar) { 
					twenty ++; 
				}
				else if(ten < tenDollar) {
					ten ++;
				}
				else if(five < fiveDollar) {
					five ++;
				}
				else if(one < oneDollar) {
					one++;
				}
				else if(this.coins < coins) {
					coins ++;
				}
				sum = calculateTotal(twenty, ten, five, one, coins);
			}
						
			twentyDollar -= twenty;
			tenDollar -= ten;
			fiveDollar -= five;
			oneDollar -= one;
			this.coins -= coins;
						
			return new Cash(twenty, ten, five, one, coins);
		}
		
		public double calculateTotal(int twenty, int ten, int five, int one, int coins) {
			return (double) (20*twenty + 10*ten + 5*five + one + 0.01*(double)(coins));
		}
		
		public double totalAmount() {
			return (double) (20*twentyDollar + 10*tenDollar + 5*fiveDollar + oneDollar + 0.01*(double)(coins));
		}
		
		public void addChanges(Cash cash) {
			twentyDollar += cash.twentyDollar;
			tenDollar += cash.tenDollar;
			fiveDollar += cash.fiveDollar;
			oneDollar += cash.oneDollar;
			coins += cash.coins;
		}
	}
}

