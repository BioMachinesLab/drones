package commoninterface.messageproviders;

import commoninterface.ThymioCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.ThymioVirtualPositionMessage;

public class ThymioVirtualPositionMessageProvider implements MessageProvider {

	private ThymioCI thymio;
	private Vector2d unknownPosition;
	private double unknownOrientation;

	public ThymioVirtualPositionMessageProvider(ThymioCI thymio) {
		this.thymio = thymio;
		unknownPosition = new Vector2d(-1, -1);
		unknownOrientation = 0;
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.THYMIO_VIRTUAL_POSITION)) {
			if (thymio.getVirtualPosition() != null)
				return new ThymioVirtualPositionMessage(
						thymio.getVirtualPosition(),
						thymio.getVirtualOrientation(),
						thymio.getNetworkAddress());
			else
				return new ThymioVirtualPositionMessage(unknownPosition,
						unknownOrientation, thymio.getNetworkAddress());
		}
		return null;
	}

}
