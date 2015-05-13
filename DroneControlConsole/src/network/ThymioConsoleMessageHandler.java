package network;

import gui.ThymioGUI;
import main.ThymioControlConsole;

import commoninterface.network.messages.CameraCaptureMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.ThymioReadingsMessage;
import commoninterface.network.messages.ThymioVirtualPositionMessage;

public class ThymioConsoleMessageHandler extends ControlConsoleMessageHandler {
	
	public ThymioConsoleMessageHandler(ThymioControlConsole console) {
		super(console);
	}

	@Override
	protected Message processMessage(Message message) {
		
		Message m = super.processMessage(message);
		
		if(m != null) {
			return null;
		} else if (message instanceof ThymioReadingsMessage) {
			((ThymioGUI)console.getGUI()).getReadingsPanel().displayData((ThymioReadingsMessage) message);
		} else if (message instanceof CameraCaptureMessage) {
			((ThymioGUI)console.getGUI()).getCapturePanel().displayData((CameraCaptureMessage) message);
		} else if (message instanceof ThymioVirtualPositionMessage) {
			((ThymioGUI)console.getGUI()).getVirtualPositionPanel().displayData((ThymioVirtualPositionMessage) message);
		} else {
			System.out.println("Received non recognized message type: " + message.getClass().toString());
		}
		
		return null;
	}
}