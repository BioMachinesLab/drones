package network;

import java.io.IOException;
import java.net.InetAddress;

import main.RobotControlConsole;

public class MotorConnection extends DroneConnection {

	private static int MOTOR_PORT = 10102;

	public MotorConnection(RobotControlConsole console, InetAddress destHost) throws IOException {
		super(console, destHost, MOTOR_PORT);
	}

	public MotorConnection(RobotControlConsole console, InetAddress destHost, int destPort)
			throws IOException {
		super(console, destHost, destPort);
	}
}