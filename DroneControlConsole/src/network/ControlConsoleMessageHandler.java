package network;

import network.server.ServerUtils;
import main.DroneControlConsole;
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
		boolean updateDronesSet = false;
		if (console instanceof DroneControlConsole) {
			updateDronesSet = true;
		}

		if (message instanceof SystemStatusMessage) {
			(console.getGUI()).getMessagesPanel().displayData(
					(SystemStatusMessage) message);

			if (updateDronesSet) {
				System.out.println("sender ip: " + message.getSenderIPAddr());
				System.out.println("sender hostname: "
						+ message.getSenderHostname());
				((DroneControlConsole) console)
						.getDronesSet()
						.getDrone(message.getSenderHostname())
						.setSystemStatusMessage(
								((SystemStatusMessage) message).getMessage());
			}

			return message;
		} else if (message instanceof BehaviorMessage) {
			(console.getGUI()).getCommandPanel().displayData(
					(BehaviorMessage) message);

			if (updateDronesSet) {
				((DroneControlConsole) console)
						.getDronesSet()
						.getDrone(message.getSenderHostname())
						.setBehaviour(
								ServerUtils
										.getAsBehaviorServerMessage((BehaviorMessage) message));
			}

			return message;
		} else if (message instanceof NeuralActivationsMessage) {
			(console.getGUI()).getNeuralActivationsPanel().displayData(
					(NeuralActivationsMessage) message);

			if (updateDronesSet) {
				((DroneControlConsole) console)
						.getDronesSet()
						.getDrone(message.getSenderHostname())
						.setNeuralActivations(
								((NeuralActivationsMessage) message));
			}

			return message;
		}

		return null;
	}

}
