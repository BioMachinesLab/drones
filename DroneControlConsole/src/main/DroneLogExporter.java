package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.entities.Entity;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;
import commoninterface.utils.logger.LogData;

public class DroneLogExporter {
	
	String[] experiments= new String[]{
			"2,3,4,5,6,8,9,10;aggregate0_8_1;22-07-15_10:52:16.00;180",
			"2,3,4,5,6,8,9,10;aggregate0_8_2;22-07-15_10:55:53.00;180",
			"2,3,4,5,6,7,8,9;aggregate0_8_3;22-07-15_11:05:20.00;180",
			"2,3,4,5,6,7,8,9;aggregate1_8_1;22-07-15_11:13:24.00;180",
			"2,3,4,5,6,7,8,9;aggregate1_8_2;22-07-15_11:17:26.00;180",
			"2,3,4,5,6,8,9,10;aggregate1_8_3;22-07-15_12:00:00.00;180",
			"2,3,4,5,6,8,9,10;aggregate2_8_1;22-07-15_12:04:31.00;180",
			"2,3,4,5,6,8,9,10;aggregate2_8_2;22-07-15_12:09:38.00;180",
			"2,3,4,5,6,8,9,10;aggregate2_8_3;22-07-15_12:14:59.00;180",
			};
	
	DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	
	public DroneLogExporter() throws Exception{
		
		for(String experiment : experiments) { 
			
			String[] split = experiment.split(";");
		
			ArrayList<LogData> allData = new ArrayList<LogData>();
			
			for(String d : split[0].split(",")) {
				ArrayList<LogData> extracted = getLogs(d,DateTime.parse(split[2],formatter),DateTime.parse(split[2],formatter).plus(Integer.parseInt(split[3])*1000));
				System.out.println(experiment+" "+d+" "+extracted.size());
				allData.addAll(extracted);
			}
			
			Collections.sort(allData);
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(split[1]+".txt")));
			
			for(LogData d : allData)
				bw.write(LogCodex.encodeLog(LogType.LOGDATA, d));
			
			bw.flush();
			bw.close();
		}
	}
	
	public ArrayList<LogData> getLogs(String drone, DateTime start, DateTime end) throws Exception{
		
		File folder = new File("drone_logs/"+drone);
		
		ArrayList<LogData> result = new ArrayList<LogData>();

		for (String file : folder.list()) {
			if (!file.contains(".log")|| !file.contains("values"))
				continue;

//			System.out.println("Parsing file: " + file);
			Scanner s = new Scanner(new File(folder.getPath() + "/" + file));

			ArrayList<Entity> currentEntities = new ArrayList<Entity>();

			while (s.hasNext()) {
				String l = s.nextLine();

				if (!l.startsWith(LogCodex.COMMENT_CHAR) && !l.isEmpty()) {
					DecodedLog decodedData = LogCodex.decodeLog(l);
					
					if(decodedData == null)
						continue;

					switch (decodedData.payloadType()) {
					case ENTITIES:
						currentEntities = (ArrayList<Entity>) LogCodex.decodeLog(l, currentEntities).getPayload();
						break;

					case LOGDATA:
						
						Object o = decodedData.getPayload();
						
						if(o == null)
							continue;
						
						LogData d = (LogData)o;
//						d.entities = currentEntities;

						if(d != null && d.systemTime != null) {
							DateTime date = DateTime.parse(d.systemTime,formatter);
							
							if(date.isAfter(start) && date.isBefore(end)) {
								result.add(d);
							}
						}
						break;
						
					case ERROR:
//						messageArea.setForeground(Color.RED);
//						messageArea.setText((String) decodedData.getPayload());
						break;

					case MESSAGE:
//						messageArea.setForeground(Color.BLACK);
//						messageArea.setText((String) decodedData.getPayload());
						break;
					}
				}
			}

			s.close();
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		new DroneLogExporter();
	}

}
