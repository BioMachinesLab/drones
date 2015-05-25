package gamepad;

import java.io.IOException;

import main.DroneControlConsole;
import main.RobotControlConsole;

public class GamePad extends Thread {
	
	public static enum GamePadType {
		GAMEPAD, LOGITECH
	}

	private final static int HISTORY_SIZE = 20;
	private final static int MAXIMUM_SPEED = 100;

	private RobotControlConsole console;
	private GamePadInput jinputGamepad;

	private int lastRightMotorSpeed = 0;
	private int lastLeftMotorSpeed = 0;
	
	private boolean keepExecuting = true;
	
	private GamePadType type;
	
	public static void main(String[] args) throws IOException {
		GamePad gamePad = new GamePad(null);
		gamePad.run();
	}

	public GamePad(RobotControlConsole console) {
		
		for(GamePadType t : GamePadType.values()) {
			
			try {
				
				this.console = console;

				if (t == GamePadType.LOGITECH) {
					jinputGamepad = new GamePadJInputLogitech();
				} else {
					jinputGamepad = new GamePadJInputGamepad();
				}

				jinputGamepad.getControllerComponents();
				jinputGamepad.pollComponentsValues();
//				jinputGamepad.calibrateJoystick();
				
				//If we got up to here, it means that there was no problem
				type = t;
				break;
				
			} catch(Exception e ){}
			
		}
		
		if(type == null)
			System.err.println("Gamepad not available");
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
			
			boolean osX = isPlatformOsx();
			
			try {

				while (keepExecuting) {
					jinputGamepad.pollComponentsValues();
					if (index == HISTORY_SIZE)
						index = 0;
					
					readingsXHistory[index] = osX && type == GamePadType.LOGITECH ?
								-jinputGamepad.getXAxisValue():
								jinputGamepad.getXAxisValue();
					readingsRZHistory[index] = osX && type == GamePadType.GAMEPAD ?
								jinputGamepad.getZAxisValue() :
								jinputGamepad.getRZAxisValue();
	
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
	
					double left = rzValue;
					double right = rzValue;
	
					if (xValue > 0) {
						left *= Math.abs((100.0 - xValue) / 100.0);
					} else if (xValue < 0) {
						right *= Math.abs((100 + xValue) / 100.0);
					}
					
	
					int leftMotorSpeed = (int) left;
					int rightMotorSpeed = (int) right;
	
					if (leftMotorSpeed > MAXIMUM_SPEED) {
						leftMotorSpeed = MAXIMUM_SPEED;
					}
	
					if (rightMotorSpeed > MAXIMUM_SPEED) {
						rightMotorSpeed = MAXIMUM_SPEED;
					}
					
					if(Math.abs(leftMotorSpeed) < 5)
						leftMotorSpeed = 0;
					
					if(Math.abs(rightMotorSpeed) < 5)
						rightMotorSpeed = 0;
					
					if(Math.abs(leftMotorSpeed-lastLeftMotorSpeed) <= 2 && Math.abs(rightMotorSpeed-lastRightMotorSpeed) <= 2)
						continue;
						
					if(console != null)
						console.getGUI().getMotorsPanel().setSliderValues(leftMotorSpeed,rightMotorSpeed);
					lastLeftMotorSpeed = leftMotorSpeed;
					lastRightMotorSpeed = rightMotorSpeed;
						
				}
			} catch(Exception e) {}
		}
	}
	
	public int getLeftMotorSpeed() {
		return lastLeftMotorSpeed;
	}
	
	public int getRightMotorSpeed() {
		return lastRightMotorSpeed;
	}

	private double map(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	public static boolean isPlatformOsx() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().startsWith("mac os x");
    }
	
	public void stopExecuting() {
		keepExecuting = false;
	}
}