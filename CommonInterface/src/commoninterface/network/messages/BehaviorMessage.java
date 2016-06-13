package commoninterface.network.messages;

public class BehaviorMessage extends Message {
	private static final long serialVersionUID = 112735532176418274L;
	protected String selectedBehavior;
	protected boolean selectedStatus = false;
	protected String args;

	public BehaviorMessage(String selectedBehavior, String args, boolean selectedStatus, String senderHostname) {
		super(senderHostname);
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

	@Override
	public Message getCopy() {
		return new BehaviorMessage(selectedBehavior, args, selectedStatus, getSenderHostname());
	}

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName() + ";" + selectedBehavior + ";" + selectedStatus + ";" + args;
		return s;
	}
}