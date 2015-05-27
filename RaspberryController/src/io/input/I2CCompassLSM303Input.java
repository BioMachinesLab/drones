package io.input;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import commoninterface.RobotCI;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;

public class I2CCompassLSM303Input extends Thread implements ControllerInput,
		MessageProvider {

	private final static String CALIBRATION_FILE = "config/calibration.txt";

	public final static byte LSM303_ADDRESS = (0x3a >> 1); // 0x1D
	public final static byte LSM303_CTRL0 = 0x1F;
	public final static byte LSM303_REGISTER_ACCEL_OUT_X_L_A = 0x28;
	public final static byte LSM303_REGISTER_MAG_OUT_X_L_M = 0x08;

	private I2CDevice compass;

	private I2CBus i2cBus;
	private boolean available = false;
	private boolean tryAgainAfterError = false;
	private boolean calibrationStatus = false;
	private int headingInDegrees = 0;
	
	private int offset = 0;

	private int[] min = { Integer.MAX_VALUE, Integer.MAX_VALUE,
			Integer.MAX_VALUE };
	private int[] max = { -Integer.MAX_VALUE, -Integer.MAX_VALUE,
			-Integer.MAX_VALUE };

	private RobotCI robotCI;
	
	public I2CCompassLSM303Input(I2CBus i2cBus, RobotCI robotCI) {
		this.i2cBus = i2cBus;
		this.robotCI = robotCI;
		configureCompass();
		readPreviousCalibration();
	}
	
	@Override
	public void run() {
		
		if(!available)
			return;
		
		try {
			while(true) {
				try {
					readValues();
					Thread.sleep(50);
				} catch(IOException e) {
					System.out.println("[I2CCompassLSM303Input] Re-initing compass module");
					configureCompass();
				}
			}
		}catch(InterruptedException e){
			System.out.println("[I2CCompassLSM303Input] Interrupted, terminating");
		}
	}

	private void configureCompass() {

		try {

			compass = i2cBus.getDevice(LSM303_ADDRESS);

			// Enable accelerometer
			// AODR = 0101 (50 Hz ODR); AZEN = AYEN = AXEN = 1 (all axes enabled)
			compass.write(0x20, (byte) 0x27);//57
			// AFS = 0 (+/- 2 g full scale)
			compass.write(0x23, (byte) 0x00);

			// Enable magnetometer
			// M_RES = 11 (high resolution mode); M_ODR = 001 (6.25 Hz ODR)
			compass.write(LSM303_CTRL0+5, (byte) 0x64);
			// MFS = 01 (+/- 4 gauss full scale)
			compass.write(LSM303_CTRL0+6, (byte) 0x20);
			// MLP = 0 (low power mode off); MD = 00 (continuous-conversion mode)
			compass.write(LSM303_CTRL0+7, (byte) 0x00);

			available = true;

		} catch (Exception e) {
			System.out.println("[I2CompassLSM303] Error during init!");
			e.printStackTrace();
			if (tryAgainAfterError) {
				configureCompass();
			}
		}
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.COMPASS)) {
			if (!available) {
				return new SystemStatusMessage(
						"[I2CompassLSM303] Unable to send Compass data",robotCI.getNetworkAddress());
			}
			return new CompassMessage(getHeadingInDegrees(),robotCI.getNetworkAddress());
		}
		return null;
	}

	/*
	Calculates the angular difference in the horizontal plane between the
	"from" vector and north, in degrees.

	Description of heading algorithm:
	Shift and scale the magnetic reading based on calibration data to find
	the North vector. Use the acceleration readings to determine the Up
	vector (gravity is measured as an upward acceleration). The cross
	product of North and Up vectors is East. The vectors East and North
	form a basis for the horizontal plane. The From vector is projected
	into the horizontal plane and the angle between the projected vector
	and horizontal north is returned.
	*/
	private void readValues() throws IOException {
		
		if(calibrationStatus) {
			
			calibrate();
			
		} else {
			
			int accelX = readSingleValue(LSM303_REGISTER_ACCEL_OUT_X_L_A);
			try{Thread.sleep(20);}catch(Exception e){}
			int accelY = readSingleValue(LSM303_REGISTER_ACCEL_OUT_X_L_A+2);
			try{Thread.sleep(20);}catch(Exception e){}
			int accelZ = readSingleValue(LSM303_REGISTER_ACCEL_OUT_X_L_A+4);
			try{Thread.sleep(20);}catch(Exception e){}
			
			double[] accel = new double[]{accelX, accelY, accelZ};
	
			int magX = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M);
			int magY = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M+2);
			int magZ = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M+4);
			
