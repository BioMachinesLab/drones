package commoninterface.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public interface CrossOver extends Operator {
	public void setProbability(double prob);
	public ChromosomeSet crossOver(ChromosomeSet parents); 
}
