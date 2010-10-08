package geogebra.euclidian;

import geogebra.gui.util.GeoGebraIcon;
import geogebra.gui.util.PopupMenuButton;
import geogebra.gui.util.SelectionTable;
import geogebra.kernel.Construction;
import geogebra.kernel.ConstructionDefaults;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoImage;
import geogebra.kernel.GeoList;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.GeoText;
import geogebra.kernel.PointProperties;
import geogebra.main.Application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
/**
 * Stylebar for the Euclidian Views
 * 
 * @author G. Sturr 
 *
 */
public class EuclidianStyleBar extends JToolBar implements ActionListener {
		
	private PopupMenuButton btnColor, btnTextColor, btnLineStyle, btnPointStyle, btnTextSize, btnMode;
	private PopupMenuButton[] popupBtnList;
			
	
	private MyToggleButton btnCopyVisualStyle, btnPen, btnShowGrid, btnShowAxes,
    		btnBold, btnItalic, btnDelete, btnLabel, btnEraser;
	private MyToggleButton[] toggleBtnList;

	private JButton btnPenDelete, btnDeleteGeo, btnHideLabel;
	
	private EuclidianController ec;
	private EuclidianView ev;
	private Application app;
	private Integer[] lineStyleArray, pointStyleArray;
	private HashMap<Integer, Integer> lineStyleMap, pointStyleMap;
	
	private HashMap<Integer,Integer> defaultGeoMap;
	private Construction cons; 
	
	
	
	private int maxIconHeight = 20;	
	
	private int mode;
	
	private Color[] colors, textColors;
	private HashMap<Color,Integer> colorMap, textColorMap;
	
	private boolean isIniting;
	
	
	
	/*************************************************
	 * Constructs a styleBar
	 */
	public EuclidianStyleBar(EuclidianView ev) {
		
		isIniting = true;
		this.ev = ev;
		ec = ev.getEuclidianController(); 
		app = ev.getApplication();
		cons = app.getKernel().getConstruction();
		createDefaultMap();
		
		
		setFloatable(false);

		Dimension d = getPreferredSize();
		d.height = maxIconHeight+8;
		setPreferredSize(d);

		
		colors = getStyleBarColors(false);
		colorMap = new HashMap<Color,Integer>();
		for(int i = 0; i < colors.length; i++)
			colorMap.put(colors[i], i);
		
		textColors = getStyleBarColors(true);
		textColorMap = new HashMap<Color,Integer>();
		for(int i = 0; i < textColors.length; i++)
			textColorMap.put(textColors[i], i);
		
		

		pointStyleArray = ev.getPointStyles();
		pointStyleMap = new HashMap<Integer,Integer>();
		for(int i = 0; i < pointStyleArray.length; i++)
			pointStyleMap.put(pointStyleArray[i], i);

		lineStyleArray = ev.getLineTypes();
		lineStyleMap = new HashMap<Integer,Integer>();
		for(int i = 0; i < lineStyleArray.length; i++)
			lineStyleMap.put(lineStyleArray[i], i);
		
		initGUI();
		isIniting = false;
		
		setMode(ev.getMode());
		updateStyleBar();
	}
	
	
	public int getMode() {
		return mode;
	}


	public void setMode(int mode) {
		this.mode = mode;	
		updateStyleBar();
		updateGUI();
	}
	
	public void add(GeoElement geo) {
		
		applyVisualStyle(geo);
		
	}
	
	
	
	//=====================================================
	//                   GUI
	//=====================================================
	
