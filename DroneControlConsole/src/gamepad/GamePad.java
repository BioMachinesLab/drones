package gamepad;

import java.io.IOException;

public class GamePad extends Thread {
	GamePadJInput jinputJoystickTest;

	public GamePad() {
		try {
			jinputJoystickTest = new GamePadJInput();
			jinputJoystickTest.getControllerComponents();
			jinputJoystickTest.pollComponentsValues();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (jinputJoystickTest != null) {
			jinputJoystickTest.pollComponentsValues();

		}

	}

}