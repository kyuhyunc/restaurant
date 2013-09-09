package restaurant.gui;

import restaurant.CustomerAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame implements ActionListener {
    /* The GUI has two frames, the control frame (in variable gui) 
     * and the animation frame, (in variable animationFrame within gui)
     */
	JFrame animationFrame = new JFrame("Restaurant Animation");
	AnimationPanel animationPanel = new AnimationPanel();
	
    /* restPanel holds 2 panels
     * 1) the staff listing, menu, and lists of current customers all constructed
     *    in RestaurantPanel()
     * 2) the infoPanel about the clicked Customer (created just below)
     */    
    private RestaurantPanel restPanel = new RestaurantPanel(this);
    
    /* infoPanel holds information about the clicked customer, if there is one*/
    private JPanel infoPanel; // this infopanel should be assigned to pane for scroll bar
    
    // these two should be modified
    private JLabel infoLabel; //part of infoPanel
    //private JCheckBox stateCB;//part of infoLabel
    
    private Vector<JCheckBox> stateCBs = new Vector<JCheckBox>();
    private Vector<String> nameList = new Vector<String>();
    private Vector<JPanel> customerList = new Vector<JPanel>();
    
    private JPanel listPanel = new JPanel(); // panel for the list of customers
    private JPanel CBPanel = new JPanel(); // panel for the check boxes    
    
    private JPanel sub_infoPanel; //for adding my name
    private JLabel sub_infoLabel;
    private JLabel sub_infoPic;
    
    private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */


    public JScrollPane pane =
            new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    public RestaurantGui() {
        int WINDOWX = 450;
        int WINDOWY = 750;

        animationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //animationFrame.setBounds(100+WINDOWX, 50 , WINDOWX+100, WINDOWY+100);
        animationFrame.setBounds(100+WINDOWX, 50 , WINDOWX, WINDOWY-400);
        animationFrame.setVisible(true);
    	animationFrame.add(animationPanel); 
    	
    	setBounds(50, 50, WINDOWX, WINDOWY);
    	
        setLayout(new BoxLayout((Container) getContentPane(), BoxLayout.Y_AXIS));
    	//setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
    	//setLayout(new GridLayout(3,0));

        Dimension restDim = new Dimension(WINDOWX, (int) (WINDOWY * .4));
        restPanel.setPreferredSize(restDim);
        restPanel.setMinimumSize(restDim);
        restPanel.setMaximumSize(restDim);
        add(restPanel);
        
        // Now, setup the info panel
        Dimension infoDim = new Dimension(WINDOWX, (int) (WINDOWY * .25));
        infoPanel = new JPanel();
        pane.setPreferredSize(infoDim);
        //infoPanel.setPreferredSize(infoDim);
        //infoPanel.setMinimumSize(infoDim);
        //infoPanel.setMaximumSize(infoDim);
        //infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Customer List"));
        
        // here should be either deleted or modified
        /**
        stateCB = new JCheckBox();
        stateCB.setVisible(false);
        stateCB.addActionListener(this);
        */
          
        infoPanel.setLayout(new BoxLayout((Container) infoPanel, BoxLayout.Y_AXIS));
        //infoPanel.setLayout(new BorderLayout());
        //infoPanel.setLayout(new GridLayout(0,2));
        
        //listPanel.setLayout(new BoxLayout((Container) listPanel, BoxLayout.Y_AXIS));
        //CBPanel.setLayout(new BoxLayout((Container) CBPanel, BoxLayout.Y_AXIS));
        
        //infoPanel.add(listPanel, BorderLayout.CENTER);
        //infoPanel.add(CBPanel, BorderLayout.EAST);
        
        //infoPanel.setLayout(new GridLayout(1, 2, 30, 0));
        
        /**infoLabel = new JLabel(); 
        infoLabel.setText("<html><pre><i>Click Add to make customers</i></pre></html>");
        
        //infoPanel.add(infoLabel, BorderLayout.NORTH);
        infoPanel.add(infoLabel);*/
        //infoPanel.add(new JLabel(""));
        //infoPanel.add(stateCB);
        pane.setViewportView(infoPanel);
        pane.setWheelScrollingEnabled(true);
        add(pane);
        
        //add(infoPanel);
        
        Dimension sub_infoDim = new Dimension(WINDOWX, (int) (WINDOWY * .1));        
        sub_infoPanel = new JPanel();
        sub_infoPanel.setPreferredSize(sub_infoDim);
        sub_infoPanel.setMinimumSize(sub_infoDim);
        sub_infoPanel.setMaximumSize(sub_infoDim);
        sub_infoPanel.setBorder(BorderFactory.createTitledBorder("Extra Information"));
     
        sub_infoLabel = new JLabel();
        sub_infoLabel.setText("<html><pre><i>Developed by Kyu Chang</i></pre></html>");
        
        sub_infoPic = new JLabel(new ImageIcon("C:/Users/Kyu/Dropbox/ing/deerant2.jpg"));
        
        sub_infoPanel.add(sub_infoPic);
        sub_infoPanel.add(sub_infoLabel);
        
        add(sub_infoPanel);
        
        pack();
    }
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
    	/**
        stateCB.setVisible(true);
        currentPerson = person;
               
        if (person instanceof CustomerAgent) {
            CustomerAgent customer = (CustomerAgent) person;
            stateCB.setText("Hungry?");
          //Should checkmark be there? 
            stateCB.setSelected(customer.getGui().isHungry());
          //Is customer hungry? Hack. Should ask customerGui
            stateCB.setEnabled(!customer.getGui().isHungry());
          // Hack. Should ask customerGui
            infoLabel.setText(
               "<html><pre>     Name: " + customer.getName() + " </pre></html>");
        }
        infoPanel.validate();*/
    	
    	currentPerson = person;
    	
    	if (person instanceof CustomerAgent) {
    		CustomerAgent customer = (CustomerAgent) person;
    		
    		customerList.add(new JPanel());
    		customerList.lastElement().setLayout(new GridLayout(1,2));
    		customerList.lastElement().setLayout(new GridLayout(1,2));
    		
    		Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            customerList.lastElement().setPreferredSize(buttonSize);
            customerList.lastElement().setMinimumSize(buttonSize);
            customerList.lastElement().setMaximumSize(buttonSize);    		
    		
    		customerList.lastElement().add(new JLabel(customer.getName()));
    		
    		
    		// adding name of customers to nameList
    		nameList.add(customer.getName());
    		
    		stateCBs.add(new JCheckBox());
    		stateCBs.lastElement().addActionListener(this);
    		stateCBs.lastElement().setVisible(true);
    		stateCBs.lastElement().setText("Hungry?");
    		stateCBs.lastElement().setSelected(customer.getGui().isHungry());
    		stateCBs.lastElement().setEnabled(!customer.getGui().isHungry());
    		
    		customerList.lastElement().add(stateCBs.lastElement());
    		
    		infoPanel.add(customerList.lastElement());
    		
    		/**
    		infoPanel.add(new JLabel(customer.getName()));
    		nameList.add(customer.getName());
    		
    		stateCBs.add(new JCheckBox());
    		stateCBs.lastElement().addActionListener(this);
    		stateCBs.lastElement().setVisible(true);
    		stateCBs.lastElement().setText("Hungry?");
    		stateCBs.lastElement().setSelected(customer.getGui().isHungry());
    		stateCBs.lastElement().setEnabled(!customer.getGui().isHungry());
    		
    		infoPanel.add(stateCBs.lastElement());
    		*/   		
    		
    		/**
    		CustomerAgent customer = (CustomerAgent) person;
    		stateCBs.add(new JCheckBox());
    		//stateCB = new JCheckBox();
    		//stateCB.addActionListener(this);
    		stateCBs.lastElement().addActionListener(this);
    		stateCBs.lastElement().setVisible(true);
    		
    		stateCBs.lastElement().setText("Hungry?");
    		stateCBs.lastElement().setSelected(customer.getGui().isHungry());
    		stateCBs.lastElement().setEnabled(!customer.getGui().isHungry());
   		
    		JLabel name = new JLabel(customer.getName());
    		//name.setFont("Courier", Font.PLAIN, 15);
    		
    		nameList.add((String) customer.getName());
    		
    		listPanel.add(name);
    		CBPanel.add(stateCBs.lastElement());*/
    	}
    	infoPanel.validate();
    
    	pane.validate();
    }
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    public void actionPerformed(ActionEvent e) {
        /**if (e.getSource() == stateCB) {
            if (currentPerson instanceof CustomerAgent) {
                CustomerAgent c = (CustomerAgent) currentPerson;
                c.getGui().setHungry();
                stateCB.setEnabled(false);
            }
        }*/
    	for(int i = 0; i < stateCBs.size() ; i++){
	    	if (e.getSource() == stateCBs.get(i)) {
	    		if( restPanel.getCustomerAgent(i) instanceof CustomerAgent) {
	    			restPanel.getCustomerAgent(i).getGui().setHungry();
	    			stateCBs.get(i).setEnabled(false);
	    		}
	    			
	    		/**if(currentPerson instanceof CustomerAgent) {
	    			CustomerAgent c = (CustomerAgent) currentPerson;
	                c.getGui().setHungry();
	                stateCBs.get(i).setEnabled(false); 
	    		}*/
	    	}
    	}
    }
    
    /**
     * Message sent from a customer gui to enable that customer's
     * "I'm hungry" checkbox.
     *
     * @param c reference to the customer
     */
    public void setCustomerEnabled(CustomerAgent c) {
    	//if (currentPerson instanceof CustomerAgent) {
    		//QQQQ: what is object and who is currentPerson? 
    		//CustomerAgent cust = (CustomerAgent) currentPerson;
    		//if (c.equals(cust)) {
    			for(int i = 0; i < stateCBs.size() ; i++){
    				if(c.getName().equals(nameList.get(i))) {
    					stateCBs.get(i).setEnabled(true);
    					stateCBs.get(i).setSelected(false);
    				}
            	}	
    		//}    		
    	//}
    	
    	/**if (currentPerson instanceof CustomerAgent) {
    		CustomerAgent cust = (CustomerAgent) currentPerson;
    		if (c.equals(cust)) {
    			for(int i = 0; i < stateCBs.size() ; i++){
    				if(c.getName().equals(stateCBs.get(i))) {
    					stateCBs.get(i).setEnabled(true);
    					stateCBs.get(i).setSelected(false);
    				}
        		}	
    		}
    	}*/
    	/**if (currentPerson instanceof CustomerAgent) {
            CustomerAgent cust = (CustomerAgent) currentPerson;
            if (c.equals(cust)) {
                stateCB.setEnabled(true);
                stateCB.setSelected(false);
            }
        }*/
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
