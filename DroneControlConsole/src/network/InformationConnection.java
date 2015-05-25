package network;

import java.io.IOException;
import java.net.InetAddress;

import main.RobotControlConsole;

public class InformationConnection extends DroneConnection {

	public static int INFORMATION_PORT = 10101;

	public InformationConnection(RobotControlConsole console, InetAddress destHost)
			throws IOException {
		super(console, destHost, INFORMATION_PORT);
	}

	public InformationConnection(RobotControlConsole console, InetAddress destHost, int destPort)
			throws IOException {
		super(console, destHost, destPort);
	}

}