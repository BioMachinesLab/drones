package network;

import java.io.IOException;
import java.net.Socket;

import commoninterfaceimpl.RealAquaticDroneCI;

public class BehaviorReceiver extends ConnectionListener {
	
	private static int BEHAVIOR_PORT = 10103;
	
	public BehaviorReceiver(RealAquaticDroneCI drone) throws IOException {
		super(drone, BEHAVIOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new MotorConnectionHandler(s, drone, this);
		addConnection(conn);
		conn.start();
	}
	
	public void closeConnections() {
		if (!connections.isEmpty()) {
			System.out.println("[CONNECTION HANDLER] Closing Connections!");
			for (ConnectionHandler conn : connections) {
				if (!conn.getSocket().isClosed())
					conn.closeConnectionWthoutDiscardConnListener();
			}
		}
	}

}
