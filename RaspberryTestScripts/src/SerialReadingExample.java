import java.io.IOException;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

/**
 * This example code demonstrates how to perform serial communications using the
 * Raspberry Pi.
 * 
 * @author Robert Savage
 */
public class SerialReadingExample {
	private static boolean printed = false;

	public static void main(String args[]) throws InterruptedException {

		// !! ATTENTION !!
		// By default, the serial port is configured as a console port
		// for interacting with the Linux OS shell. If you want to use
		// the serial port in a software program, you must disable the
		// OS from using this port. Please see this blog article by
		// Clayton Smith for step-by-step instructions on how to disable
		// the OS console for this port:
		// http://www.irrational.net/2012/04/19/using-the-raspberry-pis-serial-port/

		System.out
				.println("<--Pi4J--> Serial Communication Example ... started.");
		System.out.println(" ... connect using settings: 9600, N, 8, 1.");
		System.out
				.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(new SerialDataEventListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					// print out the data received to the console
					if (!printed) {
						System.out.print(event.getAsciiString());
						printed = true;
					}
				} catch (IOException e) {
					System.err
							.println("Error while reading the serial stream as ASCII String ("
									+ e.getMessage() + ")");
				}
			}
		});

		try {
			// open the default serial port provided on the GPIO header
			serial.open(Serial.DEFAULT_COM_PORT, 9600);

		} catch (IOException ex) {
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}