package main;

import io.UnavailableDeviceException;
import io.input.GPSModuleInput;
import io.output.DebugLedsOutput;
import io.output.ESCManagerOutputThreadedImprov;

import java.io.IOException;
import java.text.ParseException;

import network.Connection;
import network.ConnectionHandler;
import network.messages.GPSMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MotorMessage;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;

import com.pi4j.io.serial.SerialPortException;

import dataObjects.MotorSpeeds;
import dataObjects.SystemInformationsData;

public class Controller {
	private GPSModuleInput gpsModule;
	private ESCManagerOutputThreadedImprov escManager;
	private ConnectionHandler connectionHandler;
	private String messages = "";
	private String initMessages = "";
	private MotorSpeeds speeds;
	private DebugLedsOutput debugLeds;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {

		speeds = new MotorSpeeds();

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

				System.out.println("now!");
			}
		});

		System.out.println("######################################");
		System.out.println("Initializing...");
		try {
			escManager = new ESCManagerOutputThreadedImprov(speeds);
			escManager.start();
			System.out.println("# ESC modules initialized with success!");
		} catch (UnavailableDeviceException e) {
			System.out.println("Unable to start ESC modules!");
			initMessages += "Unable to start ESC module!\n";
			e.printStackTrace();
		}

		try {
			gpsModule = new GPSModuleInput();
			System.out.println("# GPS Module initialized with success!");
		} catch (UnavailableDeviceException e) {
			System.out.println("Unable to start GPS module!");
			initMessages += "Unable to start GPS module!\n";
		}

		try {
			debugLeds = new DebugLedsOutput();
			debugLeds.blinkLed(0);
		} catch (IllegalArgumentException e) {
			System.out.println("Unable to start debug leds!");
			initMessages += "Unable to start debug leds!\n";
		}

		try {
			connectionHandler = new ConnectionHandler(this);
			connectionHandler.initConnector();
			System.out
					.println("# Network Connection initialized with success!");
		} catch (IOException e) {
			System.out.println("Unable to start Netwok Connector!");
			initMessages += "Unable to start Network Connector!\n";
		}

		// batteryManager = new BatteryManagerInput();
		// compassModule = new CompassModuleInput();
	}

	public void processMotorMessage(MotorMessage message) {
		speeds.setSpeeds(message);
	}

	public void processInformationRequest(InformationRequest request,
			Connection conn) {
		Message msg;
		switch (request.getMessageTypeQuery()) {
		case BATTERY:
			// TODO
			msg = null;
			break;
		case COMPASS:
			// TODO
			msg = null;
			break;
		case GPS:
			if (gpsModule != null) {
				msg = new GPSMessage(gpsModule.getReadings());
			} else {
				msg = new SystemStatusMessage("Unable to send GPS data");
			}
			conn.sendData(msg);
			break;
		case SYSTEM_INFO:
			try {
				msg = new SystemInformationsMessage(
						new SystemInformationsData());
			} catch (IOException | InterruptedException | ParseException e) {
				System.err.println("Error fetching informations from system!");
				msg = new SystemStatusMessage(
						"Error fetching informations from system!");
			}
			conn.sendData(msg);
			break;
		case SYSTEM_STATUS:
			if (messages != null) {
				msg = new SystemStatusMessage(messages);
				System.out.println("[CONTROLLER] I sent messages: " + messages);
				messages = null;
				conn.sendData(msg);
			}
			break;
		case INITIAL_MESSAGES:
			if (messages != null) {
				msg = new SystemStatusMessage(initMessages);
				System.out.println("[CONTROLLER] I sent the initial messages: "
						+ initMessages);
				conn.sendData(msg);
			}
			break;
		default:
			msg = null;
			break;
		}
	}

	public synchronized void sendMessageToOperator(String message) {
		if (messages == null)
			messages = message + "\n";
		else
			messages += message + "\n";
	}
}
