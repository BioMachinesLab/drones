import java.io.IOException;

public class ESCTester extends Thread {
	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int DISABLE_VALUE = 0;
	private final static int ARM_VALUE = 80;

	private final static int STOP_VALUE = 120;
	private final static int MIN_VALUE = 121;
	private final static int MAX_VALUE = 179;

	private final static int DELAY_ACCEL = 10;
	private final static int DELAY_DESACEL = 10;

	public static void main(String[] args) {
		new ESCTester().start();
	}

	public ESCTester() {
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
	public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.print("# Shutting down... ");

					writeValueToESC(0, DISABLE_VALUE);
					writeValueToESC(1, DISABLE_VALUE);

					sleep(1000);

					System.out.println("now!");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		while (true) {
			System.out.println("Inscreasing speed!");
			for (int i = STOP_VALUE; i <= MAX_VALUE; i+=9) {
				writeValueToESC(0, i);
				writeValueToESC(1, i);

				try {
					sleep(DELAY_ACCEL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Decreasing speed!");
			for (int i = MAX_VALUE; i >= STOP_VALUE; i-=9) {
				writeValueToESC(0, i);
				writeValueToESC(1, i);

				try {
					sleep(DELAY_DESACEL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Restarting.....!");
		}
	}

	private synchronized void writeValueToESC(int index, int value) {
		// long time = System.currentTimeMillis();
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
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}
}
