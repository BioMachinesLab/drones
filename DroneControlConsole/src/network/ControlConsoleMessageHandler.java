package network;

import main.RobotControlConsole;
import commoninterface.network.MessageHandler;
import commoninterface.network.messages.BehaviorMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.NeuralActivationsMessage;
import commoninterface.network.messages.SystemStatusMessage;

public abstract class ControlConsoleMessageHandler extends MessageHandler {
	
	protected RobotControlConsole console;
	
	public ControlConsoleMessageHandler(RobotControlConsole console) {
		this.console = console;
	}

	@Override
	protected Message processMessage(Message message) {
		
		if (message instanceof SystemStatusMessage) {
			(console.getGUI()).getMessagesPanel().displayData((SystemStatusMessage) message);
			return message;
		} else if (message instanceof BehaviorMessage) {
			(console.getGUI()).getBehaviorsPanel().displayData((BehaviorMessage) message);
			return message;
		} else if (message instanceof NeuralActivationsMessage) {
			(console.getGUI()).getNeuralActivationsPanel().displayData((NeuralActivationsMessage) message);
			return message;
		}  
		
		return null;
	}

}
