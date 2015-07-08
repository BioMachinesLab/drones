/*
 * Created on 21-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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

	public int innovationId() {
		return (this.innovationId);
	}

	public int type() {
		// TODO Auto-generated method stub
		return 0;
	}

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

	public boolean equals(Object test) {
		boolean equals = false;
		if (test instanceof NEATNodeInnovation) {
			NEATNodeInnovation thisInnovation = (NEATNodeInnovation)test;
			equals = (this.linkInnovationId == thisInnovation.getLinkInnovationId());
		}
		
		return (equals);		
	}
}
