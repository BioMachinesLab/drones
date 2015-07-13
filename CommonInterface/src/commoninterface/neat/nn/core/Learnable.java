/*
 * Created on Sep 30, 2004
 *
 */
package commoninterface.neat.nn.core;

import java.io.Serializable;


/**
 * @author MSimmerson
 *
 */
public interface Learnable  extends Serializable {
	public void teach(NeuralNet net);
	public LearningEnvironment learningEnvironment();
}
