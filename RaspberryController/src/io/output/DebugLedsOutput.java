package io.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class DebugLedsOutput extends Thread implements ControllerOutput {
	private static final int NUM_LEDS = 1;
	private static final Pin[] LED_PINS = { RaspiPin.GPIO_03 };

	private static final int[] LEDS_TO_BLINK = { 0 };
	private static final int BLINK_DURATION = 250;
	private static final int SLEEP_DURATION = 250;

	private GpioController gpio;
	private GpioPinDigitalOutput[] ledsOutputPins;

	public DebugLedsOutput() {
		if (LED_PINS.length != NUM_LEDS) {
			throw new IllegalArgumentException(
					"The number of defined LED's doesn't match with the selected PINS");
		}

		gpio = GpioFactory.getInstance();
		ledsOutputPins = new GpioPinDigitalOutput[NUM_LEDS];

		for (int i = 0; i < NUM_LEDS; i++) {
			ledsOutputPins[i] = gpio.provisionDigitalOutputPin(LED_PINS[i]);
		}
	}

	@Override
	// if value==0 the led will be off, and on if value!=0
	public void setValue(int index, double value) {
		if (value != 0) {
			ledsOutputPins[index].setState(PinState.HIGH);
		} else {
			ledsOutputPins[index].setState(PinState.LOW);
		}
	}

	@Override
	public int getNumberOfOutputValues() {
		return NUM_LEDS;
	}

	public GpioController getGpioController() {
		return gpio;
	}

	public void blinkLed(int index, long duration) {
		ledsOutputPins[index].blink(duration);
	}

	public void shutdownGpio() {
		for (int i = 0; i < LED_PINS.length; i++) {
			ledsOutputPins[i].setShutdownOptions(true, PinState.LOW,
					PinPullResistance.OFF);
		}

		gpio.shutdown();
	}

	@Override
	public void run() {
		while (true) {
			for (int i = 0; i < LEDS_TO_BLINK.length; i++) {
				blinkLed(LEDS_TO_BLINK[i], BLINK_DURATION);
			}

			try {
				sleep(SLEEP_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
