package network.server;

import java.util.ArrayList;

import commoninterface.network.messages.NeuralActivationsMessage;

public class NeuralActivationsServerMessage {
	private ArrayList<String> inputsTitles;
	private ArrayList<Double[]> inputsValues;
	private ArrayList<String> outputsTitles;
	private ArrayList<Double[]> outputsValues;

	public NeuralActivationsServerMessage(NeuralActivationsMessage message) {
		chewData(message);
	}

	public NeuralActivationsServerMessage() {
		inputsTitles = null;
		inputsValues = null;
		outputsTitles = null;
		outputsValues = null;
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

	public void chewData(NeuralActivationsMessage message) {
		this.inputsTitles = message.getInputsTitles();
		this.inputsValues = message.getInputsValues();
		this.outputsTitles = message.getOutputsTitles();
		this.outputsValues = message.getOutputsValues();
	}
	
	public NeuralActivationsMessage getAsNeuralActivationsMessage(){
		return new NeuralActivationsMessage(inputsTitles, inputsValues, outputsTitles, outputsValues);
	}
}
