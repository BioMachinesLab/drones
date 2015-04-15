package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import net.jafama.FastMath;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.objects.Entity;
import commoninterface.objects.GeoEntity;
import commoninterface.objects.VirtualEntity;
import commoninterface.sensors.ConeTypeCISensor;
import commoninterface.sensors.ThymioConeTypeCISensor;
import commoninterface.utils.CoordinateUtilities;

public class CITwoDRenderer extends TwoDRenderer {
	
	private static final double ENTITY_DIAMETER = 1.08;
	
	private int droneID;
	private boolean seeSensors;
	private int coneSensorId;
	private String coneClass = "";
	
	public CITwoDRenderer(Arguments args) {
		super(args);
		
		droneID = args.getArgumentAsIntOrSetDefault("droneid", 0);
		seeSensors = args.getArgumentAsIntOrSetDefault("seesensors", 0) == 1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid",-1);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass","");
	}

	@Override
	protected void drawEntities(Graphics graphics, Robot robot) {
		RobotCI robotci = (RobotCI) robot;
		int circleDiameter = bigRobots ? (int)Math.max(10,Math.round(ENTITY_DIAMETER * scale)) : (int) Math.round(ENTITY_DIAMETER * scale);
		
		graphics.setColor(Color.GREEN.darker());
		if(robot.getId() == droneID){
			for (Entity entity : robotci.getEntities()) {
				if(entity instanceof GeoEntity){
					GeoEntity e = (GeoEntity)entity;
					Vector2d pos = CoordinateUtilities.GPSToCartesian(e.getLatLon());
					int x = (int) (transformX(pos.x) - circleDiameter / 2);
					int y = (int) (transformY(pos.y) - circleDiameter / 2);
					graphics.fillOval(x, y, circleDiameter, circleDiameter);
				}else if (entity instanceof VirtualEntity){
					VirtualEntity e = (VirtualEntity)entity;
					int x = (int) (transformX(e.getX()) - circleDiameter / 2);
					int y = (int) (transformY(e.getY()) - circleDiameter / 2);
					graphics.fillOval(x, y, circleDiameter, circleDiameter);
				}
			}
		}
	}
	
	@Override
	protected void drawCones(Graphics graphics, Robot robot){
		if(seeSensors){
			RobotCI robotCI = (RobotCI) robot;
			if(coneSensorId >= 0 || !coneClass.isEmpty()){
				for (CISensor ciSensor : robotCI.getCISensors()) {
					if(ciSensor.getClass().getSimpleName().equals(coneClass) || ciSensor.getId() == coneSensorId){
						if(ciSensor != null){
							if(ciSensor instanceof ConeTypeCISensor){
								ConeTypeCISensor coneCISensor = (ConeTypeCISensor)ciSensor;
								paintCones(graphics, robot, coneCISensor);
							}else if (ciSensor instanceof ThymioConeTypeCISensor){
								ThymioConeTypeCISensor coneCISensor = (ThymioConeTypeCISensor)ciSensor;
								paintCones(graphics, robot, coneCISensor);
							}
							
						}
					}
				}
				
				for(Sensor s : robot.getSensors()){		
					if(s.getClass().getSimpleName().equals(coneClass) || s.getId() == coneSensorId){
						if(s != null && s instanceof ConeTypeSensor){
							ConeTypeSensor coneSensor = (ConeTypeSensor)s;
							paintCones(graphics, robot, coneSensor);
						}
					}
				}
				
			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ConeTypeSensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length ; i++) {
			double angle = coneSensor.getAngles()[i];
		
			double xi = robot.getPosition().getX()+robot.getRadius()*FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()+robot.getRadius()*FastMath.sinQuick(angle + robot.getOrientation());
			
			double cutOff = coneSensor.getCutOff();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);
			
			int x3 = transformX(xi-cutOff);
			int y3 = transformY(yi+cutOff);
			
			int a1 = (int)(FastMath.round(FastMath.toDegrees(coneSensor.getSensorsOrientations()[i] + robot.getOrientation() - openingAngle/2)));
			
			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if(cutOff > 0){
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (cutOff*scale);
				float[] dist = {0.0f, 1.0f};
				Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY};
				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);
				
				graphics2D.fillArc(x3, y3, (int)FastMath.round(cutOff*2*scale), (int)(FastMath.round(cutOff*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}
	
	private void paintCones(Graphics graphics, Robot robot, ConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length ; i++) {
			double angle = coneSensor.getAngles()[i];
		
			double xi = robot.getPosition().getX()+robot.getRadius()*FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()+robot.getRadius()*FastMath.sinQuick(angle + robot.getOrientation());
			
			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);
			
			int x3 = transformX(xi-range);
			int y3 = transformY(yi+range);
			
			int a1 = (int)(FastMath.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle/2)));
			
			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if(range > 0){
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range*scale);
				float[] dist = {0.0f, 1.0f};
				Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY};
				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);
				
				graphics2D.fillArc(x3, y3, (int)FastMath.round(range*2*scale), (int)(FastMath.round(range*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}
	
	private void paintCones(Graphics graphics, Robot robot, ThymioConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length ; i++) {
			double angle = coneSensor.getAngles()[i];
		
			double xi = robot.getPosition().getX()+robot.getRadius()*FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()+robot.getRadius()*FastMath.sinQuick(angle + robot.getOrientation());
			
			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);
			
			int x3 = transformX(xi-range);
			int y3 = transformY(yi+range);
			
			int a1 = (int)(FastMath.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle/2)));
			
			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if(range > 0){
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range*scale);
				float[] dist = {0.0f, 1.0f};
				Color[] colors = {Color.DARK_GRAY, Color.LIGHT_GRAY};
				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);
				
				graphics2D.fillArc(x3, y3, (int)FastMath.round(range*2*scale), (int)(FastMath.round(range*2*scale)), a1, (int)FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}
	
}
