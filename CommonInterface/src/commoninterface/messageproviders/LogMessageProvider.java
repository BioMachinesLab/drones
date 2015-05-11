package commoninterface.messageproviders;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.network.messages.EntityMessage;
import commoninterface.network.messages.LogMessage;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.objects.Waypoint;
import commoninterface.utils.RobotLogger;

public class LogMessageProvider implements MessageProvider{
	
	private RobotCI robot;
	
	public LogMessageProvider(RobotCI drone) {
		this.robot = drone;
	}

	@Override
	public Message getMessage(Message m) {
		
		if(m instanceof LogMessage) {
			LogMessage lm = (LogMessage)m;
			
			RobotLogger logger = robot.getLogger();
			
			if(logger != null)
				logger.logMessage(lm.getLog());
			
			return m;
		}
		
		return null;
	}

}
