package commoninterface.mathutils;

import java.io.Serializable;

public class GeometricCalculator implements Serializable {

	//to avoid constant allocation, used as temporary variable for calculations
	private Vector2d lightDirection = new Vector2d();

	public GeometricInfo getGeometricInfoBetweenPoints(Vector2d fromPoint, double orientation,
			Vector2d toPoint){
		Vector2d light = toPoint;
		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());
		double lightAngle=orientation-lightDirection.getAngle();

		if(lightAngle>Math.PI){
			lightAngle-=2*Math.PI;
		} else if(lightAngle<-Math.PI){ 
			lightAngle+=2*Math.PI;
		}
		return new GeometricInfo(lightAngle, lightDirection.length());
	}

}