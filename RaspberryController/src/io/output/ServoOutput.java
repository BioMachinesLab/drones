package io.output;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;

import commoninterface.dataobjects.MotorSpeeds;
import commoninterface.network.messages.MotorMessage;
import commoninterface.utils.MathUtils;

public class ServoOutput extends ReversableESCManagerOutputV2 {
	
	private final static boolean DEBUG = false;

	private final static int LEFT_ESC = 0;
	private final static int RIGHT_ESC = 1;

	private final static int CENTRAL_LEFT = 1519;
	private final static int CENTRAL_RIGHT = 1519;
	
	private final static int MAX = 200;

	private int lValue = CENTRAL_LEFT;
	private int rValue = CENTRAL_RIGHT;
	
	private double lReceivedValue = 0;
	private double rReceivedValue = 0;

	private MotorSpeeds speeds;

	private boolean available = false;

	public ServoOutput(MotorSpeeds speeds, GpioController gpioController) {
		super(speeds, gpioController);
		this.speeds = speeds;

		try {
			setRawValues(CENTRAL_LEFT, CENTRAL_RIGHT);
			writeValueToESC();
			Thread.sleep(10);

			available = true;
			
			new StopThread().start();
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
		
		int finalVal = index == 0 ? CENTRAL_LEFT : CENTRAL_RIGHT;
		
		if (value != 0) {
			if (value > 0) {
				finalVal = (int) (MathUtils.map(value, 0, 1, finalVal, finalVal+MAX));
			} else if (value < 0) {
				finalVal = (int) (MathUtils.map(value, -1, 0, finalVal-MAX, finalVal));
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
				Runtime.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"echo " + LEFT_ESC + "=" + lValue
										+ "us > /dev/servoblaster; echo "
										+ RIGHT_ESC + "=" + rValue
										+ "us > /dev/servoblaster;" }).waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (DEBUG)
				System.out.println("[ESCManager] Wrote to motor L: " + lValue
						+ " R:" + rValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disableMotors() {
		setRawValues(CENTRAL_LEFT, CENTRAL_RIGHT);
		writeValueToESC();
		available=false;
	}
	
	private void enableMotors(){
		setRawValues(CENTRAL_LEFT, CENTRAL_RIGHT);
		writeValueToESC();
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
		setValue(1, -m.getRightMotor());//the right servo is the other way around on the robot
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
	
	
	class StopThread extends Thread {
		
		public void run() {
			while(true) {
				
				boolean writeVals = false;
				
				if(lReceivedValue == 0) {
					if(lValue == CENTRAL_LEFT) {
						lValue = CENTRAL_LEFT+1;
					} else {
						lValue = CENTRAL_LEFT;
					}
					writeVals = true;
				}
				
				if(rReceivedValue == 0) {
					if(rValue == CENTRAL_RIGHT) {
						rValue = CENTRAL_RIGHT+1;
					} else {
						rValue = CENTRAL_RIGHT;
					}
					writeVals = true;
				}
				
				if(writeVals) {
					writeValueToESC();
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		}
	}
}
