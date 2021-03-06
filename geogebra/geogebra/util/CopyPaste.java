/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.util;

import geogebra.euclidian.EuclidianView;
import geogebra.main.Application;
import geogebra.kernel.AlgoElement;
import geogebra.kernel.AlgoPolygon;
import geogebra.kernel.AlgoPolygonRegular;
import geogebra.kernel.AlgoPolyLine;
import geogebra.kernel.GeoPolyLine;
import geogebra.kernel.GeoAxis;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.Construction;
import geogebra.kernel.ConstructionElement;
import geogebra.kernel.GeoElement;
import geogebra.kernel.Kernel;
import geogebra.kernel.arithmetic.ExpressionNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class saves the given geos
 * (which are usually the selected ones) into an XML string, and
 * makes it possible to insert a copy of them into the construction
 * As a nature of the clipboard,
 * this class contains only static data and methods
 * 
 * @author Arpad Fekete
 */
public class CopyPaste {

	// labelPrefix has to contain something else than big letters,
	// otherwise the parsed label could be regarded as a spreadsheet label
	// see GeoElement.isSpreadsheetLabel
	public static final String labelPrefix = "CLIPBOARDmagicSTRING";

	protected static StringBuilder copiedXML;
	protected static ArrayList<String> copiedXMLlabels;

	public static boolean isEmpty() {
		if (copiedXML == null)
			return true;
		
		return (copiedXML.length() == 0);
	}

	/**
	 * copyToXML - Step 1
	 * Remove fixed sliders
	 * @param geos input and output
	 */
	public static void removeFixedSliders(ArrayList<ConstructionElement> geos) {
		GeoElement geo;
		for (int i = geos.size() - 1; i >= 0; i--)
		{
			geo = (GeoElement) geos.get(i);
			if (geo.isGeoNumeric())
				if (((GeoNumeric)geo).isSliderFixed()) {
					geos.remove(geo);
				}
		}
	}
	
	/**
	 * copyToXML - Step 1.5 (temporary)
	 * currently we remove all geos which depend on the axes
	 * TODO: make geos dependent on GeoAxis objects copiable again
	 * (this is not easy as there is a bug when copy & paste something
	 * which depends on xAxis or yAxis, then copy & paste something
	 * else, points may be repositioned)
	 * 
	 */
	public static void removeDependentFromAxes(ArrayList<ConstructionElement> geos, Application app) {

		ConstructionElement geo;
		for (int i = geos.size() - 1; i >= 0; i--)
		{
			geo = (ConstructionElement) geos.get(i);
			if (geo.getAllIndependentPredecessors().contains(app.getKernel().getXAxis())) {
				geos.remove(i);
			} else if (geo.getAllIndependentPredecessors().contains(app.getKernel().getYAxis())) {
				geos.remove(i);
			}
		}
	}

