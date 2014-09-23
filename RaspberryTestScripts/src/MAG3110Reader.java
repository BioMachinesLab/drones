import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class MAG3110Reader extends Thread {
	private final static int ADDR = 0x0E; // 7-bit address for the MAG3110,
											// doesn't change
	private I2CBus i2c_bus_1;
	private I2CDevice mag3110;

	public MAG3110Reader() {
		try {
			System.out.println("Starting sensors reading:");

			i2c_bus_1 = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connected to bus OK!");

			// get device itself
			mag3110 = i2c_bus_1.getDevice(ADDR);
			System.out.println("Connection to magnetic sensor established!");

			// Configure Device
			mag3110.write((byte) 0x11); // cntrl register2
			mag3110.write((byte) 0x80); // send 0x80, enable auto resets
			Thread.sleep(15);
			mag3110.write((byte) 0x10); // cntrl register1
			mag3110.write((byte) 0x01); // send 0x01, active mode

			System.out.println("Configuring Device OK!");

			System.out.println("Configuring sensors OK!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private synchronized int readX() throws IOException, InterruptedException {
		int xl, xh; // define the MSB and LSB

		mag3110.write((byte) 0x01); // x MSB reg
		Thread.sleep(2); // needs at least 1.3us free time between start and
							// stop
		xh = mag3110.read(1);
		Thread.sleep(2); // needs at least 1.3us free time between start andstop

		mag3110.write((byte) 0x02); // x LSB reg
		Thread.sleep(2); // needs at least 1.3us free time between start andstop
		xl = mag3110.read(1);

		int xout = (xl | (xh << 8)); // concatenate the MSB and LSB
		return xout;
	}

	private synchronized int readY() throws IOException, InterruptedException {
		int yl, yh; // define the MSB and LSB

		mag3110.write((byte) 0x03); // y MSB reg
		Thread.sleep(2); // needs at least 1.3us free time between start and
							// stop
		yh = mag3110.read(1);
		Thread.sleep(2); // needs at least 1.3us free time between start and
							// stop

		mag3110.write((byte) 0x04); // y LSB reg
		Thread.sleep(2); // needs at least 1.3us free time between start andstop
		yl = mag3110.read(1);

		int yout = (yl | (yh << 8)); // concatenate the MSB and LSB
		return yout;
	}

	private synchronized int readZ() throws IOException, InterruptedException {
		int zl, zh; // define the MSB and LSB

		mag3110.write((byte) 0x05);
		Thread.sleep(2); // needs at least 1.3us free time between start and
							// stop
		zh = mag3110.read(1);
		Thread.sleep(2); // needs at least 1.3us free time between start and
							// stop

		mag3110.write((byte) 0x06);// z LSB reg
		Thread.sleep(2); // needs at least 1.3us free time between start andstop
		zl = mag3110.read(1);

		int zout = (zl | (zh << 8)); // concatenate the MSB and LSB
		return zout;
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.print("X: " + readX());
				System.out.print("Y: " + readY());
				System.out.print("Z: " + readZ());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					i2c_bus_1.close();
				} catch (IOException e) {
					System.err.println("Error closing I2C bus");
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		new MAG3110Reader().start();
	}
}
