package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class MotorMessage extends Message {
	private static final long serialVersionUID = -8317383403438281892L;
	// speeds are between -1 (backward) and 1 (forward)
	private double leftMotor;
	private double rightMotor;

	public MotorMessage(double leftMotor, double rightMotor,
			String senderHostname) {
		super(senderHostname);
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	public double getLeftMotor() {
		return leftMotor;
	}

	public double getRightMotor() {
		return rightMotor;
	}
	
	@Override
	public Message getCopy() {
		return new MotorMessage(leftMotor, rightMotor, senderHostname);
	}
}
