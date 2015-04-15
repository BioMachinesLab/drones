package main;

import java.util.Scanner;
import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class Main {
	
	public static void main(String[] args) {
		RealAquaticDroneCI drone = new RealAquaticDroneCI();
		drone.begin(new CIArguments(""));
		drone.start();
		
		try {
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
