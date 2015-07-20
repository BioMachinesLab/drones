package unitTests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import commoninterface.AquaticDroneCI;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;
import commoninterface.utils.logger.LogData;

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
	private final Waypoint WAYPOINT = new Waypoint(WP_NAME, LATLON);

	private final String GEOFENC_NAME = "GEOFENCE";
	private final GeoFence GEOFENCE = new GeoFence(GEOFENC_NAME);

	private final String OBST_NAME = "OBSTACLE";
	private final double OBST_RADIUS = 10.1102;
	private final ObstacleLocation OBSTACLE = new ObstacleLocation(OBST_NAME, LATLON, OBST_RADIUS);
	
	private final String ROBOT_LOC_NAME = "ROBOT_LOCATION";
	private final double ROBOT_ORIENT = 139.221;
	private final DroneType ROBOT_TYPE = DroneType.DRONE;
	private final RobotLocation ROBOT_LOC = new RobotLocation(ROBOT_LOC_NAME, LATLON, ROBOT_ORIENT, ROBOT_TYPE);

	// Expected Results
	private final String expectedLogDataString = 
			  LogCodex.LOG_TYPE + LogCodex.LogType.LOGDATA
			+ LogCodex.MAIN_SEPARATOR + LogCodex.IP_ADDR_SEP + IP_ADDR
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TIMESTEP_SEP + TIMESTEP
			+ LogCodex.MAIN_SEPARATOR + LogCodex.DRONE_TYPE_SEP + DRONE_TYPE
			+ LogCodex.MAIN_SEPARATOR + LogCodex.SYS_TIME_SEP + SYS_TIME
			+ LogCodex.MAIN_SEPARATOR + LogCodex.LAT_LON_SEP + LATLON.getLat() + LogCodex.ARRAY_SEPARATOR + LATLON.getLon() 
			+ LogCodex.MAIN_SEPARATOR + LogCodex.GPS_ORIENT_SEP + GPS_ORIENT
			+ LogCodex.MAIN_SEPARATOR + LogCodex.GPS_SPD + GND_SPEED
			+ LogCodex.MAIN_SEPARATOR + LogCodex.GPS_TIME_SEP + GPS_DATE
			+ LogCodex.MAIN_SEPARATOR + LogCodex.COMP_ORIENT_SEP + COMP_ORIENT
			+ LogCodex.MAIN_SEPARATOR + LogCodex.TEMP_SEP 
				+ TEMPS[0] + LogCodex.ARRAY_SEPARATOR + TEMPS[1]
			+ LogCodex.MAIN_SEPARATOR + LogCodex.MOTORS_SPDS_SEP 
				+ MOT_SPEEDS[0] + LogCodex.ARRAY_SEPARATOR + MOT_SPEEDS[1] 
			+ LogCodex.MAIN_SEPARATOR + LogCodex.NEURAL_NET_IN_SEP 
				+ INPUT_NN[0] + LogCodex.ARRAY_SEPARATOR + INPUT_NN[1] + LogCodex.ARRAY_SEPARATOR + INPUT_NN[2] + LogCodex.ARRAY_SEPARATOR + INPUT_NN[3]
			+ LogCodex.MAIN_SEPARATOR + LogCodex.NEURAL_NET_OUT_SEP 
				+ OUTPUT_NN[0] + LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[1] + LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[2] + LogCodex.ARRAY_SEPARATOR + OUTPUT_NN[3]
			+ LogCodex.MAIN_SEPARATOR + LogCodex.SENTENCE_SEP
				+ LogCodex.SENTENCE_DELIMITATOR + SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE) + LogCodex.SENTENCE_DELIMITATOR 
			+ LogCodex.LINE_SEPARATOR;

	private final String expectedErrorLogString = 
				LogCodex.LOG_TYPE + LogCodex.LogType.ERROR 
			+ LogCodex.MAIN_SEPARATOR + LogCodex.SENTENCE_SEP
			+ LogCodex.SENTENCE_DELIMITATOR + SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE) + LogCodex.SENTENCE_DELIMITATOR 
			+ LogCodex.LINE_SEPARATOR;
	
	private final String expectedMessageLogString = 
			LogCodex.LOG_TYPE + LogCodex.LogType.MESSAGE 
		+ LogCodex.MAIN_SEPARATOR + LogCodex.SENTENCE_SEP
		+ LogCodex.SENTENCE_DELIMITATOR + SENTENCE.replace(LogCodex.SENTENCE_DELIMITATOR, LogCodex.SENTENCE_ESCAPE) + LogCodex.SENTENCE_DELIMITATOR 
		+ LogCodex.LINE_SEPARATOR;
	
	@Test
	public void TestCodex() {
		LogData codedData = new LogData();

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

		// Test coding
		String logStr = LogCodex.encodeLog(LogType.LOGDATA, codedData);
		assertEquals(expectedLogDataString, logStr);
		
		String errStr = LogCodex.encodeLog(LogType.ERROR, SENTENCE);
		assertEquals(expectedErrorLogString, errStr);
		
		String messageStr = LogCodex.encodeLog(LogType.MESSAGE, SENTENCE);
		assertEquals(expectedMessageLogString, messageStr);
		
		//TODO -> finish tests for geofences (need to create the object) and asserts for all
		String wpStr = WAYPOINT.getLogMessage();
		System.out.println(wpStr);
		
		String obstacleStr = OBSTACLE.getLogMessage();
		System.out.println(obstacleStr);
		
		String robotStr = ROBOT_LOC.getLogMessage();
		System.out.println(robotStr);

		// Decode
		DecodedLog logDataDecoded = LogCodex.decodeLog(logStr);
		LogData fullDecodedLogData = ((LogData) logDataDecoded.getPayload());
		assertEquals(LogType.LOGDATA, logDataDecoded.payloadType());
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

		DecodedLog errMsgDecoded = LogCodex.decodeLog(errStr);
		String fullDecodedErrMsg = ((String) errMsgDecoded.getPayload());
		assertEquals(LogType.ERROR, errMsgDecoded.payloadType());
		assertEquals(SENTENCE, fullDecodedErrMsg);
		
		DecodedLog messageMsgDecoded = LogCodex.decodeLog(messageStr);
		String fullDecodedMessageMsg = ((String) messageMsgDecoded.getPayload());
		assertEquals(LogType.MESSAGE, messageMsgDecoded.payloadType());
		assertEquals(SENTENCE, fullDecodedMessageMsg);
	}
}
