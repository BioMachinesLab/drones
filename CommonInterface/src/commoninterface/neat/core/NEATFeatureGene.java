package commoninterface.neat.core;

/**
 * Contains gene information for extra features
 * @author MSimmerson
 *
 */
public class NEATFeatureGene implements NEATGene {
	private Double featureValue;
	private int innovationNumber;
	
	public NEATFeatureGene(int innovationNumber, double value) {
		this.featureValue = new Double(value);
		this.innovationNumber = innovationNumber;
	}
	
	public int getInnovationNumber() {
		return (this.innovationNumber);
	}

	public Number geneAsNumber() {
		return (featureValue);
	}

	public String geneAsString() {
		return (this.featureValue.toString());
	}

}
