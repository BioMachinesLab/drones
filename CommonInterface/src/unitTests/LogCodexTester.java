package unitTests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.Entity;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.Target;
import commoninterface.entities.target.motion.LinearMotionData;
import commoninterface.entities.target.motion.MotionData;
import commoninterface.entities.target.motion.MotionData.MovementType;
import commoninterface.entities.target.motion.RotationMotionData;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.EntityManipulation.Operation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;
import commoninterface.utils.logger.ToLogData;
import net.jafama.FastMath;

public class LogCodexTester {
	// Constants to be used
	private final int TIMESTEP = 10;
	private final String SENTENCE = "This is a tes\"t";
	private final AquaticDroneCI.DroneType DRONE_TYPE = AquaticDroneCI.DroneType.DRONE;

	private final String SYS_TIME = Integer.toString(9999);
	private final String IP_ADDR = "192.168.1.1";

	private final LatLon LATLON = new LatLon(38.749368, -9.153260);
	private final double GPS_ORIENT = 234.2131;
	private final double GND_SPEED = 1.1121313;
	private final String GPS_DATE = new DateTime(2015, 07, 8, 20, 00, 50).toString();
	private final double COMP_ORIENT = 351.00021;
	private final double[] TEMPS = new double[] { 40.131341, 21.2123 };

	private final double[] MOT_SPEEDS = new double[] { 0.999912212, 0.3313444131 };

	private final double[] INPUT_NN = new double[] { 1.12, 2.12, 3.13, 4.1241 };
	private final double[] OUTPUT_NN = new double[] { 5.12, 6.12, 7.13, 8.1241 };

	// Entities Constants
	private final String WP_NAME = "WP";
	private Waypoint WAYPOINT = new Waypoint(WP_NAME, LATLON);

	// private final String GEOFENC_NAME = "GEOFENCE";
	// private final GeoFence GEOFENCE = new GeoFence(GEOFENC_NAME);

	private final String OBST_NAME = "OBSTACLE";
	private final double OBST_RADIUS = 10.1102;
	private ObstacleLocation OBSTACLE = new ObstacleLocation(OBST_NAME, LATLON, OBST_RADIUS);

	private final String ROBOT_LOC_NAME = "ROBOT_LOCATION";
	private final double ROBOT_ORIENT = 139.221;
	private final DroneType ROBOT_TYPE = DroneType.DRONE;
	private RobotLocation ROBOT_LOC = new RobotLocation(ROBOT_LOC_NAME, LATLON, ROBOT_ORIENT, ROBOT_TYPE);

	private final String TARGET_NAME = "TARGET";
	private final double TARGET_RADIUS = 1212.1;
	private final boolean TARGET_BELONGSTOFORMATION = false;
	private final boolean TARGET_OCCUPIED = true;
	private final String TARGET_OCCUPANT_ID = "OCCUPANT";
	private Target TARGET = new Target(TARGET_NAME, LATLON, TARGET_RADIUS);
	private final double TARGET_MOTIONDATA_ANGVELOC = 1212.443;
	private final boolean TARGET_MOTIONDATA_DIRECTION = true;
	private MotionData TARGET_MOTIONDATA = new RotationMotionData(TARGET, LATLON, TARGET_MOTIONDATA_ANGVELOC,
			TARGET_MOTIONDATA_DIRECTION);

