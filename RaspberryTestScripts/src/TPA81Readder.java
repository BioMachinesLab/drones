import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class TPA81Readder {
	private final static int ADDR = 0x68;
	private I2CBus i2c_bus_1;
	private I2CDevice tpa81;

	public TPA81Readder() {
		try {
			System.out.println("Starting sensors reading:");

			i2c_bus_1 = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus OK!");

			// get device itself
			tpa81 = i2c_bus_1.getDevice(ADDR);
			System.out.println("Connected to battery sensor OK!");

			// start sensing, using config registries 6B and 6C
			// battSensor.write(0x6B, (byte) 0b00000000);
			// battSensor.write(0x6C, (byte) 0b00000000);
			System.out.println("Configuring Device OK!");

			System.out.println("Configuring sensors OK!");
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void readTemperatures() {
		try {
			// int[] temperatures = new int[10];

			byte[] readBuf = new byte[1];
			// tpa81.write(0x00, (byte) );
			int res = tpa81.read(0x00, readBuf, 0, 1);
			System.out.println("ReadBuf: " + readBuf[0] + "< RES:" + res);

			res = tpa81.read(0x01, readBuf, 0, 1);
			System.out.println("ReadBuf: " + readBuf[1] + "< RES:" + res);

			// for (int i = 0; i < 10; i++) {
			// tpa81.write((byte) i);
			// temperatures[i] = tpa81.read();
			// System.out.println("Readed "+i+": "+temperatures[i]);
			// }
			//
			// for (int i = 0; i < temperatures.length; i++) {
			// switch (i) {
			// case 0:
			// System.out.println("Software version: " + temperatures[i]);
			// break;
			// case 1:
			// System.out.println("Ambient temperature: "
			// + temperatures[i]);
			// break;
			// default:
			// System.out.println("Temperature " + i + ": "
			// + temperatures[i]);
			// break;
			// }
			// }
		} catch (IOException e) {
			System.err.println("Unable to read temperatures");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TPA81Readder tpa81Readder = new TPA81Readder();

		// while (true) {
		tpa81Readder.readTemperatures();
		// }
	}
}

//
// public void startReading() {
// Task task = new Task<Void>() {
// @Override
// public Void call() {
// try {
// readingSensors();
// } catch (IOException e) {
// }
// return null;
// }
// };
// new Thread(task).start();
// }
//
// private void readingSensors() throws IOException {
// bytes = new byte[6 + 2 + 6];
// DataInputStream gyroIn;
// short accelX, accelY, accelZ;
// float tempX, tempY, tempZ;
// short temp;
// short gyroX, gyroY, gyroZ;
//
// while (true) {
// int r = device.read(0x3B, bytes, 0, bytes.length);
//
// if (r != 14) { //14 registries to be read, 6 for gyro, 6 for accel and 2 for
// temp
// System.out.println("Error reading data, < " + bytes.length + " bytes");
// }
// gyroIn = new DataInputStream(new ByteArrayInputStream(bytes));
// accelX = gyroIn.readShort();
// accelY = gyroIn.readShort();
// accelZ = gyroIn.readShort();
// temp = gyroIn.readShort();
// gyroX = gyroIn.readShort();
// gyroY = gyroIn.readShort();
// gyroZ = gyroIn.readShort();
//
// tempX = (float) accelX / SENSITIVITY;
// //Anything higher than 1 or lower than -1 is ignored
// accelX_G = (tempX > 1) ? 1 : ((tempX < -1) ? -1 : tempX);
// tempY = (float) accelY / SENSITIVITY;
// //Anything higher than 1, or lower than 01 is ignored
// accelY_G = (tempY > 1) ? 1 : ((tempY < -1) ? -1 : tempY);
// tempZ = ((float) accelZ / SENSITIVITY) * (-1); //sensor upsidedown, opposite
// value used
// accelZ_G = (tempZ > 1) ? 1 : ((tempZ < -1) ? -1 : tempZ);
//
// //use accel data as desired...
//
// gyroXdeg = gyroX * (2000d / (double) Short.MAX_VALUE);
// gyroYdeg = gyroY * (2000d / (double) Short.MAX_VALUE);
// gyroZdeg = gyroZ * (2000d / (double) Short.MAX_VALUE);
//
// //Use the gyro values as desired..
//
// double tempC = ((double) temp / 340d) + 35d;
//
// try {
// Thread.sleep(700);
// } catch (InterruptedException ex) {
// Logger.getLogger(Sensors.class.getName()).log(Level.SEVERE, null, ex);
// }
// }
// }