package network.messages;

import network.messages.Message;
import commoninterface.CIBehavior;

public class BehaviorMessage extends Message{
	
	protected Class<CIBehavior> selectedBehavior;
	protected boolean selectedStatus = false;
	protected String args;
	
	
	public BehaviorMessage(Class<CIBehavior> selectedBehavior, String args, boolean selectedStatus) {
		this.selectedBehavior = selectedBehavior;
		this.args = args;
		this.selectedStatus = selectedStatus;
	}
	
	public boolean getSelectedStatus() {
		return selectedStatus;
	}
	
	public Class<CIBehavior> getSelectedBehavior() {
		return selectedBehavior;
	}
	
	public String getArguments() {
		return args;
	}
	
	public void setArguments(String args) {
		this.args = args;
	}
}