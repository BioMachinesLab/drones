package io.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import utils.FileLogger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import commoninterface.RobotCI;
import commoninterface.dataobjects.GPSData;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.network.messages.SystemStatusMessage;
import commoninterface.utils.NMEAUtils;

public class GPSModuleInput implements ControllerInput, MessageProvider,
		Serializable {
	private static final long serialVersionUID = -5443358826645386873L;

	private final static String FILE_NAME = FileLogger.LOGS_FOLDER + "GPSLog_";
	private boolean localLog = false;
	private PrintWriter localLogPrintWriterOut;
	private RobotCI robotCI;

	private final static boolean DEBUG_MODE = false;

	/*
	 * GPS Configurations
	 */
	private final static int DEFAULT_BAUD_RATE = 9600;

	/* Possible: // 4800,9600,14400,19200,38400,57600,115200 */
	private final static int TARGET_BAUD_RATE = 57600;

	/* In milliseconds on [100,10000] interval */
	private final static int ECHO_DELAY = 200;

	/* In milliseconds on [200,10000] interval */
	private final static int FIX_UPDATE = 200;

	/* Can only be enabled when echo rate is less or equal than 5Hz (200ms) */
	private final static boolean ENABLE_SBAS = true;

	private final static String COM_PORT = Serial.DEFAULT_COM_PORT;
	private NMEAUtils nmeaUtils = new NMEAUtils();
	private Serial serial; // Serial connection
	protected StringBuffer receivedDataBuffer = new StringBuffer();
	protected GPSData gpsData = new GPSData(); // Contains the obtained info
	private List<String> ackResponses = Collections
			.synchronizedList(new ArrayList<String>());
	protected MessageParser messageParser;

	protected boolean available = false;
	private boolean enable = false;

	private boolean alreadySetDate = false;

	public GPSModuleInput(boolean fake) {
		if (!fake)
			init();
	}

	public GPSModuleInput(RobotCI robotCI) {
		this.robotCI = robotCI;
		init();
	}

	public void init() {
		try {
			print("Initializing GPS!", false);

			messageParser = new MessageParser();
			messageParser.start();

			setupGPSReceiver();

			available = true;
			enable = true;
		} catch (NotActiveException | IllegalArgumentException
				| InterruptedException e) {
			e.printStackTrace();
			System.err.println("[GPS Module] Error initializing GPSModule! ("
					+ e.getMessage() + ")");
			closeSerial();
		} catch (Error | Exception e) {
			e.printStackTrace();
			System.err.println("[GPS Module] Error initializing GPSModule! ("
					+ e.getMessage() + ")");
		}
	}

	protected void createSerial(final int baudrate) {
		serial = SerialFactory.createInstance();

		serial.addListener(new SerialDataEventListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {

				int indexSecondDollar = 0;
				int indexFirstDollar = 0;

				try {

					receivedDataBuffer.append(event.getAsciiString());

					boolean keepGoing = true;

					while (keepGoing) {
						keepGoing = false;
						indexFirstDollar = receivedDataBuffer.indexOf("$");

						if (indexFirstDollar >= 0) {

							indexSecondDollar = receivedDataBuffer.indexOf("$",
									indexFirstDollar + 1);

							if (indexSecondDollar >= 0
									&& indexSecondDollar < receivedDataBuffer
											.length()) {

								String sub = receivedDataBuffer.substring(
										indexFirstDollar, indexSecondDollar)
										.trim();

								if (messageParser != null && !sub.isEmpty()
										&& sub.charAt(0) == '$') {
									messageParser.processReceivedData(sub);
									keepGoing = true;
								}
								receivedDataBuffer.delete(0, indexSecondDollar);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(indexFirstDollar + " -> "
							+ indexSecondDollar);
					System.out.println(receivedDataBuffer);
					receivedDataBuffer.setLength(0);
				}
			}
		});

		try {
			serial.open(COM_PORT, baudrate);
		} catch (IOException e) {
			System.err
					.println("[GPS Module] Error while opening serial stream ("
							+ e.getMessage() + ")");
		}
	}

	public void closeSerial() {
		try {
			if (!serial.isClosed()) {
				serial.close();
			}
		} catch (IOException | IllegalStateException e) {
			System.err.println("[GPS Module] Error Closing Serial Stream ("
					+ e.getMessage() + ")");
		}
	}

	private void serialWrite(String str, boolean flush) {
		try {
			serial.write(str);
			if (flush)
				serial.flush();
		} catch (IllegalStateException | IOException e) {
			System.err.println("[GPS Module] Error while writing to device ("
					+ e.getMessage() + ")");
		}
	}

	@Override
	public GPSData getReadings() {
		return gpsData;
	}

	@SuppressWarnings("unused")
	private void setupGPSReceiver() throws NotActiveException,
			InterruptedException, IllegalArgumentException {
		
		if (ECHO_DELAY < 100 || ECHO_DELAY > 10000) {
			throw new IllegalArgumentException(
					"[GPS Module] Echo frequency must be in [100,10000] interval!");
		}

		if (FIX_UPDATE < 200 || FIX_UPDATE > 10000) {
			throw new IllegalArgumentException(
					"[GPS Module] Fix update frequency must be in [200,10000] interval!");
		}

		
		createSerial(DEFAULT_BAUD_RATE);
		Thread.sleep(1000);
		
		performWarmStart();
		print("[GPS Module] Warm Start performed!",false);

		/*
		 * Change Baud Rate
		 */
		String command = "$PMTK251," + TARGET_BAUD_RATE + "*";
		String checksum = nmeaUtils.calculateNMEAChecksum(command);
		command += checksum + "\r\n";
		serialWrite(command, false);
		print("[COMMAND] " + command, false);

		Thread.sleep(1000);

		/*
		 * Closing old velocity serial stream and open a new with the new
		 * velocity
		 */
		try {
			serial.flush();
			serial.close();
		} catch (IOException e) {
			print("[GPS Module] Error Flushing and/or closing serial stream ("
					+ e.getMessage() + ")", true);
		}

		Thread.sleep(1000);
		createSerial(TARGET_BAUD_RATE);
		print("[GPS Module] Started new baud rate!", false);
		Thread.sleep(1000);

		/*
		 * Change Information Echo Frequency
		 */
		command = "$PMTK220," + ECHO_DELAY + "*";
		checksum = nmeaUtils.calculateNMEAChecksum(command);
		command += checksum;
		if (sendCommandAndCheckAnswer(command, "$PMTK001,220,3*30")) {
			print("[GPS Module] OK! Update echo frequency was succefully changed!",
					false);
		} else {
			print("[GPS Module] Update echo frequency was NOT succefully changed!",
					false);
		}

		/*
		 * Change Fix update frequency
		 */
		command = "$PMTK300," + FIX_UPDATE + ",0,0,0,0*";
		checksum = nmeaUtils.calculateNMEAChecksum(command);
		command += checksum;
		if (sendCommandAndCheckAnswer(command, "$PMTK001,300,3*33")) {
			print("[GPS Module] OK! Update fix frequency was succefully changed!",
					false);
		} else {
			print("[GPS Module] Update fix frequency was NOT succefully changed!",
					false);
		}

		/*
		 * Set navigation speed threshold to 0
		 */
		if (sendCommandAndCheckAnswer("$PMTK397,0*23", "$PMTK001,397,3*3D")) {
			print("[GPS Module] OK! Navigation speed threshold was succefully changed!",
					false);
		} else {
			print("[GPS Module] Navigation speed threshold was NOT succefully changed!",
					false);
		}

		/*
		 * Disable always locate mode
		 */
		if (disableAlwaysLocateStandby()) {
			print("[GPS Module] OK! Always locate mode was succefully disabled!",
					false);
		} else {
			print("[GPS Module] Always locate mode was NOT succefully disabled!",
					false);
		}

		/*
		 * Enable AIC
		 */
		if (enableAIC()) {
			print("[GPS Module] OK! AIC mode sucessfully activated!", false);
		} else {
			print("[GPS Module] AIC mode NOT sucessfully activated!", false);
		}

		/*
		 * Set SBAS mode
		 */
		if (ENABLE_SBAS) {
			boolean success = enableSBAS();
			if (success)
				print("[GPS Module] OK! SBAS was succefully enabled!", false);
			else
				print("[GPS Module] SBAS was NOT succefully enabled!", false);
		}

		ackResponses.clear();
	}

	private boolean sendCommandAndCheckAnswer(String command, String answer)
			throws InterruptedException {
		serialWrite(command + "\r\n", false);

		Thread.sleep(1000);
		String ack = getStringStartWithFromList(answer);
		if (ack != null) {
			ackResponses.remove(ack);
			return true;
		} else {
			return false;
		}
	}

	private String getStringStartWithFromList(String str) {
		for (String string : ackResponses) {
			if (string.startsWith(str)) {
				return string;
			}
		}

		return null;
	}

	/**
	 * Print the given string on the defined system console
	 * 
	 * @param str
	 *            : string to be printed
	 * @param mode
	 *            : true if an error message, false if is a normal print
	 */
	private void print(String str, boolean mode) {
		if (DEBUG_MODE)
			if (!mode)
				System.out.println(str);
			else
				System.err.println(str);
	}

	public Message getMessage(Message request) {
		if (request instanceof InformationRequest
				&& ((InformationRequest) request).getMessageTypeQuery() == InformationRequest.MessageType.GPS) {

			if (!available)
				return new SystemStatusMessage(
						"[GPSModule] Unable to send GPS data",
						robotCI.getNetworkAddress());

			return new GPSMessage(getReadings(), robotCI.getNetworkAddress());
		}

		return null;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	/*
	 * GPS Functions
	 */
	public String getReleaseVersion() throws InterruptedException {
		serialWrite("$PMTK605*31\r\n", false);

		Thread.sleep(1000);
		int index = -1;
		String str = null;
		for (int i = 0; i < ackResponses.size(); i++) {
			if (ackResponses.get(i).startsWith("$PMTK705")) {
				index = i;
				str = ackResponses.get(i);
			}
		}

		if (index != -1) {
			ackResponses.remove(index);
		}

		return str;
	}

	public boolean startLog() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK185,0*22", "$PMTK001,185,3*3C");
	}

	public boolean stopLog() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK185,1*23", "$PMTK001,185,3*3C");
	}

	public boolean eraseLog() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK184,1*22", "$PMTK001,184,3*3D");
	}

	public boolean enableAlwaysLocateStandby() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK225,8*23", "$PMTK001,225,3*35");
	}

	public boolean disableAlwaysLocateStandby() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK225,0*2B", "$PMTK001,225,3*35");
	}

	public boolean enableAIC() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK286,1*23", "$PMTK001,286,3*3C");
	}

	public boolean disableAIC() throws InterruptedException {
		return sendCommandAndCheckAnswer("$PMTK286,0*23", "$PMTK001,286,3*3C");
	}

	public boolean enableSBAS() throws InterruptedException {
		// serial.write("$PMTK513,1*28\r\n"); // DT Command. Which is the
		// difference??
		return sendCommandAndCheckAnswer("$PMTK313,1*2E", "$PMTK001,313,3*31");
	}

	public boolean disableSBAS() throws InterruptedException {
		// serial.write("$PMTK513,0*29\r\n"); // DT Command. Which is the
		// difference??
		return sendCommandAndCheckAnswer("$PMTK313,0*2F", "$PMTK001,313,3*31");
	}

	public boolean standbyGPS() throws InterruptedException {
		if (enable) {
			return sendCommandAndCheckAnswer("$PMTK161,0*28",
					"$PMTK001,161,3*36");
		} else {
			return true;
		}
	}

	public boolean wakeUpGPS() throws InterruptedException {
		if (!enable) {
			return sendCommandAndCheckAnswer("$PMTK010,002*2D",
					"$PMTK001,010,3*31");
		} else {
			return true;
		}

	}

	/* Hot Restart: Use all available data in the NV Store. */
	public void performHotStart() throws InterruptedException {
		serialWrite("PMTK101*32\r\n", false);
	}

	/* Warm Restart: Don't use Ephemeris at re-start. */
	public void performWarmStart() throws InterruptedException {
		serialWrite("PMTK102*31\r\n", false);
	}

	/*
	 * Cold Restart: Don't use Time, Position, Almanacs and Ephemeris data at
	 * re-start.
	 */
	public void performColdStart() throws InterruptedException {
		serialWrite("PMTK103*30\r\n", false);
	}

	/*
	 * Full Cold Restart: It�s essentially a Cold Restart, but additionally
	 * clear system/user configurations at re-start. That is, reset the receiver
	 * to the factory status.
	 */
	public void performFullColdStart() throws InterruptedException {
		serialWrite("PMTK104*37\r\n", false);
	}

	public void enableMessagesOnExternalAntenna() throws InterruptedException {
		// check it here
		// https://github.com/adafruit/Adafruit-GPS-Library/blob/master/Adafruit_GPS.h
		serialWrite("$PGCMD,33,1*6C\r\n", false);
	}

	public void disableMessagesOnExternalAntenna() throws InterruptedException {
		// check it here
		// https://github.com/adafruit/Adafruit-GPS-Library/blob/master/Adafruit_GPS.h
		serialWrite("$PGCMD,33,0*6D\r\n", false);
	}

	/*
	 * Enables a logging of the received NMEA information in the disk (on the
	 * selected path)
	 */
	public void enableLocalLog() {
		try {
			Calendar cal = Calendar.getInstance();
			cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

			File dir = new File(FileLogger.LOGS_FOLDER);
			if (!dir.exists()) {
				dir.mkdir();
				System.out.println("[GPSModuleInput] Created Logs Folder");
			}

			localLogPrintWriterOut = new PrintWriter(FILE_NAME
					+ sdf.format(cal.getTime()) + ".log");
			localLog = true;
		} catch (FileNotFoundException e) {
			System.err.println("[GPSModuleInput] Unable to start local log");
			e.printStackTrace();
		}
	}

	class MessageParser extends Thread {

		private HashMap<String, String> currentValues = new HashMap<String, String>();
		private HashMap<String, String> oldValues = new HashMap<String, String>();
		private boolean pending = false;

		/**
		 * Processes the received data and splits it by commands and messages
		 * sentences and send them to the correct parser
		 * 
		 * @param data
		 *            : data to be processed
		 */
		protected void processReceivedData(String data) {

			if (!nmeaUtils.checkNMEAChecksum(data)) {
				// System.out.println("[GPS] Checksum failed for "+data);
				return;
			}

			int indexComma = data.indexOf(',');
			if (indexComma >= 0) {
				String name = data.substring(0, indexComma);

				synchronized (currentValues) {
					// if (currentValues.get(name) == null) {
					pending = true;
					// }
					currentValues.put(name, data);
				}
				synchronized (this) {
					notifyAll();
				}
			}
		}

		@Override
		public void run() {

			int size = 0;
			String[] keys = {};

			while (true) {

				synchronized (this) {
					try {
						while (!pending)
							wait();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (currentValues.size() != size) {
					size = currentValues.size();
					keys = new String[size];
					int i = 0;
					synchronized (currentValues) {
						for (String s : currentValues.keySet()) {
							if (i < keys.length) {
								keys[i++] = s;
							}
						}
					}
				}

				if (size > 0) {

					for (int index = 0; index < size; index++) {

						String name = keys[index];
						String val;

						try {

							synchronized (currentValues) {
								val = currentValues.get(name);
								currentValues.put(name, null);
							}

							if (val != null) {

								// check if it has changed in the meanwhile
								String oldVal = oldValues.get(name);

								if (oldVal == null || !oldVal.equals(val)) {

									if (name.charAt(1) == 'G') {

										if (localLog)
											localLogPrintWriterOut.println(val);

										parseNMEAData(name, val);
									}
									if (name.charAt(1) == 'P') {
										if (localLog)
											localLogPrintWriterOut.println(val);

										parsePMTKData(name, val);
									}
									oldValues.put(name, val);
								}
							}
						} catch (Exception e) {
							System.out.println("error reading " + name);
						}

					}
					pending = false;
				}
			}
		}

		/**
		 * Parses the NMEA messages send by the GPS receiver, containing the
		 * navigation information
		 * 
		 * @param data
		 *            : NMEA sentence to be processed
		 */
		private void parseNMEAData(String name, String fullString) {

			try {
				String[] split = fullString.split(",");

				switch (name) {
				// case "$GPGGA":
				// parseGPGGASentence(split);
				// break;

				case "$GPGSA":
					parseGPGSASentence(split);
					break;

				case "$GPGSV":
					parseGPGSVSentence(split);
					break;

				case "$GPRMC":
					parseGPRMCSentence(split);
					break;

				case "$PMTK":
					parsePMTKData(name, fullString);
					break;

				// case "$GPVTG":
				// parseGPVTGSentence(split);
				// break;

				default:
					// print("No parser for " + name + " sentence", false);
					break;
				}
			} catch (Exception e) {
				// System.out.println("[GPS] Error parsing " + name + "!");
				// System.out.println(fullString);
				// e.printStackTrace();
			}
		}

		/**
		 * Parses the PMTK acknowledges messages send by the GPS receiver
		 * 
		 * @param data
		 *            : PMTK sentence to be processed
		 */
		private void parsePMTKData(String name, String fullString) {
			print("[Parsing PMTK]", false);
			ackResponses.add(fullString);
			print("[ACK] " + fullString, false);
		}

		/**
		 * Process Global Positioning System Fix Data
		 * (http://aprs.gids.nl/nmea/#gga)
		 * 
		 * @param params
		 *            : Parameters extracted from GPGGA sentence
		 */
		private void parseGPGGASentence(String[] params) {
			if (params.length == 15) {
				print("[Parsing GPGGA]", false);
				if (!params[2].isEmpty()) {
					gpsData.setLatitude(params[2] + params[3]);
					gpsData.setLongitude(params[4] + params[5]);
					gpsData.setGPSSourceType(Integer.parseInt(params[6]));
					gpsData.setNumberOfSatellitesInUse(Integer
							.parseInt(params[7]));
					gpsData.setHDOP(Double.parseDouble(params[8]));

					if (!params[9].isEmpty())
						gpsData.setAltitude(Double.parseDouble(params[9]));

					// Missing the geoidal separation (not parsed in this case)
					gpsData.setFix(true);
				} else {
					gpsData.setFix(false);
				}
			} else
				throw new RuntimeException();
		}

		/**
		 * GPS DOP and active satellites Data (http://aprs.gids.nl/nmea/#gsa)
		 * 
		 * @param params
		 *            : Parameters extracted from GPGSA sentence
		 */
		private void parseGPGSASentence(String[] params) {
			if (params.length == 18) {
				print("[Parsing GPGSA]", false);
				int fixType = Integer.parseInt(params[2]);
				gpsData.setFixType(fixType);

				if (fixType == 1)
					gpsData.setFix(false);
				else
					gpsData.setFix(true);

				if (!params[15].isEmpty() && !params[16].isEmpty()
						&& !params[17].isEmpty()) {
					gpsData.setPDOP(Double.parseDouble(params[15]));
					gpsData.setHDOP(Double.parseDouble(params[16]));

					if (params[17].length() > 3)
						gpsData.setVDOP(Double.parseDouble(params[17]
								.substring(0, params[17].length() - 3)));
				}

				// Missing list of satellites in view, used to fix (not parsed
				// in
				// this case)
			} else
				throw new RuntimeException();
		}

		/**
		 * GPS Satellites in View Data (http://aprs.gids.nl/nmea/#gsv)
		 * 
		 * @param params
		 *            : Parameters extracted from GPGSV sentence
		 */
		private void parseGPGSVSentence(String[] params) {
			if (params.length == 4 && params[3].length() > 3) {
				print("[Parsing GPGSV]", false);
				gpsData.setNumberOfSatellitesInView(Integer.parseInt(params[3]
						.substring(0, params[3].length() - 3)));
			} else if (params.length > 4) {
				print("[Parsing GPGSV]", false);
				gpsData.setNumberOfSatellitesInView(Integer.parseInt(params[3]));
			} else
				throw new RuntimeException();
		}

		/**
		 * Recommended minimum specific GPS/Transit data
		 * (http://aprs.gids.nl/nmea/#rmc)
		 * 
		 * @param params
		 *            : Parameters extracted from GPRMC sentence
		 */
		private void parseGPRMCSentence(String[] params) {
			if (params.length == 13 || params.length == 10) {
				print("[Parsing GPRMC]", false);
				// TODO splits and replaces are slow

				String[] d = params[9].split("(?<=\\G.{2})");

				params[1] = params[1].replace(".", "");
				String[] t = params[1].split("(?<=\\G.{2})");

				try {
					int miliseconds = Integer.parseInt(t[3] + t[4]);

					// LocalDateTime doesn't like miliseconds with
					// a value higher than 999, but NMEA does.
					while (miliseconds > 999)
						miliseconds /= 10;

					LocalDateTime date = new LocalDateTime(
							Integer.parseInt(d[2]) + 2000,
							Integer.parseInt(d[1]), Integer.parseInt(d[0]),
							Integer.parseInt(t[0]), Integer.parseInt(t[1]),
							Integer.parseInt(t[2]), miliseconds);
					gpsData.setDate(date);

					if (!alreadySetDate) {
						DateTimeFormatter f = DateTimeFormat
								.forPattern("EEE MMM d HH:mm:ss 'UTC' YYYY");
						Runtime.getRuntime()
								.exec(new String[] {
										"bash",
										"-c",
										"sudo date --set \"" + date.toString(f)
												+ "\";" }).waitFor();
						alreadySetDate = true;
					}

				} catch (Exception e) {
					// this part is optional!
				}

				if (params[2].equals("V")) {
					gpsData.setFix(false);
				} else if (params[2].equals("A")) {
					gpsData.setFix(true);
				}

				if (!params[3].isEmpty() && !params[5].isEmpty()) {
					gpsData.setLatitude(params[3] + params[4]);
					gpsData.setLongitude(params[5] + params[6]);
				}

				gpsData.setGroundSpeedKnts(Double.parseDouble(params[7]));
				gpsData.setGroundSpeedKmh(Double.parseDouble(params[7]) * 1.85200);
				gpsData.setOrientation(Double.parseDouble(params[8]));

				// Missing magnetic declination (value and orientation)
			} else
				throw new RuntimeException();
		}

		/**
		 * Track Made Good and Ground Speed Data (http://aprs.gids.nl/nmea/#vtg
		 * and http://www.hemispheregps.com/gpsreference/GPVTG.htm)
		 * 
		 * @param params
		 *            : Parameters extracted from GPVTG sentence
		 */
		private void parseGPVTGSentence(String[] params) {
			if (params.length == 10) {
				if (!params[1].isEmpty())
					gpsData.setOrientation(Double.parseDouble(params[1]));

				if (!params[5].isEmpty())
					gpsData.setGroundSpeedKnts(Double.parseDouble(params[5]));

				if (!params[7].isEmpty())
					gpsData.setGroundSpeedKmh(Double.parseDouble(params[7]));
			} else
				throw new RuntimeException();
		}
	}
}
