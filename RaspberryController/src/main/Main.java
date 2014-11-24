package main;

import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;
import commoninterfaceimpl.RealAquaticDroneCI;

public class Main {
	
	public static void main(String[] args) {
		RealAquaticDroneCI drone = new RealAquaticDroneCI();
		CILogger logger = new CIStdOutLogger(drone);
		drone.begin(new String[]{},logger);
		drone.start();
	}

}
