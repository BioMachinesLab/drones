package commoninterface.utils;

import jama.Matrix;

import java.io.File;
import java.util.Scanner;

import jkalman.JKalman;
import commoninterface.RobotCI;
import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;

public class RobotKalman {

	private JKalman kalman;
	private Matrix s; // state [x, y]
	private Matrix c; // corrected state [x, y]
	private Matrix m; // measurement [x]
	boolean configured = false;

	private void configure(Vector2d coord1, Vector2d coord2, Vector2d coord3) {
		
		try {

			kalman = new JKalman(12, 6);

			s = new Matrix(12, 1);
			c = new Matrix(12, 1);

			m = new Matrix(6, 1);
			m.set(0, 0, coord1.getX());
			m.set(1, 0, coord1.getY());
			
			m.set(2, 0, coord2.getX());
			m.set(3, 0, coord2.getY());
			
			m.set(4, 0, coord3.getX());
			m.set(5, 0, coord3.getY());
			
			// transitions for x, y
            double[][] tr = { {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, //   p1x
                              {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, //p1y
                              {0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0}, //p2x
                              {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0}, //p2y
                              {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0}, //p3x
                              {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1}, //p3y
                              {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, //   v1x
                              {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, //v1y
                              {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                              {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                              {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                              {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},};
            
            kalman.setTransition_matrix(new Matrix(tr));
            
            kalman.setError_cov_post(kalman.getError_cov_post().identity());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		configured = true;
	}
	
	private Vector2d[] getEstimation(Vector2d coord1, Vector2d coord2, Vector2d coord3) {
		
		if(!configured)
			configure(coord1,coord2,coord3);
		
		s = kalman.Predict();
		
		m.set(0, 0, coord1.getX());
        m.set(1, 0, coord1.getY());
        m.set(2, 0, coord2.getX());
        m.set(3, 0, coord2.getY());
        m.set(4, 0, coord3.getX());
        m.set(5, 0, coord3.getY());
        
        c = kalman.Correct(m);
        
        Vector2d p1 = new Vector2d(c.get(0, 0),c.get(1, 0));
        Vector2d p2 = new Vector2d(c.get(2, 0),c.get(3, 0));
        Vector2d p3 = new Vector2d(c.get(4, 0),c.get(5, 0));
        
        return new Vector2d[]{p1,p2,p3};
	}
	
	public RobotLocation getEstimation(LatLon coordinates, double orientationInDegrees) {
		
		Vector2d cartesianCoord = CoordinateUtilities.GPSToCartesian(coordinates);
		
		double x = cartesianCoord.getX();
		double y = cartesianCoord.getY();
		double oRad = Math.toRadians(orientationInDegrees);
		
		Vector2d tail = new Vector2d(x-0.5*Math.cos(oRad),y-0.5*Math.sin(oRad));
		Vector2d center = new Vector2d(x,y);
		Vector2d front = new Vector2d(x+0.5*Math.cos(oRad),y+0.5*Math.sin(oRad));
		
		Vector2d[] estimation = getEstimation(tail, center, front);
		
		double orientation = calculateOrientation(estimation);
		
		return new RobotLocation("", CoordinateUtilities.cartesianToGPS(estimation[1]), orientation, null);
	}
	
	private double calculateOrientation(Vector2d[] estimation) {
		
		Vector2d tail = estimation[0];
		Vector2d front = estimation[2];
		
		double orientation = Math.atan2(front.getY()-tail.getY(),front.getX()-tail.getX());
		
		double degrees = Math.toDegrees(orientation);
		
		if(degrees < 0)
			degrees+=360;
		return  degrees;
	}
	
	public static void main(String[] args) throws Exception {
		
		File f = new File("data.txt");
		
		RobotKalman k = new RobotKalman();
		
		Scanner s = new Scanner(f);
		
		while(s.hasNext()) {
			double lat = s.nextDouble();
			double lon = s.nextDouble();
			double o = s.nextDouble();
			
			Vector2d v = CoordinateUtilities.GPSToCartesian(new LatLon(lat,lon));
//			System.out.println(v.getX()+" "+v.getY()+" "+o);
			
			double oRad = Math.toRadians(o);
			
			
			double x = v.x;
			double y = v.y;
			
			Vector2d tail = new Vector2d(x+(x-0.5)*Math.cos(oRad),y-(y-0.5)*Math.sin(oRad));
			Vector2d center = new Vector2d(x,y);
			Vector2d intersection = new Vector2d(x+(x+0.5)*Math.cos(oRad),y+(y-0.5)*Math.sin(oRad));
			
			Vector2d[] est = k.getEstimation(tail, center, intersection);
			
			System.out.println(est[1].getX()+" "+est[1].getY()+" "+o);
		}
		
		s.close();
		
	}
}