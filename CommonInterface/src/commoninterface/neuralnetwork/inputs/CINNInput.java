package commoninterface.neuralnetwork.inputs;

import java.util.Vector;

import commoninterface.AquaticDroneCI;
import commoninterface.CIFactory;
import commoninterface.utils.CIArguments;

public abstract class CINNInput {
	
	public abstract int getNumberOfInputValues();
	public abstract double getValue(int index);
	
	public static Vector<CINNInput> getNNInputs(AquaticDroneCI robot, CIArguments args) {
		Vector<CINNInput> nnInputs = new Vector<CINNInput>();
		CIArguments inputs = new CIArguments(args.getArgumentAsString("cinninputs"));
		
		for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
			CINNInput nnInput = createInput(robot, inputs.getArgumentAt(i));
			nnInputs.add(nnInput);
		}

		return nnInputs;
	}

	public static CINNInput createInput(AquaticDroneCI robot, String name) {
		switch (name) {
		case "Compass":
			Class<?> compassInputClass = CompassCINNInput.class;
			return (CINNInput)CIFactory.getInstance(compassInputClass.getName(), robot);
		case "GPS":
			Class<?> gpsInputClass = GPSCINNInput.class;
			return (CINNInput)CIFactory.getInstance(gpsInputClass.getName(), robot);
		default:
			return null;
		}
	}

}