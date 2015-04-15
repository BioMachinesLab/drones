package network;

import java.io.IOException;
import java.net.Socket;

import commoninterface.RealRobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;

public class MotorConnectionListener extends ConnectionListener {
	
	private static int MOTOR_PORT = 10102;

	public MotorConnectionListener(RealRobotCI drone) throws IOException{
		super(drone, MOTOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new MotorConnectionHandler(s, robot, this);
		addConnection(conn);
		conn.start();
	}
}
