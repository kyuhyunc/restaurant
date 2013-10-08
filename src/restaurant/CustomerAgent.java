package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.FoodGui;
import agent.Agent;
import restaurant.CashierAgent.Check;
import restaurant.WaiterAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent {
	private String name;
	private int hungerLevel = 1;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	private FoodGui foodGui;

	// agent correspondents
	private HostAgent host;
	private WaiterAgent wait;
	private CashierAgent cashier;
	
	//Map<String, Food> menu;
	private List<String> menu_list = new ArrayList<String> ();
	private String choice;
	private Check check;
	private Cash cash;
	
	//private Semaphore atCashier = new Semaphore(0,true);
	
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, ReadyToOrder
	, WaitingFood, Eating, DoneEating, LeavingTable, DonePayment, LeavingRestaurant};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followHost, seated, callWaiterToOrder, makeOrder
	, getFood, doneEating, askForCheck, getCheck, payment, leaveRestaurant
	, doneLeaving, reOrder};
	AgentEvent event = AgentEvent.none;

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

	/**
	 * hack to establish connection to Host agent.
	 */

	
	// Messages

	// 0: IamHungry()
	public void gotHungry() { //from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}

	// messages from gui
	public void msgSitAtTable() {
		print("Received msgSitAtTable");
		event = AgentEvent.followHost;
		stateChanged();
	}
	
	// 3: FollowMe(menu)
	public void msgFollowMe(List<String> menu_list) {
		//this.menu_list = menu_list; // update menu with the full version one
		this.menu_list.addAll(menu_list);
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
	public void msgWhatWouldYouLike() {
		event = AgentEvent.makeOrder;
		stateChanged();
	}
	
	public void msgAskForOrderAgain(List<String> menu_list) {
		//this.menu_list = menu_list; // update menu that out of stocked food is not included
		this.menu_list.clear();
		this.menu_list.addAll(menu_list);
		
		// check menu list
		event = AgentEvent.reOrder;
		stateChanged();
	}
	
	// 9: HereIsYourOrder() 
	public void msgHereIsYourOrder() {
		event = AgentEvent.getFood;
		stateChanged();
	}
	
	public void msgHereIsYourCheck(Check check) {
		event = AgentEvent.getCheck;
		this.check = check;
		stateChanged();
	}
	
	// message from gui
	public void msgArrivedAtCashier() {
		//atCashier.release();
		event = AgentEvent.payment;
		stateChanged();
	}
	
	public void msgChange(Cash cash) {
		cash.addChanges(cash);		
		event = AgentEvent.leaveRestaurant;
		stateChanged();
	}
	
	// messages from gui
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
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
		else if (state == AgentState.Seated && event == AgentEvent.callWaiterToOrder){
			state = AgentState.ReadyToOrder;
			ReadyToOrder();
			return true;
		}
		else if (state == AgentState.ReadyToOrder && event == AgentEvent.makeOrder){
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
			//state = AgentState.Leaving;
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
		else if (state == AgentState.LeavingRestaurant && event == AgentEvent.doneLeaving){
			state = AgentState.DoingNothing;
			return false;					
		}
		
		return false;
	}

	// Actions

	private void goToRestaurant() {
		Do("Going to restaurant");
		host.msgIWantFood(this); //send our instance, so he can respond to us
	}

	private void SitDown() {
		Do("Being seated. Going to table");
		for(WaiterAgent.MyCustomer myC : wait.getMyCustomers()) {
			if(myC.c == this) {
				customerGui.DoGoToSeat(myC.t.tableNumber);	
				break;
			}		
		}	
	}
	
	private void ChooseMenu() {
		// this is for reset the foodGui
		if(!(foodGui == null)){
			foodGui.state = FoodGui.State.reOrdering;
		}
		
		Do("Choosing menu");
		
		Random oRandom = new Random();
		int randomNum;
		
		if(menu_list.size() > 0) {
			randomNum = oRandom.nextInt(menu_list.size());
			
			choice = menu_list.get(randomNum);
		}
		else {
			Do("There is nothing available");
			
			wait.msgLeavingTable(this);
			exitRestaurant();
			state = AgentState.DoingNothing;
		}
				
		// algorithm for choose what to order
		
		
		// this is temporary code for testing outofFood function
		if(menu_list.contains(name)) {
			System.out.println("Hack =) by Kyu");
			choice = name;
		}
		event = AgentEvent.callWaiterToOrder;	
	}
	
	private void ReadyToOrder() {
		Do("Ready To Order");
		wait.msgReadyToOrder(this);
	}
	
	private void HereIsMyChoice(String choice) {
		Do("Here Is My Choice : " + choice);
		
		for(WaiterAgent.MyCustomer myC : wait.getMyCustomers()) {
			if(myC.c == this) {
				foodGui = new FoodGui(myC.t.tableNumber, wait.getFood(choice));
				break;
			}		
		}
		
		wait.msgHereIsMyChoice(this, choice);
	
		host.gui.animationPanel.addGui(foodGui);
		foodGui.state = FoodGui.State.waiting;
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
				//foodGui.state = FoodGui.State.doneEating;
				stateChanged();
			}
		},
		wait.getFood(choice).getEatingTime());
	}
	
	private void ReadyForCheck() {
		Do("Ask for check to waiter");
		//foodGui.state = FoodGui.State.waitingCheck;
		wait.msgReadyForCheck(this);
	}

	private void leaveTable() {
		Do("Leaving.");
		
		wait.msgLeavingTable(this);
		
		foodGui.state = FoodGui.State.goToCashier;
		foodGui.DoGoToCashier();
		
		customerGui.DoGoToCashier();
		/**try {
			atCashier.acquire(); // 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/		
	}
	
	private void Payment() {
		Cash payment;
		
		if(cash.totalAmount() >= check.price) {
			payment = cash.payCash(check.price);
			cashier.msgPayment(check, payment);
			
			Do("Here is cash " + payment.totalAmount());
		}
		else {
			Do("I am sorry, but I don't have enough money this time. Chao!");
			exitRestaurant();
			state = AgentState.DoingNothing;
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

	public String toString() {
		return "customer " + getName();
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
	
	public static class Cash {
		int twentyDollar;
		int tenDollar;
		int fiveDollar;
		int oneDollar;
		int coins;
		
		Cash(int twenty, int ten, int five, int one, int coins) {
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
			
			System.out.println("Cash --> " + sum);
			
			twentyDollar -= twenty;
			tenDollar -= ten;
			fiveDollar -= five;
			oneDollar -= one;
			this.coins -= coins;
			
			return new Cash(twenty, ten, five, one, coins);
		}
		
		public double calculateTotal(int twenty, int ten, int five, int one, int coins) {
			return (double) (20*twenty + 10*ten + 5*five + one + 0.01*coins);
		}
		
		public double totalAmount() {
			return (double) (20*twentyDollar + 10*tenDollar + 5*fiveDollar + oneDollar + 0.01*coins);
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

