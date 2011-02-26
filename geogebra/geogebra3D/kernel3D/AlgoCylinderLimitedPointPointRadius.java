package geogebra3D.kernel3D;

import geogebra.Matrix.CoordSys;
import geogebra.Matrix.Coords;
import geogebra.kernel.Construction;
import geogebra.kernel.GeoElement;
import geogebra.kernel.arithmetic.NumberValue;
import geogebra.kernel.kernelND.GeoPointND;


/**
 * Algo for cylinder between two end points and given radius.
 * @author mathieu
 *
 */
public class AlgoCylinderLimitedPointPointRadius extends AlgoElement3D {

	//input
	private GeoPointND origin, secondPoint;
	private NumberValue radius;
	
	//output
	private GeoQuadric3DPart side;
	private GeoConic3D bottom, top;
	private GeoQuadric3DLimited quadric;
	
	//coordsys
	private CoordSys bottomCoordsys, topCoordsys; 

	/**
	 * 
	 * @param c
	 * @param label
	 * @param origin
	 * @param secondPoint
	 * @param r
	 */
	public AlgoCylinderLimitedPointPointRadius(Construction c, String[] labels, GeoPointND origin, GeoPointND secondPoint, NumberValue r) {
		super(c);
		
		this.origin=origin;
		this.secondPoint=secondPoint;
		this.radius=r;
		
		side=new GeoQuadric3DPart(c);
		
		bottomCoordsys = new CoordSys(2);
		bottom=new GeoConic3D(c,bottomCoordsys);

		topCoordsys = new CoordSys(2);
		top=new GeoConic3D(c,topCoordsys);
		
		quadric=new GeoQuadric3DLimited(c);
		quadric.setParts(bottom, top, side);
		
		setInputOutput(new GeoElement[] {(GeoElement) origin,(GeoElement) secondPoint,(GeoElement) r}, new GeoElement[] {quadric,bottom,top,side});
		//compute();
		
		quadric.initLabels(labels);
		quadric.updatePartsVisualStyle();
		


		
	}
	
	
	protected void compute() {
		
		Coords o = origin.getInhomCoordsInD(3);
		Coords o2 = secondPoint.getInhomCoordsInD(3);
		Coords d = o2.sub(o);
		double r = radius.getDouble();
		
		d.calcNorm();
		double altitude = d.getNorm();
		
		side.setLimits(0, altitude);
		side.setCylinder(o,d.mul(1/altitude),r);


		
		//vectors ortho to direction
		Coords[] v = d.completeOrthonormal();
		
		//bottom
		bottomCoordsys.resetCoordSys();
		bottomCoordsys.addPoint(o);
		bottomCoordsys.addVector(v[1]);
		bottomCoordsys.addVector(v[0]);
		bottomCoordsys.makeOrthoMatrix(false,false);
		bottom.setSphereND(new Coords(0,0), radius.getDouble());
		
		//top
		topCoordsys.resetCoordSys();
		topCoordsys.addPoint(o2);
		topCoordsys.addVector(v[1]);
		topCoordsys.addVector(v[0]);
		topCoordsys.makeOrthoMatrix(false,false);
		top.setSphereND(new Coords(0,0), radius.getDouble());
		
		
	}


	public String getClassName() {
		return "AlgoCylinder";
	}
	
    final public String toString() {
    	return app.getPlain("CylinderBetweenABRadiusC",origin.getLabel(),secondPoint.getLabel(),((GeoElement) radius).getLabel());

    }
    
    /**
     * @return the quadric part
     */
    public GeoQuadric3DPart getQuadric(){
    	return side;
    }

}
