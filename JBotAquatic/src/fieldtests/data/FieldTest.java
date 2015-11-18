package fieldtests.data;

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

import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.LogData;

public class FieldTest {
	
	public static String[] EXPO_27_JULY = new String[]{
//		"dispersion;0;1;8;14-11-38;90","dispersion;0;2;8;14-16-42;90","dispersion;0;3;8;14-19-53;90",
//		"dispersion;1;1;8;14-41-33;90","dispersion;1;2;8;15-17-11;90","dispersion;1;3;8;15-19-38;90",
//		"dispersion;2;1;8;15-22-37;90","dispersion;2;2;8;15-25-08;90","dispersion;2;3;8;15-27-58;90",
//		"aggregate_waypoint;0;1;8;15-32-44;240",
//		"aggregate_waypoint;0;2;8;15-38-42;240",
//		"aggregate_waypoint;0;3;8;15-52-25;240",
//		"waypoint;0;1;8;17-11-15;180;1,2,3,4,5,6,7,10;4","waypoint;0;2;8;17-14-25;180;1,2,3,4,5,6,7,10;4","waypoint;0;3;8;17-19-08;180;1,2,3,4,5,6,7,10;4",
//		"waypoint;1;1;8;17-23-39;180;1,2,3,4,5,6,7,10;4","waypoint;1;2;8;17-28-15;180;1,2,3,4,5,6,7,10;4","waypoint;1;3;8;17-37-21;180;1,2,3,4,5,6,7,10;4",
//		"waypoint;2;1;8;17-40-39;180;1,2,3,4,5,6,7,10;4","waypoint;2;2;8;17-43-14;180;1,2,3,4,5,6,7,10;4","waypoint;2;3;8;17-46-46;180;1,2,3,4,5,6,7,10;4", 
	};
	
	public static String[] EXPO_29_JULY = new String[]{
//		"weightedcluster;0;1;4;19-07-17;180","weightedcluster;0;2;4;19-13-23;180","weightedcluster;0;3;4;19-17-32;180",
//		"weightedcluster;0;1;6;13-08-50;300","weightedcluster;0;2;6;13-26-13;300","weightedcluster;0;3;6;13-33-35;300",
//		"weightedcluster;0;1;8;11-11-37;300","weightedcluster;0;2;8;11-52-26;300","weightedcluster;0;3;8;10-58-52;300", //seed 0 missing robot 6! changed it manually in the onboard log
//		"weightedcluster;1;1;8;11-18-01;300","weightedcluster;1;2;8;11-45-46;300","weightedcluster;1;3;8;11-58-46;300",
//		"weightedcluster;2;1;8;11-31-52;300","weightedcluster;2;2;8;11-38-41;300","weightedcluster;2;3;8;12-04-59;300",
//		"dispersion;0;1;6;13-52-19;90","dispersion;0;2;6;13-48-19;90","dispersion;0;3;6;13-45-54;90",
//		"dispersion;0;1;4;19-00-06;90","dispersion;0;2;4;19-02-32;90","dispersion;0;3;4;19-04-35;90",
//		"composite;0;1;8;17-54-42;720",
//		"patrol;0;1;8;12-14-54;300",
//		"patrol;0;2;8;17-33-27;300",
//		"patrol;0;3;8;15-53-07;300",
//		"patrol_adaptive;0;1;8;19-47-06;900",//1,2,3,4->4,1 adaptive
//		"dispersion;0;1;8;20-06-21;300;1,2,3,4,6,7,8,10", //adaptive, only robots, starts with 4 (6,7,8,10), (1,2,3,4) afterwards
//		"dispersion;0;2;8;20-11-35;300;1,2,3,4,6,7,8,10,5,9", //adaptive, with kayak, not interesting!
	};
	
	public static String[] EXPO_25_SEP = new String[]{
//		"dispersion;2;0;4;12-09-41;90",
//		"dispersion;2;1;4;12-12-02;90",
//		"dispersion;2;2;4;12-14-33;90",
//		"dispersion;2;0;6;12-33-04;90",
//		"dispersion;2;1;6;12-35-45;90",
//		"dispersion;2;2;6;12-38-24;90;",
	};
	
	public static String[] EXPO_30_SEP = new String[]{
//		"dispersion;2;0;8;11-43-45;240",//adaptive, not used
//		"dispersion;2;1;8;11-50-41;240",//adaptive, used
//		"hierarchical;0;1;6;12-12-24;550;1,2,3,4,8,10",
//		"hierarchical;0;2;6;13-08-53;550;1,2,3,4,6,10",//robot 2 (jbot) had a malfunctioning motor
//		"hierarchical;0;3;6;13-22-45;550;1,2,3,4,6,10",
		"hierarchical;0;4;6;17-57-48;550;2,3,4,6,8,10",
	};
	
	public static String[] FOLDERS = new String[]{"27july","29july","25sep","30sep"};
	
	private static int folder = 3;

	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();
	private HashMap<Integer,ArrayList<LogData>> completeLogs = new HashMap<Integer, ArrayList<LogData>>();
	private int[] robots = new int[]{1,2,3,4,5,6,7,8,9,10};
	private int nSamples = 10;
	
