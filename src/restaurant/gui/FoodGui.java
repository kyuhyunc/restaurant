package restaurant.gui;

import restaurant.CookAgent;
import restaurant.CookAgent.Food;
import restaurant.MarketAgent;

import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;

public class FoodGui implements Gui{

	public int xPos, yPos;
	private int xDestination, yDestination;
	private boolean isPresent = false;
	
	private int gap = AnimationPanel.gap;
	
	MarketAgent market;
	CookAgent cook;
	
	public static enum State {noCommand, waiting, delivering, delivered, doneEating,
		waitingCheck, deliveringCheck, goToCashier, reOrdering, done, procurement,
		refrigToGill, cooking, grillToPlat, waitingToBePicked};
	public State state=State.noCommand;

	Food choice;
	String price;
	int orderedSize;
	
	private ImageIcon questionMark = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/question.jpg");
	private ImageIcon checkImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/check.jpg");
	
	private Image qImage = questionMark.getImage();
	private Image cImage = checkImage.getImage();
	private Image fImage;
	
	public int tableNumber; // for market ant table
	
	String pattern = ".##";
	DecimalFormat dFormat = new DecimalFormat(pattern);
	
	public FoodGui(int t, Food choice, int orderedSize){ //HostAgent m) {
			
		tableNumber = t;
		this.choice = choice;
		this.orderedSize = orderedSize;
		
		fImage = choice.getImageIcon().getImage();
		
		setPresent(true);
	}

	public void updatePosition() {
		if( state == State.delivering || state == State.deliveringCheck || state == State.goToCashier || state == State.procurement ||
				state == State.refrigToGill || state == State.grillToPlat ) {
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
				else if(state == State.procurement) {
					state = State.done;
					market.msgDeliveredToCook();
				}
				else if(state == State.refrigToGill) {
					if(tableNumber < 4) {
						xPos = AnimationPanel.CookingAreaLocationX + 8 + 25*(tableNumber-1);
						yPos = AnimationPanel.CookingAreaLocationY + 2;
					}
					else {
						xPos = AnimationPanel.CookingAreaLocationX + 15 + 25*(tableNumber%2);
						yPos = AnimationPanel.CookingAreaLocationY + 27;
					}
					state = State.cooking;
				}
				else if(state == State.grillToPlat) {
					state = State.waitingToBePicked;
				}				
				else {
					state = State.noCommand;
				}
			}
		}		
	}

	public void draw(Graphics2D g) {
		// followings are for food gui for watier and customer
		if( state == State.waiting || state == State.delivering ) {
			g.drawImage(fImage, AnimationPanel.TableLocationX + gap*(tableNumber-1) + 20, AnimationPanel.TableLocationY , 20, 20, null);
			g.drawImage(qImage, AnimationPanel.TableLocationX + gap*(tableNumber-1) + 40, AnimationPanel.TableLocationY, 10, 20, null);
		}		
		
		if( state == State.delivering || state == State.delivered ) {
			g.drawImage(fImage, xPos, yPos, 20, 20, null);
		}
		
		// following if statements are for check animation for waiter and customers
		if ( state == State.waitingCheck || state == State.deliveringCheck ) {
			g.drawImage(cImage, AnimationPanel.TableLocationX + gap*(tableNumber-1) + 20, AnimationPanel.TableLocationY, 20, 20, null);
			g.drawImage(qImage, AnimationPanel.TableLocationX + gap*(tableNumber-1) + 40, AnimationPanel.TableLocationY, 10, 20, null);
		}
		
		if( state == State.deliveringCheck || state == State.goToCashier ) {
			g.drawImage(cImage, xPos, yPos, 20, 20, null);
			//if ( state == State.goToCashier && price != null) {
			if (state == State.goToCashier) {
				if ( price == null) {
					//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				}
				else	
					g.drawString("$:"+price, xPos-9, yPos + 30);
			}
		}
		
		// followings are for food gui from market to cook
		if ( state == State.procurement) {
			String batchSize = Integer.toString(orderedSize);
			g.drawString(batchSize, xPos, yPos);
			g.drawImage(fImage, xPos, yPos, 20, 20, null);
		}
		
		// following the cook
		if(state == State.grillToPlat || state == State.refrigToGill || state == State.cooking || state == State.waitingToBePicked) {
			g.drawImage(fImage, xPos, yPos, 20, 20, null);
		}

	}

	// from either cook or cashier to table
	public void DoGoToTable () {//later you will map seat number to table coordinates.
		if (state == State.delivering) { // delivering food to Table
			//xPos = AnimationPanel.CookLocationX;
			//yPos = AnimationPanel.CookLocationY;
			
			xPos = AnimationPanel.PlatingAreaLocationX;
	    	yPos = AnimationPanel.CookLocationY;
			
			xDestination = AnimationPanel.TableLocationX + gap*(tableNumber-1) + 20;
			yDestination = AnimationPanel.TableLocationY;
		}
		else if (state == State.deliveringCheck) { // delivering check to table
			xPos = AnimationPanel.CashierLocationX;
			yPos = AnimationPanel.CashierLocationY + AnimationPanel.CashierSizeY - 20;
			
			xDestination = AnimationPanel.TableLocationX + gap*(tableNumber-1) + 20;
			yDestination = AnimationPanel.TableLocationY;
		}
	}

	// from table to cashier
	public void DoGoToCashier () {
		//price = dFormat.format(choice.getPrice());
		
		xPos = AnimationPanel.TableLocationX + gap*(tableNumber-1) + 20;
		yPos = AnimationPanel.TableLocationY;
		
		xDestination = AnimationPanel.CashierLocationX + 20;
		yDestination = AnimationPanel.CashierLocationY + AnimationPanel.CashierSizeY ;
	}
	
	// from market to cook
	public void DoGoToCook (MarketAgent m) {
		market = m;
		
		xPos = AnimationPanel.MarketLocationX;
		yPos = AnimationPanel.MarketLocationY + gap*(tableNumber-1);
		
		//xDestination = AnimationPanel.CookLocationX + AnimationPanel.CookSizeX;
		//yDestination = AnimationPanel.CookLocationY;
		
		xDestination = AnimationPanel.RefrigLocationX + AnimationPanel.RefrigSizeX;
		yDestination = AnimationPanel.RefrigLocationY;
	}
	
	// from refrig to gill
	public void DoGoToGrill (int grillNumber) {
		tableNumber = grillNumber;
		xPos = AnimationPanel.RefrigLocationX;
		yPos = AnimationPanel.RefrigLocationY;
		
		xDestination = AnimationPanel.CookingAreaLocationX;
		yDestination = AnimationPanel.CookingAreaLocationY;
	}

	// from grill to plat
	public void DoGoToPlat (int platNumber) {
		tableNumber = platNumber;
		xDestination = AnimationPanel.PlatingAreaLocationX;
		yDestination = AnimationPanel.PlatingAreaLocationY + 15 + 25*platNumber;
	}

	@Override
	public boolean isPresent() {
		// TODO Auto-generated method stub
		return isPresent;
	}
	
	public void setPresent(boolean p) {
		isPresent = p;
	}
	
	public void setPrice(double price) {
		this.price = dFormat.format(price);
	}
	
	public void setCook(CookAgent cook) {
		this.cook = cook;
	}
	
}