	private void initGUI() {
	
		
		//========================================
		// mode button
		
		String[] modeArray = new String[]{
				"cursor_arrow.png",
				"applications-graphics.png",
				"delete_small.gif",
				"mode_point_16.gif",
				"mode_copyvisualStyle_16.png"
		};
		btnMode = new PopupMenuButton(ev.getApplication(), modeArray, -1,1,
				new Dimension(20,maxIconHeight), SelectionTable.MODE_ICON);
		btnMode.addActionListener(this);
		btnMode.setKeepVisible(false);
		//add(btnMode);
		
		
		//========================================
		// pen button
		btnPen = new MyToggleButton(ev.getApplication().getImageIcon("applications-graphics.png")){
		      public void update(Object[] geos) {
					this.setVisible((geos.length == 0 && mode == EuclidianView.MODE_MOVE) || mode == EuclidianView.MODE_PEN);	  
			      }
			};
		btnPen.addActionListener(this);
		//add(btnPen);

		
		//========================================
		// delete button
		btnDelete = new MyToggleButton(ev.getApplication().getImageIcon("delete_small.gif")){
		      public void update(Object[] geos) {
					this.setVisible((geos.length == 0 && mode == EuclidianView.MODE_MOVE)  || mode == EuclidianView.MODE_DELETE);	  
			      }
			};
		btnDelete.addActionListener(this);
		//add(btnDelete);
		
		
		
		//========================================
		// hide/show labels button
		btnLabel = new MyToggleButton(ev.getApplication().getImageIcon("mode_copyvisualStyle_16.png")){
		      public void update(Object[] geos) {
					this.setVisible((geos.length == 0 && mode == EuclidianView.MODE_MOVE) || mode == EuclidianView.MODE_SHOW_HIDE_LABEL);	  
			      }
			};
		btnLabel.addActionListener(this);
		//add(btnLabel);
		
		
		//========================================
		// visual style button
		
		btnCopyVisualStyle = new MyToggleButton(ev.getApplication().getImageIcon("mode_copyvisualStyle_16.png")){
		      public void update(Object[] geos) {
				this.setVisible( (geos.length > 0 && mode == EuclidianView.MODE_MOVE) || mode == EuclidianView.MODE_VISUAL_STYLE);	  
		      }
		};
		btnCopyVisualStyle.addActionListener(this);
		//add(this.btnCopyVisualStyle);
		
		
		
		//this.addSeparator();
		
		
		//========================================
		// show axes button
		
		btnShowAxes = new MyToggleButton(ev.getApplication().getImageIcon("axes.gif")){
		      public void update(Object[] geos) {
				this.setVisible(geos.length == 0  && mode != EuclidianView.MODE_PEN);	  
		      }
		};
		
		btnShowAxes.setPreferredSize(new Dimension(16,16));
		btnShowAxes.addActionListener(this);
		add(btnShowAxes);
		
		
		
		//========================================
		// show grid button
		
		btnShowGrid = new MyToggleButton(ev.getApplication().getImageIcon("grid.gif")){
		      public void update(Object[] geos) {
					this.setVisible(geos.length == 0 && mode != EuclidianView.MODE_PEN);	  
			      }
			};
			
		btnShowGrid.setPreferredSize(new Dimension(16,16));
		btnShowGrid.addActionListener(this);
		add(btnShowGrid);
		
		
		
		
		//========================================
		// object color button  (color for everything except text)
		
		btnColor = new PopupMenuButton(ev.getApplication(), colors, -1,8,
				new Dimension(20,maxIconHeight), SelectionTable.MODE_COLOR_SWATCH) {

			public void update(Object[] geos) {

				if( mode == EuclidianView.MODE_PEN){
					this.setVisible(true);
					setSelectedIndex(colorMap.get(ec.getPen().getPenColor()));
					setSliderValue(100);
					getMySlider().setVisible(false);

				}else{


					boolean geosOK = (geos.length > 0 || mode == EuclidianView.MODE_PEN);
					for (int i = 0; i < geos.length; i++) {
						GeoElement geo = (GeoElement)geos[i];
						if (geo instanceof GeoImage || geo instanceof GeoText  )
							geosOK = false;
						break;
					}

					setVisible(geosOK);

					if(geosOK){

						Color geoColor = ((GeoElement) geos[0]).getObjectColor();
						float alpha = 1.0f;
						boolean geoFillable = true;
						
						// check for fillable geo
						// if true, then set slider to first geo's alpha value
						for (int i = 0; i < geos.length; i++) {
							if (!((GeoElement) geos[i]).isFillable()) {
								geoFillable = false;
								break;
							}
						}
						if(geoFillable)
							alpha = ((GeoElement) geos[0]).getAlphaValue();
						
						//set the button
						setButton(geoColor, alpha, geoFillable);
						
						this.setKeepVisible(mode == EuclidianView.MODE_MOVE);
					}
				}
			}
			
			public void setDefault(){
				//Color geoColor = ((GeoElement) geos[0]).getObjectColor();
				
			}
			
			private void setButton(Color color, float alpha, boolean showSlider){
				
				int index;
				if(colorMap.containsKey(color)){
					colors[colors.length-1] = Color.WHITE;
					index = colorMap.get(color);
				}else{		
					colors[colors.length-1] = color;
					index = colors.length-1;
				}

				getMyTable().populateModel(colors);					
				setSelectedIndex(index);
				
				
				setSliderValue((int) Math.round(alpha * 100));
				getMySlider().setVisible(showSlider);
				
				
			}
			

		};

		btnColor.getMySlider().setMinimum(0);
		btnColor.getMySlider().setMaximum(100);
		btnColor.getMySlider().setMajorTickSpacing(25);
		btnColor.getMySlider().setMinorTickSpacing(5);
		btnColor.setSliderValue(50);
		btnColor.addActionListener(this);
		add(btnColor);
		
		
		//========================================
		// text color  button
		
		btnTextColor = new PopupMenuButton(ev.getApplication(), textColors, -1,8,
				new Dimension(20,maxIconHeight), SelectionTable.MODE_COLOR_SWATCH_TEXT) {

			public void update(Object[] geos) {

				boolean geosOK = (geos.length > 0);
				for (int i = 0; i < geos.length; i++) {
					if (!(((GeoElement)geos[i]).getGeoElementForPropertiesDialog().isGeoText())) {
						geosOK = false;
						break;
					}
				}
				setVisible(geosOK);
				
				if(geosOK){			
					Color geoColor = ((GeoElement) geos[0]).getObjectColor();
					int index;
					if(textColorMap.containsKey(geoColor)){
						textColors[textColors.length-1] = Color.WHITE;
						index = textColorMap.get(geoColor);
					}else{		
						textColors[textColors.length-1] = geoColor;
						index = textColors.length-1;
					}
					btnTextColor.getMyTable().populateModel(textColors);					
					setSelectedIndex(index);
				//	setFgColor(((GeoElement)geos[0]).getObjectColor());
					setFgColor(Color.black);
					
					setFontStyle(((GeoText)geos[0]).getFontStyle());
				}
			}

		};
		btnTextColor.getMySlider().setVisible(false);
		btnTextColor.setSliderValue(100);
		btnTextColor.addActionListener(this);
		add(btnTextColor);

		
		//========================================
		// line style button
		
		btnLineStyle = new PopupMenuButton(ev.getApplication(), lineStyleArray, -1,1,
				new Dimension(80,maxIconHeight), SelectionTable.MODE_LINESTYLE){

			public void update(Object[] geos) {

				if( mode == EuclidianView.MODE_PEN){
					this.setVisible(true);
					setFgColor(ec.getPen().getPenColor());
					setSliderValue(ec.getPen().getPenSize());
					setSelectedIndex(lineStyleMap.get(ec.getPen().getPenLineStyle()));
				}else{
				
				boolean geosOK = (geos.length > 0);
				for (int i = 0; i < geos.length; i++) {
					GeoElement geo = (GeoElement)geos[i];
					if (!(geo.isPath()
							|| geo.isGeoPolygon()
							|| (geo.isGeoLocus() && ((GeoList)geo).showLineProperties() )
							|| geo.isGeoList()
							|| (geo.isGeoNumeric()
									&& ((GeoNumeric) geo).isDrawable()))) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);

				if(geosOK){	
					//setFgColor(((GeoElement)geos[0]).getObjectColor());
					
					setFgColor(Color.black);
					setSliderValue( ((GeoElement)geos[0]).getLineThickness());
					setSelectedIndex(lineStyleMap.get(((GeoElement)geos[0]).getLineType()));
					this.setKeepVisible(mode == EuclidianView.MODE_MOVE);
				}							
			}
			}

		};
		
		btnLineStyle.getMySlider().setMinimum(1);
		btnLineStyle.getMySlider().setMaximum(13);
		btnLineStyle.getMySlider().setMajorTickSpacing(2);
		btnLineStyle.getMySlider().setMinorTickSpacing(1);
		btnLineStyle.getMySlider().setPaintTicks(true);	
		btnLineStyle.addActionListener(this);
		add(btnLineStyle);
		
		
		
		//========================================
		// point style button

		btnPointStyle = new PopupMenuButton(ev.getApplication(), pointStyleArray, 2, -1, 
				new Dimension(20, maxIconHeight), SelectionTable.MODE_POINTSTYLE){

			public void update(Object[] geos) {

				boolean geosOK = (geos.length > 0 );
				for (int i = 0; i < geos.length; i++) {
					GeoElement geo = (GeoElement)geos[i];
					if (!(geo.getGeoElementForPropertiesDialog().isGeoPoint())
							&& (!(geo.isGeoList() && ((GeoList)geo).showPointProperties()))) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);

				if(geosOK){					
					//setFgColor(((GeoElement)geos[0]).getObjectColor());
					setFgColor(Color.black);
					
					setSliderValue( ((PointProperties)geos[0]).getPointSize());
					//setSelectedIndex(pointStyleMap.get(((PointProperties)geos[0]).getPointStyle()));
					this.setKeepVisible(mode == EuclidianView.MODE_MOVE);
				}
			}		  
		};
		btnPointStyle.getMySlider().setMinimum(1);
		btnPointStyle.getMySlider().setMaximum(9);
		btnPointStyle.getMySlider().setMajorTickSpacing(2);
		btnPointStyle.getMySlider().setMinorTickSpacing(1);
		btnPointStyle.getMySlider().setPaintTicks(true);		
		btnPointStyle.addActionListener(this);
		add(btnPointStyle);
			
		
		//========================================
		// bold text button
		
		btnBold = new MyToggleButton(new GeoGebraIcon()){
			public void update(Object[] geos) {
				boolean geosOK = (geos.length > 0 );
				for (int i = 0; i < geos.length; i++) {
					if (!(((GeoElement)geos[i]).getGeoElementForPropertiesDialog().isGeoText())) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);
				if(geosOK){	
					int style = ((GeoText)geos[0]).getFontStyle();
					btnBold.setSelected(style == Font.BOLD || style == (Font.BOLD + Font.ITALIC));		
				}
			}		  
		};
		btnBold.setPreferredSize(new Dimension(maxIconHeight, maxIconHeight));
		//btnBold.setText(app.getPlain("Bold").substring(0,1));
		//btnBold.setFont((new Font (app.getPlainFont().getFamily(),Font.BOLD,maxIconHeight-4)));
		btnBold.setForeground(Color.BLACK);
		btnBold.addActionListener(this);
		add(btnBold);
		
		
		
		//========================================
		// italic text button
		
		btnItalic = new MyToggleButton(new GeoGebraIcon()){
			public void update(Object[] geos) {
				boolean geosOK = (geos.length > 0 );
				for (int i = 0; i < geos.length; i++) {
					if (!(((GeoElement)geos[i]).getGeoElementForPropertiesDialog().isGeoText())) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);
				if(geosOK){	
					int style = ((GeoText)geos[0]).getFontStyle();
					btnItalic.setSelected(style == Font.ITALIC || style == (Font.BOLD + Font.ITALIC));
				}
			}	
			
		};
		btnItalic.setPreferredSize(new Dimension(maxIconHeight, maxIconHeight));
		//btnItalic.setText(app.getPlain("Italic").substring(0,1));
		//btnItalic.setFont((new Font (app.getPlainFont().getFamily(),Font.ITALIC,maxIconHeight-4)));
		btnItalic.setForeground(Color.BLACK);
		btnItalic.addActionListener(this);
		add(btnItalic);
	
		
		//========================================
		// text size button
		
		String[] textSizeArray = new String[] { 
				app.getPlain("ExtraSmall"), 
				app.getPlain("Small"), 
				app.getPlain("Medium"), 
				app.getPlain("Large"), 
				app.getPlain("ExtraLarge") };
		
		
		btnTextSize = new PopupMenuButton(ev.getApplication(), textSizeArray, -1, 1, 
				new Dimension(80, maxIconHeight), SelectionTable.MODE_TEXT){

			public void update(Object[] geos) {

				boolean geosOK = (geos.length > 0 );
				for (int i = 0; i < geos.length; i++) {
					GeoElement geo = (GeoElement)geos[i];
					if (!(((GeoElement)geos[i]).getGeoElementForPropertiesDialog().isGeoText())) {
						geosOK = false;
						break;
					}
				}
				this.setVisible(geosOK);

				if(geosOK){								
					setSelectedIndex(((GeoText)geos[0]).getFontSize() / 2 + 2); // font size ranges from -4 to 4, transform this to 0,1,..,4
				}
			}		  
		};	
		btnTextSize.addActionListener(this);
		add(btnTextSize);
	
		
		
		//========================================
		// eraser button
		GeoGebraIcon ic = new GeoGebraIcon();
		ic.setImage(app.getImageIcon("delete_small.gif").getImage());
		ic.ensureIconSize(new Dimension(maxIconHeight,maxIconHeight));
		btnEraser = new MyToggleButton(ic){
			public void update(Object[] geos) {
				this.setVisible(mode == EuclidianView.MODE_PEN);
			}	
		};
		btnEraser.addActionListener(this);
		add(btnEraser);
		
		
		//this.addSeparator();
		
		//========================================
		// delete geo button
		btnDeleteGeo = new JButton(app.getImageIcon("delete_small.gif"));
		btnDeleteGeo.addActionListener(this);
		//add(btnDeleteGeo);
		
		//========================================
		// hide label button
		btnHideLabel = new JButton(app.getImageIcon("mode_point_16.gif"));
		btnHideLabel.addActionListener(this);
		//add(btnHideLabel);
		
		
		
		
		
		popupBtnList = new PopupMenuButton[]{
				btnColor, btnTextColor, btnLineStyle, btnPointStyle, btnTextSize};
		
		toggleBtnList = new MyToggleButton[]{
				btnCopyVisualStyle, btnPen, btnShowGrid, btnShowAxes,
	            btnBold, btnItalic, btnDelete, btnLabel, btnEraser};	
		
	}

	
	
	
	
