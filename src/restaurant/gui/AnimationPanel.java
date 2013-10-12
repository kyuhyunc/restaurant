package restaurant.gui;

import javax.swing.*;

import restaurant.CookAgent;
import restaurant.HostAgent;
import restaurant.MarketAgent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AnimationPanel extends JPanel implements ActionListener {

    private final int WINDOWX = 720;
    private final int WINDOWY = 380;
    //private Image bufferImage;
    //private Dimension bufferSize;

    private List<Gui> guis = Collections.synchronizedList(new ArrayList<Gui>());
       
    //static variables to replace magic numbers
    protected static int TableLocationX = 50;
    protected static int TableLocationY = 250;
    protected static int TableSizeX = 50;
    protected static int TableSizeY = 50;
    
    protected static int CookLocationX = 450;
    protected static int CookLocationY = 130;
    protected static int CookSizeX = 30;
    protected static int CookSizeY = 30;
    
    protected static int MarketLocationX = 550;
    protected static int MarketLocationY = 50;
    protected static int MarketSizeX = 50;
    protected static int MarketSizeY = 50;
    
    protected static int CashierLocationX = 50;
    protected static int CashierLocationY = 20;
    protected static int CashierSizeX = 40;
    protected static int CashierSizeY = 40;
    
    protected static int gap = 80;
    
    protected Timer timer;
    private final int timerSpeed = 5;
    
    public static boolean pauseFlag = false;

	private CookAgent cook;
    private ImageIcon cookImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/Cook.jpg");
	private Image cImage = cookImage.getImage();
	
	private ImageIcon MarketImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/market.jpg");
	private Image mImage = MarketImage.getImage();
	
	private ImageIcon cashierImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/cashier.jpg");
	private Image cashImage = cashierImage.getImage();
	
    public AnimationPanel() {
    	setSize(WINDOWX, WINDOWY);
        setVisible(true);
        
        //bufferSize = this.getSize();
 
    	timer = new Timer(timerSpeed, this );
    	timer.start();
    }

	public void actionPerformed(ActionEvent e) {
		if(!pauseFlag) {
			repaint();  //Will have paintComponent called
		}
	}

	 public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        //Clear the screen by painting a rectangle the size of the frame
        g2.setColor(getBackground());
        g2.fillRect(0, 0, WINDOWX, WINDOWY );

        //Here is the table
        for(int i=0 ; i < HostAgent.NTABLES ; i++) {
	        g2.setColor(Color.ORANGE);
	        g2.fillRect(TableLocationX + (gap*i), TableLocationY, TableSizeX, TableSizeY);//200 and 250 need to be table params
        }
        
        for(int i=0 ; i < CookAgent.NMARKETS ; i++) {
	        g2.drawImage(mImage, MarketLocationX, MarketLocationY + (gap*i), MarketSizeX, MarketSizeY, null);
        }
        
        int m=0;
        int n=0;
        for(MarketAgent M : cook.getMarkets()) {
        	n=0;
        	for(String food : cook.getMenuList()) {
        		g2.setColor(Color.black);
            	g2.drawString(food + ": " + M.getInventory().get(food).getStock(), 
            			MarketLocationX+MarketSizeX+1, MarketLocationY + 10 + (gap*m) + (n*11));
            	n++;
        	}
        	m++;
        }
        
        int i=0;
        g.drawImage(cImage, CookLocationX, CookLocationY, CookSizeX, CookSizeY, null);
        for(String food : cook.getMenuList()) {
        	g2.setColor(Color.black);
        	g2.drawString(food + ": " + cook.getFoods().get(food).getStock(), CookLocationX, CookLocationY+CookSizeY+11+i*11);
        	i++;
        }
        
        
        
        g.drawImage(cashImage, CashierLocationX, CashierLocationY, CashierSizeX, CashierSizeY, null);
        
        synchronized (guis) {
	        for(Gui gui : guis) {
	            if (gui.isPresent()) {
	                gui.updatePosition();
	            }
	        }
        }
        
        synchronized (guis) {
	        for(Gui gui : guis) {
	            if (gui.isPresent()) {
	                gui.draw(g2);
	            	//gui.drawImg();
	            }
	        }
        }
    }

    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }

    public void addGui(WaiterGui gui) {
        guis.add(gui);
    }
    
    public void addGui(FoodGui gui) {
        guis.add(gui);
    }
    
    public void setCook(CookAgent cook) {
    	this.cook = cook;
    }
}
