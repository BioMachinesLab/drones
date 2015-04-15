package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import commoninterface.RealRobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.ConnectionListener;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.EntitiesMessage;
import commoninterface.network.messages.LogMessage;
import commoninterface.network.messages.Message;

public class CommandConnectionHandler extends ConnectionHandler {
	
	private final static boolean DEBUG = false;
	
	public CommandConnectionHandler(Socket socket, RealRobotCI drone, ConnectionListener connectionListener) {
		super(socket, drone, connectionListener);
	}

	@Override
	protected void shutdownHandler() {
		closeConnection();
	}

	@Override
	protected void processMessage(Message message) {
		
		if(DEBUG)
			System.out.println("[CommandConnectionHandler] Received new message: "+message.getClass().getSimpleName());
		
		if (message instanceof BehaviorMessage) {
			robot.processInformationRequest(message, this);
		} else if(message instanceof LogMessage) {
			robot.processInformationRequest(message, this);
		} else if(message instanceof EntitiesMessage) {
			robot.processInformationRequest(message, this);
		}
	}

	@Override
	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		System.out.println("[CommandConnectionHandler] Client "
				+ socket.getInetAddress().getHostAddress() + " (" + clientName
				+ ") connected");
	}
	
	public synchronized void sendData(Object data) {
		super.sendData(data);
		//We only need to reply that we executed the Behavior,
		//the handler won't have any other function afterwards
		shutdownHandler();
	}
}