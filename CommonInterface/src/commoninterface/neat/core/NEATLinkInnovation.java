/*
 * Created on 21-Jun-2005
 *
 */
package commoninterface.neat.core;

/**
 * @author MSimmerson
 *
 * Link innovation entry in the innovation database
 */
public class NEATLinkInnovation implements NEATInnovation {
    
    private static final long serialVersionUID = -1L;
	private int innovationId;
	private int fromId;
	private int toId;

	public NEATLinkInnovation(int from, int to) {
		this.fromId = from;
		this.toId = to;
	}

	@Override
	public int innovationId() {
		return (this.innovationId);
	}
 
	@Override
	public int type() {
		return 0;
	}

	@Override
	public void setInnovationId(int id) {
		this.innovationId = id;
	}
	/**
	 * @return Returns the fromId.
	 */
	public int getFromId() {
		return fromId;
	}
	/**
	 * @return Returns the toId.
	 */
	public int getToId() {
		return toId;
	}
	
	@Override
	public boolean equals(Object test) {
		boolean equals = false;
		if (test instanceof NEATLinkInnovation) {
			NEATLinkInnovation thisInnovation = (NEATLinkInnovation)test;
			equals = (this.fromId == thisInnovation.getFromId()) && (this.toId == thisInnovation.getToId());
		}
		
		return (equals);
		
	}
	
	@Override
	public int hashCode() {
		// this may have to change for very large nets.
		return (this.fromId * 100000 + this.toId);
	}
}
