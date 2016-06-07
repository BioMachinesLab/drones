import java.io.IOException;

import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

/**
 * This example code demonstrates how to perform serial communications using the
 * Raspberry Pi.
 * 
 * @author Robert Savage
 */
public class SerialReadingExample {
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
		System.out.println(" ... connect using settings: 38400, 8, N, 1.");
		System.out
				.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serial = SerialFactory.createInstance();

		// create and register the serial data listener
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					System.out.print("[ASCII DATA] " + event.getAsciiString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		try {
			// open the default serial port provided on the GPIO header
			serial.open(Serial.DEFAULT_COM_PORT, Baud._9600, DataBits._8,
					Parity.NONE, StopBits._1, FlowControl.NONE);

			// continuous loop to keep the program running until the user
			// terminates the program
			while (true) {
				// wait 1 second before continuing
				Thread.sleep(1000);
			}

		} catch (IOException ex) {
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}