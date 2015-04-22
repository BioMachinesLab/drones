package commoninterface.utils;

import commoninterface.mathutils.Vector2d;

public class Line {
	
	private Vector2d pointA;
	private Vector2d pointB;
	
	public Line(double x0, double y0, double x1, double y1) {
		pointA = new Vector2d(x0, y0);
		pointB = new Vector2d(x1, y1);
	}
	
	public Vector2d getPointA() {
		return pointA;
	}
	
	public Vector2d getPointB() {
		return pointB;
	}
	
	public Vector2d intersectsWithLineSegment(Vector2d p1, Vector2d p2) {
		Vector2d closestPoint      = null;
		Vector2d lineSegmentVector = new Vector2d(p2);
		lineSegmentVector.sub(p1);
		
		closestPoint = MathUtils.intersectLines(p1, p2, pointA, pointB);
		
		return closestPoint;
	}
	
//	public double getDistanceBetween(Vector2d fromPoint) {
//		
//		Vector2d light = new Vector2d(position);
//		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());
//
//		Vector2d intersection = intersectsWithLineSegment(lightDirection,fromPoint);
//		if(intersection != null) {
//			return intersection.length();
//		}
//		return fromPoint.distanceTo(position);
//	}
//	
//	public Vector2d getIntersection(Vector2d fromPoint) {
//		
//		Vector2d light = new Vector2d(position);
//		lightDirection.set(light.getX()-fromPoint.getX(),light.getY()-fromPoint.getY());
//
//		Vector2d intersection = intersectsWithLineSegment(lightDirection,fromPoint);
//		if(intersection != null) {
//			return intersection;
//		}
//		return null;
//	}
}