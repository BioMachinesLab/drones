package network;

import java.net.Socket;
import network.messages.Message;
import network.messages.MotorMessage;
import main.Controller;

public class MotorConnectionHandler extends ConnectionHandler {

	public MotorConnectionHandler(Socket socket, Controller controller,
			ConnectionListener connectionListener) {
		super(socket, controller, connectionListener);
	}
	
	@Override
	protected void shutdownHandler() {
		controller.processMotorMessage(new MotorMessage(0, 0));
		closeConnection();
	}
	
	@Override
	protected void processMessage(Message message) {
		if (message instanceof MotorMessage) {
			System.out.println("[MOTOR CONNECTION HANDLER] Motor Update Message");
			controller.processMotorMessage(((MotorMessage) message));
		}
	}
}