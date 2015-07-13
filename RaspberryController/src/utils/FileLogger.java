package utils;

import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.input.OneWireTemperatureModuleInput;
import io.output.ControllerOutput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.dataobjects.GPSData;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.utils.RobotLogger;
import commoninterfaceimpl.RealAquaticDroneCI;

public class FileLogger extends Thread implements RobotLogger {
	
	private final static long SLEEP_TIME = 100;
	private final static long TOTAL_LOGS = 6000;	//10min
	public final static String LOGS_FOLDER="/home/pi/RaspberryController/logs/";
	
	
	private String fileName = "";
	private RealAquaticDroneCI drone;
	private DateTimeFormatter fileFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH-mm-ss");
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	
	private int logs = 0;
	private String ipAddr;

	public FileLogger(RealAquaticDroneCI drone) {
		this.drone = drone;
		ipAddr = drone.getNetworkAddress();
	}
	
	public BufferedWriter setupWriter() throws IOException {
		fileName = new LocalDateTime().toString(fileFormatter);

		File dir = new File(LOGS_FOLDER);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("[FileLogger] Created Logs Folder");
		}

		FileWriter fw = new FileWriter(new File(LOGS_FOLDER + "values_"
				+ fileName + ".log"));
		return new BufferedWriter(fw);
	}
	
	@Override
	public void run() {
		BufferedWriter bw = null;
		
		try {
			bw = setupWriter();
			
			while(true) {
				logs++;
				
				if(logs > TOTAL_LOGS) {
					bw.close();
					bw = setupWriter();
					logs = 0;
				}
				
				try {					
					String logLine = getLogString();
					bw.write(logLine);
					bw.flush();
				} catch(Exception e) {
					e.printStackTrace();
					// ignore :)
				}
				
				Thread.sleep(SLEEP_TIME);
			}

		} catch (InterruptedException e) {
			// this will happen when the program exits
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getLogString() {
		// TODO -> Convert to use the LogCodex
		List<ControllerInput> inputs = drone.getIOManager().getInputs();
		List<ControllerOutput> outputs = drone.getIOManager().getOutputs();
		
		String result = new LocalDateTime().toString(hourFormatter)+"\t";
		
		if(drone.getGPSLatLon() != null) {
			result+=drone.getGPSLatLon().getLat()+"\t"+drone.getGPSLatLon().getLon()+"\t"+drone.getGPSOrientationInDegrees()+"\t";
		} else {
			result+="0\t0\t0\t";
		}
		result+=drone.getCompassOrientationInDegrees()+"\t";
		
		for(ControllerInput i : inputs) {
			if(i instanceof GPSModuleInput) {
				GPSModuleInput ig = (GPSModuleInput)i;
				GPSData data = ig.getReadings();
				
				result+=data.getGroundSpeedKmh()+"\t"+data.getDate().toString(dateFormatter)+"\t";
			}
			
			if(i instanceof OneWireTemperatureModuleInput){
				OneWireTemperatureModuleInput ig = (OneWireTemperatureModuleInput)i;
				double[] data = ig.getReadings();

				result+=String.format("%.3f\t%.3f\t",data[0],data[1]);
			}
		}
		
		for(ControllerOutput o : outputs) {
			for(int i = 0 ; i < o.getNumberOfOutputs() ; i++)
				result+=o.getValue(i)+"\t";
		}
		
		result+=drone.getDroneType()+"\t";
		
		if(drone.getActiveBehavior() instanceof ControllerCIBehavior) {
			ControllerCIBehavior controller = (ControllerCIBehavior)drone.getActiveBehavior();
			
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
		// TODO Auto-generated method stub
		//TODO VASCOOOOO I NEED THIS!
		
	}

	@Override
	public void logError(String string) {
		// TODO Auto-generated method stub
		
	}
}
