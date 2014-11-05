package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import main.Controller;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.SystemStatusMessage;

public class ConnectionHandler extends Thread {
	protected Socket socket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected Controller controller;
	protected String clientName = null;
	protected ConnectionListener connectionListener;

	public ConnectionHandler(Socket socket, Controller controller,
			ConnectionListener connectionListener) {
		this.socket = socket;
		this.controller = controller;
		this.connectionListener = connectionListener;
	}

	@Override
	public void run() {
		try {

			initConnection();

			while (true) {
				try {
					Message message = (Message) in.readObject();

					processMessage(message);

				} catch (ClassNotFoundException e) {
					System.out
							.println("[CONNECTION HANDLER] Received class of unknown type from "
									+ clientName + ", so it was discarded....");
				}
			}

		} catch (IOException e) {
			System.out.println("Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") disconnected");
		} catch (ClassNotFoundException e) {
			System.out.println("I didn't reveived a correct name from "
					+ socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		} finally {
			// always shutdown the handler when something goes wrong
			shutdownHandler();
		}
	}

	protected void shutdownHandler() {
		closeConnection();
	}

	protected void processMessage(Message message) {
		if (message instanceof InformationRequest) {
			System.out
					.println("[CONNECTION HANDLER] Information Request Message ("
							+ message.getClass().getSimpleName() + ")");
			controller.processInformationRequest(message, this);
		}
	}

	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readObject();

		System.out.println("[CONNECTION HANDLER] Client "
				+ socket.getInetAddress().getHostAddress() + " (" + clientName
				+ ") connected");

		// controller.processInformationRequest(new
		// InformationRequest(MessageType.SYSTEM_STATUS), this);
		sendData(new SystemStatusMessage(controller.getInitialMessages()));
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
					.println("[CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void closeConnectionWhithoutDiscard() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to close connection to "
							+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void sendData(Object data) {
		try {
			out.reset();//clear the cache so that we don't send old data
			out.writeObject(data);
			out.flush();
			System.out.println("[CONNECTION HANDLER] Sent Data ("
					+ data.getClass().getSimpleName() + ")");
		} catch (IOException e) {
			System.out
					.println("[CONNECTION HANDLER] Unable to send data... there is an open connection?");
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
