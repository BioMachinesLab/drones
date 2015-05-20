package commoninterface.network;

import java.io.IOException;
import java.net.Socket;

import commoninterface.RobotCI;

public class MotorConnectionListener extends ConnectionListener {
	
	private static int MOTOR_PORT = 10102;

	public MotorConnectionListener(RobotCI robot) throws IOException{
		super(robot, MOTOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new MotorConnectionHandler(s, robot, this);
		addConnection(conn);
		conn.start();
	}
}
