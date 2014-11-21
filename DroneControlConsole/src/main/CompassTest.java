package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CompassTest {
	
	public static void main(String[] args) throws Exception{
		
		Scanner s = new Scanner(new File("log.txt"));
		
		ArrayList<int[]> calibrationValues = new ArrayList<int[]>();
		
		while(s.hasNextLine()){
			String line = s.nextLine();
			
			String[] sVals = line.trim().split(" ");
			
			int[] v = new int[3];
			
			v[0] = Integer.parseInt(sVals[0]);
			v[1] = Integer.parseInt(sVals[1]);
			v[2] = Integer.parseInt(sVals[2]);
			
			calibrationValues.add(v);
		}
		
		//Translation
		int xMin = Integer.MAX_VALUE;
		int xMax = -Integer.MAX_VALUE;
		int yMin = Integer.MAX_VALUE;
		int yMax = -Integer.MAX_VALUE;
		
		for(int[] data : calibrationValues) {
			xMin = Math.min(data[0],xMin);
			xMax = Math.max(data[0],xMax);
			yMin = Math.min(data[1],yMin);
			yMax = Math.max(data[1],yMax);
		}
		
		int xCenter = (xMax + xMin)/2;
		int yCenter = (yMax + yMin)/2;
		
		System.out.println("xCenter "+xCenter+" yCenter "+yCenter);
		
		FileWriter fw = new FileWriter(new File("log1.txt"));
		for(int[] data : calibrationValues) {
			int x = data[0]-xCenter;
			int y = data[1]-yCenter;
			fw.append(x+" "+y+"\n");
		}
		fw.close();
		
		//Rotation
		int maxVector = -Integer.MAX_VALUE;
		int maxVectorX = -Integer.MAX_VALUE;
		int maxVectorY = -Integer.MAX_VALUE;
		
		for(int[] data : calibrationValues) {
			int vector = (int)(Math.pow(data[0]-xCenter, 2) + Math.pow(data[1]-yCenter, 2));
			if(vector > maxVector) {
				maxVector = vector;
				maxVectorX = data[0] - xCenter;//changed
				maxVectorY = data[1] - yCenter;//changed
			}
		}
		
//		double theta = Math.atan2(-maxVectorY, maxVectorX);
		double theta = Math.PI/2;
		
		System.out.println("maxVectorX "+maxVectorX+" maxVectorY "+maxVectorY);
		System.out.println("theta "+theta);
		
		fw = new FileWriter(new File("log2.txt"));
		for(int[] data : calibrationValues) {
			int x = (int)((data[0]-xCenter) * Math.cos(theta) - (data[1]-yCenter) * Math.sin(theta));
			int y = (int)((data[0]-xCenter) * Math.sin(theta) + (data[1]-yCenter) * Math.cos(theta));
			fw.append(x+" "+y+"\n");
		}
		fw.close();
		
		int xMaxRotatedTranslated = -Integer.MAX_VALUE;
		int yMaxRotatedTranslated = -Integer.MAX_VALUE;
		
		for(int[] data : calibrationValues) {
			int x = (int)((data[0]-xCenter) * Math.cos(theta) - (data[1]-yCenter) * Math.sin(theta));
			int y = (int)((data[0]-xCenter) * Math.sin(theta) + (data[1]-yCenter) * Math.cos(theta));
			xMaxRotatedTranslated = Math.max(x,xMaxRotatedTranslated);
			yMaxRotatedTranslated = Math.max(y,yMaxRotatedTranslated);
		}
		
		System.out.println("xMaxRotatedTranslated "+xMaxRotatedTranslated+" yMaxRotatedTranslated "+yMaxRotatedTranslated);
		
	}

}
