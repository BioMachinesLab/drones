package commoninterface.controllers;

import java.util.ArrayList;

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
	
	@Override
	public void cleanUp() {
		ArrayList<Waypoint> wps = Waypoint.getWaypoints(robot);
		
		if(wps.isEmpty())
			((AquaticDroneCI)robot).setActiveWaypoint(null);
		else
			((AquaticDroneCI)robot).setActiveWaypoint(wps.get(0));
	}
	
}