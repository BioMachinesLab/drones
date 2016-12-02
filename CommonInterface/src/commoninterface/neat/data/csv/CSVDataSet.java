/*
 * Created on Oct 12, 2004
 *
 */
package commoninterface.neat.data.csv;

import commoninterface.neat.data.core.ExpectedOutputSet;
import commoninterface.neat.data.core.NetworkDataSet;
import commoninterface.neat.data.core.NetworkInputSet;

/**
 * @author MSimmerson
 *
 */
public class CSVDataSet implements NetworkDataSet {
	private static final long serialVersionUID = -9173471273009207254L;
	private NetworkInputSet inputSet;
	private ExpectedOutputSet expectedOutputSet;

	public CSVDataSet() {
		// deliberate empty constructor
	}

	public CSVDataSet(NetworkInputSet inputSet, ExpectedOutputSet expectedOutputSet) {
		this.inputSet = inputSet;
		this.expectedOutputSet = expectedOutputSet;
	}

	@Override
	public NetworkInputSet inputSet() {
		return (this.inputSet);
	}

	@Override
	public ExpectedOutputSet expectedOutputSet() {
		return (this.expectedOutputSet);
	}
}
