/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package environment;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class ForagingEnvironment extends OpenEnvironment {

    private int numItems = 5;
    private double spotDistance = 5;
    private double captureDistance = 3;
    private HashMap<Vector2d, LightPole> waypoints;
    private Simulator sim;
    private int count = 0;

    public ForagingEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        this.sim = simulator;
        this.numItems = args.getArgumentAsIntOrSetDefault("numitems", numItems);
        this.spotDistance = args.getArgumentAsDoubleOrSetDefault("spotdistance", spotDistance);
        this.captureDistance = args.getArgumentAsDoubleOrSetDefault("capturedistance", captureDistance);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);

        List<Vector2d> positions = new ArrayList<>(numItems);
        while (positions.size() < numItems) {
            double x = width * simulator.getRandom().nextDouble() - width / 2;
            double y = height * simulator.getRandom().nextDouble() - height / 2;
            Vector2d candidate = new Vector2d(x, y);
            boolean valid = true;
            for (Vector2d p : positions) {
                if (candidate.distanceTo(p) < spotDistance) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                positions.add(candidate);
            }
        }

        waypoints = new HashMap<>();
        int i = 0;
        for (Vector2d p : positions) {
            LatLon latLon = CoordinateUtilities.cartesianToGPS(p.x, p.y);
            Waypoint wp = new Waypoint("wp" + i, latLon);
            for (Robot r : simulator.getRobots()) {
                ((AquaticDroneCI) r).getEntities().add(wp);
            }
            LightPole lp = new LightPole(simulator, "wp" + i, p.x, p.y, captureDistance);
            addObject(lp);
            waypoints.put(p, lp);
            i++;
        }
    }
    
    public int getCaptureCount() {
        return count;
    }

    @Override
    public void update(double time) {
        super.update(time); 
        Iterator<Vector2d> iter = waypoints.keySet().iterator();
        while(iter.hasNext()) {
            Vector2d next = iter.next();
            int close = 0;
            for(Robot r : sim.getRobots()) {
                double d = r.getPosition().distanceTo(next);
                if(d <= captureDistance) {
                    close++;
                }
            }
            if(close >= 2) {
                count++;
                this.removeObject(waypoints.get(next));
                iter.remove();
                for(Robot r : sim.getRobots()) {
                    ((AquaticDroneCI) r).getEntities().remove(next);
                }
            }
        }        
    }
    
    public Collection<Vector2d> getWaypoints() {
        return waypoints.keySet();
    }
    
    public int getMaxNumItems() {
        return numItems;
    }
}
