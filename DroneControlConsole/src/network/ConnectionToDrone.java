package network;

import gui.GUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import network.messages.Message;

public class ConnectionToDrone extends Thread {
	private int port = 10101;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private InetAddress destHost;
	private String destHostName;
	private GUI gui;

	public ConnectionToDrone(GUI gui, InetAddress destHost, int port) {
		socket = null;
		in = null;
		out = null;
		destHostName = null;
		this.destHost = destHost;
		this.port = port;
		this.gui = gui;
	}

	public ConnectionToDrone(GUI gui) {
		try {
			socket = null;
			in = null;
			out = null;
			destHostName = null;
			destHost = InetAddress.getLocalHost();
			this.gui = gui;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public void run() {
		try {
			socket = new Socket(destHost, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			out.writeObject(InetAddress.getLocalHost().getHostName());
			out.flush();

			destHostName = (String) in.readObject();
			System.out.println("Connected to: " + destHostName);

			while (true) {
				try {
					Message message = (Message) in.readObject();
					gui.processMessage(message);
				} catch (ClassNotFoundException e) {
					System.out.println("Received class of unknown type from "
							+ destHostName + ", so it was discarded....");
				}
			}
		} catch (IOException e) {
			System.out.println("Drone Controller closed the connection");
		} catch (ClassNotFoundException e) {
			System.out.println("I didn't reveived a correct name from "
					+ socket.getInetAddress().getHostAddress());
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				System.out
						.println("Unable to close connection... there is an open connection?");
			}
		}
	}

	public String getDestHostName() {
		return destHostName;
	}

	public void closeConnection() {
		if (socket != null && !socket.isClosed()) {
			System.out.println("Closing Connection.... ");
			try {
				socket.close();
			} catch (IOException e) {
				System.out
						.println("Unable to close connection... there is an open connection?");
			}
		}
	}
}
