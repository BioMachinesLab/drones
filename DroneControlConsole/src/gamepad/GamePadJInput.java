package gamepad;

import java.io.IOException;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Version;

public class GamePadJInput {
	private static final String GAMEPAD_NAME = "USB Gamepad";

	private Controller controller = null;

	private static final String X_IDENTIFIER = "x";
	private Component xAxis = null;

	private static final String Y_IDENTIFIER = "y";
	private Component yAxis = null;

	public void getControllerComponents() throws IOException {
		System.out.println("JInput version: " + Version.getVersion());
		System.out.println("");

		// Get a list of the controllers JInput
		Controller[] controllersList = ControllerEnvironment
				.getDefaultEnvironment().getControllers();

		for (int i = 0; i < controllersList.length; i++) {
			if (controllersList[i].getName().contains(GAMEPAD_NAME)) {
				controller = controllersList[i];

				System.out.println("Connected controller: ");

				Component[] components = controllersList[i].getComponents();

				System.out.println("Name: " + controllersList[i].getName());
				System.out.println("Type: "
						+ controllersList[i].getType().toString());
				System.out.println("Component count: " + components.length);
				System.out
						.println("-----------------------------------------------------------------");

				for (int j = 0; j < components.length; j++) {
					Component component = components[j];

					if (component.getIdentifier().getName()
							.contains(X_IDENTIFIER)) {
						xAxis = component;
						System.out.println("Found X");
					} else {
						if (component.getIdentifier().getName()
								.contains(Y_IDENTIFIER)) {
							yAxis = component;
							System.out.println("Found Y");
						}
					}
				}

				if (xAxis != null && yAxis != null) {

				} else {
					throw new IOException(
							"Unable to find the desired components");
				}
			}
		}

		if (controller == null) {
			throw new IOException("The disired GamePad (" + GAMEPAD_NAME
					+ ") is not connected to PC or it was not found");
		}
	}

	/**
	 * Prints controllers components and its values.
	 * 
	 * @param controllerType
	 *            Desired type of the controller.
	 */
	public void pollComponentsValues() {
		while (true) {
			controller.poll();

			StringBuffer buffer = new StringBuffer();
			buffer.append(X_IDENTIFIER + "= ");
			if (xAxis.isAnalog()) {
				buffer.append(xAxis.getPollData());
			} else {
				if (xAxis.getPollData() == 1.0f) {
					buffer.append("On");
				} else {
					buffer.append("Off");
				}
			}

			buffer.append("   " + Y_IDENTIFIER + "= ");
			if (yAxis.isAnalog()) {
				buffer.append(yAxis.getPollData());
			} else {
				if (yAxis.getPollData() == 1.0f) {
					buffer.append("On");
				} else {
					buffer.append("Off");
				}
			}
			System.out.println(buffer.toString());

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
