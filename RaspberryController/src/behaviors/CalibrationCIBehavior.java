package behaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.CILogger;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class CalibrationCIBehavior extends CIBehavior {
	
	private boolean executing = false;
	private boolean executed = false;
	private long startTime = 0;
	private RealAquaticDroneCI drone;
	private int timeIncrement = 10;
	
	private double speed = 0.2;
	
	public CalibrationCIBehavior(CIArguments args, AquaticDroneCI drone) {
		super(args,drone);
		this.drone = (RealAquaticDroneCI)drone;
	}

	@Override
	public void step(double time) {
		
		if(!executing && !executed) {
			drone.getIOManager().getCompassModule().startCalibration();
			startTime = System.currentTimeMillis();
			executing = true;
		}
		
		if(executing) {
			double percentage = (System.currentTimeMillis() - startTime)/(double)(timeIncrement*2*1000)*100;
			if((int)percentage % 5 == 0)
				System.out.println(percentage+"%");
			
			if(System.currentTimeMillis() - startTime < timeIncrement*1000) {
//				drone.setMotorSpeeds(speed, -speed);
			} else if(System.currentTimeMillis() - startTime < timeIncrement*2*1000) {
//				drone.setMotorSpeeds(-speed, speed);
			} else if(System.currentTimeMillis() - startTime >= timeIncrement*2*1000) {
//				drone.setMotorSpeeds(0, 0);
				drone.getIOManager().getCompassModule().endCalibration();
				executed = true;
				executing = false;
			}
		}
	}
	
	@Override
	public boolean getTerminateBehavior() {
		return executed;
	}
	
	@Override
	public void cleanUp() {
		if(executing)
			drone.getIOManager().getCompassModule().endCalibration();
		executed = false;
		executing = false;
		drone.setMotorSpeeds(0, 0);
	}
	
	
}