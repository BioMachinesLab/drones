package io.output;

import java.util.LinkedList;

import com.pi4j.wiringpi.SoftTone;

public class BuzzerOutput extends Thread implements ControllerOutput {
	public enum BuzzerMode {
		BEEP, DOUBLE_BEEP, ERROR, BUZZ, MUTE
	}

	// Sound constant
	private static int BEEP_LENGHT = 10;
	private static int BEEP_DELAY = 10;
	private static int ERROR_BEEP_DELAY = 2;
	private static int ERROR_BEEP_LENGHT = 3;
	private static int BUZZER_DEFAULT_FREQUENCY = 2000;

	private static final int BUZZER_PIN = 7;
	private LinkedList<BuzzerBehaviour> behaviours = new LinkedList<BuzzerBehaviour>();
	private int buzzer_frequency;

	private boolean available = false;

	public BuzzerOutput() {
		int rtn = SoftTone.softToneCreate(BUZZER_PIN);

		if (rtn == 0) {
			available = true;
		}
	}

	@Override
	public void setValue(int index, double value) {
		behaviours.add(new BuzzerBehaviour(BuzzerMode.BUZZ, (int) value,
				BUZZER_DEFAULT_FREQUENCY));
		notify();
	}

	public void setValue(int length, int frequency) {
		behaviours.add(new BuzzerBehaviour(BuzzerMode.BUZZ, length, frequency));
		notify();
	}

	public void setValue(BuzzerMode sound) {
		behaviours.add(new BuzzerBehaviour(sound, 0, BUZZER_DEFAULT_FREQUENCY));
		notify();
	}

	@Override
	public int getNumberOfOutputs() {
		return 1;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public double getValue(int index) {
		return buzzer_frequency;
	}

	public void muteBuzzer() {
		SoftTone.softToneStop(BUZZER_PIN);
	}

	@Override
	public void run() {
		try {
			while (true) {
				try {
					if (!behaviours.isEmpty()) {
						BuzzerBehaviour behave = behaviours.poll();

						switch (behave.getMode()) {
						case MUTE:
							muteBuzzer();
							buzzer_frequency = 0;
							break;
						case BUZZ:
							SoftTone.softToneWrite(BUZZER_PIN,
									behave.getFrequency());
							if (behave.getLenght() != 0) {
								sleep(behave.getLenght());
								muteBuzzer();
							}
							break;
						case BEEP:
							SoftTone.softToneWrite(BUZZER_PIN,
									behave.getFrequency());
							sleep(BEEP_LENGHT);
							muteBuzzer();
							break;
						case DOUBLE_BEEP:
							SoftTone.softToneWrite(BUZZER_PIN,
									behave.getFrequency());
							sleep(BEEP_LENGHT);
							muteBuzzer();
							sleep(BEEP_DELAY);
							SoftTone.softToneWrite(BUZZER_PIN,
									behave.getFrequency());
							sleep(BEEP_LENGHT);
							muteBuzzer();
						case ERROR:
							// TODO
							break;
						default:
							muteBuzzer();
						}
					}

					wait();
				} catch (InterruptedException e) {

				}
			}
		} finally {
			muteBuzzer();
		}
	}

	class BuzzerBehaviour {
		private BuzzerMode mode;
		private int lenght;
		private int frequency;

		public BuzzerBehaviour(BuzzerMode mode, int length, int frequency) {
			this.mode = mode;
			this.lenght = length;

			if (frequency > 0) {
				mode = BuzzerMode.BUZZ;

				if (frequency > 5000) {
					this.frequency = 5000;
				}
			} else {
				mode = BuzzerMode.MUTE;
				this.frequency = 0;
			}
		}

		public BuzzerMode getMode() {
			return mode;
		}

		public int getLenght() {
			return lenght;
		}

		public int getFrequency() {
			return frequency;
		}
	}
}
