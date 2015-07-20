package simpletestbehaviors;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class FixedSpeedCIBehavior extends CIBehavior{
	
	private double l;
	private double r;
	
	public FixedSpeedCIBehavior(CIArguments args, RobotCI robot) {
		super(args, robot);
		l = args.getArgumentAsDouble("left");
		r = args.getArgumentAsDouble("right");
		if (robot.getLogger() != null) {
			robot.getLogger().logMessage(
					LogCodex.encodeLog(LogType.MESSAGE, "left:" + l + " right:" + r));
		}
	}

	@Override
	public void step(double timestep) {
		robot.setMotorSpeeds(l, r);
	}
	
	@Override
	public void cleanUp() {
		robot.setMotorSpeeds(0, 0);
	}

}