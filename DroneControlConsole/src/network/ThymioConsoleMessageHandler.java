package network;

import gui.ThymioGUI;
import main.ThymioControlConsole;
import commoninterface.network.MessageHandler;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.CameraCaptureMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.NeuralActivationsMessage;
import commoninterface.network.messages.SystemStatusMessage;
import commoninterface.network.messages.ThymioReadingsMessage;

public class ThymioConsoleMessageHandler extends ControlConsoleMessageHandler {
	
	public ThymioConsoleMessageHandler(ThymioControlConsole console) {
		super(console);
	}

	@Override
	protected Message processMessage(Message message) {
		
		if (message instanceof ThymioReadingsMessage) {
			((ThymioGUI)console.getGUI()).getReadingsPanel().displayData((ThymioReadingsMessage) message);
		} else if (message instanceof CameraCaptureMessage) {
			((ThymioGUI)console.getGUI()).getCapturePanel().displayData((CameraCaptureMessage) message);
		} else {
			System.out.println("Received non recognized message type: " + message.getClass().toString());
		}
		
		return null;
	}
}