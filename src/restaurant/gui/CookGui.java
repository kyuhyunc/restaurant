package restaurant.gui;

import restaurant.CookAgent;

import java.awt.*;

import javax.swing.ImageIcon;

public class CookGui implements Gui{

	private CookAgent agent = null;
    
	protected static int CookSizeX = 30;
    protected static int CookSizeY = 30;
	
	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoGrill, GoPlat, GoRefrig, Default};
	private Command command=Command.noCommand;
	
	private int gap = 25;
	
	private ImageIcon cookImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/Cook.jpg");
	private Image image = cookImage.getImage();
	
	public CookGui(CookAgent c){ //HostAgent m) {
		agent = c;
		xPos = AnimationPanel.CookLocationX;
		yPos = AnimationPanel.CookLocationY;
		xDestination = xPos;
		yDestination = yPos;
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
			if (command==Command.Default) {
				
			}
			else if (command==Command.GoGrill) {
				agent.msgAtGrill();				
			}
			else if (command==Command.GoPlat) {
				agent.msgAtPlat();		
			}
			else if (command==Command.GoRefrig) {
				agent.msgAtRefrig();	
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {
		//g.setColor(Color.GREEN);
		//g.fillRect(xPos, yPos, 20, 20);
		g.setColor(Color.black);
		g.drawImage(image, xPos, yPos, CookSizeX, CookSizeY, null);
	}

	public void DoGoToDefault() {//later you will map seat number to table coordinates.
		
		xDestination = AnimationPanel.CookLocationX;
		yDestination = AnimationPanel.CookLocationY;
		
		command = Command.Default;
	}
	
	public void DoGoToGrill() {
		xDestination = AnimationPanel.CookingAreaLocationX;
		yDestination = AnimationPanel.CookingAreaLocationY + CookSizeY;
		
		command = Command.GoGrill;
	}

	public void DoGoToRefrig() {
		xDestination = AnimationPanel.RefrigLocationX - CookSizeX;
		yDestination = AnimationPanel.RefrigLocationY + CookSizeY;
		command = Command.GoRefrig;
	}
	
	public void DoGoToPlat(int platNumber) {
		xDestination = AnimationPanel.PlatingAreaLocationX + AnimationPanel.PlatingAreaSizeX;
		yDestination = AnimationPanel.PlatingAreaLocationY + 50 + gap*platNumber;
		command = Command.GoPlat;
	}

	@Override
	public boolean isPresent() {
		// TODO Auto-generated method stub
		return true;
	}
}
