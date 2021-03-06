package restaurant.gui;

import restaurant.CustomerAgent;

import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;

public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	protected static int customerSize = 20;
	
	private int gap = AnimationPanel.gap;
		
	//private HostAgent host;
	RestaurantGui gui;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToLine, GoToSeat, GoToCashier, LeaveRestaurant};
	private Command command=Command.noCommand;

	//public static final int xTable = 200;
	//public static final int yTable = 250;
	
	private ImageIcon custImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/C for customer.jpg");
	private Image image = custImage.getImage();
	
	String pattern = ".00";
	DecimalFormat dFormat = new DecimalFormat(pattern);

	public CustomerGui(CustomerAgent c, RestaurantGui gui){ //HostAgent m) {
		agent = c;
		xPos = 250;
		yPos = -40;
		xDestination = -40;
		yDestination = -40;
		this.gui = gui;
	}

	public void updatePosition() {
		if (xPos < xDestination)
			xPos++;
		else if (xPos > xDestination)
			xPos--;

		if (yPos < yDestination)
			yPos++;
		else if (yPos > yDestination)
			yPos--;

		if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToLine) {
				agent.msgAnimationFinishedGoToLine();
			}
			else if (command==Command.GoToSeat) {
				agent.msgAnimationFinishedGoToSeat();
			}
			else if (command==Command.GoToCashier) {
				agent.msgArrivedAtCashier();
			}
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent)");
				isHungry = false;
				gui.getCustomerPanel().setCustomerEnabled(agent);
				xPos = 250;
				yPos = -40;
				xDestination = 250;
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {
		//g.setColor(Color.GREEN);
		//g.fillRect(xPos, yPos, 20, 20);
		g.setColor(Color.black);
		g.drawImage(image, xPos, yPos, customerSize, customerSize, null);
		g.drawString("name:"+agent.getName(), xPos, yPos - 21);
		if(agent.getWaiter() != null) {
			g.drawString("waiter:"+agent.getWaiter().getName(), xPos, yPos-11);
		}
		else {
			g.drawString("waiter:", xPos, yPos-11);
		}
		g.drawString("$:"+agent.getCurrentCash(), xPos, yPos-1);
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}
	
	public boolean isPresent() {
		return isPresent;
	}
	
	public void setHungry() {
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
	}
	
	public boolean isHungry() {
		return isHungry;
	}

	public void DoGoToSeat(int tableNumber) {//later you will map seat number to table coordinates.
		
		xDestination = AnimationPanel.TableLocationX + gap*(tableNumber-1);
		
		yDestination = AnimationPanel.TableLocationY;
		command = Command.GoToSeat;
	}
	
	public void DoGoToCashier() {
		xDestination = AnimationPanel.CashierLocationX;
		yDestination = AnimationPanel.CashierLocationY + AnimationPanel.CashierSizeY;
		
		command = Command.GoToCashier;
	}

	public void DoExitRestaurant() {
		xDestination = -40;
		yDestination = -40;
		command = Command.LeaveRestaurant;
	}
	
	public void DoGoToLine(int waitingNumber) {
		xDestination = AnimationPanel.WaitingAreaLocationX + (customerSize+2)*2*(waitingNumber-1);
		yDestination = AnimationPanel.WaitingAreaLocationY - 13*(waitingNumber-1);
		command = Command.GoToLine;
	}
}
