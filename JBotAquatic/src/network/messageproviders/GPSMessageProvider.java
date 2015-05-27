package network.messageproviders;

import org.joda.time.LocalDateTime;
import commoninterface.AquaticDroneCI;
import commoninterface.dataobjects.GPSData;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.jcoord.LatLon;

public class GPSMessageProvider implements MessageProvider{
	
	private AquaticDroneCI drone;
	
	public GPSMessageProvider(AquaticDroneCI drone) {
		this.drone = drone;
	}

	@Override
	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery() == InformationRequest.MessageType.GPS) {

			GPSData data = new GPSData();
			
			LatLon latLon = drone.getGPSLatLon();
			data.setLatitudeDecimal(latLon.getLat());
			data.setLongitudeDecimal(latLon.getLon());
			data.setDate(new LocalDateTime());
			data.setFix(true);
			
			return new GPSMessage(data,drone.getNetworkAddress());
		}

		return null;
	}

}
