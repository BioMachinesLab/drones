package gamepad;

import gui.GUI;

import java.io.IOException;

public class GamePad extends Thread {
	public static enum GamePadType {
		LOGITECH, GAMEPAD
	}

	private final static int HISTORY_SIZE = 10;
	private final static int UPDATE_DELAY = 7;
	private final static int MAXIMUM_SPEED = 25;

	private GUI gui;
	private GamePadInput jinputGamepad;
	private boolean enable = true;

	private int lastRightMotorSpeed = 0;
	private int lastLeftMotorSpeed = 0;

	public static void main(String[] args) {
		GamePad gamePad = new GamePad(null, GamePadType.GAMEPAD);
		gamePad.disable();
		gamePad.run();
	}

	public GamePad(GUI gui, GamePadType type) {
		try {
			this.gui = gui;

			if (type == GamePadType.LOGITECH) {
				jinputGamepad = new GamePadJInputLogitech();
			} else {
				jinputGamepad = new GamePadJInputGamepad();
			}

			jinputGamepad.getControllerComponents();
			jinputGamepad.pollComponentsValues();

			// jinputGamepad.calibrateJoystick();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (jinputGamepad != null) {
			// Implementation of a FIFO history of the read joystick values
			int index = 0;
			double[] readingsXHistory = new double[HISTORY_SIZE];
			double[] readingsRZHistory = new double[HISTORY_SIZE];
			double middleX = jinputGamepad.getMiddleX();
			double middleRZ = jinputGamepad.getMiddleRZ();

			while (true) {
				jinputGamepad.pollComponentsValues();
				if (index == HISTORY_SIZE)
					index = 0;

				readingsXHistory[index] = jinputGamepad.getXAxisValue();
				readingsRZHistory[index] = jinputGamepad.getRZAxisValue();

				double xValue = 0;
				double rzValue = 0;

				for (int i = 0; i < HISTORY_SIZE; i++) {
					xValue += readingsXHistory[i];
					rzValue += readingsRZHistory[i];
				}

				xValue /= HISTORY_SIZE;
				xValue = Math.round(xValue * 1E10) / 1E10;
				rzValue /= HISTORY_SIZE;
				rzValue = Math.round(rzValue * 1E10) / 1E10;

				xValue = (int) map(xValue, middleX, 0.02, 0, 2.02);
				rzValue = (int) map(rzValue, middleRZ, 0.02, 0, 2.01);

				index++;

				// System.out.println("X=" + xValue + " RZ=" + rzValue);
				// Linear Transformation
				// int leftMotorSpeed = (int) (rzValue - xValue);
				// int rightMotorSpeed = (int) (rzValue + xValue);

				// int leftMotorSpeed = (int) (rzValue - Math.exp(xValue *
				// 0.046)+1);
				// int rightMotorSpeed = (int) (rzValue + Math.exp(xValue *
				// 0.046)-1);

				int leftMotorSpeed = (int) (rzValue - ((xValue / 100) * rzValue));
				int rightMotorSpeed = (int) (rzValue + ((xValue / 100) * rzValue));

				leftMotorSpeed = (int) map(leftMotorSpeed, -100, 100,
						-MAXIMUM_SPEED, MAXIMUM_SPEED);
				if (leftMotorSpeed > MAXIMUM_SPEED)
					leftMotorSpeed = MAXIMUM_SPEED;

				rightMotorSpeed = (int) map(rightMotorSpeed, -100, 100,
						-MAXIMUM_SPEED, MAXIMUM_SPEED);
				if (rightMotorSpeed > MAXIMUM_SPEED)
					rightMotorSpeed = MAXIMUM_SPEED;

				System.out.println("Left=" + leftMotorSpeed + " Right="
						+ rightMotorSpeed);

				if (enable
						&& (leftMotorSpeed != lastLeftMotorSpeed || rightMotorSpeed != lastRightMotorSpeed)) {

					gui.setMotorsValue(leftMotorSpeed, rightMotorSpeed);

					lastLeftMotorSpeed = leftMotorSpeed;
					lastRightMotorSpeed = rightMotorSpeed;
				}

				try {
					Thread.sleep(UPDATE_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private double map(double x, double in_min, double in_max, double out_min,
			double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	public synchronized void enable() {
		enable = true;
	}

	public synchronized void disable() {
		enable = false;
	}
}