package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class InformationRequest extends Message {
	
	public static enum MessageType {
		GPS, SYSTEM_STATUS, BATTERY, COMPASS, SYSTEM_INFO, TEMPERATURE, THYMIO_READINGS, CAMERA_CAPTURE, NEURAL_ACTIVATIONS, THYMIO_VIRTUAL_POSITION
	}

	private static final long serialVersionUID = 7787407138336393178L;
	private MessageType type;

	public InformationRequest(MessageType type) {
		super();
		this.type = type;
	}

	public MessageType getMessageTypeQuery() {
		return type;
	}

}
