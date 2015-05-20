package network.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import commoninterface.RobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;
import commoninterface.network.messages.Message;

public class ServerConnectionHandler extends ConnectionHandler{
	private final static boolean DEBUG = false;
	
	
	public ServerConnectionHandler(Socket socket, RobotCI robot,ConnectionListener connectionListener) {
		super(socket, robot, connectionListener);
	}

	@Override
	public void run() {
		try {
			initConnection();
			
			while (true) {
				
				
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
	
	@Override
	protected void shutdownHandler() {
		closeConnection();
	}

	@Override
	protected void processMessage(Message message) {
		
	}

	@Override
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
}
