package io.output;

import io.UnavailableDeviceException;

import java.io.IOException;

import network.messages.MotorMessage;
import utils.Math_Utils;
import dataObjects.MotorSpeeds;

public class ESCManagerOutputThreadedImprov extends Thread implements
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

	public ESCManagerOutputThreadedImprov(MotorSpeeds speeds) {
		this.speeds = speeds;
		try {
			writeValueToESC(0, 50);
			writeValueToESC(1, 50);
			Thread.sleep(500);

			writeValueToESC(0, ARM_VALUE);
			writeValueToESC(1, ARM_VALUE);
			Thread.sleep(500);
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
		
		if(!available)
			return;
		
		switch (index) {
		case 0:
			if (value == 0) {
				L_value = STOP_L_VALUE;
			} else {
				L_value = (int) new Math_Utils().map(value, 0, 1, MIN_L_VALUE,
						MAX_L_VALUE);
			}
			break;
		case 1:
			if (value == 0) {
				R_value = STOP_R_VALUE;
			} else {
				R_value = (int) new Math_Utils().map(value, 0, 1, MIN_R_VALUE,
						MAX_R_VALUE);
			}
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void writeValueToESC(int index, int value) {
		// long time = System.currentTimeMillis();
		try {
			switch (index) {
			case 0:
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + value
										+ " > /dev/servoblaster" });
				break;
			case 1:
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + RIGHT_ESC + "=" + value
										+ " > /dev/servoblaster" });
				break;

			// Case used on shutdown, to avoid spend processing time (and have
			// faith that it will run until the end!!!!)
			case 3:
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + RIGHT_ESC + "=" + value
										+ " > /dev/servoblaster; echo "
										+ LEFT_ESC + "=" + value
										+ " > /dev/servoblaster" });
				break;

			case 4:
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + RIGHT_ESC + "=" + R_value
										+ " > /dev/servoblaster; echo "
										+ LEFT_ESC + "=" + L_value
										+ " > /dev/servoblaster" });
				break;
			case 5:
				Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + L_value
										+ " > /dev/servoblaster& echo "
										+ RIGHT_ESC + "=" + R_value
										+ " > /dev/servoblaster&" });
				break;
			default:
				throw new IllegalArgumentException();
			}
			System.out.println("[ESCManager] Wrote to motor L: "+L_value+" R:"+R_value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}

	private void disableMotors() {
		L_value = DISABLE_VALUE;
		R_value = DISABLE_VALUE;

		writeValueToESC(5, 0);
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

			writeValueToESC(5, 0);
		}
	}
	
	@Override
	public boolean isAvailable() {
		return available;
	}
}
