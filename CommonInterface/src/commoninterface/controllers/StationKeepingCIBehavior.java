package commoninterface.controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;

public class StationKeepingCIBehavior extends ControllerCIBehavior {
	
	private Waypoint wp;
	
	public StationKeepingCIBehavior(CIArguments args, RobotCI robot) {
		super(args, robot);
	}
	
	@Override
	public void start() {
		super.start();
		wp = new Waypoint("station_keeping", ((AquaticDroneCI)robot).getGPSLatLon());
	}
	
	@Override
	public void step(double timestep) {
		((AquaticDroneCI)robot).setActiveWaypoint(wp);
		super.step(timestep);
	}
	
}