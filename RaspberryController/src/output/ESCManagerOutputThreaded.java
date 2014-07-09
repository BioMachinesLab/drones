package output;

import java.io.IOException;

import utils.Math_Utils;

public class ESCManagerOutputThreaded extends Thread implements
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

	public ESCManagerOutputThreaded() {
		try {
			writeValueToESC(0, 1);
			writeValueToESC(1, 1);
			Thread.sleep(1000);

			writeValueToESC(0, ARM_VALUE);
			writeValueToESC(1, ARM_VALUE);
			Thread.sleep(1000);
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
		interrupt();
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
			System.out.println("oioi");
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
		}
	}

	public void disableMotor(int index) {
		if (index == 0) {
			L_value = DISABLE_VALUE;
			interrupt();
		} else {
			if (index == 1) {
				R_value = DISABLE_VALUE;
				interrupt();
			}
		}
	}

	public void disableMotors() {
		L_value = DISABLE_VALUE;
		R_value = DISABLE_VALUE;
		interrupt();
	}

	@Override
	public void run() {
		while (true) {
			try {
				writeValuesToESC();
			} catch (InterruptedException e) {
			}
		}
	}

	private synchronized void writeValuesToESC() throws InterruptedException {
		writeValueToESC(0, L_value);
		writeValueToESC(1, R_value);
		wait();
	}
}
