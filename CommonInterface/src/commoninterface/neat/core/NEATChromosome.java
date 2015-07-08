/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;

import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.Gene;

/**
 * NEAT specific chromosome
 * @author MSimmerson
 *
 */
public class NEATChromosome implements Chromosome {
    
    private static final long serialVersionUID = -1L;
    
	private Gene[] genes;
	private double fitness;
	private int specieId = -1;
	private boolean nOrder = false;
	
	
	public NEATChromosome(Gene[] genes) {
		this.updateChromosome(genes);
	}

	
	/**
	 * @return Returns the specieId.
	 */
	public int getSpecieId() {
		return specieId;
	}

	/**
	 * @param specieId The specieId to set.
	 */
	public void setSpecieId(int specieId) {
		this.specieId = specieId;
	}

	public Gene[] genes() {
		return (this.genes);
	}

	public int size() {
		return (this.genes.length);
	}

	public void updateChromosome(Gene[] newGenes) {
		this.genes = new NEATGene[newGenes.length];
		System.arraycopy(newGenes, 0, this.genes, 0, this.genes.length);
	}

	public void updateFitness(double fitness) {
		this.fitness = fitness;
	}

	public double fitness() {
		return (this.fitness);
	}
	
	public void setNaturalOrder(boolean nOrder) {
		this.nOrder = nOrder;
	}

	public int compareTo(Object o) {
		int returnVal = 0;
		NEATChromosome test = (NEATChromosome)o;
		// sorts with highest first
		if (this.fitness > test.fitness()) {
			if (this.nOrder) {
				returnVal = 1;
			} else {
				returnVal = -1;
			}
		} else if (this.fitness < test.fitness()) {
			if (this.nOrder) {
				returnVal = -1;
			} else {
				returnVal = 1;
			}
		}
		
		return (returnVal);
	}
}
