package commoninterface.utils.logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class LogCodex {
	public enum LogType {
		LOGDATA, ENTITIES, ERROR;
	}

	// Delimiters and Log Separators
	public static final String MAIN_SEPARATOR = "\t";
	public static final String ARRAY_SEPARATOR = ";";
	public static final String LINE_SEPARATOR = "\n";
	public static final String COMMENT_CHAR = "#";
	public static final String LOG_TYPE = "LT=";

	public static final String SENTENCE_SEP = "SN=";
	public static final String SENTENCE_DELIMITATOR = "\"";
	public static final String SENTENCE_ESCAPE = "\\" + SENTENCE_DELIMITATOR;

	// Drone information Separators
	public static final String LAT_LON_SEP = "LL=";
	public static final String GPS_ORIENT_SEP = "GO=";
	public static final String GPS_SPD = "GS=";
	public static final String GPS_TIME_SEP = "GT=";
	public static final String SYS_TIME_SEP = "ST=";
	public static final String COMP_ORIENT_SEP = "CO=";
	public static final String TEMP_SEP = "TE=";
	public static final String MOTORS_SPDS_SEP = "MS=";
	public static final String DRONE_TYPE_SEP = "DT=";

	// Control Informations Separators
	public static final String ENTITY_SEP = "#entity";
	public static final String IP_ADDR_SEP = "IP=";
	public static final String TIMESTEP_SEP = "TS=";
	public static final String NEURAL_NET_IN_SEP = "NI=";
	public static final String NEURAL_NET_OUT_SEP = "NO=";

	public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy_HH:mm:ss.SS");

	// Main methods
	public static String encodeLog(LogType type, Object object) {
		String data = "";

		switch (type) {
		case LOGDATA:
			data += LOG_TYPE + LogType.LOGDATA + MAIN_SEPARATOR;
			data += encodeLogData((LogData) object);
			break;
			
		case ENTITIES:
			data += LOG_TYPE + LogType.ENTITIES + MAIN_SEPARATOR;
			data += encodeEntities((ArrayList<Entity>) object);
			break;

		case ERROR:
			data += LOG_TYPE + LogType.ERROR + MAIN_SEPARATOR;
			data += SENTENCE_SEP + escapeComment((String) object);
			break;
		}

		data += LINE_SEPARATOR;
		return data;
	}

	public static DecodedLogData decodeLog(String logLine) {		
		String[] infoBlocks = logLine.split(MAIN_SEPARATOR);
		DecodedLogData decodedLogData = null;

		if (infoBlocks[0].substring(0, 3).equals(LOG_TYPE)) {
			LogType logType = LogType.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));

			switch (logType) {
			case ENTITIES:
				// TODO
				break;

			case LOGDATA:
				String[] blocks = new String[infoBlocks.length - 1];

				for (int i = 0; i < blocks.length; i++) {
					blocks[i] = infoBlocks[i + 1];
				}

				LogData logData = decodeLogData(blocks);
				decodedLogData = new DecodedLogData(LogType.LOGDATA, logData);
				break;
			case ERROR:
				if(infoBlocks[1].substring(0, 3).equals(SENTENCE_SEP)){
					String decodedSentence = unescapeComment(infoBlocks[1].substring(3, infoBlocks[1].length()));
					decodedLogData=new DecodedLogData(LogType.ERROR, decodedSentence);
				}
				break;
			}
			
			
			return decodedLogData;
		}else{
			return null;
		}
	}
	
	// Decoders
	private void decodeEntities(String line, ArrayList<Entity> entities) {
		Scanner s = new Scanner(line);
		s.next();// ignore first token

		String event = s.next();

		if (event.equals("added")) {

			String className = s.next();

			String name = s.next();

			if (className.equals(GeoFence.class.getSimpleName())) {

				GeoFence fence = new GeoFence(name);

				int number = s.nextInt();

				for (int i = 0; i < number; i++) {
					double lat = s.nextDouble();
					double lon = s.nextDouble();
					fence.addWaypoint(new LatLon(lat, lon));
				}
				entities.add(fence);
			} else if (className.equals(Waypoint.class.getSimpleName())) {

				double lat = s.nextDouble();
				double lon = s.nextDouble();
				Waypoint wp = new Waypoint(name, new LatLon(lat, lon));
				entities.remove(wp);
				entities.add(wp);

			} else if (className.equals(ObstacleLocation.class.getSimpleName())) {

				double lat = s.nextDouble();
				double lon = s.nextDouble();

				double radius = s.nextDouble();
				entities.add(new ObstacleLocation(name, new LatLon(lat, lon),
						radius));
			}

		} else if (event.equals("removed")) {

			String name = s.next();

			Iterator<Entity> i = entities.iterator();
			while (i.hasNext()) {
				if (i.next().getName().equals(name)) {
					i.remove();
					break;
				}
			}
		}

		s.close();
	}

	private static LogData decodeLogData(String[] infoBlocks){
		LogData logData = new LogData();
		
		for (int i = 0; i < infoBlocks.length; i++) {
			String information = infoBlocks[i].substring(3, infoBlocks[i].length());

			switch (infoBlocks[i].substring(0, 3)) {
			case SENTENCE_SEP:
				logData.comment = unescapeComment(information);
				break;

			case LAT_LON_SEP:
				String[] latLon = information.split(ARRAY_SEPARATOR);
				double lat = Double.parseDouble(latLon[0]);
				double lng = Double.parseDouble(latLon[1]);
				logData.latLon = new LatLon(lat, lng);
				break;

			case GPS_ORIENT_SEP:
				logData.GPSorientation = Double.parseDouble(information);
				break;

			case GPS_SPD:
				logData.GPSspeed = Double.parseDouble(information);
				break;

			case GPS_TIME_SEP:
				logData.GPSdate = dateTimeFormatter.parseDateTime(information);
				break;

			case SYS_TIME_SEP:
				logData.systemTime = information;
				break;

			case COMP_ORIENT_SEP:
				logData.compassOrientation = Double.parseDouble(information);
				break;

			case TEMP_SEP:
				String[] temps = information.split(ARRAY_SEPARATOR);
				logData.temperatures = new double[temps.length];

				for (int j = 0; j < temps.length; j++) {
					logData.temperatures[j] = Double.parseDouble(temps[j]);
				}
				break;

			case MOTORS_SPDS_SEP:
				String[] speeds = information.split(ARRAY_SEPARATOR);
				logData.motorSpeeds = new double[speeds.length];

				for (int j = 0; j < speeds.length; j++) {
					logData.motorSpeeds[j] = Double.parseDouble(speeds[j]);
				}
				break;

			case DRONE_TYPE_SEP:
				logData.droneType = AquaticDroneCI.DroneType.valueOf(information);
				break;

			case IP_ADDR_SEP:
				logData.ip = information;
				break;

			case TIMESTEP_SEP:
				logData.timestep = Integer.parseInt(information);
				break;

			case NEURAL_NET_IN_SEP:
				String[] neuralInputs = information.split(ARRAY_SEPARATOR);
				logData.inputNeuronStates = new double[neuralInputs.length];

				for (int j = 0; j < neuralInputs.length; j++) {
					logData.inputNeuronStates[j] = Double.parseDouble(neuralInputs[j]);
				}
				break;

			case NEURAL_NET_OUT_SEP:
				String[] neuralOutputs = information.split(ARRAY_SEPARATOR);
				logData.outputNeuronStates = new double[neuralOutputs.length];

				for (int j = 0; j < neuralOutputs.length; j++) {
					logData.outputNeuronStates[j] = Double.parseDouble(neuralOutputs[j]);
				}
				break;

			default:
				System.err.println("[LoggerCodex] Awkward tag appeared.... " + infoBlocks[i]);
				break;
			}
		}
		
		return logData;
	}
	
	// Coders
	private static String escapeComment(String str) {
		if (str.contains(SENTENCE_DELIMITATOR)) {
			str=str.replace(SENTENCE_DELIMITATOR, SENTENCE_ESCAPE);
		}
		
		return SENTENCE_DELIMITATOR+str+SENTENCE_DELIMITATOR;
	}
	
	private static String unescapeComment(String str) {
		if (str != null && str.length() > 0) {
			if (str.contains(SENTENCE_ESCAPE)) {
				str = str.replace(SENTENCE_ESCAPE, SENTENCE_DELIMITATOR);
			}

			if (str.length() <= 2) {
				return "";
			} else {
				return str.substring(1, str.length() - 2);
			}
		} else {
			return "";
		}
	}
	
	private static String encodeLogData(LogData logData){
		String data ="";
		data += (logData.ip != null) ? IP_ADDR_SEP + logData.ip + MAIN_SEPARATOR : "";
		data += (logData.timestep >= 0) ? TIMESTEP_SEP + Integer.toString(logData.timestep) + MAIN_SEPARATOR : "";
		data += (logData.droneType.name() != null) ? DRONE_TYPE_SEP	+ logData.droneType.name() + MAIN_SEPARATOR : "";
		data += (logData.systemTime != null) ? SYS_TIME_SEP	+ logData.systemTime + MAIN_SEPARATOR : "";

		if (logData.latLon != null) {
			data += LAT_LON_SEP + logData.latLon.getLat() + ARRAY_SEPARATOR;
			data += logData.latLon.getLon() + MAIN_SEPARATOR;
		}
		
		data += (logData.GPSorientation!=-1)?GPS_ORIENT_SEP + logData.GPSorientation + MAIN_SEPARATOR:"";
		data += (logData.GPSspeed>=0)?GPS_SPD + logData.GPSspeed + MAIN_SEPARATOR:"";
		data += (logData.GPSdate!=null)?GPS_TIME_SEP + logData.GPSdate.toString(dateTimeFormatter) + MAIN_SEPARATOR:"";
		data += (logData.compassOrientation>=0)?COMP_ORIENT_SEP + logData.compassOrientation + MAIN_SEPARATOR:"";

		if (logData.temperatures != null && logData.temperatures.length > 0) {
			data += TEMP_SEP;
			for (int i = 0; i < logData.temperatures.length; i++) {
				data += Double.toString(logData.temperatures[i]);
				if (i < logData.temperatures.length - 1)
					data += ARRAY_SEPARATOR;
			}
			data += MAIN_SEPARATOR;
		}
		
		if (logData.motorSpeeds != null && logData.motorSpeeds.length > 0) {
			data += MOTORS_SPDS_SEP;
			for (int i = 0; i < logData.motorSpeeds.length; i++) {
				data += Double.toString(logData.motorSpeeds[i]);
				if (i < logData.motorSpeeds.length - 1) {
					data += ARRAY_SEPARATOR;
				}
			}
			data += MAIN_SEPARATOR;
		}
		
		if (logData.inputNeuronStates != null && logData.inputNeuronStates.length > 0) {
			data += NEURAL_NET_IN_SEP;
			for (int i = 0; i < logData.inputNeuronStates.length; i++) {
				data += Double.toString(logData.inputNeuronStates[i]);
				if (i < logData.inputNeuronStates.length - 1) {
					data += ARRAY_SEPARATOR;
				}
			}
			data += MAIN_SEPARATOR;
		}

		if (logData.outputNeuronStates != null && logData.outputNeuronStates.length > 0) {
			data += NEURAL_NET_OUT_SEP;
			for (int i = 0; i < logData.outputNeuronStates.length; i++) {
				data += Double.toString(logData.outputNeuronStates[i]);
				if (i < logData.outputNeuronStates.length - 1) {
					data += ARRAY_SEPARATOR;
				}
			}
			data += MAIN_SEPARATOR;
		}
				
		data += (logData.comment != null) ? SENTENCE_SEP + escapeComment(logData.comment) : "";
		
		return data;
	}

	private static String encodeEntities(ArrayList<Entity> entities) {
		String data = "";

		// TODO
		
		return data;
	}
	
	// Data Classes
	public static class DecodedLogData {
		LogType payloadType;
		Object payload;

		public DecodedLogData(LogType payloadType, Object payload) {
			this.payload = payload;
			this.payloadType = payloadType;
		}
		
		public Object getPayload() {
			return payload;
		}
		
		public LogType payloadType() {
			return payloadType;
		}
	}
}
