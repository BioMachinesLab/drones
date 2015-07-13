package commoninterface.neat.data.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface NetworkOutputSet extends Serializable {
	public int size();
	public void addNetworkOutput(NetworkOutput op);
	public void removeNetworkOutput(int idx);
	public NetworkOutput nextOutput();
}
