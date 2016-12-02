package utils.GPSTimeProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;

import org.joda.time.LocalDateTime;

import commoninterface.dataobjects.GPSData;
import commoninterface.utils.NMEAUtils;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class GPSModuleInput {
	private boolean DEBUG_MODE = false;

	private final static int DEFAULT_BAUD_RATE = 4800;
	private final static String COM_PORT_WINDOWS = "COM4";
	private final static String COM_PORT_LINUX = "/dev/ttyUSB0";
	private String port;

	private SerialPort serialPort;
	private NMEAUtils nmeaUtils = new NMEAUtils();

	private SerialReader reader;
	private Thread readerThread;

	private GPSData gpsData = new GPSData();
	private MessageParser messageParser;

	private boolean available = false;

	public GPSModuleInput() {
		Properties prop = System.getProperties();
		prop.put("gnu.io.rxtx.NoVersionOutput", "true");

		System.setProperties(prop);
	}

	public boolean init() throws UnsatisfiedLinkError, IOException {
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			port = COM_PORT_WINDOWS;
		} else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
			port = COM_PORT_LINUX;
		} else {
			// System.err.printf("[%s] Input not implemented for this OS%n",
			// getClass().getName());
			throw new UnsatisfiedLinkError("Input not implemented for this OS");
		}

		return init(port);
	}

	public boolean init(String port) throws UnsatisfiedLinkError, IOException {
		this.port = port;
		// System.out.printf("[%s] Initializing GPS!%n", getClass().getName());

		messageParser = new MessageParser();
		messageParser.start();

		available = createSerialPort();
		if (!available) {
			messageParser.interrupt();
		}

		return available;
	}

	private boolean createSerialPort() throws IOException {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);

			if (portIdentifier.isCurrentlyOwned()) {
				// System.out.printf("[%s] Error: Port is currently in use%n",
				// getClass().getName());
				throw new IOException("Port is currently in use");
			} else {

				CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(DEFAULT_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

					InputStream inputStream = serialPort.getInputStream();
					reader = new SerialReader(inputStream, messageParser);
					readerThread = new Thread(reader);
					readerThread.start();

				} else {
					commPort.close();
					// System.out.printf("[%s] Error: Only serial ports are
					// handled%n", getClass().getName());
					throw new IOException("Only serial ports are handled");
				}
			}
		} catch (NoSuchPortException e) {
			// System.err.printf("[%s] GPS was not found on the %s port%n",
			// getClass().getName(), port);
			throw new IOException("GPS module was not found on " + port + " port");
		} catch (IOException e) {
			// System.err.printf("[%s] Error on input/output streams%n%s%n",
			// getClass().getName(), e.getMessage());
			throw new IOException("Error establishing input/output streams");
		} catch (UnsupportedCommOperationException e) {
			// System.err.printf("[%s] Error on setting up comm port
			// parameters%n%s%n", getClass().getName(),
			// e.getMessage());
			throw new IOException("Error on setting up comm port parameters");
		} catch (PortInUseException e) {
			System.err.printf("[%s] Error acquiring comm port%n%s%n", getClass().getName(), e.getMessage());
			throw new IOException("Error acquiring comm port");
		}

		return true;
	}

	public static class SerialReader implements Runnable {
		private InputStream inputStream;
		private MessageParser messageParser;
		private boolean exit = false;
		private byte[] inputBytes = new byte[1024];
		private int bytesRead = 0;

		public SerialReader(InputStream inputStream, MessageParser messageParser) {
			this.inputStream = inputStream;
			this.messageParser = messageParser;
		}

		@Override
		public void run() {
			StringBuffer str = new StringBuffer();
			while (!exit) {
				try {
					if ((bytesRead = inputStream.read(inputBytes)) != -1) {
						int indexSecondDollar = 0;
						int indexFirstDollar = 0;
						boolean keepGoing = true;

						byte[] toProcess = new byte[bytesRead];
						for (int i = 0; i < bytesRead; i++) {
							toProcess[i] = inputBytes[i];
						}

						inputBytes = new byte[1024];
						str.append(new String(toProcess));

						while (keepGoing) {
							keepGoing = false;
							indexFirstDollar = str.indexOf("$");

							if (indexFirstDollar >= 0) {
								indexSecondDollar = str.indexOf("$", indexFirstDollar + 1);
								if (indexSecondDollar >= 0 && indexSecondDollar < str.length()) {

									String sub = str.substring(indexFirstDollar, indexSecondDollar).trim();
									if (messageParser != null && !sub.isEmpty() && sub.charAt(0) == '$') {
										messageParser.processReceivedData(sub);
										keepGoing = true;
									}
									str.delete(0, indexSecondDollar);
								}
							}
						}
					}
				} catch (IOException e) {
					System.err.printf("[%s] Error while reading line from serial%n", getClass().getName());
				}
			}
		}

		public void close() {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void stopService() {
		if (readerThread != null) {
			reader.close();
			readerThread.interrupt();
		}

		if (messageParser != null) {
			messageParser.interrupt();
		}

		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public boolean isAvailable() {
		return available;
	}

	public synchronized GPSData getGPSData() {
		return gpsData.clone();
	}

	private void print(String str) {
		if (DEBUG_MODE) {
			System.out.println(str);
		}
	}

	public static String[] getSerialPortIdentifiers() {
		ArrayList<String> identifiersStrings = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier.getPortIdentifiers();

		while (identifiers.hasMoreElements()) {
			identifiersStrings.add(identifiers.nextElement().getName());
		}

		if (identifiersStrings.isEmpty()) {
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				identifiersStrings.add(COM_PORT_WINDOWS);
			} else if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
				identifiersStrings.add(COM_PORT_LINUX);
			} else if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
				// identifiersStrings.add(COM_PORT_MAC);
				throw new UnsatisfiedLinkError("Input not implemented for this OS");
			}
		}

		return identifiersStrings.toArray(new String[identifiersStrings.size()]);
	}

	private class MessageParser extends Thread {
		protected LinkedList<String> messages = new LinkedList<String>();
		private boolean newData = false;

		/**
		 * Processes the received data and splits it by commands and messages
		 * sentences and send them to the correct parser
		 * 
		 * @param data
		 *            : data to be processed
		 */
		protected void processReceivedData(String data) {
			if (!nmeaUtils.checkNMEAChecksum(data)) {
				return;
			}

			int indexComma = data.indexOf(',');
			if (indexComma >= 0) {
				synchronized (messages) {
					newData = true;
					messages.add(data);
				}

				synchronized (this) {
					notifyAll();
				}
			}
		}

		@Override
		public void run() {
			boolean exit = false;

			while (!exit) {
				synchronized (this) {
					try {
						while (!newData) {
							wait();
						}
					} catch (InterruptedException e) {
						exit = true;
					}
				}

				if (!exit) {
					String message = null;
					synchronized (messages) {
						if (messages.size() > 0) {
							message = messages.removeFirst();
						}

						newData = !messages.isEmpty();
					}

					if (message != null) {
						int indexComma = message.indexOf(',');
						if (indexComma >= 0) {
							String name = message.substring(0, indexComma);
							parseNMEAData(name.substring(1), message);
						}
					}
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
				case "GPGGA":
					// parseGPGGASentence(split);
					break;
				case "GPGSA":
					// parseGPGSASentence(split);
					break;

				case "GPGSV":
					parseGPGSVSentence(split);
					break;

				case "GPRMC":
					parseGPRMCSentence(split);
					break;

				case "GPVTG":
					// parseGPVTGSentence(split);
					break;

				default:
					// print("No parser for " + name + " sentence", false);
					break;
				}
			} catch (Exception e) {
				// System.out.println("[GPS] Error parsing " + name + "!");
				// System.out.println(fullString);
				// e.printStackTrace();
			}

			if ((name.equals("GPRMC") || name.equals("GPGSV")) && DEBUG_MODE) {
				System.out.println("######################");
				System.out.println(fullString);
				System.out.println(gpsData.getDate());
				System.out.println("In view: " + gpsData.getNumberOfSatellitesInView());
			}
		}

		/**
		 * Process Global Positioning System Fix Data
		 * (http://aprs.gids.nl/nmea/#gga)
		 * 
		 * @param params
		 *            : Parameters extracted from GPGGA sentence
		 */
		@SuppressWarnings("unused")
		private void parseGPGGASentence(String[] params) {
			if (params.length == 15) {
				print("[Parsing GPGGA]");
				if (!params[2].isEmpty()) {
					gpsData.setLatitude(params[2] + params[3]);
					gpsData.setLongitude(params[4] + params[5]);
					gpsData.setGPSSourceType(Integer.parseInt(params[6]));
					gpsData.setNumberOfSatellitesInUse(Integer.parseInt(params[7]));
					gpsData.setHDOP(Double.parseDouble(params[8]));

					if (!params[9].isEmpty())
						gpsData.setAltitude(Double.parseDouble(params[9]));

					// Missing the geoidal separation (not parsed in this
					// case)
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
		@SuppressWarnings("unused")
		private void parseGPGSASentence(String[] params) {
			if (params.length == 18) {
				print("[Parsing GPGSA]");
				int fixType = Integer.parseInt(params[2]);
				gpsData.setFixType(fixType);

				if (fixType == 1)
					gpsData.setFix(false);
				else
					gpsData.setFix(true);

				if (!params[15].isEmpty() && !params[16].isEmpty() && !params[17].isEmpty()) {
					gpsData.setPDOP(Double.parseDouble(params[15]));
					gpsData.setHDOP(Double.parseDouble(params[16]));

					if (params[17].length() > 3)
						gpsData.setVDOP(Double.parseDouble(params[17].substring(0, params[17].length() - 3)));
				}

				// Missing list of satellites in view, used to fix (not
				// parsed
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
				print("[Parsing GPGSV]");
				gpsData.setNumberOfSatellitesInView(Integer.parseInt(params[3].substring(0, params[3].length() - 3)));
			} else if (params.length > 4) {
				print("[Parsing GPGSV]");
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
				print("[Parsing GPRMC]");
				String[] d = params[9].split("(?<=\\G.{2})");

				params[1] = params[1].replace(".", "");
				String[] t = params[1].split("(?<=\\G.{2})");

				try {
					int miliseconds = Integer.parseInt(t[3] + t[4]);

					// LocalDateTime doesn't like miliseconds with
					// a value higher than 999, but NMEA does.
					while (miliseconds > 999)
						miliseconds /= 10;

					LocalDateTime date = new LocalDateTime(Integer.parseInt(d[2]) + 2000, Integer.parseInt(d[1]),
							Integer.parseInt(d[0]), Integer.parseInt(t[0]), Integer.parseInt(t[1]),
							Integer.parseInt(t[2]), miliseconds);
					gpsData.setDate(date);

					// if (!alreadySetDate) {
					// DateTimeFormatter f = DateTimeFormat.forPattern("EEE MMM
					// d HH:mm:ss 'UTC' YYYY");
					// Runtime.getRuntime()
					// .exec(new String[] { "bash", "-c", "sudo date --set \"" +
					// date.toString(f) + "\";" })
					// .waitFor();
					// alreadySetDate = true;
					// }

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
		@SuppressWarnings("unused")
		private void parseGPVTGSentence(String[] params) {
			if (params.length == 10) {
				if (!params[1].isEmpty())
					gpsData.setOrientation(Double.parseDouble(params[1]));

				if (!params[5].isEmpty())
					gpsData.setGroundSpeedKnts(Double.parseDouble(params[5]));

				if (!params[7].isEmpty())
					gpsData.setGroundSpeedKmh(Double.parseDouble(params[7]));
			} else {
				throw new RuntimeException();
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, UnsatisfiedLinkError, IOException {
		GPSModuleInput module = new GPSModuleInput();
		module.init(COM_PORT_WINDOWS);

		Thread.sleep(5000);
		System.err.println("Stoping!");
		module.stopService();
	}
}
