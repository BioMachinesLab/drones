package network;

import gui.DroneGUI;
import main.DroneControlConsole;
import network.messages.BehaviorMessage;
import network.messages.CompassMessage;
import network.messages.EntityMessage;
import network.messages.GPSMessage;
import network.messages.Message;
import network.messages.SystemStatusMessage;

import commoninterface.network.ConnectionHandler;
import commoninterface.network.MessageHandler;

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