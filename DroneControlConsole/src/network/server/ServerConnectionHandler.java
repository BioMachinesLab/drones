package network.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import network.server.messages.DronesInformationRequest;
import network.server.messages.DronesInformationResponse;
import network.server.messages.ServerMessage;
import network.server.messages.ServerMessage.MessageType;
import commoninterface.network.messages.Message;
import dataObjects.DroneData;
import dataObjects.ServerStatusData;

public class ServerConnectionHandler extends Thread {
	private final static boolean DEBUG = false;

	protected Socket socket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected String clientName = null;
	protected ServerConnectionListener connectionListener;

	public ServerConnectionHandler(Socket socket,
			ServerConnectionListener connectionListener) {
		this.socket = socket;
		this.connectionListener = connectionListener;
	}

	@Override
	public void run() {
		try {
			initConnection();

			while (true) {
				Object data = (Message) in.readObject();
				processData(data);
			}
		} catch (IOException e) {
			System.out.println("[SERVER CONNECTION HANDLER] Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") disconnected");
		} catch (ClassNotFoundException e) {
			System.out
					.println("[SERVER CONNECTION HANDLER] I didn't reveived a correct name from "
							+ socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		} finally {
			// always shutdown the handler when something goes wrong
			shutdownHandler();
		}
	}

	private void processData(Object data) throws ClassNotFoundException {
		if (data instanceof ServerMessage) {
			switch (((ServerMessage) data).getMessageType()) {
			case DRONES_INFORMATION_REQUEST:
				DronesInformationResponse response1 = new DronesInformationResponse(
						MessageType.DRONES_INFORMATION_RESPONSE);
				ArrayList<DroneData> dronesIdentification = (connectionListener
						.getConsole().getDronesSet()
						.getDrones(((DronesInformationRequest) data)
								.getDroneIdentification()));
				response1.setDronesData(dronesIdentification);
				// send to client (response1);
				break;

			case CONECTED_DRONE_INFORMATIONS_REQUEST:
				DronesInformationResponse response2 = new DronesInformationResponse(
						MessageType.CONECTED_DRONE_INFORMATIONS_RESPONSE);

				response2.setDronesData(connectionListener.getConsole()
						.getDronesSet().getDronesSet());
				// send to client (response2);
				break;

			case SERVER_INFORMATIONS_REQUEST:
				ServerStatusData serverStatus = new ServerStatusData();
				serverStatus.setAvailableBehaviors(connectionListener
						.getConsole().getGUI().getCommandPanel()
						.getAvailableBehaviors());
				serverStatus.setAvailableControllers(connectionListener
						.getConsole().getGUI().getCommandPanel()
						.getAvailableControllers());
				serverStatus.setConnectedClientsQty(connectionListener
						.getClientQuantity());
				// send to client (serverStatus)
				break;
			default:
				System.out.println("Received message with type: "
						+ ((ServerMessage) data).getMessageType());
				break;
			}
		} else {
			throw new ClassNotFoundException(
					"Unable to process the receied object");
		}
	}

	protected void shutdownHandler() {
		closeConnection();
	}

	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readObject();

		System.out.println("[SERVER CONNECTION HANDLER] Client "
				+ socket.getInetAddress().getHostAddress() + " (" + clientName
				+ ") connected");

	}

	public synchronized void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				connectionListener.removeConnection(this);
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out
					.println("[SERVER CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void closeConnectionWithoutDiscardConnListener() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out
					.println("[SERVER CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void sendData(Object data) {
		try {
			out.reset(); // clear the cache so that we don't send old data
			out.writeObject(data);
			out.flush();
			if (DEBUG)
				System.out.println("[SERVER CONNECTION HANDLER] Sent Data ("
						+ data.getClass().getSimpleName() + ")");
		} catch (IOException e) {
			System.out
					.println("[SERVER CONNECTION HANDLER] Unable to send data... there is an open connection?");
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
