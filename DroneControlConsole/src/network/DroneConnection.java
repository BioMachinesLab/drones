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

public abstract class DroneConnection extends Thread {
	
	protected int port;
	protected Socket socket;
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected InetAddress destHost;
	protected String destHostName;
	protected GUI gui;
	
	protected boolean ready = false;
	
	public DroneConnection(GUI gui, InetAddress destHost, int port) throws IOException {
		this.socket = null;
		this.in = null;
		this.out = null;
		this.destHostName = null;
		this.destHost = destHost;
		this.port = port;
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
				System.out.println("[SEND] Sent "+data.getClass().getSimpleName());
			}
		} catch (IOException e) {
			System.err.println("Unable to send data... there is an open connection?");
			e.printStackTrace();
		}
	}
	
	private boolean checkIP(InetAddress destHost) throws IOException {
		return InetAddress.getByName(destHost.getHostAddress()).isReachable(5*1000);//5 sec timeout
	}

	@Override
	public void run() {
		try {
			
			initialization();
			
			while (true) {
				update();
			}
			
		} catch (IOException e) {
			System.err.println("Drone Controller closed the connection");
		} catch (ClassNotFoundException e) {
			System.err.println("I didn't reveived a correct name from "+ socket.getInetAddress().getHostAddress());
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				System.err.println("Unable to close connection... is there an open connection?");
			}
		}
		
		shutdownConnection();
	}
	
	protected void shutdownConnection() {
		//No shutdown procedure in the default class
	}
	
	protected void initialization() throws IOException, ClassNotFoundException{
		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		destHostName = (String) in.readObject();
		System.out.println("Connected to " + destHost.getHostAddress()
				+ " (" + destHostName + ")");

		ready = true;
	}
	
	protected void update() throws IOException {
		try {
			Message message = (Message) in.readObject();
			System.out.println("[RECEIVED] Received "+message.getClass().getSimpleName());
			gui.processMessage(message);
		} catch (ClassNotFoundException e) {
			System.err.println("Received class of unknown type from "
							+ destHostName
							+ ", so it was discarded....");
		}
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