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
import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;
import dataObjects.GPSData;

public class RealAquaticDroneCI implements AquaticDroneCI, Controller {

	private String status = "";
	private String initMessages = "\n";
	private Logger logThread;
	private IOManager ioManager;
	private ControllerMessageHandler messageHandler;
	private ConnectionListener connectionListener;
	private MotorConnectionListener motorConnectionListener;
	private List<MessageProvider> messageProviders = new ArrayList<MessageProvider>();
	private String[] args;
	private CILogger logger;
	private long     startTimeInMillis;
	
	
	@Override
	public void start(String[] args, CILogger logger) {		
		this.startTimeInMillis = System.currentTimeMillis();
		this.args   = args;
		this.logger = logger; 
		
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
		ioManager.setMotorSpeeds(left * 2.0 - 1.0, right * 2.0 - 1.0);
	}

	@Override
	public double getCompassOrientationInDegrees() {
		double orientation = ioManager.getCompassModule().getHeadingInDegrees();
		return (orientation % 360.0);
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
		long elapsedMillis =  System.currentTimeMillis() - this.startTimeInMillis;
		
		return ((double) elapsedMillis) / 1000.0;
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
		return messageProviders;
	}


	@Override
	public IOManager getIOManager() {
		return ioManager;
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

	@Override
	public void setLed(int index, AquaticDroneCI.LedState state) {
		if (index >= 0 && index < ioManager.getDebugLeds().getNumberOfLeds()) {
			switch (state) {
			case ON:
				ioManager.getDebugLeds().removeBlinkLed(index);
				ioManager.getDebugLeds().setValue(index, 1);
				break;
			case OFF:
				ioManager.getDebugLeds().removeBlinkLed(index);
				ioManager.getDebugLeds().setValue(index, 0);
				break;
			case BLINKING:
				ioManager.getDebugLeds().addBlinkLed(index);
				break;
			} 
		} else {
			logger.logError("Invalid led index: " + index + ", must be >= 0 and < " + ioManager.getDebugLeds().getNumberOfLeds());
		}
	}
	
}
