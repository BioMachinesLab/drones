package io;

import io.input.CameraCaptureInput;
import io.input.ControllerInput;
import io.input.ThymioProximitySensorsInput;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import network.messages.MotorMessage;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import utils.CommandLine;
import ch.epfl.mobots.AsebaNetwork;
import ch.epfl.mobots.Aseba.ThymioRemoteConnection;


public class ThymioIOManager {
	
	private final static String CONFIG_FILE = "thymio_io_config.conf";
	
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();
	
	private ThymioRemoteConnection thymioRemoteConnection;
	
	private CameraCaptureInput cameraInput;
	
	private String initMessages = "\n";
	
	private LinkedList<String> enabledIO = new LinkedList<String>();
	
	public ThymioIOManager() {
		try {
			loadConfigurations();
			
			initThymioConnection();
			initInputs();
			
		} catch (DBusException e) {
			initMessages += ("\n[INIT] DBus/Aseba: not ok! ("+ e.getMessage() + ")\n");
			e.printStackTrace();
		}
	}

	private void loadConfigurations() {

		try {
			Scanner s = new Scanner(new File(CONFIG_FILE));
			while (s.hasNextLine()) {
				String line = s.nextLine();

				// So we can put comments on the io_config.conf
				if (line.startsWith("#") || line.length() < 2) {
					continue;
				}

				String[] split = line.split(" ");
				if (split[1].equals("1"))
					enabledIO.add(split[0]);
			}
			s.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	private void initThymioConnection() throws DBusException {
		DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
		AsebaNetwork recvInterface = (AsebaNetwork) conn.getRemoteObject("ch.epfl.mobots.Aseba", "/", AsebaNetwork.class);
		thymioRemoteConnection = new ThymioRemoteConnection(recvInterface);
	}

	private void initInputs(){
		ThymioProximitySensorsInput proximitySensorsInput = new ThymioProximitySensorsInput(this);
		inputs.add(proximitySensorsInput);
		
		if (enabledIO.contains("picamera")) {
			cameraInput = new CameraCaptureInput();
			inputs.add(cameraInput);
			initMessages += "[INIT] Raspberry Camera initialized \n";
		}
		
	}
	
	public void shutdown() {
		System.out.println("# Shutting down IO...");
		stopThymio();
		if(cameraInput != null)
			CommandLine.executeShellCommand("./picamera/stop_server.sh");
		System.out.println("# Finished IO cleanup!");
	}
	
	public void stopThymio() {
		setMotorSpeeds(0, 0);
	}
	
	public List<Short> getProximitySensorsReadings(){
		return thymioRemoteConnection.getProximitySensorValues();
	}
	
	public void setMotorSpeeds(MotorMessage message) {
		thymioRemoteConnection.setTargetWheelSpeed((short) (message.getLeftMotor() * 500), (short) (message.getRightMotor() * 500));
	}
	
	public void setMotorSpeeds(double left, double right) {
		thymioRemoteConnection.setTargetWheelSpeed((short) (left * 500), (short) (right * 500));
	}
	
	public ThymioRemoteConnection getThymioRemoteConnection() {
		return thymioRemoteConnection;
	}
	
	public String getInitMessages() {
		return initMessages;
	}
	
	public ArrayList<ControllerInput> getInputs() {
		return inputs;
	}
	
}
