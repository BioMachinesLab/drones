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
public interface Chromosome extends Comparable, Serializable {
	public Gene[] genes();
	public int size();
	public void updateChromosome(Gene[] newGenes);
	public void updateFitness(double fitness);
	public double fitness();
}
