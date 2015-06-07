package network.server.shared;

import java.net.InetAddress;
import java.net.UnknownHostException;

import network.server.shared.dataObjects.BatteryStatusServerData;

import commoninterface.dataobjects.BatteryStatus;
import commoninterface.dataobjects.GPSData;
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
				behaviorServerMessage.selectedStatus, getHostname());
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

	public static NeuralActivationsServerMessage getAsNeuralActivationsServerMessage(
			NeuralActivationsMessage neuralActivationsMessage) {
		return new NeuralActivationsServerMessage(
				neuralActivationsMessage.getInputsTitles(),
				neuralActivationsMessage.getInputsValues(),
				neuralActivationsMessage.getOutputsTitles(),
				neuralActivationsMessage.getOutputsValues(),
				neuralActivationsMessage.getSenderHostname());
	}

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	public static BatteryStatusServerData getAsBatteryStatusServerData(
			BatteryStatus batteryStatus) {
		return new BatteryStatusServerData(batteryStatus.getBatteryID(),
				batteryStatus.getCellsVoltages(),
				batteryStatus.getBatteryTemperature());
	}
}
