package restaurant.gui;

import javax.swing.*;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Subpanel of restaurantPanel.
 * This holds the scroll panes for the customers and, later, for waiters
 */
public class ListPanel extends JPanel implements ActionListener {
	 
    private String type;
     
    private RestaurantPanel restPanel;
    
    // there are common objects for customers and waiters
    private JTextField Name = new JTextField();
    private JButton addPersonB;
    
    // infoPanel will have list of customers/waiters
    private JPanel infoPanel; // list box
    
    private Vector<JCheckBox> stateCBs = new Vector<JCheckBox>();
    private Vector<JPanel> agentList = new Vector<JPanel>();
    
    public JScrollPane pane =
            new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    // these objects are only for customer list panel
    private JCheckBox initialStateB = new JCheckBox();
       
    /**
     * Constructor for ListPanel.  Sets up all the gui
     *
     * @param rp   reference to the restaurant panel
     * @param type indicates if this is for customers or waiters
     */
    public ListPanel(RestaurantPanel rp, String type) {
        int WINDOWX = 220;
        int WINDOWY = 300;
    	
    	restPanel = rp;
        this.type = type;
        
        setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
        //setLayout(new GridLayout(7,1));
        
        if(type.equals("Customers")) {
        	addPersonB = new JButton("Add " + type);
        }
        else if(type.equals("Waiters")) {
        	addPersonB = new JButton("Add " + type);
        }
                    
        Name.setFont(new Font("Courier", Font.PLAIN, 20));
        Name.setSize(230, 50);
        add(Name);
        
        JPanel ButtonAndCB = new JPanel();
        
        Dimension buttonDim = new Dimension((int) (WINDOWX), (int) (WINDOWY * 0.1));
        addPersonB.addActionListener(this);
        //add(addPersonB);
        ButtonAndCB.add(addPersonB);
        
        if(type.equals("Customers")) {
        	buttonDim.setSize((int) (WINDOWX *0.65), (int) (WINDOWY * 0.1));
        	Dimension initialDim = new Dimension((int) (WINDOWX *0.3), (int) (WINDOWY * 0.1)); 
        	
        	initialStateB.setText("Hungry?");
        	initialStateB.setFont(new Font("Arial", Font.PLAIN, 11));
	        initialStateB.setPreferredSize(initialDim);
	        //add(initialStateB);
	        ButtonAndCB.add(initialStateB);
        }
        
        addPersonB.setPreferredSize(buttonDim);
        
        add(ButtonAndCB);
         
        // Now, setup the info panel
        Dimension infoDim = new Dimension(WINDOWX, (int) (WINDOWY * 0.8));       
        infoPanel = new JPanel();
        //infoPanel.setBorder(BorderFactory.createTitledBorder(type + " List"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        pane.setPreferredSize(infoDim);
        pane.setViewportView(infoPanel);
        pane.setWheelScrollingEnabled(true);
        pane.setBorder(BorderFactory.createTitledBorder(type + " List"));
        add(pane);
    }

