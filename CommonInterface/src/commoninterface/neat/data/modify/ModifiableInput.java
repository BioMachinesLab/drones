/*
 * Created on Oct 12, 2004
 *
 */
package commoninterface.neat.data.modify;

import commoninterface.neat.data.core.ModifiableNetworkInput;
import commoninterface.neat.data.core.NetworkInput;

/**
 * @author MSimmerson
 *
 */
public class ModifiableInput implements ModifiableNetworkInput {
	private static final long serialVersionUID = 4007178178300814269L;
	NetworkInput ip;

	public ModifiableInput(NetworkInput ip) {
		this.ip = ip;
	}

	@Override
	public double[] pattern() {
		return (this.ip.pattern());
	}

	@Override
	public String toString() {
		int i;
		StringBuffer sBuff = new StringBuffer();
		for (i = 0; i < this.ip.pattern().length; i++) {
			sBuff.append(this.ip.pattern()[i]);
			sBuff.append(",");
		}

		return (sBuff.toString());
	}

	@Override
	public void modifyInput(double input, int idx) {
		if (idx < this.ip.pattern().length) {
			this.ip.pattern()[idx] = input;
		}
	}

	@Override
	public void modifyLastInput(double input) {
		this.modifyInput(input, this.ip.pattern().length - 1);
	}

	@Override
	public void modifyFirstInput(double input) {
		this.modifyInput(input, 0);
	}
}
