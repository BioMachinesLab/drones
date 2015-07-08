/*
 * Created on Sep 27, 2004
 *
 */
package commoninterface.neat.nn.core;

import java.io.Serializable;

/**
 * @author MSimmerson
 *
 */
public interface LayerInput  extends Serializable {
	double[] inputs();
}
