package network;

import gui.GUI;
import java.io.IOException;
import java.net.InetAddress;

public class MotorConnection extends DroneConnection {

	private static int MOTOR_PORT = 10102;

	public MotorConnection(GUI gui, InetAddress destHost) throws IOException {
		super(gui, destHost, MOTOR_PORT);
	}

	public MotorConnection(GUI gui, InetAddress destHost, int destPort)
			throws IOException {
		super(gui, destHost, destPort);
	}

}
