package commoninterface.utils;

import jama.Matrix;

import java.io.File;
import java.util.Scanner;

import jkalman.JKalman;

import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;

public class KalmanCompass {

	private JKalman kalman;
	private Matrix s; // state [x, y]
	private Matrix c; // corrected state [x, y]
	private Matrix m; // measurement [x]
	boolean configured = false;

	private void configure(double d) {
		
		try {

			kalman = new JKalman(2, 1);

			s = new Matrix(2, 1);
			c = new Matrix(2, 1);

			m = new Matrix(1, 1);
			m.set(0, 0, d);
			
			// transitions for o,vo
            double[][] tr = { {1, 1,}, //o
                              {0, 1,}, //vo
                              		};
            
            kalman.setTransition_matrix(new Matrix(tr));
            
            kalman.setError_cov_post(kalman.getError_cov_post().identity());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		configured = true;
	}
	
	public double getEstimation(double d) {
		
		if(!configured)
			configure(d);
		
		s = kalman.Predict();
		
		m.set(0, 0, d);
        
        c = kalman.Correct(m);
        
        return c.get(0, 0);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		File f = new File("logs/values_18-5-2015_14-5-33.7.log");
		
		KalmanCompass k = new KalmanCompass();
		
		Scanner s = new Scanner(f);
		
		double prevO1 = 0;
		double prevO2 = 0;
		
		double acum1 = 0;
		double acum2 = 0;
		
		double cummulativeO1 = 0;
		double cummulativeO2 = 0;
		
		while(s.hasNextLine()) {
			
			String line = s.nextLine();
			
			if(line.startsWith("[") || line.startsWith("#") || line.trim().isEmpty())
				continue;
			
			String[] split = line.split("\t");
			
			double orientation = Double.parseDouble(split[4]);
			
			double rlOrientation = k.getEstimation(orientation);
			
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
			
			System.out.println(orientation+" "+rlOrientation);
		}
		
		s.close();
		
	}
	
}