package geogebra3D.kernel3D;

import geogebra.Matrix.GgbMatrix;
import geogebra.Matrix.GgbMatrix4x4;
import geogebra.Matrix.GgbVector;
import geogebra.kernel.Construction;
import geogebra.kernel.Kernel;

/**
 * Class describing 1D, 2D and 3D coordinate systems.
 * 
 * @author ggb3D
 *
 */
public abstract class GeoCoordSysAbstract extends GeoElement3D
implements GeoCoordSys{

	//matrix for the coord sys
	private GgbMatrix m_matrix;
	private int m_dimension;
	private int m_madeCoordSys;
	
	//matrix completed to 4x4 for drawing, etc.
	private GgbMatrix4x4 m_matrix4x4 = new GgbMatrix4x4();

	
	private GeoCoordSysAbstract(Construction c) {
		super(c);
		
	}
	
	/** create a coord sys with a_dimension dimensions, creating m_matrix for this */
	public GeoCoordSysAbstract(Construction c, int a_dimension) {
		this(c);
		m_matrix=new GgbMatrix(4,a_dimension+1);
		m_dimension = a_dimension;
		resetCoordSys();
		
	}	
	
	

	
	public GgbMatrix getMatrix(){
		return m_matrix;
	}
	
	/*
	public GeoCoordSysAbstract getCoordSys(){
		return this;
	}
	*/
	
	public int getDimension(){
		return m_dimension;
	}
	
	////////////////////////////
	// setters
	
	public void setOrigin(GgbVector a_O){
		m_matrix.set(a_O,m_dimension+1);
	}
	
	public void setVx(GgbVector a_V){
		setV(a_V,1);
	}
	
	public void setVy(GgbVector a_V){
		setV(a_V,2);
	}
	
	public void setVz(GgbVector a_V){
		setV(a_V,3);
	}
	
	public void setV(GgbVector a_V, int i){
		m_matrix.set(a_V,i);
	}
	
	public GgbVector getV(int i){
		return m_matrix.getColumn(i);
	}
	
	public GgbVector getOrigin(){
		return getV(m_dimension+1);
	}
	
	public GgbVector getVx(){
		return getV(1);
	}
	
	public GgbVector getVy(){
		return getV(2);
	}
	
	public GgbVector getVz(){
		return getV(3);
	}
	
	
	
	
	
	///////////////////////////////////////
	// creating a coord sys
	
	
	/**
	 * set how much the coord sys is made
	 * @param i value of made coord sys
	 */
	public void setMadeCoordSys(int i){
		m_madeCoordSys = i;
	}
	
	/**
	 * set the coord sys is finish
	 */
	public void setMadeCoordSys(){
		setMadeCoordSys(m_dimension);
	}
	
	/**
	 * reset the coord sys
	 */
	public void resetCoordSys(){
		setMadeCoordSys(-1);
	}
	
	/** return how much the coord sys is made
	 * @return how much the coord sys is made
	 */
	public int getMadeCoordSys(){
		return m_madeCoordSys;
	}
	
	/** return if the coord sys is made
	 * @return if the coord sys is made
	 */
	public boolean isMadeCoordSys(){
		return (getMadeCoordSys()==m_dimension);
	}
	
	
	/**
	 * Try to add the point described by v to complete the coord sys.
	 * @param v a point (x,y,z,1)
	 * @param orthonormal say if the coord sys has to be orthonormal
	 * 
	 */
	public void addPointToCoordSys(GgbVector v, boolean orthonormal){
		
		addPointToCoordSys(v, orthonormal, false);
	
	}
	
	
	
	/**
	 * Try to add the point described by v to complete the coord sys.
	 * @param v a point (x,y,z,1)
	 * @param orthonormal say if the coord sys has to be orthonormal
	 * @param standardCS says if the coord sys has to be "standard": 
	 * (0,0,0) projected for origin,
	 * and vector Vx parallel to xOy plane
	 * 
	 */
	public void addPointToCoordSys(GgbVector v, boolean orthonormal, boolean standardCS){
		
		if (isMadeCoordSys())
			return;
		
		GgbVector v1;
		
		
		switch(getMadeCoordSys()){
		case -1: //add the origin
			setOrigin(v);
			setMadeCoordSys(0);
			break;
		case 0: //add first vector
			v1 = v.sub(getOrigin());
			//check if v==0
			if (!Kernel.isEqual(v1.norm(), 0, Kernel.STANDARD_PRECISION)){		
				if(orthonormal)
					v1.normalize();
				setVx(v1);
				setMadeCoordSys(1);
			}
			break;
		case 1: //add second vector
			v1 = v.sub(getOrigin());
			//calculate normal vector
			GgbVector vn = getVx().crossProduct(v1);
			//check if vn==0
			if (!Kernel.isEqual(vn.norm(), 0, Kernel.STANDARD_PRECISION)){	
				if(orthonormal){
					v1=vn.crossProduct(getVx());
					v1.normalize();
				}
				setVy(v1);
				setMadeCoordSys(2);
			}
			break;						
		}

		//if the coord sys is made, the drawing matrix is updated
		if (isMadeCoordSys()){
			if (standardCS && m_dimension==2){
				
				updateDrawingMatrix();
				
				// (0,0,0) projected for origin
				GgbVector o = new GgbVector(new double[] {0,0,0,1});
				setOrigin(o.projectPlane(getMatrix4x4())[0]);
				
				// vector Vx parallel to xOy plane
				GgbVector vn = getVx().crossProduct(getVy());
				GgbVector vz = new GgbVector(new double[] {0,0,1,0});
				GgbVector vx = vn.crossProduct(vz);
				if (!Kernel.isEqual(vx.norm(), 0, Kernel.STANDARD_PRECISION)){
					vx.normalize();
					setVx(vx);
					GgbVector vy = vn.crossProduct(vx);
					vy.normalize();
					setVy(vy);
				} else {
					setVx(new GgbVector(new double[] {1,0,0,0}));
					setVy(new GgbVector(new double[] {0,1,0,0}));
				}
				

			}
			
			updateDrawingMatrix();
		}
		
	}
	
	
	
	
	public boolean isDefined() {
		return isMadeCoordSys();
	}
	
	public void setUndefined() {
		resetCoordSys();
		
	}
	
	
	
	////////////////////////////////////////
	// drawing matrix
	
	public void updateDrawingMatrix(){
		m_matrix4x4 = new GgbMatrix4x4(m_matrix);
		setDrawingMatrix(m_matrix4x4);
		setLabelMatrix(m_matrix4x4);
	}
	
	
	/** returns completed matrix for drawing : (V1 V2 V3 O)  */
	public GgbMatrix4x4 getMatrix4x4(){
		return m_matrix4x4;
	}
	

	

	
	
}
