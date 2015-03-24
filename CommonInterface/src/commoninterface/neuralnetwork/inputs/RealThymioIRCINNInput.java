package commoninterface.neuralnetwork.inputs;

import commoninterface.CISensor;
import commoninterface.utils.CIArguments;

public class RealThymioIRCINNInput extends CINNInput {

	static double sensorAverages[] = { 4513.437143, 4539.878571, 4527.204286, 4499.942857, 4466.281429, 4312.424286, 3868.898571, 3473.807143, 3097.858571, 2795.664286, 2623.207143, 2398.692857, 2213.665714, 2029.824286, 1856.642857, 1672.965714, 1167.87, 983.0885714, 507.1414286 };
	
	static double distances[] = { 0, 0.5, 1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
	
	public RealThymioIRCINNInput(CISensor s, CIArguments args) {
		super(s, args);
	}

	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {

		if(index >= getNumberOfInputValues())
			throw new RuntimeException("[ThymioIRCINNInput] Invalid number of input index!");
		
		return sensorToInput(sensor.getSensorReading(index), index);
	}

	double sensorToInput(double value, int sensorNumber) {

		if (value > sensorAverages[sensorAverages.length - 1]) {

			int i;
			for (i = 0; i < sensorAverages.length; i++) {
				if (value > sensorAverages[i]) {
					if (i == 0)
						return 1.0;

					double referenceBefore = sensorAverages[i - 1];
					double referenceAfter = sensorAverages[i];

					double linearization = (value - referenceAfter) / (referenceBefore - referenceAfter);

					double distanceAfter = distances[i];
					double distanceBefore = distances[i - 1];
					double currentDistance = (distanceBefore - distanceAfter) * (linearization) + distanceAfter;

					double currentValue = 1.0 - currentDistance / distances[sensorAverages.length - 1];
					currentValue = Math.min(currentValue, 1);

					return currentValue;
				}
			}
		}
		return 0.0;
	}
	
}
