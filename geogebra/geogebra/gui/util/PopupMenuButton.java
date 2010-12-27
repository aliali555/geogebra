package geogebra.gui.util;


import geogebra.main.Application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
	private PopupMenuButton thisButton;
	
	private JPopupMenu myPopup;
	public JPopupMenu getMyPopup() {
		return myPopup;
	}

	private JSlider mySlider;
	
	
	private Color fgColor;
	private int fontStyle = 0;
	
	
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}

	public void setFgColor(Color fgColor) {
		this.fgColor = fgColor;
		if(myTable != null)
			myTable.setFgColor(fgColor);
		updateGUI();
		
	}

	private SelectionTable myTable;
	public SelectionTable getMyTable() {
		return myTable;
	}

	private Dimension iconSize;
	
	public void setIconSize(Dimension iconSize) {
		this.iconSize = iconSize;
	}

	private boolean hasTable, hasSlider;
	
	// flag to determine if the popup should persist after a mouse click
	private boolean keepVisible = true;
	
	private boolean isDownwardPopup = true;
	
	public void setDownwardPopup(boolean isDownwardPopup) {
		this.isDownwardPopup = isDownwardPopup;
	}

	
	private boolean isStandardButton = false;
	public void setStandardButton(boolean isStandardButton) {
		this.isStandardButton = isStandardButton;
	}

	private boolean isFixedIcon = false;
	
	
	private boolean isIniting = true;
	private JMenuItem component;
	protected boolean popupIsVisible;
	
	
	
	/*************************************
	/** Button constructors */
	
	public PopupMenuButton(){
		this( null, null, -1, -1, null, -1,  false,  false);
	}
	
	
	
	public PopupMenuButton(ImageIcon icon, JComponent component){
		this( null, null, -1, -1, null, -1,  false,  false);
		myPopup.add(component);
		setIcon(icon);	
	}
	
	public PopupMenuButton(String text, JComponent component){
		this( null, null, -1, -1, null, -1,  false,  false);
		myPopup.add(component);
		setText(text);
		setIcon(GeoGebraIcon.createEmptyIcon(1, 1));
	}
	
	public PopupMenuButton(String text,ImageIcon icon,  JComponent component){
		this( null, null, -1, -1, null, -1,  false,  false);
		myPopup.add(component);
		setText(text);
		setIcon(icon);
	}
	
	
	public PopupMenuButton(Application app, Object[] data, Integer rows, Integer columns, Dimension iconSize, Integer mode){
		this( app, data, rows, columns, iconSize, mode,  true,  false);	
	}
	
	
	public PopupMenuButton(Application app, Object[] data, Integer rows, Integer columns, Dimension iconSize, 
			Integer mode, final boolean hasTable, boolean hasSlider){
		super(); 
		this.app = app;
		this.hasTable = hasTable;
		this.hasSlider = hasSlider;
		this.mode = mode;
		this.iconSize = iconSize;
		this.thisButton = this;
		
		this.setFocusable(false);
		
		
		// create the popup
		myPopup = new JPopupMenu();
		myPopup.setFocusable(false);
		myPopup.setBackground(Color.WHITE);
		myPopup.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY),
				BorderFactory.createEmptyBorder(3,3,3,3)));

		

		// add a mouse listener to our button that triggers the popup		
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				popupIsVisible = myPopup.isShowing();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
	
				if(popupIsVisible == true && !myPopup.isVisible()){
					popupIsVisible = false;
					return;
				}
				
				Point locButton = getLocation();
				int h = e.getX() - locButton.x;

				// trigger popup 
				// default: trigger only when the mouse is over the right side of the button
				// if isStandardButton: pressing anywhere triggers the popup
				if( isStandardButton || e.getX() >= getWidth()-16 &&  e.getX() <= getWidth()) { 
					if(hasTable)
						myTable.updateFonts();
					if(isDownwardPopup)
						// popup appears below the button
						myPopup.show(getParent(), locButton.x,locButton.y + getHeight());
					else
						// popup appears above the button
						myPopup.show(getParent(), 
								locButton.x - myPopup.getPreferredSize().width + thisButton.getWidth(),
								locButton.y - myPopup.getPreferredSize().height - 2);
				}
				
				popupIsVisible = myPopup.isShowing();
			}
		});
		
		
		// place text to the left of drop down icon
		this.setHorizontalTextPosition(JButton.LEFT); 
		this.setHorizontalAlignment(JButton.LEFT);
		

		// create selection table
		if(hasTable){
			this.rows = rows;
			this.columns = columns;
			this.data = data;

			myTable = new SelectionTable(app,data,rows,columns,iconSize,mode);
			setSelectedIndex(0);	

			// add a mouse listener to handle table selection
			myTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					handlePopupActionEvent();
				}
			});

	/*		
			// if displaying text only, then adjust the width 
			if(mode == SelectionTable.MODE_TEXT){
				 Dimension d = this.getPreferredSize();
				 d.width = myTable.getColumnWidth();
				 setMinimumSize(d); 
				 setMaximumSize(d); 
			 }
		*/	
			
			myTable.setBackground(myPopup.getBackground());
			myPopup.add(myTable);
		}


		// create slider
		if(hasSlider)
			getMySlider();
		
		isIniting = false;
		
		
		if(mode == SelectionTable.MODE_TEXT && iconSize.width == -1){
			iconSize.width = myTable.getColumnWidth()-4;
			iconSize.height = myTable.getRowHeight()-4;	
		}
		
		
		
		
	}
	
	
	public void addPopupMenuItem(JComponent component){
		myPopup.add(component);
	}
	
	
	/**
	 * Override processMouseEvents to prevent firing a mouseReleased event and
	 * the resulting ActionPerformed event when the mouse is clicked in the
	 * dropdown triangle region. Clicking in this part of the button should just
	 * trigger the popup. ActionPerformed events will be fired by the popup
	 * following user selection.
	 */
	@Override
	protected void processMouseEvent(MouseEvent e){
		
		if(e.getID() == MouseEvent.MOUSE_RELEASED){
			Point locButton = getLocation();
			int h = e.getX() - locButton.x;
			// mouse is over the popup triangle side of the button
			if(isStandardButton || e.getX() >= getWidth()-16 &&  e.getX() <= getWidth()) 
				return;
		}
		
		super.processMouseEvent(e);
	}
	
	
	 public void update(Object[] geos) {
		 
	 }
	
	//=============================================
	//         GUI
	//=============================================

	
	 private void updateGUI(){

		 if(isIniting) return;
		 
		 setIcon(getButtonIcon());
		 
		 if(hasTable){
			 myTable.repaint();
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
	//	mySlider.setSnapToTicks(true);

		mySlider.addChangeListener(this);

		// set slider dimensions
		Dimension d = mySlider.getPreferredSize();
		if(hasTable)
			d.width = myTable.getPreferredSize().width;
		else
			d.width = 110;
		mySlider.setPreferredSize(d);
		
		mySlider.setBackground(myPopup.getBackground());

		myPopup.add(mySlider);
	}

	
	 //==============================================
	 //    Handlers and Listeners
	 //==============================================
	
	/**
	 * Pass a popup action event up to the button invoker. If the first button
	 * click triggered our popup (the click was in the triangle region), then we
	 * must pass action events from the popup to the invoker
	 */
	public void handlePopupActionEvent(){
		//System.out.println("handlepopup");
		this.fireActionPerformed(new ActionEvent(this,
				ActionEvent.ACTION_PERFORMED,getActionCommand())); 
		updateGUI();
		if(!keepVisible)
			myPopup.setVisible(false);
	}
	
	
	/**
	 * Change listener for slider. Fires an action event up to the
	 * button invoker.
	 */
	public void stateChanged(ChangeEvent e) {
		
		//if (mySlider.getValueIsAdjusting()) return;
		
		if(mySlider != null)
			setSliderValue(mySlider.getValue());
		//System.out.println(mySlider.getValue());
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
		return myTable.getSelectedValue();
	}
	
	
	public void setSelectedIndex(Integer selectedIndex) {
		
		if(selectedIndex == null)
			selectedIndex = -1;
		
		myTable.setSelectedIndex(selectedIndex);
		updateGUI();
	}
	

	public int getSliderValue() {
		return mySlider.getValue();
	}

	public void setSliderValue(int value) {
		
		mySlider.removeChangeListener(this);
		mySlider.setValue(value);
		mySlider.addChangeListener(this);
		
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
	 //    Icon Handling
	 //==============================================

	

	public ImageIcon getButtonIcon(){
		
		ImageIcon icon = (ImageIcon) this.getIcon();
		if(isFixedIcon) return icon;
		
		
		// draw the icon for the current table selection
		if(hasTable){
			switch (mode){
			
			case SelectionTable.MODE_TEXT:
				// Strings are converted to icons. We don't use setText so that the button size can be controlled
				// regardless of the layout manager.

				icon = GeoGebraIcon.createStringIcon((String)data[getSelectedIndex()], app.getPlainFont(), 
						false, false, true, iconSize, Color.BLACK, null);

				break;

			case SelectionTable.MODE_ICON:
			case SelectionTable.MODE_LATEX:
				icon  = (ImageIcon) myTable.getSelectedValue();
				break;

			default:
				icon = myTable.getDataIcon(data[getSelectedIndex()]);
			}
		}
		return icon;
	}

	
	
	
	/**
	 * Append a downward triangle image to the right hand side of an input icon.
	 */
	@Override
	public void setIcon(Icon icon) {
		
		if(isFixedIcon) {
			super.setIcon(icon);
			return;
		}
		
		if(iconSize == null) 
			if(icon != null)
				iconSize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
			else
				iconSize = new Dimension(1,1);

		if(icon == null){
			//icon = GeoGebraIcon.createEmptyIcon(1, iconSize.height);
		}else{
			icon = GeoGebraIcon.ensureIconSize((ImageIcon) icon, iconSize);
		}
		
		
		// get icon width and height		
		int w = 0;
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
		if(icon != null)
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

	
	public void setFixedIcon(Icon icon){
		isFixedIcon = true;
		setIcon(icon);
	}
	
	
	
	
	
	
	
	
	
}		

