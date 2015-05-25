import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MAG3110Reader extends Thread {
	private final static int ADDR = 0x0E; // 7-bit address for the MAG3110

	private final static int CTRL_REG1_DR = 0x00;
	private final static int CTRL_REG1_OS = 0x18;
	private final static int CTRL_REG1_STANDBY = (CTRL_REG1_DR | CTRL_REG1_OS);

	private final static int CTRL_REG2_AUTO_MRST_EN = 0x80;
	private final static int CTRL_REG2_RAW = 0x00;
	private final static int CTRL_REG2_MAG_RST = 0x00;

	private I2CBus i2c_bus_1;
	private I2CDevice mag3110;
	private boolean enable = false;

	public MAG3110Reader() {
		try {
			System.out.println("Starting sensors reading:");

			// Get I2C instance
			i2c_bus_1 = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus OK!");

			// Get device instance
			mag3110 = i2c_bus_1.getDevice(ADDR);
			System.out.println("Connection to magnetic sensor established!");

			// Write bits in CTRL_REG2 (set reset and data types)
			mag3110.write((byte) 0x11, (byte) (CTRL_REG2_AUTO_MRST_EN
					| CTRL_REG2_MAG_RST | CTRL_REG2_RAW));

			Thread.sleep(15);

			// Write bits in CTRL_REG1 (set output rate and over sample ratio)
			mag3110.write((byte) 0x10,
					(byte) (0x01 | CTRL_REG1_DR | CTRL_REG1_OS));

			System.out.println("Sensor configuration OK!");
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int readX() throws IOException, InterruptedException {
		if (enable) {
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

	public int readY() throws IOException, InterruptedException {
		if (enable) {
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

	public int readZ() throws IOException, InterruptedException {
		if (enable) {

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

	// private int readTemperature() throws IOException, InterruptedException {
	// int temperature = mag3110.read((byte) 0x0F); // DIE_TEMP reg
	// Thread.sleep(2); // needs at least 1.3us free time between start & stop
	//
	// long teste = Long.parseLong(Integer.toBinaryString(temperature), 2);
	//
	// System.out.println("Teste: " + teste);
	//
	// return temperature;
	// }

	public void closeBus() {
		try {
			i2c_bus_1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void standBy() {
		if (enable) {
			try {
				mag3110.write((byte) 0x10, (byte) CTRL_REG1_STANDBY);
				Thread.sleep(2); // needs at least 1.3us free time between start
									// &
									// stop
				enable = false;
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void wakeUp() {
		if (!enable) {
			try {
				mag3110.write((byte) 0x10, (byte) (CTRL_REG1_STANDBY | 0x01));
				Thread.sleep(2); // needs at least 1.3us free time between start
									// &
									// stop
				enable = true;
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("X: " + readX() + "   Y: " + readY()
						+ "   Z: " + readZ());
				Thread.sleep(15);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeBus();
		}
	}

	public static void main(String[] args) {
		new MAG3110Reader().start();
	}
}