	private final String FORMATION_NAME = "FORMATION";
	private Formation FORMATION = new Formation(FORMATION_NAME, LATLON);
	private final int FORMATION_ROBOT_QNT = 2;
	private final double FORMATION_TARGET_RADIUS = 1;
	private final long FORMATION_RANDOM_SEED = 1;
	private final double FORMATION_INITIAL_ROTATION = 93.3;
	private final boolean FORMATION_VARIATE_PARAMETERS = false;
	private final double FORMATION_CIRCLE_RADIUS = 143.2;
	private final double FORMATION_LINE_DELTA = 10.0;
	private Vector2d FORMATION_ARROW_DELTAS = new Vector2d(123.1, 133.1);
	private final FormationType FORMATION_SHAPE = FormationType.line;
	private final double FORMATION_MOTIONDATA_VELOCITY = 10012.1;
	private final double FORMATION_MOTIONDATA_AZIMUTH = 121.1;
	private final double FORMATION_SAFETY_DISTANCE = 10.2;
	private final double FORMATION_RADIUS_OBJECTPOS = 11.2;
	private MotionData FORMATION_MOTIONDATA = new LinearMotionData(FORMATION, FORMATION_MOTIONDATA_VELOCITY,
			FORMATION_MOTIONDATA_AZIMUTH);

