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

public class EvoRobotFaultTolerance {
	
	public static void main(String[] args) {
		
		double areaPerRobot = (100*100)/(double)5;
		
//		double[] faults = new double[]{30,20,10,5,3,2,1,0.5};
		
		double[] faults = new double[]{30,15,5,3,1,0.5};
		
		int[] chosenRobots = new int[]{5,10,20,30,40,50};
		double[] chosenAreas = new double[chosenRobots.length];
		
		for(int i = 0 ; i < chosenAreas.length ; i++)
			chosenAreas[i] = chosenRobots[i]*areaPerRobot;
		
		int[] times = new int[chosenAreas.length];
		
		for(int i = 0 ; i < times.length ; i++)
			times[i] = (int)(Math.sqrt(chosenAreas[i])/100 * 5000);
		
		int maxSamples = 10;
		
		String[] files = new String[]{/*"0",*/"1","3","999"};
		String[] allTypes = new String[]{"percentageseeing"/*,"coverage","averagerobotsseeing"*/};
		
		String configName = "h_tests/test.conf";
		
		String prefix = "controllerfile=h_tests/hierarchical_pursuit_";
		
		String fileOut = "faults.txt";
		String out = "";
		
		int doneTasks = 0;
		
		try {
			
			JBotEvolver jBotEvolver = new JBotEvolver(new String[]{configName});
		
//			TaskExecutor taskExecutor = new ConillonTaskExecutor(jBotEvolver, new Arguments(""));
			TaskExecutor taskExecutor = new ParallelTaskExecutor(jBotEvolver, new Arguments(""));
			
			taskExecutor.setDescription("miguel 0/"+files.length*maxSamples);
			taskExecutor.setTotalNumberOfTasks(files.length*maxSamples);
			
			for(String type : allTypes) {
				for(String controller : files) {
					for(double fault : faults) {
						for(int r = 0 ; r < chosenRobots.length ; r++) {
								
							double area = chosenAreas[r];
							int robot = chosenRobots[r];
							double size = Math.sqrt(area);
							
							args = new String[]{
									"--robots","+numberofrobots="+robot,
									"--environment","+fencew="+size+",fenceh="+size+",steps="+times[r]+","+prefix+controller+".conf",
									"--evaluation","+type="+type,
									"--updatables FaultInjection=(classname=FaultInjection,faulteveryminutes="+fault+")"};
							
							String s = "";
							for(int i = 0 ; i < args.length ; i++)
								s+=args[i]+" ";
							
//							System.out.println(s);
//							if(1==1)continue;
							
							jBotEvolver.loadFile(configName, s);
							
							for(int sample = 0 ; sample < maxSamples ; sample++) {
								
								SingleSamplePostEvaluationTask task =
										new SingleSamplePostEvaluationTask(
												robot,
												new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed()),
												0, jBotEvolver.getPopulation().getBestChromosome(), sample, 0);
								
								taskExecutor.addTask(task);
							}
							
							for(int sample = 0 ; sample < maxSamples ; sample++) {
								doneTasks++;
								taskExecutor.setDescription("miguel "+doneTasks+"/"+files.length*maxSamples);
								PostEvaluationResult result = (PostEvaluationResult)taskExecutor.getResult();
								String currentResult = type+"\t"+fault+"\t"+controller+"\t"+robot+"\t"+size+"\t"+times[r]+"\t"+result.getFitness();
								out+=currentResult+"\n";
								System.out.println(currentResult);
							}
						}
					}
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileOut)));
			bw.write(out);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
