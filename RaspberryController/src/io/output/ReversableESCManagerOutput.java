package io.output;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import commoninterface.dataobjects.MotorSpeeds;
import commoninterface.network.messages.MotorMessage;
import commoninterface.utils.MathUtils;

public class ReversableESCManagerOutput extends Thread implements
		ControllerOutput {

	private final static boolean DEBUG = false;

	private final static Pin SWITCH_PIN = RaspiPin.GPIO_13;
	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int CENTRAL_VALUE_LEFT = 150;
	private final static int CENTRAL_VALUE_RIGHT = 150;
	private final static int MIN_VALUE = 132;
	private final static int MAX_VALUE = 190;

	private final static int MIN_FW_VALUE = 155;
	private final static int MIN_BW_VALUE = 145;

	private int lValue = CENTRAL_VALUE_LEFT;
	private int rValue = CENTRAL_VALUE_RIGHT;
	
	private int prevLValue = CENTRAL_VALUE_LEFT;
	private int prevRValue = CENTRAL_VALUE_RIGHT;
	
	private double lReceivedValue = 0;
	private double rReceivedValue = 0;

	private MotorSpeeds speeds;
	private GpioPinDigitalOutput escSwitch;

	private boolean available = false;

	public ReversableESCManagerOutput(MotorSpeeds speeds, GpioController gpioController) {
		this.speeds = speeds;

		escSwitch = gpioController.provisionDigitalOutputPin(SWITCH_PIN, PinState.LOW);

		try {
			setRawValues(CENTRAL_VALUE_LEFT, CENTRAL_VALUE_RIGHT);
			writeValueToESC();
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
		
		int finalVal = index == 0 ? CENTRAL_VALUE_LEFT : CENTRAL_VALUE_RIGHT;
		
		if (value != 0) {
			if (value > 0) {
				finalVal = (int) (MathUtils.map(value, 0, 1, MIN_FW_VALUE,
						MAX_VALUE));
			} else if (value < 0) {
				finalVal = (int) (MathUtils.map(value, -1, 0, MIN_VALUE,
						MIN_BW_VALUE));
			}
		}
		
		if(index == 0) {
			lValue = finalVal;
			lReceivedValue = value;
		} else {
			rValue = finalVal;
			rReceivedValue = value;
		}
	}

	private void writeValueToESC() {
		// long time = System.currentTimeMillis();
		try {
			
			try {
				
				preventStuckMotors();
				
				//set the real speed
				Runtime.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + lValue
										+ " > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + rValue
										+ " > /dev/servoblaster;" }).waitFor();
				
				prevLValue = lValue;
				prevRValue = rValue;
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
	
	private void preventStuckMotors() {
		int addVal = 5;
		
		try {
			
			String str = "";
	
			if(prevLValue == CENTRAL_VALUE_LEFT && lValue-MIN_FW_VALUE <= addVal && lValue-MIN_FW_VALUE >= 0) {
				str+= "echo " + LEFT_ESC + "=" + 175
						+ " > /dev/servoblaster;";
				
			}
			if(prevRValue == CENTRAL_VALUE_RIGHT && rValue-MIN_FW_VALUE <= addVal && rValue-MIN_FW_VALUE >= 0) {
				str+= "echo " + RIGHT_ESC + "=" + 175
						+ " > /dev/servoblaster;";
			}
			
			if(!str.isEmpty()) {
				Runtime.getRuntime()
				.exec(new String[] {
						"bash",
						"-c",
						str}).waitFor();
				Thread.sleep(100);
			}
		
		} catch(Exception e){}
	}

	public void disableMotors() {
		escSwitch.low();
		setRawValues(CENTRAL_VALUE_LEFT, CENTRAL_VALUE_RIGHT);
		writeValueToESC();
		available=false;
	}
	
	private void enableMotors(){
		setRawValues(CENTRAL_VALUE_LEFT, CENTRAL_VALUE_RIGHT);
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
			return lReceivedValue;
		}
		return rReceivedValue;
	}
}
