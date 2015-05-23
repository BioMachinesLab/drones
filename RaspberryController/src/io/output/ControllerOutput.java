package io.output;

public interface ControllerOutput {
	public void setValue(int index, double value);

	public int getNumberOfOutputs();

	public boolean isAvailable();
	
	public double getValue(int index);
}
