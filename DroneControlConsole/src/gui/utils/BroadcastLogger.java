package gui.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class BroadcastLogger {

	private static final String BROADCAST_LOGS_FOLDER = "broadcast_logs";

	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH-mm-ss");
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	
	private String experiment = "";
	private String experimentName = "";

	private BufferedWriter bw = null;
	
	private File folderExperiments;
	
	public BroadcastLogger() {
		
	}

	public void open(){
		String fileName = DateTime.now().toString(dateFormatter)+".log";

		try {
			File folder = new File(BROADCAST_LOGS_FOLDER);
			folderExperiments = new File(BROADCAST_LOGS_FOLDER+"/experiments");

			if(!folder.exists())
				folder.mkdir();
			
			if(!folderExperiments.exists())
				folderExperiments.mkdir();

			bw = new BufferedWriter(new FileWriter(new File(BROADCAST_LOGS_FOLDER+"/"+fileName)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void log(String msg) {
		if(bw != null) {
			try {
				String time = DateTime.now().toString(hourFormatter);
				String txt = time+" "+msg;
				bw.append(txt+"\n");
				
				handleExperiment(time,msg);
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleExperiment(String time, String msg) throws IOException {
		
		if(msg.startsWith("STARTING")) {
			String[] split = msg.split(" ");
			experimentName = split[1];
			experiment = "";
		}
		
		if(msg.startsWith("STOPPING") || msg.startsWith("STARTING")) {
			if(!experiment.isEmpty()) {
				String name = experimentName;
				String content = experiment;
				experimentName = "";
				experiment = "";
				
				File f = new File(folderExperiments.getPath()+"/"+name+".txt");
				BufferedWriter b = new BufferedWriter(new FileWriter(f));
				
				b.write(content);
				b.flush();
				b.close();
			}
		
		}else {
			if(!experimentName.isEmpty())
				experiment+=time+" "+msg+"\n";
		}
	}

	public void close() throws IOException{
		if(bw != null) {
			bw.close();
		}
		handleExperiment("","STOPPING exp");
	}

}
