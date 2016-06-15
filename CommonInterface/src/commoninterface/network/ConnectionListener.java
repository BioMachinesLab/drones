package commoninterface.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
			if (System.getProperty("os.name").equals("Linux") && System.getProperty("os.arch").equals("arm")) {
				System.out.printf("[%s] Connection Handler Initialized on %s:%d\n",getClass().getName(),
						InetAddress.getLocalHost().getHostAddress(), port);
				System.out.printf("[%s] Connection Handler Initialized on %s:%d\n",getClass().getName(),
						NetworkUtils.getAddress("wlan0"), port);
			} else {
				System.out.printf("[%s] Connection Handler Initialized on %s:%d\n",getClass().getName(),
						InetAddress.getLocalHost().getHostAddress(), port);
			}

			System.out.printf("[%s] Waiting for connection requests!\n", getClass().getName());
			while (true) {
				Socket socket = serverSocket.accept();
				createHandler(socket);
			}
		} catch (IOException e) {
			System.err.printf("[%s] Error: %s\n", getClass().getName(), e.getMessage());
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.printf("[%s] Unable to close server socket.... there was an open socket?\n",
						getClass().getName());
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
			System.out.printf("[%s] Closing Connections!\n", getClass().getName());
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
			System.out.printf("[%s] Unable to close server socket.... there was an open socket?\n", getClass().getName());
		}
	}

	public synchronized void removeConnection(ConnectionHandler conn) {
		connections.remove(conn);
	}

	public ArrayList<ConnectionHandler> getConnections() {
		return connections;
	}
}
