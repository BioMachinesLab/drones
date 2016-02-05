/*
 * Created on 21-Jun-2005
 *
 * 
 */
package commoninterface.neat.core;

/**
 * @author MSimmerson
 *
 * NEAT Node entry into the innovation database
 */
public class NEATNodeInnovation implements NEATInnovation {
    
    private static final long serialVersionUID = -1L;
	private int innovationId;
	private int nodeId;
	private int linkInnovationId;
	
	public NEATNodeInnovation() {
	}

	public NEATNodeInnovation(int linkInnovationId) {
		this.linkInnovationId = linkInnovationId;
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
	
	public void setNodeId(int id) {
		this.nodeId = id;
	}
	
	public int getNodeId() {
		return (this.nodeId);
	}
	
	public int getLinkInnovationId() {
		return (this.linkInnovationId);
	}

	@Override
	public boolean equals(Object test) {
		boolean equals = false;
		if (test instanceof NEATNodeInnovation) {
			NEATNodeInnovation thisInnovation = (NEATNodeInnovation)test;
			equals = (this.linkInnovationId == thisInnovation.getLinkInnovationId());
		}
		
		return (equals);		
	}
}
