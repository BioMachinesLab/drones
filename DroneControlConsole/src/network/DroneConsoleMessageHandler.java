package network;

import gui.DroneGUI;
import main.DroneControlConsole;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.MessageHandler;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.CompassMessage;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.SystemStatusMessage;

public class DroneConsoleMessageHandler extends MessageHandler {
	
	private DroneControlConsole console;
	
	public DroneConsoleMessageHandler(DroneControlConsole console) {
		this.console = console;
	}

	@Override
	protected void processMessage(Message message, ConnectionHandler c) {
		if (message instanceof GPSMessage) {
			((DroneGUI)console.getGUI()).getGPSPanel().displayData(((GPSMessage) message).getGPSData());
		} else if (message instanceof SystemStatusMessage) {
			((DroneGUI)console.getGUI()).getMessagesPanel().displayData((SystemStatusMessage) message);
		} else if (message instanceof CompassMessage) {
			((DroneGUI)console.getGUI()).getCompassPanel().displayData((CompassMessage) message);
		} else if (message instanceof BehaviorMessage) {
			((DroneGUI)console.getGUI()).getBehaviorsPanel().displayData((BehaviorMessage) message);
		} else if (message instanceof EntityMessage) {
			((DroneGUI)console.getGUI()).getMapPanel().displayData((EntityMessage) message);
		}else {
			System.out.println("Received non recognise message type: " + message.getClass().toString());
		}
	}
}