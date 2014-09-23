package network;

import gui.GUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

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
			if (socket != null && !socket.isClosed()) {
				out.writeObject(data);
				out.flush();
				System.out.println("[SEND] Sent Message");
			}
		} catch (IOException e) {
			System.err
					.println("Unable to send data... there is an open connection?");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			socket = new Socket(destHost, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			try {
				out.writeObject(InetAddress.getLocalHost().getHostName());
				out.flush();

				destHostName = (String) in.readObject();
				System.out.println("Connected to " + destHost.getHostAddress()
						+ " (" + destHostName + ")");

				while (true) {
					try {
						Message message = (Message) in.readObject();
						System.out.println("[RECEIVED] Received Message");
						gui.processMessage(message);
					} catch (ClassNotFoundException e) {
						System.err
								.println("Received class of unknown type from "
										+ destHostName
										+ ", so it was discarded....");
					}
				}
			} catch (IOException e) {
				System.err.println("Drone Controller closed the connection");
			} catch (ClassNotFoundException e) {
				System.err.println("I didn't reveived a correct name from "
						+ socket.getInetAddress().getHostAddress());
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					System.err
							.println("Unable to close connection... there is an open connection?");
				}
			}
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					null,
					"Unable to open a connection to "
							+ destHost.getHostAddress(), "Connection Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public String getDestHostName() {
		return destHostName;
	}

	public InetAddress getDestInetAddress() {
		return destHost;
	}

	public void closeConnection() {
		if (socket != null && !socket.isClosed()) {
			System.out.println("Closing Connection.... ");
			try {
				this.interrupt();
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				System.err
						.println("Unable to close connection... there is an open connection?");
			}
		}
	}
}
