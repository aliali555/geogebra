package geogebra.cas.view;

import geogebra.cas.GeoGebraCAS;
import geogebra.kernel.Kernel;
import geogebra.main.Application;
import geogebra.main.CasManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * A class which will give the view of the CAS
 */

/**
 * @author Quan Yuan
 * 
 */
public class CASView extends JComponent implements CasManager {

	private Kernel kernel;
	
	// TODO: add checkbox to set useGeoGebraVariableValues
	private boolean useGeoGebraVariableValues = true;

	
	private CASTable consoleTable;

	// public JList rowHeader;

	public JTable rowHeaderTable;

	public static final int ROW_HEADER_WIDTH = 30;

	private Application app;

	private GeoGebraCAS cas;

	private JButton btSub, btEval, btExp, btFactor;

	private final int numOfRows = 1;
	
	private static final int SUB_Flag = 0;
	private static final int EVAL_Flag = 1;
	private static final int EXP_Flag = 2;
	private static final int FAC_Flag = 3;

	public CASView(Application app) {
		kernel = app.getKernel();
		this.app = app;
		
		// init cas
		cas = (geogebra.cas.GeoGebraCAS) kernel.getGeoGebraCAS();			
		
		setLayout(new BorderLayout());
		
		// button panel
		initButtons();
		JPanel btPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btPanel.add(btEval);
		btPanel.add(btExp);
		btPanel.add(btFactor);
		btPanel.add(btSub);	
		add(btPanel, BorderLayout.NORTH);
		
		// CAS input/output cells
		createCASTable();
		createRowHeader();

		// init the scroll panel
		JScrollPane scrollPane = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		// put the table and the row header list into a scroll plane

		// scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setRowHeaderView(rowHeaderTable);
		scrollPane.setViewportView(consoleTable);

		// put the scrollpanel in 
		add(scrollPane, BorderLayout.CENTER);

		this.setBackground(Color.WHITE);
	}			

	private void createRowHeader() {

		// Option 1 ---- Implement rowheader with JTable

		rowHeaderTable = new JTable((CASTableModel) consoleTable.getModel());

		rowHeaderTable.setEnabled(false);

		rowHeaderTable.setFocusable(true);

		rowHeaderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		rowHeaderTable.addMouseListener(new RowHeaderMouseListener());

		// rowHeaderTable.addKeyListener(new RowHeaderKeyListener());

		rowHeaderTable.getColumnModel().getColumn(0).setPreferredWidth(
				ROW_HEADER_WIDTH);

		rowHeaderTable.setRowHeight(CASPara.inputLineHeight);

		rowHeaderTable.setDefaultRenderer(rowHeaderTable.getColumnClass(0),
				new RowHeaderTableRenderer(consoleTable));

		rowHeaderTable.setPreferredScrollableViewportSize(new Dimension(
				rowHeaderTable.getColumnModel().getColumn(0)
						.getPreferredWidth(), 0));

		// Option 2 ---- Implement rowheader with JList
		/*
		 * ------------- CASListModel listModel = new
		 * CASListModel((CASTableModel) consoleTable .getModel()); rowHeader =
		 * new JList(listModel); rowHeader.setFocusable(true);
		 * rowHeader.setAutoscrolls(false); rowHeader.addMouseListener(new
		 * RowHeaderMouseListener()); // rowHeader.addMouseMotionListener(new
		 * MouseMotionListener1()); // rowHeader.addKeyListener(new
		 * KeyListener1()); rowHeader.setFixedCellWidth(ROW_HEADER_WIDTH);
		 * rowHeader.setFixedCellHeight(consoleTable.getRowHeight()); // + //
		 * rowHeader.setFixedCellHeight(-1); // table.getRowMargin(); rowHeader
		 * .setCellRenderer(new RowHeaderRenderer(consoleTable, rowHeader)); //
		 * table.setView(this); -------------
		 */
	}

