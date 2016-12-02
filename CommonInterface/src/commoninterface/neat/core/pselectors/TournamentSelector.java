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
 *         Tournament style parent selector
 */
public class TournamentSelector implements ParentSelector {
	private static final long serialVersionUID = 2472579544901218780L;
	private int numElitist;
	private boolean naturalOrder = false;
	private Random rand = new Random();

	@Override
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

	@Override
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

	// TODO
	@Override
	public void setOrderStrategy(boolean naturalOrder) {
		this.naturalOrder = naturalOrder;
	}

	@Override
	public ChromosomeSet selectParents(Specie specie, boolean useElitism) {
		ArrayList<Chromosome> members = specie.specieMembers();
		int i;
		Chromosome[] genoTypes = new Chromosome[members.size()];
		for (i = 0; i < members.size(); i++) {
			genoTypes[i] = members.get(i);
		}

		return (this.select(genoTypes, useElitism));
	}

	@Override
	public ChromosomeSet selectParents(Chromosome[] members, boolean useElitism) {
		return (this.select(members, useElitism));
	}
}
