/*
 * Created on 23-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;

import java.util.ArrayList;
import java.util.Arrays;

import commoninterface.neat.ga.core.Chromosome;
import commoninterface.neat.ga.core.ChromosomeSet;
import commoninterface.neat.ga.core.CrossOver;
import commoninterface.neat.ga.core.Mutator;
import commoninterface.neat.ga.core.ParentSelector;
import commoninterface.neat.ga.core.Specie;

/**
 * @author MSimmerson
 *
 * A species container for NEAT individuals
 */
public class NEATSpecie extends Specie {

    private double excessCoeff;
    private double disjointCoeff;
    private double weightCoeff;
    private int specieAge;
    private double agePenalty;
    private double youthBoost;
    private int ageThreshold;
    private int youthThreshold;
    private double fitnessMultiplier = 1;
    private boolean copyBest;
    
    public NEATSpecie(double threshold, double excessCoeff, double disjointCoeff, double weightCoeff, int id, boolean copyBest) {
        super(threshold, id);
        this.disjointCoeff = disjointCoeff;
        this.excessCoeff = excessCoeff;
        this.weightCoeff = weightCoeff;
        this.copyBest = copyBest;
    }

    public boolean addSpecieMember(Chromosome specieMember) {
        boolean addedOk = false;
        if (super.addSpecieMember(specieMember)) {
            ((NEATChromosome) specieMember).setSpecieId(this.id());
            addedOk = true;
        }

        return (addedOk);
    }

    /**
     * @param agePenalty The agePenalty to set.
     */
    public void setAgePenalty(double agePenalty) {
        this.agePenalty = agePenalty;
    }

    /**
     * @param ageThreshold The ageThreshold to set.
     */
    public void setAgeThreshold(int ageThreshold) {
        this.ageThreshold = ageThreshold;
    }

    /**
     * @param specieAge The specieAge to set.
     */
    public void setSpecieAge(int specieAge) {
        this.specieAge = specieAge;
    }

    /**
     * @param youthBoost The youthBoost to set.
     */
    public void setYouthBoost(double youthBoost) {
        this.youthBoost = youthBoost;
    }

    protected void adjustFitness() {
        ArrayList members = this.specieMembers();
        int i;
        Chromosome member;
        this.fitnessMultiplier = 1;

        if (this.ageThreshold < this.specieAge) {
            this.fitnessMultiplier = this.agePenalty;
        } else if (this.youthThreshold > this.specieAge) {
            this.fitnessMultiplier = this.youthBoost;
        }

        for (i = 0; i < members.size(); i++) {
            member = (Chromosome) members.get(i);
            member.updateFitness((member.fitness() * this.fitnessMultiplier));
        }
    }

    protected double fitnessMultiplier() {
        return (this.fitnessMultiplier);
    }

    protected boolean isCompatable(Chromosome specieApplicant, Chromosome specieRepresentative) {
        boolean compatable = false;
        double compatabilityScore = Integer.MAX_VALUE;

        if (specieRepresentative == null) {
            compatable = true;
        } else {
            compatabilityScore = NEATSpecieManager.specieManager().compatibilityScore(specieApplicant, specieRepresentative, this.excessCoeff, this.disjointCoeff, this.weightCoeff);
            //cat.debug("compatabilityScore:" + compatabilityScore);
            compatable = compatabilityScore < this.specieThreshold();
        }

        return (compatable);
    }

    protected Chromosome[] produceOffspring(int count, Mutator mut, ParentSelector selector, CrossOver xOver) {
        int i = 0;
        ChromosomeSet parents;
        ChromosomeSet child;
        Chromosome[] offspring = new Chromosome[count];
        Chromosome[] matableMembers;

        // first order the members
        ArrayList unsorted = this.specieMembers();
        Object[] sorted = unsorted.toArray();
        Arrays.sort(sorted);

        // take the top n%
        int matableCount = (int) Math.ceil(sorted.length * this.getSurvivalThreshold());
        matableMembers = new NEATChromosome[matableCount];

        for (i = 0; i < matableCount; i++) {
            matableMembers[i] = (NEATChromosome) sorted[i];
        }

        if (count > 0 && copyBest) {
            // copy best member.
            offspring[0] = this.cloneChromosome((NEATChromosome) sorted[0]);
        }

        try {
            for (i = (copyBest ? 1 : 0) ; i < offspring.length; i++) {
                parents = selector.selectParents(matableMembers, false);
                child = xOver.crossOver(this.cloneParents(parents));
                offspring[i] = mut.mutate(child.nextChromosome());
                parents = null;
            }
            this.specieAge++;
        } catch (Exception e) {
            System.err.println("produceOffspring produced error:count:" + count + ":current iteration:" + i + ":specie size:" + this.specieMembers().size());
            e.printStackTrace();
        }

        return (offspring);
    }

    public int specieAge() {
        return (this.specieAge);
    }

    /**
     * @param youthThreshold The youthThreshold to set.
     */
    public void setYouthThreshold(int youthThreshold) {
        this.youthThreshold = youthThreshold;
    }

    private ChromosomeSet cloneParents(ChromosomeSet clonee) {
        ChromosomeSet clonedSet = new ChromosomeSet();

        Chromosome cloneeChromo = clonee.nextChromosome();
        Chromosome clone = this.cloneChromosome(cloneeChromo);
        clone.updateFitness(cloneeChromo.fitness());
        clonedSet.add(clone);

        cloneeChromo = clonee.nextChromosome();
        clone = this.cloneChromosome(cloneeChromo);
        clone.updateFitness(cloneeChromo.fitness());
        clonedSet.add(clone);

        return (clonedSet);
    }

    public int compareTo(Object arg0) {
        int compare = 0;
        Specie s;
        if (arg0 instanceof Specie) {
            s = (Specie) arg0;
            if (s.getAverageFitness() > this.getAverageFitness()) {
                compare = 1;
            } else if (s.getAverageFitness() < this.getAverageFitness()) {
                compare = -1;
            }
        }

        return (compare);
    }
}