	private void createCASTable() {
		consoleTable = new CASTable(app);
		consoleTable.initializeTable(numOfRows, app);

		// Set the property of the value column;
		consoleTable.getColumnModel().getColumn(CASPara.contCol)
				.setCellRenderer(
						new CASTableCellRender(this, consoleTable, app));
		consoleTable.getColumnModel().getColumn(CASPara.contCol).setCellEditor(
				new CASTableCellEditor(this, consoleTable, app));
		consoleTable.getColumnModel().getColumn(CASPara.contCol)
				.setHeaderValue("");

		// CAScontroller
		// CASKeyController casKeyCtrl = new CASKeyController(this, session,
		// consoleTable);
		// consoleTable.addKeyListener(casKeyCtrl);
		consoleTable.addKeyListener(new ConsoleTableKeyListener());
		CASMouseController casMouseCtrl = new CASMouseController(this,
				consoleTable);
		consoleTable.addMouseListener(casMouseCtrl);
	}

	public static class CASListModel extends AbstractListModel {

		private static final long serialVersionUID = 1L;

		protected CASTableModel model;

		public CASListModel(CASTableModel model0) {
			model = model0;
		}

		public int getSize() {
			return model.getRowCount();
		}

		public Object getElementAt(int index) {
			return "" + (index + 1);
		}

	}

	protected int minSelectionRow = -1;
	protected int maxSelectionRow = -1;

	// This part is to implement rowheader with JList
	// public class RowHeaderRenderer extends JLabel implements
	// ListCellRenderer,
	// ListSelectionListener {
	//
	// private static final long serialVersionUID = 1L;
	//
	// protected JTableHeader header;
	// protected JList rowHeader;
	// protected ListSelectionModel selectionModel;
	// private Color defaultBackground;
	//
	// public RowHeaderRenderer(CASTable table, JList rowHeader) {
	// super("", JLabel.CENTER);
	// setOpaque(true);
	// defaultBackground = getBackground();
	//
	// this.rowHeader = rowHeader;
	// header = table.getTableHeader();
	// // setOpaque(true);
	// setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	// // setHorizontalAlignment(CENTER) ;
	// // setForeground(header.getForeground()) ;
	// // setBackground(header.getBackground());
	// if (getFont().getSize() == 0) {
	// Font font1 = app.getPlainFont();
	// if (font1 == null || font1.getSize() == 0) {
	// font1 = new Font("dialog", 0, 12);
	// }
	// setFont(font1);
	// }
	// // TODO: add a Listener
	// table.getSelectionModel().addListSelectionListener(this);
	// }
	//
	// public Component getListCellRendererComponent(JList list, Object value,
	// int index, boolean isSelected, boolean cellHasFocus) {
	// setText((value == null) ? "" : value.toString());
	// if (minSelectionRow != -1 && maxSelectionRow != -1) {
	// if (index >= minSelectionRow && index <= maxSelectionRow
	// && selectionModel.isSelectedIndex(index)) {
	// setBackground(CASTable.SELECTED_BACKGROUND_COLOR_HEADER);
	// } else {
	// setBackground(defaultBackground);
	// }
	// } else {
	// setBackground(defaultBackground);
	// }
	// return this;
	// }
	//
	// public void valueChanged(ListSelectionEvent e) {
	// selectionModel = (ListSelectionModel) e.getSource();
	// minSelectionRow = selectionModel.getMinSelectionIndex();
	// maxSelectionRow = selectionModel.getMaxSelectionIndex();
	// rowHeader.repaint();
	// }
	//
	// }

	protected int row0 = -1;

	// Key Listener for Console Table
	protected class ConsoleTableKeyListener implements KeyListener {

		public void keyTyped(KeyEvent e) {
			// System.out.println("Key typed on rowheader");
			e.consume();
		}

		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();

			boolean metaDown = Application.isControlDown(e);
			boolean altDown = e.isAltDown();

			// System.out.println("Key pressed on rowheader");
			// Application.debug(keyCode);
			switch (keyCode) {

			case KeyEvent.VK_DELETE: // delete
			case KeyEvent.VK_BACK_SPACE: // delete on MAC
				if (minSelectionRow != -1 && maxSelectionRow != -1) {
					int[] delRows = consoleTable.getSelectedRows();
					int delRowsSize = delRows.length;
					int i = 0;
					while (i < delRowsSize) {
						int delRow = delRows[i];
						consoleTable.deleteRow(delRow - i);
						System.out.println("Key Delete row : " + delRow);
						i++;
					}
				}
				System.out.println("Key Delete or BackSpace Action Performed ");
				break;
			default:
				e.consume();
			}
		}

