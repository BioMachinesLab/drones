package network;

import java.io.IOException;
import java.net.InetAddress;

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

}