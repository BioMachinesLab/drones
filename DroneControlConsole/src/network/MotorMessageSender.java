package network;

import network.messages.MotorMessage;
import dataObjects.MotorSpeeds;

public class MotorMessageSender extends  Thread {
	
	private final static boolean DEBUG = false;
	private MotorConnection connection;
	private MotorSpeeds speeds;
	private boolean keepExecuting = true;
	
	public MotorMessageSender(MotorConnection connection, MotorSpeeds speeds) {
		this.connection = connection;
		this.speeds = speeds;
	}
	
	@Override
	public void run() {
		while(keepExecuting) {
			MotorMessage m = speeds.getSpeeds();
			connection.sendData(m);
			if(DEBUG)
				System.out.println("[SEND] Sent motor speed: L="+m.getLeftMotor()+" R="+m.getRightMotor());
		}
	}
	
	public void stopExecuting() {
		keepExecuting = false;
	}
}