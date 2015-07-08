/*
 * Created on Oct 13, 2004
 *
 */
package commoninterface.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public interface Mutator extends Operator {
	public void setProbability(double prob);
	public Chromosome mutate(Chromosome mutatee);
}
