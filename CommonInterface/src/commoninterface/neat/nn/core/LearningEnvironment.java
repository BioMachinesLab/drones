/*
 * Created on Sep 30, 2004
 *
 */
package commoninterface.neat.nn.core;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author MSimmerson
 *
 */
public class LearningEnvironment implements Serializable {
	private HashMap env;
	
	public LearningEnvironment() {
		this.env = new HashMap();
	}
	
	public void addEnvironmentParameter(String key, Object value) {
		if (value != null) {
			this.env.put(key, value);
		}
	}
	
	public Object learningParameter(String key) {
		return this.env.get(key);
	}
}
