/*
 * Created on Oct 13, 2004
 *
 */
package commoninterface.neat.ga.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface Gene extends Serializable {
	public Number geneAsNumber();
	public String geneAsString();
}
