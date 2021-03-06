/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel.statistics;

import geogebra.kernel.AlgoElement;
import geogebra.kernel.Construction;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoList;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.arithmetic.NumberValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * Sort a list. Adapted from AlgoMax and AlgoIterationList
 * @author Michael Borcherds
 * @version 04-01-2008
 */

public class AlgoShuffle extends AlgoElement {

	private static final long serialVersionUID = 1L;
	private GeoList inputList; //input
    private GeoList outputList; //output	
    private int size;

    public AlgoShuffle(Construction cons, String label, GeoList inputList) {
        super(cons);
        this.inputList = inputList;
               
        outputList = new GeoList(cons);
        
        cons.addRandomGeo(outputList);

        setInputOutput();
        compute();
        outputList.setLabel(label);
    }

    public String getClassName() {
        return "AlgoShuffle";
    }

    protected void setInputOutput(){
        input = new GeoElement[1];
        input[0] = inputList;

        output = new GeoElement[1];
        output[0] = outputList;
        setDependencies(); // done by AlgoElement
    }

    public GeoList getResult() {
        return outputList;
    }

    protected final void compute() {
    	
    	size = inputList.size();
    	if (!inputList.isDefined() ||  size == 0) {
    		outputList.setUndefined();
    		return;
    	} 
    	
    	ArrayList<GeoElement> list = new ArrayList();

        // copy inputList into arraylist
        for (int i=0 ; i<size ; i++)
        {
        	list.add(inputList.get(i));
        }
        
        // copy the geos back into a GeoList in a random order
        outputList.setDefined(true);
        outputList.clear();
        for (int i=0 ; i<size ; i++)
        {
        	int pos = (int) Math.floor(Math.random() * (size - i));
        	outputList.add(list.get(pos));
        	list.remove(pos);
        }


    }
  
}
