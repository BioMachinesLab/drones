package fieldtests.evorobothybrid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import simulation.util.Arguments;
import taskexecutor.ParallelTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;
import evolutionaryrobotics.JBotEvolver;

public class EvoRobotHeatmap {
	
	public static void main(String[] args) {
		
		double sArea = 100*100;
		int sRobots = 5;
		int maxRuns = 10;
		int maxSamples = 100;
		int sTime = 5000;
		double areaPerRobot = sArea/(double)sRobots;
		
		int[] chosenRobots = new int[]{5/*,10,20,30,40,50*/};
		double[] chosenAreas = new double[chosenRobots.length];
		
		for(int i = 0 ; i < chosenAreas.length ; i++)
			chosenAreas[i] = chosenRobots[i]*areaPerRobot;
		
		String configName = "h_tests/test.conf";
		
		String file="heatmap_3.txt";
		String out = "";
		
		String[] allTypes = new String[]{/*"averagerobotsseeing","coverage",*/"percentageseeing"};
		
		int doneTasks = 0;
		
		try {
			
			JBotEvolver jBotEvolver = new JBotEvolver(new String[]{configName});
		
//			TaskExecutor taskExecutor = new ConillonTaskExecutor(jBotEvolver, new Arguments(""));
			TaskExecutor taskExecutor = new ParallelTaskExecutor(jBotEvolver, new Arguments(""));
			
			taskExecutor.setDescription("miguel 0/"+maxRuns*maxSamples);
			taskExecutor.setTotalNumberOfTasks(maxRuns*maxSamples);
			
			for(String type : allTypes) {
				for(int nR = 0 ; nR < chosenRobots.length ; nR++) {
					
					int nRobots = chosenRobots[nR];
					
					for(int nA = 0 ; nA < chosenAreas.length ; nA++) {
						
						double area = chosenAreas[nA];
					
						double size = Math.sqrt(area);
						int time = (int)(size/100 * sTime);
						
	//					System.out.println("Robots:"+nRobots+" Area:"+size+" Time:"+time);
	//					if(1==1)continue;
						
						args = new String[]{"--robots","+numberofrobots="+nRobots,"--environment","+fencew="+size+",fenceh="+size+",steps="+time,"--evaluation","+type="+type};
						
						String s = "";
						for(int i = 0 ; i < args.length ; i++)
							s+=args[i]+" ";
						
		//				System.out.println(s.replace("--", "\n--"));
						
						jBotEvolver.loadFile(configName, s);
						
						for(int sample = 0 ; sample < maxSamples ; sample++) {
							
							SingleSamplePostEvaluationTask task =
									new SingleSamplePostEvaluationTask(
											nRobots,
											new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed()),
											0, jBotEvolver.getPopulation().getBestChromosome(), sample, 0);
							
							taskExecutor.addTask(task);
						}
						
						for(int sample = 0 ; sample < maxSamples ; sample++) {
							doneTasks++;
							taskExecutor.setDescription("miguel "+doneTasks+"/"+maxRuns*maxSamples);
							PostEvaluationResult result = (PostEvaluationResult)taskExecutor.getResult();
							String currentResult = type+"\t"+nRobots+"\t"+size+"\t"+time+"\t"+result.getFitness();
							out+=currentResult+"\n";
							System.out.println(currentResult);
						}
					}
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
					bw.write(out);
					bw.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
