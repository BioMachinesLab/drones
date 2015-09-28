package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import commoninterface.utils.logger.LogData;

public class BcastToOnboard {
	
	public static void main(String[] args) throws FileNotFoundException {
		Scanner s = new Scanner(new File("compare/25sep/onboard/8/bcast_6_3.txt"));
		
		while(s.hasNextLine()) {
			String l = s.nextLine();
//			System.out.println(l);
			String[] split = l.split(" ");
			
			String time = split[0];
			String log = split[1];
			
			if(log.startsWith("GPS")) {
				String[] logSplit = log.split(";");
				String ip = logSplit[1];
				
				if(!ip.endsWith(".8"))
					continue;
				
				String lat = logSplit[2];
				String lon = logSplit[3];
				String orientation = logSplit[4];
				
//				LT=LOGDATA	IP=192.168.3.8	DT=DRONE	ST=25-09-15_13:20:29.99	LL=38.766679696627456;-9.093102109002645	GO=59.85	GS=0.2778	GT=25-09-15_12:20:31.00	CO=71.0	TE=54.099998474121094;20.812	MS=0.0;0.0	
				String result = "LT=LOGDATA\tIP="+ip+"\tDT=DRONE\tST=25-09-15_"+time+"\t"+"LL="+lat+";"+lon+"\tGO="+orientation+"\tGS=0\tGT=25-09-15_"+time+"\tCO="+orientation;
				System.out.println(result);
			}
		}
	}

}
