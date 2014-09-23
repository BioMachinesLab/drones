import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class SerialReaderWriter {
	private final static int BAUD_RATE = 9600;
	private final static String COM_PORT = Serial.DEFAULT_COM_PORT;

	private Serial serial; // Serial connection

	public SerialReaderWriter() throws InterruptedException {
		serial = SerialFactory.createInstance();

		serial.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				System.out.print(event.getData());
			}
		});

		System.out.println("Initializing GPS!");
		serial.open(COM_PORT, BAUD_RATE);

		Thread.sleep(5000);

		System.out.println("[Writting....]");
		serial.write("$PMTK251,57600*2C\r\n");

		Thread.sleep(5000);
	}

	public static void main(String[] args) throws InterruptedException {
		new SerialReaderWriter();
	}
}
