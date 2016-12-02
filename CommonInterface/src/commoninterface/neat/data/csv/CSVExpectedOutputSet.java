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
	private static final long serialVersionUID = -8890738684265135411L;
	private ArrayList<NetworkOutput> ops;
	private int idx;

	public CSVExpectedOutputSet(ArrayList<NetworkOutput> eOps) {
		this.idx = 0;
		this.ops = eOps;
	}

	@Override
	public int size() {
		return (this.ops.size());
	}

	/**
	 * Wraps round to beginning
	 * 
	 */
	@Override
	public NetworkOutput nextOutput() {
		this.idx = this.idx % this.size();
		return (this.ops.get(this.idx++));
	}

	@Override
	public void addNetworkOutput(NetworkOutput op) {
		this.ops.add(op);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neat4j.ailibrary.nn.core.ExpectedOutputSet#outputAt(int)
	 */
	@Override
	public NetworkOutput outputAt(int idx) {
		return (this.ops.get(idx));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neat4j.ailibrary.nn.data.NetworkOutputSet#removeNetworkOutput(int)
	 */
	@Override
	public void removeNetworkOutput(int idx) {
		if (idx < this.size()) {
			this.ops.remove(idx);
		}
	}
}
