/*
 * Created on 22-Jun-2005
 *
 */
package commoninterface.neat.core;

import commoninterface.neat.ga.core.Gene;

/**
 * Extension of the Gene interface to provide more specific NEAT behaviour
 * @author MSimmerson
 *
 */
public interface NEATGene extends Gene {
	public int getInnovationNumber();
}
