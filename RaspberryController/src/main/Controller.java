package main;

import input.BatteryManagerInput;
import input.GPSModuleInput;
import input.SystemInformationsInput;

import java.io.IOException;

import network.Connection;
import network.ConnectionHandler;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MotorMessage;
import output.ESCManagerOutput;

import com.pi4j.io.serial.SerialPortException;

public class Controller {
	private GPSModuleInput gpsModule;
	private BatteryManagerInput batteryManager;
	private SystemInformationsInput sysInformations;
	private ESCManagerOutput escManager;
	private ConnectionHandler networkConnector;

	public static void main(String[] args) throws SerialPortException {
		new Controller();
	}

	public Controller() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (networkConnector != null) {
					networkConnector.closeConnections();
					escManager.disableMotors();
				}
			}
		});

		try {
			System.out.println("######################################");
			System.out.println("Initializing.....");
			escManager = new ESCManagerOutput();
			networkConnector = new ConnectionHandler(this);
			networkConnector.initConnector();

			// gpsModule = new GPSModuleInput();
			// sysInformations = new SystemInformationsInput();
			// batteryManager = new BatteryManagerInput();
			// escManager = new ESCManagerOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processMotorMessage(MotorMessage message, Connection conn) {
		escManager.setValue(0, message.getLeftMotor());
		escManager.setValue(1, message.getRightMotor());
		System.out.println("New velocity: L-" + message.getLeftMotor() + " R-"
				+ message.getRightMotor());
	}

	public void processInformationRequest(InformationRequest request,
			Connection conn) {
		Message msg;
		System.out.println("Request of " + request.getMessageTypeQuery());

		switch (request.getMessageTypeQuery()) {
		// case BATTERY:
		// // TODO
		// msg = null;
		// break;
		// case COMPASS:
		// // TODO
		// msg = null;
		// break;
		// case GPS:
		// msg = new GPSMessage(gpsModule.getReadings());
		// break;
		// case SYSTEM:
		// try {
		// msg = new SystemInformationsMessage(
		// new SystemInformationsData());
		// } catch (IOException | InterruptedException | ParseException e1) {
		// System.err.println("Error fetching informations from system!");
		// msg = null;
		// e1.printStackTrace();
		// }
		// break;
		default:
			msg = null;
			break;
		}

		conn.sendData(msg);
	}
}
