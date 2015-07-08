package commoninterface.neat.core.pselectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.ChromosomeSet;
import commoninterface.neat.ga.core.ParentSelector;
import commoninterface.neat.ga.core.Population;
import commoninterface.neat.ga.core.Specie;

/**
 * @author MSimmerson
 *
 * Tournament style parent selector
 */
public class TournamentSelector implements ParentSelector {
	private int numElitist;
	private boolean naturalOrder = false;
	private Random rand = new Random();
	
	/**
	 * @see org.neat4j.ailibrary.ga.core.ParentSelector#setElitismStrategy(int)
	 */
	public void setElitismStrategy(int numElitst) {
		this.numElitist = numElitst;
	}
	
	private ChromosomeSet select(Chromosome[] genoTypes, boolean useElitismStrategy) {
		ChromosomeSet set = new ChromosomeSet(useElitismStrategy && (numElitist > 0));
		
		Chromosome pOne;
		Chromosome pTwo;
		int i;
		
		if (useElitismStrategy) {
			// need to sort.
			Arrays.sort(genoTypes);
			for (i = 0; i < this.numElitist; i++) {
				// copy numElitist of parents
				set.add(genoTypes[i]);
			}
		} else {
			// select two parents
			for (i = 0; i < 2; i++) {
				if (genoTypes.length == 1) {
					pOne = genoTypes[0];
					pTwo = genoTypes[0];
				} else {
					pOne = genoTypes[this.rand.nextInt(genoTypes.length - 1)];
					pTwo = genoTypes[this.rand.nextInt(genoTypes.length - 1)];
				}
				set.add(this.performATournament(pOne, pTwo));
			}
		}
		
		return (set);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.core.ParentSelector#selectParents(org.neat4j.ailibrary.ga.core.Population)
	 */
	public ChromosomeSet selectParents(Population currentPop, boolean useElitismStrategy) {
		Chromosome[] genoTypes = currentPop.genoTypes();
		
		return (this.select(genoTypes, useElitismStrategy));
	}
	
	private Chromosome performATournament(Chromosome pOne, Chromosome pTwo) {
		Chromosome winner = null;
		if (pOne.fitness() >= pTwo.fitness()) {
			if (!this.naturalOrder) {
				winner = pOne;
			} else {
				winner = pTwo;
			}
		} else {
			if (!this.naturalOrder) {
				winner = pTwo;
			} else {
				winner = pOne;
			}
		}		
		
		return (winner);
	}

	/**
	 * @see org.neat4j.ailibrary.ga.core.ParentSelector#setOrderStrategy(boolean)
	 */
	// TODO
	public void setOrderStrategy(boolean naturalOrder) {
		this.naturalOrder = naturalOrder;
	}

	public ChromosomeSet selectParents(Specie specie, boolean useElitism) {
		ArrayList members = specie.specieMembers();
		int i;
		Chromosome[] genoTypes = new Chromosome[members.size()];
		for (i = 0; i < members.size(); i++) {
			genoTypes[i] = (Chromosome)members.get(i);
		}

		return (this.select(genoTypes, useElitism));
	}

	public ChromosomeSet selectParents(Chromosome[] members, boolean useElitism) {
		return (this.select(members, useElitism));
	}
}
