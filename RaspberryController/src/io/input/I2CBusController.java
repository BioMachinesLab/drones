package io.input;

import java.io.IOException;
import java.util.ArrayList;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

public class I2CBusController extends Thread {
	/*
	 * ################ Currently not in use!
	 */
	
	/*
	 * Bus_1 on the 512Mb raspberry version and Bus_0 on the 256Mb raspberry
	 * version
	 */
	public static final int I2C_BUS = I2CBus.BUS_1;

	private I2CBus i2cBus;
	private boolean available = false;
	private ArrayList<ControllerInput> devices = new ArrayList<ControllerInput>();

	public I2CBusController() {
		try {
			i2cBus = I2CFactory.getInstance(I2C_BUS);
			available = true;
		} catch (IOException e) {
			System.out
					.println("[I2CBusController] Failed to start the I2C bus!");
			System.out.println(e.getMessage());
		}
	}

	public boolean isAvailable() {
		return available;
	}

	public void addDeviceToBus(ControllerInput device) {
		devices.add(device);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}
}
