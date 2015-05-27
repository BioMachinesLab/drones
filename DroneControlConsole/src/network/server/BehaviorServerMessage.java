package network.server;

import commoninterface.network.NetworkUtils;
import commoninterface.network.messages.BehaviorMessage;

public class BehaviorServerMessage {
	protected String selectedBehavior;
	protected boolean selectedStatus = false;
	protected String args;
	private String myHostname;

	public BehaviorServerMessage(BehaviorMessage message) {
		updateHostname();
		chewData(message);
	}

	public BehaviorServerMessage() {
		this.selectedBehavior = null;
		this.args = null;
		this.selectedStatus = false;
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

	public void chewData(BehaviorMessage message) {
		this.selectedBehavior = message.getSelectedBehavior();
		this.args = message.getArguments();
		this.selectedStatus = message.getSelectedStatus();
	}

	public BehaviorMessage getAsBehaviorMessage() {
		return new BehaviorMessage(selectedBehavior, args, selectedStatus,
				myHostname);
	}

	private void updateHostname() {
		myHostname = NetworkUtils.getHostname();
	}
}
