package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import main.Controller;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MotorMessage;

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
			out.writeObject(InetAddress.getLocalHost().getHostName());
			out.flush();

			clientName = (String) in.readObject();

			System.out.println("Client "
					+ socket.getInetAddress().getHostAddress() + " ("
					+ clientName + ") connected");

			while (true) {
				try {
					Message message = (Message) in.readObject();

					if (message instanceof MotorMessage) {
						controller.processMotorMessage((MotorMessage) message,
								this);
					}

					if (message instanceof InformationRequest) {
						controller.processInformationRequest(
								(InformationRequest) message, this);
					}
				} catch (ClassNotFoundException e) {
					System.out.println("Received class of unknown type from "
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
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out
						.println("Unable to close connection... there is an open connection?");
			}
		}
	}

	public void closeConnection() {
		try {
			socket.close();
			connectionHandler.removeConnection(this);
		} catch (IOException e) {
			System.out.println("Unable to close connection to " + clientName
					+ "... there is an open connection?");
		}
	}

	public void sendData(Object data) {
		try {
			out.writeObject(data);
			out.flush();
		} catch (IOException e) {
			System.out
					.println("Unable to send data... there is an open connection?");
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
