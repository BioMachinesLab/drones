package gamepad;

import java.io.IOException;
import java.util.Scanner;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public abstract class GamePadInput {
	private final static int CALIBRATION_UPDATE_DELAY = 5;

	private String gamePadName = null;
	private Controller controller = null;

	private String xIdentifier = null;
	private Component xAxis = null;
	protected float xAxisValue = -100;

	private String yIdentifier = null;
	private Component yAxis = null;
	protected float yAxisValue = -100;

	private String zIdentifier = null;
	private Component zAxis = null;
	protected float zAxisValue = -100;

	private String rzIdentifier = null;
	private Component rzAxis = null;
	protected float rzAxisValue = -100;

	protected double middleX = 0;
	protected double middleY = 0;
	protected double middleZ = 0;
	protected double middleRZ = 0;

	public GamePadInput(String gamePadName) {
		this.gamePadName = gamePadName;
	}

	public void getControllerComponents() throws IOException {
		// Get a list of the controllers JInput
		Controller[] controllersList = ControllerEnvironment
				.getDefaultEnvironment().getControllers();

		for (int i = 0; i < controllersList.length; i++) {
			
			//don't ask
			if (controllersList[i].getName().contains(gamePadName) || controllersList[i].getName().contains("倀䅌卙䅔䥔乏刨㌩䌠湯牴汯敬齲")) {
				controller = controllersList[i];
				Component[] components = controllersList[i].getComponents();

				for (int j = 0; j < components.length; j++) {
					Component component = components[j];

					String componentName = component.getIdentifier().getName();

					if (componentName.equals(xIdentifier)) {
						xAxis = component;
					} else {
						if (componentName.equals(yIdentifier)) {
							yAxis = component;
						} else {
							if (componentName.equals(zIdentifier)) {
								zAxis = component;
							} else {
								if (componentName.equals(rzIdentifier)) {
									rzAxis = component;
								}
							}
						}
					}

					if (xAxis != null && yAxis != null && zAxis != null
							&& rzAxis != null) {
						break;
					}
				}

				if (xAxis == null || yAxis == null || zAxis == null
						|| rzAxis == null) {
					throw new IOException(
							"Unable to find the desired components");
				}
			}
		}

		if (controller == null) {
			throw new IOException("The disired GamePad (" + gamePadName
					+ ") is not connected to PC or it was not found");
		}
	}

	public void pollComponentsValues() {
		
		controller.poll();

		if (xAxis.isAnalog()) {
			xAxisValue = xAxis.getPollData();
		} else {
			if (xAxis.getPollData() == 1.0f) {
				xAxisValue = 1;
			} else {
				xAxisValue = -1;
			}
		}

		if (yAxis.isAnalog()) {
			yAxisValue = -yAxis.getPollData();
		} else {
			if (yAxis.getPollData() == 1.0f) {
				yAxisValue = 1;
			} else {
				yAxisValue = -1;
			}
		}

		if (zAxis.isAnalog()) {
			zAxisValue = -zAxis.getPollData();
		} else {
			if (zAxis.getPollData() == 1.0f) {
				zAxisValue = 1;
			} else {
				zAxisValue = -1;
			}
		}

		if (rzAxis.isAnalog()) {
			rzAxisValue = -rzAxis.getPollData();
		} else {
			if (rzAxis.getPollData() == 1.0f) {
				rzAxisValue = 1;
			} else {
				rzAxisValue = -1;
			}
		}
	}

	public void calibrateJoystick() {
		Scanner scanner = new Scanner(System.in);
		boolean finished = false;

		System.out.println("Calibrating Gamepad!");
		System.out.println("Press enter to calibrate Left Joystick!");
		scanner.nextLine();
		System.out.println("Calibrating.....");

		System.out.println("Reading values, so move the joystick in circles. When finished, press enter!");

		int readedLeftSamples = 0;
		while (!finished) {
			pollComponentsValues();

			middleX += xAxisValue;
			middleY += yAxisValue;

			readedLeftSamples++;

			if (scanner.hasNextLine()) {
				finished = true;
				scanner.nextLine();
			} else {
				try {
					Thread.sleep(CALIBRATION_UPDATE_DELAY);
				} catch (InterruptedException e) {
					System.err.println("Error during the calibration process!");
					e.printStackTrace();
				}
			}
		}
		
		

		System.out.println("Press enter to calibrate Right Joystick!");
		scanner.nextLine();
		System.out.println("Reading values, so move the joystick in circles. When finished, press enter!");
		
		System.out.println("Calibrating.....");

		finished = false;
		int readedRightSamples = 0;
		while (!finished) {
			pollComponentsValues();

			middleZ += zAxisValue;
			middleRZ += rzAxisValue;

			readedRightSamples++;

			if (scanner.hasNextLine()) {
				finished = true;
				scanner.nextLine();
			} else {
				try {
					Thread.sleep(CALIBRATION_UPDATE_DELAY);
				} catch (InterruptedException e) {
					System.err.println("Error during the calibration process!");
					e.printStackTrace();
				}
			}
		}

		scanner.close();
		System.out.println("Calibration done!");

		middleX /= readedLeftSamples;
		middleY /= readedLeftSamples;
		middleZ /= readedRightSamples;
		middleRZ /= readedRightSamples;

		System.out.println("Results: ");
		System.out.println("protected double middleX= " + middleX+";");
		System.out.println("protected double middleY= " + middleY+";");
		System.out.println("protected double middleZ= " + middleZ+";");
		System.out.println("protected double middleRZ= " + middleRZ+";");
	}

	public void setXIdentifier(String xIdentifier) {
		this.xIdentifier = xIdentifier;
	}

	public void setYIdentifier(String yIdentifier) {
		this.yIdentifier = yIdentifier;
	}

	public void setZIdentifier(String zIdentifier) {
		this.zIdentifier = zIdentifier;
	}

	public void setRZIdentifier(String rzIdentifier) {
		this.rzIdentifier = rzIdentifier;
	}

	public double getMiddleX() {
		return middleX;
	}

	public void setMiddleX(double middleX) {
		this.middleX = middleX;
	}

	public double getMiddleY() {
		return middleY;
	}

	public void setMiddleY(double middleY) {
		this.middleY = middleY;
	}

	public double getMiddleZ() {
		return middleZ;
	}

	public void setMiddleZ(double middleZ) {
		this.middleZ = middleZ;
	}

	public double getMiddleRZ() {
		return middleRZ;
	}

	public void setMiddleRZ(double middleRZ) {
		this.middleRZ = middleRZ;
	}

	public abstract float getXAxisValue();

	public abstract float getYAxisValue();

	public abstract float getZAxisValue();

	public abstract float getRZAxisValue();
}
