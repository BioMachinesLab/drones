package network.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import commoninterface.RobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;

public class ServerConnectionListener extends ConnectionListener {
	private static int SERVER_PORT = 10110;
	private AtomicBoolean enable = new AtomicBoolean();

	public ServerConnectionListener(RobotCI controller) throws IOException {
		super(controller, SERVER_PORT);
	}

	@Override
	protected void createHandler(Socket s) {
		ConnectionHandler conn = new ServerConnectionHandler(s, robot, this);
		addConnection(conn);
		conn.start();
	}

	@Override
	public void run() {
		try {
			System.out
					.println("[SERVER CONNECTION LISTENER] Connection Handler Initialized on "
							+ InetAddress.getLocalHost().getHostAddress() + ":" + port);
			System.out
					.println("[SERVER CONNECTION LISTENER] Waiting for connection requests!");
			while (enable.get()) {
				Socket socket = serverSocket.accept();
				createHandler(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out
						.println("[SERVER CONNECTION LISTENER] Unable to close server socket.... there was an open socket?");
			}
		}
	}
	
	@Override
	public synchronized void start() {
		enable.set(true);
		super.start();
	};
	
	@Override
	public void shutdown() {
		enable.set(false);
		super.shutdown();
	}
}
