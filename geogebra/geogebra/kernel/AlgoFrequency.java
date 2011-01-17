/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package geogebra.kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math.stat.Frequency;




public class AlgoFrequency extends AlgoElement {

	private static final long serialVersionUID = 1L;
	private GeoList dataList; //input
	private GeoList classList; //input
	private GeoBoolean showRelative; //input

	private GeoList frequency; //output	

	// for compute
	private GeoList value = new GeoList(cons);
	
	


	AlgoFrequency(Construction cons, String label, GeoList dataList, GeoList classList, GeoBoolean isRelative ) {
		this(cons, dataList, classList, isRelative);
		frequency.setLabel(label);
	}

	AlgoFrequency(Construction cons, GeoList dataList, GeoList classList, GeoBoolean showRelative ) {
		super(cons);
		this.classList = classList;
		this.dataList = dataList;
		this.showRelative = showRelative;

		frequency = new GeoList(cons);

		setInputOutput();
		compute();

	}

	public String getClassName() {
		return "AlgoFrequency";
	}

	protected void setInputOutput(){

		ArrayList<GeoElement> tempList = new ArrayList<GeoElement>();

		tempList.add(dataList);

		if(classList !=null)
			tempList.add(classList);

		if(showRelative !=null)
			tempList.add(showRelative);


		tempList.toString();
		input = new GeoElement[tempList.size()];
		input = tempList.toArray(input);

		output = new GeoElement[1];
		output[0] = frequency;
		setDependencies(); // done by AlgoElement
	}

	GeoList getResult() {
		return frequency;
	}



	protected final void compute() {

		
		if(frequency != null) frequency.clear();
		if(value != null) value.clear();
		
		int size = dataList.size();
		double numMax = 0, numMin = 0;

		
		//=======================================================
		// Validate input arguments
		//=======================================================
		
		if (!dataList.isDefined() || size == 0) {
			frequency.setUndefined();		
			return; 		
		}

		if( !( dataList.getElementType() != GeoElement.GEO_CLASS_TEXT 
				|| dataList.getElementType() == GeoElement.GEO_CLASS_NUMERIC )){
			frequency.setUndefined();		
			return;
		}

		if(classList != null){
			if(classList.getElementType() != GeoElement.GEO_CLASS_NUMERIC || classList.size() < 2){
				frequency.setUndefined();		
				return; 		
			}
		}


		
		//=======================================================
		// Load the data into f, an instance of Frequency class 
		//=======================================================
		
		Frequency f = new Frequency();
		for (int i=0 ; i < size ; i++){
			if(dataList.getElementType() == GeoElement.GEO_CLASS_TEXT)
				f.addValue(((GeoText)dataList.get(i)).toValueString());
			if(dataList.getElementType() == GeoElement.GEO_CLASS_NUMERIC)
				f.addValue(((GeoNumeric)dataList.get(i)).getDouble());
		}



		//=======================================================
		// Get the unique value list and compute frequencies for  
		// each value if classList does not exist   
		//=======================================================
		
		// handle string data
		if(dataList.getElementType() == GeoElement.GEO_CLASS_TEXT){

			Iterator itr = f.valuesIterator();
			String strMax = (String) itr.next();
			String strMin = strMax;
			itr = f.valuesIterator();

			while(itr.hasNext()) {		
				String s = (String) itr.next();
				if( s.compareTo(strMax) > 0) strMax = s;
				if( s.compareTo(strMin) < 0) strMin = s;
				GeoText text = new GeoText(cons);
				text.setTextString(s);
				value.add(text);
				if(classList == null)
					if( showRelative != null && showRelative.getBoolean())
						frequency.add(new GeoNumeric(cons,f.getPct((Comparable)s )));
					else
						frequency.add(new GeoNumeric(cons,f.getCount((Comparable)s )));
			}
		}

		// handle numeric data
		else
		{
			Iterator itr = f.valuesIterator();
			numMax = (Double) itr.next();
			numMin = numMax;
			itr = f.valuesIterator();

			while(itr.hasNext()) {		
				Double n = (Double) itr.next();
				if( n > numMax) numMax = n.doubleValue();
				if( n < numMin) numMin = n.doubleValue();
				value.add(new GeoNumeric(cons,n));

				if(classList == null)
					if( showRelative != null && showRelative.getBoolean())
						frequency.add(new GeoNumeric(cons,f.getPct((Comparable)n )));
					else
						frequency.add(new GeoNumeric(cons,f.getCount((Comparable)n )));
			}
		} 



		//=======================================================
		// Compute frequencies for the classes (if needed)
		//=======================================================
		
		if(classList != null){

			double lowerClassBound = 0 ;
			double upperClassBound = 0 ;
			double classFreq = 0;

			for(int i=1; i < classList.size()-1; i++){

				lowerClassBound = ((GeoNumeric)classList.get(i-1)).getDouble();
				upperClassBound = ((GeoNumeric)classList.get(i)).getDouble();
				classFreq = f.getCumFreq((Comparable)upperClassBound) 
				- f.getCumFreq((Comparable)lowerClassBound) 
				+ f.getCount((Comparable)lowerClassBound)
				- f.getCount((Comparable)upperClassBound);

				frequency.add(new GeoNumeric(cons, classFreq));

				/*
				System.out.println(" =================================");
				System.out.println("lower class bound: " + lowerClassBound);
				System.out.println("upper class bound: " + upperClassBound);
				System.out.println("lower class cumFreq: " + f.getCumFreq((Comparable)lowerClassBound));
				System.out.println("upper class cumFreq: " + f.getCumFreq((Comparable)upperClassBound));
				System.out.println("lower class count: " + f.getCount((Comparable)lowerClassBound));
				 */

			}

			// handle the last class specially
			// it contains all values equal to or larger than the next to last class bound  
			lowerClassBound = ((GeoNumeric)classList.get(classList.size()-2)).getDouble();
			classFreq = f.getSumFreq()
			- f.getCumFreq((Comparable)lowerClassBound) 
			+ f.getCount((Comparable)lowerClassBound);
			frequency.add(new GeoNumeric(cons, classFreq));

		}
	}

}
