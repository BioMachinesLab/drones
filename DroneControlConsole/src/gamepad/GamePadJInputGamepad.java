package gamepad;


public class GamePadJInputGamepad extends GamePadInput {
	private static final String GAMEPAD_NAME = "USB Gamepad";

	private static final String X_IDENTIFIER = "x";
	private static final String Y_IDENTIFIER = "y";
	private static final String Z_IDENTIFIER = "z";
	private static final String RZ_IDENTIFIER = "rz";

	public GamePadJInputGamepad() {
		super(GAMEPAD_NAME);

		setXIdentifier(X_IDENTIFIER);
		setYIdentifier(Y_IDENTIFIER);
		setZIdentifier(Z_IDENTIFIER);
		setRZIdentifier(RZ_IDENTIFIER);
	}
}