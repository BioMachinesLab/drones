package network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import main.DroneControlConsole;
import main.RobotControlConsole;
import network.server.shared.dataObjects.DroneData;
import network.server.shared.dataObjects.ServerStatusData;
import network.server.shared.messages.DronesInformationRequest;
import network.server.shared.messages.DronesInformationResponse;
import network.server.shared.messages.NetworkMessage;
import network.server.shared.messages.ServerMessage;
import network.server.shared.messages.ServerStatusResponse;

import com.google.gson.Gson;

public class ServerConnectionHandler extends Thread {
	protected Socket socket;
	protected PrintWriter out;
	protected BufferedReader in;
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
				NetworkMessage networkMessage = new Gson().fromJson(
						in.readLine(), NetworkMessage.class);
				processData(networkMessage);
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

	private void processData(NetworkMessage data) throws ClassNotFoundException {
		ServerMessage inMessage = data.getMessage();
		switch (data.getMsgType()) {
		case DronesInformationRequest:
			NetworkMessage responseMessageA = new NetworkMessage();
			DronesInformationResponse dronesInformationResponse = new DronesInformationResponse();
			ArrayList<DroneData> dronesIdentification = (connectionListener
					.getConsole().getDronesSet()
					.getDrones(((DronesInformationRequest) inMessage)
							.getDroneIdentification()));
			dronesInformationResponse.setDronesData(dronesIdentification);
			responseMessageA.setMessage(dronesInformationResponse);
			sendData(responseMessageA);
			break;
		case ServerStatusRequest:
			NetworkMessage responseMessageB = new NetworkMessage();
			ServerStatusData serverStatusData = new ServerStatusData();
			RobotControlConsole console = connectionListener.getConsole();

			serverStatusData.setAvailableBehaviors(console.getGUI()
					.getCommandPanel().getAvailableBehaviors());
			serverStatusData.setAvailableControllers(console.getGUI()
					.getCommandPanel().getAvailableControllers());
			serverStatusData.setConnectedClientsQty(connectionListener
					.getClientQuantity());

			if (console instanceof DroneControlConsole) {
				serverStatusData.setConnectedTo(((DroneControlConsole) console)
						.getDronesSet().getConnectedToAddress());
			}

			ServerStatusResponse responseB = new ServerStatusResponse();
			responseB.setServerStatusData(serverStatusData);
			responseMessageB.setMessage(responseB);
			sendData(responseMessageB);
			break;
		default:
			System.out.println("Received message with type: "
					+ ((ServerMessage) inMessage).getMessageType());
			break;
		}
	}

	public void sendData(NetworkMessage outMessage) {
		String json = new Gson().toJson(outMessage, NetworkMessage.class);
		out.println(json);
		System.out
				.println("[SERVER CONNECTION HANDLER] Sent information of type "
						+ outMessage.getMsgType());
	}

	protected void shutdownHandler() {
		closeConnection();
	}

	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),
				true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		out.println(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readLine();

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

	public Socket getSocket() {
		return socket;
	}
}
