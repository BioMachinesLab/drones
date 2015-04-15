package io.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileGPSModuleInput extends GPSModuleInput {
	
	private static final long INITIAL_SLEEP = 10*1000;
	private static final long CYCLE_SLEEP = 5;//25
	private static final String FILENAME = "logs/fakegps.log";
	private Scanner s = null;
	
	public FileGPSModuleInput() {
		super(true);
		messageParser = new MessageParser();
		messageParser.start();
		
		try {
			s = new Scanner(new File(FILENAME));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		FakeInputThread t = new FakeInputThread();
		t.start();

		available = true;
	}
	
	class FakeInputThread extends Thread {
		
		@Override
		public void run() {
			
			
			System.out.println("Wait "+INITIAL_SLEEP+" milisec for FakeGPS");
			try {
				Thread.sleep(INITIAL_SLEEP);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			System.out.println("FakeGPS starting!");
			
			StringBuilder data = new StringBuilder();
			
			while(s.hasNextLine()) {
				String line = s.nextLine().trim()+"\r\n";
				data.append(line);
			}
			
			System.out.println("Read from file ("+data.length()+")");
			
			int increment = 0;
			
			for(int i = 0 ; i < data.length() ; i+=increment) {
				
				increment = (int)(Math.random()*10+20);
				
				if(i+increment <= data.length()) {
				
					String substring = data.substring(i, i+increment);
					
					dataReceived(substring);
					try {
						Thread.sleep(CYCLE_SLEEP);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			System.out.println("FakeGPS ending!");
		}
		
		public void dataReceived(String received) {
			
			receivedDataBuffer.append(received);
			
			boolean keepGoing = true;

			while (keepGoing) {
				keepGoing = false;
				int indexFirstDollar = receivedDataBuffer.indexOf("$");
				
				if(indexFirstDollar >= 0) {
					
					int indexSecondDollar = receivedDataBuffer.indexOf("$",indexFirstDollar+1);
					
					if(indexSecondDollar > 0) {
					
						String sub = receivedDataBuffer.substring(indexFirstDollar,indexSecondDollar).trim();
		
						if (messageParser != null && !sub.isEmpty() && sub.charAt(0) == '$') {
							messageParser.processReceivedData(sub);
							
							receivedDataBuffer.delete(0,indexSecondDollar);
							keepGoing = true;
						}
					}
				}
			}
		}
	}
}