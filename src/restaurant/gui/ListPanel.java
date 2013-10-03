package restaurant.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Subpanel of restaurantPanel.
 * This holds the scroll panes for the customers and, later, for waiters
 */
public class ListPanel extends JPanel implements ActionListener {
	
    private List<JButton> list = new ArrayList<JButton>();
    
    private JButton addPersonB = new JButton("Add Customer");

    private RestaurantPanel restPanel;
    private String type;
    
    private JTextField CustName = new JTextField();

    /**
     * Constructor for ListPanel.  Sets up all the gui
     *
     * @param rp   reference to the restaurant panel
     * @param type indicates if this is for customers or waiters
     */
    public ListPanel(RestaurantPanel rp, String type) {
    	 restPanel = rp;
         this.type = type;
        
         //setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
         setLayout(new GridLayout(7,1));
         add(new JLabel("<html><pre> <u>" + type + "</u><br></pre></html>"));
   
         
         CustName.setFont(new Font("Courier", Font.PLAIN, 20));		
         add(CustName);
              
         addPersonB.addActionListener(this);
         add(addPersonB);

         
         //add(new JLabel("Click add to add customers"));
    }

    /**
     * Method from the ActionListener interface.
     * Handles the event of the add button being pressed
     */
    public void actionPerformed(ActionEvent e) {
    	 if (e.getSource() == addPersonB) {
         	// Chapter 2.19 describes showInputDialog()
             //addPerson(JOptionPane.showInputDialog("Please enter a name:"));
         	if(!CustName.getText().equals("")) {
         		addPerson(CustName.getText());
            }
         }
                
         else {
         	// Isn't the second for loop more beautiful?
             /*for (int i = 0; i < list.size(); i++) {
                 JButton temp = list.get(i);*/
         	for (JButton temp:list){
                 if (e.getSource() == temp)
                     restPanel.showInfo(type, temp.getText());
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
         	
         	/*
             JButton button = new JButton(name);
             button.setBackground(Color.white);

             Dimension paneSize = pane.getSize();
             Dimension buttonSize = new Dimension(paneSize.width - 20,
                     (int) (paneSize.height / 7));
             button.setPreferredSize(buttonSize);
             button.setMinimumSize(buttonSize);
             button.setMaximumSize(buttonSize);
             button.addActionListener(this);
             list.add(button);
             view.add(button);*/
             restPanel.addPerson(type, name);//puts customer on list
             restPanel.showInfo(type, name);//puts hungry button on panel
             validate();
         }
    }
    
    public void disableButtons() {
    	addPersonB.setEnabled(false);
    }
    
    public void enableButtons() {
    	addPersonB.setEnabled(true);
    }
    
    public String getTextInTextBox() {
    	return CustName.getText();
    }
}
