/*
 * Created on Oct 12, 2004
 *
 */
package commoninterface.neat.data.csv;

import commoninterface.neat.data.core.NetworkOutput;

/**
 * @author MSimmerson
 *
 */
public class CSVExpectedOutput implements NetworkOutput {
	private double[] values;
	
	public CSVExpectedOutput(double[] eOut) {
		this.values = new double[eOut.length];
		System.arraycopy(eOut, 0, this.values, 0, this.values.length);
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutput#values()
	 */
	public double[] values() {
		return (this.values);
	}

	public String toString() {
		int i;
		StringBuffer sBuff = new StringBuffer();
		for (i = 0; i < this.values.length; i++) {
			sBuff.append(this.values[i]);
			sBuff.append(",");
		}
		
		return (sBuff.toString());
	}
}