    /**
     * Method from the ActionListener interface.
     * Handles the event of the add button being pressed
     */
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == addPersonB) {
         	// Chapter 2.19 describes showInputDialog()
            //addPerson(JOptionPane.showInputDialog("Please enter a name:"));
    		if(!Name.getText().equals("")) {
    			if(Name.getText().contains("!hack:")) {
    				System.out.println("Hack by Q!");
    				if(Name.getText().contains("resetMarket")) {
    					restPanel.hackResetMarket();;
    				}
    				else if(Name.getText().contains("resetCashier")) {
    					restPanel.hackResetCashier();
    				}  
    				else if(Name.getText().contains("batchSizeToTwo")) {
    					restPanel.hackBatchSizeToTwo();
    				}
    				else if(Name.getText().contains("batchSizeToThree")) {
    					restPanel.hackBatchSizeToThree();
    				}
    				else {
    					System.out.println("There is no matching hack code");
    				}
    			}
    			else
         		addPerson(Name.getText());
    		}
        }
    }

    /**
     * If the add button is pressed, this function creates
     * a spot for it in the scroll pane, and tells the restaurant panel
     * to add a new person.
     *
     * @param name name of new person
     */
    public void addPerson(String name) {
    	if (name != null) {
             restPanel.addPerson(type, name);//puts customer on list
             validate();
    	}
    }
    
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
    	//System.out.println(type);
 		
    	if (person instanceof CustomerAgent && type.equals("Customers")) {
     		CustomerAgent customer = (CustomerAgent) person;
 
     		// set initial state of customer
    		if(initialStateB.isSelected()) {
     			//restPanel.getTheLastCustomer().getGui().setHungry();
    			customer.getGui().setHungry();
     		}
    		
    		agentList.add(new JPanel());
    		agentList.lastElement().setLayout(new GridLayout(1,2));
    		    		
    		Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            agentList.lastElement().setPreferredSize(buttonSize);
            agentList.lastElement().setMinimumSize(buttonSize);
            agentList.lastElement().setMaximumSize(buttonSize);    		
    		            
            agentList.lastElement().add(new JLabel(customer.getName()));
    		    		
            stateCBs.add(new JCheckBox());
    		stateCBs.lastElement().addActionListener(new custChkBox());
    		stateCBs.lastElement().setVisible(true);
    		stateCBs.lastElement().setText("Hungry?");
    		stateCBs.lastElement().setSelected(customer.getGui().isHungry());
    		stateCBs.lastElement().setEnabled(!customer.getGui().isHungry());
    		
    		agentList.lastElement().add(stateCBs.lastElement());
    		
    		infoPanel.add(agentList.lastElement());
    	}
    	else if (person instanceof WaiterAgent && type.equals("Waiters")) {
    		WaiterAgent waiter = (WaiterAgent) person;
    		
    		agentList.add(new JPanel());
    		agentList.lastElement().setLayout(new GridLayout(1,2));
    		    		
    		Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            agentList.lastElement().setPreferredSize(buttonSize);
            agentList.lastElement().setMinimumSize(buttonSize);
            agentList.lastElement().setMaximumSize(buttonSize);    		
    		
            agentList.lastElement().add(new JLabel(waiter.getName()));
    		
            stateCBs.add(new JCheckBox());
    		stateCBs.lastElement().addActionListener(new waitChkBox());
    		stateCBs.lastElement().setVisible(true);
    		stateCBs.lastElement().setText("Break?");
    		stateCBs.lastElement().setSelected(waiter.getGui().isBreak());
    		stateCBs.lastElement().setEnabled(!waiter.getGui().isBreak());
    		
    		agentList.lastElement().add(stateCBs.lastElement());
        		
    		infoPanel.add(agentList.lastElement());
    	}
    
    	pane.validate();
    }
    
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    class custChkBox implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		synchronized(stateCBs) {
	    		for(int i = 0; i < stateCBs.size() ; i++){
	    	    	if (e.getSource() == stateCBs.get(i)) {
    	    			restPanel.getCustomerAgent(i).getGui().setHungry();
    	    			stateCBs.get(i).setEnabled(false);
    	    			break;
	    	    	}
	    	   	}
    		}
	    }
    }
    
    class waitChkBox implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		synchronized(stateCBs) {
	    		for(int i = 0; i < stateCBs.size() ; i++){
	    	    	if (e.getSource() == stateCBs.get(i)) {
	    	    		restPanel.getWaiterAgent(i).getGui().setBreak();
    	    			if(stateCBs.get(i).isSelected()) {
    	    				stateCBs.get(i).setEnabled(false);
    	    			}
    	    			break;
	    	    	}
	    	   	}
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
    	synchronized(stateCBs) {
	    	for(int i = 0; i < stateCBs.size() ; i++) {
				if(restPanel.getCustomerAgent(i) == c) {
					stateCBs.get(i).setEnabled(true);
					stateCBs.get(i).setSelected(false);
					break;
				}
	    	}
    	}
    }
    
    public void setWaiterEnabled(WaiterAgent w, boolean breakPermission) {
    	synchronized(stateCBs) {
	    	for(int i = 0; i < stateCBs.size() ; i++) {
				if(restPanel.getWaiterAgent(i) == w) {
					stateCBs.get(i).setEnabled(true);
					if(!breakPermission) { // if the waiter cannot be on break, uncheck the box 
						stateCBs.get(i).setSelected(false);
						restPanel.getWaiterAgent(i).getGui().setBreakFalse();
					}
					break;
				}
	    	}
    	}
    }
    
    public JCheckBox getStateCB(int i) {
    	synchronized(stateCBs) {
    		return stateCBs.get(i);
    	}
    }
    
    public void disableButtons() {
    	addPersonB.setEnabled(false);
    }
    
    public void enableButtons() {
    	addPersonB.setEnabled(true);
    }
    
    public String getTextInTextBox() {
    	return Name.getText();
    }
}
