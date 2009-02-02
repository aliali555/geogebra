/* 
GeoGebra - Dynamic Mathematics for Schools
Copyright Markus Hohenwarter and GeoGebra Inc.,  http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License v2 as published by 
the Free Software Foundation.

*/

package geogebra3D.Matrix;

import geogebra.kernel.Construction;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoList;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.Kernel;
import geogebra.main.Application;





/**
 *
 * @author  ggb3D
 * @version 
 */
public class Ggb3DMatrix
	{
	
	protected double[] val;//values of the matrix
	/*
	 * the matrix represented by val = {1,2,3,4} is
	 *    | 1  3 |
	 *    | 2  4 |
	 */
	protected int rows, columns; // dimensions
	protected boolean transpose=false; //transposing the matrix is logical operation
	
	private boolean isSingular = false;
	
	//for rotations
	public static final int X_AXIS = 1;
	public static final int Y_AXIS = 2;
	public static final int Z_AXIS = 3;
	
	
	
	///////////////////////////////////////////////////:
	//Constructors 
	
	//public GgbMatrix(){}
	
	
	/** creates a GgbMatrix or a GgbVector if a_columns==1 */
	static final public Ggb3DMatrix GgbMatrixOrVector(int a_rows, int a_columns){
		if (a_columns==1)
			return new Ggb3DVector(a_rows);
		else
			return new Ggb3DMatrix(a_rows,a_columns);
	}
	
	
	
	
	/** see class description */
	public Ggb3DMatrix(int rows, int columns, double[] val){
		this.rows = rows;
		this.columns = columns;
		this.val = val;
	}
	
	
	/** creates an empty rows * columns matrix (all values set to 0)  */
	public Ggb3DMatrix(int rows, int columns){
		
		initialise(rows, columns);
		
	}
	
	private void initialise(int rows, int columns) {
		setIsSingular(false);
		
		this.rows=rows;
		this.columns=columns;
		transpose = false;
		
		val = new double[columns*rows];
		for(int i=0;i<columns*rows;i++){
			val[i]=0.0;
		}
		
	}
	
	public Ggb3DMatrix(GeoList inputList) {

    	int cols = inputList.size();
    	if (!inputList.isDefined() || cols == 0) {
			setIsSingular(true);
    		return;
    	} 
    	
    	GeoElement geo = inputList.get(0);
    	
    	if (!geo.isGeoList()) {
			setIsSingular(true);
    		return;   		
    	}
    	

   		int rows = ((GeoList)geo).size();
   		
   		if (rows == 0) {
			setIsSingular(true);
    		return;   		
    	}
   		
   		initialise(rows,cols);
		
		GeoList columnList;
   		
   		for (int r = 0 ; r < rows ; r++) {
   			geo = inputList.get(r);
   			if (!geo.isGeoList()) {
   				setIsSingular(true);
   				return;   		
   			}
   			columnList = (GeoList)geo;
   			if (columnList.size() != columns) {
   				setIsSingular(true);
   				return;   		
   			}
   	   		for (int c = 0 ; c < rows ; c++) {
   	   			geo = columnList.get(c);
   	   			if (!geo.isGeoNumeric()) {
   	   			setIsSingular(true);
   	   				return;   		
   	   			}
   	   			
   	   			set(r + 1, c + 1, ((GeoNumeric)geo).getValue());
   	   		}
   		}
   		

	}
	
	/** returns n*n identity matrix  */
	public static final Ggb3DMatrix Identity(int n){
		
		Ggb3DMatrix m = new Ggb3DMatrix(n,n);
		
		for (int i=1;i<=n;i++){
			m.set(i,i,1.0);
		}
		
		return m;
		
	}

	/** returns scale homogenic matrix, dim v.length+1  */
	public static final Ggb3DMatrix ScaleMatrix(double[] v){
		
		return ScaleMatrix(new Ggb3DVector(v));
		
	}
	
	/** returns scale homogenic matrix, dim v.length+1  */
	public static final Ggb3DMatrix ScaleMatrix(Ggb3DVector v){
		
		int n = v.getLength();
		Ggb3DMatrix m = new Ggb3DMatrix(n+1,n+1);
		
		for (int i=1;i<=n;i++){
			m.set(i,i,v.get(i));
		}
		m.set(n+1,n+1,1.0);
		
		return m;
		
	}

	
	/** returns translation homogenic matrix, dim v.length+1  */
	public static final Ggb3DMatrix TranslationMatrix(double[] v){
		
		return TranslationMatrix(new Ggb3DVector(v));
		
	}

	/** returns translation homogenic matrix, dim v.length+1  */
	public static final Ggb3DMatrix TranslationMatrix(Ggb3DVector v){
		
		int n = v.getLength();
		Ggb3DMatrix m = new Ggb3DMatrix(n+1,n+1);
		
		for (int i=1;i<=n;i++){
			m.set(i,i,1.0);
			m.set(i,n+1,v.get(i));
		}
		m.set(n+1,n+1,1.0);
		
		return m;
		
	}

	
	/** returns 3d rotation homogenic matrix, dim 4x4  */
	public static final Ggb3DMatrix Rotation3DMatrix(int axe, double angle){
		
		Ggb3DMatrix m = new Ggb3DMatrix(4,4);
		
		switch(axe){
		
		case Z_AXIS:
			m.set(1,1, Math.cos(angle)); m.set(1,2,-Math.sin(angle));
			m.set(2,1, Math.sin(angle)); m.set(2,2, Math.cos(angle));
			m.set(3,3,1.0);
			break;
		case X_AXIS:
			m.set(1,1,1.0);
			m.set(2,2, Math.cos(angle)); m.set(2,3,-Math.sin(angle));
			m.set(3,2, Math.sin(angle)); m.set(3,3, Math.cos(angle));
			break;
		case Y_AXIS:
			m.set(2,2,1.0);
			m.set(3,3, Math.cos(angle)); m.set(3,1,-Math.sin(angle));
			m.set(1,3, Math.sin(angle)); m.set(1,1, Math.cos(angle));
			break;
		default:
			break;
		}
		
		m.set(4,4,1.0);
		
		return m;
		
	}
	
	
	///////////////////////////////////////////////////:
	//setters and getters
	
	/** returns double[] describing the matrix for openGL */
	public double[] get(){
		
		return val;
	}
	
	/** returns m(i,j)  */
	public double get(int i, int j){
		if (transpose){
			return val[(i-1)*rows+(j-1)];
		}else{
			return val[(j-1)*rows+(i-1)];
		}
	}
	
	/** returns this minus the row i and the column j */
	public Ggb3DMatrix subMatrix(int i, int j){
		Ggb3DMatrix ret = new Ggb3DMatrix(getRows()-1, getColumns()-1);
		
		for (int i1=1; i1<i; i1++){
			for (int j1=1; j1<j; j1++){
				ret.set(i1,j1,get(i1,j1));
			}
			for (int j1=j+1; j1<=getColumns(); j1++){
				ret.set(i1,j1-1,get(i1,j1));
			}
		}
		
		for (int i1=i+1; i1<=getRows(); i1++){
			for (int j1=1; j1<j; j1++){
				ret.set(i1-1,j1,get(i1,j1));
			}
			for (int j1=j+1; j1<=getColumns(); j1++){
				ret.set(i1-1,j1-1,get(i1,j1));
			}
		}
		
		return ret;
	}
	
	/** returns the column number j */
	public Ggb3DVector getColumn(int j){
		
		Ggb3DVector ret = new Ggb3DVector(getRows());
		for (int i=1;i<=getRows();i++){
			ret.set(i,get(i,j));
		}
		
		return ret;
		
	}
	
	
	
	/**
	 * returns GgbMatrix as a GeoList eg { {1,2}, {3,4} }
	 */
	public GeoList getGeoList(GeoList outputList, Construction cons) {
		
		if (isSingular) {
	        outputList.setDefined(false);
	        return outputList;
		}
		
		outputList.clear();
        outputList.setDefined(true);
		
	   		for (int r = 0 ; r < rows ; r++) {  	   			
   			GeoList columnList = new GeoList(cons);
   	   		for (int c = 0 ; c < columns ; c++) {
   	   			columnList.add(new GeoNumeric(cons, get(r + 1, c + 1)));  	   			
   	   		}
   	   		outputList.add(columnList);
   		}
   		
   		return outputList;

	}
	
	/** sets V to column j of m, rows=V.getLength() */
	public void set(Ggb3DVector V, int j){
		int i;
		for (i=1;i<=V.getLength();i++){
			set(i,j,V.get(i));
		}
	}
	
	/** sets m(V[]), all V[j].getLength equal rows and V.Length=columns */
	public void set(Ggb3DVector[] V){
		int j;
		for (j=0;j<V.length;j++){
			set(V[j],j+1);
		}
	}
	
	/** sets m(i,j) to val0 */
	public void set(int i, int j, double val0){
		if (transpose){
			val[(i-1)*rows+(j-1)]=val0;
		}else{
			val[(j-1)*rows+(i-1)]=val0;
		}
	}

	
	/** sets all values to val0 */
	public void set(double val0){

		for(int i=0;i<columns*rows;i++){
			val[i]=val0;
		}
		
	}

	/** copies all values of m */
	public void set(Ggb3DMatrix m){

		for(int i=1;i<=m.getRows();i++){
			for(int j=1;j<=m.getColumns();j++){
				this.set(i,j,m.get(i, j));
			}
		}
		
	}

	
	/** returns number of rows */
	public int getRows(){
		
		if (!transpose)
			return rows;
		else
			return columns;
	}
	
	/** returns number of columns */
	public int getColumns(){
		
		if (!transpose)
			return columns;
		else
			return rows;
	}
	
	/** transpose the copy (logically) */
	public boolean transpose(){
		
		transpose=!transpose;
		return transpose;
	}
	
	/** returns a copy of the matrix */
	public Ggb3DMatrix copy(){
		
		Ggb3DMatrix result = new Ggb3DMatrix(getRows(),getColumns()); 

		for(int i=1;i<=result.getRows();i++){
			for(int j=1;j<=result.getColumns();j++){
				result.set(i,j,get(i,j));
			}
		}
		
		return result;
				
	}
	
	/** returns a transposed copy of the matrix */
	public Ggb3DMatrix transposeCopy(){
		
		this.transpose();		
		Ggb3DMatrix result = this.copy(); 
		this.transpose();

		return result;
				
	}
	
	
	
	/** prints the matrix to the screen */
	public void SystemPrint(){
		
		String s="";
		
		for(int i=1;i<=getRows();i++){
			
			for(int j=1;j<=getColumns();j++){
				s+="  "+get(i,j);
			}
			s+="\n";
		}
		
		Application.debug(s);
	}
	
	
	
	
	
	
	
	
	
	/** returns if one value equals NaN */
	public boolean isDefined(){
		
		boolean result = true;
		
		for(int i=0;(i<columns*rows)&&(result);i++){
			result = result && (!Double.isNaN(val[i]));
		}
		
		return result;
	}

	
	///////////////////////////////////////////////////:
	//basic operations 
	
	//multiplication by a real
	/** returns this * val0 */
	public Ggb3DMatrix mul(double val0){
		
		Ggb3DMatrix result = GgbMatrixOrVector(getRows(),getColumns()); 

		for(int i=1;i<=result.getRows();i++){
			for(int j=1;j<=result.getColumns();j++){
				result.set(i,j,val0*get(i,j));
			}
		}
		
		return result;
		
	}
	
	
	//matrix addition
	/** returns this + m */
	public Ggb3DMatrix add(Ggb3DMatrix m){
		
		Ggb3DMatrix result = GgbMatrixOrVector(getRows(),getColumns());
		//resulting matrix has the same dimension than this
		//and is a GgbVector if this has 1 column
		
		for(int i=1;i<=result.getRows();i++){
			for(int j=1;j<=result.getColumns();j++){
				result.set(i,j,get(i,j)+m.get(i,j));
			}
		}
		
		return result;
		
	}
	
	
	//vector multiplication
	/** returns this * v */
	public Ggb3DVector mul(Ggb3DVector v){

		Ggb3DVector result = new Ggb3DVector(getRows());
		
		for(int i=1;i<=result.getRows();i++){
			
				double r = 0;
				for (int n=1; n<=getColumns(); n++)
					r+=get(i,n)*v.get(n);
				
				result.set(i,r);
		}
		
		return result;
		
	}
	
	//matrix multiplication
	/** returns this * m */
	public Ggb3DMatrix mul(Ggb3DMatrix m){
		
		Ggb3DMatrix result = new Ggb3DMatrix(getRows(),m.getColumns()); //resulting matrix has the maximal dimension
		
		for(int i=1;i<=result.getRows();i++){
			for(int j=1;j<=result.getColumns();j++){
				
				double r = 0;
				for (int n=1; n<=getColumns(); n++)
					r+=get(i,n)*m.get(n,j);
				
				result.set(i,j,r);
			}
		}
		
		
		return result;
		
	}
	
	/** returns determinant */
	public double det(){
		
		double ret = 0.0;
		
		if(getRows()==1){
			ret = get(1,1);
		}else{
			double signe = 1.0;
			for (int j=1;j<=getColumns();j++){
				ret += get(1,j) * signe * (subMatrix(1,j).det());
				signe = -signe;
			}
		}
		
		return ret;
	}
	
	public boolean isSquare() {
		if (isSingular()) return false;
		return getRows() == getColumns();
	}
	
	/** returns inverse matrix (2x2 or larger)
	 * you must check with isSquare() before calling this
	 * 
	 * */
	public Ggb3DMatrix inverse(){
		
		Ggb3DMatrix ret = new Ggb3DMatrix(getRows(),getColumns());

		double d = this.det();
		
		if (Kernel.isEqual(d, 0.0, Kernel.STANDARD_PRECISION)){			
			ret.setIsSingular(true);
			return ret;
		}
		
		double signe_i = 1.0;
		for(int i=1; i<=getRows(); i++){
			double signe = signe_i;
			for(int j=1; j<=getColumns(); j++){
				ret.set(i,j,(subMatrix(j,i).det())*signe/d);
				signe = -signe;
			}
			signe_i = -signe_i;
		}
		
		return ret;
		
	}
	
	
	///////////////////////////////////////////////////:
	//more linear operations 
	/** returns ret that makes this * ret = v */
	public Ggb3DVector solve(Ggb3DVector v){
		//GgbVector ret;
		Ggb3DMatrix mInv = this.inverse(); //TODO: use gauss pivot to optimize
		if (mInv==null)
			return null;
		return mInv.mul(v);		
		
	}
	
	/*
	 * returns whether the matrix is singular, eg after an inverse
	 */
	public boolean isSingular() {
		return isSingular;
	}
	
	public void setIsSingular(boolean isSingular) {
		this.isSingular = isSingular;
	}
	
	
	
	///////////////////////////////////////////////////:
	//testing the package
	public static synchronized void main(String[] args) {		
		
		Ggb3DMatrix m1 = Ggb3DMatrix.Identity(3);
		m1.set(1, 2, 5.0);
		m1.set(3, 1, 4.0);
		m1.set(3, 2, 3.0);
		m1.transpose();
		Application.debug("m1");
		m1.SystemPrint();
		
		Ggb3DMatrix m2 = new Ggb3DMatrix(3,4);
		m2.set(1, 1, 1.0);
		m2.set(2, 2, 2.0);
		m2.set(3, 3, 3.0);
		m2.set(1, 4, 4.0);
		m2.set(2, 4, 3.0);
		m2.set(3, 4, 1.0);
		m2.set(3, 2, -1.0);
		Application.debug("m2");		
		m2.SystemPrint();
		

		Ggb3DMatrix m4 = m1.add(m2);
		Application.debug("m4");
		m4.SystemPrint();

		Ggb3DMatrix m5 = m1.mul(m2);
		Application.debug("m5");
		m5.SystemPrint();
		
		Application.debug("subMatrix");
		m5.subMatrix(2,3).SystemPrint();
		
		m1.set(1, 2, -2.0);m1.set(3, 1, -9.0);m1.set(3, 2, -8.0);
		Application.debug("m1");
		m1.SystemPrint();
		Application.debug("det m1 = "+m1.det());
		
		
		Application.debug("inverse");
		Ggb3DMatrix m4inv = m4.inverse();
		m4inv.SystemPrint();
		m4.mul(m4inv).SystemPrint();
		m4inv.mul(m4).SystemPrint();
	}
	
	
	

}