		public void keyReleased(KeyEvent e) {
			// System.out.println("Key Released on rowheader");
			e.consume();
		}

	}

	// Listener for RowHeader
	protected class RowHeaderMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {

		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			boolean shiftPressed = e.isShiftDown();
			boolean metaDown = Application.isControlDown(e);
			boolean rightClick = Application.isRightClick(e);

			int x = e.getX();
			int y = e.getY();

			// rowHeaderTable.requestFocus();
			//
			consoleTable.requestFocusInWindow();			
			
			// TODO: remove
			// Component compFocusOwner = KeyboardFocusManager
			// .getCurrentKeyboardFocusManager().getFocusOwner();
			// System.out.println("focus owner: " + compFocusOwner);
			System.out.println("consoleTable has Focus: "
					+ consoleTable.isFocusOwner());
			System.out.println("rowHeaderTable has Focus: "
					+ rowHeaderTable.isFocusOwner());

			// TODO: remive the left click handling here and just set the
			// selection model once when the consoleTable is created
			// left click
			if (!rightClick) {
				Point point = consoleTable.getIndexFromPixel(x, y);
				if (point != null) {
					if (consoleTable.getSelectionModel().getSelectionMode() != ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
							|| consoleTable.getColumnSelectionAllowed() == true) {
						consoleTable
								.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
						consoleTable.setColumnSelectionAllowed(false);
						consoleTable.setRowSelectionAllowed(true);
					}
					if (shiftPressed) {
						if (row0 != -1) {
							int row = (int) point.getY();
							consoleTable.setRowSelectionInterval(row0, row);
						}
					} else if (metaDown) {
						row0 = (int) point.getY();
						consoleTable.addRowSelectionInterval(row0, row0);
					} else {
						row0 = (int) point.getY();
						consoleTable.setRowSelectionInterval(row0, row0);
					}
					// consoleTable.repaint();
				}

				// Todo: Remove, for debug information
				// System.out.println("Selected number of rows is: "
				// + consoleTable.getSelectedRowCount());
				// for (int i = 0; i < consoleTable.getSelectedRowCount(); i++)
				// {
				// System.out.println(consoleTable.getSelectedRows()[i]);
				// }

			}
			// RIGHT CLICK
			else {
				if (!app.letShowPopupMenu())
					return;

				Point point = consoleTable.getIndexFromPixel(x, y);
				if (point != null) {
					int row0 = (int) point.getY();

					if (!consoleTable.isRowSelected(row0)) // This row is
						// already selected
						consoleTable.setRowSelectionInterval(row0, row0);
				}

				if (minSelectionRow != -1 && maxSelectionRow != -1) {
					CASContextMenuRow popupMenu = new CASContextMenuRow(
							consoleTable, 0, minSelectionRow, consoleTable
									.getModel().getColumnCount() - 1,
							maxSelectionRow, new boolean[0]);
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}

			}
		}

	}

	public class RowHeaderTableRenderer extends JLabel implements
			TableCellRenderer, ListSelectionListener

	{

		JTable reftable;
		protected JTableHeader header;
		protected ListSelectionModel selectionModel;

		public RowHeaderTableRenderer(JTable reftable)

		{

			this.reftable = reftable;
			header = reftable.getTableHeader();

			reftable.getSelectionModel().addListSelectionListener(this);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int col)

		{

			CASTableModel casTableModel = (CASTableModel) table.getModel();

			CASTableCellValue cellValue = (CASTableCellValue) casTableModel
					.getValueAt(row, col);

			if (cellValue.isOutputVisible())
				table.setRowHeight(row, CASPara.inputOutputHeight);
			else
				table.setRowHeight(row, CASPara.inputLineHeight);

			JTableHeader header = reftable.getTableHeader();

			this.setOpaque(true);

			/* Set the border of TableHeader */

			setBorder(UIManager.getBorder("TableHeader.cellBorder"));

			setHorizontalAlignment(CENTER);

			setBackground(header.getBackground());

			Font font = header.getFont();

			if (isSelect(row)) /* if seleted, set the color of row header */

			{

				setForeground(Color.white);

				setBackground(Color.LIGHT_GRAY);

				setFont(font);

				setText(String.valueOf(row + 1));

			}

			else

			{

				setForeground(header.getForeground());

				setFont(font);

				setText(String.valueOf(row + 1));

			}

			return this;

		}

		private boolean isSelect(int row)

		{

			int[] sel = reftable.getSelectedRows();

			for (int i = 0; i < sel.length; i++)

			{

				if (sel[i] == row)
					return true;

			}

			return false;

		}

		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			selectionModel = (ListSelectionModel) e.getSource();
			minSelectionRow = selectionModel.getMinSelectionIndex();
			maxSelectionRow = selectionModel.getMaxSelectionIndex();
		}

	}

	public GeoGebraCAS getCAS() {
		return cas;
	}

	public CASTable getConsoleTable() {
		return consoleTable;
	}

	/**
	 * returns settings in XML format
	 */
	public String getGUIXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<casView>\n");

		int width = getWidth(); // getPreferredSize().width;
		int height = getHeight(); // getPreferredSize().height;

		// if (width > MIN_WIDTH && height > MIN_HEIGHT)
		{
			sb.append("\t<size ");
			sb.append(" width=\"");
			sb.append(width);
			sb.append("\"");
			sb.append(" height=\"");
			sb.append(height);
			sb.append("\"");
			sb.append("/>\n");
		}

		sb.append("</casView>\n");
		return sb.toString();
	}

	public String getSessionXML() {

		CASTableModel tableModel = (CASTableModel) consoleTable.getModel();

		StringBuffer sb = new StringBuffer();
		sb.append("<casSession>\n");

		// get the number of pairs in the view
		int numOfRows = tableModel.getRowCount();

		// get the content of each pair in the table with a loop
		// append the content to the string sb
		for (int i = 0; i < numOfRows; ++i) {
			CASTableCellValue temp = (CASTableCellValue) tableModel.getValueAt(
					i, CASPara.contCol);
			sb.append(temp.getXML());
		}

		sb.append("</casSession>\n");
		return sb.toString();
	}
	
	/**
	 * Returns the output string in the n-th row of this CAS view. 
	 * If the n-th cell has no output string, the input string of this cell is returned.
	 */
	public String getRowValue(int n) {
		CASTableModel tableModel = (CASTableModel) consoleTable.getModel();
		CASTableCellValue temp = (CASTableCellValue) tableModel.getValueAt(n, CASPara.contCol);
		
		String result = temp.getOutput();
		if (result == null || result.length() == 0)
			result = temp.getInput();
		
		return result;
	}
	
	/**
	 * Returns the number of rows of this CAS view.
	 */
	public int getRowCount() {
		return consoleTable.getRowCount();
	}

	public JComponent getCASViewComponent() {
		return this;
	}

	/**
	 * Loads
	 * 
	 * @param cellPairList
	 */
	public void initCellPairs(LinkedList cellPairList) {
		// Delete the current rows
		consoleTable.deleteAllRow();

		if (cellPairList == null) {
			CASTableCellValue cellPair = new CASTableCellValue();
			consoleTable.insertRow(-1, 0, cellPair);
		} else {
			Iterator it = cellPairList.iterator();
			boolean firstElementFlag = true;
			while (it.hasNext()) {
				CASTableCellValue cellPair = (CASTableCellValue) it.next();
				if (firstElementFlag) {
					consoleTable.insertRow(-1, 0, cellPair);
					firstElementFlag = false;
				} else
					consoleTable.insertRow(cellPair);
			}
		}

		// Set the focus at the right cell
		// table.setFocusAtRow(table.getRowCount() - 1,
		// geogebra.cas.view.CASPara.contCol);
	}

	public Object setInputExpression(Object cellValue, String input) {
		if (cellValue instanceof CASTableCellValue) {
			((CASTableCellValue) cellValue).setInput(input);
		}
		return cellValue;
	}

	public Object setOutputExpression(Object cellValue, String output) {
		if (cellValue instanceof CASTableCellValue) {
			((CASTableCellValue) cellValue).setOutput(output);
			((CASTableCellValue) cellValue).setOutputAreaInclude(true);
		}
		return cellValue;
	}

	public Object createCellValue() {
		CASTableCellValue cellValue = new CASTableCellValue();
		return cellValue;
	}

	private void initButtons() {
		JButton ret = null;
		
		ButtonListener btListener = new ButtonListener();
		
		// evaluate
		btEval = new JButton("=");
		btEval.setActionCommand("Simplify");
		btEval.addActionListener(btListener);

		// expand
		btExp = new JButton(app.getPlain("Expand"));
		btExp.setActionCommand("Expand");
		btExp.addActionListener(btListener);

		// factor
		btFactor = new JButton("Factor");
		btFactor.setActionCommand("Factor");
		btFactor.addActionListener(btListener);
		
		// substitute
		btSub = new JButton(app.getPlain("Substitute"));
		btSub.setActionCommand("Subsim");
		btSub.addActionListener(btListener);

	}

	protected class ButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			
			
			Object src = ae.getSource();
			if (src == btSub) {
				apply(SUB_Flag);
			} else if (src == btEval) {
				apply(EVAL_Flag);
			} else if (src == btExp) {
				apply(EXP_Flag);
			} else if (src == btFactor) {
				apply(FAC_Flag);
			}

		}

		private void apply(int mod) {				
			// get editor and possibly selected text
			CASTableCellEditor cellEditor = consoleTable.getEditor();
			String selectedText = cellEditor == null ? null : cellEditor.getInputSelectedText();
			int selStart = cellEditor.getInputSelectionStart();
			int selEnd = cellEditor.getInputSelectionEnd();
			
			// TODO: remove
			System.out.println("selectedText: " + selectedText + ", selStart: " + selStart + ", selEnd: " + selEnd);					
		
			
			// save the edited value into the table model
			consoleTable.stopEditing();
			
			// get current row and input text		
			int selRow = consoleTable.getSelectedRow();			
			CASTableCellValue cellValue = consoleTable.getCASTableCellValue(selRow);
			String selRowInput = cellValue.getInput();
			
			// break text into prefix, evalText, postfix
			String prefix, evalText, postfix;
			CASTableCellValue outputCellValue;
			boolean useSameCell = selectedText == null || selectedText.trim().length() == 0;
			if (useSameCell) {
				// no selected text: evaluate input using current cell
				prefix = "";
				evalText = selRowInput;
				postfix = "";		
				outputCellValue = cellValue;
			}
			else {
				// selected text: break it up into prefix, evalText, and postfix
				prefix = selRowInput.substring(0, selStart);
				evalText = selectedText;
				postfix = selRowInput.substring(selEnd);
				// use new output cell
				outputCellValue = new CASTableCellValue();
			}
						
			
			
			// TODO: remove
			System.out.println("SELECTED ROW: " + selRow + ", prefix: " + prefix + ", evalText: " + evalText + ", postfix: " + postfix);					
	
			switch (mod) {
				case SUB_Flag:
					// Create a CASSubDialog with the cell value
					CASSubDialog d = new CASSubDialog(CASView.this, cellValue, evalText, selRow);
					d.setVisible(true);
					break;
					
				case EVAL_Flag:	
					evalText = "Simplify[" + evalText + "]";				
					break;
					
				case EXP_Flag:
					evalText = "Expand[" + evalText + "]";	
					break;
					
				case FAC_Flag:
					evalText = "Factor[" + evalText + "]";	
					break;
			}
			
			// process evalText
			String result;
			boolean error;
			try {
				evalText = cas.processCASInput(evalText, true, useGeoGebraVariableValues);
				result = prefix + evalText + postfix;	
				error = false;
			} catch (Throwable e) {					
				e.printStackTrace();	
				result = cas.getMathPiperError();
				error = true;
			}
			
			// set new values of output cell
			outputCellValue.setInput(selRowInput);						
			outputCellValue.setOutput(result, error);	
			
	// TODO: check how to update output panel in cell 
			
			// update output cell
			DefaultTableModel model = (DefaultTableModel) consoleTable.getModel();			
			if (useSameCell) {
				//model.fireTableCellUpdated(selRow, CASPara.contCol);
				consoleTable.insertRow(selRow-1, CASPara.contCol, outputCellValue);
			} else {
				consoleTable.insertRow(selRow, CASPara.contCol, outputCellValue);				
			}
			//SwingUtilities.updateComponentTreeUI(consoleTable);			
		}

	}

	public JButton getBtSub() {
		return btSub;
	}

	public Application getApp() {
		return app;
	}
	
	public final boolean isUseGeoGebraVariableValues() {
		return useGeoGebraVariableValues;
	}

	public final void setUseGeoGebraVariableValues(boolean useGeoGebraVariableValues) {
		this.useGeoGebraVariableValues = useGeoGebraVariableValues;
	}


}