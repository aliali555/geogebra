package geogebra3D.kernel3D;

import geogebra.Matrix.Coords;
import geogebra.kernel.Construction;
import geogebra.kernel.GeoElement;
import geogebra.kernel.Kernel;
import geogebra.kernel.arithmetic.NumberValue;
import geogebra.kernel.kernelND.GeoPointND;
import geogebra.main.Application;

/**
 * @author ggb3D
 *
 */
public class AlgoQuadricSide extends AlgoQuadric {
	
	
	
	
	
	public AlgoQuadricSide(Construction c, GeoPointND bottomPoint, GeoPointND topPoint, GeoQuadric3DLimited inputQuadric) {		
		super(c,inputQuadric,null,new AlgoQuadricComputerSide());
		
		setInputOutput(new GeoElement[] {inputQuadric, (GeoElement) bottomPoint,  (GeoElement) topPoint}, new GeoElement[] {(GeoElement) bottomPoint,  (GeoElement) topPoint}, new GeoElement[] {getQuadric()});
		
	}
	
	
	/**
	 * @param c construction
	 * @param inputQuadric 
	 */
	public AlgoQuadricSide(Construction c, GeoQuadric3DLimited inputQuadric) {		
		super(c,inputQuadric,null,new AlgoQuadricComputerSide());

		
		setInputOutput(new GeoElement[] {inputQuadric}, new GeoElement[] {getQuadric()});
		
		//compute();
	}

	public AlgoQuadricSide(Construction c, String label, GeoQuadric3DLimited inputQuadric) {		

		this(c,inputQuadric);
		getQuadric().setLabel(label);
	}
	
	
	private GeoQuadric3DLimited getInputQuadric(){
		return (GeoQuadric3DLimited) getSecondInput();
	}
	
	

	protected void compute() {
				
		//check origin
		if (!getInputQuadric().isDefined()){
			getQuadric().setUndefined();
			return;
		}
		
		//compute the quadric
		getQuadric().setDefined();
		getQuadric().setType(getInputQuadric().getType());
		getComputer().setQuadric(getQuadric(), getInputQuadric().getMidpoint3D(), getInputQuadric().getEigenvec3D(2), getInputQuadric().getHalfAxis(0));
		((GeoQuadric3DPart) getQuadric()).setLimits(getInputQuadric().getMin(), getInputQuadric().getMax());
	
	
	}
	



	protected Coords getDirection() {
		return null;
	}
	
	/*
    final public String toString() {
    	return app.getPlain("SideOfABetweenBC",((GeoElement) getInputQuadric()).getLabel(),point.getLabel(),pointThrough.getLabel());
    }
	 */
	
	

}
