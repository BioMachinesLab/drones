package main;

import io.input.GPSModuleInput;

public class Test {
	
	public static void main(String[] args) {
		
		GPSModuleInput gpsModule = null;
		
		try {
			// GPS Module Init
			gpsModule = new GPSModuleInput();

			if (gpsModule.isAvailable()) {
				gpsModule.enableLocalLog();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}