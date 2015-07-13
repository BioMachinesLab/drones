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
public interface GeneticAlgorithm extends Serializable {
	public GADescriptor descriptor();
	public void createPopulation();
	public void runEpoch();
	public Chromosome discoverdBestMember();
	public void pluginMutator(Mutator mut);
	public void pluginFitnessFunction(FitnessFunction func);
	public void pluginParentSelector(ParentSelector selector);
	public void pluginCrossOver(CrossOver xOver);
	public void savePopulationState(String file);
	public Population population();
}
