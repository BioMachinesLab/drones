package io.output;

import java.io.IOException;

import utils.Math_Utils;

public class ESCManagerOutput implements ControllerOutput {
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

	/**
	 * Initiates the ESC and arms it (skipping the configuration process)
	 */
	public ESCManagerOutput() {
		try {
			writeValueToESC(0, 1);
			writeValueToESC(1, 1);
			Thread.sleep(1000);

			writeValueToESC(0, ARM_VALUE);
			writeValueToESC(1, ARM_VALUE);
			Thread.sleep(1000);

			writeValueToESC(0, STOP_L_VALUE);
			writeValueToESC(1, STOP_R_VALUE);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	/**
	 * Sets the velocity (between 0 and 1) for each motor (identified by an
	 * index: 0 for the Left Motor and 1 for the Right Motor)
	 */
	@Override
	public void setValue(int index, double value) {
		int value_to_set;
		switch (index) {
		case 0:
			if (value == 0) {
				value_to_set = STOP_L_VALUE;
			} else {
				value_to_set = (int) new Math_Utils().map(value, 0, 1,
						MIN_L_VALUE, MAX_L_VALUE);
			}

			writeValueToESC(index, value_to_set);
			break;
		case 1:
			if (value == 0) {
				value_to_set = STOP_R_VALUE;
			} else {
				value_to_set = (int) new Math_Utils().map(value, 0, 1,
						MIN_R_VALUE, MAX_R_VALUE);
			}

			writeValueToESC(index, value_to_set);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void writeValueToESC(int index, int value) {
		try {
			Process p;
			switch (index) {
			case 0:
				p = Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + value
										+ " > /dev/servoblaster" });
				p.waitFor();
				break;
			case 1:
				p = Runtime.getRuntime().exec(
						new String[] {
								"bash",
								"-c",
								"echo " + RIGHT_ESC + "=" + value
										+ " > /dev/servoblaster" });
				p.waitFor();
				break;
			default:
				throw new IllegalArgumentException();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Disables the power applied to a specific motor
	 * 
	 * @param ESC
	 *            side (0 for left ESC and 1 for the right ESC)
	 */
	public void disableMotor(int index) {
		writeValueToESC(index, DISABLE_VALUE);
	}
	
	public void disableMotors() {
		writeValueToESC(0, DISABLE_VALUE);
		writeValueToESC(1, DISABLE_VALUE);
	}
}
