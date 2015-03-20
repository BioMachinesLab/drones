package main;

import java.util.Scanner;

import simpletestbehaviors.ThymioRandomWalkCIBehavior;

import commoninterface.CIStdOutLogger;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealThymioCI;

public class MainThymio {

	public static void main(String[] args) {
		RealThymioCI thymio = new RealThymioCI();
		CIStdOutLogger logger = new CIStdOutLogger(thymio);
		thymio.begin(new CIArguments(""),logger);
		thymio.start();
		
		try {
			Scanner s = new Scanner(System.in);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(line.equals("q")) {
					thymio.shutdown();
					break;
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
