package dataObjects;

import network.messages.MotorMessage;

public class MotorSpeeds {
	
	private double speedLeft = 0;
	private double speedRight = 0;
	private boolean changed = true;
	
	public synchronized void setSpeeds(double left, double right) {
		if(Math.abs(left-speedLeft) >= 0.05 || Math.abs(right-speedRight) >= 0.05 || (left == 0 && right == 0)){
			speedLeft = left;
			speedRight = right;
			changed = true;
			notifyAll();
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
		
		return new MotorMessage(speedLeft, speedRight);
	}
	
}