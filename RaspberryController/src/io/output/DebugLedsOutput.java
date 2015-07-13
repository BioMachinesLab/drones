package io.output;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class DebugLedsOutput extends Thread implements ControllerOutput {
	private static final Pin[] LED_PINS = { RaspiPin.GPIO_05 };

	private static final int BLINK_DURATION = 250;

	private GpioPinDigitalOutput[] ledsOutputPins;

	private boolean available = false;

	public DebugLedsOutput(GpioController gpioController) {
		ledsOutputPins = new GpioPinDigitalOutput[LED_PINS.length];

		for (int i = 0; i < LED_PINS.length; i++) {
			ledsOutputPins[i] = gpioController.provisionDigitalOutputPin(
					LED_PINS[i], PinState.LOW);
		}

		available = true;
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
	public int getNumberOfOutputs() {
		return LED_PINS.length;
	}

	public void addBlinkLed(int index) {
		// ledsOutputPins[index].blink(BLINK_DURATION);
		if (index < 0 || index > (LED_PINS.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(
					"[Debug Leds] Invalid LED index");
		} else {
			ledsOutputPins[index].blink(BLINK_DURATION, PinState.LOW);
		}
	}

	public void removeBlinkLed(int index) {
		if (index < 0 || index > (LED_PINS.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(
					"[Debug Leds] Invalid LED index");
		} else {
			ledsOutputPins[index].low();
		}
	}

	public synchronized void shutdownLeds() {
		for (int i = 0; i < LED_PINS.length; i++) {
			ledsOutputPins[i].low();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (this) {
					try {
						// for (int ledIndex : leds_to_blink) {
						// ledsOutputPins[ledIndex].blink(BLINK_DURATION);
						// }
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			for (int i = 0; i < LED_PINS.length; i++) {
				ledsOutputPins[i].setShutdownOptions(true, PinState.LOW,
						PinPullResistance.OFF);
			}
		}
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public double getValue(int index) {
		return ledsOutputPins[index].getState().getValue();
	}
}
