package commoninterface.neat.data.core;

/**
 * @author MSimmerson
 *
 */
public interface ModifiableNetworkInput extends NetworkInput {
	public void modifyInput(double input, int idx);
	public void modifyLastInput(double input);
	public void modifyFirstInput(double input);
}
