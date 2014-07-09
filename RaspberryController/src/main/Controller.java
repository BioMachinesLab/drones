package main;

import input.BatteryManagerInput;
import input.CompassModuleInput;
import input.GPSModuleInput;

import java.io.IOException;
import java.text.ParseException;

import network.Connection;
import network.ConnectionHandler;
import network.messages.GPSMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MotorMessage;
import network.messages.SystemInformationsMessage;
import output.ESCManagerOutputThreaded;

import com.pi4j.io.serial.SerialPortException;

import dataObjects.SystemInformationsData;

public class Controller {
	private GPSModuleInput gpsModule;
	private BatteryManagerInput batteryManager;
	private ESCManagerOutputThreaded escManagerThreaded;
	private CompassModuleInput compassModule;
	private ConnectionHandler networkConnector;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (networkConnector != null) {
					networkConnector.closeConnections();
					escManagerThreaded.disableMotors();
				}
			}
		});

		System.out.println("######################################");
		System.out.print("Initializing ESC modules");
		escManagerThreaded = new ESCManagerOutputThreaded();
		escManagerThreaded.start();

		System.out.print(", GPS module");
		gpsModule = new GPSModuleInput();

		System.out.println(", network connections");
		networkConnector = new ConnectionHandler(this);
		networkConnector.initConnector();

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
			msg = new GPSMessage(gpsModule.getReadings());
			break;
		case SYSTEM:
			try {
				msg = new SystemInformationsMessage(
						new SystemInformationsData());
			} catch (IOException | InterruptedException | ParseException e) {
				System.err.println("Error fetching informations from system!");
				msg = null;
				e.printStackTrace();
			}
			break;
		default:
			msg = null;
			break;
		}

		conn.sendData(msg);
	}
}
