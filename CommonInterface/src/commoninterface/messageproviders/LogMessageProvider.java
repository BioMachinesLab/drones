package commoninterface.messageproviders;

import commoninterface.RobotCI;
import commoninterface.network.messages.LogMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class LogMessageProvider implements MessageProvider {

	private RobotCI robot;

	public LogMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {

		if (m instanceof LogMessage) {
			LogMessage lm = (LogMessage) m;

			if (robot.getLogger() != null) {
				robot.getLogger().logMessage(LogCodex.encodeLog(LogType.MESSAGE, lm.getLog()));
			}

			return m;
		}

		return null;
	}

}
