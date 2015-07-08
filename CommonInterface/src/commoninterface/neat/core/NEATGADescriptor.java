/*
 * Created on 20-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package commoninterface.neat.core;

import commoninterface.neat.ga.core.GADescriptor;

/**
 * NEAT GA Descriptor - provides the behaviour environment for the NEAT GA.
 * @author MSimmerson
 *
 */
public class NEATGADescriptor implements GADescriptor {
	private static final long serialVersionUID = 1L;
	private double pXover;
	private double pAddLink;
	private double pAddNode;
	private double pToggleLink;
	private double pMutation;
	private double pMutateBias;
	private int inputNodes;
	private int outputNodes;
	private int popSize;
	private double disjointCoeff;
	private double excessCoeff;
	private double weightCoeff;
	private double threshold;
	private int maxSpecieAge;
	private int specieAgeThreshold;
	private int specieYouthThreshold;
	private double agePenalty;
	private double youthBoost;
	private double compatabilityChange;
	private int specieCount;
	private double pWeightReplaced;
	private double survivalThreshold;
	private boolean featureSelection;
	private int extraFeatureCount;
	private boolean eleEvents;
	private double eleSurvivalCount;
	private int eleEventTime;
	private boolean recurrencyAllowed;
	private boolean keepBestEver;
	private double terminationValue;
	private boolean naturalOrder;
	private double maxPerturb;
	private double maxBiasPerturb;
        private boolean copyBest;

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
	/**
	 * @return Returns the compatabilityChange.
	 */
	public double getCompatabilityChange() {
		return compatabilityChange;
	}
	/**
	 * @param compatabilityChange The compatabilityChange to set.
	 */
	public void setCompatabilityChange(double compatabilityChange) {
		this.compatabilityChange = compatabilityChange;
	}
	/**
	 * @return Returns the pWeightReplaced.
	 */
	public double getPWeightReplaced() {
		return pWeightReplaced;
	}
	/**
	 * @param weightReplaced The pWeightReplaced to set.
	 */
	public void setPWeightReplaced(double weightReplaced) {
		pWeightReplaced = weightReplaced;
	}
	/**
	 * @return Returns the specieCount.
	 */
	public int getSpecieCount() {
		return specieCount;
	}
	/**
	 * @param specieCount The specieCount to set.
	 */
	public void setSpecieCount(int specieCount) {
		this.specieCount = specieCount;
	}
	/**
	 * @return Returns the agePenalty.
	 */
	public double getAgePenalty() {
		return agePenalty;
	}
	/**
	 * @param agePenalty The agePenalty to set.
	 */
	public void setAgePenalty(double agePenalty) {
		this.agePenalty = agePenalty;
	}
	/**
	 * @return Returns the maxSpecieAge.
	 */
	public int getMaxSpecieAge() {
		return maxSpecieAge;
	}
	/**
	 * @param maxSpecieAge The maxSpecieAge to set.
	 */
	public void setMaxSpecieAge(int maxSpecieAge) {
		this.maxSpecieAge = maxSpecieAge;
	}
	/**
	 * @return Returns the specieAgeThreshold.
	 */
	public int getSpecieAgeThreshold() {
		return specieAgeThreshold;
	}
	/**
	 * @param specieAgeThreshold The specieAgeThreshold to set.
	 */
	public void setSpecieAgeThreshold(int specieAgeThreshold) {
		this.specieAgeThreshold = specieAgeThreshold;
	}
	/**
	 * @return Returns the youthBoost.
	 */
	public double getYouthBoost() {
		return youthBoost;
	}
	/**
	 * @param youthBoost The youthBoost to set.
	 */
	public void setYouthBoost(double youthBoost) {
		this.youthBoost = youthBoost;
	}
	/**
	 * @return Returns the disjointCoeff.
	 */
	public double getDisjointCoeff() {
		return disjointCoeff;
	}
	/**
	 * @param disjointCoeff The disjointCoeff to set.
	 */
	public void setDisjointCoeff(double disjointCoeff) {
		this.disjointCoeff = disjointCoeff;
	}
	/**
	 * @return Returns the excessCoeff.
	 */
	public double getExcessCoeff() {
		return excessCoeff;
	}
	/**
	 * @param excessCoeff The excessCoeff to set.
	 */
	public void setExcessCoeff(double excessCoeff) {
		this.excessCoeff = excessCoeff;
	}
	/**
	 * @return Returns the popSize.
	 */
	public int getPopSize() {
		return popSize;
	}
	/**
	 * @param popSize The popSize to set.
	 */
	public void setPopSize(int popSize) {
		this.popSize = popSize;
	}
	/**
	 * @return Returns the threshold.
	 */
	public double getThreshold() {
		return threshold;
	}
	/**
	 * @param threshold The threshold to set.
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	/**
	 * @return Returns the weightCoeff.
	 */
	public double getWeightCoeff() {
		return weightCoeff;
	}
	/**
	 * @param weightCoeff The weightCoeff to set.
	 */
	public void setWeightCoeff(double weightCoeff) {
		this.weightCoeff = weightCoeff;
	}
	/**
	 * @return Returns the pAddLink.
	 */
	public double getPAddLink() {
		return pAddLink;
	}
	/**
	 * @param addLink The pAddLink to set.
	 */
	public void setPAddLink(double addLink) {
		pAddLink = addLink;
	}
	/**
	 * @return Returns the pAddNode.
	 */
	public double getPAddNode() {
		return pAddNode;
	}
	/**
	 * @param addNode The pAddNode to set.
	 */
	public void setPAddNode(double addNode) {
		pAddNode = addNode;
	}
	/**
	 * @param disableLink The pDisableLink to set.
	 */
	public void setPToggleLink(double toggleLink) {
		pToggleLink = toggleLink;
	}
	/**
	 * @return Returns the pMutation.
	 */
	public double getPMutation() {
		return pMutation;
	}
	/**
	 * @param mutation The pMutation to set.
	 */
	public void setPMutation(double mutation) {
		pMutation = mutation;
	}
	/**
	 * @return Returns the pXover.
	 */
	public double getPXover() {
		return pXover;
	}
	/**
	 * @param xover The pXover to set.
	 */
	public void setPXover(double xover) {
		pXover = xover;
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.ga.core.GADescriptor#gaPopulationSize()
	 */
	public int gaPopulationSize() {
		return this.popSize;
	}
	
	public void setPopulationSize(int popSize) {
		this.popSize = popSize;
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.ga.core.GADescriptor#mutationProbability()
	 */
	public double mutationProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.ga.core.GADescriptor#crossOverProbability()
	 */
	public double crossOverProbability() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.ga.core.GADescriptor#elitistSize()
	 */
	public int elitistSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isNaturalOrder() {
		return (this.naturalOrder);
	}

	/**
	 * @return Returns the inputNodes.
	 */
	public int getInputNodes() {
		return inputNodes;
	}
	/**
	 * @param inputNodes The inputNodes to set.
	 */
	public void setInputNodes(int inputNodes) {
		this.inputNodes = inputNodes;
	}
	/**
	 * @return Returns the outputNodes.
	 */
	public int getOutputNodes() {
		return outputNodes;
	}
	/**
	 * @param outputNodes The outputNodes to set.
	 */
	public void setOutputNodes(int outputNodes) {
		this.outputNodes = outputNodes;
	}
	/**
	 * @return Returns the pToggleLink.
	 */
	public double getPToggleLink() {
		return pToggleLink;
	}
	/**
	 * @return Returns the specieYouthThreshold.
	 */
	public int getSpecieYouthThreshold() {
		return specieYouthThreshold;
	}
	/**
	 * @param specieYouthThreshold The specieYouthThreshold to set.
	 */
	public void setSpecieYouthThreshold(int specieYouthThreshold) {
		this.specieYouthThreshold = specieYouthThreshold;
	}
	/**
	 * @return Returns the featureSelection.
	 */
	public boolean featureSelectionEnabled() {
		return featureSelection;
	}
	/**
	 * @param featureSelection The featureSelection to set.
	 */
	public void setFeatureSelection(boolean featureSelection) {
		this.featureSelection = featureSelection;
	}
	public int getExtraFeatureCount() {
		return extraFeatureCount;
	}
	public void setExtraFeatureCount(int extraFeatureCount) {
		this.extraFeatureCount = extraFeatureCount;
	}
	public boolean isEleEvents() {
		return eleEvents;
	}
	public void setEleEvents(boolean eleEvents) {
		this.eleEvents = eleEvents;
	}
	public int getEleEventTime() {
		return eleEventTime;
	}
	public void setEleEventTime(int eleEventTime) {
		this.eleEventTime = eleEventTime;
	}
	public double getEleSurvivalCount() {
		return eleSurvivalCount;
	}
	public void setEleSurvivalCount(double eleSurvivalCount) {
		this.eleSurvivalCount = eleSurvivalCount;
	}
	public boolean isRecurrencyAllowed() {
		return recurrencyAllowed;
	}
	public void setRecurrencyAllowed(boolean recurrencyAllowed) {
		this.recurrencyAllowed = recurrencyAllowed;
	}
	public boolean keepBestEver() {
		return keepBestEver;
	}
	public void setKeepBestEver(boolean keepBestEver) {
		this.keepBestEver = keepBestEver;
	}
	public double getPMutateBias() {
		return pMutateBias;
	}
	public void setPMutateBias(double mutateBias) {
		pMutateBias = mutateBias;
	}
	public double getTerminationValue() {
		return terminationValue;
	}
	public void setTerminationValue(double terminationValue) {
		this.terminationValue = terminationValue;
	}
	public void setNaturalOrder(boolean naturalOrder) {
		this.naturalOrder = naturalOrder;
	}
	public double getMaxBiasPerturb() {
		return maxBiasPerturb;
	}
	public void setMaxBiasPerturb(double maxBiasPerturb) {
		this.maxBiasPerturb = maxBiasPerturb;
	}
	public double getMaxPerturb() {
		return maxPerturb;
	}
	public void setMaxPerturb(double maxPerturb) {
		this.maxPerturb = maxPerturb;
	}
        
        public boolean getCopyBest() {
            return copyBest;
        }
        
        public void setCopyBest(boolean b) {
            this.copyBest = b;
        }
}
