/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.data.csv;

import java.util.ArrayList;

import commoninterface.neat.data.core.NetworkInput;
import commoninterface.neat.data.core.NetworkInputSet;

/**
 * @author MSimmerson
 *
 */
public class CSVInputSet implements NetworkInputSet {
	private static final long serialVersionUID = 6777582249712156256L;
	private ArrayList<NetworkInput> inputs;
	private int idx;

	public CSVInputSet(ArrayList<NetworkInput> inputs) {
		this.inputs = inputs;
		this.idx = 0;
	}

	@Override
	public int size() {
		return (this.inputs.size());
	}

	@Override
	public NetworkInput nextInput() {
		this.idx = this.idx % this.size();
		return (this.inputs.get(idx++));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neat4j.ailibrary.nn.core.NetworkInputSet#inputAt(int)
	 */
	@Override
	public NetworkInput inputAt(int idx) {
		return (this.inputs.get(idx));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#removeInputAt(int)
	 */
	@Override
	public void removeInputAt(int idx) {
		if (idx < this.size()) {
			this.inputs.remove(idx);
		}
	}
}
