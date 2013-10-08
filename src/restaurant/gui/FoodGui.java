package restaurant.gui;

import restaurant.CookAgent.Food;

import java.awt.*;

import javax.swing.ImageIcon;

public class FoodGui implements Gui{

	private int xPos, yPos;
	private int xDestination, yDestination;
	private boolean isPresent = false;
	
	public static enum State {noCommand, waiting, delivering, delivered,
		doneEating, waitingCheck, deliveringCheck, goToCashier, reOrdering, done};
	public State state=State.noCommand;

	Food choice;
	
	private ImageIcon questionMark = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/question.jpg");
	private ImageIcon checkImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/check.jpg");
	
	private Image qImage = questionMark.getImage();
	private Image cImage = checkImage.getImage();
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
		if( state == State.delivering || state == State.deliveringCheck || state == State.goToCashier ) {
			if (xPos < xDestination)
				xPos++;
			else if (xPos > xDestination)
				xPos--;
	
			if (yPos < yDestination)
				yPos++;
			else if (yPos > yDestination)
				yPos--;
	
			if (xPos == xDestination && yPos == yDestination) {
				if(state == State.delivering) {
					state = State.delivered;
				}
				else if(state == State.deliveringCheck) {
					state = State.goToCashier;
				}
			}
		}
	}

	public void draw(Graphics2D g) {
		if( state == State.waiting || state == State.delivering ) {
			g.drawImage(fImage, AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20, AnimationPanel.TableLocationY , 20, 20, null);
			g.drawImage(qImage, AnimationPanel.TableLocationX + 70*(tableNumber-1) + 40, AnimationPanel.TableLocationY, 10, 20, null);
		}		
		
		if( state == State.delivering || state == State.delivered ) {
			g.drawImage(fImage, xPos, yPos, 20, 20, null);
		}
		
		
		// following if statements are for check animation
		if ( state == State.waitingCheck || state == State.deliveringCheck ) {
			g.drawImage(cImage, AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20, AnimationPanel.TableLocationY, 20, 20, null);
			g.drawImage(qImage, AnimationPanel.TableLocationX + 70*(tableNumber-1) + 40, AnimationPanel.TableLocationY, 10, 20, null);
		}
		
		if( state == State.deliveringCheck || state == State.goToCashier ) {
			g.drawImage(cImage, xPos, yPos, 20, 20, null);
		}
	}

	public void DoGoToTable () {//later you will map seat number to table coordinates.
		if (state == State.delivering) { // delivering food to Table
			xPos = AnimationPanel.CookLocationX;
			yPos = AnimationPanel.CookLocationY;
			
			xDestination = AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20;
			yDestination = AnimationPanel.TableLocationY;
		}
		else if (state == State.deliveringCheck) { // delivering check to table
			xPos = AnimationPanel.CashierLocationX;
			yPos = AnimationPanel.CashierLocationY + AnimationPanel.CashierSizeY - 20;
			
			xDestination = AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20;
			yDestination = AnimationPanel.TableLocationY;
		}
	}

	public void DoGoToCashier () {
		xPos = AnimationPanel.TableLocationX + 70*(tableNumber-1) + 20;
		yPos = AnimationPanel.TableLocationY;
		
		xDestination = AnimationPanel.CashierLocationX + 20;
		yDestination = AnimationPanel.CashierLocationY + AnimationPanel.CashierSizeY ;
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
