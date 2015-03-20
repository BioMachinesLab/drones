package network;

import gui.ThymioGUI;
import main.ThymioControlConsole;
import network.messages.BehaviorMessage;
import network.messages.CameraCaptureMessage;
import network.messages.Message;
import network.messages.SystemStatusMessage;
import network.messages.ThymioReadingsMessage;

public class ThymioConsoleMessageHandler extends MessageHandler {
	
	private ThymioControlConsole console;
	
	public ThymioConsoleMessageHandler(ThymioControlConsole console) {
		this.console = console;
	}

	@Override
	protected void processMessage(Message message, ConnectionHandler c) {
		if (message instanceof SystemStatusMessage) {
			((ThymioGUI)console.getGUI()).getMessagesPanel().displayData((SystemStatusMessage) message);
		} else if (message instanceof BehaviorMessage) {
			((ThymioGUI)console.getGUI()).getBehaviorsPanel().displayData((BehaviorMessage) message);
		} else if (message instanceof ThymioReadingsMessage) {
			((ThymioGUI)console.getGUI()).getReadingsPanel().displayData((ThymioReadingsMessage) message);
		} else if (message instanceof CameraCaptureMessage) {
			((ThymioGUI)console.getGUI()).getCapturePanel().displayData((CameraCaptureMessage) message);
		}  else {
			System.out.println("Received non recognise message type: " + message.getClass().toString());
		}
	}
}