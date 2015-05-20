package main;

import java.util.HashMap;
import java.util.Scanner;

import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class MainAquaticDrone {
	
	public static void main(String[] args) {
		
		RealAquaticDroneCI drone = new RealAquaticDroneCI();
		
		try {
		
			HashMap<String,CIArguments> arg = CIArguments.parseArgs(new String[]{"config/drone.conf"});
			
			drone.begin(arg);
			drone.start();
		
			Scanner s = new Scanner(System.in);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(line.equals("q")) {
					drone.shutdown();
					s.close();
					System.exit(0);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
	}

}
