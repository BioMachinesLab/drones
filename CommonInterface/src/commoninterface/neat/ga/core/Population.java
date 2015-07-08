/*
 * Created on 27-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
