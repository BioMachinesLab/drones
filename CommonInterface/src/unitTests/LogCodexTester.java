package unitTests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.jcoord.LatLon;
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
	private final DateTime GPS_DATE = new DateTime(2015, 07, 8, 20, 00, 50);
	private final double COMP_ORIENT = 351.00021;
	private final double[] TEMPS = new double[] { 40.131341, 21.2123 };

	private final double[] MOT_SPEEDS = new double[] { 0.999912212, 0.3313444131 };

	private final double[] INPUT_NN = new double[] { 1.12, 2.12, 3.13, 4.1241 };
	private final double[] OUTPUT_NN = new double[] { 5.12, 6.12, 7.13, 8.1241 };

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
			+ LogCodex.MAIN_SEPARATOR + LogCodex.GPS_TIME_SEP	+ GPS_DATE.toString(LogCodex.dateTimeFormatter)
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

		// Decode
		LogCodex.DecodedLogData logDataDecoded = LogCodex.decodeLog(logStr);
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

		LogCodex.DecodedLogData errMsgDecoded = LogCodex.decodeLog(errStr);
		String fullDecodedErrMsg = ((String) errMsgDecoded.getPayload());
		assertEquals(LogType.ERROR, errMsgDecoded.payloadType());
		assertEquals(SENTENCE, fullDecodedErrMsg);
	}
}