	/**
	 * copyToXML - Step 2
	 * Add subgeos of geos like points of a segment or line or polygon
	 * These are copied anyway but this way they won't be hidden 
	 *  
	 * @param geos input and output 
	 */
	public static void addSubGeos(ArrayList<ConstructionElement> geos) {
		GeoElement geo;
		for (int i = geos.size() - 1; i >= 0; i--)
		{
			geo = (GeoElement) geos.get(i);
			if ((geo.isGeoLine() && geo.getParentAlgorithm().getClassName().equals("AlgoJoinPoints")) ||
				(geo.isGeoSegment() && geo.getParentAlgorithm().getClassName().equals("AlgoJoinPointsSegment")) ||
				(geo.isGeoRay() && geo.getParentAlgorithm().getClassName().equals("AlgoJoinPointsRay")) ||
				(geo.isGeoVector() && geo.getParentAlgorithm().getClassName().equals("AlgoVector"))) {

				if (!geos.contains(geo.getParentAlgorithm().getInput()[0])) {
					geos.add(geo.getParentAlgorithm().getInput()[0]);
				}
				if (!geos.contains(geo.getParentAlgorithm().getInput()[1])) {
					geos.add(geo.getParentAlgorithm().getInput()[1]);
				}
			} else if (geo.isGeoPolygon()) {
				if (geo.getParentAlgorithm().getClassName().equals("AlgoPolygon")) {
					GeoElement [] points = ((AlgoPolygon)(geo.getParentAlgorithm())).getPoints();
					for (int j = 0; j < points.length; j++) {
						if (!geos.contains(points[j])) {
							geos.add(points[j]);
						}
					}
					GeoElement [] ogeos = ((AlgoPolygon)(geo.getParentAlgorithm())).getOutput();
					for (int j = 0; j < ogeos.length; j++) {
						if (!geos.contains(ogeos[j]) && ogeos[j].isGeoSegment()) {
							geos.add(ogeos[j]);
						}
					}
				} else if (geo.getParentAlgorithm().getClassName().equals("AlgoPolygonRegular")) {
					GeoElement [] pgeos = ((AlgoPolygonRegular)(geo.getParentAlgorithm())).getInput();
					for (int j = 0; j < pgeos.length; j++) {
						if (!geos.contains(pgeos[j]) && pgeos[j].isGeoPoint()) {
							geos.add(pgeos[j]);
						}
					}
					GeoElement [] ogeos = ((AlgoPolygonRegular)(geo.getParentAlgorithm())).getOutput();
					for (int j = 0; j < ogeos.length; j++) {
						if (!geos.contains(ogeos[j]) && (ogeos[j].isGeoSegment() || ogeos[j].isGeoPoint())) {
							geos.add(ogeos[j]);
						}
					}
				}
			} else if (geo instanceof GeoPolyLine) {
				if (geo.getParentAlgorithm().getClassName().equals("AlgoPolyLine")) {
					GeoElement [] pgeos = ((AlgoPolyLine)(geo.getParentAlgorithm())).getPoints();
					for (int j = 0; j < pgeos.length; j++) {
						if (!geos.contains(pgeos[j])) {
							geos.add(pgeos[j]);
						}
					}
				}
			} else if (geo.isGeoConic()) {
				if (geo.getParentAlgorithm().getClassName().equals("AlgoCircleTwoPoints")) {
					GeoElement [] pgeos = geo.getParentAlgorithm().getInput();
					if (!geos.contains(pgeos[0]))
						geos.add(pgeos[0]);
					if (!geos.contains(pgeos[1]))
						geos.add(pgeos[1]);
				} else if (geo.getParentAlgorithm().getClassName().equals("AlgoCircleThreePoints") ||
							geo.getParentAlgorithm().getClassName().equals("AlgoEllipseFociPoint") ||
							geo.getParentAlgorithm().getClassName().equals("AlgoHyperbolaFociPoint")) {
					GeoElement [] pgeos = geo.getParentAlgorithm().getInput();
					if (!geos.contains(pgeos[0]))
						geos.add(pgeos[0]);
					if (!geos.contains(pgeos[1]))
						geos.add(pgeos[1]);
					if (!geos.contains(pgeos[2]))
						geos.add(pgeos[2]);
				} else if (geo.getParentAlgorithm().getClassName().equals("AlgoConicFivePoints")) {
					GeoElement [] pgeos = geo.getParentAlgorithm().getInput();
					for (int j = 0; j < pgeos.length; j++) {
						if (!geos.contains(pgeos[j]))
							geos.add(pgeos[j]);
					}
				} else if (geo.getParentAlgorithm().getClassName().equals("AlgoCirclePointRadius")) {
					GeoElement [] pgeos = geo.getParentAlgorithm().getInput();
					if (!geos.contains(pgeos[0]))
						geos.add(pgeos[0]);
				}
			} else if (geo.isGeoList()) {
				if (geo.getParentAlgorithm().getClassName().equals("AlgoSequence")) {
					GeoElement [] pgeos = geo.getParentAlgorithm().getInput();
					if (pgeos.length > 1) {
						if (!geos.contains(pgeos[0]))
							geos.add(pgeos[0]);
					}
				}
			}
		}
	}