	public static void main(String[] args) throws Exception{
		
		boolean export = false;
		
		if(export) {
			new FieldTest(EXPO_27_JULY);
			new FieldTest(EXPO_29_JULY);
			new FieldTest(EXPO_25_SEP);
			new FieldTest(EXPO_30_SEP);
		}else {
			
			folder = 0;
			boolean gui = true;
			
			for(String s :EXPO_27_JULY) {
				String file = "experiments/"+FOLDERS[folder]+"/"+s;
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
		        Experiment e = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
		        AssessFitness.compareFitness(e, 1,gui);
			}
			
			folder = 1;
			
			for(String s :EXPO_29_JULY) {
				String file = "experiments/"+FOLDERS[folder]+"/"+s;
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
		        Experiment e = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
		        AssessFitness.compareFitness(e, 1,gui);
			}
			
			folder = 2;
			
			for(String s :EXPO_25_SEP) {
				String file = "experiments/"+FOLDERS[folder]+"/"+s;
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
		        Experiment e = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
		        AssessFitness.compareFitness(e, 1,gui);
			}
			folder = 3;
			
			for(String s :EXPO_30_SEP) {
				String file = "experiments/"+FOLDERS[folder]+"/"+s;
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
		        Experiment e = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
		        AssessFitness.compareFitness(e, 1,gui);
			}
			
//			String s = "aggregate_waypoint;0;3;8;15-52-25;240";
//			String file = "experiments/"+FOLDERS[folder]+"/"+s;
//			ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
//	        Experiment e = (Experiment) objectinputstream.readObject();
//	        objectinputstream.close();
//	        AssessFitness.compareFitness(e, 1,true);
			
//	        double f;
//	        f = AssessFitness.getSimulatedFitness(e, 2, true);
//	        System.out.println(f);
//	        f = AssessFitness.getRealFitness(e, 1, true);
//	        System.out.println(f);
		}
		
	}
	
	public FieldTest(String[] descriptions) throws Exception{
		
		if(descriptions == EXPO_27_JULY)
			folder = 0;
		if(descriptions == EXPO_29_JULY)
			folder = 1;
		if(descriptions == EXPO_25_SEP)
			folder = 2;
		if(descriptions == EXPO_30_SEP)
			folder = 3;
		
		boolean readCompleteFiles = false;
		
		for(String s : descriptions) {
			
			String file = "experiments/"+FOLDERS[folder]+"/"+s;
			
			Experiment exp = null;
			
			if(!(new File(file).exists())) {
				
				if(!readCompleteFiles) {
					System.out.println("Reading from the files");
					populateCompleteLogs();
					readCompleteFiles = true;
				}
				
				exp = getExperiment(s);
				
				new File("experiments/"+FOLDERS[folder]).mkdir();
				
				FileOutputStream fout = new FileOutputStream(file);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(exp);
				oos.close();
				fout.close();
	        
			} else {
				ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(file));
		        exp = (Experiment) objectinputstream.readObject();
		        objectinputstream.close();
			}
	        
			experiments.add(exp);
			System.out.println(s+" "+exp.logs.size());
		}
		
		System.out.println();
		System.out.println("ASSESSING FITNESS");
		System.out.println();
		int j = 0;
		for(Experiment e : experiments) {
			double fitnessReal = AssessFitness.getRealFitness(e, 1);
			double fitnessSim = 0;
			for(int i = 1 ; i <= nSamples ; i++) {
				double f = AssessFitness.getSimulatedFitness(e, i,false);
				 fitnessSim+= f;
//				 System.out.print(f+" ");
			}
//			System.out.println();
			fitnessSim/=nSamples;
			System.out.println((j++)+"\t"+fitnessReal+"\t"+fitnessSim+"\t"+e);
//			System.out.println();
		}
		
	}
	
	private void populateCompleteLogs() throws IOException{
		
		for(int i : robots) {
			
			String file = "compare/"+FOLDERS[folder]+"/onboard/"+i;
			
			completeLogs.put(i, DroneLogExporter.getCompleteLogs(file));
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
				
				if(startTime != null && startTime.isBefore(timeFound))
					timeFound = startTime;
				
				startTime = timeFound;
				participatingRobots.add(i);
			}
		}
		
		if(split.length >= 7) {
			System.out.println("Manually defining the robots: "+split[6]);
			participatingRobots.clear();
			String[] splitRobots = split[6].split(",");
			for(String id : splitRobots)
				participatingRobots.add(Integer.parseInt(id));
		}
		
		if(startTime == null) {
			System.out.println("Can't find start time for experiment "+description);
			System.exit(0);
		}
		
		DateTime endTime = startTime.plus(duration*1000);
		
		if(nRobots != participatingRobots.size()) {
			System.out.print("Missing logs for some of the robots "+description+" [");
			for(int i : participatingRobots) {
				System.out.print(i+",");
			}
			System.out.println("]");
		}
		
		ArrayList<LogData> allData = new ArrayList<LogData>();
		
		for(int i : participatingRobots) {
			allData.addAll(DroneLogExporter.getLogs(completeLogs.get(i), startTime, endTime));
		}
		
		Collections.sort(allData);
		
		Experiment experiment = new Experiment();
		
		if(split.length == 8) {
			experiment.activeRobot = Integer.parseInt(split[7]);
		}
		
		experiment.robots = participatingRobots;
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
