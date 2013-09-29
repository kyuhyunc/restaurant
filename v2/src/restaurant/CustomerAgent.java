package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.gui.FoodGui;
import agent.Agent;
import restaurant.CookAgent.Food;
import restaurant.WaiterAgent;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent {
	private String name;
	private int hungerLevel = 2;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	private FoodGui foodGui;

	// agent correspondents
	private HostAgent host;
	private WaiterAgent wait;
	
	Set<String> menu;
	String choice;
	Food fChoice;
	
	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, ReadyToOrder, WaitingFood, Eating, DoneEating, Leaving};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followHost, seated, callWaiterToOrder, makeOrder, getFood, doneEating, doneLeaving};
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
	public void msgFollowMe(Set<String> menu) {
		this.menu = menu;
		event = AgentEvent.followHost;
		stateChanged();
	}

	// 5: WhatWouldYouLike()
	public void msgWhatWouldYouLike() {
		event = AgentEvent.makeOrder;
		stateChanged();
	}
	
	// 9: HereIsYourOrder() 
	public void msgHereIsYourOrder() {
		event = AgentEvent.getFood;
		stateChanged();
	}
	
	// messages from gui
	public void msgAnimationFinishedGoToSeat() {
		//from animation
		event = AgentEvent.seated;
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
		else if (state == AgentState.WaitingFood && event == AgentEvent.getFood){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		else if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.Leaving;
			leaveTable();
			return true;					
		}
		else if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
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
		Do("Choosing menu");
		Random oRandom = new Random();
		
		int randomNum = oRandom.nextInt(4);
				
		// algorithm for choose what to order
		switch (randomNum) {
		case 0:
			choice = "Stake";
			break;
		case 1:
			choice = "Chicken";
			break;
		case 2:
			choice = "Salad";
			break;
		case 3:
			choice = "Pizza";
			break;
		default:
			print("default");
			choice = "Stake";
			break;
		}
		
		if(menu.contains(choice)) {
			fChoice = new Food(choice);
		}
		else {
			Do("failed to choose menu");
		}
			
		event = AgentEvent.callWaiterToOrder;
		stateChanged();
	}
	
	private void ReadyToOrder() {
		Do("Ready To Order");
		wait.msgReadyToOrder(this);
	}
	
	private void HereIsMyChoice(String choice) {
		Do("Here Is My Choice : " + choice);
			
		for(WaiterAgent.MyCustomer myC : wait.getMyCustomers()) {
			if(myC.c == this) {
				foodGui = new FoodGui(myC.t.tableNumber, fChoice);			
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
				foodGui.state = FoodGui.State.doneEating;
				stateChanged();
			}
		},
		(int) (fChoice.time * fChoice.eatingTimeMultiplier) * getHungerLevel());//how long to wait before running task
	}

	private void leaveTable() {
		Do("Leaving.");
		wait.msgLeavingTable(this);
		customerGui.DoExitRestaurant();
	}

	// Accessors, etc.
	public void setHost(HostAgent host) {
		this.host = host;
	}

	public void setWaiter(WaiterAgent waiter) {
		this.wait = waiter;
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
}

