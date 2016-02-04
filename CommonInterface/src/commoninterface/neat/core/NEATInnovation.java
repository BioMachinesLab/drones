/*
 * Created on 21-Jun-2005
 *
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
