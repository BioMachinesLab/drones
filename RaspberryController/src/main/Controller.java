package main;

import io.SystemInfoMessageProvider;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.input.GPSModuleInput;
import io.input.I2CCompassModuleInput;
import io.output.ControllerOutput;
import io.output.DebugLedsOutput;
import io.output.ReversableESCManagerOutput;

import java.io.IOException;
import java.util.ArrayList;

import network.ConnectionHandler;
import network.ConnectionListener;
import network.MotorConnectionListener;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.MotorMessage;
import network.messages.SystemStatusMessage;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.serial.SerialPortException;

import dataObjects.MotorSpeeds;

public class Controller {
	private GPSModuleInput gpsModule;
	private ReversableESCManagerOutput escManager;
	private I2CCompassModuleInput compassModule;
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;

	private ArrayList<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();

	private String status = "";
	private String initMessages = "";
	private MotorSpeeds speeds;
	private DebugLedsOutput debugLeds;
	
	private boolean debug = false;

	// TO-DO Refactor this initialization!!!!!
	private I2CBus i2cBus;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	/*
	 * Constructor and main initialization routine
	 */
	public Controller() {
		speeds = new MotorSpeeds();

		addShutdownHooks();

		initModules();
	}

	private void initModules() {
		System.out.println("######################################");

		setStatus("Initializing...");
		
		if(!debug) {
			initInputs();
			initOutputs();
			initMessageProviders();
		}

		initConnections();

		setStatus("Running");

		System.out.println(initMessages);
	}

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.print("# Shutting down... ");
				if (escManager != null) {
					speeds.setSpeeds(new MotorMessage(-1, -1));
				}

				if (i2cBus != null) {
					try {
						i2cBus.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

	/*
	 * Command or Request Messages providers and handlers
	 */
	public void processMotorMessage(MotorMessage message) {
		speeds.setSpeeds(message);
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

		Message response = null;

		for (MessageProvider p : messageProviders) {
			response = p.getMessage(request);
			if (response != null)
				break;
		}

		if (response == null)
			response = new SystemStatusMessage(
					"No message provider for the current request ("
							+ request.getClass().getSimpleName() + ")");

		conn.sendData(response);
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

		for (ControllerInput i : inputs) {
			if (i instanceof MessageProvider)
				messageProviders.add((MessageProvider) i);
		}

		for (ControllerOutput o : outputs) {
			if (o instanceof MessageProvider)
				messageProviders.add((MessageProvider) o);
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		System.out.println(status);
	}

	/*
	 * Hardware initialization routines
	 */
	private void initInputs() {
		/*try {
			// Get I2C instance
			i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);

			compassModule = new I2CCompassModuleInput(i2cBus);
			initMessages += "\n[INIT] I2CCompassModule: "
					+ (compassModule.isAvailable() ? "ok" : "not ok!") + "\n";
			inputs.add(compassModule);
			System.out.print(".");

			// batteryManager = new BatteryManagerInput();
		} catch (IOException e) {
			initMessages += "\n[INIT] I2CCompassModule: not ok!" + "\n";
			e.printStackTrace();
		}*/

		gpsModule = new GPSModuleInput();
		initMessages += "\n[INIT] GPSModule: "
				+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";
		gpsModule.enableLocalLog();
		System.out.print(".");

		inputs.add(gpsModule);
	}

	private void initOutputs() {
		escManager = new ReversableESCManagerOutput(speeds);
		initMessages += "[INIT] ESCManager: "
				+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";
		if (escManager.isAvailable())
			escManager.start();
		System.out.print(".");

		debugLeds = new DebugLedsOutput();
		initMessages += "[INIT] DebugLEDs: "
				+ (debugLeds.isAvailable() ? "ok" : "not ok!") + "\n";
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
}