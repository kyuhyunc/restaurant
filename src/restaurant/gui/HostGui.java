package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;

import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;


public class HostGui implements Gui {

    private HostAgent agent = null;

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position

    public static final int xTable = 200;
    public static final int yTable = 250;

    private ImageIcon hostImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/H for host.jpg");
    private Image image = hostImage.getImage();
    
    public HostGui(HostAgent agent) {
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

        if (xPos == xDestination && yPos == yDestination
        		& (xDestination == xTable + 20) & (yDestination == yTable - 20)) {
           agent.msgAtTable();
        }
    }

    
    public void draw(Graphics2D g) {
        //g.setColor(Color.MAGENTA);
        //g.fillRect(xPos, yPos, 20, 20);
    	g.drawImage(image, xPos, yPos, 20, 20, null);
    	
    }
    /**
	public void draw(JLabel g){
		g.setIcon(hostImage);
		g.setLocation(xPos, yPos);
	}*/

    public boolean isPresent() {
        return true;
    }

    public void DoBringToTable(CustomerAgent customer) {
        xDestination = xTable + 20;
        yDestination = yTable - 20;
    }

    public void DoLeaveCustomer() {
        xDestination = -20;
        yDestination = -20;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}
