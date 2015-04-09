package commoninterface.dataobjects;

import commoninterface.network.messages.MotorMessage;

public class MotorSpeeds {

	private MotorMessage msg;
	private boolean changed = false;

	public synchronized void setSpeeds(MotorMessage message) {
		if (msg == null || message.getLeftMotor() != msg.getLeftMotor()
				|| message.getRightMotor() != msg.getRightMotor()) {
			this.msg = message;
			changed = true;
			notifyAll();
		}
	}
	
	public synchronized MotorMessage getSpeeds() {
		try {
			while (!changed) {
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		changed = false;
		return msg;
	}
	
	public MotorMessage getNonBlockingSpeeds() {
		return msg;
	}
}