	private void updateGUI(){

		if(isIniting) return;

		btnMode.removeActionListener(this);
		switch (mode){
		case EuclidianView.MODE_MOVE:
			btnMode.setSelectedIndex(0);
			break;
		case EuclidianView.MODE_PEN:
			btnMode.setSelectedIndex(1);
			break;
		case EuclidianView.MODE_DELETE:
			btnMode.setSelectedIndex(2);
			break;
		case EuclidianView.MODE_SHOW_HIDE_LABEL:
			btnMode.setSelectedIndex(3);
			break;
		case EuclidianView.MODE_VISUAL_STYLE:
			btnMode.setSelectedIndex(4);
			break;
		}
		btnMode.addActionListener(this);


		
		
		btnPen.removeActionListener(this);
		btnPen.setSelected(mode == EuclidianView.MODE_PEN);
		btnPen.addActionListener(this);
		
		btnDelete.removeActionListener(this);
		btnDelete.setSelected(mode == EuclidianView.MODE_DELETE);
		btnDelete.addActionListener(this);
		
		btnLabel.removeActionListener(this);
		btnLabel.setSelected(mode == EuclidianView.MODE_SHOW_HIDE_LABEL);
		btnLabel.addActionListener(this);
		
		btnShowAxes.removeActionListener(this);
		btnShowAxes.setSelected(ev.getShowXaxis());
		btnShowAxes.addActionListener(this);

		btnShowGrid.removeActionListener(this);
		btnShowGrid.setSelected(ev.getShowGrid());
		btnShowGrid.addActionListener(this);
	
		// draw icons for the the bold and italic symbols
		// using icons instead of text keeps our button width fixed in the toolbar
		((GeoGebraIcon)btnBold.getIcon()).createCharIcon(app.getPlain("Bold").substring(0,1),
				app.getPlainFont(), true, false, new Dimension(maxIconHeight,maxIconHeight), btnBold.getForeground(), null);
		
		((GeoGebraIcon)btnItalic.getIcon()).createCharIcon(app.getPlain("Italic").substring(0,1),
				app.getPlainFont(), false, true, new Dimension(maxIconHeight,maxIconHeight), btnItalic.getForeground(), null);
		
	}


