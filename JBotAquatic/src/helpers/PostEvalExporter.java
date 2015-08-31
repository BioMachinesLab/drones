package helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import evolutionaryrobotics.NEATPostEvaluation;

public class PostEvalExporter {
	
	private String[] exps;
	
	public PostEvalExporter(String dir, String ... exps) {
		this.exps = exps;
		
		for(int i = 0 ; i < this.exps.length ; i++) {
			exps[i] = dir+this.exps[i]+"/";
		}
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
	
	public static void main(String[] args) {
		new PostEvalExporter("bigdisk/july2015/rudder_final/","waypoint").export();
	}
}
