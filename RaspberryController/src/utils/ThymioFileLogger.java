package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.utils.RobotLogger;
import commoninterfaceimpl.RealThymioCI;

public class ThymioFileLogger extends Thread implements RobotLogger  {
	
	private final static long SLEEP_TIME = 100;
	
	private String fileName = "";
	private RealThymioCI thymio;
	private String extraLog = "";
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("d-M-Y_H:m:s.S");
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("H:m:s.S");
	
	public ThymioFileLogger(RealThymioCI thymio) {
		this.thymio = thymio;
		fileName = new LocalDateTime().toString(dateFormatter);
	}
	
	@Override
	public void run() {
		
		BufferedWriter bw = null;
		
		try {
		
			FileWriter fw = new FileWriter(new File("logs/values_"+fileName+".log"));
			bw = new BufferedWriter(fw);
			
			while(true) {
				try {
					
					if(!extraLog.isEmpty()) {
						bw.write(extraLog);
						extraLog = "";
					}
					
					bw.write(getLogString());
					bw.flush();
				} catch(Exception e) {
					//ignore :)
				}
				Thread.sleep(SLEEP_TIME);
			}
			
		} catch(InterruptedException e) {
			//this will happen when the program exits
		} catch(Exception e) {
			e.printStackTrace();
		} finally { 
			try {
			if(bw != null)
				bw.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getLogString() {
		
		String result = new LocalDateTime().toString(hourFormatter)+"\t";
		
		result+=thymio.getVirtualPosition().x+"\t"+thymio.getVirtualPosition().y+"\t"+thymio.getVirtualOrientation();
		
		if(thymio.getActiveBehavior() instanceof ControllerCIBehavior) {
			ControllerCIBehavior controller = (ControllerCIBehavior)thymio.getActiveBehavior();
			
			CINeuralNetwork network = controller.getNeuralNetwork();
			
			if(network != null) {
				
				result+="network\t";
				
				double[] in = network.getInputNeuronStates();
				double[] out = network.getOutputNeuronStates();
				
				for(double d : in)
					result+=d+"\t";
				for(double d : out)
					result+=d+"\t";
			}
			
		}
		return result+"\n";
	}

	@Override
	public void stopLogging() {
		interrupt();
	}

	@Override
	public void logMessage(String string) {
		this.extraLog+= "#"+string+"\n";
	}

	@Override
	public void logError(String string) {
		this.extraLog+="ERROR: "+string;
	}
}
