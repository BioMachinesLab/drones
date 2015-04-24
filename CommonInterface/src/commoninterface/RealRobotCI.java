package commoninterface;

import java.util.ArrayList;
import java.util.List;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.objects.Entity;
import commoninterface.utils.RobotLogger;

public abstract class RealRobotCI extends Thread implements RobotCI {
	
	protected RobotLogger logger;
	
	public abstract List<MessageProvider> getMessageProviders();
	
	public abstract ArrayList<Entity> getEntities();
	
	public abstract void startBehavior(CIBehavior b);
	
	public abstract void stopActiveBehavior();
	
	public abstract String getStatus();
	
	public abstract CIBehavior getActiveBehavior();
	
	public RobotLogger getLogger() {
		return logger;
	}
}
