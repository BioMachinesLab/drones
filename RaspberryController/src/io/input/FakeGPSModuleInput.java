package io.input;

import commoninterface.dataobjects.MotorSpeeds;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.MotorMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import commoninterfaceimpl.RealAquaticDroneCI;

public class FakeGPSModuleInput extends GPSModuleInput {

	private static final long CYCLE_SLEEP = 100;// 100 ms
	private LatLon currentCoordinates = CoordinateUtilities
			.cartesianToGPS(new Vector2d(0, 0));
	private RealAquaticDroneCI drone;

	public FakeGPSModuleInput(RealAquaticDroneCI drone) {
		super(true);
		this.drone = drone;
		FakeInputThread t = new FakeInputThread();
		t.start();
		updateData(currentCoordinates);
		available = true;
	}

	private void updateData(LatLon coord) {
		currentCoordinates = coord;
		gpsData.setFix(true);
		gpsData.setLatitudeDecimal(currentCoordinates.getLat());
		gpsData.setLongitudeDecimal(currentCoordinates.getLon());
	}

	class FakeInputThread extends Thread {

		@Override
		public void run() {
			while (true) {

				MotorSpeeds spd = drone.getIOManager().getMotorSpeeds();

				MotorMessage msg = spd.getNonBlockingSpeeds();

				if (msg != null) {

					double left = msg.getLeftMotor();
					double right = msg.getRightMotor();

					double totalSpeed = Math.abs(left + right) / 2;

					if (left + right < 0)
						totalSpeed *= -1;

					// [0;1] to cm/s
					// 10km/h = 270cm/s = 27.0cm/100ms
					totalSpeed *= 0.27 / 10;// 2.5km/h

					// +90 because North is up, but cartesian math has the
					// reference to the right
					double orientation = Math.toRadians(drone
							.getCompassOrientationInDegrees() - 90) * -1;
					Vector2d cartesian = CoordinateUtilities
							.GPSToCartesian(currentCoordinates);

					double x = cartesian.getX();
					double y = cartesian.getY();
					x += Math.cos(orientation) * totalSpeed;
					y += Math.sin(orientation) * totalSpeed;

					updateData(CoordinateUtilities.cartesianToGPS(x, y));

					try {
						Thread.sleep(CYCLE_SLEEP);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
