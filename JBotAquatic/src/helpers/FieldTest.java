package helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.joda.time.DateTime;

import commoninterface.utils.logger.LogData;

public class FieldTest {
	
	public static String[] EXPO_27_JULY = new String[]{
		/*	"dispersion;0;1;8;14-11-38;90",
		"dispersion;0;2;8;14-16-42;90",
		"dispersion;0;3;8;14-19-53;90",
		
		"dispersion;1;1;8;14-41-33;90",
		"dispersion;1;2;8;15-17-11;90",
		"dispersion;1;3;8;15-19-38;90",
		
		"dispersion;2;1;8;15-22-37;90",
		"dispersion;2;2;8;15-25-08;90",
		"dispersion;2;3;8;15-27-58;90",
		
		"aggregate_waypoint0;1;8;15-32-44;240",
		"aggregate_waypoint0;2;8;15-38-42;240",
		"aggregate_waypoint0;3;8;15-52-25;240",
		
		"waypoint0;1;8;17-11-15;0",
		"waypoint0;2;8;17-14-25;0",
		"waypoint0;3;8;17-19-08;0",
		
		"waypoint1;1;8;17-23-39;0",
		"waypoint1;2;8;17-28-15;0",
		"waypoint1;3;8;17-37-21;0",
		
		"waypoint2;1;8;17-40-39;0",
		"waypoint2;2;8;17-43-14;0",
		"waypoint2;3;8;17-46-46;0", 
		
		"patrol0;1;8;17-58-41;1080",*/
		
		"cluster;0;1;8;19-00-33;180",
		"cluster;0;2;8;19-05-14;180",
		"cluster;0;3;8;19-10-47;180",
		};

	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();
	private HashMap<Integer,ArrayList<LogData>> completeLogs = new HashMap<Integer, ArrayList<LogData>>();
	private int[] robots = new int[]{1,2,3,4,5,6,7,8,9,10};
	private int nSamples = 100;
	
	public static void main(String[] args) throws Exception{
//		new FieldTest(EXPO_27_JULY);
		String s = EXPO_27_JULY[0];
		ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream("experiments/"+s));
        Experiment e = (Experiment) objectinputstream.readObject();
        objectinputstream.close();
        double f = AssessFitness.getSimulatedFitness(e, 1, true);
        System.out.println(f);
//        f = AssessFitness.getRealFitness(e, 1, true);
//        System.out.println(f);
		
	}
	
	public FieldTest(String[] descriptions) throws Exception{
		
		boolean readCompleteFiles = false;
		
		/*
		if(new File("temp.io").exists()) {
			System.out.print("Getting logs from TEMP file...");

	        ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream("temp.io"));
	        completeLogs = (HashMap<Integer,ArrayList<LogData>>) objectinputstream.readObject();
	        objectinputstream.close();
	        
	        System.out.println(" Done!");
			
		} else {
			System.out.println("Getting logs from ORIGINAL files");
			
			populateCompleteLogs();
			
//			FileOutputStream fout = new FileOutputStream("temp.io");
//			ObjectOutputStream oos = new ObjectOutputStream(fout);
//			oos.writeObject(completeLogs);
//			oos.close();
//			fout.close();
		}*/
		
		for(String s : descriptions) {
			
			Experiment exp = null;
			
			if(!(new File("experiments/"+s).exists())) {
				
				if(!readCompleteFiles) {
					System.out.println("Reading from the files");
					populateCompleteLogs();
					readCompleteFiles = true;
				}
				
				exp = getExperiment(s);
				
				FileOutputStream fout = new FileOutputStream("experiments/"+s);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(exp);
				oos.close();
				fout.close();
	        
			} else {
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream("experiments/"+s));
		        exp = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
			}
	        
			experiments.add(exp);
			System.out.println(s+" "+exp.logs.size());
		}
		
		System.out.println();
		System.out.println("ASSESSING FITNESS");
		System.out.println();
		
		for(Experiment e : experiments) {
			System.out.println(e);
			double fitnessReal = AssessFitness.getRealFitness(e, 1);
			double fitnessSim = 0;
			for(int i = 1 ; i <= nSamples ; i++) {
				 fitnessSim+= AssessFitness.getSimulatedFitness(e, i);
			}
			fitnessSim/=nSamples;
			System.out.println("Real: "+fitnessReal+" Sim:"+fitnessSim);
			System.out.println();
		}
		
	}
	
	private void populateCompleteLogs() throws IOException{
		
		for(int i : robots) {
			completeLogs.put(i, DroneLogExporter.getCompleteLogs(i));
			System.out.println("Robot "+i+": "+completeLogs.get(i).size()+" logs");
		}
		
	}
	
	public Experiment getExperiment(String description) throws IOException{
		
		String[] split = description.split(";");
		
		String controller = split[0];
		int controllerNumber = Integer.parseInt(split[1]);
		int sample = Integer.parseInt(split[2]);
		int nRobots = Integer.parseInt(split[3]);
		String experimentName = split[4];
		int duration = Integer.parseInt(split[5]);
		
		DateTime startTime = null;
		
		ArrayList<Integer> participatingRobots = new ArrayList<Integer>();
		
		for(int i : robots) {
			DateTime timeFound = DroneLogExporter.getStartTime(completeLogs.get(i), experimentName);
			if(timeFound != null) {
				startTime = timeFound;
				participatingRobots.add(i);
			}
		}
		
		if(startTime == null) {
			System.out.println("Can't find start time for experiment "+description);
			System.exit(0);
		}
		
		DateTime endTime = startTime.plus(duration*1000);
		
		if(nRobots != participatingRobots.size()) {
			System.out.println("Missing logs for some of the robots "+description);
		}
		
		ArrayList<LogData> allData = new ArrayList<LogData>();
		
		for(int i : participatingRobots) {
			allData.addAll(DroneLogExporter.getLogs(completeLogs.get(i), startTime, endTime));
		}
		
		Collections.sort(allData);
		
		Experiment experiment = new Experiment();
		
		experiment.timeSteps = duration*10;
		experiment.start = startTime;
		experiment.end = endTime;
		experiment.controllerNumber = controllerNumber;
		experiment.sample = sample;
		experiment.controllerName = controller;
		experiment.logs = allData;
		
		return experiment;
	}
	
}
