package fieldtests.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import taskexecutor.TaskExecutor;
import taskexecutor.results.NEATPostEvaluationResult;
import taskexecutor.tasks.NEATMultipleSamplePostEvaluationTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.NEATPostEvaluation;

public class PostEvalExporter {
	
	private String[] exps;
	private static boolean SAVE_FILE = false;
	
	public PostEvalExporter(String dir, String ... exps) {
		this.exps = exps;
		
		for(int i = 0 ; i < this.exps.length ; i++) {
			exps[i] = dir+this.exps[i]+"/";
		}
	}
	
	public static void evalBest() throws Exception{
		File subFolder = new File("bigdisk/september2015/rudder_final/dispersion");
		
		ArrayList<RunData> data = BestControllersWeights.getData(subFolder);
		
		String genFile = subFolder.getPath()+"/1/show_best/showbest1.conf";
		
		JBotEvolver jbot = new JBotEvolver(new String[]{genFile});
		jbot.getArguments().get("--evaluation").setArgument("classname", "evaluation.DispersionFitnessTest");
		jbot.getArguments().get("--evaluation").setArgument("laststeps", "1");
		
		TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jbot, jbot.getArguments().get("--executor"));
		taskExecutor.start();
		
		for(RunData d : data) {
			
			int samples = 100;
			int sampleIncrement = 1;
			
			genFile = subFolder.getPath()+"/"+d.run+"/show_best/showbest"+d.gen+".conf";
			jbot = new JBotEvolver(new String[]{genFile});
			jbot.getArguments().get("--evaluation").setArgument("classname", "evaluation.DispersionFitnessTest");
			jbot.getArguments().get("--evaluation").setArgument("laststeps", "1");
			
			for(int sample = 0 ; sample < samples ; sample+=sampleIncrement) {
				JBotEvolver newJBot = new JBotEvolver(jbot.getArgumentsCopy(),jbot.getRandomSeed());
				NEATMultipleSamplePostEvaluationTask t = new NEATMultipleSamplePostEvaluationTask(d.run,d.gen,newJBot,1,newJBot.getPopulation().getBestChromosome(),sample,sample+sampleIncrement,0);
				taskExecutor.addTask(t);
			}
			
			for(int sample = 0 ; sample < samples ; sample+=sampleIncrement) {
				
				NEATPostEvaluationResult sfr = (NEATPostEvaluationResult)taskExecutor.getResult();
				System.out.println(sfr.getRun()+" "+sfr.getGeneration()+" "+sfr.getFitnesssample()+" "+sfr.getSample()+" "+sfr.getFitness());
			}
			System.out.println();
		}
		
		taskExecutor.stopTasks();
	}
	
	public void export() {
		
		String output = "posteval";
		
		File outputFolder = new File(output);
		if(!outputFolder.exists())
			outputFolder.mkdir();
		
		for(String s : exps) {
			
//			[run][generation][fitnesssample];
			
			String stringArguments = "samples=100 dir="+s;
			NEATPostEvaluation post = new NEATPostEvaluation(stringArguments.split(" "));
			
			double[][][] result = post.runPostEval();
			
			System.out.println(s);
			String out = s+"\n";
			
			for(int gen = 0 ; gen < result[0].length ; gen++) {
				System.out.print(gen+" ");
				out+= gen+"\t";
				for(int run = 0 ; run < result.length ; run++) {	
					System.out.print(result[run][gen][0]+" ");
					out+= result[run][gen][0]+"\t";
				}
				System.out.println();
				out+="\n";
			}
			
			if(SAVE_FILE) {
				String[] split = s.split("/");
				
				File outFile = new File(output+"/"+split[split.length-1]+".txt");
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
					bw.write(out);
					bw.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
//		evalBest();
		new PostEvalExporter("bigdisk/october2015/maritime/","intruder").export();
	}
}
