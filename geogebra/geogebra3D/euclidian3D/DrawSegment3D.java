package geogebra3D.euclidian3D;




import java.util.ArrayList;

import geogebra3D.Matrix.Ggb3DMatrix;
import geogebra3D.euclidian3D.opengl.EuclidianRenderer3D;
import geogebra3D.kernel3D.GeoLine3D;
import geogebra3D.kernel3D.GeoSegment3D;



public class DrawSegment3D extends DrawCoordSys1D {

	
	double dashLength;
	
	public DrawSegment3D(EuclidianView3D a_view3D, GeoSegment3D a_segment3D){
		
		super(a_view3D,a_segment3D);
	}


	/*
	public void drawGeometry(EuclidianRenderer3D renderer) {
		//renderer.setThickness(LINE3D_THICKNESS*getGeoElement().getLineThickness());
		super.drawGeometry(renderer);
		renderer.drawSegment(); 
	}
	
	public void drawGeometryPicked(EuclidianRenderer3D renderer){
		//renderer.setThickness(LINE3D_THICKNESS*PICKED_DILATATION*getGeoElement().getLineThickness());
		super.drawGeometryPicked(renderer);
		renderer.drawSegment(); 
	}
		
		*/

	
	////////////////////////////////
	// Previewable interface 
	
	
	public DrawSegment3D(EuclidianView3D a_view3D, ArrayList selectedPoints){
		
		super(a_view3D,selectedPoints, new GeoSegment3D(a_view3D.getKernel().getConstruction()));
		

		
	}	


}
