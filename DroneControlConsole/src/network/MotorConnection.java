package network;

import gui.GUI;

import java.io.IOException;
import java.net.InetAddress;

import network.messages.Message;

public class MotorConnection extends DroneConnection {
	
	private static int MOTOR_PORT = 10102;

	public MotorConnection(GUI gui, InetAddress destHost) throws IOException {
		super(gui, destHost, MOTOR_PORT);
	}
	
}
