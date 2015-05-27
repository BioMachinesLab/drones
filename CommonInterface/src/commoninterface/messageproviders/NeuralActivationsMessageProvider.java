package commoninterface.messageproviders;

import java.util.ArrayList;
import commoninterface.RobotCI;
import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.NeuralActivationsMessage;

public class NeuralActivationsMessageProvider implements MessageProvider {

	private RobotCI robot;

	public NeuralActivationsMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {

		if (m instanceof InformationRequest
				&& ((InformationRequest) m).getMessageTypeQuery().equals(
						InformationRequest.MessageType.NEURAL_ACTIVATIONS)) {
			if (robot.getActiveBehavior() instanceof ControllerCIBehavior)
				return createNeuralActivationMessage();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private NeuralActivationsMessage createNeuralActivationMessage() {
		ArrayList<?>[] info = ((ControllerCIBehavior) robot.getActiveBehavior())
				.getNeuralNetworkActivations();

		ArrayList<String> inputsTitles = (ArrayList<String>) info[0];
		ArrayList<String> outputsTitles = (ArrayList<String>) info[1];
		ArrayList<Double[]> inputsValues = (ArrayList<Double[]>) info[2];
		ArrayList<Double[]> outputsValues = (ArrayList<Double[]>) info[3];

		return new NeuralActivationsMessage(inputsTitles, inputsValues,
				outputsTitles, outputsValues, robot.getNetworkAddress());
	}

}
