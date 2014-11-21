package commoninterfaceimpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.IOManager;
import io.SystemInfoMessageProvider;
import io.SystemStatusMessageProvider;
import io.input.ControllerInput;
import io.output.ControllerOutput;
import main.Controller;
import network.ConnectionHandler;
import network.ConnectionListener;
import network.ControllerMessageHandler;
import network.MotorConnectionListener;
import network.messages.Message;
import network.messages.MessageProvider;
import utils.Logger;
import utils.Nmea0183ToDecimalConverter;
import commoninterface.DroneCI;
import dataObjects.GPSData;

public class RealDroneCI implements DroneCI, Controller {

	private String status = "";
	private String initMessages = "\n";
	private Logger logThread;
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();

	
	@Override
	public void start() {		
		initIO();
		initMessageProviders();
		initConnections();

		logThread = new Logger(this);
		logThread.start();

		messageHandler = new ControllerMessageHandler(this);
		messageHandler.start();

		setStatus("Running!\n");

		System.out.println(initMessages);
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void setMotorSpeeds(double left, double right) {
		ioManager.setMotorSpeeds(left, right);
	}

	@Override
	public double getCompassOrientaiton() {
		return ioManager.getCompassModule().getHeading();
	}

	@Override
	public double getGPSLatitude() {
		GPSData gpsData = ioManager.getGpsModule().getReadings();
		
		//NMEA format: e.g. 3844.9474N 00909.2214W
		String latitude = gpsData.getLatitude();

		double lat = Double.parseDouble(latitude.substring(0,latitude.length()-1));
		char latPos = latitude.charAt(latitude.length()-1);


		lat = Nmea0183ToDecimalConverter.convertLatitudeToDecimal(lat, latPos);
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getGPSLongitude() {
		GPSData gpsData = ioManager.getGpsModule().getReadings();

		String longitude = gpsData.getLongitude();
		double lon = Double.parseDouble(longitude.substring(0,longitude.length()-1));
		char lonPos = longitude.charAt(longitude.length()-1);
		lon = Nmea0183ToDecimalConverter.convertLongitudeToDecimal(lon, lonPos);
		return lon;
	}

	@Override
	public double getTimeSinceStart() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void processInformationRequest(Message request,
			ConnectionHandler conn) {
		messageHandler.addMessage(request, conn);
	}

	@Override
	public String getInitMessages() {
		return initMessages;
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
	
	private void initModules() {
		System.out.println("######################################");


	}	
	
	private void initIO() {
		ioManager = new IOManager(this);
		initMessages += ioManager.getInitMessages();
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
	}
	
}
