package io;

import io.input.CameraCaptureInput;
import io.input.ControllerInput;
import io.input.ThymioProximitySensorsInput;
import java.util.ArrayList;
import java.util.List;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import commoninterface.network.messages.MotorMessage;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealThymioCI;
import utils.CommandLine;
import ch.epfl.mobots.AsebaNetwork;
import ch.epfl.mobots.Aseba.ThymioRemoteConnection;


public class ThymioIOManager {
	
	private ArrayList<ControllerInput> inputs = new ArrayList<ControllerInput>();
	
	private ThymioRemoteConnection thymioRemoteConnection;
	
	private CameraCaptureInput cameraInput;
	
	private String initMessages = "\n";
	
	public ThymioIOManager(RealThymioCI thymio, CIArguments args) {
		try {
			
			initThymioConnection();
			initInputs(args);
			
			if (args.getFlagIsTrue("filelogger")) {
				thymio.startLogger();
			}
			
		} catch (DBusException e) {
			initMessages += ("\n[INIT] DBus/Aseba: not ok! ("+ e.getMessage() + ")\n");
			e.printStackTrace();
		}
	}

	private void initThymioConnection() throws DBusException {
		DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
		AsebaNetwork recvInterface = (AsebaNetwork) conn.getRemoteObject("ch.epfl.mobots.Aseba", "/", AsebaNetwork.class);
		thymioRemoteConnection = new ThymioRemoteConnection(recvInterface);
	}

	private void initInputs(CIArguments args){
		ThymioProximitySensorsInput proximitySensorsInput = new ThymioProximitySensorsInput(this);
		inputs.add(proximitySensorsInput);
		
		if (args.getFlagIsTrue("picamera")) {
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
