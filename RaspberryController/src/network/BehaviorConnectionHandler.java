package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import commoninterfaceimpl.RealAquaticDroneCI;
import network.messages.BehaviorMessage;
import network.messages.Message;

public class BehaviorConnectionHandler extends ConnectionHandler {
	
	private final static boolean DEBUG = false;
	
	public BehaviorConnectionHandler(Socket socket, RealAquaticDroneCI drone, ConnectionListener connectionListener) {
		super(socket, drone, connectionListener);
	}

	@Override
	protected void shutdownHandler() {
		closeConnection();
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof BehaviorMessage) {
			if(DEBUG)
				System.out.println("[BehaviorConnectionHandler] Received new Behavior");
			drone.processInformationRequest(message, this);
		}
	}

	@Override
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

	}
	
}