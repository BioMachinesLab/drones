package io.input;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import commoninterface.RobotCI;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;

public class I2CCompassModuleInput extends Thread implements ControllerInput,
		MessageProvider {
	/*
	 * I2C Device variables and settings
	 */
	private final static String CALIBRATION_FILE = "calibration.txt";
	private final static int ADDR = 0x0E; // 7-bit address for the MAG3110

	private final static int CTRL_REG1_DR = 0x00;
	private final static int CTRL_REG1_OS = 0x18;
	private final static int CTRL_REG1 = (CTRL_REG1_DR | CTRL_REG1_OS);

	private final static int CTRL_REG2_AUTO_MRST_EN = 0x80;
	private final static int CTRL_REG2_RAW = 0x20;// was 0x00 before!
	private final static int CTRL_REG2_MAG_RST = 0x00;

	/*
	 * Other variables
	 */
	private final static int I2C_DEVICE_UPDATE_DELAY = 15;

	private I2CDevice mag3110;
	private I2CBus i2cBus;
	private boolean available = false;
	private boolean deviceActiveMode = true;
	private int headingInDegrees = 0;

	private ArrayList<double[]> calibrationValues = new ArrayList<double[]>();

	private boolean calibrationStatus = false;

	private double xCenter, yCenter, zCenter;

	private double alpha, beta, theta;

	private double scaleX, scaleY, scaleZ;

	private double ct, st;
	private double cb, sb;
	private double ca, sa;
	private double gps, sgps, cgps;

	private double[] north = { 0, 0, 0 };

	private RobotCI robotCI;

	public I2CCompassModuleInput(I2CBus i2cBus, RobotCI robotCI) {
		this.robotCI = robotCI;
		this.i2cBus = i2cBus;
		configureCompass();
		readPreviousCalibration();
	}

	private void readPreviousCalibration() {
		try {
			Scanner s = new Scanner(new File(CALIBRATION_FILE));
			xCenter = s.nextDouble();
			yCenter = s.nextDouble();
			zCenter = s.nextDouble();
			alpha = s.nextDouble();
			ca = s.nextDouble();
			sa = s.nextDouble();
			beta = s.nextDouble();
			cb = s.nextDouble();
			sb = s.nextDouble();
			theta = s.nextDouble();
			ct = s.nextDouble();
			st = s.nextDouble();
			scaleX = s.nextDouble();
			scaleY = s.nextDouble();
			scaleZ = s.nextDouble();
			gps = s.nextDouble();
			sgps = s.nextDouble();
			cgps = s.nextDouble();
			north[0] = s.nextDouble();
			north[1] = s.nextDouble();
			north[2] = s.nextDouble();
			s.close();
			System.out.println("[Compass] Calibration loaded from the file!");
		} catch (IOException e) {
		}
	}

	public void configureCompass() {
		try {

			// Get device instance
			mag3110 = i2cBus.getDevice(ADDR);
			// System.out.println("Connection to magnetic sensor established!");

			// Write bits in CTRL_REG2 (set reset and data types)
			mag3110.write((byte) 0x11, (byte) (CTRL_REG2_AUTO_MRST_EN
					| CTRL_REG2_MAG_RST | CTRL_REG2_RAW));

			Thread.sleep(15);

			// Write bits in CTRL_REG1 (set output rate and over sample ratio)
			mag3110.write((byte) 0x10, (byte) (0x01 /* | CTRL_REG1 */));

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
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.COMPASS)) {
			if (!available) {
				return new SystemStatusMessage(
						"[CompassModule] Unable to send Compass data",
						robotCI.getNetworkAddress());
			}
			return new CompassMessage(getHeadingInDegrees(),
					robotCI.getNetworkAddress());
		}
		return null;
	}

	@Override
	public Object getReadings() {
		return headingInDegrees;
	}

	/*
	 * Device readers and modifiers
	 */
	private short readX() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int xl, xh; // define the MSB and LSB

			xh = getByte((byte) 0x01); // x MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &

			xl = getByte((byte) 0x02); // x LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &

			short xout = (short) ((xl | (xh << 8)) & 0xFFFF); // concatenate the
																// MSB and LSB

			return xout;
		} else {
			return -1;
		}
	}

	private short readY() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int yl, yh; // define the MSB and LSB

			yh = getByte((byte) 0x03); // y MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			yl = getByte((byte) 0x04); // y LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			short yout = (short) ((yl | (yh << 8)) & 0xFFFF); // concatenate the
																// MSB and LSB

			return yout;
		} else {
			return -1;
		}
	}

	private int getByte(byte address) throws IOException {
		return mag3110.read(address);
	}

	private short readZ() throws IOException, InterruptedException {

		// return 0;
		if (deviceActiveMode) {
			int zl, zh; // define the MSB and LSB

			zh = getByte((byte) 0x05); // z MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			zl = getByte((byte) 0x06); // z LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			short zout = (short) ((zl | (zh << 8)) & 0xFFFF); // concatenate the
																// MSB and LSB

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
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				double[] rawAxisReadings = readXYZ();

				if (calibrationStatus) {
					handleCalibration(rawAxisReadings[0], rawAxisReadings[1],
							rawAxisReadings[2]);
				} else {
					double[] converted = convert(rawAxisReadings[0],
							rawAxisReadings[1], rawAxisReadings[2]);
					double heading = Math.atan2(converted[1], converted[0]);

					// Value for Lisbon is -2º (0.034906585 rad). Find more
					// here: http://www.magnetic-declination.com
					// double declinationAngle = 0.034906585;
					// heading += declinationAngle;

					// heading = 2*Math.PI-heading;

					if (heading < 0) {
						heading += 2 * Math.PI;
					}

					if (heading > 2 * Math.PI) {
						heading -= 2 * Math.PI;
					}

					// Convert radians to degrees for readability.
					this.headingInDegrees = (int) (heading * 180 / Math.PI);
				}

				Thread.sleep(I2C_DEVICE_UPDATE_DELAY);
			}

		} catch (InterruptedException e) {
			System.out.println("[Compass] Terminated!");
		}
	}

	private void handleCalibration(double x, double y, double z) {
		double[] vals = new double[3];
		vals[0] = x;
		vals[1] = y;
		vals[2] = z;

		// System.out.println(vals[0]+" "+vals[1]+" "+vals[2]);
		System.out.print(".");
		calibrationValues.add(vals);
	}

	public boolean getCalibrationStatus() {
		return calibrationStatus;
	}

	public void startCalibration() {

		double[] readings = readXYZ();

		north[0] = readings[0];
		north[1] = readings[1];
		north[2] = readings[2];

		System.out.println("North acquired. Rotate drone.");

		calibrationValues.clear();
		calibrationStatus = true;
	}

	public void endCalibration() {
		System.out.println("Calibration ended!");
		calibrationStatus = false;
		finalizeCalibration();
	}

	public int getHeadingInDegrees() {
		return headingInDegrees;
	}

	public synchronized double[] readXYZ() {

		double[] readings = new double[3];

		boolean successful = false;
		while (!successful) {
			try {
				readings[0] = readX();
				readings[1] = readY();
				readings[2] = readZ();
				successful = true;
			} catch (IOException | InterruptedException e) {
				try {
					// after a PI4J exception, the next reading is usually crap.
					// Get rid of it right away
					// System.out.println("Compass went haywire! Reconnecting...");
					System.out
							.println("[I2CompassModule] Re-initing compass module");
					configureCompass();
				} catch (Exception ex) {
					e.printStackTrace();
				}
			}
		}
		return readings;
	}

	private void finalizeCalibration() {

		String toSave = "";

		// Translation
		double xMin = Double.MAX_VALUE;
		double xMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double zMin = Double.MAX_VALUE;
		double zMax = -Double.MAX_VALUE;

		for (double[] data : calibrationValues) {
			// System.out.println(data[0]+" "+data[1]+" "+data[2]);
			xMin = Math.min(data[0], xMin);
			xMax = Math.max(data[0], xMax);
			yMin = Math.min(data[1], yMin);
			yMax = Math.max(data[1], yMax);
			zMin = Math.min(data[2], zMin);
			zMax = Math.max(data[2], zMax);
		}

		xCenter = (xMax + xMin) / 2;
		yCenter = (yMax + yMin) / 2;
		zCenter = (zMax + zMin) / 2;

		// center all coordinates
		for (double[] data : calibrationValues) {
			data[0] = data[0] - xCenter;
			data[1] = data[1] - yCenter;
			data[2] = data[2] - zCenter;
		}

		System.out.println("xCenter " + xCenter + " yCenter " + yCenter
				+ " zCenter " + zCenter);

		// Rotation
		double maxVector = -Double.MAX_VALUE;
		double maxVectorX = -Double.MAX_VALUE;
		double maxVectorY = -Double.MAX_VALUE;
		double maxVectorZ = -Double.MAX_VALUE;

		for (double[] data : calibrationValues) {
			double vector = Math.pow(data[0], 2) + Math.pow(data[1], 2)
					+ Math.pow(data[2], 2);
			if (vector > maxVector) {
				maxVector = vector;
				maxVectorX = data[0];
				maxVectorY = data[1];
				maxVectorZ = data[2];
			}
		}

		// System.out.println("RAW maxVectorX "+(maxVectorX+xCenter)+" maxVectorY "+(maxVectorY+yCenter)+" maxVectorZ "+(maxVectorZ+zCenter));
		// System.out.println("Vector size ^2 "+(Math.pow(maxVectorX, 2) +
		// Math.pow(maxVectorY, 2) + Math.pow(maxVectorZ, 2)));
		// System.out.println("maxVectorX "+maxVectorX+" maxVectorY "+maxVectorY+" maxVectorZ "+maxVectorZ);

		alpha = Math.atan2(maxVectorZ, maxVectorX);

		if (maxVectorX < 0) {
			alpha += Math.PI;
		}

		ca = Math.cos(alpha);
		sa = Math.sin(alpha);

		double xRotated = ca * maxVectorX + sa * maxVectorZ;
		double yRotated = maxVectorY;
		double zRotated = -sa * maxVectorX + ca * maxVectorZ;
		// System.out.println();
		// System.out.println("###FIRST ROTATION");
		// System.out.println("Vector size rotated "+(Math.pow(xRotated, 2) +
		// Math.pow(yRotated, 2) + Math.pow(zRotated, 2)));
		// System.out.println("maxVectorXR "+xRotated+" maxVectorYR "+yRotated+" maxVectorZR "+zRotated);

		beta = Math.atan2(yRotated, xRotated);
		cb = Math.cos(-beta);
		sb = Math.sin(-beta);

		if (xRotated < 0) {
			beta += Math.PI;
		}

		// System.out.println("alpha "+Math.toDegrees(alpha)+" beta "+Math.toDegrees(beta));

		// double xRotated2 = cosb*xRotated - sinb*yRotated ;
		// double yRotated2 = sinb*xRotated + cosb*yRotated ;
		// double zRotated2 = zRotated ;

		// System.out.println("maxVectorXR "+xRotated2+" maxVectorYR "+yRotated2+" maxVectorZR "+zRotated2);
		// System.out.println();
		// System.out.println("###SECOND AND THIRD ROTATION");

		for (double[] data : calibrationValues) {
			double x = data[0];
			double y = data[1];
			double z = data[2];

			xRotated = cb * ca * x - sb * y + cb * sa * z;
			yRotated = ca * sb * x + cb * y + sa * sb * z;
			zRotated = -sa * x + ca * z;

			data[0] = xRotated;
			data[1] = yRotated;
			data[2] = zRotated;
		}

		maxVector = -Double.MAX_VALUE;
		maxVectorX = -Double.MAX_VALUE;
		maxVectorY = -Double.MAX_VALUE;
		maxVectorZ = -Double.MAX_VALUE;

		for (double[] data : calibrationValues) {
			double vector = Math.pow(data[0], 2) + Math.pow(data[1], 2)
					+ Math.pow(data[2], 2);
			if (vector > maxVector) {
				maxVector = vector;
				maxVectorX = data[0];
				maxVectorY = data[1];
				maxVectorZ = data[2];
			}
		}

		// System.out.println("maxVectorX "+maxVectorX+" maxVectorY "+maxVectorY+" maxVectorZ "+maxVectorZ);

		theta = Math.atan2(maxVectorZ, maxVectorY);

		if (maxVectorY < 0) {
			theta += Math.PI;
		}

		ct = Math.cos(-theta);
		st = Math.sin(-theta);

		// System.out.println("theta "+Math.toDegrees(theta));

		for (double[] data : calibrationValues) {
			double y = data[1];
			double z = data[2];

			yRotated = ct * y - st * z;
			zRotated = st * y + ct * z;

			data[1] = yRotated;
			data[2] = zRotated;

		}

		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		double maxZ = -Double.MAX_VALUE;

		for (double[] data : calibrationValues) {
			maxX = Math.max(data[0], maxX);
			maxY = Math.max(data[1], maxY);
			maxZ = Math.max(data[2], maxZ);
		}

		scaleX = 1.0 / maxX;
		scaleY = 1.0 / maxY;
		scaleZ = 1.0 / maxZ;

		double[] northTransformed = transformReading(north[0], north[1],
				north[2]);

		gps = Math.atan2(northTransformed[1], northTransformed[0]);

		// if(north[0] < 0)
		// gps+=Math.PI;

		sgps = Math.sin(-gps);
		cgps = Math.cos(-gps);

		double[] res = convert(north[0], north[1], north[2]);

		toSave += xCenter + " ";
		toSave += yCenter + " ";
		toSave += zCenter + " ";
		toSave += alpha + " ";
		toSave += ca + " ";
		toSave += sa + " ";
		toSave += beta + " ";
		toSave += cb + " ";
		toSave += sb + " ";
		toSave += theta + " ";
		toSave += ct + " ";
		toSave += st + " ";
		toSave += scaleX + " ";
		toSave += scaleY + " ";
		toSave += scaleZ + " ";
		toSave += gps + " ";
		toSave += sgps + " ";
		toSave += cgps + " ";
		toSave += north[0] + " ";
		toSave += north[1] + " ";
		toSave += north[2] + " ";

		try {
			FileWriter fw = new FileWriter(new File(CALIBRATION_FILE));
			fw.write(toSave);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("north transf " + northTransformed[0] + " "
				+ northTransformed[1] + " " + northTransformed[2]);
		System.out.println("gps angle " + gps);
		System.out.println("RESULT " + res[0] + " " + res[1] + " " + res[2]);
	}

	private double[] convert(double x, double y, double z) {

		double[] res = new double[3];

		res[0] = (-ct * sb * sgps * scaleY + cb
				* (ca * cgps * scaleX - sa * sgps * st * scaleY))
				* (x - xCenter)
				+ (-cb * ct * sgps * scaleY - sb
						* (ca * cgps * scaleX - sa * sgps * st * scaleY))
				* (y - yCenter)
				+ (cgps * sa * scaleX + ca * sgps * st * scaleY)
				* (z - zCenter);

		res[1] = (cgps * ct * sb * scaleY + cb
				* (ca * sgps * scaleX + cgps * sa * st * scaleY))
				* (x - xCenter)
				+ (cb * cgps * ct * scaleY - sb
						* (ca * sgps * scaleX + cgps * sa * st * scaleY))
				* (y - yCenter)
				+ (sa * sgps * scaleX - ca * cgps * st * scaleY)
				* (z - zCenter);

		res[2] = (-cb * ct * sa * scaleZ + sb * st * scaleZ) * (x - xCenter)
				+ (ct * sa * sb * scaleZ + cb * st * scaleZ) * (y - yCenter)
				+ ca * ct * scaleZ * (z - zCenter);

		return res;
	}

	private double[] transformReading(double x, double y, double z) {

		double[] res = new double[3];

		res[0] = ca * cb * scaleX * (x - xCenter) - ca * sb * scaleX
				* (y - yCenter) + sa * scaleX * (z - zCenter);

		res[1] = (ct * sb * scaleY + cb * sa * st * scaleY) * (x - xCenter)
				+ (cb * ct * scaleY - sa * sb * st * scaleY) * (y - yCenter)
				- ca * st * scaleY * (z - zCenter);
		res[2] = (-cb * ct * sa * scaleZ + sb * st * scaleZ) * (x - xCenter)
				+ (ct * sa * sb * scaleZ + cb * st * scaleZ) * (y - yCenter)
				+ ca * ct * scaleZ * (z - zCenter);

		return res;

	}
}
