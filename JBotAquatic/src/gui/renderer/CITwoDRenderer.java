package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.SharedDroneLocation;
import commoninterface.entities.VirtualEntity;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.sensors.ConeTypeCISensor;
import commoninterface.sensors.ThymioConeTypeCISensor;
import commoninterface.utils.CoordinateUtilities;
import environment.GridBoundaryEnvironment;
import environment.VoronoiEnvironment;
import environment.utils.EnvironmentGrid;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;
import net.jafama.FastMath;
import simulation.physicalobjects.Line;
import simulation.robot.Robot;
import simulation.robot.sensor.GridSensor;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class CITwoDRenderer extends TwoDRenderer {

	private static final double ENTITY_DIAMETER = 1.08;

	protected int droneID;
	protected boolean seeSensors;
	protected boolean seeEntities;
	protected int coneSensorId;
	protected double coneTransparence = .1;
	protected String coneClass = "";
	protected Color[] lineColors = new Color[] { Color.RED, Color.BLUE, Color.GREEN };
	protected int colorIndex = 0;

	public CITwoDRenderer(Arguments args) {
		super(args);

		droneID = args.getArgumentAsIntOrSetDefault("droneid", 0);
		seeSensors = args.getArgumentAsIntOrSetDefault("seesensors", 0) == 1;
		seeEntities = args.getArgumentAsIntOrSetDefault("seeentities", 1) == 1;
		coneSensorId = args.getArgumentAsIntOrSetDefault("conesensorid", -1);
		coneTransparence = args.getArgumentAsDoubleOrSetDefault("coneTransparence", coneTransparence);
		coneClass = args.getArgumentAsStringOrSetDefault("coneclass", "");
	}

	@Override
	public synchronized void drawFrame() {
		super.drawFrame();

		drawEnvironment();

	}

	protected void drawEnvironment() {

		if (simulator.getEnvironment() instanceof VoronoiEnvironment) {
			VoronoiEnvironment env = (VoronoiEnvironment) simulator.getEnvironment();

			// for each site we can no get the resulting polygon of its cell.
			// note that the cell can also be empty, in this case there is no
			// polygon for the corresponding site.

			if (env.getSites() != null) {

				Graphics2D g = (Graphics2D) graphics;

				for (Site s : env.getSites()) {

					PolygonSimple polygon = s.getPolygon();

					if (polygon != null) {
						double[] x = new double[polygon.length];
						double[] y = new double[polygon.length];

						for (int i = 0; i < polygon.length; i++) {
							x[i] = transformX(polygon.getXPoints()[i]);
							y[i] = transformY(polygon.getYPoints()[i]);
						}

						PolygonSimple translated = new PolygonSimple(x, y);
						g.draw(translated);
					}
				}
			}
		}

		if (simulator.getEnvironment() instanceof GridBoundaryEnvironment) {

			GridBoundaryEnvironment env = (GridBoundaryEnvironment) simulator.getEnvironment();

			EnvironmentGrid first = env.getGrids().get(0);

			double[][] firstGrid = first.getGrid();
			double[][] drawGrid = new double[firstGrid.length][firstGrid[0].length];

			for (EnvironmentGrid g : env.getGrids()) {

				double[][] grid = g.getGrid();

				for (int y = 0; y < grid.length; y++) {
					for (int x = 0; x < grid[y].length; x++) {
						drawGrid[y][x] = Math.max(drawGrid[y][x], grid[y][x]);
					}
				}
			}

			for (int y = 0; y < drawGrid.length; y++) {
				for (int x = 0; x < drawGrid[y].length; x++) {

					mathutils.Vector2d pos = first.getCartesianPosition(x, y);

					int w = (int) (first.getResolution() * scale);

					graphics.setColor(drawGrid[y][x] > 0 ? Color.LIGHT_GRAY : Color.white);

					if (first.getDecay() == 0)
						graphics.drawRect(transformX(pos.x), transformY(pos.y) - w, w, w);
					else if (drawGrid[y][x] > 0 && simulator.getTime() - first.getDecay() < drawGrid[y][x])
						graphics.drawRect(transformX(pos.x), transformY(pos.y) - w, w, w);
				}
			}

			for (Robot r : simulator.getRobots()) {
				Sensor s = r.getSensorByType(GridSensor.class);
				if (s != null) {
					GridSensor gs = (GridSensor) s;
					gs.paint(graphics, this);
				}
			}
		}
	}

	@Override
	protected void drawEntities(Graphics graphics, Robot robot) {
		if (seeEntities) {
			RobotCI robotci = (RobotCI) robot;
			int circleDiameter = bigRobots ? (int) Math.max(10, Math.round(ENTITY_DIAMETER * scale))
					: (int) Math.round(ENTITY_DIAMETER * scale);

			if (robot.getId() == 0)
				colorIndex = 0;

			// if(robot.getId() == droneID){

			// to prevent ConcurrentModificationExceptions
			Object[] entities = robotci.getEntities().toArray();

			for (Object o : entities) {
				Entity entity = (Entity) o;
				if (entity instanceof GeoEntity) {

					if (entity instanceof SharedDroneLocation)
						graphics.setColor(Color.BLUE.darker());
					else if (entity instanceof RobotLocation)
						graphics.setColor(Color.GREEN.darker());
					else
						graphics.setColor(Color.yellow.darker());

					GeoEntity e = (GeoEntity) entity;
					Vector2d pos = CoordinateUtilities.GPSToCartesian(e.getLatLon());
					int x = transformX(pos.x) - circleDiameter / 2;
					int y = transformY(pos.y) - circleDiameter / 2;
					graphics.fillOval(x, y, circleDiameter, circleDiameter);
				} else if (entity instanceof VirtualEntity) {
					graphics.setColor(Color.GREEN.darker());
					VirtualEntity e = (VirtualEntity) entity;
					int x = transformX(e.getX()) - circleDiameter / 2;
					int y = transformY(e.getY()) - circleDiameter / 2;
					graphics.fillOval(x, y, circleDiameter, circleDiameter);
				} else if (entity instanceof GeoFence) {
					drawGeoFence((GeoFence) entity, lineColors[colorIndex % lineColors.length]);
					colorIndex++;
				}
			}
			// }
		}
	}

	protected void drawGeoFence(GeoFence geo, Color c) {
		LinkedList<Waypoint> waypoints = geo.getWaypoints();

		for (int i = 1; i < waypoints.size(); i++) {

			Waypoint wa = waypoints.get(i - 1);
			Waypoint wb = waypoints.get(i);
			Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
			Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

			Line l = new Line(simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
			l.setColor(c);
			drawLine(l);
		}

		Waypoint wa = waypoints.get(waypoints.size() - 1);
		Waypoint wb = waypoints.get(0);
		Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
		Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

		Line l = new Line(simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
		l.setColor(c);
		drawLine(l);
	}

	@Override
	protected void drawCones(Graphics graphics, Robot robot) {
		if (seeSensors) {
			RobotCI robotCI = (RobotCI) robot;
			if (coneSensorId >= 0 || !coneClass.isEmpty()) {
				for (CISensor ciSensor : robotCI.getCISensors()) {
					if (ciSensor.getClass().getSimpleName().equals(coneClass) || ciSensor.getId() == coneSensorId) {
						if (ciSensor != null) {
							if (ciSensor instanceof ConeTypeCISensor) {
								ConeTypeCISensor coneCISensor = (ConeTypeCISensor) ciSensor;
								paintCones(graphics, robot, coneCISensor);
							} else if (ciSensor instanceof ThymioConeTypeCISensor) {
								ThymioConeTypeCISensor coneCISensor = (ThymioConeTypeCISensor) ciSensor;
								paintCones(graphics, robot, coneCISensor);
							}

						}
					}
				}

				for (Sensor s : robot.getSensors()) {
					if (s.getClass().getSimpleName().equals(coneClass) || s.getId() == coneSensorId) {
						if (s != null && s instanceof ConeTypeSensor) {
							ConeTypeSensor coneSensor = (ConeTypeSensor) s;
							paintCones(graphics, robot, coneSensor);
						}
					}
				}

			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ConeTypeSensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double cutOff = coneSensor.getCutOff();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - cutOff);
			int y3 = transformY(yi + cutOff);

			int a1 = (int) (FastMath.round(FastMath
					.toDegrees(coneSensor.getSensorsOrientations()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (cutOff > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (cutOff * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(cutOff * 2 * scale),
						(int) (FastMath.round(cutOff * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - range);
			int y3 = transformY(yi + range);

			int a1 = (int) (FastMath
					.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (range > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(range * 2 * scale),
						(int) (FastMath.round(range * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

	private void paintCones(Graphics graphics, Robot robot, ThymioConeTypeCISensor coneSensor) {
		for (int i = 0; i < coneSensor.getAngles().length; i++) {
			double angle = coneSensor.getAngles()[i];

			double xi = robot.getPosition().getX()
					+ robot.getRadius() * FastMath.cosQuick(angle + robot.getOrientation());
			double yi = robot.getPosition().getY()
					+ robot.getRadius() * FastMath.sinQuick(angle + robot.getOrientation());

			double range = coneSensor.getRange();
			double openingAngle = coneSensor.getOpeningAngle();

			int x1 = transformX(xi);
			int y1 = transformY(yi);

			int x3 = transformX(xi - range);
			int y3 = transformY(yi + range);

			int a1 = (int) (FastMath
					.round(FastMath.toDegrees(coneSensor.getAngles()[i] + robot.getOrientation() - openingAngle / 2)));

			Graphics2D graphics2D = (Graphics2D) graphics.create();

			if (range > 0) {
				Point2D p = new Point2D.Double(x1, y1);
				float radius = (float) (range * scale);
				float[] dist = { 0.0f, 1.0f };

				Color dark_grey = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(),
						Color.DARK_GRAY.getBlue(), (int) (coneTransparence * 100));
				Color light_grey = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),
						Color.LIGHT_GRAY.getBlue(), (int) (coneTransparence * 100));

				// Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY };
				Color[] colors = { dark_grey, light_grey };

				RadialGradientPaint rgp = new RadialGradientPaint(p, radius, dist, colors);
				graphics2D.setPaint(rgp);

				graphics2D.fillArc(x3, y3, (int) FastMath.round(range * 2 * scale),
						(int) (FastMath.round(range * 2 * scale)), a1,
						(int) FastMath.round(FastMath.toDegrees(openingAngle)));
			}
		}
	}

}
