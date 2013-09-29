package restaurant.gui;

import restaurant.WaiterAgent.Food;

import java.awt.*;

import javax.swing.ImageIcon;

public class FoodGui implements Gui{

	private int xPos, yPos;
	private int xDestination, yDestination;
	private boolean isPresent = false;
	
	public static enum State {noCommand, waiting, delivering, delivered, doneEating};
	public State state=State.noCommand;

	//public static final int xTable = 200;
	//public static final int yTable = 250;
	
	Food choice;
	
	private ImageIcon questionMark = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/question.jpg");
	
	private Image qImage = questionMark.getImage();
	private Image fImage;
	
	int tableNumber;
	
	public FoodGui(int t, Food choice){ //HostAgent m) {
	
		xPos = AnimationPanel.CookLocationX;
		yPos = AnimationPanel.CookLocationY;
		
		tableNumber = t;
		this.choice = choice;
		
		fImage = choice.getImageIcon().getImage();
		
		setPresent(true);
	}

	public void updatePosition() {
		if( state == State.delivering ) {
			if (xPos < xDestination)
				xPos++;
			else if (xPos > xDestination)
				xPos--;
	
			if (yPos < yDestination)
				yPos++;
			else if (yPos > yDestination)
				yPos--;
	
			if (xPos == xDestination && yPos == yDestination) {
				state = State.delivered;
			}
		}
	}

	public void draw(Graphics2D g) {
		if( state == State.waiting || state == State.delivering ) {
			g.drawImage(qImage, AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20, AnimationPanel.TableLocationY , 20, 20, null);
		}		
		
		if( state == State.delivering || state == State.delivered ) {
			g.drawImage(fImage, xPos, yPos, 20, 20, null);
		}
	}

	public void DoGoToTable () {//later you will map seat number to table coordinates.
		if (state == State.delivering) {
			xDestination = AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20;
			yDestination = AnimationPanel.TableLocationY;
		}
	}

	@Override
	public boolean isPresent() {
		// TODO Auto-generated method stub
		return isPresent;
	}
	
	public void setPresent(boolean p) {
		isPresent = p;
	}
}
