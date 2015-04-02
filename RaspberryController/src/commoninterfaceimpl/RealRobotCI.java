package commoninterfaceimpl;

import java.util.ArrayList;
import java.util.List;

import objects.Entity;
import network.ConnectionHandler;
import network.messages.Message;
import network.messages.MessageProvider;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;

public abstract class RealRobotCI extends Thread implements RobotCI {

	public abstract void reset();
	
	public abstract void processInformationRequest(Message request,ConnectionHandler conn);
	
	public abstract String getInitMessages();
	
	public abstract List<MessageProvider> getMessageProviders();
	
	public abstract ArrayList<Entity> getEntities();
	
	public abstract void startBehavior(CIBehavior b);
	
	public abstract void stopActiveBehavior();
	
	public abstract String getStatus();
	
	public abstract CIBehavior getActiveBehavior();
}
