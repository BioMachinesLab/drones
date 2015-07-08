package commoninterface.neat.data.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface NetworkInputSet extends Serializable{
	public int size();
	public NetworkInput nextInput();
	public NetworkInput inputAt(int idx);	
	public void removeInputAt(int idx);
}
