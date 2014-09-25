package main;

import io.UnavailableDeviceException;
import io.input.GPSModuleInput;
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

import dataObjects.SystemInformationsData;

public class Controller {
	private GPSModuleInput gpsModule;
	private ESCManagerOutputThreadedImprov escManagerThreaded;
	private ConnectionHandler networkConnector;
	private String messages = "";

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("# Shutting down... ");
				if (networkConnector != null) {
					networkConnector.closeConnections();
				}
				if (escManagerThreaded != null) {
					escManagerThreaded.disableMotors();
				}
				if (gpsModule != null) {
					gpsModule.closeSerial();
				}
				System.out.println("now!");
			}
		});

		System.out.println("######################################");
		System.out.println("Initializing...");
		try {
			escManagerThreaded = new ESCManagerOutputThreadedImprov();
			escManagerThreaded.start();
			System.out.println("# ESC modules initialized with success!");
		} catch (UnavailableDeviceException e) {
			System.out.println("Unable to start ESC modules!");
			messages += "Unable to start ESC module!\n";
			e.printStackTrace();
		}

		try {
			gpsModule = new GPSModuleInput();
			System.out.println("# GPS Module initialized with success!");
		} catch (UnavailableDeviceException e) {
			System.out.println("Unable to start GPS module!");
			messages += "Unable to start GPS module!\n";
		}

		try {
			networkConnector = new ConnectionHandler(this);
			networkConnector.initConnector();
			System.out
					.println("# Network Connection initialized with success!");
		} catch (IOException e) {
			System.out.println("Unable to start Netwok Connector!");
			messages += "Unable to start Network Connector!\n";
		}

		// batteryManager = new BatteryManagerInput();
		// compassModule = new CompassModuleInput();
	}

	public void processMotorMessage(MotorMessage message, Connection conn) {
		escManagerThreaded.setValue(0, message.getLeftMotor());
		escManagerThreaded.setValue(1, message.getRightMotor());
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
				System.out
						.println("[CONTROLLER)] I sent messages: " + messages);
				messages = null;
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
