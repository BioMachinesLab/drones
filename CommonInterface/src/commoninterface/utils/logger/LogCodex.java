package commoninterface.utils.logger;

import java.util.ArrayList;
import java.util.Iterator;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.motion.LinearMotionData;
import commoninterface.entities.target.motion.MixedMotionData;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.entities.target.motion.RotationMotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.EntityManipulation.Operation;

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
	public static final String NULL_VALUE = "NULL";

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
	public static final String ENTITY_TYPE_SEP = "ET=";
	public static final char ENTITY_INFORMATION_BEGIN = '{';
	public static final char ENTITY_INFORMATION_END = '}';
	public static final String ENTITY_OP_SEP = "EO=";

	// Motion Data Informations Separators
	public static final String MOTION_DATA_ARGUMENTS_SEPARATOR = "!";
	public static final char MOTION_DATA_INFORMATION_BEGIN = '[';
	public static final char MOTION_DATA_INFORMATION_END = ']';
	public static final String MOTION_DATA_TYPE_SEP = "MDTS=";
	public static final String MOTION_DATA_POS_SEP = "MDPO=";
	public static final String MOTION_DATA_VELOCITY_SEP = "MDVL=";

	public static final String MOTION_DATA_ROTATION_DIRECTION_SEP = "MDRD=";
	public static final String MOTION_DATA_TRANSLATION_AZIMUTH_SEP = "MDTA=";

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

	@SuppressWarnings("unchecked")
	public static DecodedLog decodeLog(String logLine, Object... objs) {
		String[] infoBlocks = logLine.split(MAIN_SEPARATOR);
		DecodedLog decodedLog = null;

		if (logLine.startsWith("[") || logLine.startsWith("\"")) {
			System.out.println("Ignoring this line:" + logLine);
			return null;
		}

		if (infoBlocks[0].substring(0, 3).equals(LOG_TYPE)) {
			LogType logType = LogType.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));

			switch (logType) {
			case ENTITIES:
				if (infoBlocks.length == 5) {
					String[] blocks1 = new String[infoBlocks.length - 1];

					for (int i = 0; i < blocks1.length; i++) {
						blocks1[i] = infoBlocks[i + 1];
					}

					ArrayList<Entity> entities;
					if (objs.length > 0) {
						entities = (ArrayList<Entity>) objs[0];
					} else {
						entities = new ArrayList<Entity>();
					}
					double timeStep = decodeEntities(blocks1, entities);

					ArrayList<Entity> currentEntities = new ArrayList<Entity>();
					currentEntities.addAll(entities);

					decodedLog = new DecodedLog(LogType.ENTITIES, currentEntities, timeStep);
				}
				break;

			case LOGDATA:
				String[] blocks2 = new String[infoBlocks.length - 1];

				for (int i = 0; i < blocks2.length; i++) {
					blocks2[i] = infoBlocks[i + 1];
				}

				LogData logData = decodeLogData(blocks2);
				decodedLog = new DecodedLog(LogType.LOGDATA, logData, logData.timestep);
				break;
			case ERROR:
				if (infoBlocks[1].substring(0, 3).equals(SENTENCE_SEP)) {
					String decodedSentence = unescapeComment(infoBlocks[1].substring(3, infoBlocks[1].length()));
					decodedLog = new DecodedLog(LogType.ERROR, decodedSentence);
				}
				break;
			case MESSAGE:
				if (infoBlocks[1].substring(0, 3).equals(SENTENCE_SEP)) {
					String decodedSentence = unescapeComment(infoBlocks[1].substring(3, infoBlocks[1].length()));
					decodedLog = new DecodedLog(LogType.MESSAGE, decodedSentence);
				}
				break;
			}

			return decodedLog;
		} else {
			return null;
		}
	}

	// Decoders
	private static double decodeEntities(String[] blocks, ArrayList<Entity> entities) {
		double timeStep = -1;
		Operation event = null;
		String className = null;
		String data = null;

		for (String d : blocks) {
			String information = d.substring(3, d.length());
			switch (d.substring(0, 3)) {
			case ENTITY_TYPE_SEP:
				className = information;
				break;
			case TIMESTEP_SEP:
				timeStep = Double.parseDouble(information);
				break;
			case ENTITY_OP_SEP:
				event = Operation.valueOf(information);
				break;
			default:
				data = d;
				break;
			}
		}

		if (data != null && event != null && className != null) {
			decodeEntity(data, entities, event, className);
		}
		return timeStep;
	}

	private static void decodeEntity(String data, ArrayList<Entity> entities, Operation event, String className) {
		switch (event) {
		case MOVE:
		case ADD:
			if (className.equals(GeoFence.class.getSimpleName())) {
				String[] split = data.split("\\};\\{");
				GeoFence fence = new GeoFence("geofence");

				for (String s : split) {
					String str = s.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
					str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
					String[] splitArray = str.split(ARRAY_SEPARATOR);

					String name = splitArray[0];
					double lat = Double.parseDouble(splitArray[1]);
					double lon = Double.parseDouble(splitArray[2]);

					fence.addWaypoint(new Waypoint(name, new LatLon(lat, lon)));
				}

				entities.remove(fence);
				entities.add(fence);

			} else if (className.equals(Waypoint.class.getSimpleName())) {
				String str = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
				str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
				String[] split = str.split(ARRAY_SEPARATOR);

				String name = split[0];
				double lat = Double.parseDouble(split[1]);
				double lon = Double.parseDouble(split[2]);

				Waypoint wp = new Waypoint(name, new LatLon(lat, lon));
				entities.remove(wp);
				entities.add(wp);

			} else if (className.equals(ObstacleLocation.class.getSimpleName())) {
				String str = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
				str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
				String[] split = str.split(ARRAY_SEPARATOR);

				String name = split[0];
				double lat = Double.parseDouble(split[1]);
				double lon = Double.parseDouble(split[2]);
				double radius = Double.parseDouble(split[3]);

				ObstacleLocation ol = new ObstacleLocation(name, new LatLon(lat, lon), radius);
				entities.remove(ol);
				entities.add(ol);

			} else if (className.equals(RobotLocation.class.getSimpleName())) {
				String str = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
				str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
				String[] split = str.split(ARRAY_SEPARATOR);

				String name = split[0];
				double lat = Double.parseDouble(split[1]);
				double lon = Double.parseDouble(split[2]);
				double orientation = Double.parseDouble(split[3]);
				AquaticDroneCI.DroneType droneType = AquaticDroneCI.DroneType.valueOf(split[4]);

				RobotLocation rl = new RobotLocation(name, new LatLon(lat, lon), orientation, droneType);
				entities.remove(rl);
				entities.add(rl);

			} else if (className.equals(Target.class.getSimpleName())) {
				String str = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
				str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
				String[] split = str.split(ARRAY_SEPARATOR);

				String name = split[0];
				double lat = Double.parseDouble(split[1]);
				double lon = Double.parseDouble(split[2]);
				double radius = Double.parseDouble(split[3]);
				boolean inFormation = Boolean.parseBoolean(split[4]);
				boolean isOccupied = Boolean.parseBoolean(split[5]);
				String occupantID = split[6];

				if (occupantID.equals(LogCodex.NULL_VALUE)) {
					occupantID = null;
				}

				Target target = new Target(name, new LatLon(lat, lon), radius);
				target.setInFormation(inFormation);
				target.setOccupied(isOccupied);
				target.setOccupantID(occupantID);
				target.step(0);
				target.setMotionData(decodeMotionData(split[7], target));
				entities.remove(target);
				entities.add(target);

			} else if (className.equals(Formation.class.getSimpleName())) {
				Formation f = getFormation(data);
				entities.remove(f);
				entities.add(f);
			}
			break;
		case REMOVE:
			data = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
			data = data.replace(ENTITY_INFORMATION_END, ' ').trim();
			String[] split = data.split(ARRAY_SEPARATOR);

			Iterator<Entity> i = entities.iterator();
			whilecicle: while (i.hasNext()) {
				if (i.next().getName().equals(split[0])) {
					i.remove();
					break whilecicle;
				}
			}
			break;
		}
	}

	private static Formation getFormation(String data) {
		String str = data.replace(ENTITY_INFORMATION_BEGIN, ' ').trim();
		str = str.replace(ENTITY_INFORMATION_END, ' ').trim();
		String[] split = str.split(ARRAY_SEPARATOR);

		String formationName = split[0];
		double formationLat = Double.parseDouble(split[1]);
		double formationLon = Double.parseDouble(split[2]);
		int targetsQnt = Integer.parseInt(split[3]);
		FormationType formationType = FormationType.valueOf(split[4]);
		double lineFormationDelta = Double.parseDouble(split[6]);
		Vector2d arrowFormationDeltas = Vector2d.parseVector2d(split[7]);
		double circleFormationRadius = Double.parseDouble(split[8]);
		long randomSeed = Long.parseLong(split[9]);
		boolean variateFormationParameters = Boolean.parseBoolean(split[10]);
		double initialFormationRotation = Double.parseDouble(split[11]);
		double targetsRadius = Double.parseDouble(split[12]);
		double safetyDistance = Double.parseDouble(split[13]);
		double radiusOfObjPositioning = Double.parseDouble(split[14]);

		Formation f = new Formation(formationName, new LatLon(formationLat, formationLon));
		f.setLineFormationDelta(lineFormationDelta);
		f.setArrowFormationDeltas(arrowFormationDeltas);
		f.setCircleFormationRadius(circleFormationRadius);
		f.setRandomSeed(randomSeed);
		f.setVariateFormationParameters(variateFormationParameters);
		f.setInitialRotation(initialFormationRotation);
		f.setFormationType(formationType);
		f.setTargetRadius(targetsRadius);
		f.setSafetyDistance(safetyDistance);
		f.setRadiusOfObjPositioning(radiusOfObjPositioning);

		MotionData formationMotionData = decodeMotionData(split[5], f);
		f.setMotionData(formationMotionData);

		ArrayList<Target> targets = new ArrayList<Target>();
		for (int i = 0; i < targetsQnt; i++) {
			String name = split[15 + i * 8];
			double lat = Double.parseDouble(split[16 + i * 8]);
			double lon = Double.parseDouble(split[17 + i * 8]);
			double radius = Double.parseDouble(split[18 + i * 8]);
			boolean inFormation = Boolean.parseBoolean(split[19 + i * 8]);
			boolean isOccupied = Boolean.parseBoolean(split[20 + i * 8]);
			String occupantID = split[21 + i * 8];

			if (occupantID.equals(LogCodex.NULL_VALUE)) {
				occupantID = null;
			}

			Target target = new Target(name, new LatLon(lat, lon), radius);
			target.setInFormation(inFormation);
			target.setOccupied(isOccupied);
			target.setOccupantID(occupantID);
			target.setFormation(f);
			target.setMotionData(decodeMotionData(split[22 + i * 8], target));
			targets.add(target);
		}

		f.setTargets(targets);
		return f;
	}

	private static LogData decodeLogData(String[] infoBlocks) {
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

		} catch (Exception e) {
			System.err.println("Problem decoding. " + e.getMessage());
			return null;
		}

		return logData;
	}

	// Encoders
	private static String escapeComment(String str) {
		if (str.contains(SENTENCE_DELIMITATOR)) {
			str = str.replace(SENTENCE_DELIMITATOR, SENTENCE_ESCAPE);
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

	private static String encodeLogData(LogData logData) {
		String data = "";
		data += (logData.ip != null) ? IP_ADDR_SEP + logData.ip + MAIN_SEPARATOR : "";
		data += (logData.timestep >= 0) ? TIMESTEP_SEP + Integer.toString(logData.timestep) + MAIN_SEPARATOR : "";
		data += (logData.droneType.toString() != null) ? DRONE_TYPE_SEP + logData.droneType.toString() + MAIN_SEPARATOR
				: "";
		data += (logData.systemTime != null) ? SYS_TIME_SEP + logData.systemTime + MAIN_SEPARATOR : "";

		if (logData.latLon != null) {
			data += LAT_LON_SEP + logData.latLon.getLat() + ARRAY_SEPARATOR;
			data += logData.latLon.getLon() + MAIN_SEPARATOR;
		}

		data += (logData.GPSorientation != -1) ? GPS_ORIENT_SEP + logData.GPSorientation + MAIN_SEPARATOR : "";
		data += (logData.GPSspeed >= 0) ? GPS_SPD + logData.GPSspeed + MAIN_SEPARATOR : "";
		data += (logData.GPSdate != null) ? GPS_TIME_SEP + logData.GPSdate + MAIN_SEPARATOR : "";
		data += (logData.compassOrientation >= 0) ? COMP_ORIENT_SEP + logData.compassOrientation + MAIN_SEPARATOR : "";

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

		if (entities != null && !entities.getEntities().isEmpty()) {
			String className = entities.getEntitiesClass();

			data += ENTITY_OP_SEP + entities.getOperation() + MAIN_SEPARATOR;
			data += ENTITY_TYPE_SEP + className + MAIN_SEPARATOR;
			data += TIMESTEP_SEP + entities.getTimestep() + MAIN_SEPARATOR;

			ArrayList<Entity> ent = entities.getEntities();
			for (int i = 0; i < ent.size(); i++) {
				data += ENTITY_INFORMATION_BEGIN + ent.get(i).getName() + ARRAY_SEPARATOR;

				if (ent.get(i) instanceof GeoEntity) {
					data += ((GeoEntity) ent.get(i)).getLatLon().getLat() + ARRAY_SEPARATOR;
					data += ((GeoEntity) ent.get(i)).getLatLon().getLon();

					if (ent.get(i) instanceof ObstacleLocation) {
						data += ARRAY_SEPARATOR + ((ObstacleLocation) ent.get(i)).getRadius();

					} else if (ent.get(i) instanceof RobotLocation) {
						data += ARRAY_SEPARATOR + ((RobotLocation) ent.get(i)).getOrientation();
						data += ARRAY_SEPARATOR + ((RobotLocation) ent.get(i)).getDroneType();
					} else if (ent.get(i) instanceof Target) {
						data += ARRAY_SEPARATOR + ((Target) ent.get(i)).getRadius();
						data += ARRAY_SEPARATOR + ((Target) ent.get(i)).isInFormation();
						data += ARRAY_SEPARATOR + ((Target) ent.get(i)).isOccupied();

						if (((Target) ent.get(i)).getOccupantID() == null) {
							data += ARRAY_SEPARATOR + LogCodex.NULL_VALUE;
						} else {
							data += ARRAY_SEPARATOR + ((Target) ent.get(i)).getOccupantID();
						}

						data += ARRAY_SEPARATOR + encodeMotionData(((Target) ent.get(i)).getMotionData());
					} else if (ent.get(i) instanceof Formation) {
						Formation f = ((Formation) ent.get(i));

						data += ARRAY_SEPARATOR + f.getTargetQuantity();
						data += ARRAY_SEPARATOR + f.getFormationType();
						data += ARRAY_SEPARATOR + encodeMotionData(f.getMotionData());
						data += ARRAY_SEPARATOR + f.getLineFormationDelta();
						data += ARRAY_SEPARATOR + f.getArrowFormationDeltas();
						data += ARRAY_SEPARATOR + f.getCircleFormationRadius();
						data += ARRAY_SEPARATOR + f.getRandomSeed();
						data += ARRAY_SEPARATOR + f.getVariateFormationParameters();
						data += ARRAY_SEPARATOR + f.getInitialRotation();
						data += ARRAY_SEPARATOR + f.getTargetRadius();
						data += ARRAY_SEPARATOR + f.getSafetyDistance();
						data += ARRAY_SEPARATOR + f.getRadiusOfObjPositioning();

						for (Target t : f.getTargets()) {
							data += ARRAY_SEPARATOR + t.getName();
							data += ARRAY_SEPARATOR + t.getLatLon().getLat();
							data += ARRAY_SEPARATOR + t.getLatLon().getLon();
							data += ARRAY_SEPARATOR + t.getRadius();
							data += ARRAY_SEPARATOR + t.isInFormation();
							data += ARRAY_SEPARATOR + t.isOccupied();

							if (t.getOccupantID() == null) {
								data += ARRAY_SEPARATOR + NULL_VALUE;
							} else {
								data += ARRAY_SEPARATOR + t.getOccupantID();
							}
							data += ARRAY_SEPARATOR + encodeMotionData(t.getOwnMotionData());
						}
					}

					data += ENTITY_INFORMATION_END;
				}

				if (i != ent.size() - 1) {
					data += ARRAY_SEPARATOR;
				}
			}
		} else {
			System.out.println("Type not found... " + entities.getEntitiesClass());
		}

		return data;
	}

	public static String encodeMotionData(MotionData data) {
		String str = MOTION_DATA_INFORMATION_BEGIN + "";

		if (data != null) {
			str += MOTION_DATA_TYPE_SEP + data.getMotionType().toString();

			if (data instanceof LinearMotionData) {
				str += MOTION_DATA_ARGUMENTS_SEPARATOR + MOTION_DATA_TRANSLATION_AZIMUTH_SEP
						+ ((LinearMotionData) data).getMovementAzimuth();
				str += MOTION_DATA_ARGUMENTS_SEPARATOR + MOTION_DATA_VELOCITY_SEP
						+ ((LinearMotionData) data).getMovementVelocity();
			} else if (data instanceof RotationMotionData) {
				str += MOTION_DATA_ARGUMENTS_SEPARATOR + MOTION_DATA_POS_SEP
						+ ((RotationMotionData) data).getRotationCenter();
				str += MOTION_DATA_ARGUMENTS_SEPARATOR + MOTION_DATA_ROTATION_DIRECTION_SEP
						+ ((RotationMotionData) data).getRotationDirection();
				str += MOTION_DATA_ARGUMENTS_SEPARATOR + MOTION_DATA_VELOCITY_SEP
						+ ((RotationMotionData) data).getAngularVelocity();
			} else if (data instanceof MixedMotionData) {

			}

		} else {
			str += MOTION_DATA_TYPE_SEP + NULL_VALUE;
		}

		return str + MOTION_DATA_INFORMATION_END;
	}

	public static MotionData decodeMotionData(String data, GeoEntity entity) {
		String[] args = data.replace(MOTION_DATA_INFORMATION_BEGIN, ' ').replace(MOTION_DATA_INFORMATION_END, ' ')
				.trim().split(MOTION_DATA_ARGUMENTS_SEPARATOR);

		if (args.length == 1 && args[0].substring(5, args[0].length()).equals(NULL_VALUE)) {
			return null;
		}

		MovementType motionDataType = null;
		LatLon position = null;
		double velocity = 0;
		double translationAzimuth = 0;
		boolean rotationDirection = false;
		for (String arg : args) {
			String information = arg.substring(5, arg.length());
			switch (arg.substring(0, 5)) {
			case MOTION_DATA_TYPE_SEP:
				motionDataType = MovementType.valueOf(information);
				break;

			case MOTION_DATA_POS_SEP:
				String[] latLon = information.replace("(", "").replace(")", "").split(",");
				double lat = Double.parseDouble(latLon[0]);
				double lon = Double.parseDouble(latLon[1]);
				position = new LatLon(lat, lon);
				break;

			case MOTION_DATA_VELOCITY_SEP:
				velocity = Double.parseDouble(information);
				break;

			case MOTION_DATA_ROTATION_DIRECTION_SEP:
				rotationDirection = Boolean.parseBoolean(information);
				break;

			case MOTION_DATA_TRANSLATION_AZIMUTH_SEP:
				translationAzimuth = Double.parseDouble(information);
				break;
			}
		}

		if (motionDataType != null && entity != null) {
			MotionData motionData = null;
			switch (motionDataType) {
			case LINEAR:
				motionData = new LinearMotionData(entity, velocity, translationAzimuth);
				break;
			case ROTATIONAL:
				motionData = new RotationMotionData(entity, position, velocity, rotationDirection);
				break;
			case MIXED:
				throw new IllegalArgumentException("Not implemented!");
			}

			return motionData;
		} else {
			return null;
		}
	}
}