	/**
	 * copyToXML - Step 2.5
	 * Drop the GeoElements in ArrayList which are depending from outside
	 * Deprecated
	 *  
	 * @param geos input and output
	 */
	/*public static void dropGeosDependentFromOutside(ArrayList<ConstructionElement> geos) {
		
		ConstructionElement geo;
		GeoElement geo2;
		for (int i = geos.size() - 1; i >= 0; i--)
		{
			geo = (ConstructionElement) geos.get(i);
			if (geo.isIndependent()) {
				continue;
			} else {
				TreeSet ts = geo.getAllIndependentPredecessors();

				// exclude from ts the numeric input of AlgoPolygonRegular or AlgoCirclePointRadius
		    	Iterator it = ts.iterator();
		    	while (it.hasNext()) {
		    		geo2 = (GeoElement)it.next();
		    		if (geo2.isGeoNumeric()) {
		    			// check the case of input of AlgoPolygonRegular
		    			ArrayList<ConstructionElement> geoal = geo2.getAlgorithmList();
		    			if (geoal.size() == 0) {
		    				// nothing special, GeoNumeric remains
		    			} else if ((geoal.size() == 1) && ((AlgoElement)geoal.get(0)).getClassName().equals("AlgoPolygonRegular")) {
		    				it.remove();
		    			// or AlgoCirclePointRadius
		    			} else if ((geoal.size() == 1) && ((AlgoElement)geoal.get(0)).getClassName().equals("AlgoCirclePointRadius")) {
		    				it.remove();
		    			// or single intersection point
		    			} else if ((geoal.size() == 1) && ((AlgoElement)geoal.get(0)).getClassName().equals("AlgoIntersectSingle")) {
		    				if (((AlgoElement)geoal.get(0)).getInput()[2].isGeoNumeric()) {
		    					it.remove();
		    				}
		    			} else if (((GeoElement)geo).isGeoList()) {
		    				// GeoNumerics: from, to, step
		    				if ((geoal.size() == 1) && ((AlgoElement)geoal.get(0)).getClassName().equals("AlgoSequence")) {
		    					it.remove();
		    				} else if (geoal.size() == 2) { // var
		    					if (((AlgoElement)geoal.get(0)).getClassName().equals("AlgoSequence")) {
		    						if (((AlgoElement)geoal.get(0)).getInput().length > 1) {
			    						if (((AlgoElement)geoal.get(0)).getInput()[1] == geo2) {
			    							it.remove();
			    						}
		    						}
		    					} else if (((AlgoElement)geoal.get(1)).getClassName().equals("AlgoSequence")) {
		    						if (((AlgoElement)geoal.get(1)).getInput().length > 1) {
			    						if (((AlgoElement)geoal.get(1)).getInput()[1] == geo2) {
			    							it.remove();
			    						}
		    						}
		    					}
		    				}
		    			} else {
		    				// angle of rotation and degree of enlarging may be used more times,
		    				// but it should be removed for all times

		    				boolean algorotatefound = true;
		    				boolean algodilatefound = true;
		    				for (int k = 0; k < geoal.size(); k++) {
		    					if (!((AlgoElement)geoal.get(k)).getClassName().equals("AlgoRotate") &&
	    							!((AlgoElement)geoal.get(k)).getClassName().equals("AlgoRotatePoint")) {
		    						algorotatefound = false;
		    					} else if (((AlgoElement)geoal.get(k)).getInput()[1] != geo2) {
		    						algorotatefound = false;
		    					}
		    					if (!((AlgoElement)geoal.get(k)).getClassName().equals("AlgoDilate")) {
		    						algodilatefound = false;
		    					} else if (((AlgoElement)geoal.get(k)).getInput()[1] != geo2) {
		    						algodilatefound = false;
		    					}
		    				}

		    				if (algorotatefound || algodilatefound) {
		    					it.remove();
		    				}
		    			} 
		    		}
		    	}

				if (geos.containsAll(ts)) {
					continue;
				} else {
					geos.remove(i);
				}
			}
		}
	}*/

	/**
	 * copyToXML - Step 3
	 * Add geos which might be intermediates between our existent geos
	 * And also add all predecessors of our geos except GeoAxis objects
	 * (GeoAxis objects should be dealt with later - we suppose they are always on)
	 *  
	 * @param geos input and output
	 * @return just the predecessor and intermediate geos for future handling
	 */
	public static ArrayList<ConstructionElement> addPredecessorGeos(ArrayList<ConstructionElement> geos) {

		ArrayList<ConstructionElement> ret = new ArrayList<ConstructionElement>();

		GeoElement geo, geo2;
		TreeSet<GeoElement> ts;
		Iterator<GeoElement> it;
		for (int i = 0; i < geos.size(); i++)
		{
			geo = (GeoElement) geos.get(i);
			ts = geo.getAllPredecessors();
	    	it = ts.iterator();
	    	while (it.hasNext()) {
	    		geo2 = it.next();
	    		if (!ret.contains(geo2) && !geos.contains(geo2) && !(geo2 instanceof GeoAxis)) {
	    			ret.add(geo2);
	    		}
	    	}  
		}
		geos.addAll(ret);
		return ret;
	}

