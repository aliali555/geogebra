/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel;

import geogebra.Matrix.Coords;
import geogebra.euclidian.EuclidianView;
import geogebra.kernel.arithmetic.ExpressionNode;
import geogebra.kernel.arithmetic.ExpressionValue;
import geogebra.kernel.arithmetic.FunctionNVar;
import geogebra.kernel.arithmetic.FunctionalNVar;
import geogebra.kernel.arithmetic.Inequality;
import geogebra.kernel.arithmetic.MyDouble;
import geogebra.kernel.arithmetic.NumberValue;
import geogebra.kernel.kernelND.GeoPointND;
import geogebra.kernel.kernelND.SurfaceEvaluable;
import geogebra.util.Util;


/**
 * Explicit function in multiple variables, e.g. f(a, b, c) := a^2 + b - 3c. 
 * This is actually a wrapper class for FunctionNVar
 * in geogebra.kernel.arithmetic. In arithmetic trees (ExpressionNode) it evaluates
 * to a FunctionNVar.
 * 
 * @author Markus Hohenwarter
 */
public class GeoFunctionNVar extends GeoElement
implements FunctionalNVar, CasEvaluableFunction, Region, Transformable, Translateable, MatrixTransformable,
 Dilateable, Rotateable, PointRotateable, Mirrorable, SurfaceEvaluable{

	private static final double STRICT_INEQ_OFFSET = 4*Kernel.MIN_PRECISION;
	private static final int SEEK_DENSITY = 30;
	private FunctionNVar fun;
	//private List<Inequality> ineqs;	
	private boolean isInequality;
	private boolean isDefined = true;
	
	/** intervals for plotting, may be null (then interval is R) */
	private double[] from, to;

	/**
	 * Creates new GeoFunction
	 * @param c
	 */
	public GeoFunctionNVar(Construction c) {
		super(c);
	}
	
	/**
	 * Creates new GeoFunction from Function
	 * @param c
	 * @param f function to be wrapped
	 */
	public GeoFunctionNVar(Construction c, FunctionNVar f) {
		this(c);
		fun = f;	
	}

	/**
	 * Creates labeled GeoFunction from Function
	 * @param c
	 * @param label
	 * @param f function to be wrapped
	 */
	public GeoFunctionNVar(Construction c, String label, FunctionNVar f) {
		this(c,f);	
		setLabel(label);		
	}
	
	public String getClassName() {
		return "GeoFunctionNVar";
	}
	
	protected String getTypeString() {
		return isInequality ? "Inequality":"FunctionNVar";
	}
	
    public int getGeoClassType() {
    	return GEO_CLASS_FUNCTION_NVAR;
    }

	/** copy constructor 
	 * @param f source function */
	public GeoFunctionNVar(GeoFunctionNVar f) {
		super(f.cons);
		set(f);
	}

	public GeoElement copy() {
		return new GeoFunctionNVar(this);
	}
	
	public void set(GeoElement geo) {
		GeoFunctionNVar geoFun = (GeoFunctionNVar) geo;				
						
		if (geo == null || geoFun.fun == null) {
			fun = null;
			isDefined = false;
			return;
		} else {
			isDefined = geoFun.isDefined;
			fun = new FunctionNVar(geoFun.fun, kernel);
		}			
	
		// macro OUTPUT
		if (geo.cons != cons && isAlgoMacroOutput()) {								
			// this object is an output object of AlgoMacro
			// we need to check the references to all geos in its function's expression
			if (!geoFun.isIndependent()) {
				AlgoMacro algoMacro = (AlgoMacro) getParentAlgorithm();
				algoMacro.initFunction(this.fun);	
			}			
		}
		isInequality = fun.initIneqs(this.getFunctionExpression(),this);		
	}
	

	/**
	 * @param f
	 */
	public void setFunction(FunctionNVar f) {
		fun = f;
	}
			
	final public FunctionNVar getFunction() {
		return fun;
	}
	
	/**
	 * @return expression of the wrapped function
	 */
	final public ExpressionNode getFunctionExpression() {
		if (fun == null)
			return null;
		else 
			return fun.getExpression();
	}	
	
	 /**
     * Replaces geo and all its dependent geos in this function's
     * expression by copies of their values.
	 * @param geo 
     */
    public void replaceChildrenByValues(GeoElement geo) {     	
    	if (fun != null) {
    		fun.replaceChildrenByValues(geo);
    	}
    }
    
    /**
     * Returns this function's value at position.    
     * @param vals
     * @return f(vals)
     */
	public double evaluate(double[] vals) {
		//Application.printStacktrace("");
		if (fun == null)
			return Double.NaN;
		else 
			return fun.evaluate(vals);
	}
	
	public Coords evaluatePoint(double[] vals) {
		//Application.printStacktrace("");
		if (fun == null)
			return null;
		else 
			return new Coords(vals[0],vals[1],fun.evaluate(vals));
	}
	
	public double evaluate(double x, double y, double z) {
		//Application.printStacktrace("");
		if (fun == null)
			return Double.NaN;
		else 
			return fun.evaluate(new double[]{x,y,z});
	}	
	
	/**
	 * Sets this function by applying a GeoGebraCAS command to a function.
	 * 
	 * @param ggbCasCmd the GeoGebraCAS command needs to include % in all places
	 * where the function f should be substituted, e.g. "Derivative(%,x)"
	 * @param f the function that the CAS command is applied to
	 */
	public void setUsingCasCommand(String ggbCasCmd, CasEvaluableFunction f, boolean symbolic){
		GeoFunctionNVar ff = (GeoFunctionNVar) f;
		
		if (ff.isDefined()) {
			fun = ff.fun.evalCasCommand(ggbCasCmd, symbolic);
			isDefined = fun != null;
		} else {
			isDefined = false;
		}		
	}
	

	
	public ExpressionValue evaluate() {
		return this;
	}
	
	public boolean isDefined() {
		return isDefined && fun != null;
	}

	/**
	 * @param defined
	 */
	public void setDefined(boolean defined) {
		isDefined = defined;
	}

	public void setUndefined() {
		isDefined = false;
	}

	public boolean showInAlgebraView() {
		return true;
	}

	protected boolean showInEuclidianView() {
		return isDefined() && (!isBooleanFunction() || isInequality);
	}
	
	
	/**
	 * @return function description as f(x)=...
	 */
	private String toXMLString(){
		sbToString.setLength(0);
		sbToString.append(label);
		sbToString.append("(");
		sbToString.append(getVarString());
		sbToString.append(") = ");
		sbToString.append(toValueString());
		return sbToString.toString();
	}
	
	/**
	 * @return function description as f(x,y)=... for real and e.g. f:x>4*y for bool
	 */
	public String toString() {	
		if (isLabelSet() && !isBooleanFunction())
			return toXMLString();
		sbToString.setLength(0);
		if(isLabelSet()) {
			sbToString.append(label);
			sbToString.append(": ");
		}
		sbToString.append(toValueString());
		return sbToString.toString();
	}
	
	private StringBuilder sbToString = new StringBuilder(80);
	public String toValueString() {			
		if (isDefined())
			return fun.toValueString();
		else
			return app.getPlain("undefined");
	}	
	
	public String toSymbolicString() {	
		if (isDefined())
			return fun.toString();
		else
			return app.getPlain("undefined");
	}
	
	public String toLaTeXString(boolean symbolic) {
		if (isDefined())
			return fun.toLaTeXString(symbolic);
		else
			return app.getPlain("undefined");
	}
	
	@Override
	protected char getLabelDelimiter(){
		return isBooleanFunction()?':':'=';
	}
		
	/**
	   * save object in xml format
	   */ 
	  public final void getXML(StringBuilder sb) {
		 
		 // an indpendent function needs to add
		 // its expression itself
		 // e.g. f(a,b) = a^2 - 3*b
		 if (isIndependent()) {
			sb.append("<expression");
				sb.append(" label =\"");
				sb.append(label);
				sb.append("\" exp=\"");
				sb.append(Util.encodeXML(toXMLString()));
				// expression   
			sb.append("\"/>\n");
		 }
	  		  
		  sb.append("<element"); 
			  sb.append(" type=\"functionNVar\"");
			  sb.append(" label=\"");
			  sb.append(label);
		  sb.append("\">\n");
		  getXMLtags(sb);
		  sb.append(getCaptionXML());
		  sb.append("</element>\n");

	  }

	final public boolean isCasEvaluableObject() {
		return true;
	}

	public boolean isNumberValue() {
		return false;		
	}

	public boolean isVectorValue() {
		return false;
	}

	public boolean isPolynomialInstance() {
		return false;
	}   

	public boolean isTextValue() {
		return false;
	}
	
	public boolean isBooleanFunction() {
		if (fun != null)
			return fun.isBooleanFunction();
		else
			return false;
	}


//	public boolean isGeoDeriveable() {
//		return true;
//	}
	/** 
	 * Returns name of i-th variable
	 * @param i index of variable
	 * @return name of i-th variable
	 */
	public String getVarString(int i) {	
		return fun == null ? "" : fun.getVarString(i);
	}

	public String getVarString() {	
		return fun == null ? "" : fun.getVarString();
	}
	

	
    // Michael Borcherds 2009-02-15
	public boolean isEqual(GeoElement geo) {
		if (!(geo instanceof GeoFunctionNVar))
			return false;
		
		String f = getFormulaString(ExpressionNode.STRING_TYPE_MATH_PIPER, true);
		String g = geo.getFormulaString(ExpressionNode.STRING_TYPE_MATH_PIPER, true);
		String diff = ""; 
		try {
			diff = kernel.evaluateMathPiper("TrigSimpCombine(ExpandBrackets(" + f + "-(" + g + ")))");
		}
		catch (Exception e) { return false; }
		
		if ("0".equals(diff)) 
			return true; 
		else 
			return false;
	}
	
	public boolean isVector3DValue() {
		return false;
	}
	
    /**
	 * Returns a representation of geo in currently used CAS syntax.
	 * For example, "a*x^2 + b*y"
	 */
	public String getCASString(boolean symbolic) {
		return fun.getExpression().getCASstring(symbolic);
	}
    
	 public String getLabelForAssignment() {
		StringBuilder sb = new StringBuilder();
		sb.append(getLabel());
		sb.append("(" );
		sb.append(fun.getVarString());
		sb.append(")");
		return sb.toString();
	 }

	 
	 

	 
	 
	 
	 

	 
		/////////////////////////////////////////
		// INTERVALS
		/////////////////////////////////////////

	 /**
	  * return Double.NaN if none has been set
	  * @param index of parameter
	  * @return min parameter
	  */
	 public double getMinParameter(int index) {

		 if (from==null) 
			 return Double.NaN;

		 return from[index];

	 }


	 /**
	  * return Double.NaN if none has been set
	  * @param index of parameter
	  * @return max parameter
	  */
	 public double getMaxParameter(int index) {

		 if (to==null)
			 return Double.NaN;

		 return to[index];
	 }

		

		/** 
		 * Sets the start and end parameters values of this function.
		 * @param from
		 * @param to
		 */
		public void setInterval(double[] from, double[] to) {
			
			this.from = from;
			this.to =to;
			
			
			
		}
	 
		/////////////////////////////////////////
		// For 3D
		/////////////////////////////////////////
		
	 /** used if 2-var function, for plotting 
	 * @param u 
	 * @param v 
	 * @return coords of the point (u,v,f(u,v)) */
		 public Coords evaluatePoint(double u, double v){

			 Coords p = new Coords(3);
			 double val = fun.evaluate(new double[] {u,v});
			 p.set(1, u);
			 p.set(2, v);
			 p.set(3, val);
//			 p.set(3, Double.isNaN(val)?0:val);

			 return p;

		 }
	 

		 /**
		  * 
		  * @return number of vars
		  */
		 public int getVarNumber(){
			 return fun.getVarNumber();
		 }

		 //will be drawn as a surface if can be interpreted as (x,y)->z function
		 //or implicit f(x,y,z)=0 function
		 public boolean hasDrawable3D() {  		
			 return getVarNumber()==2 || getVarNumber()==3;
		 }

	  	
		public Coords getLabelPosition(){
			return new Coords(0, 0, 0, 1); //TODO
		}

	    
		/** to be able to fill it with an alpha value */
		public boolean isFillable() {
			if(fun==null)return true;
			return hasDrawable3D();
		}
		
		
		/**
		 * @return the ineqs
		 */
		public FunctionNVar.IneqTree getIneqs(){
			if(fun.getIneqs() == null){
				isInequality = fun.initIneqs(fun.getExpression(),this);				
			}
			return fun.getIneqs();
		}
				
		public void update(){			
			isInequality = fun.updateIneqs();
			super.update();
		}
		public boolean isRegion() {
			return isBooleanFunction();
		}
		public boolean isInRegion(GeoPointND P) {
			P.updateCoords2D();
			return isInRegion(P.getX2D(),P.getY2D());
		}

		public boolean isInRegion(double x0, double y0) {
			return fun.evaluateBoolean(new double[] {x0,y0});
		}

		public void pointChangedForRegion(GeoPointND P) {
			if(!((GeoPoint)P).isDefined())
				return;
			RegionParameters rp = P.getRegionParameters();
			if(!isInRegion(P) && ((GeoPoint)P).isDefined()){
				double bestX = rp.getT1(), bestY = rp.getT2(), 
				myX = P.getX2D(), myY = P.getY2D();
				double bestDist = (bestY-myY)*(bestY-myY)+(bestX-myX)*(bestX-myX);
				
				FunctionNVar.IneqTree ineqs = getIneqs();
				int size = ineqs.getSize();
				for(int i = 0; i<size; i++){
					Inequality in = ineqs.get(i);
					double px=0,py=0;
					if(in.getType()==Inequality.INEQUALITY_PARAMETRIC_Y){
						px = P.getX2D();
						py = in.getFunBorder().evaluate(px);
						py += in.isAboveBorder()? STRICT_INEQ_OFFSET : -STRICT_INEQ_OFFSET;
					}
					else if(in.getType()==Inequality.INEQUALITY_PARAMETRIC_X){
						py = P.getY2D();
						px = in.getFunBorder().evaluate(py);
						px += in.isAboveBorder()? STRICT_INEQ_OFFSET : -STRICT_INEQ_OFFSET;
					}
					double myDist = (py-myY)*(py-myY)+(px-myX)*(px-myX);
					if((myDist < bestDist) && isInRegion(px,py)){
						bestDist = myDist;
						bestX = px;
						bestY = py;
					}
				}
				if(isInRegion(bestX,bestY)){
					rp.setT1(bestX);
					rp.setT2(bestY);
					((GeoPoint)P).setCoords(bestX, bestY, 1);
				}
				else tryLocateInEV(P); 
					
			}else{
				rp.setT1(P.getX2D());
				rp.setT2(P.getY2D());
			}
			
		}

	/**
	 * We seek for a point in region by desperately testing grid points
	 * in euclidian view. This should be called only when every algorithm fails.
	 * @param P
	 */
	private void tryLocateInEV(GeoPointND P) {
		EuclidianView ev = kernel.getApplication().getEuclidianView();
		boolean found = false;
		for (int i = 0; !found && i < ev.getWidth() / SEEK_DENSITY; i++)
			for (int j = 0; !found && j < ev.getHeight() / SEEK_DENSITY; j++) {
				double rx = ev.toRealWorldCoordX(SEEK_DENSITY * i);
				double ry = ev.toRealWorldCoordX(SEEK_DENSITY * i);
				if (isInRegion(rx, ry)) {
					((GeoPoint) P).setCoords(rx, ry, 1);
					//Application.debug("Desperately found"+rx+","+ry);
					found = true;
				}
			}	
		if(!found)
			((GeoPoint)P).setUndefined();
			
	}

		public void regionChanged(GeoPointND P) {
			pointChangedForRegion(P);
			
		}

		public boolean isInequality() {
			return isInequality;
		}

		
	 
/*
		public GgbVector evaluateNormal(double u, double v){
			if (funD1 == null) {
				funD1 = new FunctionNVar[2];
				for (int i=0;i<2;i++){
					funD1[i] = fun.derivative(i, 1);
				}
			}

			
			GgbVector vec = new GgbVector(
					-funD1[0].evaluate(new double[] {u,v}),
					-funD1[1].evaluate(new double[] {u,v}),
					1,
					0).normalized();
		
			//Application.debug("vec=\n"+vec.toString());
		
			return vec;
			
			//return new GgbVector(0,0,1,0);
		}

*/
		public void translate(Coords v){
			fun.translate(v.getX(),v.getY());
		}
		/**
		 * Return the geo
		 * @return geo element
		 */
		public GeoElement toGeoElement(){
			return this;
		}
		/**
		 * Returns true if the element is translateable
		 * @return true
		 */
		public boolean isTranslateable(){
			return true;
		
		}

		public void matrixTransform(double a00, double a01, double a10,
				double a11) {
			double d=a00*a11-a01*a10;
			if(d==0)
				setUndefined();
			else
				fun.matrixTransform(a11/d,-a01/d,-a10/d,a00/d);				
		}

		public void dilate(NumberValue r, GeoPoint S) {
			fun.translate(-S.getX(),-S.getY());
			fun.matrixTransform(1/r.getDouble(),0,0,1/r.getDouble());
			fun.translate(S.getX(),S.getY());
			
		}
		
		public void dilate(NumberValue r){
			matrixTransform(r.getDouble(),0,0,r.getDouble());
		}

		public void rotate(NumberValue phi) {
			double cosPhi = Math.cos(phi.getDouble());
			double sinPhi = Math.sin(phi.getDouble());
			matrixTransform(cosPhi,-sinPhi,sinPhi,cosPhi);			
		}

		public void rotate(NumberValue phi, GeoPoint P) {
			fun.translate(-P.getX(),-P.getY());
			rotate(phi);
			fun.translate(P.getX(),P.getY());
			
		}

		public void mirror(GeoPoint Q) {
			dilate(new MyDouble(kernel,-1.0),Q);
			
		}

		public void mirror(GeoLine g) {
			double qx, qy; 
	        if (Math.abs(g.x) > Math.abs(g.y)) {
	            qx = g.z / g.x;
	            qy = 0.0d;
	        } else {
	            qx = 0.0d;
	            qy = g.z / g.y;
	        }
	        
	        // translate -Q
	        fun.translate(qx, qy);     
	        
	        // S(phi)        
	        mirror(new MyDouble(kernel,2.0 * Math.atan2(-g.x, g.y)));
	        
	        // translate back +Q
	        fun.translate(-qx, -qy);
			
		}
		private void mirror(NumberValue phi){				
			double cosPhi = Math.cos(phi.getDouble());
			double sinPhi = Math.sin(phi.getDouble());
			matrixTransform(cosPhi,sinPhi,sinPhi,-cosPhi);				
		}
		
		public void matrixTransform(double a00, double a01, double a02,
				double a10, double a11, double a12, double a20, double a21,
				double a22) {
			fun.matrixTransform(a00, a01, a02, a10, a11, a12, a20, a21, a22);
			
		}
}
