package commoninterface;

import commoninterface.utils.CIArguments;
import commoninterface.utils.Factory;

/**
 * @author alc
 */
public abstract class CIBehavior {
	private CIArguments args;
	protected RobotCI robot; 
	
	/**
	 * Initialize the controller
	 * 
	 * @param args    Command-line arguments.
	 * @param robot   Common drone hardware interface. 
	 * @param logger  Instance of the CI logger to use.
	 */
	public CIBehavior(CIArguments args, RobotCI robot) {
		this.robot  = robot;
		this.args   = args;
		
		if(args.getArgumentIsDefined("robotproperties")) {
			CIArguments properties = new CIArguments(args.getArgumentAsString("robotproperties"));
			for(String cprop : properties.getArguments()) {
				robot.setProperty(cprop, properties.getArgumentAsString(cprop));
			}
		}
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
		robot.setMotorSpeeds(0, 0);
	}
	
	public static CIBehavior getController(CIArguments args, RobotCI robot) {

		if (!args.getArgumentIsDefined("classname"))
			throw new RuntimeException("CIBehavior 'name' not defined: "+args.toString());
		
		return (CIBehavior)Factory.getInstance(args.getArgumentAsString("classname"),args,robot);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
