package restaurant.gui;

import restaurant.CashierAgent;
import restaurant.CookAgent;
import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.MarketAgent;
import restaurant.WaiterAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class RestaurantPanel extends JPanel {

    //Host, cook, waiters and customers
    private HostAgent host = new HostAgent("Sarah");
    private CookAgent cook = new CookAgent("Cook");
    private CashierAgent cashier = new CashierAgent("Cashier");
 
    private Vector<CustomerAgent> customers = new Vector<CustomerAgent>();
    private Vector<WaiterAgent> waiters = new Vector<WaiterAgent>();    

    // these buttons are for the first list panel 
    private JButton addTable = new JButton("Add Table");
    private JButton addMarket = new JButton("Add Market");
    private JButton pause = new JButton("Pause");
    private JPanel buttons = new JPanel();
    
    // restLabel has menu information
    private JPanel restLabel = new JPanel();
    
    protected ListPanel customerPanel = new ListPanel(this, "Customers");
    protected ListPanel waiterPanel = new ListPanel(this, "Waiters");
    
    // group will have both types of panel
    //private JPanel group = new JPanel();

    private RestaurantGui gui; //reference to main gui

    public RestaurantPanel(RestaurantGui gui) {
        this.gui = gui;
        host.setRestaurantGui(gui);
        host.setCook(cook);
        host.startThread();
        
        cook.setHost(host);
        cook.setCashier(cashier);
        cook.setDefaultMarkets();
        cook.setDefaultPlatsAndGrills();
        CookGui cookGui = new CookGui(cook);
		gui.animationPanel.addGui(cookGui);
		cook.setGui(cookGui);
        cook.startThread();
		
        cashier.startThread();
        
        gui.animationPanel.setCook(cook);
        gui.animationPanel.setCashier(cashier);
        
        setLayout(new GridLayout(1, 3, 10, 10));

        initRestLabel();
        add(restLabel);
        add(customerPanel);
        add(waiterPanel);
    }

    /**
     * Sets up the restaurant label that includes the menu,
     * and host and cook information
     */
    private void initRestLabel() {
    	int WINDOWX = 220;
        int WINDOWY = 300;
    	
    	JLabel label = new JLabel();
    	JPanel info = new JPanel();
    	
    	info.setLayout(new BoxLayout((Container)info, BoxLayout.X_AXIS));
    	
        restLabel.setLayout(new BoxLayout((Container)restLabel, BoxLayout.Y_AXIS));
        
        buttons.setLayout(new GridLayout(2,1,10,5));
        
        Dimension buttonDim = new Dimension((int) (WINDOWX), (int) (WINDOWY * 0.1));
        addMarket.setPreferredSize(buttonDim);
        addTable.setPreferredSize(buttonDim);
        pause.setPreferredSize(buttonDim);
    
        buttons.add(addMarket);
        buttons.add(addTable);
        buttons.add(pause);   
                
        addMarket.addActionListener(new ButtonListener());
        addTable.addActionListener(new ButtonListener());
        pause.addActionListener(new ButtonListener());
        
        label.setText(
                "<html><h3><u>Tonight's Staff</u></h3><table><tr><td>"
                + "host:</td><td>" + host.getName() + "</td></tr></table><h3><u> "
	    		+ "Menu</u></h3><table><tr><td>"
	    		+ "Steak</td><td>$15.99</td></tr><tr><td>"
	    		+ "Chicken</td><td>$10.99</td></tr><tr><td>"
	    		+ "Salad</td><td>$5.99</td></tr><tr><td>"
	    		+ "Pizza</td><td>$8.99</td></tr></table><br></html>");
    
        label.setFont(new Font("Arial", Font.PLAIN, 10));
        
        info.add(new JLabel("           "));
        info.add(label);
        info.setBorder(BorderFactory.createTitledBorder("Restaurant Information"));
                
        //restLabel.add(new JLabel(" "));
        restLabel.add(buttons);
        JLabel blank = new JLabel("  ");
        blank.setFont(new Font("Arial", Font.PLAIN, 8));
        restLabel.add(blank);
        restLabel.add(info);
    }

    /**
     * Adds a customer or waiter to the appropriate list
     *
     * @param type indicates whether the person is a customer or waiter (later)
     * @param name name of person
     */
    public void addPerson(String type, String name) {
    	if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui c_g = new CustomerGui(c, gui);

    		gui.animationPanel.addGui(c_g);// dw
    		c.setHost(host);
    		c.setCashier(cashier);
    		c.setGui(c_g);
    		customers.add(c);
    		c.startThread();
    		
    		customerPanel.updateInfoPanel(c);
    	}
    	else if (type.equals("Waiters")) {
    		if(waiters.size() < HostAgent.NWAITERS){
	    		WaiterAgent w = new WaiterAgent(name);
	    		WaiterGui w_g = new WaiterGui(w, gui);
	    		
	    		gui.animationPanel.addGui(w_g);
	    		w.setHost(host);
	    		w.setCook(cook);
	    		w.setCashier(cashier);
	    		w.setGui(w_g);
	    		waiters.add(w);
	    		w.waiterNumber = waiters.size();
	    		w.startThread();
	    		
	    		host.msgAddWaiterByGui(w);
	    		
	    		waiterPanel.updateInfoPanel(w);
    		}
    		else
    			System.out.println("Cannot add more waiter!! (maximum is 5)"); 		
    	}
    }
        
    // action listener for pause button 
    class ButtonListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		if(e.getSource() == pause){
    			host.msgPauseAgent();
    			cook.msgPauseAgent();
    		
    			for(WaiterAgent w : waiters) {
    				w.msgPauseAgent();
    			}
    			
    			for(CustomerAgent c : customers) {
    				c.msgPauseAgent();
    			}   		
    			
    			if(AnimationPanel.pauseFlag == false){
    				AnimationPanel.pauseFlag = true;
    	
    				System.out.println("Pause");
    				
    				for(int i = 0; i < waiters.size() ; i++) {
    					waiterPanel.getStateCB(i).setEnabled(false);
    		    	}
    				for(int i = 0; i < customers.size() ; i++) {
    					customerPanel.getStateCB(i).setEnabled(false);
    		    	}
    				
    				addMarket.setEnabled(false);
    				addTable.setEnabled(false);
    				customerPanel.disableButtons();
    				waiterPanel.disableButtons();
    			}
    			
    			else {
    				AnimationPanel.pauseFlag = false;
    				System.out.println("Resume");
    				
    				for(int i = 0; i < waiters.size() ; i++) {
    					waiterPanel.getStateCB(i).setEnabled(true);	
    		    	}
    				for(int i = 0; i < customers.size() ; i++) {
    					if(!customerPanel.getStateCB(i).isSelected()){	
    						customerPanel.getStateCB(i).setEnabled(true);
    					}
    		    	}
    				
    				addMarket.setEnabled(true);
    				addTable.setEnabled(true);
    				customerPanel.enableButtons();
    				waiterPanel.enableButtons();
    			}    				
    		}
    		// action listener for add table button
    		else if (e.getSource() == addTable) {
    			if(HostAgent.NTABLES < 5) {
	    			HostAgent.NTABLES ++;
	    			host.addTableByGui();
	    			System.out.println("Adding one more table: " + HostAgent.NTABLES);
    			}
    			else {
    				System.out.println("Cannot add more table!! (maximum is 5)");
    			}
    		}
    		else if (e.getSource() == addMarket) {
    			if(CookAgent.NMARKETS < 4) {
    				CookAgent.NMARKETS ++;
    				cook.addMarketByGui();
    				System.out.println("Adding one more market: " + CookAgent.NMARKETS);
    			}
    		}
    	}
    }
        
    public Vector<CustomerAgent> getCustomers() {
    	return customers;
    }
    
    public CustomerAgent getCustomerAgent(int index) {
    	return customers.get(index);
    }
    
    public WaiterAgent getWaiterAgent(int index) {
    	return waiters.get(index);
    }
    
    public CustomerAgent getTheLastCustomer() {
    	return customers.lastElement();
    }

    public HostAgent getHostAgent() {
    	return host;
    }
    
    public ListPanel getCustomerPanel() {
    	return customerPanel;
    }
    
    public ListPanel getWaiterPanel() {
    	return waiterPanel;
    }
    
    public void hackResetCashier() {
    	cashier.cashTotal = 0;
    }
    
    public void hackResetMarket() {
    	synchronized(cook.getMarkets()) {
	    	for(MarketAgent m : cook.getMarkets()) {
	    		m.cash = 0;
	    	}
    	}
    }
    
    public void hackBatchSizeToTwo() {
    	cook.defaultBatchSize = 2;
    }
    
    public void hackBatchSizeToThree() {
    	cook.defaultBatchSize = 3;
    }
}
