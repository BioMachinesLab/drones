package commoninterface.network.messages;

public class TargetMessage extends Message {
	private static final long serialVersionUID = -5860653409245723021L;
	private boolean move;
	private double timeStep;

	public TargetMessage(boolean move, double timeStep, String senderHostname) {
		super(senderHostname);
		this.move = move;
		this.timeStep = timeStep;
	}

	public boolean isToMove() {
		return move;
	}

	public double getTimeStep() {
		return timeStep;
	}

	@Override
	public Message getCopy() {
		return new TargetMessage(move, timeStep, senderHostname);
	}

	@Override
	public String toString() {
		return "move=" + move + ";timestep=" + timeStep;
	}
}
