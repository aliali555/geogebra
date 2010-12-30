package geogebra3D.gui;

import geogebra.gui.GuiManager;
import geogebra.gui.layout.Layout;
import geogebra.gui.layout.panels.Euclidian2DockPanel;
import geogebra.gui.layout.panels.EuclidianDockPanel;
import geogebra.gui.toolbar.Toolbar;
import geogebra.main.Application;
import geogebra3D.Application3D;
import geogebra3D.gui.layout.panels.EuclidianDockPanel3D;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Extending DefaultGuiManager class for 3D
 * 
 * @author matthieu
 *
 */
public class GuiManager3D extends GuiManager {

	
	private AbstractAction showAxes3DAction, showGrid3DAction, showPlaneAction;
	
	/** 
	 * default constructor
	 * @param app
	 */
	public GuiManager3D(Application app) {
		super(app);
		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	protected void createLayout(){
		setLayout(new Layout());
	}
	
	/**
	 * Add 3D euclidian view to layout.
	 */
	protected void initLayoutPanels() {
		super.initLayoutPanels();

		/*

    	DockPanel panel3D = new DockPanel(Application3D.VIEW_EUCLIDIAN3D, 
    			"GraphicsView3D", 
    			null, 
    			false, 4) {
    		protected JComponent loadStyleBar() {
				return null;
			}

    		protected JComponent loadComponent() {
				return ((Application3D)app).getEuclidianView3D();
			}
		};
		panel3D.setMaximumSize(new Dimension(0,0));
		panel3D.setMinimumSize(new Dimension(0,0));

    	getLayout().registerPanel(panel3D);

		 */
		
		getLayout().registerPanel(new EuclidianDockPanel3D(app));
	}
	
	/*
	protected EuclidianDockPanel newEuclidianDockPanel(){
		return new EuclidianDockPanel(app,Toolbar.getAllToolsNoMacros());
	}
	
	protected Euclidian2DockPanel newEuclidian2DockPanel(){
		return new Euclidian2DockPanel(app,Toolbar.getAllToolsNoMacros());
	}*/
	
	//////////////////////////////
	// ACTIONS
	//////////////////////////////
	
	
	protected boolean initActions() {
		
		if (!super.initActions())
			return false;
		showAxes3DAction = new AbstractAction(app.getMenu("Axes"),
				app.getImageIcon("axes.gif")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				// toggle axes
				((Application3D) app).toggleAxis3D();
				//app.getEuclidianView().repaint();
				app.storeUndoInfo();
				app.updateMenubar();
				
			}
		};

		showGrid3DAction = new AbstractAction(app.getMenu("Grid"),
				app.getImageIcon("grid.gif")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				// toggle grid
				((Application3D) app).toggleGrid3D();
				//app.getEuclidianView().repaint();
				app.storeUndoInfo();
				app.updateMenubar();
				
			}
		};
		
		showPlaneAction = new AbstractAction(app.getMenu("Plane"),
				app.getImageIcon("plane.gif")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				// toggle plane
				((Application3D) app).togglePlane();
				app.storeUndoInfo();
				app.updateMenubar();
			}
		};

		
		return true;
		
	}
	
	
	public AbstractAction getShowAxes3DAction() {
		initActions();
		return showAxes3DAction;
	}

	public AbstractAction getShowGrid3DAction() {
		initActions();
		return showGrid3DAction;
	}
	
	public AbstractAction getShowPlaneAction() {
		initActions();
		return showPlaneAction;
	}
	
	
	
	
	
	
	
	//////////////////////////////
	// POPUP MENU
	//////////////////////////////
	
	
	/**
	 * Displays the zoom menu at the position p in the coordinate space of
	 * euclidianView
	 */
	/*
	public void showDrawingPadPopup(Component invoker, Point p) {
		// clear highlighting and selections in views		
		app.getEuclidianView().resetMode();
		
		// menu for drawing pane context menu
		ContextMenuGraphicsWindow3D popupMenu = new ContextMenuGraphicsWindow3D(
				app, p.x, p.y);
		popupMenu.show(invoker, p.x, p.y);
	}
	*/
	
	/**
	 * Displays the zoom menu at the position p in the coordinate space of
	 * euclidianView
	 */
	public void showDrawingPadPopup3D(Component invoker, Point p) {
		// clear highlighting and selections in views		
		((Application3D) app).getEuclidianView3D().resetMode();
		
		// menu for drawing pane context menu
		ContextMenuGraphicsWindow3D popupMenu = new ContextMenuGraphicsWindow3D(
				app, p.x, p.y);
		popupMenu.show(invoker, p.x, p.y);
	}
	

}
