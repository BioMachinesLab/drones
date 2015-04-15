package network;

import commoninterface.network.messages.MotorMessage;

import dataObjects.ConsoleMotorSpeeds;

public class MotorMessageSender extends  Thread {
	
	private final static boolean DEBUG = false;
	private MotorConnection connection;
	private ConsoleMotorSpeeds speeds;
	private boolean keepExecuting = true;
	
	public MotorMessageSender(MotorConnection connection, ConsoleMotorSpeeds speeds) {
		this.connection = connection;
		this.speeds = speeds;
	}
	
	@Override
	public void run() {
		while(keepExecuting) {
			MotorMessage m = speeds.getMotorMessage();
			connection.sendData(m);
			if(DEBUG)
				System.out.println("[SEND] Sent motor speed: L="+m.getLeftMotor()+" R="+m.getRightMotor());
		}
	}
	
	public void stopExecuting() {
		keepExecuting = false;
	}
}