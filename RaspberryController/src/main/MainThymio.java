package main;

import java.util.HashMap;
import java.util.Scanner;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealThymioCI;

public class MainThymio {

	public static void main(String[] args) {
		RealThymioCI thymio = new RealThymioCI();
		
		try {
		
			HashMap<String,CIArguments> arg = CIArguments.parseArgs(new String[]{"config/thymio.conf"});
			
			thymio.begin(arg);
			thymio.start();
		
			Scanner s = new Scanner(System.in);
			while(s.hasNextLine()) {
				String line = s.nextLine();
				if(line.equals("q")) {
					thymio.shutdown();
					s.close();
					System.exit(0);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
