package main;

import io.input.I2CCompassLSM303Input;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

public class Test {
	
	public static void main(String[] args) throws InterruptedException {
		
		I2CBus i2cBus = null;
		
		boolean calibration = false;
		
		try {
			i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		I2CCompassLSM303Input c = new I2CCompassLSM303Input(i2cBus);
		
		if(calibration)
			c.startCalibration();
		
		c.start();
		
		if(calibration) {
			for(int i = 0 ; i < 20 ; i++) {
				System.out.println("Sleeping... "+i);
				Thread.sleep(1000);
			}
			
			c.endCalibration();
		}
	}
	
}