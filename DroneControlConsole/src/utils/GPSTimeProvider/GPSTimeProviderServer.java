package utils.GPSTimeProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import commoninterface.dataobjects.GPSData;

public class GPSTimeProviderServer implements GPSTimeProviderServerObservated {
	public final static int SERVER_PORT = 9190;

	private ArrayList<GPSTimeProviderServerConnectionHandler> connections = new ArrayList<GPSTimeProviderServerConnectionHandler>();
	private ServerSocket serverSocket = null;
	private AtomicBoolean enable = new AtomicBoolean();
	private GPSTimeProviderServerObserver observer;
	private ServerConnectionListener server = null;
	private int serverPort;

	private GPSModuleInput gpsModuleInput;

	public GPSTimeProviderServer() {
		observer = new GPSTimeProviderServerGUI(this);
	}

	@Override
	public void startServer() {
		startServer(SERVER_PORT);
	}

	@Override
	public void startServer(int port) {
		if (server == null) {
			try {
				serverPort = port;
				serverSocket = new ServerSocket(port);
				enable.set(true);

				server = new ServerConnectionListener(this);
				server.start();
			} catch (IOException e) {
				observer.setErrorMessage("Error initializing: " + e.getMessage());
				observer.setOfflineServer();
			}
		} else {
			observer.setErrorMessage("Server already online!");
		}
	}

	@Override
	public void stopServer() {
		if (server != null) {
			enable.set(false);

			closeConnections();
			try {
				serverSocket.close();
			} catch (IOException e) {
				observer.setErrorMessage("Unable to close server socket.... was there an open socket?");
			}

			observer.setOfflineServer();
			server = null;
			connections.clear();

			// TODO kill gpsModule
		} else {
			observer.setErrorMessage("Server already offline!");
		}
	}

	@Override
	public void startGPSModule() {
		startGPSModule(null);
	}

	@Override
	public void startGPSModule(String port) {
		if (gpsModuleInput == null || (gpsModuleInput != null && !gpsModuleInput.isAvailable())) {
			gpsModuleInput = new GPSModuleInput();
			try {
				if (port == null) {
					observer.setMessage("No serial port specified. Using default one");
					gpsModuleInput.init();
				} else {
					gpsModuleInput.init(port);
				}

				if (gpsModuleInput.isAvailable()) {
					observer.setOnlineGPSModule();
				} else {
					observer.setErrorMessage("Error initializing GPS module!");
				}
			} catch (UnsatisfiedLinkError e) {
				observer.setErrorMessage("GPS Module - Error satisfying link -> " + e.getMessage());
			} catch (IOException e) {
				observer.setErrorMessage("GPS Module Error -> " + e.getMessage());
			}
		} else {
			observer.setErrorMessage("GPS module already online!");
		}
	}

	@Override
	public void stopGPSModule() {
		if (gpsModuleInput != null && gpsModuleInput.isAvailable()) {
			gpsModuleInput.stopService();
			gpsModuleInput = null;
		} else {
			observer.setErrorMessage("GPS module already offline!");
		}
	}

	private class ServerConnectionListener extends Thread {
		private GPSTimeProviderServer server;

		public ServerConnectionListener(GPSTimeProviderServer server) {
			this.server = server;
		}

		@Override
		public void run() {
			try {
				observer.setMessage(
						"Server initialized on " + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort);
				observer.setMessage("Waiting for connection requests!");
				observer.setOnlineServer();

				while (enable.get()) {
					Socket socket = serverSocket.accept();
					observer.setMessage("New client at " + socket.getInetAddress().getHostAddress());

					// Create a connection handler
					GPSTimeProviderServerConnectionHandler conn = new GPSTimeProviderServerConnectionHandler(socket,
							server);
					addConnection(conn);
					conn.start();
				}
				observer.setMessage("Shutting down server...");
				observer.setOfflineServer();
			} catch (IOException e) {
				observer.setOfflineServer();
				if (!e.getMessage().equals("socket closed")) {
					observer.setErrorMessage("Error on server -> " + e.getMessage());
				}
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					observer.setErrorMessage("Unable to close server socket.... was there an open socket?");
				}
			}
		}
	}

	/*
	 * Connection management
	 */
	protected synchronized void addConnection(GPSTimeProviderServerConnectionHandler conn) {
		connections.add(conn);
		observer.updateStatus();
	}

	public synchronized void removeConnection(GPSTimeProviderServerConnectionHandler conn) {
		connections.remove(conn);
		observer.updateStatus();
	}

	public void closeConnections() {
		if (!connections.isEmpty()) {
			System.out.printf("[%s] Closing Connections!\n");
			for (GPSTimeProviderServerConnectionHandler conn : connections) {
				if (!conn.getSocket().isClosed())
					conn.closeConnectionWhitoutRemove();
			}
		}
	}

	/*
	 * Getters and setters
	 */
	@Override
	public boolean isServerRunning() {
		return enable.get();
	}

	@Override
	public int getConnectedClientsQuantity() {
		return connections.size();
	}

	@Override
	public void setObserver(GPSTimeProviderServerObserver observer) {
		this.observer = observer;
	}

	public void setMessage(String message) {
		observer.setMessage(message);
	}

	public void setErrorMessage(String message) {
		observer.setErrorMessage(message);
	}

	@Override
	public GPSData getGPSData() {
		if (gpsModuleInput != null) {
			return gpsModuleInput.getGPSData();
		} else {
			return null;
		}
	}

	@Override
	public String[] getSerialPortIdentifiers() {
		return GPSModuleInput.getSerialPortIdentifiers();
	}

	@Override
	public boolean isGPSModuleRunning() {
		return gpsModuleInput != null && gpsModuleInput.isAvailable();
	}

	@Override
	public int getDefaultPort() {
		return SERVER_PORT;
	}

	public static void main(String[] args) {
		new GPSTimeProviderServer();
	}
}
