package network.messages;

import network.messages.Message;

public class BehaviorMessage extends Message{
	
	protected String selectedBehavior;
	protected boolean selectedStatus = false;
	protected String args;
	
	
	public BehaviorMessage(String selectedBehavior, String args, boolean selectedStatus) {
		this.selectedBehavior = selectedBehavior;
		this.args = args;
		this.selectedStatus = selectedStatus;
	}
	
	public boolean getSelectedStatus() {
		return selectedStatus;
	}
	
	public String getSelectedBehavior() {
		return selectedBehavior;
	}
	
	public String getArguments() {
		return args;
	}
	
	public void setArguments(String args) {
		this.args = args;
	}
}