package network;

import dataObjects.MotorSpeeds;
import network.messages.MotorMessage;

public class MotorMessageSender extends  Thread {
	
	private ConnectionToDrone connection;
	private MotorSpeeds speeds;
	
	public MotorMessageSender(ConnectionToDrone connection, MotorSpeeds speeds) {
		this.connection = connection;
		this.speeds = speeds;
	}
	
	@Override
	public void run() {
		while(true) {
			MotorMessage m = speeds.getSpeeds();
			connection.sendData(m);
			System.out.println("[SEND] Sent motor speed: L="+m.getLeftMotor()+" R="+m.getRightMotor());
		}
	}

}
