package network;

import gui.DroneGUI;
import main.DroneControlConsole;
import commoninterface.network.messages.BatteryMessage;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.TemperatureMessage;

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
			
//			if(((DroneControlConsole) console).getDronesSet().existsDrone(message.getSenderHostname())){
//				((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderHostname()).setGPSData(ServerUtils.getAsGGPSServerData(((GPSMessage) message).getGPSData()));
//			}else{
//				DroneData droneData = new DroneData(message.getSenderIPAddr(),message.getSenderHostname());
//				droneData.setGPSData(ServerUtils.getAsGGPSServerData(((GPSMessage) message).getGPSData()));
//				
//				((DroneControlConsole) console).getDronesSet().addDrone(droneData);
//			}
			
		} else if (message instanceof CompassMessage) {
			((DroneGUI)console.getGUI()).getCompassPanel().displayData((CompassMessage) message);
			
//			if(((DroneControlConsole) console).getDronesSet().existsDrone(message.getSenderHostname())){
//				((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderHostname()).setOrientation(((CompassMessage) message).getHeading());
//			}else{
//				DroneData droneData = new DroneData(message.getSenderIPAddr(),message.getSenderHostname());
//				droneData.setOrientation(((CompassMessage) message).getHeading());
//				
//				((DroneControlConsole) console).getDronesSet().addDrone(droneData);
//			}
		} else if (message instanceof EntityMessage) {
			((DroneGUI)console.getGUI()).getMapPanel().displayData((EntityMessage) message);
			
			// TODO Entity processing on DroneData
			//((DroneControlConsole) console).getDronesSet().getDrone(message.getSenderIPAddr()).setOrientation(((CompassMessage) message).getHeading());
		
		} else if (message instanceof BatteryMessage) {
			((DroneGUI)console.getGUI()).getBatteryPanel().displayData((BatteryMessage) message);
		} else if (message instanceof TemperatureMessage) {
			((DroneGUI)console.getGUI()).getTemperaturesPanel().displayData((TemperatureMessage) message);
		}else {
			System.out.println("Received non recognized message type: " + message.getClass().toString());
		}
		return null;
	}
}