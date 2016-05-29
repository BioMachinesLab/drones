import java.util.LinkedList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftTone;

public class BuzzerOutput extends Thread {
	public enum BuzzerMode {
		BEEP, DOUBLE_BEEP, ERROR, BUZZ, MUTE
	}

	// Sound constant
	private final long DEFAULT_BEEP_DURATION = 250;
	private final long ERROR_BEEP_DURATION = 80;
	private final long DOUBLE_BEEP_DURATION = 100;
	private final long DOUBLE_BEEP_SLEEP = 400;

	private final Pin BUZZER_PIN = RaspiPin.GPIO_11;
	private LinkedList<BuzzerMode> behaviours = new LinkedList<BuzzerMode>();
	private boolean shutdown = false;
	private GpioPinDigitalOutput buzzer;

	public static void main(String[] args) {
		BuzzerOutput buzzOutput = new BuzzerOutput();
		buzzOutput.start();

		try {
			System.out.println("Beeping!");
			buzzOutput.setValue(BuzzerMode.BEEP);
			sleep(5000);

			System.out.println("Double beeping!");
			buzzOutput.setValue(BuzzerMode.DOUBLE_BEEP);
			buzzOutput.setValue(BuzzerMode.DOUBLE_BEEP);
			buzzOutput.setValue(BuzzerMode.DOUBLE_BEEP);
			buzzOutput.setValue(BuzzerMode.DOUBLE_BEEP);
			buzzOutput.setValue(BuzzerMode.DOUBLE_BEEP);
			buzzOutput.buzzOff();
			sleep(2000);

			System.out.println("Error beeping!");
			buzzOutput.setValue(BuzzerMode.ERROR);
			sleep(5000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			buzzOutput.shutdown();
		}
	}

	public BuzzerOutput() {
		final GpioController gpio = GpioFactory.getInstance();
		buzzer = gpio.provisionDigitalOutputPin(BUZZER_PIN);

		buzzer.setState(PinState.LOW);
	}

	public synchronized void setValue(int length, int frequency) {
		// behaviours.add(new BuzzerBehaviour(BuzzerMode.BUZZ, length,
		// frequency));
		notify();
	}

	public synchronized void setValue(BuzzerMode sound) {
		behaviours.add(sound);
		notify();
	}

	public void buzzOff() {
		buzzer.setState(PinState.LOW);
	}

	public void buzzOn() {
		buzzOff();
		buzzer.setState(PinState.HIGH);
	}

	public void buzzOn(long duration) {
		buzzOff();
		buzzer.pulse(duration);
	}

	public void beep() {
		buzzOff();
		buzzer.blink(DEFAULT_BEEP_DURATION);
	}

	public void beep(long duration) {
		buzzOff();
		buzzer.blink(duration);
	}

	public synchronized void shutdown() {
		shutdown = true;
		notify();
	}

	@Override
	public void run() {
		synchronized (this) {
			try {
				while (!shutdown) {
					try {
						if (!behaviours.isEmpty()) {
							switch (behaviours.poll()) {
							case MUTE:
								buzzOff();
								break;
							case BUZZ:
								buzzOn();
								break;
							case BEEP:
								beep();
								break;
							case DOUBLE_BEEP:
								buzzOn(DOUBLE_BEEP_DURATION);
								sleep(DOUBLE_BEEP_SLEEP / 2);
								buzzOn(DOUBLE_BEEP_DURATION);
								sleep(DOUBLE_BEEP_SLEEP);
								break;
							case ERROR:
								beep(ERROR_BEEP_DURATION);
								break;
							default:
								buzzOff();
								break;
							}
						}

						wait();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}
			} finally {
				buzzOff();
				buzzer.setShutdownOptions(true, PinState.LOW,
						PinPullResistance.OFF);
			}
		}
	}
}
