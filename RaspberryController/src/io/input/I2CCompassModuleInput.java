package io.input;

import java.io.IOException;

import utils.Math_Utils;
import network.messages.CompassMessage;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class I2CCompassModuleInput extends Thread implements ControllerInput,
		MessageProvider {
	/*
	 * I2C Device variables and settings
	 */
	private final static int ADDR = 0x0E; // 7-bit address for the MAG3110

	private final static int CTRL_REG1_DR = 0x00;
	private final static int CTRL_REG1_OS = 0x18;
	private final static int CTRL_REG1 = (CTRL_REG1_DR | CTRL_REG1_OS);

	private final static int CTRL_REG2_AUTO_MRST_EN = 0x80;
	private final static int CTRL_REG2_RAW = 0x00;
	private final static int CTRL_REG2_MAG_RST = 0x00;

	/*
	 * Other variables
	 */
	private final static int I2C_DEVICE_UPDATE_DELAY = 15;

	private I2CBus i2cBus;
	private I2CDevice mag3110;
	private boolean available = false;
	private int[] axisReadings = new int[3];
	private boolean deviceActiveMode = true;

	private int[] rangeXAxisValues={1000,5000};
	private int[] rangeYAxisValues={1000,5000};
	private int[] rangeZAxisValues={1000,5000};
	
	public I2CCompassModuleInput(I2CBus i2cBus) {
		this.i2cBus = i2cBus;

		try {
			// Get device instance
			mag3110 = i2cBus.getDevice(ADDR);

			// Get device instance
			mag3110 = i2cBus.getDevice(ADDR);
			System.out.println("Connection to magnetic sensor established!");

			// Write bits in CTRL_REG2 (set reset and data types)
			mag3110.write((byte) 0x11, (byte) (CTRL_REG2_AUTO_MRST_EN
					| CTRL_REG2_MAG_RST | CTRL_REG2_RAW));

			Thread.sleep(15);

			// Write bits in CTRL_REG1 (set output rate and over sample ratio)
			mag3110.write((byte) 0x10, (byte) (0x01 | CTRL_REG1));

			available = true;
		} catch (IOException e) {
			System.out
					.println("[I2CCompassModule] Error on device initialization");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out
					.println("[I2CCompassModule] Error on device initialization (interruped thread)");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public Message getMessage(Message request) {
		if (!available) {
			return new SystemStatusMessage(
					"[CompassModule] Unable to send Compass data");
		}

		return new CompassMessage((int[]) getReadings());
	}

	@Override
	public Object getReadings() {
		// int[] readings=new
		return null;
	}

	/*
	 * Device readers and modifiers
	 */
	private int readX() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int xl, xh; // define the MSB and LSB

			xh = mag3110.read((byte) 0x01); // x MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			xl = mag3110.read((byte) 0x02); // x LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			int xout = (xl | (xh << 8)); // concatenate the MSB and LSB
			return xout;
		} else {
			return -1;
		}
	}

	private int readY() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int yl, yh; // define the MSB and LSB

			yh = mag3110.read((byte) 0x03); // y MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			yl = mag3110.read((byte) 0x04); // y LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			int yout = (yl | (yh << 8)); // concatenate the MSB and LSB
			return yout;
		} else {
			return -1;
		}
	}

	private int readZ() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int zl, zh; // define the MSB and LSB

			zh = mag3110.read((byte) 0x05); // z MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			zl = mag3110.read((byte) 0x06); // z LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			int zout = (zl | (zh << 8)); // concatenate the MSB and LSB
			return zout;
		} else {
			return -1;
		}
	}

	public void standBy() {
		if (deviceActiveMode) {
			try {
				mag3110.write((byte) 0x10, (byte) CTRL_REG1);
				Thread.sleep(2); // 1.3us free time between start & stop
				deviceActiveMode = false;
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void wakeUp() {
		if (!deviceActiveMode) {
			try {
				mag3110.write((byte) 0x10, (byte) (CTRL_REG1 | 0x01));
				Thread.sleep(2); // 1.3us free time between start & stop
				deviceActiveMode = true;
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				int[] rawAxisReadings = new int[3];

				rawAxisReadings[0] = readX();
				rawAxisReadings[1] = readY();
				rawAxisReadings[2] = readZ();

				processRawAxisReadings(rawAxisReadings);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				Thread.sleep(I2C_DEVICE_UPDATE_DELAY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private synchronized void processRawAxisReadings(int[] readings) {
		if(readings[0]<rangeXAxisValues[0]){
			rangeXAxisValues[0]=readings[0];
		}
		
		if(readings[0]>rangeXAxisValues[1]){
			rangeXAxisValues[1]=readings[0];
		}
		
		
		if(readings[1]<rangeYAxisValues[0]){
			rangeYAxisValues[0]=readings[1];
		}
		
		if(readings[1]>rangeYAxisValues[1]){
			rangeYAxisValues[1]=readings[1];
		}
		
		
		if(readings[2]<rangeZAxisValues[0]){
			rangeZAxisValues[0]=readings[2];
		}
		
		if(readings[2]>rangeZAxisValues[1]){
			rangeZAxisValues[1]=readings[2];
		}
		
		
		axisReadings[0]=(int)(Math_Utils.map(readings[0], rangeXAxisValues[0], rangeXAxisValues[1], 0, 359));
		axisReadings[1]=(int)(Math_Utils.map(readings[1], rangeYAxisValues[0], rangeYAxisValues[1], 0, 359));
		axisReadings[2]=(int)(Math_Utils.map(readings[2], rangeZAxisValues[0], rangeZAxisValues[1], 0, 359));
	}
}
