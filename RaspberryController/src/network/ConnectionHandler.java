package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import main.Controller;

public class ConnectionHandler {
	private static final int SOCKET_PORT = 10101;
	private ArrayList<Connection> connections = new ArrayList<>();
	private Controller controller;

	public ConnectionHandler(Controller controller) {
		this.controller = controller;
	}

	public void initConnector() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(SOCKET_PORT);

			try {
				System.out.println("Connection Handler Initialized on "
						+ InetAddress.getLocalHost().getHostAddress() + ":"
						+ SOCKET_PORT);
				System.out.println("Waiting for connection requests!");
				while (true) {
					Socket socket = serverSocket.accept();
					Connection conn = new Connection(socket, controller, this);
					connections.add(conn);
					conn.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out
							.println("Unable to close server socket.... there was an open socket?");
				}
			}
		} catch (IOException e) {
			System.out.println("Unable to init socket!");
		}
	}

	public void closeConnections() {
		if (!connections.isEmpty()) {
			System.out.println("Closing Connections!");
			for (Connection conn : connections) {
				if (!conn.getSocket().isClosed())
					conn.closeConnection();
			}
		}
	}

	public void removeConnection(Connection conn) {
		connections.remove(conn);
	}
}
