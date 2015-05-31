package network.server.shared;

import java.util.ArrayList;

public class NeuralActivationsServerMessage {
	private ArrayList<String> inputsTitles;
	private ArrayList<Double[]> inputsValues;
	private ArrayList<String> outputsTitles;
	private ArrayList<Double[]> outputsValues;
	private String myHostname;

	public NeuralActivationsServerMessage() {
		inputsTitles = null;
		inputsValues = null;
		outputsTitles = null;
		outputsValues = null;
	}

	public NeuralActivationsServerMessage(ArrayList<String> inputsTitles,
			ArrayList<Double[]> inputsValues, ArrayList<String> outputsTitles,
			ArrayList<Double[]> outputsValues, String senderHostname) {
		this.inputsTitles = inputsTitles;
		this.inputsValues = inputsValues;
		this.outputsTitles = outputsTitles;
		this.outputsValues = outputsValues;
		this.myHostname=senderHostname;
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

	public String getHostname() {
		return myHostname;
	}
}
