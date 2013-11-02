package restaurant.gui;

import javax.swing.*;

import java.awt.*;

/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame{
    /* The GUI has two frames, the control frame (in variable gui) 
     * and the animation frame, (in variable animationFrame within gui)
     */
	public AnimationPanel animationPanel = new AnimationPanel();
    
	/* restPanel holds 2 panels
     * 1) the staff listing, menu, and lists of current customers all constructed
     *    in RestaurantPanel()
     * 2) listPanel of customers
     * 3) listPanel of waiters
     */    
    private RestaurantPanel restPanel = new RestaurantPanel(this);
	
    private JPanel sub_infoPanel; //for adding my name
    private JLabel sub_infoLabel;

    
    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    public RestaurantGui() {
        int WINDOWX = 850;
        int WINDOWY = 730;

    	setBounds(400, 40, WINDOWX, WINDOWY);
    	//setSize(WINDOWX, WINDOWY + 400);

    	setLayout(new BoxLayout((Container) getContentPane(), BoxLayout.Y_AXIS));
              
    	add(new JLabel("  "));
    	
        Dimension restDim = new Dimension(WINDOWX, (int) (WINDOWY * .4));
        restPanel.setPreferredSize(restDim);
        restPanel.setMinimumSize(restDim);
        restPanel.setMaximumSize(restDim);
        add(restPanel);       
        
        // adding animationPanel directly to the main frame
        Dimension animationDim = new Dimension(WINDOWX, (int) (WINDOWY * .5));
        animationPanel.setPreferredSize(animationDim);
        animationPanel.setBorder(BorderFactory.createTitledBorder("Animation Panel"));
        add(animationPanel); 
        
        Dimension sub_infoDim = new Dimension(WINDOWX, (int) (WINDOWY * .1));        
        sub_infoPanel = new JPanel();
        sub_infoPanel.setPreferredSize(sub_infoDim);
        sub_infoPanel.setMinimumSize(sub_infoDim);
        sub_infoPanel.setMaximumSize(sub_infoDim);
        sub_infoPanel.setBorder(BorderFactory.createTitledBorder("Extra Information"));
     
        sub_infoLabel = new JLabel();
        sub_infoLabel.setText("<html><pre><i>Developed by Kyu Chang</i></pre></html>");
        
        sub_infoPanel.add(sub_infoLabel);                     
        
        add(sub_infoPanel);
        
        pack();
    }
  
    public ListPanel getCustomerPanel() {
    	return restPanel.getCustomerPanel();
    }
    
    public ListPanel getWaiterPanel() {
    	return restPanel.getWaiterPanel();
    }
    
    /**
     * Main routine to get gui started
     */
    public static void main(String[] args) {
        RestaurantGui gui = new RestaurantGui();
        gui.setTitle("csci201 Restaurant");
        gui.setVisible(true);
        gui.setResizable(false);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
