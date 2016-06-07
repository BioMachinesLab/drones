package fieldtests.oceans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import simulation.util.Arguments;
import taskexecutor.ParallelTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;
import evolutionaryrobotics.JBotEvolver;

public class CoverageTest {
	
	public static void main(String[] args) {
		
		double subtract = 10;
		int nSamples = 10;
		
		int startDistance = 2500;
//		int robots = 50;
		int[] nRobots = new int[]{5,10,20,30,40,50};
		
		double[] chosenAreas = new double[]{2500*2500};
		int oneHour = 60*60*10;
		
		int[] times = new int[]{/*oneHour,2*oneHour,3*oneHour,*/4*oneHour};
		int faultTypes[] = new int[]{/*-60,-30,-15,0,*/25,50,75,100};
//		int faultTypes[] = new int[]{0};
		
		int unfault = 30;
		
		String[] files = new String[]{"ann_nogrid"};
		String[] allTypes = new String[]{"coverage"};
		String[] environment = new String[]{"square"};//"square","rectangle", "l"};
		
//		//29 is the best
//		double[] angleTolerance = new double[45];
//		
//		for(int i = 0 ; i < 45 ; i++) {
//			angleTolerance[i] = i;
//		}
		
		String folder = "grid/";
		String configName = folder+files[0]+"/"+files[0]+".conf";

		String fileOut = "oceans_coverage.txt";
		String out = "";
		
		int doneTasks = 0;
		
		try {
			
			JBotEvolver jBotEvolver = new JBotEvolver(new String[]{configName});
		
//			TaskExecutor taskExecutor = new ConillonTaskExecutor(jBotEvolver, new Arguments(""));
			TaskExecutor taskExecutor = new ParallelTaskExecutor(jBotEvolver, new Arguments(""));
			
			taskExecutor.setDescription("miguel 0/"+files.length*nSamples);
			taskExecutor.setTotalNumberOfTasks(files.length*nSamples);

			for(String env : environment) {
				for(int time : times) {
					for(int robots : nRobots) {
						for(int fault : faultTypes) {
							for(double area : chosenAreas) {
								for(String type : allTypes) {
									for(String controller : files) {
										
										configName = folder+controller+"/"+controller+".conf";
							
										double size = Math.sqrt(area);
										
										args = new String[]{"--robots","+numberofrobots="+robots,"--environment","+width="+size+",environment="+env+",height="+size+",wallsdistance="+size/2+",steps="+time+",griddecay=0,distance="+startDistance/*,"--controllers","+angletolerance="+angle*/};
										
										String s = "";
										for(int i = 0 ; i < args.length ; i++)
											s+=args[i]+" ";
										
										if(fault < 0) {
											s+=" --updatables +fault=(classname=FaultInjection,faulteveryminutes="+Math.abs(fault)+",unfaulteveryminutes="+Math.abs(unfault)+")";
										} else {
											s+=" --updatables +fault=(classname=ProbabilityFaultInjection,deadrobots="+((fault/100.0)*robots)+",nrobots="+robots+")";
										}
										
										for(int sample = 0 ; sample < nSamples ; sample++) {
				 							String current = s+" --updatables +export=(classname=ExportPositions,time=100,file=("+type+","+controller+","+robots+","+size+","+time+","+sample+","+fault+"))";
	//			 							String current = s;
											
				 							System.out.println(current);
//				 							if(1==1)continue;
											
											jBotEvolver.loadFile(configName, current);
											
											SingleSamplePostEvaluationTask task =
													new SingleSamplePostEvaluationTask(
															robots,
															new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed()),
															0, jBotEvolver.getPopulation().getBestChromosome(), sample, 0);
											
											taskExecutor.addTask(task);
										}
										
										for(int sample = 0 ; sample < nSamples ; sample++) {
											doneTasks++;
											taskExecutor.setDescription("miguel "+doneTasks+"/"+files.length*nSamples);
											PostEvaluationResult result = (PostEvaluationResult)taskExecutor.getResult();
											String currentResult = type+"\t"+controller+"\t"+robots+"\t"+size+"\t"+time+"\t"+fault+"\t"+sample+"\t"+(result.getFitness()-subtract);
											out+=currentResult+"\n";
											System.out.println(currentResult);
										}
									}
							}
							}
						}
					}
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileOut)));
			bw.write(out);
			bw.close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
