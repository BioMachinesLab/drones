package main;

import io.*;
import network.*;
import network.messages.*;

import java.util.List;

import commoninterface.CIBehavior;

public interface Controller {
	
	public String getStatus();
	public void   setStatus(String status);
	public String getInitMessages();
	
	public void processInformationRequest(Message request, ConnectionHandler conn);
	
	public List<MessageProvider> getMessageProviders();
	public IOManager getIOManager();
	public List<CIBehavior> getBehaviors();
	
	public void executeBehavior(CIBehavior b, boolean c);
}