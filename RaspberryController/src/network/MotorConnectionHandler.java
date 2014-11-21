package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import dataObjects.MotorSpeeds;
import main.Controller;
import network.messages.Message;
import network.messages.MotorMessage;

public class MotorConnectionHandler extends ConnectionHandler {
	
	public MotorConnectionHandler(Socket socket, Controller controller, ConnectionListener connectionListener) {
		super(socket, controller, connectionListener);
	}

	@Override
	protected void shutdownHandler() {
		if (connectionListener.getConnections().isEmpty()) {
			controller.getIOManager().setMotorSpeeds(-1, -1);
		}
		closeConnection();
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof MotorMessage) {
			MotorMessage motorMessage = (MotorMessage) message;
			controller.getIOManager().setMotorSpeeds(motorMessage.getLeftMotor(), motorMessage.getRightMotor());
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

		// controller.processInformationRequest(new
		// InformationRequest(MessageType.SYSTEM_STATUS), this);
		/* sendData(new SystemStatusMessage(controller.getInitialMessages())); */
	}
}