package fieldtests.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.entities.Entity;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.ToLogData;

public class DroneLogExporter {

	static String[] experiments = new String[] {
			// "2,3,4,5,6,8,9,10;aggregate0_8_1;22-07-15_10:52:16.00;180",
			// "2,3,4,5,6,8,9,10;aggregate0_8_2;22-07-15_10:55:53.00;180",
			// "2,3,4,5,6,7,8,9;aggregate0_8_3;22-07-15_11:05:20.00;180",
			// "2,3,4,5,6,7,8,9;aggregate1_8_1;22-07-15_11:13:24.00;180",
			// "2,3,4,5,6,7,8,9;aggregate1_8_2;22-07-15_11:17:26.00;180",
			// "2,3,4,5,6,8,9,10;aggregate1_8_3;22-07-15_12:00:00.00;180",
			// "2,3,4,5,6,8,9,10;aggregate2_8_1;22-07-15_12:04:31.00;180",
			// "2,3,4,5,6,8,9,10;aggregate2_8_2;22-07-15_12:09:38.00;180",
			// "2,3,4,5,6,8,9,10;aggregate2_8_3;22-07-15_12:14:59.00;180",
			"1,2,3,4,5,6,8,9;dispersion0_8_1;22-07-15_12:27:49.00;90", };

	public static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");

	public static DateTime getStartTime(ArrayList<ToLogData> data, String description) {

		for (ToLogData d : data) {
			if (d.comment != null) {
				if (d.comment.contains(description))
					return DateTime.parse(d.GPSdate, formatter);
			}

		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ToLogData> getCompleteLogs(String f) throws IOException {
		File folder = new File(f);

		ArrayList<ToLogData> result = new ArrayList<ToLogData>();

		ArrayList<Entity> currentEntities = new ArrayList<Entity>();

		for (String file : folder.list()) {
			if (!file.contains(".log") || !file.contains("values"))
				continue;

			// System.out.println("Parsing file: " + file);
			Scanner s = new Scanner(new File(folder.getPath() + "/" + file));

			String prevDate = "";
			boolean prevNormalLog = false;

			while (s.hasNext()) {
				String l = s.nextLine();

				if (l.startsWith("[") || l.startsWith("\"")) {
					// robot restarted, has no entities
					currentEntities.clear();
				}

				if (!l.startsWith(LogCodex.COMMENT_CHAR) && !l.isEmpty()) {
					DecodedLog decodedData = LogCodex.decodeLog(l, currentEntities);

					if (decodedData == null)
						continue;

					switch (decodedData.getPayloadType()) {
					case ENTITIES:

						// TODO there is a bug in the logging of new entities.
						// the old ones are not being logged when they are
						// REMOVED!
						if (prevNormalLog)
							currentEntities.clear();

						currentEntities = (ArrayList<Entity>) LogCodex.decodeLog(l, currentEntities).getPayload();

						prevNormalLog = false;
						break;

					case LOGDATA:

						Object o = decodedData.getPayload();

						if (o == null)
							continue;

						ToLogData d = (ToLogData) o;
						ArrayList<Entity> cE = new ArrayList<Entity>();
						cE.addAll(currentEntities);
						d.entities = cE;

						if (d != null && d.GPSdate != null) {
							prevDate = d.GPSdate;
							result.add(d);
						}
						prevNormalLog = true;

						break;

					case ERROR:
						// messageArea.setForeground(Color.RED);
						// messageArea.setText((String)
						// decodedData.getPayload());
						break;

					case MESSAGE:

						ToLogData data = new ToLogData();
						data.comment = (String) decodedData.getPayload();
						data.GPSdate = prevDate;
						result.add(data);

						break;
					}
				}
			}

			s.close();
		}

		return result;

	}

	public static ArrayList<ToLogData> getLogs(ArrayList<ToLogData> data, DateTime start, DateTime end)
			throws IOException {

		ArrayList<ToLogData> result = new ArrayList<ToLogData>();

		for (ToLogData d : data) {
			try {
				DateTime date = DateTime.parse(d.GPSdate, formatter);

				if (date.isAfter(start) && date.isBefore(end)) {
					result.add(d);
				}
			} catch (Exception e) {
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<ToLogData> getLogs(String f, DateTime start, DateTime end) throws IOException {

		File folder = new File(f);

		ArrayList<ToLogData> result = new ArrayList<ToLogData>();

		boolean prevNormalLog = false;

		for (String file : folder.list()) {
			if (!file.contains(".log") || !file.contains("values"))
				continue;

			// System.out.println("Parsing file: " + file);
			Scanner s = new Scanner(new File(folder.getPath() + "/" + file));

			ArrayList<Entity> currentEntities = new ArrayList<Entity>();

			while (s.hasNext()) {
				String l = s.nextLine();

				if (!l.startsWith(LogCodex.COMMENT_CHAR) && !l.isEmpty()) {
					DecodedLog decodedData = LogCodex.decodeLog(l, currentEntities);

					if (decodedData == null)
						continue;

					switch (decodedData.getPayloadType()) {
					case ENTITIES:

						if (prevNormalLog)
							currentEntities.clear();

						currentEntities = (ArrayList<Entity>) LogCodex.decodeLog(l, currentEntities).getPayload();

						prevNormalLog = false;

						break;

					case LOGDATA:

						Object o = decodedData.getPayload();

						if (o == null)
							continue;

						ToLogData d = (ToLogData) o;
						// d.entities = currentEntities;

						if (d != null && d.GPSdate != null) {
							DateTime date = DateTime.parse(d.GPSdate, formatter);
							if (date.isAfter(start) && date.isBefore(end)) {
								result.add(d);
							}

							d.entities = currentEntities;
							prevNormalLog = true;
						}
						break;

					case ERROR:
						// messageArea.setForeground(Color.RED);
						// messageArea.setText((String)
						// decodedData.getPayload());
						break;

					case MESSAGE:
						// messageArea.setForeground(Color.BLACK);
						// messageArea.setText((String)
						// decodedData.getPayload());
						break;
					}
				}
			}

			s.close();
		}
		return result;
	}

	public static void main(String[] args) throws Exception {
		// for(String experiment : experiments) {
		//
		// String[] split = experiment.split(";");
		//
		// ArrayList<LogData> allData = new ArrayList<LogData>();
		//
		// for(String d : split[0].split(",")) {
		// ArrayList<LogData> extracted =
		// getLogs(Integer.parseInt(d),DateTime.parse(split[2],formatter),DateTime.parse(split[2],formatter).plus(Integer.parseInt(split[3])*1000));
		// System.out.println(experiment+" "+d+" "+extracted.size());
		// allData.addAll(extracted);
		// }
		//
		// Collections.sort(allData);
		//
		// BufferedWriter bw = new BufferedWriter(new FileWriter(new
		// File(split[1]+".txt")));
		//
		// for(LogData d : allData)
		// bw.write(LogCodex.encodeLog(LogType.LOGDATA, d));
		//
		// bw.flush();
		// bw.close();
		// }
	}

}
