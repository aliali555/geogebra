/* 
GeoGebra - Dynamic Geometry and Algebra
Copyright Markus Hohenwarter, http://www.geogebra.at

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation; either version 2 of the License, or 
(at your option) any later version.
*/

package geogebra.algebra;

import geogebra.MyError;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoPolygon;
import geogebra.kernel.Macro;
import geogebra.kernel.arithmetic.Command;

/**
 * Tries to find a macro for the given command name. 
 * If a macro is found it will be used.
 * Syntax: Macro[ <list of input objects>, <list of output objects> ]  
 */
public class MacroProcessor extends CommandProcessor {
	
	private Macro macro;
	
	public MacroProcessor(AlgebraController algCtrl) {
		super(algCtrl);
	}
	
	void setMacro(Macro macro) {
		this.macro = macro;
	}
		
	public GeoElement[] process(Command c) throws MyError {        						 							
		// resolve command arguments
		GeoElement [] arg = resArgs(c);
				
		Class [] macroInputTypes = macro.getInputTypes();		
		
		// wrong number of arguments
		if (arg.length != macroInputTypes.length) {
			boolean lengthOk = false;
			
			// check if we have a polygon in the arguments
			// if yes, let's use its points
			if (arg[0].isGeoPolygon()) {
				arg = ((GeoPolygon) arg[0]).getPoints();
				lengthOk = true;
			}
			
			if (!lengthOk) {
				StringBuffer sb = new StringBuffer();
		        sb.append(app.getPlain("Macro") + " " + macro.getCommandName() + ":\n");
		        sb.append(app.getError("IllegalArgumentNumber") + ": " + arg.length);
		        sb.append("\n\nSyntax:\n" + macro.toString());
				throw new MyError(app, sb.toString());
			}
		}				
		
		// check whether the types of the arguments are ok for our macro
		for (int i=0; i < macroInputTypes.length; i++) {
			if (!macroInputTypes[i].isInstance(arg[i]))	{				
				StringBuffer sb = new StringBuffer();
		        sb.append(app.getPlain("Macro") + " " + macro.getCommandName() + ":\n");
		        sb.append(app.getError("IllegalArgument") + ": ");	            
	            sb.append(arg[i].getNameDescription());	            	            
		        sb.append("\n\nSyntax:\n" + macro.toString());
		        throw new MyError(app, sb.toString());
			}
		}
		
		// if we get here we have the right arguments for our macro
	    return kernel.useMacro(c.getLabels(), macro, arg);
    }    
}