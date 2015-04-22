package commoninterface.neuralnetwork.inputs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import commoninterface.CIFactory;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public abstract class CINNInput implements Serializable {
	
	protected CISensor sensor;
	
	public CINNInput(CISensor s, CIArguments args) {
		this.sensor = s;
	}
	
	public abstract int getNumberOfInputValues();
	public abstract double getValue(int index);
	
	public static Vector<CINNInput> getNNInputs(RobotCI robot, CIArguments args) {
		Vector<CINNInput> nnInputs = new Vector<CINNInput>();
		CIArguments inputs = new CIArguments(args.getArgumentAsString("inputs"));
		
		for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
			CINNInput nnInput = createInput(robot, new CIArguments(inputs.getArgumentAsString(inputs.getArgumentAt(i)), true));
			nnInputs.add(nnInput);
		}

		return nnInputs;
	}
	
	public static CINNInput createInput(RobotCI robot, CIArguments args) {
		int id = 0;
		
		if(args.getArgumentIsDefined("id"))
			id = args.getArgumentAsInt("id");
		
		ArrayList<CISensor> sensors = robot.getCISensors();
		
		for(CISensor s : sensors) {
			if(s.getId() == id) {
				return (CINNInput)CIFactory.getInstance(args.getArgumentAsString("classname"), s, args);
			}
		}
		
		return null;
	}

}