package geogebra.gui.util;


import geogebra.kernel.GeoElement;
import geogebra.main.Application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JButton with popup component. A mouse click on the left side of the button
 * fires a normal action event. A mouse click on the right side triggers a popup
 * with either a selection table, a slider or both. Events generated by the
 * popup are passed up to the button invoker as action events.
 * 
 * 
 * @author G. Sturr
 * 
 */
public class PopupMenuButton extends JButton implements ChangeListener{
			
	private int mode;
	private int rows, columns;
	private Object[] data;
	private int size;
	private Application app;
	
	private JPopupMenu myPopup;
	private JSlider mySlider;
	private GeoGebraIcon selectedIcon;
	
	private SelectionTable myTable;
	private Dimension cellSize;
	
	private boolean hasTable, hasSlider;
	
	// flag to determine if the popup should persist after a mouse click
	private boolean keepVisible = false;
	

	

	/** Button constructor */
	public PopupMenuButton(Application app, Object[] data, Integer rows, Integer columns, Dimension cellSize, Integer mode){
		super(); 
		this.app = app;

		selectedIcon = new GeoGebraIcon();
		
		// create the popup
		myPopup = new JPopupMenu();
		myPopup.setBackground(this.getBackground());
		myPopup.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));


		this.mode = mode;
		this.cellSize = cellSize;

		// create slider
		if(mode == GeoGebraIcon.MODE_SLIDER){
			hasTable = false;
			getMySlider();
			

		// create selection table
		}else{
			hasTable = true;
			this.rows = rows;
			this.columns = columns;
			this.data = data;

			myTable = new SelectionTable(app,data,rows,columns,cellSize,mode);
			setSelectedIndex(0);	

			// add a mouse listener to the table that handles table selection
			myTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					handlePopupActionEvent();
					if(!keepVisible)
						myPopup.setVisible(false);
				}
			});

			myPopup.add(myTable);
		}


		// add a mouse listener to our button that triggers the popup		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point locButton = getLocation();
				int h = e.getX() - locButton.x;

				// trigger popup if the mouse is over the right side of the button
				if(e.getX() >= getWidth()-16 &&  e.getX() <= getWidth()) { 
					myPopup.show(getParent(), locButton.x,locButton.y + getHeight());
				}
			}
		});
				
		updateGUI();				
	}
	
	
	
	//=============================================
	//         GUI
	//=============================================

	
	private void updateGUI(){
		
		if(hasTable){
			myTable.repaint();
			myTable.updateIcon(selectedIcon, data[getSelectedIndex()]);
			setIcon(selectedIcon);
		}
		
		if(mode == GeoGebraIcon.MODE_SLIDER){
			Object args[] = {getSliderValue()};
			selectedIcon.setImage(app, args, cellSize, mode);
			setIcon(selectedIcon);
		}
		
		repaint();
	}

	
	

	
	/** 
	 * Create our JSlider 
	 */  
	private void initSlider() {

		mySlider = new JSlider(0, 100);
		mySlider.setMajorTickSpacing(25);
		mySlider.setMinorTickSpacing(5);
		mySlider.setPaintTicks(false);
		mySlider.setPaintLabels(false);
		mySlider.setSnapToTicks(true);

		mySlider.addChangeListener(this);

		// set slider dimensions
		Dimension d = mySlider.getPreferredSize();
		if(hasTable)
			d.width = myTable.getPreferredSize().width;
		else
			d.width = 110;
		mySlider.setPreferredSize(d);

		myPopup.add(mySlider);
	}

	
	 //==============================================
	 //    Handlers and Listeners
	 //==============================================
	
	/**
	 * Pass a popup action event up to the button invoker. If the first button
	 * click triggered our popup (the click was in the triangle region), then we
	 * must pass action events from the popup up to the invoker
	 */
	public void handlePopupActionEvent(){
		updateGUI();
		this.fireActionPerformed(new ActionEvent(this,
				ActionEvent.ACTION_PERFORMED,getActionCommand())); 
	}
	
	
	/**
	 * Change listener for our slider. This also fires an action event up to the
	 * button invoker.
	 */
	public void stateChanged(ChangeEvent e) {
		
		//if (mySlider.getValueIsAdjusting()) return;
		
		if(mySlider != null)
			setSliderValue(mySlider.getValue());
		
		this.fireActionPerformed(new ActionEvent(this,
				ActionEvent.ACTION_PERFORMED,getActionCommand())); 
		
		updateGUI();
	}


	

	 //==============================================
	 //    Getters/Setters
	 //==============================================

	
	public int getSelectedIndex() {
		return myTable.getSelectedIndex();
	}

	public Object getSelectedValue() {
		return data[getSelectedIndex()];
	}
	
	
	public void setSelectedIndex(int selectedIndex) {
		myTable.setSelectedIndex(selectedIndex);
		updateGUI();
	}
	

	public int getSliderValue() {
		return mySlider.getValue();
	}

	public void setSliderValue(int value) {
		mySlider.setValue(value);
		if(hasTable)
			myTable.setSliderValue(value);
		updateGUI();
	}

	public JSlider getMySlider() {
		if(mySlider == null)
			initSlider();
		return mySlider;
	}

	
	public void setKeepVisible(boolean keepVisible) {
		this.keepVisible = keepVisible;
	}

	



	 //==============================================
	 //    Set Icon
	 //==============================================

	/**
	 * Append a downward triangle image to the right hand side of an input icon.
	 */
	@Override
	public void setIcon(Icon icon) {
		
		// get icon width and height		
		int w = 4;
		int h = 18;
		if(icon != null){
			w = (icon.getIconWidth() > 0) ? icon.getIconWidth() : 0;
			h = (icon.getIconHeight() > 0) ? icon.getIconHeight() : 18;
		}

		// create a new buffered image with the same height as the input icon, 
		// but with 14 more pixels in width for the drop down triangle
		BufferedImage image = new BufferedImage(w + 14, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();

		// draw the input icon
		g2.drawImage(((ImageIcon) icon).getImage(), 0, 0, w, h, 0, 0, w, h, null);

		// draw a downward triangle on the right hand side
		g2.setColor(Color.BLACK);
		int x = w + 5;
		int y = h/2-1;
		g2.drawLine(x, y, x+6, y);
		g2.drawLine(x+1, y+1, x+5, y+1);
		g2.drawLine(x+2, y+2, x+4, y+2);
		g2.drawLine(x+3, y+3, x+3, y+3);
		
		super.setIcon(new ImageIcon(image));
	}
	
	
	
}		

