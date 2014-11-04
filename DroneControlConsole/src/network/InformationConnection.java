package network;

import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JOptionPane;
import main.DroneControlConsole;

public class InformationConnection extends DroneConnection {

	private static int INFORMATION_PORT = 10101;

	public InformationConnection(DroneControlConsole console, InetAddress destHost)
			throws IOException {
		super(console, destHost, INFORMATION_PORT);
	}

	public InformationConnection(DroneControlConsole console, InetAddress destHost, int destPort)
			throws IOException {
		super(console, destHost, destPort);
	}

	@Override
	protected void shutdownConnection() {
		JOptionPane.showMessageDialog(console.getGUI(),
				"Connection to drone was lost!");
		System.exit(0);
		// TODO fix this! when this happens, the old threads are still in the
		// background sending stuff
		console.connect();
	}

}
