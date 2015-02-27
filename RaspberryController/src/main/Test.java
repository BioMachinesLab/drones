package main;

import io.input.I2CTemperatureModuleInput;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

public class Test {
	
	public static void main(String[] args) {
		
		I2CBus i2cBus = null;
		
		try {
			i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		I2CTemperatureModuleInput tmp = new I2CTemperatureModuleInput(i2cBus);
		tmp.start();
	}
	
}