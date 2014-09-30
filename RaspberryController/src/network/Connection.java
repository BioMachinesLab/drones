package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import main.Controller;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MotorMessage;
import network.messages.InformationRequest.MessageType;

public class Connection extends Thread {
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Controller controller;
	private String clientName = null;
	private ConnectionHandler connectionHandler;

	public Connection(Socket socket, Controller controller,
			ConnectionHandler connectionHandler) {
		super();
		this.socket = socket;
		this.controller = controller;
		this.connectionHandler = connectionHandler;
	}

	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			out.reset();

			out.writeObject(InetAddress.getLocalHost().getHostName());
			out.flush();

			clientName = (String) in.readObject();

			System.out.println("[CONNECTION] Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") connected");

			controller.sendMessageToOperator("Welcome to drone "
					+ InetAddress.getLocalHost().getHostName()
					+ "! Take good care of me :)");
			controller.processInformationRequest(new InformationRequest(
					MessageType.SYSTEM_STATUS), this);

			while (true) {
				try {
					Message message = (Message) in.readObject();

					if (message instanceof MotorMessage) {
						// System.out.println("[CONNECTION] Motor Message");
						controller.processMotorMessage((MotorMessage) message);
					}

					if (message instanceof InformationRequest) {
						System.out
								.println("[CONNECTION] Information Request Message");
						controller.processInformationRequest(
								(InformationRequest) message, this);
					}
				} catch (ClassNotFoundException e) {
					System.out
							.println("[CONNECTION] Received class of unknown type from "
									+ clientName + ", so it was discarded....");
					// e.printStackTrace();
				} catch (SocketException e) {
					controller
							.processMotorMessage(new MotorMessage(0, 0));
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			System.out.println("Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") disconnected");
			controller.processMotorMessage(new MotorMessage(0, 0));
		} catch (ClassNotFoundException e) {
			System.out.println("I didn't reveived a correct name from "
					+ socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out
						.println("[CONNECTION] Unable to close connection... there is an open connection?");
				e.printStackTrace();
			}
		}
	}

	public synchronized void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				connectionHandler.removeConnection(this);
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out.println("[CONNECTION] Unable to close connection to "
					+ clientName + "... there is an open connection?");
		}
	}

	public synchronized void sendData(Object data) {
		try {
			out.writeObject(data);
			out.flush();
			System.out.println("[CONNECTION] Sent Data");
		} catch (IOException e) {
			System.out
					.println("[CONNECTION] Unable to send data... there is an open connection?");
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
