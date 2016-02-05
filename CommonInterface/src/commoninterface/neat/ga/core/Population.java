/*
 * Created on 27-Oct-2004
 *
 * 
 */
package commoninterface.neat.ga.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface Population extends Serializable {
	public Chromosome[] genoTypes();
	public void createPopulation();
	public void updatePopulation(Chromosome[] newGenoTypes);
}
