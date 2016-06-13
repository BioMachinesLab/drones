package commoninterface.neuralnetwork.inputs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import commoninterface.CIFactory;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public abstract class CINNInput implements Serializable {
	private static final long serialVersionUID = 4600272381367575839L;
	protected CISensor sensor;
	protected String label;

	public CINNInput(CISensor s, CIArguments args) {
		this.sensor = s;
	}

	public abstract int getNumberOfInputValues();

	public abstract double getValue(int index);

	public static Vector<CINNInput> getNNInputs(RobotCI robot, CIArguments args) {
		Vector<CINNInput> nnInputs = new Vector<CINNInput>();
		CIArguments inputs = new CIArguments(args.getArgumentAsString("inputs"));

		for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
			CINNInput nnInput = createInput(robot,
					new CIArguments(inputs.getArgumentAsString(inputs.getArgumentAt(i)), true));
			nnInputs.add(nnInput);
		}

		return nnInputs;
	}

	public static CINNInput createInput(RobotCI robot, CIArguments args) {
		int id = 0;

		if (args.getArgumentIsDefined("id"))
			id = args.getArgumentAsInt("id");

		ArrayList<CISensor> sensors = robot.getCISensors();

		for (CISensor s : sensors) {
			if (s.getId() == id) {
				CINNInput input = (CINNInput) CIFactory.getInstance(args.getArgumentAsString("classname"), s, args);
				String label = args.getArgumentAsStringOrSetDefault("label", input.getClass().getSimpleName());
				input.setLabel(label);

				return input;
			}
		}

		return null;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}