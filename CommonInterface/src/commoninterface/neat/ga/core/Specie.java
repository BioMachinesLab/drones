/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.ga.core;

import java.util.ArrayList;

import commoninterface.neat.core.NEATChromosome;
import commoninterface.neat.core.NEATFeatureGene;
import commoninterface.neat.core.NEATGene;
import commoninterface.neat.core.NEATLinkGene;
import commoninterface.neat.core.NEATNodeGene;

/**
 * @author MSimmerson
 *
 * Controls addition of specie members and provides specie behaviour. 
 */
public abstract class Specie implements Comparable {
	private ArrayList specieMembers;
	private double bestFitness;
	private double avFitness = 0;
	private int maxFitnessAge;
	private int currentFitnessAge = 0;
	private boolean extinct = false;	
	private double threshold;
	private Chromosome specieRepresentative = null;
	private int specieId = -1;	
	private double bestAvFitness = 0;
	private double survivalThreshold = 0.3;
	
	public Specie(double threshold, int id) {
		this.specieMembers = new ArrayList();
		this.threshold = threshold;
		//this.bestFitness = 0;
		this.specieId = id;
	}
	
	public int id() {
		return (this.specieId);
	}
	
	/**
	 * Ages the specie fitness age if the best has not increased in fitness or the
	 * average fitness as a whole has not increased.
	 *
	 */
	public void ageFitness() {
		int i;
		double totalFitness = 0;
		
		if (this.specieMembers.size() > 0) {
			for (i = 0; i < this.specieMembers.size(); i++) {
				totalFitness += ((Chromosome)this.specieMembers.get(i)).fitness();
			}
			this.avFitness = totalFitness / this.specieMembers.size();
			if (this.avFitness > this.bestAvFitness) {
				this.currentFitnessAge = 0;
				this.bestAvFitness = this.avFitness; 
			} else if (this.specieRepresentative.fitness() > this.bestFitness) {
				this.currentFitnessAge = 0;
				this.bestFitness = this.specieRepresentative.fitness(); 
			} else {
				this.currentFitnessAge++;
			}
		}
	}
	
	/**
	 * Clears all memebers from the species and checks to see if it is extinct.
	 * @param threshold
	 */
	public void resetSpecie(double threshold) {
		int specieSize = this.specieMembers.size();
		if ((this.currentFitnessAge >= this.maxFitnessAge) || (specieSize == 0)) {
			this.extinct = true;
		} else {
			this.threshold = threshold;
		}
		this.clearSpecieMembers();
	}
	
	protected void clearSpecieMembers() {
		this.specieMembers = new ArrayList();
	}
	
	public Chromosome findBestMember() {		
		return (this.specieRepresentative);
	}
	
	/**
	 * Adds the given individual to the specie iff it is compatible 
	 * with the fitest member.
	 * @param specieMember
	 * @return true if added, false otherwise
	 */
	public boolean addSpecieMember(Chromosome specieMember) {
		boolean addedOk = false;
		boolean isCompat = false;

		if (this.specieMembers.size() == 0 && this.specieRepresentative == null) {
			this.specieRepresentative = this.cloneChromosome(specieMember);
			this.specieRepresentative.updateFitness(specieMember.fitness());
			this.specieMembers.add(specieMember);
			addedOk = true;
		} else {
			isCompat = this.isCompatable(specieMember, this.specieRepresentative); 
			if (isCompat) {
				if (specieMember.fitness() > this.specieRepresentative.fitness()) {
					this.specieRepresentative = this.cloneChromosome(specieMember);
					this.specieRepresentative.updateFitness(specieMember.fitness());
				}
				
				this.specieMembers.add(specieMember);
				addedOk = true;
			}
		}
		
		return (addedOk);
	}
	
	protected void addMatable(Chromosome specieMember) {
		this.specieMembers.add(specieMember);
	}
	
	public boolean isExtinct() {
		//return (this.extinct || (this.specieMembers.size() == 0));
		return (this.extinct);
	}
	
	public void setExtinct() {
		this.extinct = true;
	}
	
	public void reprieve() {
		this.extinct = false;
		this.currentFitnessAge = 0;
	}
	
