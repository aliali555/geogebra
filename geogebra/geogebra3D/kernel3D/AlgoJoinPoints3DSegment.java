/* 
GeoGebra - Dynamic Mathematics for Schools
Copyright Markus Hohenwarter and GeoGebra Inc.,  http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoJoinPointsSegment
 *
 * Created on 21. August 2003
 */

package geogebra3D.kernel3D;

import geogebra.kernel.Construction;




/**
 *
 * @author  ggb3D
 * @version 
 */
public class AlgoJoinPoints3DSegment extends AlgoJoinPoints3DCoordSys1D {

	private static final long serialVersionUID = 1L;
    //private GeoSegment3D s; // output 


    /** Creates new AlgoJoinPoints */
    AlgoJoinPoints3DSegment(
        Construction cons,
        String label,
        GeoPoint3D P,
        GeoPoint3D Q) {
    	
        this(cons,P,Q);
        cs.setLabel(label);
        
    }

    AlgoJoinPoints3DSegment(
        Construction cons,        
        GeoPoint3D P,
        GeoPoint3D Q) {
    	
    	super(cons,P,Q);
          
        cs = new GeoSegment3D(cons, P, Q);    
        setInputOutput(); // for AlgoElement
               
        // compute line through P, Q
        compute();
    }   

    protected String getClassName() {
        return "AlgoJoinPoints3DSegment";
    }



    GeoSegment3D getSegment() {
        return (GeoSegment3D) cs;
    }
     

   

    
  
 
    final public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(app.getPlain("SegmentAB",P.getLabel(),Q.getLabel()));

        return sb.toString();
    }
}
