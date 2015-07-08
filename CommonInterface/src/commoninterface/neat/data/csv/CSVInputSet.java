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
	private ArrayList inputs;
	private int idx;
	
	public CSVInputSet(ArrayList inputs) {
		this.inputs = inputs;
		this.idx = 0;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#size()
	 */
	public int size() {
		return (this.inputs.size());
	}

	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#nextInput()
	 */
	public NetworkInput nextInput() {
		this.idx = this.idx % this.size();
		return ((NetworkInput)this.inputs.get(idx++));		
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.NetworkInputSet#inputAt(int)
	 */
	public NetworkInput inputAt(int idx) {
		return ((NetworkInput)this.inputs.get(idx));		
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.data.NetworkInputSet#removeInputAt(int)
	 */
	public void removeInputAt(int idx) {
		if (idx < this.size()) {
			this.inputs.remove(idx);
		}
	}
}
