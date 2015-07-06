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
		// boolean updateDronesSet = false;
		// if (console instanceof DroneControlConsole) {
		// updateDronesSet = true;
		// }

		if (message instanceof SystemStatusMessage) {
			(console.getGUI()).getMessagesPanel().displayData(
					(SystemStatusMessage) message);

//			if (updateDronesSet) {
//				if (((DroneControlConsole) console).getDronesSet().existsDrone(
//						message.getSenderHostname())) {
//					((DroneControlConsole) console)
//							.getDronesSet()
//							.getDrone(message.getSenderHostname())
//							.setSystemStatusMessage(
//									((SystemStatusMessage) message)
//											.getMessage());
//				} else {
//					DroneData droneData = new DroneData(
//							message.getSenderIPAddr(),
//							message.getSenderHostname());
//					droneData
//							.setSystemStatusMessage(((SystemStatusMessage) message)
//									.getMessage());
//					((DroneControlConsole) console).getDronesSet().addDrone(
//							droneData);
//				}
//			}

			return message;
		} else if (message instanceof BehaviorMessage) {
			(console.getGUI()).getCommandPanel().displayData(
					(BehaviorMessage) message);

//			if (updateDronesSet) {
//				if (((DroneControlConsole) console).getDronesSet().existsDrone(
//						message.getSenderHostname())) {
//					((DroneControlConsole) console)
//							.getDronesSet()
//							.getDrone(message.getSenderHostname())
//							.setBehaviour(
//									ServerUtils
//											.getAsBehaviorServerMessage((BehaviorMessage) message));
//				} else {
//					DroneData droneData = new DroneData(
//							message.getSenderIPAddr(),
//							message.getSenderHostname());
//					droneData
//							.setBehaviour(ServerUtils
//									.getAsBehaviorServerMessage((BehaviorMessage) message));
//					((DroneControlConsole) console).getDronesSet().addDrone(
//							droneData);
//				}
//			}

			return message;
		} else if (message instanceof NeuralActivationsMessage) {
			(console.getGUI()).getNeuralActivationsPanel().displayData(
					(NeuralActivationsMessage) message);

//			if (updateDronesSet) {
//				if (((DroneControlConsole) console).getDronesSet().existsDrone(
//						message.getSenderHostname())) {
//					((DroneControlConsole) console)
//							.getDronesSet()
//							.getDrone(message.getSenderHostname())
//							.setNeuralActivations(
//									ServerUtils
//											.getAsNeuralActivationsServerMessage(((NeuralActivationsMessage) message)));
//				} else {
//					DroneData droneData = new DroneData(
//							message.getSenderIPAddr(),
//							message.getSenderHostname());
//					droneData
//							.setNeuralActivations(ServerUtils
//									.getAsNeuralActivationsServerMessage(((NeuralActivationsMessage) message)));
//					((DroneControlConsole) console).getDronesSet().addDrone(
//							droneData);
//				}
//			}

			return message;
		}

		return null;
	}
}
