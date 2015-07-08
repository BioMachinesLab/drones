/*
 * Created on Oct 13, 2004
 *
 */
package commoninterface.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public interface ParentSelector extends Operator {
	public void setElitismStrategy(int numElitst);
	public void setOrderStrategy(boolean naturalOrder);
	public ChromosomeSet selectParents(Population currentPop, boolean useElitism);
	public ChromosomeSet selectParents(Specie specie, boolean useElitism);
	public ChromosomeSet selectParents(Chromosome[] members, boolean useElitism);
}
