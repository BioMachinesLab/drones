package commoninterface.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import commoninterface.RobotCI;

public class ConnectionListener extends Thread {

	private static final int DEFAULT_PORT = 10101;
	protected ArrayList<ConnectionHandler> connections = new ArrayList<>();
	protected RobotCI robot;
	protected int port;
	protected ServerSocket serverSocket = null;

	public ConnectionListener(RobotCI controller) throws IOException {
		this(controller, DEFAULT_PORT);
	}

	public ConnectionListener(RobotCI robot, int port) throws IOException {
		this.robot = robot;
		this.port = port;

		serverSocket = new ServerSocket(port);
	}

	@Override
	public void run() {

		try {
			if (System.getProperty("os.name").equals("Linux")
					&& System.getProperty("os.arch").equals("arm")) {
				System.out
						.println("[CONNECTION HANDLER] Connection Handler Initialized on "
								+ NetworkUtils.getAddress("wlan0") + ":" + port);
			} else {
				System.out
				.println("[CONNECTION HANDLER] Connection Handler Initialized on "
						+ InetAddress.getLocalHost().getHostAddress() + ":" + port);
			}
			
			System.out
					.println("[CONNECTION HANDLER] Waiting for connection requests!");
			while (true) {
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
						.println("[CONNECTION HANDLER] Unable to close server socket.... there was an open socket?");
			}
		}
	}

	protected void addConnection(ConnectionHandler conn) {
		connections.add(conn);
	}

	protected void createHandler(Socket s) {
		ConnectionHandler conn = new ConnectionHandler(s, robot, this);
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

	public void shutdown() {
		closeConnections();
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to close server socket.... there was an open socket?");
		}
	}

	public synchronized void removeConnection(ConnectionHandler conn) {
		connections.remove(conn);
	}

	public ArrayList<ConnectionHandler> getConnections() {
		return connections;
	}
}
