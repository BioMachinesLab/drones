/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.core;

import java.util.ArrayList;

import commoninterface.neat.data.core.NetworkOutput;
import commoninterface.neat.data.core.NetworkOutputSet;

/**
 * @author MSimmerson
 *
 *         Stores a output set from an NEAT neural network evaluation run.
 */
public class NEATNetOutputSet implements NetworkOutputSet {
	private static final long serialVersionUID = 1L;
	private ArrayList<NetworkOutput> outputSet;
	private int idx;

	public NEATNetOutputSet() {
		this.outputSet = new ArrayList<NetworkOutput>();
		this.idx = 0;
	}

	@Override
	public int size() {
		return (this.outputSet.size());
	}

	@Override
	public NetworkOutput nextOutput() {
		this.idx = this.idx % this.size();
		return (this.outputSet.get(this.idx++));
	}

	@Override
	public void addNetworkOutput(NetworkOutput op) {
		this.outputSet.add(op);
	}

	@Override
	public void removeNetworkOutput(int idx) {
		if (idx < this.size()) {
			this.outputSet.remove(idx);
		}
	}
}