	/**
	 * copyToXML - Step 4
	 * Add the algos which belong to our selected geos
	 * Also add the geos which might be side-effects of these algos
	 *  
	 * @param conels input and output
	 * @return the possible side-effect geos
	 */
	public static ArrayList<ConstructionElement> addAlgosDependentFromInside(ArrayList<ConstructionElement> conels) {

		ArrayList<ConstructionElement> ret = new ArrayList<ConstructionElement>();
		
		GeoElement geo;
		ArrayList<ConstructionElement> geoal;
		AlgoElement ale;
		ArrayList<ConstructionElement> ac;
		GeoElement [] geos;
		for (int i = conels.size() - 1; i >= 0; i--)
		{
			geo = (GeoElement) conels.get(i);
			geoal = geo.getAlgorithmList();

			for (int j = 0; j < geoal.size(); j++) {
				ale = (AlgoElement) geoal.get(j);
				ac = new ArrayList<ConstructionElement>();
				ac.addAll(ale.getAllIndependentPredecessors());
				if (conels.containsAll(ac) && !conels.contains((ConstructionElement) ale)) {
					conels.add((ConstructionElement) ale);
					geos = ale.getOutput();
					for (int k = 0; k < geos.length; k++) {
						if (!ret.contains(geos[k]) && !conels.contains(geos[k])) {
					    	ret.add(geos[k]);
					    }
					}
				}
			}
		}
		conels.addAll(ret);
		return ret;
	}

	/**
	 * copyToXML - Step 4
	 * Before saving the conels to xml, we have to rename its labels
	 * with labelPrefix and memorize those renamed labels
	 * and also hide the GeoElements in geostohide, and keep in
	 * geostohide only those which were actually hidden...
	 * 
	 * @param conels
	 * @param geostohide
	 */
	public static void beforeSavingToXML(ArrayList<ConstructionElement> conels, ArrayList<ConstructionElement> geostohide) {

		copiedXMLlabels = new ArrayList<String>();

		ConstructionElement geo;
		String label;
		String reallabel;
		for (int i = 0; i < conels.size(); i++)
		{
			geo = (ConstructionElement) conels.get(i);
			if (geo.isGeoElement())
			{
				label = ((GeoElement)geo).getLabelSimple();
				if (label != null) {
					((GeoElement)geo).setLabel(labelPrefix + label);
					copiedXMLlabels.add(((GeoElement)geo).getLabelSimple());

					// TODO: check possible realLabel issues
					//reallabel = ((GeoElement)geo).getRealLabel();
					//if (!reallabel.equals( ((GeoElement)geo).getLabelSimple() )) {
					//	((GeoElement)geo).setRealLabel(labelPrefix + reallabel);
					//}
				}
			}
		}

		for (int j = geostohide.size() - 1; j >= 0; j--)
		{
			geo = geostohide.get(j);
			if (geo.isGeoElement() && ((GeoElement)geo).isEuclidianVisible()) {
				((GeoElement)geo).setEuclidianVisible(false);
			} else {
				geostohide.remove(geo);
			}
		}
	}

