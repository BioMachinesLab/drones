package gui.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class IncidentLogger {

	private static final String INCIDENTS_FOLDER = "incidents_logs";

	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY");
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");

	private BufferedWriter bw = null;

	public synchronized void open(){
		String fileName = DateTime.now().toString(dateFormatter)+".txt";

		try {
			File folder = new File(INCIDENTS_FOLDER);

			if(!folder.exists())
				folder.mkdir();

			bw = new BufferedWriter(new FileWriter(new File(INCIDENTS_FOLDER+"/"+fileName),true));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void log(String msg) {
		if(bw == null){
			open();
		}
		
		if(bw != null) {
			try {
				String txt = "# "+DateTime.now().toString(hourFormatter)+" "+msg;
				bw.append(txt+"\n");
				bw.flush();
				close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void close() throws IOException{
		if(bw != null) {
			bw.close();
			bw = null;
		}
	}
}