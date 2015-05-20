package gamepad;

public class GamePadJInputLogitech extends GamePadInput {
	private static final String GAMEPAD_NAME = "Logitech RumblePad 2 USB";

	private static final String X_IDENTIFIER = "x";
	private static final String Y_IDENTIFIER = "y";
	private static final String Z_IDENTIFIER = "z";
	private static final String RZ_IDENTIFIER = "rz";

	private static final double X_MIDDLE = -1.52587890625E-5;
	private static final double Y_MIDDLE = 1.52587890625E-5;
	private static final double Z_MIDDLE = 0.3962462544441223;
	private static final double RZ_MIDDLE = -3.0517578125E-5;

	public GamePadJInputLogitech() {
		super(GAMEPAD_NAME);

		setXIdentifier(X_IDENTIFIER);
		setYIdentifier(Y_IDENTIFIER);
		setZIdentifier(Z_IDENTIFIER);
		setRZIdentifier(RZ_IDENTIFIER);

		middleX = X_MIDDLE;
		middleY = Y_MIDDLE;
		middleZ = Z_MIDDLE;
		middleRZ = RZ_MIDDLE;
	}

	@Override
	public float getXAxisValue() {
		return xAxisValue;
	}

	@Override
	public float getYAxisValue() {
		return yAxisValue;
	}

	@Override
	public float getZAxisValue() {
		return zAxisValue;
	}

	@Override
	public float getRZAxisValue() {
		return rzAxisValue;
	}
}