	/* 
	 * copyToXML - Step 6
	 * After saving the conels to xml, we have to rename its labels
	 * and also show the GeoElements in geostoshow
	 * 
	 * @param conels
	 * @param geostoshow
	 */
	public static void afterSavingToXML(ArrayList<ConstructionElement> conels, ArrayList<ConstructionElement> geostoshow) {

		ConstructionElement geo;
		String label;
		for (int i = 0; i < conels.size(); i++)
		{
			geo = (ConstructionElement) conels.get(i);
			if (geo.isGeoElement())
			{
				label = ((GeoElement)geo).getLabelSimple();
				if (label != null && label.length() >= labelPrefix.length()) {
					try {
						((GeoElement)geo).setLabel(label.substring(labelPrefix.length()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (int j = geostoshow.size() - 1; j >= 0; j--)
		{
			geo = geostoshow.get(j);
			if (geo.isGeoElement()) {
				((GeoElement)geo).setEuclidianVisible(true);
			}
		}
	}

	/**
	 * This method saves geos and all predecessors of them in XML 
	 */
	public static void copyToXML(Application app, ArrayList<GeoElement> geos) {

		copiedXML = new StringBuilder();
		copiedXMLlabels = new ArrayList<String>();

		if (geos.isEmpty())
			return;
		
		// create geoslocal and geostohide
		ArrayList<ConstructionElement> geoslocal = new ArrayList<ConstructionElement>();
		geoslocal.addAll(geos);
		removeFixedSliders(geoslocal);
		
		if (geoslocal.isEmpty())
			return;
		
		removeDependentFromAxes(geoslocal, app);
		
		if (geoslocal.isEmpty())
			return;
		
		addSubGeos(geoslocal);
		//dropGeosDependentFromOutside(geoslocal);
		
		if (geoslocal.isEmpty())
			return;

		ArrayList<ConstructionElement> geostohide = addPredecessorGeos(geoslocal);
		geostohide.addAll(addAlgosDependentFromInside(geoslocal));

		// store undo info
		Kernel kernel = app.getKernel();
		//kernel.setUndoActive(true);
        //kernel.getConstruction().storeUndoInfo();

		// change kernel settings temporarily
		int oldCoordStlye = kernel.getCoordStyle();
		int oldPrintForm = kernel.getCASPrintForm();
        boolean oldValue = kernel.isTranslateCommandName();
		kernel.setCoordStyle(Kernel.COORD_STYLE_DEFAULT);
		kernel.setCASPrintForm(ExpressionNode.STRING_TYPE_GEOGEBRA_XML);
        kernel.setTranslateCommandName(false);

		beforeSavingToXML(geoslocal, geostohide);
		try {
			// step 5
			copiedXML = new StringBuilder();
			ConstructionElement ce;

			// loop through Construction to keep the good order of ConstructionElements
			Construction cons = app.getKernel().getConstruction();
			for (int i = 0; i < cons.steps(); ++i) {
				ce = cons.getConstructionElement(i);
				if (geoslocal.contains(ce))
					ce.getXML(copiedXML);
			}
		} catch (Exception e) {
			e.printStackTrace();
			copiedXML = new StringBuilder();
		}
		//kernel.restoreCurrentUndoInfo();
		afterSavingToXML(geoslocal, geostohide);

		// restore kernel settings
		kernel.setCoordStyle(oldCoordStlye);
		kernel.setCASPrintForm(oldPrintForm);
		kernel.setTranslateCommandName(oldValue);
		app.setMode(EuclidianView.MODE_MOVE);
		app.getActiveEuclidianView().setSelectionRectangle(null);
		
		// make sure the coord style is updated the right way
		ConstructionElement ce;
		Construction cons = app.getKernel().getConstruction();
		for (int i = 0; i < cons.steps(); ++i) {
			ce = cons.getConstructionElement(i);
			if (geoslocal.contains(ce) && ce.isGeoElement())
				((GeoElement)ce).updateRepaint();
		}
		kernel.notifyRepaint();
	}

	/**
	 * In some situations, we may need to clear the clipboard
	 */
	public static void clearClipboard() {
		copiedXML = null;
		copiedXMLlabels = new ArrayList<String>();
	}

	/**
	 * This method pastes the content of the clipboard from XML
	 * into the construction
	 * 
	 * @param app
	 * @param cons
	 */
	public static void pasteFromXML(Application app) {

		if (copiedXML == null)
			return;

		if (copiedXML.length() == 0)
			return;

		if (!app.getActiveEuclidianView().getEuclidianController().mayPaste())
			return;

		app.getActiveEuclidianView().getEuclidianController().clearSelections();
		app.getActiveEuclidianView().getEuclidianController().setPastePreviewSelected();
		app.getGgbApi().evalXML(copiedXML.toString());

		Kernel kernel = app.getKernel();

		GeoElement geo;
		for (int i = 0; i < copiedXMLlabels.size(); i++) {
			String ll = copiedXMLlabels.get(i);
			geo = kernel.lookupLabel(ll);
			if (geo != null) {
				if (app.getActiveEuclidianView() == app.getEuclidianView()) {
					app.addToEuclidianView(geo);
					if (app.getGuiManager().hasEuclidianView2()) {
						geo.removeView(app.getEuclidianView2());
						app.getEuclidianView2().remove(geo);
					}
				} else {
					app.removeFromEuclidianView(geo);
					geo.addView(app.getEuclidianView2());
					app.getEuclidianView2().add(geo);
				}

				geo.setLabel(geo.getDefaultLabel(false));
				app.addSelectedGeo(geo);
			}
		}
		app.getActiveEuclidianView().getEuclidianController().setPastePreviewSelected();
		app.setMode(EuclidianView.MODE_MOVE);
	}
}
