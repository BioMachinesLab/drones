package network;

import java.io.IOException;
import java.net.Socket;

import commoninterfaceimpl.RealRobotCI;

public class CommandConnectionListener extends ConnectionListener {
	
	private static int BEHAVIOR_PORT = 10103;
	
	public CommandConnectionListener(RealRobotCI drone) throws IOException {
		super(drone, BEHAVIOR_PORT);
	}
	
	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new CommandConnectionHandler(s, robot, this);
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
