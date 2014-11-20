package main;

import io.*;
import io.input.*;
import io.output.*;
import network.*;
import network.messages.*;

import java.io.IOException;
import java.util.ArrayList;

import utils.Logger;
import behaviors.*;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.serial.SerialPortException;

import dataObjects.MotorSpeeds;

public class Controller {

	// Messages and behaviors
	private ArrayList<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
	
	//Hardware Manager
	private IOManager ioManager;
	
	//Network
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;

	// Other Stuff
	private String status = "";
	private String initMessages = "\n";
	private MotorSpeeds motorSpeeds;

	private Logger logThread;

	private MessageHandler messageHandler;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {
		motorSpeeds = new MotorSpeeds();
		addShutdownHooks();
		initModules();
	}

	private void initModules() {
		System.out.println("######################################");

		setStatus("Initializing...\n");

		initIO();
		
		initBehaviors();
		initMessageProviders();

		initConnections();

		logThread = new Logger(this);
		logThread.start();

		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();

		setStatus("Running!\n");

		System.out.println(initMessages);
	}

	private void initIO() {
		ioManager = new IOManager(this);
		initMessages+=ioManager.getInitMessages();
	}

	/*
	 * Command or Request Messages providers and handlers
	 */
	public void processMotorMessage(MotorMessage message) {
		motorSpeeds.setSpeeds(message);
	}

	/**
	 * Checks if there is a provider for the requested information (from the
	 * remote controller/ drone/ ...) and answers with a response for the
	 * requested information. If there is not a provider for the requested
	 * information, a message is sent to the user, alerting that there is no
	 * provider
	 * 
	 * @param InformationRequest
	 *            message
	 * @param The
	 *            connection handler for the information requester
	 */
	public void processInformationRequest(Message request,
			ConnectionHandler conn) {
		messageHandler.addMessage(request, conn);
	}

	public String getInitialMessages() {
		return initMessages;
	}

	/**
	 * Create a message provider for all the possible message provider classes
	 * like the inputs, outputs, system information queries
	 */
	private void initMessageProviders() {
		messageProviders.add(new SystemInfoMessageProvider());
		messageProviders.add(new SystemStatusMessageProvider(this));

		for (ControllerInput i : ioManager.getInputs()) {
			if (i instanceof MessageProvider)
				messageProviders.add((MessageProvider) i);
		}

		for (ControllerOutput o : ioManager.getOutputs()) {
			if (o instanceof MessageProvider)
				messageProviders.add((MessageProvider) o);
		}
		
		for (Behavior b : behaviors) {
			if (b instanceof MessageProvider)
				messageProviders.add((MessageProvider) b);
		}
	}

	private void initBehaviors() {
		
		try{
			Behavior turnBehavior = new TurnToOrientation(this);
			turnBehavior.start();
			behaviors.add(turnBehavior);
		} catch(Exception e) {
			initMessages += "[INIT] Behavior "+e.getMessage()+"\n";
		}

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
			initMessages += "[INIT] Unable to start Network Connection Listeners! ("
					+ e.getMessage() + ")\n";
		}
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		System.out.print(status);
	}

	public ArrayList<ControllerInput> getInputs() {
		return ioManager.getInputs();
	}

	public ArrayList<ControllerOutput> getOutputs() {
		return ioManager.getOutputs();
	}

	public ArrayList<MessageProvider> getMessageProviders() {
		return messageProviders;
	}

	public ArrayList<Behavior> getBehaviors() {
		return behaviors;
	}
	
	public MotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}
	
	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.print("# Shutting down Controller...");
				
				if (logThread != null)
					logThread.interrupt();

				System.out.println("# Finished Controller cleanup!");
			}
		});
	}
}