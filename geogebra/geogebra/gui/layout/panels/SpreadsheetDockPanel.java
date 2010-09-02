package geogebra.gui.layout.panels;

import javax.swing.JComponent;

import geogebra.gui.layout.DockPanel;
import geogebra.gui.view.spreadsheet.SpreadsheetView;
import geogebra.main.Application;

/**
 * Dock panel for the spreadsheet view.
 */
public class SpreadsheetDockPanel extends DockPanel {
	private static final long serialVersionUID = 1L;
	private Application app;
	
	/**
	 * @param app
	 */
	public SpreadsheetDockPanel(Application app) {
		super(
			Application.VIEW_SPREADSHEET, 		// view id
			"Spreadsheet", 						// view title phrase
			"0 66",								// toolbar string
			true,								// style bar?
			3, 									// menu order
			'S'									// menu shortcut
		);
		
		this.app = app;
	}

	protected JComponent loadStyleBar() {
		return ((SpreadsheetView)app.getGuiManager().getSpreadsheetView()).getSpreadsheetStyleBar();
	}
	
	protected JComponent loadComponent() {
		return app.getGuiManager().getSpreadsheetView();
	}
	
	protected void focusGained() {
	}
	
	protected void focusLost() {
	}
}
