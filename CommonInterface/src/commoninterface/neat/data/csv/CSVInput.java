/*
 * Created on Oct 12, 2004
 *
 */
package commoninterface.neat.data.csv;

import commoninterface.neat.data.core.NetworkInput;

/**
 * @author MSimmerson
 *
 */
public class CSVInput implements NetworkInput {
	private double[] inputPattern;
	
	public CSVInput(double[] input) {
		this.inputPattern = new double[input.length];
		System.arraycopy(input, 0, this.inputPattern, 0, this.inputPattern.length);
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInput#pattern()
	 */
	public double[] pattern() {
		return (this.inputPattern);
	}
	
	public String toString() {
		int i;
		StringBuffer sBuff = new StringBuffer();
		for (i = 0; i < this.inputPattern.length; i++) {
			sBuff.append(this.inputPattern[i]);
			sBuff.append(",");
		}
		
		return (sBuff.toString());
	}
}
