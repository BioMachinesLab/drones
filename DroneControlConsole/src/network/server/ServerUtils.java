package network.server;

import network.server.RobotServerLocation.DroneType;
import commoninterface.dataobjects.GPSData;
import commoninterface.entities.RobotLocation;
import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.NeuralActivationsMessage;

public class ServerUtils {
	public static GPSServerData getAsGGPSServerData(GPSData gpsData) {
		GPSServerData gpsServerData = new GPSServerData();

		gpsServerData.setLatitudeDecimal(gpsData.getLatitudeDecimal());
		gpsServerData.setLongitudeDecimal(gpsData.getLongitudeDecimal());

		if (gpsData.getLatitude() != null) {
			gpsServerData.setLatitude(gpsData.getLatitude());
		}

		if (gpsData.getLongitude() != null) {
			gpsServerData.setLongitude(gpsData.getLongitude());
		}

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

	public static BehaviorServerMessage getAsBehaviorServerMessage(
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

	public static RobotServerLocation getAsRobotServerLocation(
			RobotLocation robotLocation) {
		RobotServerLocation.DroneType droneType = null;
		switch (robotLocation.getDroneType()) {
		case DRONE:
			droneType = DroneType.DRONE;
			break;
		case ENEMY:
			droneType = DroneType.ENEMY;
			break;
		default:
			droneType = DroneType.OTHER;
			break;
		}
		return new RobotServerLocation(robotLocation.getName(), robotLocation
				.getLatLon().getLat(), robotLocation.getLatLon().getLon(),
				robotLocation.getOrientation(), droneType);
	}

	public static NeuralActivationsMessage getAsNeuralActivationsMessage(
			NeuralActivationsServerMessage neuralActivationsServerMessage) {
		return new NeuralActivationsMessage(
				neuralActivationsServerMessage.getInputsTitles(),
				neuralActivationsServerMessage.getInputsValues(),
				neuralActivationsServerMessage.getOutputsTitles(),
				neuralActivationsServerMessage.getOutputsValues(),
				neuralActivationsServerMessage.getHostname());
	}
}
