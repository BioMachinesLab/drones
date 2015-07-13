/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.ga.core;

import java.util.ArrayList;

import commoninterface.neat.core.NEATChromosome;
import commoninterface.neat.core.NEATSpecie;

/**
 * @author MSimmerson
 *
 * Generic specie handling
 */
public class Species {
	private ArrayList specieList;
	
	public Species() {
		this.specieList = new ArrayList();
	}
	
	public void addSpecie(Specie specie) {
		this.specieList.add(specie);
	}
	
	public ArrayList specieList() {
		return (this.specieList);
	}

	/**
	 * Provides a list of species that have not become extinct (or contains the champion individual)
	 * @param championSpecieId
	 * @return
	 */
	public ArrayList validSpecieList(int championSpecieId) {
		ArrayList validSpecies = new ArrayList();
		Specie specie;
		int i;
		
		for (i = 0; i < this.specieList.size(); i++) {
			specie = (Specie)this.specieList.get(i);
			// always include champion specie
			if ((!specie.isExtinct() || (specie.id() == championSpecieId)) && specie.specieMembers().size() > 0) {
				validSpecies.add(specie);
			}
		}
				
		return (validSpecies);
	}
	
	/**
	 * Clears down all the current species
	 * @param threshold
	 */
	public void resetSpecies(double threshold) {
		int i;
		Specie specie;
		
		for (i = 0; i < this.specieList.size(); i++) {
			specie = (Specie)this.specieList.get(i);
			if (!specie.isExtinct()) {
				specie.resetSpecie(threshold);
			}
		}
	}
	
	public void shareFitness() {
		int i;
		Specie specie;
		
		for (i = 0; i < this.specieList.size(); i++) {
			specie = (Specie)this.specieList.get(i);
			if (specie.specieMembers().size() > 0) {
				specie.adjustFitness();
				specie.ageFitness();
			}
		}
	}
	
	public double totalAvSpeciesFitness() {
		int i;
		Specie specie;
		double totalFitness = 0;
		
		for (i = 0; i < this.specieList.size(); i++) {
			specie = (Specie)this.specieList.get(i);
			if (!specie.isExtinct() && specie.specieMembers().size() > 0) {
				totalFitness += specie.averageFitness();
			}
		}
		
		return (totalFitness);
	}
	
	public void removeExtinctSpecies(NEATChromosome champion) {
		int i = 0;
		Specie specie;
		
		while (i < this.specieList.size()) {
			specie = (Specie)this.specieList.get(i);			
			if (specie.isExtinct() || specie.specieMembers().size() == 0) {
				if (!specie.containsMember(champion) && (this.specieList.size() > 1)) {
					//cat.info("Removing specie " + specie.id() + " size:" + specie.specieMembers().size() + ":fage:" + specie.getCurrentFitnessAge() + ":age:" + ((NEATSpecie)specie).specieAge() + ":avF:" + specie.averageFitness());
					this.specieList.remove(i);
				} else {
					//cat.info("Specie " + specie.id() + " saved:" + specie.specieMembers().size() + ":fage:" + specie.getCurrentFitnessAge() + ":age:" + ((NEATSpecie)specie).specieAge() + ":avF:" + specie.averageFitness());
					specie.reprieve();
					i++;
				}
			} else {
				i++;
			}
		}		
	}
	
	public Chromosome findBestFromSpecies() {
		Chromosome best = null;
		int i;
		Specie specie;
		
		for (i = 0; i < this.specieList.size(); i++) {
			specie = (Specie)this.specieList.get(i);
			if (specie.specieMembers().size() > 0) {
				if ((best == null) || (best.fitness() > specie.findBestMember().fitness())) {
					best = specie.findBestMember();					
				} 
			}
		}
		
		return (best);
	}
	
	public int calcSpecieOffspringCount(Specie specie, int popSize, double totalAvFitness) {
		int count = (int)Math.floor((specie.averageFitness() / totalAvFitness) * popSize);
		
		return (count);
	}

	public Specie selectSpecie(int specieId) {
		Specie selectedSpecie = null;
		boolean specieFound = false;
		NEATSpecie testSpecie;
		int i = 0;
		
		while (!specieFound && i < this.specieList.size() && specieId >= 1) {
			testSpecie = (NEATSpecie)this.specieList.get(i);
			if (testSpecie.id() == specieId) {
				specieFound = true;
				selectedSpecie = testSpecie;
			}
			i++;
		}
		
		return (selectedSpecie);
	}
}
