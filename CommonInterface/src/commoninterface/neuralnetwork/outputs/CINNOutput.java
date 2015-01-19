package commoninterface.neuralnetwork.outputs;

import java.util.ArrayList;
import java.util.Vector;

import commoninterface.AquaticDroneCI;
import commoninterface.CIFactory;
import commoninterface.CISensor;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.utils.CIArguments;

public abstract class CINNOutput {
	
	protected AquaticDroneCI drone;
	
	public CINNOutput(AquaticDroneCI drone) {
		this.drone = drone;
	}
	
	public abstract int getNumberOfOutputValues();
	public abstract void setValue(int index, double value);
	public abstract void apply();
	
	public static Vector<CINNOutput> getNNOutputs(AquaticDroneCI robot, CIArguments arguments) {
		
		CIArguments outputs = new CIArguments(arguments.getArgumentAsString("outputs"));
		Vector<CINNOutput> nnOutputs = new Vector<CINNOutput>();
		
		for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
			CINNOutput nnOutput = createOutput(robot, new CIArguments(outputs.getArgumentAsString(outputs.getArgumentAt(i)),true));
			nnOutputs.add(nnOutput);
		}

		return nnOutputs;
	}
	
	public static CINNOutput createOutput(AquaticDroneCI robot, CIArguments args) {
		return (CINNOutput)CIFactory.getInstance(args.getArgumentAsString("classname"), robot);
	}


}
