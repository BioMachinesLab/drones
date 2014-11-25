package io.output;

import java.io.IOException;

import network.messages.MotorMessage;
import utils.Math_Utils;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import dataObjects.MotorSpeeds;

public class ReversableESCManagerOutputV2 extends Thread implements
		ControllerOutput {

	private final static boolean DEBUG = false;

	private final static Pin SWITCH_PIN = RaspiPin.GPIO_13;
	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int CENTRAL_VALUE = 150;
	private final static int MIN_VALUE = 60;
	private final static int MAX_VALUE = 240;

	private final static int MIN_FW_VALUE = 165;
	private final static int MIN_BW_VALUE = 135;

	private int lValue = CENTRAL_VALUE;
	private int rValue = CENTRAL_VALUE;

	private MotorSpeeds speeds;
	private GpioPinDigitalOutput escSwitch;

	private boolean available = false;

	public ReversableESCManagerOutputV2(MotorSpeeds speeds,
			GpioController gpioController) {
		this.speeds = speeds;

		escSwitch = gpioController.provisionDigitalOutputPin(SWITCH_PIN, PinState.LOW);

		try {
			setRawValues(CENTRAL_VALUE, CENTRAL_VALUE);
			Thread.sleep(1000);
			escSwitch.high();
			Thread.sleep(10);

			available = true;
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public int getNumberOfOutputs() {
		return 2;
	}

	@Override
	public void setValue(int index, double value) {
		
		if (!available)
			return;
		
		int finalVal = CENTRAL_VALUE;
		
		if (value == 0) {
			finalVal = CENTRAL_VALUE;
		} else {
			if (value > 0) {
				finalVal = (int) (Math_Utils.map(value, 0, 1, MIN_FW_VALUE,
						MAX_VALUE));
			} else if (value < 0) {
				finalVal = (int) (Math_Utils.map(value, -1, 0, MIN_VALUE,
						MIN_BW_VALUE));
			}
		}
		
		if(index == 0)
			lValue = finalVal;
		else
			rValue = finalVal;
	}

	private void writeValueToESC() {
		// long time = System.currentTimeMillis();
		try {
			try {
				Runtime.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + lValue
										+ " > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + rValue
										+ " > /dev/servoblaster;" }).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (DEBUG)
				System.out.println("[ESCManager] Wrote to motor L: " + lValue
						+ " R:" + rValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// System.out.println("Time to update motor "
		// + (System.currentTimeMillis() - time));
	}

	public void disableMotors() {
		escSwitch.low();
		setRawValues(CENTRAL_VALUE, CENTRAL_VALUE);
		writeValueToESC();
		available=false;
	}
	
	private void enableMotors(){
		setRawValues(CENTRAL_VALUE, CENTRAL_VALUE);
		writeValueToESC();
		escSwitch.high();
		available=true;
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
		if (!available){
			enableMotors();
		}
		
		setValue(0, m.getLeftMotor());
		setValue(1, m.getRightMotor());
		writeValueToESC();
	}

	private void setRawValues(int left, int right) {
		lValue = left;
		rValue = right;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public double getValue(int index) {
		if (index == 0) {
			return lValue;
		}
		return rValue;
	}
}
