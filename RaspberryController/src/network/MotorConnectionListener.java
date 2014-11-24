package network;

import java.io.IOException;
import java.net.Socket;
import commoninterfaceimpl.RealAquaticDroneCI;

public class MotorConnectionListener extends ConnectionListener {
	
	private static int MOTOR_PORT = 10102;

	public MotorConnectionListener(RealAquaticDroneCI drone) throws IOException{
		super(drone, MOTOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new MotorConnectionHandler(s, drone, this);
		addConnection(conn);
		conn.start();
	}
}
