/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.data.csv;

import java.util.ArrayList;

import commoninterface.neat.data.core.ExpectedOutputSet;
import commoninterface.neat.data.core.NetworkOutput;

/**
 * @author MSimmerson
 *
 */
public class CSVExpectedOutputSet implements ExpectedOutputSet {

	private ArrayList ops;
	private int idx;
	
	public CSVExpectedOutputSet(ArrayList eOps) {
		this.idx = 0;
		this.ops = eOps;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#size()
	 */
	public int size() {
		return (this.ops.size());
	}

	/**
	 * Wraps round to beginning
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#nextOutput()
	 */
	public NetworkOutput nextOutput() {
		this.idx = this.idx % this.size();
		return ((NetworkOutput)this.ops.get(this.idx++));		
	}
	/**
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#addNetworkOutput(org.neat4j.ailibrary.nn.core.NetworkOutput)
	 */
	public void addNetworkOutput(NetworkOutput op) {
		this.ops.add(op);
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.core.ExpectedOutputSet#outputAt(int)
	 */
	public NetworkOutput outputAt(int idx) {
		return ((NetworkOutput)this.ops.get(idx));		
	}
	/* (non-Javadoc)
	 * @see org.neat4j.ailibrary.nn.data.NetworkOutputSet#removeNetworkOutput(int)
	 */
	public void removeNetworkOutput(int idx) {
		if (idx < this.size()) {
			this.ops.remove(idx);
		}
	}
}
