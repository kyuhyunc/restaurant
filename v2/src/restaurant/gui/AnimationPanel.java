package restaurant.gui;

import javax.swing.*;

import restaurant.HostAgent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class AnimationPanel extends JPanel implements ActionListener {

    private final int WINDOWX = 450;
    private final int WINDOWY = 350;
    //private Image bufferImage;
    //private Dimension bufferSize;

    private List<Gui> guis = new ArrayList<Gui>();
    
    //static variables to replace magic numbers
    protected static int TableLocationX = 50;
    protected static int TableLocationY = 250;
    private static int TableSizeX = 50;
    private static int TableSizeY = 50;
    
    protected static int CookLocationX = 370;
    protected static int CookLocationY = 50;
    
    protected Timer timer;
    private final int timerSpeed = 15;
    
    public static boolean pauseFlag = false;

	private ImageIcon cookImage = new ImageIcon("C:/Users/Kyu/Dropbox/my work/USC/2013 2_fall/csci 201/git/restaurant_kyuhyunc/img/Cook.jpg");
	private Image image = cookImage.getImage();
    
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
	        g2.fillRect(TableLocationX + (70*i), TableLocationY, TableSizeX, TableSizeY);//200 and 250 need to be table params
        }
        
        g.drawImage(image, CookLocationX, CookLocationY, 30, 30, null);

        for(Gui gui : guis) {
            if (gui.isPresent()) {
            	
                gui.updatePosition();
            }
        }

        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.draw(g2);
            	//gui.drawImg();
            }
        }
    }

    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }

    public void addGui(WaiterGui gui) {
        guis.add(gui);
    }
}
