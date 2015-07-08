/*
 * Created on 21-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;

import java.io.Serializable;

/**
 * Innovation database entry
 * @author MSimmerson
 *
 */
public interface NEATInnovation extends Serializable {
	public void setInnovationId(int id);
	public int innovationId();
	public int type();
}