	public double specieThreshold() {
		return (this.threshold);
	}
	
	public ArrayList specieMembers() {
		return (this.specieMembers);
	}
	
	public double averageFitness() {
		return (this.avFitness);
	}
	
	/**
	 * Creates offspring from the specie.  The offspring count is passed in and is 
	 * dependant on the overall specie fitness.
	 * @param offspringCount
	 * @param mut
	 * @param selector
	 * @param xOver
	 * @return Specie offspring
	 */
	public ChromosomeSet specieOffspring(int offspringCount, Mutator mut, ParentSelector selector, CrossOver xOver) {
		int i;
		Chromosome[] offspring = this.produceOffspring(offspringCount, mut, selector, xOver);
		ChromosomeSet specieOffspring = new ChromosomeSet();
		for (i = 0; i < offspring.length; i++) {
			specieOffspring.add(offspring[i]);
		}
	
		return (specieOffspring);
	}
	
	protected abstract void adjustFitness();
	protected abstract boolean isCompatable(Chromosome specieApplicant, Chromosome specieRepresentative);
	protected abstract Chromosome[] produceOffspring(int count, Mutator mut, ParentSelector selector, CrossOver xOver);
	protected abstract double fitnessMultiplier();
	/**
	 * @return Returns the currentFitnessAge.
	 */
	public int getCurrentFitnessAge() {
		return currentFitnessAge;
	}
	/**
	 * @return Returns the averageFitness.
	 */
	public double getAverageFitness() {
		return (this.avFitness);
	}
	/**
	 * @param maxFitnessAge The maxFitnessAge to set.
	 */
	public void setMaxFitnessAge(int maxFitnessAge) {
		this.maxFitnessAge = maxFitnessAge;
	}
	
	public int maxFittnessAge() {
		return (this.maxFitnessAge);
	}

	public Chromosome cloneChromosome(Chromosome clonee) {
		Gene[] genes = clonee.genes();
		Chromosome clone = new NEATChromosome(this.cloneGenes(genes));
		((NEATChromosome)clone).setSpecieId(((NEATChromosome)clonee).getSpecieId());
		
		return (clone);
	}
	
	public Gene[] cloneGenes(Gene[] clonee) {
		Gene[] cloned = new Gene[clonee.length];
		int i;
		
		for (i = 0; i < clonee.length; i++) {
			if (clonee[i] instanceof NEATLinkGene) {
				//int innovationNumber, boolean enabled, int fromId, int toId, double weight
				cloned[i] = new NEATLinkGene(((NEATGene)clonee[i]).getInnovationNumber(),
											 ((NEATLinkGene)clonee[i]).isEnabled(), 
											 ((NEATLinkGene)clonee[i]).getFromId(), 
											 ((NEATLinkGene)clonee[i]).getToId(),
											 ((NEATLinkGene)clonee[i]).getWeight()
											);											 
			} else if (clonee[i] instanceof NEATNodeGene) {
				//int innovationNumber, int id, double bias, int type
				cloned[i] = new NEATNodeGene(((NEATGene)clonee[i]).getInnovationNumber(),
											 ((NEATNodeGene)clonee[i]).id(), 
											 ((NEATNodeGene)clonee[i]).sigmoidFactor(), 
											 ((NEATNodeGene)clonee[i]).getType(),
											 ((NEATNodeGene)clonee[i]).bias()
											);
			} else if (clonee[i] instanceof NEATFeatureGene) {
				cloned[i] = new NEATFeatureGene(((NEATGene)clonee[i]).getInnovationNumber(),
												((NEATFeatureGene)clonee[i]).geneAsNumber().doubleValue()
											);
			}
		}
		return (cloned);
	}
	
	public boolean containsMember(NEATChromosome member) {
		return (this.id() == member.getSpecieId());
	}

	/**
	 * @return Returns the survivalThreshold.
	 */
	public double getSurvivalThreshold() {
		return survivalThreshold;
	}

	/**
	 * @param survivalThreshold The survivalThreshold to set.
	 */
	public void setSurvivalThreshold(double survivalThreshold) {
		this.survivalThreshold = survivalThreshold;
	}
}
