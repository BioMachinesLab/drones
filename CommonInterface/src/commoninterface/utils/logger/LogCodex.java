package commoninterface.utils.logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class LogCodex {
	public enum LogType {
		LOGDATA, ENTITIES, ERROR, MESSAGE;
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

	// Drone informations Separators
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
	public static final String IP_ADDR_SEP = "IP=";
	public static final String TIMESTEP_SEP = "TS=";
	public static final String NEURAL_NET_IN_SEP = "NI=";
	public static final String NEURAL_NET_OUT_SEP = "NO=";

	// Entities Informations Separators
	public static final String ENTITY_CLASS_SEP = "ET=";
	public static final String ENTITY_INFORMATION_BEGIN = "{";
	public static final String ENTITY_INFORMATION_END = "}";
	public static final String ENTITY_OP_SEP = "EO=";
	
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
			data += encodeEntities((EntityManipulation) object);
			break;

		case ERROR:
			data += LOG_TYPE + LogType.ERROR + MAIN_SEPARATOR;
			data += SENTENCE_SEP + escapeComment((String) object);
			break;
			
		case MESSAGE:
			data += LOG_TYPE + LogType.MESSAGE + MAIN_SEPARATOR;
			data += SENTENCE_SEP + escapeComment((String) object);
			break;
		}

		data += LINE_SEPARATOR;
		return data;
	}

	public static DecodedLog decodeLog(String logLine, Object... objs) {
		String[] infoBlocks = logLine.split(MAIN_SEPARATOR);
		DecodedLog decodedLog = null;
		
		if(logLine.startsWith("[") || logLine.startsWith("\"")) {
//			System.out.println("Ignoring this line:" +logLine);
			return null;
		}
		
		if (infoBlocks[0].substring(0, 3).equals(LOG_TYPE)) {
			LogType logType = LogType.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));

			switch (logType) {
			case ENTITIES:
				if (objs.length > 0 && infoBlocks.length == 4) {
					String[] blocks1 = new String[infoBlocks.length - 1];

					for (int i = 0; i < blocks1.length; i++) {
						blocks1[i] = infoBlocks[i + 1];
					}

					ArrayList<Entity> entities = (ArrayList<Entity>) objs[0];
					decodeEntities(blocks1, entities);
					
					ArrayList<Entity> currentEntities = new ArrayList<Entity>();
					currentEntities.addAll(entities);
					
					decodedLog = new DecodedLog(LogType.ENTITIES, currentEntities);
				}
				break;

			case LOGDATA:
				String[] blocks2 = new String[infoBlocks.length - 1];

				for (int i = 0; i < blocks2.length; i++) {
					blocks2[i] = infoBlocks[i + 1];
				}

				LogData logData = decodeLogData(blocks2);
				decodedLog = new DecodedLog(LogType.LOGDATA, logData);
				break;
			case ERROR:
				if(infoBlocks[1].substring(0, 3).equals(SENTENCE_SEP)){
					String decodedSentence = unescapeComment(infoBlocks[1].substring(3, infoBlocks[1].length()));
					decodedLog=new DecodedLog(LogType.ERROR, decodedSentence);
				}
				break;
			case MESSAGE:
				if(infoBlocks[1].substring(0, 3).equals(SENTENCE_SEP)){
					String decodedSentence = unescapeComment(infoBlocks[1].substring(3, infoBlocks[1].length()));
					decodedLog=new DecodedLog(LogType.MESSAGE, decodedSentence);
				}
				break;
			}
			
			
			return decodedLog;
		}else{
			return null;
		}
	}
	
	// Decoders
	private static void decodeEntities(String[] blocks, ArrayList<Entity> entities) {
		
		String event = blocks[0].split("=")[1];
		String className = blocks[1].split("=")[1];
		String data = blocks[2];
		
		if (event.equals("ADD")) {

			if (className.equals(GeoFence.class.getSimpleName())) {
				
				
				String[] split = data.split("\\};\\{");
				
				GeoFence fence = new GeoFence("geofence");
				
				for(String s : split) {
					
					Waypoint wp = getWaypoint(s);
					fence.addWaypoint(wp);
					
				}
				
				entities.remove(fence);
				entities.add(fence);
				
//				GeoFence fence = new GeoFence(name);
//
//				int number = s.nextInt();
//
//				for (int i = 0; i < number; i++) {
//					double lat = s.nextDouble();
//					double lon = s.nextDouble();
//					fence.addWaypoint(new LatLon(lat, lon));
//				}
//				entities.add(fence);
			} else if (className.equals(Waypoint.class.getSimpleName())) {
				
				Waypoint wp = getWaypoint(data);
				entities.remove(wp);
				entities.add(wp);

			} else if (className.equals(ObstacleLocation.class.getSimpleName())) {

				//TODO
//				double lat = s.nextDouble();
//				double lon = s.nextDouble();
//
//				double radius = s.nextDouble();
//				entities.add(new ObstacleLocation(name, new LatLon(lat, lon),
//						radius));
			}
//
		} else if (event.equals("REMOVE")) {

			data = data.replace('{',' ').trim();
			data = data.replace('}',' ').trim();
			String[] split = data.split(";");

			Iterator<Entity> i = entities.iterator();
			while (i.hasNext()) {
				if (i.next().getName().equals(split[0])) {
					i.remove();
					break;
				}
			}
		}
	
	}
	
	private static Waypoint getWaypoint(String s) {
		s = s.replace('{',' ').trim();
		s = s.replace('}',' ').trim();
		String[] split = s.split(";");

		String name = split[0];
		double lat = Double.parseDouble(split[1]);
		double lon = Double.parseDouble(split[2]);
		return new Waypoint(name, new LatLon(lat, lon));
	}
	
	private static LogData decodeLogData(String[] infoBlocks){

		LogData logData = new LogData();
		
		try {
		
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
					logData.GPSdate = information;
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
					return null;
				}
			}
		
		}catch(Exception e) {
			System.err.println("Problem decoding. "+e.getMessage());
			return null;
		}
		
		return logData;
	}
	
	// Coders
	private static String escapeComment(String str) {
		if (str.contains(SENTENCE_DELIMITATOR)) {
			str=str.replaceAll(SENTENCE_DELIMITATOR, SENTENCE_ESCAPE);
		}
		
		return SENTENCE_DELIMITATOR + str + SENTENCE_DELIMITATOR;
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
		
		data += (logData.GPSorientation != -1) ? GPS_ORIENT_SEP	+ logData.GPSorientation + MAIN_SEPARATOR : "";
		data += (logData.GPSspeed >= 0) ? GPS_SPD + logData.GPSspeed + MAIN_SEPARATOR : "";
		data += (logData.GPSdate != null) ? GPS_TIME_SEP + logData.GPSdate + MAIN_SEPARATOR : "";
		data += (logData.compassOrientation >= 0) ? COMP_ORIENT_SEP	+ logData.compassOrientation + MAIN_SEPARATOR : "";

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

	private static String encodeEntities(EntityManipulation entities) {
		String data = "";

		if(entities!=null && !entities.getEntities().isEmpty()){
			String className = entities.getEntitiesClass();
			
			data += ENTITY_OP_SEP + entities.operation() + MAIN_SEPARATOR;
			data += ENTITY_CLASS_SEP + className + MAIN_SEPARATOR;

			ArrayList<Entity> ent = entities.getEntities();
			for (int i = 0; i < ent.size(); i++) {
				data += ENTITY_INFORMATION_BEGIN + ent.get(i).getName()	+ ARRAY_SEPARATOR;
				
				if(ent.get(i) instanceof GeoEntity ){
					data += ((GeoEntity)ent.get(i)).getLatLon().getLat() + ARRAY_SEPARATOR;
					data += ((GeoEntity)ent.get(i)).getLatLon().getLon();
					
					if (ent.get(i) instanceof ObstacleLocation) {
						data += ARRAY_SEPARATOR + ((ObstacleLocation) ent.get(i)).getRadius();
						
					} else if (ent.get(i) instanceof RobotLocation) {
						data += ARRAY_SEPARATOR + ((RobotLocation) ent.get(i)).getOrientation();
						data += ARRAY_SEPARATOR	+ ((RobotLocation) ent.get(i)).getDroneType();
					}

					data += ENTITY_INFORMATION_END;
				}
				
				if (i != ent.size() - 1) {
					data += ARRAY_SEPARATOR;
				}
			}
		}else{
			System.out.println("Type not found... "+entities.getEntitiesClass());
		}
		
		return data;
	}
	
}
