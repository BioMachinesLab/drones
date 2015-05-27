package dataObjects;

import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.MotorMessage;

public class ConsoleMotorSpeeds {

	private double speedLeft = 0;
	private double speedRight = 0;

	private double limit = 0;
	private double offset = 0;
	private boolean changed = false;

	private String myHostname = "";

	public ConsoleMotorSpeeds() {
		updateHostname();
	}

	public synchronized void setSpeeds(double left, double right) {

		left = limit(left);
		right = limit(right);

		left = offset(left, offset);
		right = offset(right, -offset);

		if (Math.abs(left - speedLeft) >= 0.01
				|| Math.abs(right - speedRight) >= 0.01) {

			this.speedLeft = left;
			this.speedRight = right;

			changed = true;
			notifyAll();
		}
	}

	private double limit(double val) {
		val *= limit;
		return val;
	}

	private double offset(double val, double offset) {

		if (offset > 0) {
			// right should have more power
			val *= (1 - Math.abs(offset));
		}

		return val;
	}

	public synchronized MotorMessage getMotorMessage() {

		try {
			while (!changed)
				wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		changed = false;

		return new MotorMessage(speedLeft, speedRight, myHostname);
	}

	public void setLimit(double motorLimit) {
		this.limit = motorLimit;
	}

	public void setOffset(double motorOffset) {
		this.offset = motorOffset;
	}

	private void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}
}