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

	// Modules
	private GPSModuleInput gpsModule;
	private ReversableESCManagerOutputV2 escManager;
	private I2CCompassModuleInput compassModule;
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private I2CBatteryManagerInput batteryManager;

	// Messages and IO's
	private ArrayList<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private ArrayList<ControllerOutput> outputs = new ArrayList<ControllerOutput>();
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();
	private ArrayList<Behavior> behaviors = new ArrayList<Behavior>();

	// Hardware Instances
	private GpioController gpioController;
	/* TO-DO Refactor I2C initialization!!!!! */
	private I2CBus i2cBus;

	// Other Stuff
	private String status = "";
	private String initMessages = "\n";
	private MotorSpeeds speeds;
	private DebugLedsOutput debugLeds;

	private Logger logThread;

	private MessageHandler messageHandler;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {
		speeds = new MotorSpeeds();
		addShutdownHooks();
		initModules();
	}

	private void initModules() {
		System.out.println("######################################");

		setStatus("Initializing...\n");

		initHardwareCommunicatonProtocols();
		initInputs();
		initOutputs();
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

	private void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.print("# Shutting down... ");
				if (escManager != null)
					speeds.setSpeeds(new MotorMessage(-1, -1));
				
				if (logThread != null)
					logThread.interrupt();

				if (compassModule != null)
					compassModule.interrupt();

				if (i2cBus != null) {
					try {
						i2cBus.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (gpsModule != null)
					gpsModule.closeSerial();

				if (debugLeds != null)
					debugLeds.shutdownLeds();

				if (gpioController != null)
					gpioController.shutdown();
				
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

		for (ControllerInput i : inputs) {
			if (i instanceof MessageProvider)
				messageProviders.add((MessageProvider) i);
		}

		for (ControllerOutput o : outputs) {
			if (o instanceof MessageProvider)
				messageProviders.add((MessageProvider) o);
		}
		
		for (Behavior b : behaviors) {
			if (b instanceof MessageProvider)
				messageProviders.add((MessageProvider) b);
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		System.out.print(status);
	}

	private void initBehaviors() {

		Behavior turnBehavior = new TurnToOrientation(this);
		turnBehavior.start();
		behaviors.add(turnBehavior);

	}

	/**
	 * Initialize the Hardware protocols shared and used on input and output
	 * devices ("physical layer"), like I2C and GPIO management instances
	 */
	private void initHardwareCommunicatonProtocols() {
		// try {
		// // Get I2C instance
		// i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
		// } catch (IOException e) {
		// initMessages += "\n[INIT] I2C Interface: not ok!\n";
		// e.printStackTrace();
		// }

		try {
			gpioController = GpioFactory.getInstance();
		} catch (Exception | Error e) {
			initMessages += ("\n[INIT] GPIO Controller: not ok! ("
					+ e.getMessage() + ")\n");
		}
	}

	/*
	 * Hardware initialization routines
	 */
	private void initInputs() {
		// Compass Module Init
		// compassModule = new I2CCompassModuleInput(i2cBus);
		// initMessages += "[INIT] I2CCompassModule: "
		// + (compassModule.isAvailable() ? "ok" : "not ok!") + "\n";
		//
		// if (compassModule.isAvailable()) {
		// compassModule.start();
		// inputs.add(compassModule);
		// System.out.print(".");
		// }

		// Battery Module Init
		// batteryManager = new I2CBatteryManagerInput();
		// initMessages += "[INIT] BatteryManager: "
		// + (batteryManager.isAvailable() ? "ok" : "not ok!") + "\n";
		// if (batteryManager.isAvailable()) {
		// batteryManager.start();
		// inputs.add(batteryManager);
		// System.out.print(".");
		// }
		
		try {
			// GPS Module Init
			gpsModule = new GPSModuleInput();
			initMessages += "[INIT] GPSModule: "
					+ (gpsModule.isAvailable() ? "ok" : "not ok!") + "\n";
	
			if (gpsModule.isAvailable()) {
				gpsModule.enableLocalLog();
				inputs.add(gpsModule);
				System.out.print(".");
			}
		} catch(Exception e) {
			initMessages += "[INIT] GPSModule: not ok! ("
					+ e.getMessage() + ")\n";
		}
	}

	private void initOutputs() {
		
		try {
			// ESC Output Init
			escManager = new ReversableESCManagerOutputV2(speeds, gpioController);

			initMessages += "[INIT] ESCManager: "
					+ (escManager.isAvailable() ? "ok" : "not ok!") + "\n";
	
			if (escManager.isAvailable()) {
				escManager.start();
				outputs.add(escManager);
				System.out.print(".");
			}
		
		} catch(Exception e) {
			initMessages += "[INIT] ESCManager: not ok! ("
					+ e.getMessage() + ")\n";
		}
		
		try {
			// Debug Leds Init
			debugLeds = new DebugLedsOutput(gpioController);
			initMessages += "[INIT] DebugLEDs: "
					+ (debugLeds.isAvailable() ? "ok" : "not ok!") + "\n";
			if (debugLeds.isAvailable()) {
				debugLeds.start();
				outputs.add(debugLeds);
	
				debugLeds.addBlinkLed(0);
	
				System.out.print(".");
			}
		} catch(Exception e) {
			initMessages += "[INIT] DebugLEDs: not ok! ("
					+ e.getMessage() + ")\n";
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

	public ArrayList<ControllerInput> getInputs() {
		return inputs;
	}

	public ArrayList<ControllerOutput> getOutputs() {
		return outputs;
	}

	public ArrayList<MessageProvider> getMessageProviders() {
		return messageProviders;
	}

	public ArrayList<Behavior> getBehaviors() {
		return behaviors;
	}
}