//			System.out.println("accel "+accelX+" "+accelY+" "+accelZ);
//			System.out.println("mag "+magX+" "+magY+" "+magZ);
			
			magX-= (min[0] + max[0])/2;
			magY-= (min[1] + max[1])/2;
			magZ-= (min[2] + max[2])/2;
			
			double[] mag = new double[]{magX, magY, magZ};
			
			//compute E and N
			double[] east = new double[3];
			double[] north = new double[3];
			
			vectorCross(mag, accel, east);
			vectorNormalize(east);
			vectorCross(accel, east, north);
			vectorNormalize(north);
			
			//depending on how the compass is physically oriented
			double[] from = new double[]{0, 1 ,0};
			
		    double heading =
		    		Math.atan2(
		    			vectorDot(east, from),
		    			vectorDot(north, from)
		    		) * 180 / Math.PI;
		    if (heading < 0) heading += 360;
			
		    this.headingInDegrees = (int)heading + offset;
		}
	}
	
	public void startCalibration() {
		
		min = new int[]{ Integer.MAX_VALUE, Integer.MAX_VALUE,
				Integer.MAX_VALUE };
		max = new int[]{ -Integer.MAX_VALUE, -Integer.MAX_VALUE,
				-Integer.MAX_VALUE };
		
		calibrationStatus = true;
	}
	
	public void endCalibration() {
		calibrationStatus = false;
		saveCalibration();
	}
	
	private void saveCalibration() {
		try {
			FileWriter fw = new FileWriter(new File(CALIBRATION_FILE));
			fw.write(min[0]+" "+min[1]+" "+min[2]+" "+max[0]+" "+max[1]+" "+max[2]);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readPreviousCalibration() {
		try {
			Scanner s = new Scanner(new File(CALIBRATION_FILE));
			min[0] = s.nextInt();
			min[1] = s.nextInt();
			min[2] = s.nextInt();
			max[0] = s.nextInt();
			max[1] = s.nextInt();
			max[2] = s.nextInt();
			s.close();
			System.out.println("[I2CompassLSM303] Calibration loaded from the file!");
		} catch (Exception e) {}
	}
	
	private void calibrate() throws IOException {
		
		int magX = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M);
		int magY = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M+2);
		int magZ = readSingleValue(LSM303_REGISTER_MAG_OUT_X_L_M+4);
		
		min[0] = Math.min(magX,min[0]);
		min[1] = Math.min(magY,min[1]);
		min[2] = Math.min(magZ,min[2]);
		
		max[0] = Math.max(magX,max[0]);
		max[1] = Math.max(magY,max[1]);
		max[2] = Math.max(magZ,max[2]);
		
		System.out.println("min "+min[0]+" "+min[1]+" "+min[2]+" max "+max[0]+" "+max[1]+" "+max[2]);
	}

	@Override
	public Object getReadings() {
		return getHeadingInDegrees();
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	public int getHeadingInDegrees() {
		return headingInDegrees;
	}
	
	private short readSingleValue(int addr) throws IOException{
		int l = compass.read(addr);
		int h = compass.read(addr+1);
		
		short out = (short)((l | (h << 8)) & 0xFFFF);
		return out;
	}
	
	private static void vectorCross(double[] a, double[] b, double[] out) {
		//out->x = (a->y * b->z) - (a->z * b->y)
		out[0] = (a[1] * b[2]) - (a[2] * b[1]);
		//out->y = (a->z * b->x) - (a->x * b->z)
		out[1] = (a[2] * b[0]) - (a[0] * b[2]);
		//out->z = (a->x * b->y) - (a->y * b->x)
		out[2] = (a[0] * b[1]) - (a[1] * b[0]);
	}
	
	private static double vectorDot(double[] a, double[] b) {
		//(a->x * b->x) + (a->y * b->y) + (a->z * b->z)
		return (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]);
	}
	
	private static void vectorNormalize(double[] a) {
		double mag = Math.sqrt(vectorDot(a,a));
		a[0]/= mag;
		a[1]/= mag;
		a[2]/= mag;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
}