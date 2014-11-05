package dataObjects;

import network.messages.MotorMessage;

public class MotorSpeeds {
	
	private double speedLeft = 0;
	private double speedRight = 0;
	
	private double limit = 0;
	private double offset = 0;
	private boolean changed = true;
	
	public synchronized void setSpeeds(double left, double right) {
		
		left = limit(left);
		right = limit(right);
		
		left = offsetLeft(left);
		right = offsetRight(right);

		if(Math.abs(left-speedLeft) >= 0.05 || Math.abs(right-speedRight) >= 0.05){
			
			this.speedLeft = left;
			this.speedRight = right;
			
			changed = true;
			notifyAll();
		}
}
	
	private double limit(double val) {
		return val > limit ? limit : val;
	}
	
	private double offsetLeft(double leftVal) {
		if(offset > 0) {
			//right should have more power
			leftVal*= (1-offset);
		}
		
		return leftVal;
		
	}
	
	private double offsetRight(double rightVal) {
		if (offset < 0) {
			//left should have more power
			offset = -offset;
			rightVal*= (1-offset);
		}
		return rightVal;
		
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

	public void setLimit(double motorLimit) {
		this.limit = motorLimit;
	}

	public void setOffset(double motorOffset) {
		this.offset = motorOffset;
	}
	
}