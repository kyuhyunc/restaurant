package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import java.awt.*;

import javax.swing.ImageIcon;

public class WaiterGui implements Gui {

    private WaiterAgent agent = null;

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position
    private enum Command {noCommand, GoToTable, GoToCook, GoToHost, GoToHost2};
	private Command command=Command.noCommand;
	
    //public static final int xTable = 200;
    //public static final int yTable = 250;

    private ImageIcon hostImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/W for Waiter.jpg");
    private Image image = hostImage.getImage();
    
    public WaiterGui(WaiterAgent agent) {
        this.agent = agent;
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
    		// What is & in this statement?
    		//& (xDestination == AnimationPanel.TableLocationX + 20) & (yDestination == AnimationPanel.TableLocationY - 20)) {
        	//if (yPos == AnimationPanel.TableLocationY - 20) {
        	if(command == Command.GoToTable) {
        		agent.msgAtTable();
        	}
        	else if (command == Command.GoToHost) {
        	//else if (xPos == -20 && yPos == -20) {
        		//agent.msgReadyToServe();
        		agent.msgAtHost();
        	}
        	else if (command == Command.GoToCook) {
        		agent.msgArrivedToCook();
        	}
        	command=Command.noCommand;
        }
    }

    public void draw(Graphics2D g) {
        g.drawImage(image, xPos, yPos, 20, 20, null);
    }

    public boolean isPresent() {
        return true;
    }

    public void DoGoToTable(CustomerAgent customer, int tableNumber) {
        xDestination = AnimationPanel.TableLocationX + ((tableNumber-1)*70) + 20;
        yDestination = AnimationPanel.TableLocationY - 20;
        
        command = Command.GoToTable;
    }

    public void GoToCook() {
    	xDestination = AnimationPanel.CookLocationX - 20;
    	yDestination = AnimationPanel.CookLocationY;
    	
        command = Command.GoToCook;
    }
    
    // DoGoBackToHost is to make waiter go back to Host strictly
    // DoGoBackToHost2 is to make waiter go back to Host, but can be interrupted if the customer calls him
    public void DoGoBackToHost() {
        xDestination = -20;
        yDestination = -20;
                
        command = Command.GoToHost;
    }
    
    public void DoGoBackToHost2() {
        xDestination = -20;
        yDestination = -20;
                
        command = Command.GoToHost2;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}
