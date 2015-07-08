package testclasses;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.Test;

import commoninterface.AquaticDroneCI;
import commoninterface.utils.LogCodex;
import commoninterface.utils.LogCodex.LogType;
import commoninterface.utils.jcoord.LatLon;

public class LogCodexTester {
	// Constants to be used
	private final int TIMESTEP = 10;
	private final String COMMENT = "Tes\"te";
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
			  LogCodex.LOG_TYPE+ LogCodex.LogType.LOGDATA.getValue()
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
			+ LogCodex.MAIN_SEPARATOR + LogCodex.COMMENT_SEP
				+ LogCodex.COMMENT_DELIMITATOR + COMMENT.replace(LogCodex.COMMENT_DELIMITATOR, LogCodex.COMMENT_ESCAPE) + LogCodex.COMMENT_DELIMITATOR 
			+ LogCodex.LINE_SEPARATOR;

	private final String expectedIpString = 
			  LogCodex.LOG_TYPE+ LogCodex.LogType.IP.getValue()
			+ LogCodex.MAIN_SEPARATOR + LogCodex.IP_ADDR_SEP + IP_ADDR 
			+ LogCodex.LINE_SEPARATOR;

	@Test
	public void TestCodex() {
		LogCodex.LogData codedData = new LogCodex.LogData();

		// Set data
		codedData.timestep = TIMESTEP;
		codedData.comment = COMMENT;
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
		String str1 = LogCodex.encodeLog(LogType.LOGDATA, codedData);
		assertEquals(expectedLogDataString, str1);

		String str2 = LogCodex.encodeLog(LogType.IP, IP_ADDR);
		assertEquals(expectedIpString, str2);

		// Decode
		// LoggerCodex.LogData fullDecodedData = LoggerCodex.decodeLog(str1);
		// assertEquals(codedData, fullDecodedData);
		//
		// LoggerCodex.LogData ipDecoded = LoggerCodex.decodeLog(str2);
		// assertEquals(expected, ipDecoded);

	}
}
