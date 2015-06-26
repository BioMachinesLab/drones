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

	private void configure(Vector2d coord1, Vector2d coord2) {
		
		try {

			kalman = new JKalman(8, 4);

			s = new Matrix(8, 1);
			c = new Matrix(8, 1);

			m = new Matrix(4, 1);
			m.set(0, 0, coord1.getX());
			m.set(1, 0, coord1.getY());
			
			m.set(2, 0, coord2.getX());
			m.set(3, 0, coord2.getY());
			
			// transitions for x1,y1,x2,y2,v1,v2,v1,v2,v1,v2
            double[][] tr = { {1, 0, 0, 0, 1, 0, 0, 0}, //p1x
                              {0, 1, 0, 0, 0, 1, 0, 0}, //p1y
                              {0, 0, 1, 0, 0, 0, 1, 0}, //p2x
                              {0, 0, 0, 1, 0, 0, 0, 1}, //p2y
                              {0, 0, 0, 0, 1, 0, 0, 0}, //v1x
                              {0, 0, 0, 0, 0, 1, 0, 0}, //v1y
                              {0, 0, 0, 0, 0, 0, 1, 0}, //v2x
                              {0, 0, 0, 0, 0, 0, 0, 1}, //v2y
                              						  };
            
            kalman.setTransition_matrix(new Matrix(tr));
            
            kalman.setError_cov_post(kalman.getError_cov_post().identity());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		configured = true;
	}
	
	private Vector2d[] getEstimation(Vector2d coord1, Vector2d coord2) {
		
		if(!configured)
			configure(coord1,coord2);
		
		s = kalman.Predict();
		
		m.set(0, 0, coord1.getX());
        m.set(1, 0, coord1.getY());
        m.set(2, 0, coord2.getX());
        m.set(3, 0, coord2.getY());
        
        c = kalman.Correct(m);
        
        Vector2d p1 = new Vector2d(c.get(0, 0),c.get(1, 0));
        Vector2d p2 = new Vector2d(c.get(2, 0),c.get(3, 0));
        
        return new Vector2d[]{p1,p2};
	}
	
	public RobotLocation getEstimation(LatLon coordinates, double orientationInDegrees) {
		
		Vector2d cartesianCoord = CoordinateUtilities.GPSToCartesian(coordinates);
		
		double x = cartesianCoord.getX();
		double y = cartesianCoord.getY();
		double oRad = Math.toRadians(orientationInDegrees);
		
		Vector2d center = new Vector2d(x,y);
		Vector2d front = new Vector2d(x+0.5*Math.cos(oRad),y+0.5*Math.sin(oRad));
		
		Vector2d[] estimation = getEstimation(center,front);
		
		double orientation = calculateOrientation(estimation);
		
		return new RobotLocation("", CoordinateUtilities.cartesianToGPS(estimation[0]), orientation, null);
	}
	
	private double calculateOrientation(Vector2d[] estimation) {
		
		Vector2d center = estimation[0];
		Vector2d front = estimation[1];
		
		double orientation = Math.atan2(front.getY()-center.getY(),front.getX()-center.getX());
		
		double degrees = Math.toDegrees(orientation);
		
		if(degrees < 0)
			degrees+=360;
		return  degrees;
	}
	
	/*
	public static void main(String[] args) throws Exception {
		File f = new File("logs/values_18-5-2015_14-5-33.7.log");
		
		RobotKalman k = new RobotKalman();
		
		Scanner s = new Scanner(f);
		
		double prevLat = 0;
		double prevLon = 0;
		
		double cummulativeO1 = 0;
		double cummulativeO2 = 0;
		
		double prevO1 = 0;
		double prevO2 = 0;
		
		double acum1 = 0;
		double acum2 = 0;
		
		while(s.hasNextLine()) {
			
			String line = s.nextLine();
			
			if(line.startsWith("[") || line.startsWith("#") || line.trim().isEmpty())
				continue;
			
			String[] split = line.split("\t");
			
			double lat = Double.parseDouble(split[1]);
			double lon = Double.parseDouble(split[2]);
			double orientation = Double.parseDouble(split[4]);
			double speedL = Double.parseDouble(split[7]);
			double speedR = Double.parseDouble(split[8]);
			
			LatLon latLon = new LatLon(lat,lon);
			Vector2d vec = CoordinateUtilities.GPSToCartesian(latLon);
			
			RobotLocation rl = k.getEstimation(latLon, orientation);
			
			prevLat = lat;
			prevLon = lon;
			
			Vector2d original = CoordinateUtilities.GPSToCartesian(CoordinateUtilities.cartesianToGPS(0,0));
			Vector2d kal = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
			
			double rlOrientation = rl.getOrientation();
			
			cummulativeO1+=(prevO1-orientation);
			cummulativeO2+=(prevO2-rlOrientation);
			
			if(orientation < 50 && prevO1 > 310)
				acum1+=360;
			
			if(orientation > 310 && prevO1 < 50)
				acum1-=360;
			
			if(rlOrientation < 50 && prevO2 > 310)
				acum2+=360;
			
			if(rlOrientation > 310 && prevO2 < 50)
				acum2-=360;
			
			prevO1 = orientation;
			prevO2 = rlOrientation;
			
			System.out.println(orientation+acum2+" "+(rlOrientation+acum2));
		}
		
		s.close();
	} 
	
	
	public static void main(String[] args) throws Exception {
		
		File f = new File("logs/values_18-5-2015_14-5-33.7.log");
		
		RobotKalman k = new RobotKalman();
		
		Scanner s = new Scanner(f);
		
		double prevLat = 0;
		double prevLon = 0;
		
		double cummulativeO1 = 0;
		double cummulativeO2 = 0;
		
		double prevO1 = 0;
		double prevO2 = 0;
		
		double acum1 = 0;
		double acum2 = 0;
		
		DroneMovementModel model = null;
		
		boolean first = true;
		
		int times = 0;
		
		while(s.hasNextLine()) {
			
			String line = s.nextLine();
			
			if(line.startsWith("[") || line.startsWith("#") || line.trim().isEmpty())
				continue;
			
			times++;
			
			String[] split = line.split("\t");
			
			double lat = Double.parseDouble(split[1]);
			double lon = Double.parseDouble(split[2]);
			double orientation = Double.parseDouble(split[4]);
			double speedL = Double.parseDouble(split[7]);
			double speedR = Double.parseDouble(split[8]);
			
			LatLon latLon = new LatLon(lat,lon);
			Vector2d vec = CoordinateUtilities.GPSToCartesian(latLon);
			
			RobotLocation rl = k.getEstimation(latLon, orientation);
			
			if(prevLat == lat && prevLon == lon) {
			
//				if(model != null) {
//					model.correctOrientation(orientation);
//					model.move(speedL, speedR);
//					Vector2d kp = model.getKalmanPosition();
//					double ko = model.getKalmanOrientation();
//					
//					Vector2d simVec = CoordinateUtilities.GPSToCartesian(model.getSimulatedGPSPosition());
//					System.out.println(vec.x+" "+vec.y+" "+kp.x+" "+kp.y+" "+ko+" "+vec.x+" "+vec.y);
//				}
				
				continue;
			}
			
//			if(first) {
//				model = new DroneMovementModel(CoordinateUtilities.GPSToCartesian(new LatLon(lat,lon)), orientation);
//				first = false;
//			} 
//			model.correctPosition(CoordinateUtilities.GPSToCartesian(new LatLon(lat,lon)));
//			model.correctOrientation(orientation);
//			model.move(speedL, speedR);
//			Vector2d kp = model.getKalmanPosition();
//			double ko = model.getKalmanOrientation();
//			Vector2d simVec = CoordinateUtilities.GPSToCartesian(model.getSimulatedGPSPosition());
//			System.out.println(vec.x+" "+vec.y+" "+kp.x+" "+kp.y+" "+ko+" "+vec.x+" "+vec.y);
			
			
//			System.out.println(split[0]+" "+split[7]+" "+split[8]);
			
			prevLat = lat;
			prevLon = lon;
			
			Vector2d original = CoordinateUtilities.GPSToCartesian(latLon);
			Vector2d kal = CoordinateUtilities.GPSToCartesian(rl.getLatLon());
			
//			double rlOrientation = rl.getOrientation();
//			
//			cummulativeO1+=(prevO1-orientation);
//			cummulativeO2+=(prevO2-rlOrientation);
//			
//			if(orientation < 50 && prevO1 > 310)
//				acum1+=360;
//			
//			if(orientation > 310 && prevO1 < 50)
//				acum1-=360;
//			
//			if(rlOrientation < 50 && prevO2 > 310)
//				acum2+=360;
//			
//			if(rlOrientation > 310 && prevO2 < 50)
//				acum2-=360;
//			
//			prevO1 = orientation;
//			prevO2 = rlOrientation;
			
//			System.out.println(orientation+acum2+" "+(rlOrientation+acum2));
			System.out.println(original.getX()+" "+original.getY()+" "+kal.getX()+" "+kal.getY());
//			
//			
//			double lat = s.nextDouble();
//			double lon = s.nextDouble();
//			double o = s.nextDouble();
//			
//			Vector2d v = CoordinateUtilities.GPSToCartesian(new LatLon(lat,lon));
////			System.out.println(v.getX()+" "+v.getY()+" "+o);
//			
//			double oRad = Math.toRadians(o);
//			
//			
//			double x = v.x;
//			double y = v.y;
//			
//			Vector2d tail = new Vector2d(x+(x-0.5)*Math.cos(oRad),y-(y-0.5)*Math.sin(oRad));
//			Vector2d center = new Vector2d(x,y);
//			Vector2d intersection = new Vector2d(x+(x+0.5)*Math.cos(oRad),y+(y-0.5)*Math.sin(oRad));
//			
//			Vector2d[] est = k.getEstimation(tail, center, intersection);
//			
//			System.out.println(est[1].getX()+" "+est[1].getY()+" "+o);
		}
		
		s.close();
		
	}*/
	
}