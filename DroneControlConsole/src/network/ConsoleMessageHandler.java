package network;

import main.DroneControlConsole;
import network.messages.BehaviorMessage;
import network.messages.CompassMessage;
import network.messages.GPSMessage;
import network.messages.Message;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;

public class ConsoleMessageHandler extends MessageHandler {
	
	private DroneControlConsole console;
	
	public ConsoleMessageHandler(DroneControlConsole console) {
		this.console = console;
	}

	@Override
	protected void processMessage(Message message, ConnectionHandler c) {
		if (message instanceof GPSMessage) {
			console.getGUI().getGPSPanel().displayData(((GPSMessage) message).getGPSData());
			console.getGUI().getMapPanel().displayData(((GPSMessage) message).getGPSData());
		} else if (message instanceof SystemInformationsMessage) {
			console.getGUI().getSysInfoPanel().displayData(((SystemInformationsMessage) message).getSysInformations());
		} else if (message instanceof SystemStatusMessage) {
			console.getGUI().getMessagesPanel().displayData((SystemStatusMessage) message);
		} else if (message instanceof CompassMessage) {
			console.getGUI().getCompassPanel().displayData((CompassMessage) message);
			console.getGUI().getMapPanel().displayData(((CompassMessage) message));
		} else if (message instanceof BehaviorMessage) {
			console.getGUI().getBehaviorsPanel().displayData((BehaviorMessage) message);
		} else {
			System.out.println("Received non recognise message type: " + message.getClass().toString());
		}
	}
}