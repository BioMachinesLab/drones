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
	private boolean ready = false;

	public ConnectionToDrone(GUI gui, InetAddress destHost, int port) throws IOException {
		socket = null;
		in = null;
		out = null;
		destHostName = null;
		this.destHost = destHost;
		this.port = port;
		this.gui = gui;
		if(!checkIP(destHost)) {
			throw new UnknownHostException(destHost.getHostAddress()+" unreachable!");
		}
		
		connect();
	}

	public ConnectionToDrone(GUI gui) throws IOException {
		socket = null;
		in = null;
		out = null;
		destHostName = null;
		destHost = InetAddress.getLocalHost();
		this.gui = gui;
		
		if(!checkIP(destHost)) {
			throw new UnknownHostException(destHost.getHostAddress()+" unreachable!");
		}
		connect();
	}
	
	private void connect() throws IOException {
		socket = new Socket(destHost, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	public synchronized void sendData(Object data) {
		
		if(!ready)
			return;
		
		try {
			if (socket != null && !socket.isClosed()) {
				out.writeObject(data);
				out.flush();
				System.out.println("[SEND] Sent Message "+data.getClass().getSimpleName());
			}
		} catch (IOException e) {
			System.err
					.println("Unable to send data... there is an open connection?");
			e.printStackTrace();
		}
	}
	
	private boolean checkIP(InetAddress destHost) throws IOException {
		return InetAddress.getByName(destHost.getHostAddress()).isReachable(5*1000);//10 sec timeout
	}

	@Override
	public void run() {
		try {
			out.writeObject(InetAddress.getLocalHost().getHostName());
			out.flush();

			destHostName = (String) in.readObject();
			System.out.println("Connected to " + destHost.getHostAddress()
					+ " (" + destHostName + ")");

			ready = true;
			while (true) {
				try {
					Message message = (Message) in.readObject();
					System.out.println("[RECEIVED] Received Message "+message.getClass().getSimpleName());
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
				System.err.println("Unable to close connection... is there an open connection?");
			}
		}
		JOptionPane.showMessageDialog(gui.getFrame(), "Connection to drone was lost!");
		gui.connect();
		
	}

	public String getDestHostName() {
		return destHostName;
	}

	public InetAddress getDestInetAddress() {
		return destHost;
	}

	public synchronized void closeConnection() {
		if (socket != null && !socket.isClosed()) {
			System.out.println("Closing Connection.... ");
			try {
				this.interrupt();
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				System.err.println("Unable to close connection... is there an open connection?");
			}
		}
	}
}
