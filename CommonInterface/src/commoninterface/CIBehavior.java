package commoninterface;

/**
 * 
 * @author alc
 */


public abstract class CIBehavior {
	private CILogger logger;
	private String[] args;
	protected AquaticDroneCI drone; 
	
	
	/**
	 * Initialize the controller
	 * 
	 * @param args    Command-line arguments.
	 * @param drone   Common drone hardware interface. 
	 * @param logger  Instance of the CI logger to use.
	 */
	public CIBehavior(String[] args, AquaticDroneCI drone, CILogger logger) {
		this.drone  = drone;
		this.logger = logger;
		this.args   = args;
	}

	public void start() {		
		drone.start(args, logger);
	}
	
	/**
	 * Take one control step.
	 */
	public abstract void step();
	
	/**
	 * Return the control step period: the time between sense-think-act loops.
	 * @return Control step period in seconds.
	 */
	public double getControlStepPeriod() {
		return 0.1;
	}
	
	/**
	 * Get the logger.
	 * 
	 * @return the logger assigned to this behavior.
	 */
	public CILogger getLogger() {
		return logger;
	}

	/**
	 * Check if we should terminate the behavior.
	 * 
	 * @return true if the behavior should be terminated, false otherwise.
	 */
	public boolean getTerminateBehavior() {
		return false;
	}
	
	/**
	 * After termination (either by this behavior, some other behavior or from the outside), this method 
	 * is called to allow the behavior to clean up.
	 */
	public void cleanUp() {
		
	}
}
