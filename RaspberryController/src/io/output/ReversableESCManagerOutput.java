package io.output;

import java.io.IOException;

import network.messages.MotorMessage;
import utils.Math_Utils;
import dataObjects.MotorSpeeds;

public class ReversableESCManagerOutput extends Thread implements
		ControllerOutput {

	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int CENTRAL_L_VALUE = 150;
	private final static int MIN_L_VALUE = 60;
	private final static int MAX_L_VALUE = 240;

	private final static int CENTRAL_R_VALUE = 150;
	private final static int MIN_R_VALUE = 60;
	private final static int MAX_R_VALUE = 240;

	private int L_value = CENTRAL_L_VALUE;
	private int R_value = CENTRAL_R_VALUE;

	private MotorSpeeds speeds;

	private boolean available = false;

	public ReversableESCManagerOutput(MotorSpeeds speeds) {
		this.speeds = speeds;
		try {
			setRawValues(CENTRAL_L_VALUE, CENTRAL_R_VALUE);
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
			if (value == 0) {
				L_value = CENTRAL_L_VALUE;
			} else {
				L_value = (int) Math_Utils.map(value, 0, 1, MIN_L_VALUE,
						MAX_L_VALUE);
			}
			break;
		case 1:
			if (value == 0) {
				R_value = CENTRAL_R_VALUE;
			} else {
				R_value = (int) Math_Utils.map(value, 0, 1, MIN_R_VALUE,
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
				Runtime.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + L_value
										+ " > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + R_value
										+ " > /dev/servoblaster;" }).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("[ESCManager] Wrote to motor L: " + L_value
					+ " R:" + R_value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}

	private void disableMotors() {
		setRawValues(CENTRAL_L_VALUE, CENTRAL_R_VALUE);
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
		L_value = left;
		R_value = right;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}
}
