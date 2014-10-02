package main;

import io.SystemInfoMessageProvider;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.output.ControllerOutput;
import io.output.DebugLedsOutput;
import io.output.ESCManagerOutputThreadedImprov;

import java.io.IOException;
import java.util.ArrayList;

import network.ConnectionHandler;
import network.ConnectionListener;
import network.MotorConnectionListener;
import network.messages.GPSMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.MotorMessage;
import network.messages.SystemStatusMessage;

import com.pi4j.io.serial.SerialPortException;

import dataObjects.MotorSpeeds;

public class Controller {
	private GPSModuleInput gpsModule;
	private ESCManagerOutputThreadedImprov escManager;
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	
	private ArrayList<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();
	
	private String status = "";
	private String initMessages = "";
	private MotorSpeeds speeds;
	private DebugLedsOutput debugLeds;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {

		speeds = new MotorSpeeds();

		addShutdownHooks();

		initModules();
	}

	public void processMotorMessage(MotorMessage message) {
		speeds.setSpeeds(message);
	}

	public void processInformationRequest(Message request, ConnectionHandler conn) {
		
		Message response = null;
		
		for(MessageProvider p : messageProviders) {
			response = p.getMessage(request);
			if(response != null)
				break;
		}
		
		if(response == null)
			response = new SystemStatusMessage("No message provider for the current request ("+request.getClass().getSimpleName()+")");
		
		conn.sendData(response);
		
	}

	public String getInitialMessages() {
		return initMessages;
	}
	
	private void initModules() {
		
		System.out.println("######################################");
		
		setStatus("Initializing...");
		
		initInputs();
		initOutputs();
		
		initMessageProviders();
		
		initConnections();
		
		setStatus("Running");

		System.out.println(initMessages);
	}
	
	private void initMessageProviders() {
		
		messageProviders.add(new SystemInfoMessageProvider());
		messageProviders.add(new SystemStatusMessageProvider(this));
		
		for(ControllerInput i : inputs) {
			if(i instanceof MessageProvider)
				messageProviders.add((MessageProvider)i);
		}
		
		for(ControllerOutput o : outputs) {
			if(o instanceof MessageProvider)
				messageProviders.add((MessageProvider)o);
		}
	}
	
	private void initInputs() {
		gpsModule = new GPSModuleInput();
		initMessages += "\n[INIT] GPSModule: "+ (gpsModule.isAvailable() ? "ok" : "not ok!") +"\n";
		gpsModule.enableLocalLog();
		System.out.print(".");
		
		inputs.add(gpsModule);
		
		// batteryManager = new BatteryManagerInput();
		// compassModule = new CompassModuleInput();
	}
	
	private void initOutputs() {
		escManager = new ESCManagerOutputThreadedImprov(speeds);
		initMessages += "[INIT] ESCManager: "+ (escManager.isAvailable() ? "ok" : "not ok!") +"\n";
		if(escManager.isAvailable())
			escManager.start();
		System.out.print(".");

		debugLeds = new DebugLedsOutput();
		initMessages += "[INIT] DebugLEDs: "+ (debugLeds.isAvailable() ? "ok" : "not ok!") +"\n";
		System.out.print(".");
		
		outputs.add(escManager);
		outputs.add(debugLeds);
	}
	
	private void initConnections() {
		try {
			connectionListener = new ConnectionListener(this);
			connectionListener.start();
			
			System.out.print(".");
			initMessages += "[INIT] ConnectionListener: ok\n";
			
			motorConnectionListener = new MotorConnectionListener(this);
			motorConnectionListener.start();
			
			System.out.print(".");
			initMessages += "[INIT] MotorConnectionListener: ok\n";
			
		} catch (IOException e) {
			e.printStackTrace();
			initMessages += "[INIT] Unable to start Network Connection Listeners!\n";
		}
	}
	
	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.print("# Shutting down... ");
				if (escManager != null) {
					speeds.setSpeeds(new MotorMessage(-1, -1));
				}
				if (gpsModule != null) {
					gpsModule.closeSerial();
				}
				
				if (debugLeds != null) {
					debugLeds.shutdownGpio();
				}
				System.out.println("# Finished cleanup!");
			}
		});
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
		System.out.println(status);
	}
}