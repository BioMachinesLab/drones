package network.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import main.DroneControlConsole;

public class ServerConnectionListener implements ServerObservated {
	public static int SERVER_PORT = 10110;

	protected ArrayList<ServerConnectionHandler> connections = new ArrayList<ServerConnectionHandler>();
	protected int port;
	protected ServerSocket serverSocket = null;
	private AtomicBoolean enable = new AtomicBoolean();
	private DroneControlConsole console;
	private ServerObserver observer;
	private MobileApplicationServer server;

	public ServerConnectionListener(DroneControlConsole console) {
		this.console = console;
	}

	protected synchronized void addConnection(ServerConnectionHandler conn) {
		connections.add(conn);
		observer.updateStatus();
	}

	public void closeConnections() {
		if (!connections.isEmpty()) {
			System.out
					.println("[SERVER CONNECTION LISTENER] Closing Connections!");
			for (ServerConnectionHandler conn : connections) {
				if (!conn.getSocket().isClosed())
					conn.closeConnectionWhitoutRemove();
			}
		}
	}

	public void stopServer() {
		if (server != null) {
			enable.set(false);

			closeConnections();
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out
						.println("[SERVER CONNECTION LISTENER] Unable to close server socket.... there was an open socket?");
			}

			observer.setOfflineServer();
			server = null;
			connections.clear();
		}
	}

	public synchronized void removeConnection(ServerConnectionHandler conn) {
		connections.remove(conn);
		observer.updateStatus();
	}

	public ArrayList<ServerConnectionHandler> getConnections() {
		return connections;
	}

	public void startServer() {
		startServer(SERVER_PORT);
	}

	public void startServer(int port) {
		if (server == null) {
			try {
				this.port = port;
				serverSocket = new ServerSocket(port);
				enable.set(true);

				server = new MobileApplicationServer(this);
				server.start();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				observer.setOfflineServer();
			}
		}
	}

	public DroneControlConsole getConsole() {
		return console;
	}

	public void setObserver(ServerObserver observer) {
		this.observer = observer;
	}

	public boolean isRunning() {
		return enable.get();
	}

	public int getPort() {
		return port;
	}

	public int getClientQuantity() {
		return connections.size();
	}

	class MobileApplicationServer extends Thread {
		private ServerConnectionListener serverConnectionListener;

		public MobileApplicationServer(
				ServerConnectionListener serverConnectionListener) {
			this.serverConnectionListener = serverConnectionListener;
		}

		@Override
		public void run() {
			try {
				System.out
						.println("[SERVER CONNECTION LISTENER] Server initialized on "
								+ InetAddress.getLocalHost().getHostAddress()
								+ ":" + port);
				System.out
						.println("[SERVER CONNECTION LISTENER] Waiting for connection requests!");

				observer.setOnlineServer();

				while (enable.get()) {
					Socket socket = serverSocket.accept();
					System.out
							.println("[SERVER CONNECTION LISTENER] New client at "
									+ socket.getInetAddress().getHostAddress());
					createHandler(socket);
					//printConnections();
				}
				System.out.println("Offline!");
				observer.setOfflineServer();
			} catch (IOException e) {
				observer.setOfflineServer();
				if (!e.getMessage().equals("socket closed")) {
					observer.setMessage("Error on server");
//					System.err.println(e.getMessage());
				}
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					System.out
							.println("[SERVER CONNECTION LISTENER] Unable to close server socket.... there was an open socket?");
				}
			}
		}

		protected void createHandler(Socket s) {
			ServerConnectionHandler conn = new ServerConnectionHandler(s,
					serverConnectionListener);
			addConnection(conn);
			conn.start();
		}
	}

	private void printConnections() {
		System.out.println("[SERVER CONNECTION LISTENER]");
		System.out.println("###### Connected clients list init");
		for (ServerConnectionHandler connection : connections) {
			System.out.println(connection.clientName);
		}
		System.out.println("###### Connected clients list end");
	}
}
