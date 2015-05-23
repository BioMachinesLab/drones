import java.io.IOException;

public class ServoSwipe {
	private final static int[] SERVO_PORTS = { 1 }; // WPI Ports
	private final static int DELAY = 50;
	private final static int MIN = 115;
	private final static int MAX = 237;
	private final static int STEPS = 1;

	public static void main(String[] args) {
		try {
			int step = STEPS;
			int i = MIN;
			Process p;

			for (int j = 0; j < SERVO_PORTS.length; j++) {
				p = Runtime
						.getRuntime()
						.exec(new String[] { "bash", "-c",
								"echo " + j + "=" + 0 + " > /dev/servoblaster" });
				p.waitFor();

				Thread.sleep(200);

				p = Runtime
						.getRuntime()
						.exec(new String[] { "bash", "-c",
								"echo " + j + "=" + 50 + " > /dev/servoblaster" });
				p.waitFor();
			}

			while (true) {
				System.out.println("Set on: " + i);
				for (int j = 0; j < SERVO_PORTS.length; j++) {
					p = Runtime.getRuntime().exec(
							new String[] {
									"bash",
									"-c",
									"echo " + SERVO_PORTS[j] + "=" + i
											+ " > /dev/servoblaster" });
					p.waitFor();
				}

				i += step;
				if (i == MIN || i == MAX)
					step = -step;

				Thread.sleep(DELAY);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("Terminated!");
		}
	}
}
