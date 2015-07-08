/*
 * Created on Oct 6, 2004
 *
 */
package commoninterface.neat.data.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface NetworkDataSet extends Serializable{
	public NetworkInputSet inputSet();
	public ExpectedOutputSet expectedOutputSet();
}
