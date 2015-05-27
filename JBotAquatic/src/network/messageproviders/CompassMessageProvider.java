package network.messageproviders;

import commoninterface.AquaticDroneCI;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;

public class CompassMessageProvider implements MessageProvider {

	private AquaticDroneCI drone;

	public CompassMessageProvider(AquaticDroneCI drone) {
		this.drone = drone;
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery() == InformationRequest.MessageType.COMPASS) {

			return new CompassMessage(
					(int) drone.getCompassOrientationInDegrees(),
					drone.getNetworkAddress());
		}

		return null;
	}

}
