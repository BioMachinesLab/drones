package commoninterface.neuralnetwork.outputs;

import java.io.Serializable;
import java.util.Vector;

import commoninterface.AquaticDroneCI;
import commoninterface.CIFactory;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public abstract class CINNOutput implements Serializable {
	
	protected RobotCI robot;
	
	public CINNOutput(RobotCI robot, CIArguments args) {
		this.robot = robot;
	}
	
	public abstract int getNumberOfOutputValues();
	public abstract void setValue(int index, double value);
	public abstract void apply();
	
	public static Vector<CINNOutput> getNNOutputs(RobotCI robot, CIArguments arguments) {
		
		CIArguments outputs = new CIArguments(arguments.getArgumentAsString("outputs"));
		Vector<CINNOutput> nnOutputs = new Vector<CINNOutput>();
		
		for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
			CINNOutput nnOutput = createOutput(robot, new CIArguments(outputs.getArgumentAsString(outputs.getArgumentAt(i)),true));
			nnOutputs.add(nnOutput);
		}

		return nnOutputs;
	}
	
	public static CINNOutput createOutput(RobotCI robot, CIArguments args) {
		return (CINNOutput)CIFactory.getInstance(args.getArgumentAsString("classname"), robot, args);
	}


}
