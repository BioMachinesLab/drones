package io.output;

import java.io.IOException;

import network.messages.MotorMessage;
import utils.Math_Utils;
import dataObjects.MotorSpeeds;

public class ReversableESCManagerOutput extends Thread implements
		ControllerOutput {

	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int CENTRAL_VALUE = 150;
	private final static int MIN_VALUE = 60;
	private final static int MAX_VALUE = 240;
	
	private final static int MIN_FW_VALUE = 165;
	private final static int MIN_BW_VALUE = 135;

	private int lValue = CENTRAL_VALUE;
	private int rValue = CENTRAL_VALUE;
	
	private MotorSpeeds speeds;

	private boolean available = false;

	public ReversableESCManagerOutput(MotorSpeeds speeds) {
		this.speeds = speeds;
		try {
			setRawValues(CENTRAL_VALUE, CENTRAL_VALUE);
			Thread.sleep(1000);
			
			available = true;
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int index, double value) {

		if (!available)
			return;

		switch (index) {
		case 0:
			if (value == 0.5 || value == -1) {
				lValue = CENTRAL_VALUE;
			} else {
				if(value > 0.5) {
					lValue = (int)(Math_Utils.map(value, 0.5, 1, MIN_FW_VALUE, MAX_VALUE));
				} else if(value < 0.5) {
					lValue = (int)(Math_Utils.map(value, 0, 0.5, MIN_VALUE, MIN_BW_VALUE));
				}
			}
			break;
		case 1:
			if (value == 0.5 || value == -1) {
				rValue = CENTRAL_VALUE;
			} else {
				if(value > 0.5) {
					rValue = (int)(Math_Utils.map(value, 0.5, 1, MIN_FW_VALUE, MAX_VALUE));
				} else if(value < 0.5) {
					rValue = (int)(Math_Utils.map(value, 0, 0.5, MIN_VALUE, MIN_BW_VALUE));
				}
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
				Runtime.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + lValue
										+ " > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + rValue
										+ " > /dev/servoblaster;" }).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("[ESCManager] Wrote to motor L: " + lValue
					+ " R:" + rValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}

	private void disableMotors() {
		setRawValues(CENTRAL_VALUE, CENTRAL_VALUE);
		writeValueToESC();
	}

	@Override
	public void run() {
		if (!available)
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
		lValue = left;
		rValue = right;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}
	
	@Override
	public double getValue(int index) {
		if(index == 0) {
			return lValue;
		}
		return rValue;
	}
}
