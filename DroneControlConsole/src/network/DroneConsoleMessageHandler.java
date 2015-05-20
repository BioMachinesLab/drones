package network;

import gui.DroneGUI;
import main.DroneControlConsole;

import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.Message;

public class DroneConsoleMessageHandler extends ControlConsoleMessageHandler {
	
	public DroneConsoleMessageHandler(DroneControlConsole console) {
		super(console);
	}

	@Override
	protected Message processMessage(Message message) {
		
		Message m = super.processMessage(message);
		
		if(m != null) {
			return null;
		} else if (message instanceof GPSMessage) {
			((DroneGUI)console.getGUI()).getGPSPanel().displayData(((GPSMessage) message).getGPSData());
			((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderIPAddr()).setGPSData(((GPSMessage) message).getGPSData());
			
		} else if (message instanceof CompassMessage) {
			((DroneGUI)console.getGUI()).getCompassPanel().displayData((CompassMessage) message);
			((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderIPAddr()).setOrientation(((CompassMessage) message).getHeading());
						
		} else if (message instanceof EntityMessage) {
			((DroneGUI)console.getGUI()).getMapPanel().displayData((EntityMessage) message);
			
			// TODO Entity processing on DroneData
			//((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderIPAddr()).setOrientation(((CompassMessage) message).getHeading());
			
		}else {
			System.out.println("Received non recognized message type: " + message.getClass().toString());
		}
		return null;
	}
}