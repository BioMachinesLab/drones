package network.server.shared;

public class BehaviorServerMessage {
	protected String selectedBehavior;
	protected boolean selectedStatus = false;
	protected String args;

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

	public void setSelectedBehavior(String selectedBehavior) {
		this.selectedBehavior = selectedBehavior;
	}

	public void setSelectedStatus(boolean selectedStatus) {
		this.selectedStatus = selectedStatus;
	}
}
