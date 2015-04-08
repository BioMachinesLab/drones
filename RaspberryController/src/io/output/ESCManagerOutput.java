package io.output;

import java.io.IOException;
import network.messages.MotorMessage;
import commoninterface.utils.MathUtils;

import dataObjects.MotorSpeeds;

public class ESCManagerOutput extends Thread implements
		ControllerOutput {

	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int DISABLE_VALUE = 0;
	private final static int ARM_VALUE = 80;

	private final static int STOP_L_VALUE = 120;
	private final static int MIN_L_VALUE = 121;
	private final static int MAX_L_VALUE = 179;

	private final static int STOP_R_VALUE = 120;
	private final static int MIN_R_VALUE = 121;
	private final static int MAX_R_VALUE = 179;

	private int L_value = STOP_L_VALUE;
	private int R_value = STOP_R_VALUE;

	private MotorSpeeds speeds;
	
	private boolean available = false;

	public ESCManagerOutput(MotorSpeeds speeds) {
		this.speeds = speeds;
		try {
			
			setRawValues(50,50);
			Thread.sleep(500);

			setRawValues(ARM_VALUE,ARM_VALUE);
			Thread.sleep(500);
			available = true;
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public int getNumberOfOutputs() {
		return 2;
	}

	@Override
	public void setValue(int index, double value) {
		
		if(!available)
			return;
		
		switch (index) {
		case 0:
			if (value == 0) {
				L_value = STOP_L_VALUE;
			} else {
				L_value = (int) MathUtils.map(value, 0, 1, MIN_L_VALUE,
						MAX_L_VALUE);
			}
			break;
		case 1:
			if (value == 0) {
				R_value = STOP_R_VALUE;
			} else {
				R_value = (int) MathUtils.map(value, 0, 1, MIN_R_VALUE,
						MAX_R_VALUE);
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void writeValueToESC() {
		// long time = System.currentTimeMillis();
		try {
			try {
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + L_value
										+ " > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + R_value
										+ " > /dev/servoblaster;" }).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("[ESCManager] Wrote to motor L: "+L_value+" R:"+R_value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}

	private void disableMotors() {
		setRawValues(DISABLE_VALUE, DISABLE_VALUE);
		writeValueToESC();
	}

	@Override
	public void run() {
		
		if(!available)
			return;
		
		while (true) {
			MotorMessage m = speeds.getSpeeds();
			writeValuesToESC(m);
		}
	}

	private void writeValuesToESC(MotorMessage m) {
		if (m.getLeftMotor() == -1 || m.getRightMotor() == -1) {
			disableMotors();
		} else {
			setValue(0, m.getLeftMotor());
			setValue(1, m.getRightMotor());
			writeValueToESC();
		}
	}
	
	private void setRawValues(int left, int right) {
		L_value = left;
		R_value = right;
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}
	
	@Override
	public double getValue(int index) {
		if(index == 0) {
			return L_value;
		}
		return R_value;
	}
}
