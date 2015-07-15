package commoninterface.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import commoninterface.RobotCI;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MotorMessage;

public class MotorConnectionHandler extends ConnectionHandler {
	
	private final static boolean DEBUG = false;
	
	public MotorConnectionHandler(Socket socket, RobotCI robot, ConnectionListener connectionListener) {
		super(socket, robot, connectionListener);
	}

	@Override
	protected void shutdownHandler() {
		closeConnection();
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof MotorMessage) {
			MotorMessage motorMessage = (MotorMessage) message;
			if(DEBUG)
				System.out.println("[MotorConnectionHandler] Got new speeds "+motorMessage.getLeftMotor()+" "+motorMessage.getRightMotor());
			robot.setMotorSpeeds(motorMessage.getLeftMotor(), motorMessage.getRightMotor());
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