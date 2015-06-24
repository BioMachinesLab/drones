package behaviors;

import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.entities.RobotLocation;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealAquaticDroneCI;

public class LogDronesCIBehavior extends CIBehavior {
	
	private RealAquaticDroneCI drone;
	
	public LogDronesCIBehavior(CIArguments args, RobotCI drone) {
		super(args,drone);
		this.drone = (RealAquaticDroneCI)drone;
	}

	@Override
	public void step(double time) {
		for(RobotLocation r : RobotLocation.getDroneLocations(drone)) {
			drone.log(r.getLogMessage());
		}
	}
	
	public String getLogMessage(RobotLocation r) {
		String str = r.getLatLon().getLat()+" "+r.getLatLon().getLon();
		return r.getClass().getSimpleName()+" "+r.getName()+" "+str;
	}
	
}