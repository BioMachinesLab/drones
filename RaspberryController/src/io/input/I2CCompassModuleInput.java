package io.input;

import java.io.IOException;
import java.util.ArrayList;

import network.messages.CompassMessage;
import network.messages.InformationRequest;
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
	private final static int CTRL_REG2_RAW = 0x00;//0x20;
	private final static int CTRL_REG2_MAG_RST = 0x00;

	/*
	 * Other variables
	 */
	private final static int CALIBRATION_TIME = 20*1000;
	private final static int I2C_DEVICE_UPDATE_DELAY = 15;

	private I2CDevice mag3110;
	private boolean available = false;
	private boolean deviceActiveMode = true;
	private int headingInDegrees = 0;
	
	private long startTime = System.currentTimeMillis();

	private ArrayList<int[]> calibrationValues = new ArrayList<int[]>(); 
	
	private boolean calibrationStatus = false;
	
	private int yCenter = 0;
	private int xCenter = 0;
	private double theta = 0;
	private double scaleX = 0;
	private double scaleY = 0;
	
	public I2CCompassModuleInput(I2CBus i2cBus) {

		try {
			// Get device instance
			mag3110 = i2cBus.getDevice(ADDR);

			// Get device instance
			mag3110 = i2cBus.getDevice(ADDR);
			//System.out.println("Connection to magnetic sensor established!");

			// Write bits in CTRL_REG2 (set reset and data types)
			mag3110.write((byte) 0x11, (byte) (CTRL_REG2_AUTO_MRST_EN
					| CTRL_REG2_MAG_RST | CTRL_REG2_RAW));

			Thread.sleep(15);

			// Write bits in CTRL_REG1 (set output rate and over sample ratio)
			mag3110.write((byte) 0x10, (byte) (0x01 /*| CTRL_REG1*/));

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
		if(request instanceof InformationRequest && ((InformationRequest)request).getMessageTypeQuery().equals(InformationRequest.MessageType.COMPASS)){
			if (!available) {
				return new SystemStatusMessage("[CompassModule] Unable to send Compass data");
			}
			return new CompassMessage(getHeadingInDegrees());
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
			
			short xout = (short)((xl | (xh << 8)) & 0xFFFF) ; // concatenate the MSB and LSB
			
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

			short yout = (short)((yl | (yh << 8)) & 0xFFFF); // concatenate the MSB and LSB
			
			return yout;
		} else {
			return -1;
		}
	}
	
	private int getByte(byte address) throws IOException {
		return mag3110.read(address);
	}

	private short readZ() throws IOException, InterruptedException {
		if (deviceActiveMode) {
			int zl, zh; // define the MSB and LSB

			zh = getByte((byte) 0x05); // z MSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			zl = getByte((byte) 0x06); // z LSB reg
			Thread.sleep(2); // needs at least 1.3us free time between start &
								// stop

			short zout = (short)((zl | (zh << 8)) & 0xFFFF); // concatenate the MSB and LSB
			
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
		while (true) {
			try {
				short[] rawAxisReadings = new short[3];

				rawAxisReadings[0] = readX();
				rawAxisReadings[1] = readY();
				rawAxisReadings[2] = readZ();
				
				if(System.currentTimeMillis() - startTime < CALIBRATION_TIME) {
					
					if(!calibrationStatus) {
						System.out.println("Calibration started!");
						calibrationStatus = true;
					}
						
					
					int[] vals = new int[3];
					vals[0] = rawAxisReadings[0];
					vals[1] = rawAxisReadings[1];
					vals[2] = rawAxisReadings[2];
					
					System.out.println(vals[0]+" "+vals[1]+" "+vals[2]);
					
					calibrationValues.add(vals);
					
				} else {
					if(calibrationStatus) {
						System.out.println("Calibration ended!");
						calibrationStatus = false;
						
						//Translation
						int xMin = Integer.MAX_VALUE;
						int xMax = -Integer.MAX_VALUE;
						int yMin = Integer.MAX_VALUE;
						int yMax = -Integer.MAX_VALUE;
						
						for(int[] data : calibrationValues) {
							xMin = Math.min(data[0],xMin);
							xMax = Math.max(data[0],xMax);
							yMin = Math.min(data[1],yMin);
							yMax = Math.max(data[1],yMax);
						}
						
						xCenter = (xMax + xMin)/2;
						yCenter = (yMax + yMin)/2;
						
						System.out.println("xCenter "+xCenter+" yCenter "+yCenter);
						
						//Rotation
						int maxVector = -Integer.MAX_VALUE;
						int maxVectorX = -Integer.MAX_VALUE;
						int maxVectorY = -Integer.MAX_VALUE;
						
						for(int[] data : calibrationValues) {
							int vector = (int)(Math.pow(data[0]-xCenter, 2) + Math.pow(data[1]-yCenter, 2));
							if(vector > maxVector) {
								maxVector = vector;
								maxVectorX = data[0];
								maxVectorY = data[1];
							}
						}
						
						theta = Math.atan2(-maxVectorY, maxVectorX);
						
						System.out.println("maxVectorX "+maxVectorX+" maxVectorY "+maxVectorY);
						System.out.println("theta "+theta);
						
						int xMaxRotatedTranslated = -Integer.MAX_VALUE;
						int yMaxRotatedTranslated = -Integer.MAX_VALUE;
						
						for(int[] data : calibrationValues) {
							int x = (int)((data[0]-xCenter) * Math.cos(theta) - (data[1]-yCenter) * Math.sin(theta));
							int y = (int)((data[0]-xCenter) * Math.sin(theta) + (data[1]-yCenter) * Math.cos(theta));
							xMaxRotatedTranslated = Math.max(x,xMaxRotatedTranslated);
							yMaxRotatedTranslated = Math.max(y,yMaxRotatedTranslated);
						}
						
						System.out.println("xMaxRotatedTranslated "+xMaxRotatedTranslated+" yMaxRotatedTranslated "+yMaxRotatedTranslated);
						
						scaleX = 1.0/xMaxRotatedTranslated;
						scaleY = 1.0/yMaxRotatedTranslated;
						
						System.out.println("scaleX "+scaleX+" scaleY "+scaleY);
					}
				}
				
				double heading = Math.atan2(-(rawAxisReadings[1] - yCenter)*scaleY, (rawAxisReadings[0] - xCenter)*scaleX);
				
				//Value for Lisbon is -2º (0.034906585 rad). Find more here: http://www.magnetic-declination.com
				double declinationAngle = 0.034906585;
				heading += declinationAngle;
				
				heading = 2*Math.PI-heading;
				
				heading+=Math.PI/2;

				if(heading < 0) {
					heading += 2*Math.PI;
				}
				
				if(heading > 2*Math.PI) {
					heading -= 2*Math.PI;
				}
				
				  // Convert radians to degrees for readability.
				this.headingInDegrees = (int)(heading * 180/Math.PI);
				
				/*
				short middleX = (short)((max[0] + min[0])/2);
				short middleY = (short)((max[1] + min[1])/2);
				
				double scaleX = 1.0/(max[0]-min[0]);
				double scaleY = 1.0/(max[1]-min[1]);
				
				double heading = Math.atan2(-(rawAxisReadings[1] - middleY)*scaleY, (rawAxisReadings[0] - middleX)*scaleX);
				
				//Value for Lisbon is -2�� (0.034906585 rad). Find more here: http://www.magnetic-declination.com
				double declinationAngle = 0.034906585;
				heading += declinationAngle;
				
				heading = 2*Math.PI-heading;
				
				heading+=Math.PI/2;

				if(heading < 0) {
					heading += 2*Math.PI;
				}
				
				if(heading > 2*Math.PI) {
					heading -= 2*Math.PI;
				}
				
				  // Convert radians to degrees for readability.
				this.headingInDegrees = (int)(heading * 180/Math.PI);
				*/
				
			} catch (IOException | InterruptedException e) {
				try {
					//after a PI4J exception, the next reading is usually crap. Get rid of it right away
					getByte((byte)0x01);Thread.sleep(2);
					getByte((byte)0x02);Thread.sleep(2);
					getByte((byte)0x03);Thread.sleep(2);
					getByte((byte)0x04);Thread.sleep(2);
					getByte((byte)0x05);Thread.sleep(2);
					getByte((byte)0x06);Thread.sleep(2);
				}catch(Exception ex){
					e.printStackTrace();
				}
				e.printStackTrace();
			}

			try {
				Thread.sleep(I2C_DEVICE_UPDATE_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getHeadingInDegrees() {
		return headingInDegrees;
	}
}
