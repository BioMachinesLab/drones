package simulation.robot.sensor;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.GeoFence;
import commoninterface.neat.utils.MathUtils;
import mathutils.Vector2d;
import environment.GridBoundaryEnvironment;
import environment.utils.EnvironmentGrid;
import evolutionaryrobotics.evolution.neat.NEATNeuralNetwork;
import gui.renderer.TwoDRenderer;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.AquaticDrone;
import simulation.robot.CISensorWrapper;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class GridSensor extends ConeTypeSensor{
	
	protected EnvironmentGrid grid;
	protected Simulator sim;
	
	public GridSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.sim = simulator;
	}
	
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		if(grid == null)
			grid = ((GridBoundaryEnvironment)sim.getEnvironment()).getGrid(robot);

		try { 
			for(int i = 0; i < readings.length; i++)
				readings[i] = 0.0;
			
			double startX = robot.getPosition().x - range;
			double startY = robot.getPosition().y - range;
			
			double endX = robot.getPosition().x + range;
			double endY = robot.getPosition().y + range;
			
			Vector2d pos = new Vector2d(0,0);
			
			for(int i = 0 ; i < numberOfSensors ; i++) {
				
				double count = 0;
				double value = 0;
				
				for(double y = startY ; y < endY ; y+=grid.getResolution()) {
					for(double x = startX ; x < endX ; x+=grid.getResolution()) {
						
						pos.set(x, y);
						
						GeometricInfo sensorInfo = getSensorGeometricInfo(i, pos);
						
						if((sensorInfo.getDistance() < getCutOff()) && 
						   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
						   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
							
							double currValue = grid.getGridValue(pos.x, pos.y);
							
							if(currValue >= 0) {
							
								if(grid.getDecay() == 0) {
									value+= currValue > 0 ? 1 : 0;
								} else {
									if(!(currValue == 0 || sim.getTime() - grid.getDecay() > currValue)) {
										value+= 1;
									}
								}
								
								count++;
							} else if(currValue == -1) {
								value+=1;
								count++;
							}
						}
					}
				}
				
				readings[i] = value/count;
				if(Double.isNaN(readings[i]) || readings[i] < 0 || readings[i] > 1) {
					readings[i] = 1;
				}
				
				readings[i] = 1 - readings[i];
				
//				if(robot.getId() == 0) {
//					System.out.print(readings[i]+"\t");
//				}
			}
//			if(robot.getId() == 0) 
//				System.out.println();

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	public void paint(Graphics g, TwoDRenderer r) {
		
		if(robot.getId() != 0 || grid == null)
			return;
		
		int grX = r.transformX(robot.getPosition().x);
		int grY = r.transformY(robot.getPosition().y);
		
		double startX = robot.getPosition().x - range;
		double startY = robot.getPosition().y - range;
		
		double endX = robot.getPosition().x + range;
		double endY = robot.getPosition().y + range;
		
		Vector2d pos = new Vector2d(0,0);
		
		for(int i = 0 ; i < numberOfSensors ; i++) {
			
			double orientation = angles[i]+robot.getOrientation() - openingAngle/2;
			
			double x = robot.getPosition().x+Math.cos(orientation)*getRange();
			double y = robot.getPosition().y+Math.sin(orientation)*getRange();
			
			g.setColor(Color.RED);
			g.drawLine(grX, grY, r.transformX(x), r.transformY(y));
			
			for(double yy = startY ; yy < endY ; yy+=grid.getResolution()) {
				for(double xx = startX ; xx < endX ; xx+=grid.getResolution()) {
					
					pos.set(xx, yy);
					GeometricInfo sensorInfo = getSensorGeometricInfo(i, pos);
					
					double angle = sensorInfo.getAngle();
					
					if((sensorInfo.getDistance() < getCutOff()) && 
					   (angle < (openingAngle / 2.0)) && 
					   (angle > (-openingAngle / 2.0))) {
						
						double currValue = grid.getGridValue(pos.x, pos.y);
						
						if(currValue >= 0) {
							
							g.setColor(Color.RED);
						
							if(grid.getDecay() == 0) {
								
								if(currValue == 0) {
									g.drawRect(r.transformX(pos.x), r.transformY(pos.y), (int)(grid.getResolution()*r.getScale()), (int)(grid.getResolution()*r.getScale()));
								}
								
							} else {
								if(currValue == 0 || sim.getTime() - grid.getDecay() > currValue) {
									g.drawRect(r.transformX(pos.x), r.transformY(pos.y), (int)(grid.getResolution()*r.getScale()), (int)(grid.getResolution()*r.getScale()));
								}
							}
						} else if(currValue == -1) {
							g.setColor(Color.blue);
							g.drawRect(r.transformX(pos.x), r.transformY(pos.y), (int)(grid.getResolution()*r.getScale()), (int)(grid.getResolution()*r.getScale()));
						}
					}
				}
			}
		}
	}
	
	@Override
	protected double calculateContributionToSensor(int i,PhysicalObjectDistance source) {
		return 0;
	}
}