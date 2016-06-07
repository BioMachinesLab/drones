import java.io.IOException;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

public class SerialReaderWriter {
	private final static int BAUD_RATE = 9600;
	private final static String COM_PORT = Serial.DEFAULT_COM_PORT;

	private Serial serial; // Serial connection

	public SerialReaderWriter() throws InterruptedException {
		serial = SerialFactory.createInstance();

		serial.addListener(new SerialDataEventListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					System.out.print(event.getAsciiString());
				} catch (IOException e) {
					System.err
							.println("Error while reading the serial stream as ASCII String ("
									+ e.getMessage() + ")");
				}
			}
		});

		System.out.println("Initializing GPS!");
		try {
			serial.open(COM_PORT, BAUD_RATE);
		} catch (IOException e) {
			System.err.println("Error while opening serial stream ("
					+ e.getMessage() + ")");
		}

		Thread.sleep(5000);

		System.out.println("[Writting....]");
		try {
			serial.write("$PMTK251,57600*2C\r\n");
		} catch (IOException e) {
			System.err.println("Error while writing to serial stream ("
					+ e.getMessage() + ")");
		}

		Thread.sleep(5000);
	}

	public static void main(String[] args) throws InterruptedException {
		new SerialReaderWriter();
	}
}
