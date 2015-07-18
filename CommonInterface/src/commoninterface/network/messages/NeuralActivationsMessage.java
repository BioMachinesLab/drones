package commoninterface.network.messages;

import java.util.ArrayList;

import commoninterface.network.messages.Message;

public class NeuralActivationsMessage extends Message {

	private ArrayList<String> inputsTitles;
	private ArrayList<Double[]> inputsValues;
	private ArrayList<String> outputsTitles;
	private ArrayList<Double[]> outputsValues;

	public NeuralActivationsMessage(ArrayList<String> inputsTitles,
			ArrayList<Double[]> inputsValues, ArrayList<String> outputsTitles,
			ArrayList<Double[]> outputsValues, String senderHostname) {
		super(senderHostname);
		this.inputsTitles = inputsTitles;
		this.inputsValues = inputsValues;
		this.outputsTitles = outputsTitles;
		this.outputsValues = outputsValues;
	}

	public ArrayList<String> getInputsTitles() {
		return inputsTitles;
	}

	public ArrayList<String> getOutputsTitles() {
		return outputsTitles;
	}

	public ArrayList<Double[]> getInputsValues() {
		return inputsValues;
	}

	public ArrayList<Double[]> getOutputsValues() {
		return outputsValues;
	}
	
	@Override
	public Message getCopy() {
		return new NeuralActivationsMessage(inputsTitles, inputsValues, outputsTitles, outputsValues, senderHostname);
	}

}
