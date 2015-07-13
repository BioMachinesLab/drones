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
public interface GADescriptor extends Serializable {
	public int gaPopulationSize();
	public double mutationProbability();
	public double crossOverProbability();
	public int elitistSize();	
	public boolean isNaturalOrder();
}
