package commoninterface.neat.ga.core;

/**
 * @author MSimmerson
 *
 */
public class OperatorFactory {
	private static final OperatorFactory factory = new OperatorFactory();
	
	private OperatorFactory() {
	}
	
	public static OperatorFactory factory() {
		return (factory);
	}
	
	public Operator createOperator(String operator) {
		return (null);
	}
}
