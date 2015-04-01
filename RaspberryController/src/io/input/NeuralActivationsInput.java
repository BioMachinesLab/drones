package io.input;

import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.NeuralActivationsMessage;
import simpletestbehaviors.ControllerCIBehavior;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.neuralnetwork.inputs.CINNInput;

public class NeuralActivationsInput implements ControllerInput, MessageProvider {

	private ControllerCIBehavior controllerBehavior;
	private CINeuralNetwork neuralNetwork;

	public NeuralActivationsInput(ControllerCIBehavior controllerBehavior) {
		this.controllerBehavior = controllerBehavior;
		neuralNetwork = controllerBehavior.getNeuralNetwork();
	}
	
	@Override
	public Message getMessage(Message request) {
		if(request instanceof InformationRequest && ((InformationRequest)request).getMessageTypeQuery().equals(InformationRequest.MessageType.NEURAL_ACTIVATIONS)){
			return (NeuralActivationsMessage) getReadings();
		}
		
		return null;
	}

	@Override
	public Object getReadings() {
		String[] inputsTitles = new String[neuralNetwork.getInputs().size()];
		
		for (int i = 0; i < neuralNetwork.getInputs().size(); i++) 
			inputsTitles[i] = neuralNetwork.getInputs().get(i).getClass().getSimpleName();
		
		double[] inputsValues = neuralNetwork.getInputNeuronStates();
		
		for (CINNInput input : neuralNetwork.getInputs()) {
			input.getClass().getSimpleName();
		}
		
		String[] outputsTitles = new String[neuralNetwork.getOutputs().size()];
		
		for (int i = 0; i < outputsTitles.length; i++)
			outputsTitles[i] = neuralNetwork.getOutputs().get(i).getClass().getSimpleName();
		
		double[] outputsValues = neuralNetwork.getOutputNeuronStates();
		
		return new NeuralActivationsMessage(controllerBehavior.getCurrentTimeStep(), inputsTitles, inputsValues, outputsTitles, outputsValues);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

}