	// Expected Results
	private final String expectedLogDataString = LogCodex.LOG_TYPE + LogCodex.LogType.LOGDATA + LogCodex.MAIN_SEPARATOR
			+ LogCodex.IP_ADDR_SEP + IP_ADDR + LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + TIMESTEP
			+ LogCodex.MAIN_SEPARATOR + LogCodex.DRONE_TYPE_SEP + DRONE_TYPE + LogCodex.MAIN_SEPARATOR
			+ LogCodex.SYS_TIME_SEP + SYS_TIME + LogCodex.MAIN_SEPARATOR + LogCodex.LAT_LON_SEP + LATLON.getLat()
			+ LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.MAIN_SEPARATOR + LogCodex.GPS_ORIENT_SEP
			+ GPS_ORIENT + LogCodex.MAIN_SEPARATOR + LogCodex.GPS_SPD + GND_SPEED + LogCodex.MAIN_SEPARATOR
			+ LogCodex.GPS_TIME_SEP + GPS_DATE + LogCodex.MAIN_SEPARATOR + LogCodex.COMP_ORIENT_SEP + COMP_ORIENT
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TEMP_SEP + TEMPS[0] + LogCodex.ARRAY_SEPARATOR + TEMPS[1]
			+ LogCodex.MAIN_SEPARATOR + LogCodex.MOTORS_SPDS_SEP + MOT_SPEEDS[0] + LogCodex.ARRAY_SEPARATOR
			+ MOT_SPEEDS[1] + LogCodex.MAIN_SEPARATOR + LogCodex.NEURAL_NET_IN_SEP + INPUT_NN[0]
			+ LogCodex.ARRAY_SEPARATOR + INPUT_NN[1] + LogCodex.ARRAY_SEPARATOR + INPUT_NN[2] + LogCodex.ARRAY_SEPARATOR
			+ INPUT_NN[3] + LogCodex.MAIN_SEPARATOR + LogCodex.NEURAL_NET_OUT_SEP + OUTPUT_NN[0]
			+ LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[1] + LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[2]
			+ LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[3] + LogCodex.MAIN_SEPARATOR + LogCodex.SENTENCE_SEP
			+ LogCodex.SENTENCE_DELIMITATOR + SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE)
			+ LogCodex.SENTENCE_DELIMITATOR + LogCodex.LINE_SEPARATOR;

	private final String expectedErrorLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ERROR + LogCodex.MAIN_SEPARATOR
			+ LogCodex.SENTENCE_SEP + LogCodex.SENTENCE_DELIMITATOR
			+ SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE) + LogCodex.SENTENCE_DELIMITATOR
			+ LogCodex.LINE_SEPARATOR;

	private final String expectedMessageLogString = LogCodex.LOG_TYPE + LogCodex.LogType.MESSAGE
			+ LogCodex.MAIN_SEPARATOR + LogCodex.SENTENCE_SEP + LogCodex.SENTENCE_DELIMITATOR
			+ SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE) + LogCodex.SENTENCE_DELIMITATOR
			+ LogCodex.LINE_SEPARATOR;

	private final String expectedWaypointLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ENTITIES
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_TYPE_SEP + WAYPOINT.getClass().getSimpleName()
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + (double) TIMESTEP + LogCodex.MAIN_SEPARATOR
			+ LogCodex.ENTITY_INFORMATION_BEGIN + WP_NAME + LogCodex.ARRAY_SEPARATOR + LATLON.getLat()
			+ LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.ENTITY_INFORMATION_END + LogCodex.LINE_SEPARATOR;

	private final String expectedObstacleLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ENTITIES
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_TYPE_SEP + OBSTACLE.getClass().getSimpleName()
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + (double) TIMESTEP + LogCodex.MAIN_SEPARATOR
			+ LogCodex.ENTITY_INFORMATION_BEGIN + OBST_NAME + LogCodex.ARRAY_SEPARATOR + LATLON.getLat()
			+ LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.ARRAY_SEPARATOR + OBST_RADIUS
			+ LogCodex.ENTITY_INFORMATION_END + LogCodex.LINE_SEPARATOR;

	private final String expectedRobotLocationLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ENTITIES
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_TYPE_SEP + ROBOT_LOC.getClass().getSimpleName()
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + (double) TIMESTEP + LogCodex.MAIN_SEPARATOR
			+ LogCodex.ENTITY_INFORMATION_BEGIN + ROBOT_LOC_NAME + LogCodex.ARRAY_SEPARATOR + LATLON.getLat()
			+ LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.ARRAY_SEPARATOR + ROBOT_ORIENT
			+ LogCodex.ARRAY_SEPARATOR + DRONE_TYPE + LogCodex.ENTITY_INFORMATION_END + LogCodex.LINE_SEPARATOR;

	private final String expectedTargetLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ENTITIES
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD
			+ LogCodex.MAIN_SEPARATOR + LogCodex.ENTITY_TYPE_SEP + TARGET.getClass().getSimpleName()
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + (double) TIMESTEP + LogCodex.MAIN_SEPARATOR
			+ LogCodex.ENTITY_INFORMATION_BEGIN + TARGET_NAME + LogCodex.ARRAY_SEPARATOR + LATLON.getLat()
			+ LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.ARRAY_SEPARATOR + TARGET_RADIUS
			+ LogCodex.ARRAY_SEPARATOR + TARGET_BELONGSTOFORMATION + LogCodex.ARRAY_SEPARATOR + TARGET_OCCUPIED
			+ LogCodex.ARRAY_SEPARATOR + TARGET_OCCUPANT_ID + LogCodex.ARRAY_SEPARATOR
			+ LogCodex.encodeMotionData(TARGET_MOTIONDATA) + LogCodex.ENTITY_INFORMATION_END + LogCodex.LINE_SEPARATOR;

	private final String expectedNullMotionDataLogString = LogCodex.MOTION_DATA_INFORMATION_BEGIN
			+ LogCodex.MOTION_DATA_TYPE_SEP + LogCodex.NULL_VALUE + LogCodex.MOTION_DATA_INFORMATION_END;

	private final String expectedLinearMotionDataLogString = LogCodex.MOTION_DATA_INFORMATION_BEGIN
			+ LogCodex.MOTION_DATA_TYPE_SEP + MovementType.LINEAR + LogCodex.MOTION_DATA_ARGUMENTS_SEPARATOR
			+ LogCodex.MOTION_DATA_TRANSLATION_AZIMUTH_SEP + FORMATION_MOTIONDATA_AZIMUTH
			+ LogCodex.MOTION_DATA_ARGUMENTS_SEPARATOR + LogCodex.MOTION_DATA_VELOCITY_SEP
			+ FORMATION_MOTIONDATA_VELOCITY + LogCodex.MOTION_DATA_INFORMATION_END;

	private final String expectedRotationalMotionDataLogString = LogCodex.MOTION_DATA_INFORMATION_BEGIN
			+ LogCodex.MOTION_DATA_TYPE_SEP + MovementType.ROTATIONAL + LogCodex.MOTION_DATA_ARGUMENTS_SEPARATOR
			+ LogCodex.MOTION_DATA_POS_SEP + LATLON + LogCodex.MOTION_DATA_ARGUMENTS_SEPARATOR
			+ LogCodex.MOTION_DATA_ROTATION_DIRECTION_SEP + TARGET_MOTIONDATA_DIRECTION
			+ LogCodex.MOTION_DATA_ARGUMENTS_SEPARATOR + LogCodex.MOTION_DATA_VELOCITY_SEP + TARGET_MOTIONDATA_ANGVELOC
			+ LogCodex.MOTION_DATA_INFORMATION_END;

	private String expectedFormationLogString;

	private ToLogData codedData;

	@Before
	public void setUp() {
		codedData = new ToLogData();

		// Set data
		codedData.timestep = TIMESTEP;
		codedData.comment = SENTENCE;
		codedData.droneType = DRONE_TYPE;

		codedData.systemTime = SYS_TIME;
		codedData.ip = IP_ADDR;

		codedData.latLon = LATLON;
		codedData.GPSorientation = GPS_ORIENT;
		codedData.GPSspeed = GND_SPEED;

		codedData.GPSdate = GPS_DATE;
		codedData.compassOrientation = COMP_ORIENT;
		codedData.temperatures = TEMPS;

		codedData.motorSpeeds = MOT_SPEEDS;

		codedData.inputNeuronStates = INPUT_NN;
		codedData.outputNeuronStates = OUTPUT_NN;

		// Build log string
		expectedFormationLogString = LogCodex.LOG_TYPE + LogCodex.LogType.ENTITIES + LogCodex.MAIN_SEPARATOR
				+ LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD + LogCodex.MAIN_SEPARATOR
				+ LogCodex.ENTITY_TYPE_SEP + FORMATION.getClass().getSimpleName() + LogCodex.MAIN_SEPARATOR
				+ LogCodex.TIMESTEP_SEP + (double) TIMESTEP + LogCodex.MAIN_SEPARATOR;

		expectedFormationLogString += LogCodex.ENTITY_INFORMATION_BEGIN + FORMATION_NAME + LogCodex.ARRAY_SEPARATOR
				+ LATLON.getLat() + LogCodex.ARRAY_SEPARATOR + LATLON.getLon() + LogCodex.ARRAY_SEPARATOR
				+ FORMATION_ROBOT_QNT + LogCodex.ARRAY_SEPARATOR + FORMATION_SHAPE.toString() + LogCodex.ARRAY_SEPARATOR
				+ LogCodex.encodeMotionData(FORMATION_MOTIONDATA) + LogCodex.ARRAY_SEPARATOR;
		expectedFormationLogString += FORMATION_LINE_DELTA + LogCodex.ARRAY_SEPARATOR + FORMATION_ARROW_DELTAS
				+ LogCodex.ARRAY_SEPARATOR + FORMATION_CIRCLE_RADIUS + LogCodex.ARRAY_SEPARATOR + FORMATION_RANDOM_SEED
				+ LogCodex.ARRAY_SEPARATOR + FORMATION_VARIATE_PARAMETERS + LogCodex.ARRAY_SEPARATOR
				+ FORMATION_INITIAL_ROTATION + LogCodex.ARRAY_SEPARATOR + FORMATION_TARGET_RADIUS
				+ LogCodex.ARRAY_SEPARATOR + FORMATION_SAFETY_DISTANCE + LogCodex.ARRAY_SEPARATOR
				+ FORMATION_RADIUS_OBJECTPOS + LogCodex.ARRAY_SEPARATOR;

		double target_0_x = -(FORMATION_LINE_DELTA + FORMATION_TARGET_RADIUS) * (FORMATION_ROBOT_QNT / 2)
				+ (FORMATION_LINE_DELTA + FORMATION_TARGET_RADIUS) / 2;
		double target_1_x = (-(FORMATION_LINE_DELTA + FORMATION_TARGET_RADIUS) * (FORMATION_ROBOT_QNT / 2)
				+ (FORMATION_LINE_DELTA + FORMATION_TARGET_RADIUS) / 2) + FORMATION_LINE_DELTA
				+ FORMATION_TARGET_RADIUS;

		Vector2d[] positions = { new Vector2d(target_0_x, 0), new Vector2d(target_1_x, 0) };
		Vector2d[] transformedPositions = transformPositions(positions, FORMATION_INITIAL_ROTATION,
				CoordinateUtilities.GPSToCartesian(LATLON));

		LatLon target_0_pos = CoordinateUtilities.cartesianToGPS(transformedPositions[0]);
		LatLon target_1_pos = CoordinateUtilities.cartesianToGPS(transformedPositions[1]);

		expectedFormationLogString += "formation_target_0" + LogCodex.ARRAY_SEPARATOR + target_0_pos.getLat()
				+ LogCodex.ARRAY_SEPARATOR + target_0_pos.getLon() + LogCodex.ARRAY_SEPARATOR + FORMATION_TARGET_RADIUS
				+ LogCodex.ARRAY_SEPARATOR + true + LogCodex.ARRAY_SEPARATOR + false + LogCodex.ARRAY_SEPARATOR
				+ LogCodex.NULL_VALUE + LogCodex.ARRAY_SEPARATOR + LogCodex.encodeMotionData(TARGET_MOTIONDATA)
				+ LogCodex.ARRAY_SEPARATOR;
		expectedFormationLogString += "formation_target_1" + LogCodex.ARRAY_SEPARATOR + target_1_pos.getLat()
				+ LogCodex.ARRAY_SEPARATOR + target_1_pos.getLon() + LogCodex.ARRAY_SEPARATOR + FORMATION_TARGET_RADIUS
				+ LogCodex.ARRAY_SEPARATOR + true + LogCodex.ARRAY_SEPARATOR + false + LogCodex.ARRAY_SEPARATOR
				+ LogCodex.NULL_VALUE + LogCodex.ARRAY_SEPARATOR + LogCodex.encodeMotionData(TARGET_MOTIONDATA);
		expectedFormationLogString += LogCodex.ENTITY_INFORMATION_END + LogCodex.LINE_SEPARATOR;

		// Build formation
		FORMATION.setArrowFormationDeltas(FORMATION_ARROW_DELTAS);
		FORMATION.setCircleFormationRadius(FORMATION_CIRCLE_RADIUS);
		FORMATION.setLineFormationDelta(FORMATION_LINE_DELTA);
		FORMATION.setVariateFormationParameters(FORMATION_VARIATE_PARAMETERS);
		FORMATION.setRandomSeed(FORMATION_RANDOM_SEED);
		FORMATION.setInitialRotation(FORMATION_INITIAL_ROTATION);
		FORMATION.setMotionData(FORMATION_MOTIONDATA);
		FORMATION.setSafetyDistance(FORMATION_SAFETY_DISTANCE);
		FORMATION.setRadiusOfObjPositioning(FORMATION_RADIUS_OBJECTPOS);
		FORMATION.buildFormation(FORMATION_ROBOT_QNT, FORMATION_SHAPE, FORMATION_TARGET_RADIUS);

		for (Target t : FORMATION.getTargets()) {
			t.setMotionData(new RotationMotionData(t, FORMATION.getLatLon(), TARGET_MOTIONDATA_ANGVELOC,
					TARGET_MOTIONDATA_DIRECTION));
		}

		// Build target
		TARGET.setOccupied(TARGET_OCCUPIED);
		TARGET.setOccupantID(TARGET_OCCUPANT_ID);
		TARGET.setInFormation(TARGET_BELONGSTOFORMATION);
		TARGET.step(0);
		TARGET.setMotionData(TARGET_MOTIONDATA);
	}

	private Vector2d[] transformPositions(Vector2d[] positions, double rotationAngle, Vector2d translation) {
		Vector2d[] newPositions = new Vector2d[positions.length];

		double cos = FastMath.cos(rotationAngle);
		double sin = FastMath.sin(rotationAngle);
		for (int i = 0; i < positions.length; i++) {
			// Rotation given by a 2D rotation matrix:
			// x= x.cos(tetha) - y.sin(tetha)
			// Y= x.sin(tetha) + y.cos(tetha)
			newPositions[i] = new Vector2d();
			newPositions[i].x = positions[i].x * cos - positions[i].y * sin;
			newPositions[i].y = positions[i].x * sin + positions[i].y * cos;

			// Translation given by a 2D translation matrix:
			// x= x + dx
			// y= y + dy
			newPositions[i].x += translation.x;
			newPositions[i].y += translation.y;
		}

		return newPositions;
	}

	@After
	public void cleanMess() {
		// Build entities
		WAYPOINT = new Waypoint(WP_NAME, LATLON);
		OBSTACLE = new ObstacleLocation(OBST_NAME, LATLON, OBST_RADIUS);
		ROBOT_LOC = new RobotLocation(ROBOT_LOC_NAME, LATLON, ROBOT_ORIENT, ROBOT_TYPE);
		TARGET = new Target(TARGET_NAME, LATLON, TARGET_RADIUS);
		FORMATION = new Formation(FORMATION_NAME, LATLON);
		TARGET_MOTIONDATA = new RotationMotionData(TARGET, LATLON, TARGET_MOTIONDATA_ANGVELOC,
				TARGET_MOTIONDATA_DIRECTION);
		FORMATION_MOTIONDATA = new LinearMotionData(TARGET, FORMATION_MOTIONDATA_VELOCITY,
				FORMATION_MOTIONDATA_AZIMUTH);
	}

	@Test
	public void TestLogDataCodex() {
		String logStr = LogCodex.encodeLog(LogType.LOGDATA, codedData);
		assertEquals(expectedLogDataString, logStr);

		DecodedLog logDataDecoded = LogCodex.decodeLog(logStr);
		ToLogData fullDecodedLogData = ((ToLogData) logDataDecoded.getPayload()[0]);
		assertEquals(LogType.LOGDATA, logDataDecoded.getPayloadType());
		assertEquals(TIMESTEP, fullDecodedLogData.timestep);
		assertEquals(SENTENCE, fullDecodedLogData.comment);
		assertEquals(DRONE_TYPE, fullDecodedLogData.droneType);
		assertEquals(SYS_TIME, fullDecodedLogData.systemTime);
		assertEquals(IP_ADDR, fullDecodedLogData.ip);
		assertEquals(LATLON, fullDecodedLogData.latLon);
		assertTrue(GPS_ORIENT == fullDecodedLogData.GPSorientation);
		assertTrue(GND_SPEED == fullDecodedLogData.GPSspeed);
		assertEquals(GPS_DATE, fullDecodedLogData.GPSdate);
		assertTrue(COMP_ORIENT == fullDecodedLogData.compassOrientation);
		assertArrayEquals(TEMPS, fullDecodedLogData.temperatures, 0);
		assertArrayEquals(MOT_SPEEDS, fullDecodedLogData.motorSpeeds, 0);
		assertArrayEquals(INPUT_NN, fullDecodedLogData.inputNeuronStates, 0);
		assertArrayEquals(OUTPUT_NN, fullDecodedLogData.outputNeuronStates, 0);
		assertTrue(TIMESTEP == logDataDecoded.getTimeStep());
	}

	@Test
	public void TestErrorDataCodex() {
		String errStr = LogCodex.encodeLog(LogType.ERROR, SENTENCE);
		assertEquals(expectedErrorLogString, errStr);

		DecodedLog errMsgDecoded = LogCodex.decodeLog(errStr);
		String fullDecodedErrMsg = ((String) errMsgDecoded.getPayload()[0]);
		assertEquals(LogType.ERROR, errMsgDecoded.getPayloadType());
		assertEquals(SENTENCE, fullDecodedErrMsg);
	}

	@Test
	public void TestMessageDataCodex() {
		String messageStr = LogCodex.encodeLog(LogType.MESSAGE, SENTENCE);
		assertEquals(expectedMessageLogString, messageStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(messageStr);
		String fullDecodedMessageMsg = ((String) messageMsgDecoded.getPayload()[0]);
		assertEquals(LogType.MESSAGE, messageMsgDecoded.getPayloadType());
		assertEquals(SENTENCE, fullDecodedMessageMsg);
	}

	@Test
	public void TestWaypointDataCodex() {
		String wpStr = WAYPOINT.getLogMessage(Operation.ADD, (double) TIMESTEP);
		assertEquals(expectedWaypointLogString, wpStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(wpStr);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) messageMsgDecoded.getPayload()[0];

		assertEquals(1, entities.size());
		assertEquals(WAYPOINT.getClass().getSimpleName(), entities.get(0).getClass().getSimpleName());

		Waypoint w = (Waypoint) entities.get(0);
		assertEquals(LATLON, w.getLatLon());
		assertEquals(WP_NAME, w.getName());
	}

	@Test
	public void TestObstacleDataCodex() {
		String obstacleStr = OBSTACLE.getLogMessage(Operation.ADD, (double) TIMESTEP);
		assertEquals(expectedObstacleLogString, obstacleStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(obstacleStr);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) messageMsgDecoded.getPayload()[0];

		assertEquals(1, entities.size());
		assertEquals(OBSTACLE.getClass().getSimpleName(), entities.get(0).getClass().getSimpleName());

		ObstacleLocation ol = (ObstacleLocation) entities.get(0);
		assertEquals(LATLON, ol.getLatLon());
		assertEquals(OBST_NAME, ol.getName());
		assertTrue(OBST_RADIUS == ol.getRadius());
	}

	@Test
	public void TestRobotLocationDataCodex() {
		String robotLocationStr = ROBOT_LOC.getLogMessage(Operation.ADD, (double) TIMESTEP);
		assertEquals(expectedRobotLocationLogString, robotLocationStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(robotLocationStr);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) messageMsgDecoded.getPayload()[0];

		assertEquals(1, entities.size());
		assertEquals(ROBOT_LOC.getClass().getSimpleName(), entities.get(0).getClass().getSimpleName());

		RobotLocation rl = (RobotLocation) entities.get(0);
		assertEquals(LATLON, rl.getLatLon());
		assertEquals(ROBOT_LOC_NAME, rl.getName());
		assertTrue(ROBOT_ORIENT == rl.getOrientation());
		assertEquals(ROBOT_TYPE, rl.getDroneType());
	}

	@Test
	public void TestTargetDataCodex() {
		String targetStr = TARGET.getLogMessage(Operation.ADD, (double) TIMESTEP);
		assertEquals(expectedTargetLogString, targetStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(targetStr);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) messageMsgDecoded.getPayload()[0];

		assertEquals(1, entities.size());
		assertEquals(TARGET.getClass().getSimpleName(), entities.get(0).getClass().getSimpleName());

		Target t = (Target) entities.get(0);
		assertEquals(LATLON, t.getLatLon());
		assertEquals(TARGET_NAME, t.getName());
		assertTrue(TARGET_RADIUS == t.getRadius());
		assertEquals(TARGET_OCCUPIED, t.isOccupied());
		assertEquals(TARGET_OCCUPANT_ID, t.getOccupantID());
		assertEquals(TARGET_BELONGSTOFORMATION, t.isInFormation());
		assertEquals(TARGET_MOTIONDATA, t.getOwnMotionData());
		assertEquals(TARGET, t);
	}

	@Test
	public void TestFormationDataCodex() {
		String formationStr = FORMATION.getLogMessage(Operation.ADD, (double) TIMESTEP);
		assertEquals(expectedFormationLogString, formationStr);

		DecodedLog messageMsgDecoded = LogCodex.decodeLog(formationStr);
		@SuppressWarnings("unchecked")
		ArrayList<Entity> entities = (ArrayList<Entity>) messageMsgDecoded.getPayload()[0];

		assertEquals(1, entities.size());
		assertEquals(FORMATION.getClass().getSimpleName(), entities.get(0).getClass().getSimpleName());

		Formation f = (Formation) entities.get(0);
		assertEquals(LATLON, f.getLatLon());
		assertEquals(FORMATION_NAME, f.getName());
		assertEquals(FORMATION_ROBOT_QNT, f.getTargetQuantity());
		assertEquals(FORMATION_SHAPE, f.getFormationType());
		assertEquals(FORMATION_ROBOT_QNT, f.getTargets().size());
		assertTrue(FORMATION_TARGET_RADIUS == f.getTargets().get(0).getRadius());
		assertEquals(FORMATION_ARROW_DELTAS, f.getArrowFormationDeltas());
		assertTrue(FORMATION_CIRCLE_RADIUS == f.getCircleFormationRadius());
		assertTrue(FORMATION_LINE_DELTA == f.getLineFormationDelta());
		assertEquals(FORMATION_VARIATE_PARAMETERS, f.getVariateFormationParameters());
		assertEquals(FORMATION_RANDOM_SEED, f.getRandomSeed());
		assertTrue(FORMATION_INITIAL_ROTATION == f.getInitialRotation());
		assertTrue(FORMATION_SAFETY_DISTANCE == f.getSafetyDistance());
		assertTrue(FORMATION_RADIUS_OBJECTPOS == f.getRadiusOfObjPositioning());
		assertEquals(FORMATION, f);
		assertEquals(FORMATION_MOTIONDATA, f.getMotionData());
	}

	@Test
	public void TestMotionDataCodex() {
		// Coding test
		String nullMotionDataStr = LogCodex.encodeMotionData(null);
		assertEquals(expectedNullMotionDataLogString, nullMotionDataStr);

		String linearMotionDataStr = LogCodex.encodeMotionData(FORMATION_MOTIONDATA);
		assertEquals(expectedLinearMotionDataLogString, linearMotionDataStr);

		String rotationalMotionDataStr = LogCodex.encodeMotionData(TARGET_MOTIONDATA);
		assertEquals(expectedRotationalMotionDataLogString, rotationalMotionDataStr);

		// Decoding test
		MotionData nullMotionData = LogCodex.decodeMotionData(nullMotionDataStr, TARGET);
		assertEquals(null, nullMotionData);

		MotionData linearMotionData = LogCodex.decodeMotionData(linearMotionDataStr, FORMATION);
		assertEquals(MovementType.LINEAR, linearMotionData.getMotionType());
		assertEquals(LinearMotionData.class.getSimpleName(), linearMotionData.getClass().getSimpleName());
		assertEquals(FORMATION_MOTIONDATA, linearMotionData);

		assertEquals(FORMATION, linearMotionData.getEntity());
		assertTrue(FORMATION_MOTIONDATA_AZIMUTH == ((LinearMotionData) linearMotionData).getMovementAzimuth());
		assertTrue(FORMATION_MOTIONDATA_VELOCITY == ((LinearMotionData) linearMotionData).getMovementVelocity());

		MotionData rotationalMotionData = LogCodex.decodeMotionData(rotationalMotionDataStr, TARGET);
		assertEquals(MovementType.ROTATIONAL, rotationalMotionData.getMotionType());
		assertEquals(RotationMotionData.class.getSimpleName(), rotationalMotionData.getClass().getSimpleName());
		assertEquals(TARGET_MOTIONDATA, rotationalMotionData);

		assertEquals(TARGET, rotationalMotionData.getEntity());
		assertEquals(TARGET_MOTIONDATA_DIRECTION, ((RotationMotionData) rotationalMotionData).getRotationDirection());
		assertEquals(LATLON, ((RotationMotionData) rotationalMotionData).getRotationCenter());
		assertTrue(TARGET_MOTIONDATA_ANGVELOC == ((RotationMotionData) rotationalMotionData).getAngularVelocity());
	}
}
