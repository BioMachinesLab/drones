package network;

import java.io.IOException;
import java.net.Socket;
import main.Controller;

public class MotorConnectionListener extends ConnectionListener {
	
	private static int MOTOR_PORT = 10102;

	public MotorConnectionListener(Controller controller) throws IOException{
		super(controller, MOTOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new MotorConnectionHandler(s, controller, this);
		addConnection(conn);
		conn.start();
	}
}
