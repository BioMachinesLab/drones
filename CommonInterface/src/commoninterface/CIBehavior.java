package commoninterface;

import commoninterface.utils.CIArguments;

/**
 * @author alc
 */
public abstract class CIBehavior {
	private CIArguments args;
	protected AquaticDroneCI drone; 
	
	/**
	 * Initialize the controller
	 * 
	 * @param args    Command-line arguments.
	 * @param drone   Common drone hardware interface. 
	 * @param logger  Instance of the CI logger to use.
	 */
	public CIBehavior(CIArguments args, AquaticDroneCI drone) {
		this.drone  = drone;
		this.args   = args;
	}

	/**
	 * Make any initial configurations.
	 */
	public void start() {		
		
	}
	
	/**
	 * Take one control step.
	 */
	public abstract void step(double timestep);
	
	/**
	 * Return the control step period: the time between sense-think-act loops.
	 * @return Control step period in seconds.
	 */
	public double getControlStepPeriod() {
		return 0.1;
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