	public void updateStyleBar(){
	
		if(mode == EuclidianView.MODE_VISUAL_STYLE)
			return;
		ArrayList<GeoElement> geos = new ArrayList<GeoElement>();
		
		
		if(mode == EuclidianView.MODE_MOVE){
			geos = ev.getApplication().getSelectedGeos();
		}
		else if (defaultGeoMap.containsKey(mode)){
			geos.add(cons.getConstructionDefaults().getDefaultGeo(defaultGeoMap.get(mode)));
		}
		
		
		for(int i = 0; i < popupBtnList.length; i++){
			popupBtnList[i].update(geos.toArray());
		}
		for(int i = 0; i < toggleBtnList.length; i++){
			toggleBtnList[i].update(geos.toArray());
		}
		
		btnDeleteGeo.setVisible(geos.toArray().length >0);
		btnHideLabel.setVisible(geos.toArray().length >0);	
	}
	
	private void createDefaultMap(){
		defaultGeoMap = new HashMap<Integer,Integer>();
		defaultGeoMap.put(EuclidianView.MODE_POINT, ConstructionDefaults.DEFAULT_POINT_FREE);
		defaultGeoMap.put(EuclidianView.MODE_POLYGON, ConstructionDefaults.DEFAULT_POLYGON);
		defaultGeoMap.put(EuclidianView.MODE_TEXT, ConstructionDefaults.DEFAULT_TEXT);
		defaultGeoMap.put(EuclidianView.MODE_JOIN, ConstructionDefaults.DEFAULT_LINE);
		
	}
	
	
	
	
	
	
	//=====================================================
	//                 Event Handlers
	//=====================================================
	

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source.equals(btnMode)) {
			switch (btnMode.getSelectedIndex()){
			case 0:
				ev.getApplication().setMoveMode();
				break;
			case 1:
				ev.getApplication().setMode(EuclidianView.MODE_PEN);
				break;
			case 2:
				ev.getApplication().setMode(EuclidianView.MODE_DELETE);	
				break;
			case 3:
				ev.getApplication().setMode(EuclidianView.MODE_SHOW_HIDE_LABEL);
				break;
			case 4:
				ev.getApplication().setMode(EuclidianView.MODE_VISUAL_STYLE);
			}
		}

		
		
		if (source.equals(btnCopyVisualStyle)) {		
			if(btnCopyVisualStyle.isSelected())
				ev.getApplication().setMode(EuclidianView.MODE_VISUAL_STYLE);
			else
				ev.getApplication().setMoveMode();
		}
		
		
		if (source.equals(btnPen)) {		
			if(btnPen.isSelected())
				ev.getApplication().setMode(EuclidianView.MODE_PEN);
			else
				ev.getApplication().setMoveMode();
		}
		
		if (source.equals(btnDelete)) {		
			if(btnDelete.isSelected())
				ev.getApplication().setMode(EuclidianView.MODE_DELETE);
			else
				ev.getApplication().setMoveMode();
		}
		
		if (source.equals(btnLabel)) {		
			if(btnLabel.isSelected())
				ev.getApplication().setMode(EuclidianView.MODE_SHOW_HIDE_LABEL);
			else
				ev.getApplication().setMoveMode();
		}
		
		
		
		if (source.equals(btnShowAxes)) {		
			ev.setShowAxes(!ev.getShowXaxis(), true);
			ev.repaint();
		}
		
		else if (source.equals(btnShowGrid)) {
			ev.showGrid(!ev.getShowGrid());
			ev.repaint();
		}
		else if (source == btnColor) {
			if(btnColor.getSelectedValue() != null){
				if(mode == EuclidianView.MODE_MOVE){
					applyVisualStyle(app.getSelectedGeos());
					//btnLineStyle.setFgColor((Color)btnColor.getSelectedValue());
					//btnPointStyle.setFgColor((Color)btnColor.getSelectedValue());
				}
				else if(mode == EuclidianView.MODE_PEN){
					ec.getPen().setPenColor((Color) btnColor.getSelectedValue());
					//btnLineStyle.setFgColor((Color)btnColor.getSelectedValue());
				}

			}
		}
		else if (source == btnTextColor) {
			if(btnTextColor.getSelectedValue() != null){
				applyVisualStyle(app.getSelectedGeos());
				//btnTextColor.setFgColor((Color)btnTextColor.getSelectedValue());
				//btnItalic.setForeground((Color)btnTextColor.getSelectedValue());
				//btnBold.setForeground((Color)btnTextColor.getSelectedValue());
			}
		}
		else if (source == btnLineStyle) {
			if(btnLineStyle.getSelectedValue() != null){
				if(mode == EuclidianView.MODE_MOVE)
					applyVisualStyle(app.getSelectedGeos());
				else if(mode == EuclidianView.MODE_PEN){
					ec.getPen().setPenLineStyle((Integer) btnLineStyle.getSelectedValue());
					ec.getPen().setPenSize((Integer) btnLineStyle.getSliderValue());
				}
					
			}
		}
		else if (source == btnPointStyle) {
			if(btnPointStyle.getSelectedValue() != null){
				applyVisualStyle(app.getSelectedGeos());				
			}
		}
		else if (source == btnBold || source == btnItalic) {
			applyVisualStyle(app.getSelectedGeos());			
		}
		else if (source == btnTextSize) {
			applyVisualStyle(app.getSelectedGeos());			
	}
		
		updateGUI();
	}

	
	
	
	//==============================================
	//           Apply Styles
	//==============================================
	
	private void applyLineStyle(ArrayList<GeoElement> geos) {
		int lineStyle = (Integer) btnLineStyle.getSelectedValue();
		int lineSize = btnLineStyle.getSliderValue();

		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);
			geo.setLineType(lineStyle);
			geo.setLineThickness(lineSize);
			geo.updateRepaint();			
		}

	}
	
	private void applyPointStyle(ArrayList<GeoElement> geos) {
		int pointStyle = (Integer) btnPointStyle.getSelectedValue();
		int pointSize = btnPointStyle.getSliderValue();

		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);

			if (geo instanceof PointProperties) {
				((PointProperties)geo).setPointSize(pointSize);
				((PointProperties)geo).setPointStyle(pointStyle);
				geo.updateRepaint();
				}
		}
	}


	private void applyColor(ArrayList<GeoElement> geos) {
		
		Color color = (Color) btnColor.getSelectedValue();
		float alpha = btnColor.getSliderValue() / 100.0f;
		
		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);
			if (!(geo instanceof GeoImage || geo instanceof GeoText)){
				geo.setObjColor(color);
				geo.setAlphaValue(alpha);
				geo.updateRepaint();
			}
		}
	}

	private void applyTextColor(ArrayList<GeoElement> geos) {

		Color color = (Color) btnTextColor.getSelectedValue();

		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);
			if(geo.isGeoText()){
				geo.setObjColor(color);
				geo.updateRepaint();
			}
		}
	}

	private void applyTextFormat(ArrayList<GeoElement> geos) {

		int fontStyle = 0;
		if (btnBold.isSelected()) fontStyle += 1;
		if (btnItalic.isSelected()) fontStyle += 2;
		
		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);
			if(geo.isGeoText()){
			((GeoText)geo).setFontStyle(fontStyle);
			geo.updateRepaint();
			}
		}
	}
	
	private void applyTextSize(ArrayList<GeoElement> geos) {

		int fontSize = btnTextSize.getSelectedIndex() * 2 - 4; // transform indices to the range -4, .. , 4
		
		for (int i = 0 ; i < geos.size() ; i++) {
			GeoElement geo = (GeoElement)geos.get(i);
			if(geo.isGeoText()){
			((GeoText)geo).setFontSize(fontSize); 
			geo.updateRepaint();
			}		
		}
	}
	
	
	public void applyVisualStyle(GeoElement geo) {
		ArrayList<GeoElement> geos = new ArrayList<GeoElement>();
		geos.add(geo);
		applyVisualStyle(geos);
	}
	public void applyVisualStyle(ArrayList<GeoElement> geos) {
		
		
		if(btnColor.isVisible()) applyColor(geos);
		if(btnLineStyle.isVisible()) applyLineStyle(geos);
		if(btnPointStyle.isVisible()) applyPointStyle(geos);
		if(btnBold.isVisible()) applyTextFormat(geos);
		if(btnItalic.isVisible()) applyTextFormat(geos);
		if(btnTextColor.isVisible()) applyTextColor(geos);
		if(btnTextSize.isVisible()) applyTextSize(geos);
		
		// TODO update prop panel
		// see code in PropertiesDialog.applyDefaults
		//propPanel.updateSelection(selectionList.toArray());
		
	}

	
	
	

	public Color[] getStyleBarColors(boolean doTextColor) {
		
		Color[] colors = new Color[24];
		
		Color[]	primaryColors = new Color[] {		
				new Color(255, 0, 0), // Red
				new Color(255, 153, 0), // Orange
				new Color(255, 255, 0), // Yellow
				new Color(0, 255, 0), // Green 
				new Color(0, 255, 255), // Cyan 
				new Color(0, 0, 255), // Blue
				new Color(153, 0, 255), // Purple
				new Color(255, 0, 255) // Magenta 
		};
		
		
		for(int i = 0; i< 8; i++){
			
			// first row: primary colors
			colors[i] = primaryColors[i];
			
			// second row: modified primary colors
			float[] hsb = Color.RGBtoHSB(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), null); 
			int rgb;
			if(doTextColor){
				colors[i+8] = colors[i].darker();
			}else{
				rgb = Color.HSBtoRGB((float) (.9*hsb[0]), (float) (.5*hsb[1]), (float) (1*hsb[2]));
				colors[i+8] = new Color(rgb);
			}
			// third row: gray scales (white ==> black)
			float p = 1.0f - i/7f;
			colors[i+16] = new Color(p,p,p);
		}

		// third row: gray scales (white ==> black)
		for(int i = 0; i< 6; i++){		
			float p = 1.0f - i/5f;
			colors[i+16] = new Color(p,p,p);
		}
		colors[22] = colors[23] = Color.white;
		return colors;
		
	}
	
	
	
	
	private class MyToggleButton extends JToggleButton{
		public MyToggleButton(ImageIcon imageIcon){
			super(imageIcon);
		}
		public void update(Object[] geos) {	 
		}
		public void setDefault() {	 
		}
	}


}
