/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commoninterface.controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.Line;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author jorge
 */
public class SafeControllerCIBehavior extends ControllerCIBehavior {

    private ArrayList<Line> lines = new ArrayList<>();
    private GeoFence fence;

    public SafeControllerCIBehavior(CIArguments args, RobotCI robot) {
        super(args, robot);
    }

    @Override
    public void step(double timestep) {
        ArrayList<Entity> entities = super.robot.getEntities();
        updateLines(timestep, entities.toArray());
        if (lines.isEmpty() || insideBoundary()) { // is inside -- do as usual
            super.step(timestep);
        } else { // is outside -- stop
            AquaticDroneCI drone = (AquaticDroneCI) super.robot;
            drone.setMotorSpeeds(0, 0);
        }
    }

    /**
     * CODE COPY-PASTE FROM INSIDEBOUNDARYCISENSOR
     */
    private void updateLines(double time, Object[] entities) {

        GeoFence newFence = null;

        for (Object e : entities) {
            if (e instanceof GeoFence) {
                newFence = (GeoFence) e;
                break;
            }
        }

        if (newFence == null) {
            lines.clear();
        } else {

            if (this.fence != null && newFence.getTimestepReceived() == this.fence.getTimestepReceived()) {
                return;
            }

            lines.clear();

            this.fence = newFence;

            LinkedList<Waypoint> waypoints = newFence.getWaypoints();

            //force this every 100 seconds just to be on the safe side
            if (waypoints.size() != lines.size() || (time % 1000) == 0) {

                lines.clear();

                for (int i = 1; i < waypoints.size(); i++) {

                    Waypoint wa = waypoints.get(i - 1);
                    Waypoint wb = waypoints.get(i);

                    addLine(wa, wb);
                }

                //loop around
                Waypoint wa = waypoints.get(waypoints.size() - 1);
                Waypoint wb = waypoints.get(0);

                addLine(wa, wb);
            }
        }
    }

    private void addLine(Waypoint wa, Waypoint wb) {
        Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
        Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

        Line l = new Line(va.getX(), va.getY(), vb.getX(), vb.getY());
        lines.add(l);
    }

    private boolean insideBoundary() {
        int count = 0;
        AquaticDroneCI drone = (AquaticDroneCI) super.robot;
        if (drone.getGPSLatLon() != null) {
            Vector2d dronePosition = CoordinateUtilities.GPSToCartesian(drone.getGPSLatLon());

            for (Line l : lines) {
                if (l.intersectsWithLineSegment(dronePosition, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
                    count++;
                }
            }
            return count % 2 != 0;
        }
        return false;
    }

}
