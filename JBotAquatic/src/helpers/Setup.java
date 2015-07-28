package helpers;

import gui.renderer.Renderer;

import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

class Setup {
	
	public Renderer renderer;
	public Simulator sim;
	public ArrayList<Robot> robots = new ArrayList<Robot>();
	
	public void setupDrones(HashMap<Integer,Integer> robotList, Arguments args, Vector2d start) {
		for(int i = 0 ; i < robotList.keySet().size() ; i++) {
			AquaticDrone drone = new AquaticDrone(sim, args);
			drone.setPosition(start.x,start.y);
			robots.add(drone);
			robotList.put((Integer)robotList.keySet().toArray()[i], robots.size()-1);
		}
	}
	
}