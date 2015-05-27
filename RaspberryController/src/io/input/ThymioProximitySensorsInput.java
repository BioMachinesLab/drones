package io.input;

import io.ThymioIOManager;

import java.util.List;

import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;
import commoninterface.network.messages.ThymioReadingsMessage;

public class ThymioProximitySensorsInput implements ControllerInput,
		MessageProvider {

	private ThymioIOManager thymioIOManager;
	protected boolean available = false;

	public ThymioProximitySensorsInput(ThymioIOManager thymioIOManager) {
		this.thymioIOManager = thymioIOManager;
		available = true;
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery().equals(
						InformationRequest.MessageType.THYMIO_READINGS)) {
			if (!available) {
				return new SystemStatusMessage(
						"[Thymio Proximity Sensors] Unable to send Thymio Sensors data",
						thymioIOManager.getThymioCI().getNetworkAddress());
			}
			return new ThymioReadingsMessage(
					(List<Short>) thymioIOManager.getProximitySensorsReadings(),
					thymioIOManager.getThymioCI().getNetworkAddress());
		}
		return null;
	}

	@Override
	public Object getReadings() {
		return thymioIOManager.getProximitySensorsReadings();
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

}
