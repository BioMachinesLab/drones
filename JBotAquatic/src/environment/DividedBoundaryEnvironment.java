package environment;

import commoninterface.entities.GeoFence;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

public class DividedBoundaryEnvironment extends GridBoundaryEnvironment {
	private static final long serialVersionUID = 3876700620605579774L;

	public DividedBoundaryEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);

		double w = fence.getWaypoints().get(0).getLatLon().distanceInMeters(fence.getWaypoints().get(2).getLatLon());
		w /= robots.size();

		Vector2d start = CoordinateUtilities.GPSToCartesian(fence.getWaypoints().get(0).getLatLon());
		Vector2d end = CoordinateUtilities.GPSToCartesian(fence.getWaypoints().get(2).getLatLon());

		for (int i = 0; i < robots.size(); i++) {

			GeoFence fence = new GeoFence("fence" + i);

			Vector2d wp1 = new Vector2d(start.x + i * w, start.y);
			Vector2d wp2 = new Vector2d(end.x + i * w, end.y);

			Vector2d wp3 = new Vector2d(end.x + (i + 1) * w, end.y);
			Vector2d wp4 = new Vector2d(start.x + (i + 1) * w, start.y);

			fence.addWaypoint(CoordinateUtilities.cartesianToGPS(wp1));
			fence.addWaypoint(CoordinateUtilities.cartesianToGPS(wp2));
			fence.addWaypoint(CoordinateUtilities.cartesianToGPS(wp3));
			fence.addWaypoint(CoordinateUtilities.cartesianToGPS(wp4));

			((AquaticDrone) robots.get(i)).getEntities().clear();
			((AquaticDrone) robots.get(i)).getEntities().add(fence);
			robots.get(i).setPosition(wp1.x, wp2.y);
			// ((AquaticDrone)robots.get(i)).set
		}

	}

	@Override
	public void update(double time) {

	}

}