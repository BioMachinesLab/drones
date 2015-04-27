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

public class ThymioConsoleMessageHandler extends MessageHandler {
	
	private ThymioControlConsole console;
	
	public ThymioConsoleMessageHandler(ThymioControlConsole console) {
		this.console = console;
	}

	@Override
	protected Message processMessage(Message message) {
		if (message instanceof SystemStatusMessage) {
			((ThymioGUI)console.getGUI()).getMessagesPanel().displayData((SystemStatusMessage) message);
		} else if (message instanceof BehaviorMessage) {
			((ThymioGUI)console.getGUI()).getBehaviorsPanel().displayData((BehaviorMessage) message);
		} else if (message instanceof ThymioReadingsMessage) {
			((ThymioGUI)console.getGUI()).getReadingsPanel().displayData((ThymioReadingsMessage) message);
		} else if (message instanceof CameraCaptureMessage) {
			((ThymioGUI)console.getGUI()).getCapturePanel().displayData((CameraCaptureMessage) message);
		} else if (message instanceof NeuralActivationsMessage) {
			((ThymioGUI)console.getGUI()).getNeuralActivationsPanel().displayData((NeuralActivationsMessage) message);
		}  else {
			System.out.println("Received non recognise message type: " + message.getClass().toString());
		}
		
		return null;
	}
}