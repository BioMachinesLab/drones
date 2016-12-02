package commoninterface.neat.core;

/**
 * Contains gene information for extra features
 * @author MSimmerson
 *
 */
public class NEATFeatureGene implements NEATGene {
	private static final long serialVersionUID = 5652100652243918472L;
	private Double featureValue;
	private int innovationNumber;
	
	public NEATFeatureGene(int innovationNumber, double value) {
		this.featureValue = new Double(value);
		this.innovationNumber = innovationNumber;
	}
	
	@Override
	public int getInnovationNumber() {
		return (this.innovationNumber);
	}

	@Override
	public Number geneAsNumber() {
		return (featureValue);
	}

	@Override
	public String geneAsString() {
		return (this.featureValue.toString());
	}

}
