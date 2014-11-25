package commoninterface.neuralnetwork.outputs;

import java.util.Vector;

import commoninterface.AquaticDroneCI;
import commoninterface.CIFactory;
import commoninterface.utils.CIArguments;

public abstract class CINNOutput {
	
	public abstract int getNumberOfOutputValues();
	public abstract void setValue(int index, double value);
	public abstract void apply();
	
	public static Vector<CINNOutput> getNNOutputs(AquaticDroneCI robot, CIArguments arguments) {
		
		CIArguments outputs = new CIArguments(arguments.getArgumentAsString("cinnoutputs"));
		Vector<CINNOutput> nnOutputs = new Vector<CINNOutput>();
		
		for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
			CINNOutput nnOutput = createOutput(robot, outputs.getArgumentAt(i));
			nnOutputs.add(nnOutput);
		}

		return nnOutputs;
	}

	public static CINNOutput createOutput(AquaticDroneCI robot, String name) {
		switch (name) {
		case "TwoWheels":
			Class<?> wheelsOutputClass = TwoWheelCINNOutput.class;
			return (CINNOutput)CIFactory.getInstance(wheelsOutputClass.getName(), robot);
		default:
			return null;
		}

	}

}
