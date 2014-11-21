package commoninterfaceimpl;

import java.util.List;

import io.IOManager;
import io.input.ControllerInput;
import io.output.ControllerOutput;
import main.Controller;
import network.ConnectionHandler;
import network.ControllerMessageHandler;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.MotorMessage;
import utils.Logger;
import commoninterface.DroneCI;
import dataObjects.MotorSpeeds;

public class RealDroneCI implements DroneCI, Controller {

	private String status = "";
	private String initMessages = "\n";
	private Logger logThread;
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;

	
	@Override
	public void start() {
		
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void setSpeed(double leftMotor, double rightMotor) {
		
	}

	@Override
	public double getOrientaiton() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getGPSLatitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getGPSLongitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTimeSinceStart() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void initModules() {
		System.out.println("######################################");

		initIO();

		logThread = new Logger(this);
		logThread.start();

		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();

		setStatus("Running!\n");

		System.out.println(initMessages);
	}

	private void initIO() {
		ioManager = new IOManager(this);
		initMessages += ioManager.getInitMessages();
	}

	@Override
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(String status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processInformationRequest(Message request,
			ConnectionHandler conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInitMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MessageProvider> getMessageProviders() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IOManager getIOManager() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
