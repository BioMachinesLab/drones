package behaviors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class TestMotorsCIBehavior extends CIBehavior {
	
	private RealAquaticDroneCI drone;
	private String fileName = "vals.log";
	private Scanner s;
	private boolean zero = false;
	
	public TestMotorsCIBehavior(CIArguments args, RobotCI drone) {
		super(args,drone);
		this.drone = (RealAquaticDroneCI)drone;
		try {
			s = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void step(double time) {
		if(s.hasNextLine()) {
			
			String line = getGoodString();
			String[] split = line.split("\t");
			double left = Double.parseDouble(split[7]);
			double right = Double.parseDouble(split[8]);
			
			while(left == 0 && right == 0 && zero) {
				System.out.println("Ignoring "+line);
				line = getGoodString();
				split = line.split("\t");
				left = Double.parseDouble(split[7]);
				right = Double.parseDouble(split[8]);
			} 
			
			zero = left == 0 && right == 0;
			
			drone.setMotorSpeeds(left, right);
			System.out.println(left+" "+right);
			
		} else {
			try {
				System.out.println("GOING AGAIN!");
				s.close();
				s = new Scanner(new File(fileName));
			}catch(IOException e){};
		}
	}
	
	private String getGoodString() {
		String line = s.nextLine();
		
		while(s.hasNextLine() && line.startsWith("#"))
			line = s.nextLine();
		
		return line;
	}
	
}