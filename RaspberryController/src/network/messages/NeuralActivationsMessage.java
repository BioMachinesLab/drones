package network.messages;

public class NeuralActivationsMessage extends Message {

	private double timeStep;
	private String[] inputsTitles;
	private double[] inputsValues;
	private String[] outputsTitles;
	private double[] outputsValues;
	
	public NeuralActivationsMessage(double timeStep, String[] inputsTitles, double[] inputsValues, String[] outputsTitles, double[] outputsValues) {
		this.timeStep = timeStep;
		this.inputsTitles = inputsTitles;
		this.inputsValues = inputsValues;
		this.outputsTitles = outputsTitles;
		this.outputsValues = outputsValues;
	}
	
	public double getTimeStep() {
		return timeStep;
	}
	
	public String[] getInputsTitles() {
		return inputsTitles;
	}
	
	public double[] getInputsValues() {
		return inputsValues;
	}
	
	public String[] getOutputsTitles() {
		return outputsTitles;
	}
	
	public double[] getOutputsValues() {
		return outputsValues;
	}
	
}
