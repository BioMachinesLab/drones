package fieldtests.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import simulation.util.Arguments;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import evolutionaryrobotics.JBotEvolver;

public class BestControllersWeights {
	
	static int TOP = 1;
	
	public static void main(String[] args) {
		
		String folder = "bigdisk/october2015/maritime100/";
		
		File f = new File(folder);
		
		if(!f.isDirectory()) {
			System.out.println("Can't find folder "+folder);
			System.exit(0);
		}
		
		File save = new File("exported_controllers");
		save.mkdir();
		
		for(File subFolder : f.listFiles()) {
			if(subFolder.isDirectory()) {
				System.out.println("Going for "+subFolder.getName());
				
				try {
					
					ArrayList<RunData> data = getData(subFolder);
					
					for(int i = 0 ; i < TOP ; i++) {
						
						String result = "";
						
						RunData d = data.get(i);
						String genFile = subFolder.getPath()+"/"+d.run+"/show_best/showbest"+d.gen+".conf";
						JBotEvolver jbot = new JBotEvolver(new String[]{genFile});
						
						double[] weights = jbot.getPopulation().getBestChromosome().getAlleles();
						
						Arguments robotArgs = jbot.getArguments().get("--robots");
						Arguments sensors = new Arguments(robotArgs.getArgumentAsString("sensors"));
						
						result+="type=(ControllerCIBehavior),";
						result+="sensors=(";
						
						String sensorArgsStr = "";
						int sensorNumber = 1;
						
						for(int argNumber = 0 ; argNumber < sensors.getNumberOfArguments() ; argNumber++) {
							Arguments wrapper = new Arguments(sensors.getArgumentAsString(sensors.getArgumentAt(argNumber)));
							String ci = wrapper.getArgumentAsString("ci");
							sensorArgsStr+="Sensor"+(sensorNumber++)+"=("+ci+"),";
						}
						
						Arguments sensorArgs = new Arguments(sensorArgsStr);
						
						result+=sensorArgs.getCompleteArgumentString();
						
						String weightsStr = implode(weights);
						
						result+="),";
						result+="network=(";
						
						Arguments controllerArgs = jbot.getArguments().get("--controllers");
						controllerArgs = new Arguments(controllerArgs.getArgumentAsString("network"));
						controllerArgs.setArgument("weights", weightsStr);
						
						result+=controllerArgs.getCompleteArgumentString();
						
						result+=")";
						
						result = Arguments.beautifyString(result);
						
						File saving = new File(save.getPath()+"/"+subFolder.getName()+i+".conf");
						BufferedWriter wr = new BufferedWriter(new FileWriter(saving));
						wr.write(result);
						wr.flush();
						wr.close();
					}
					
					
				
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static ArrayList<RunData> getData(File subFolder) throws IOException{
		String post = subFolder.getPath()+"/post.txt";
		
		File postFile = new File(post);
	
		Scanner s = new Scanner(postFile);
		s.nextLine();
		
		ArrayList<RunData> data = new ArrayList<RunData>();
		
		while(s.hasNextLine()) {
			
			String line = s.nextLine();
			
			if(!line.startsWith("O")) {
				
				Scanner lineScanner = new Scanner(line);
				
				RunData d = new RunData();
				
				d.run = lineScanner.nextInt();
				d.fitness = lineScanner.nextDouble();
				
				String[] split = line.trim().split(" ");
				int gen = Integer.parseInt(split[split.length-1]);
				d.gen = gen;
				
				data.add(d);
				
				lineScanner.close();
			}
		}
		
		s.close();
		
		Collections.sort(data);
		
		return data;
	}
	
	public static String implode(double[] vals) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vals.length; i++) {
		    sb.append(vals[i]);
		    if (i != vals.length - 1) {
		        sb.append(",");
		    }
		}
		return sb.toString();
	}
}

class RunData implements Comparable<RunData>{
	
	int run;
	double fitness;
	int gen;

	@Override
	public int compareTo(RunData o) {
		return (int)((o.fitness - fitness)*100000.0);
	}
	
}
