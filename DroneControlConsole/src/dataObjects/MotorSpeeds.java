package dataObjects;

import network.messages.MotorMessage;

public class MotorSpeeds {
	
	private double speedLeft = 0.5;
	private double speedRight = 0.5;
	
	private double limit = 0;
	private double offset = 0;
	private boolean changed = false;
	
	public synchronized void setSpeeds(double left, double right) {
		
		left= left/2.0 + 0.5;
		right= right/2.0 + 0.5;
		
		left = limit(left);
		right = limit(right);
		
		left = offset(left, offset);
		right = offset(right, -offset);
		
		if(Math.abs(left-speedLeft) >= 0.01 || Math.abs(right-speedRight) >= 0.01){
			
			this.speedLeft = left;
			this.speedRight = right;
			
			changed = true;
			notifyAll();
		}
}
	
	private double limit(double val) {
		
		val = (val - 0.5)*2;
		
		if(Math.abs(val) > limit) {
			val*=limit;
		}
		
		val/=2;
		val+=0.5;
		
		return val;
		
	}
	
	private double offset(double val, double offset) {
		
		val = (val - 0.5)*2;
		
		if(offset > 0) {
			//right should have more power
			val*= (1-Math.abs(offset));
		}
		
		val/=2;
		val+=0.5;
		
		return val;
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