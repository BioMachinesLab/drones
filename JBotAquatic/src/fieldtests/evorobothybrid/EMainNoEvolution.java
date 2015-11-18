package fieldtests.evorobothybrid;

import simulation.Simulator;
import evolutionaryrobotics.JBotEvolver;

public class EMainNoEvolution {
	
	public static void main(String[] args) {
		
		double sArea = 100*100;
		int sRobots = 5;
		int maxRuns = 10;
		int maxSamples = 100;
		int sTime = 5000;
		
		for(int run = 1 ; run <= maxRuns ; run++) {
			
			double area = sArea*run;
		
			String configName = "h_tests/test.conf";
			
			double size = Math.sqrt(area);
			int nRobots = sRobots*run;
			int time = (int)(size/100 * sTime);
			
			System.out.println("Robots:"+nRobots+" Area:"+(int)size+" Time:"+time);
			
			for(int sample = 0 ; sample < maxSamples ; sample++) {
				
				args = new String[]{"--robots","+numberofrobots="+nRobots,"--environment","+fencew="+size+",fenceh="+size+",steps="+time,"--random-seed",""+sample};
				
				/*
				
		//		SCALABILITY EXPERIMENTS UP TO 1000
				
				int range = Integer.parseInt(args[0]);
				int robots = Integer.parseInt(args[1]);
				System.out.println("range: "+range+" robots:"+robots);
				
				String configName = "swarm/mission"+range+".conf";
				args = new String[]{"--robots","+numberofrobots="+robots};
				
				*/
		
				String s = "";
				
				for(int i = 0 ; i < args.length ; i++)
					s+=args[i]+" ";
				
				try {
					JBotEvolver jBotEvolver = new JBotEvolver(new String[]{configName});
					jBotEvolver.loadFile(configName, s);
					Simulator sim = jBotEvolver.createSimulator();
					jBotEvolver.setupBestIndividual(sim);
					sim.simulate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
