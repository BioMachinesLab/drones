package network.server;

import commoninterface.dataobjects.GPSData;
import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.BehaviorMessage;

public class ServerUtils {
	public static GPSServerData getAsGGPSServerData(GPSData gpsData) {
		GPSServerData gpsServerData = new GPSServerData();

		gpsServerData.setLatitudeDecimal(gpsData.getLatitudeDecimal());
		gpsServerData.setLongitudeDecimal(gpsData.getLongitudeDecimal());
		gpsServerData.setLatitude(gpsData.getLatitude());
		gpsServerData.setLongitude(gpsData.getLongitude());
		gpsServerData.setAltitude(gpsData.getAltitude());

		gpsServerData.setFix(gpsData.isFix());
		gpsServerData.setFixType(gpsData.getFixType());
		gpsServerData.setNumberOfSatellitesInView(gpsData
				.getNumberOfSatellitesInView());
		gpsServerData.setNumberOfSatellitesInUse(gpsData
				.getNumberOfSatellitesInUse());
		gpsServerData.setHDOP(gpsData.getHDOP());
		gpsServerData.setPDOP(gpsData.getPDOP());
		gpsServerData.setVDOP(gpsData.getVDOP());
		gpsServerData.setGPSSourceType(gpsData.getGPSSourceType());

		gpsServerData.setGroundSpeedKnts(gpsData.getGroundSpeedKnts());
		gpsServerData.setGroundSpeedKmh(gpsData.getGroundSpeedKmh());
		gpsServerData.setOrientation(gpsData.getOrientation());

		gpsServerData.setDate(gpsData.getDate().toString());

		return gpsServerData;
	}

	public static  BehaviorServerMessage getAsBehaviorServerMessage(
			BehaviorMessage message) {
		BehaviorServerMessage behaviorServerMessage = new BehaviorServerMessage();

		behaviorServerMessage.selectedBehavior = message.getSelectedBehavior();
		behaviorServerMessage.setArguments(message.getArguments());
		behaviorServerMessage.selectedStatus = message.getSelectedStatus();

		return behaviorServerMessage;
	}

	public static BehaviorMessage getAsBehaviorMessage(
			BehaviorServerMessage behaviorServerMessage) {
		return new BehaviorMessage(behaviorServerMessage.selectedBehavior,
				behaviorServerMessage.getArguments(),
				behaviorServerMessage.selectedStatus,
				NetworkUtils.getHostname());
	}

}
