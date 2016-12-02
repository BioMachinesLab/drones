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
	private static final long serialVersionUID = 8595259319277713400L;
	private double[] values;
	
	public CSVExpectedOutput(double[] eOut) {
		this.values = new double[eOut.length];
		System.arraycopy(eOut, 0, this.values, 0, this.values.length);
	}
	
	@Override
	public double[] values() {
		return (this.values);
	}

	@Override
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
