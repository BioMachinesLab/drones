package dataObjects;

import network.messages.MotorMessage;

public class MotorSpeeds {
	
	private MotorMessage msg;
	private boolean changed = false;
	
	public synchronized void setSpeeds(MotorMessage message) {
		
		if(changed == false)
			System.out.println("##############ignoring message");
		
		if(msg == null || message.getLeftMotor() != msg.getLeftMotor() || message.getRightMotor() != msg.getRightMotor()) {
			this.msg = message;
			changed = true;
			notifyAll();
		} else {
			System.out.println("##############ignoring message");
		}
	}
	
	public synchronized MotorMessage getSpeeds() {
		
		try {
		
			while(!changed)
				wait();
		
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		changed = false;
		
		return msg;
	}
	
}