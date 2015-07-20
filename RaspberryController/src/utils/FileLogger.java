package utils;

import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.input.OneWireTemperatureModuleInput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.controllers.ControllerCIBehavior;
import commoninterface.dataobjects.GPSData;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.utils.RobotLogger;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;
import commoninterface.utils.logger.LogData;
import commoninterfaceimpl.RealAquaticDroneCI;

public class FileLogger extends Thread implements RobotLogger {
	
	private final static long SLEEP_TIME = 100;
	private final static long TOTAL_LOGS = 3000;	//5min
	public final static String LOGS_FOLDER="/home/pi/RaspberryController/logs/";
	
	
	private String fileName = "";
	private RealAquaticDroneCI drone;
	private DateTimeFormatter fileFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH-mm-ss");
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
//	private DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("HH:mm:ss.SS");
	
	private int logs = 0;
	private String ipAddr;
	private ArrayList<String> toLog;

	// Unused for now
	// private String comment = null;

	public FileLogger(RealAquaticDroneCI drone) {
		this.drone = drone;
		ipAddr = drone.getNetworkAddress();
		toLog=new ArrayList<String>();
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
					String logLine = LogCodex.encodeLog(LogType.LOGDATA, getLogData());
					bw.write(logLine);
					bw.flush();
					
					synchronized (toLog) {
						if (!toLog.isEmpty()) {
							for (String str : toLog) {
								bw.write(str);
								bw.flush();
							}
							toLog.clear();
						}
					}
					
				} catch(Exception e) {
					e.printStackTrace();
					// ignore :)
				}
				
				Thread.sleep(SLEEP_TIME);
			}

		} catch (InterruptedException e) {
			// this will happen when the program exits
			System.err.println("Exiting Logger? "+e.getMessage());
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
	
	private LogData getLogData() {
		
		LogData data = new LogData();
		
		if(drone.getGPSLatLon() != null) {
			data.latLon = drone.getGPSLatLon();
			data.GPSorientation = drone.getGPSOrientationInDegrees();
		}
		
		data.compassOrientation = drone.getCompassOrientationInDegrees();
		
		List<ControllerInput> inputs = drone.getIOManager().getInputs();
		
		for(ControllerInput i : inputs) {
			if(i instanceof GPSModuleInput) {
				GPSModuleInput ig = (GPSModuleInput)i;
				GPSData gpsData = ig.getReadings();
				data.GPSspeed = gpsData.getGroundSpeedKmh();
				data.GPSdate = gpsData.getDate().toString(dateFormatter);
			}
			
			if(i instanceof OneWireTemperatureModuleInput){
				OneWireTemperatureModuleInput ig = (OneWireTemperatureModuleInput)i;
				data.temperatures = ig.getReadings();
			}
		}
		
		data.motorSpeeds = new double[]{drone.getLeftMotorSpeed(), drone.getRightMotorSpeed()};
		
		if(drone.getActiveBehavior() instanceof ControllerCIBehavior) {
			ControllerCIBehavior controller = (ControllerCIBehavior)drone.getActiveBehavior();
			
			CINeuralNetwork network = controller.getNeuralNetwork();
			
			if(network != null) {
				
				double[] in = network.getInputNeuronStates();
				double[] out = network.getOutputNeuronStates();
				
				data.inputNeuronStates = in;
				data.outputNeuronStates = out;
			}
		}
		
		data.ip = ipAddr;
		data.systemTime = new LocalDateTime().toString(dateFormatter);
		data.droneType = drone.getDroneType();

		// data.comment = this.comment;
		// this.comment = null;
		
		data.file = fileName;

		return data;
	}

	@Override
	public void stopLogging() {
		interrupt();
	}

	@Override
	public void logMessage(String string) {
		synchronized (toLog) {
			toLog.add(string);
		}
	}

	@Override
	public void logError(String string) {
		String str = LogCodex.encodeLog(LogType.ERROR, string);
		
		synchronized (toLog) {
			toLog.add(str);
		}
	}
}
