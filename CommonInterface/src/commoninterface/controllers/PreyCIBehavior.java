/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commoninterface.controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.jcoord.LatLon;
import java.util.ArrayList;

/**
 *
 * @author jorge
 */
public class PreyCIBehavior extends CIBehavior {
    
    private double speed = 1;
    private double headingRandom = 0;
    private double escapeRange = 10;
    
    public PreyCIBehavior(CIArguments args, RobotCI robot) {
        super(args, robot);
        this.speed = args.getArgumentAsDouble("speed");
        this.headingRandom = args.getArgumentAsDouble("headingrandom");
        this.escapeRange = args.getArgumentAsDouble("escaperange");
    }

    @Override
    public void step(double timestep) {
        AquaticDroneCI ad = (AquaticDroneCI) robot;
        ArrayList<Entity> entities = ad.getEntities();

        LatLon closest = null;
        double closestDist = Double.POSITIVE_INFINITY;
        for (Entity e : entities) {
            if (e instanceof RobotLocation) {
                RobotLocation rl = (RobotLocation) e;
                double d = ad.getGPSLatLon().distanceInKM(rl.getLatLon());
                if (d < closestDist && d * 1000.0 <= escapeRange) {
                    closest = rl.getLatLon();
                    closestDist = d;
                }
            }
        }
        
        if (closest == null) {
            ad.setRudder(0, 0);
        } else {
            double currentOrientation = ad.getCompassOrientationInDegrees();
            double radAngle = currentOrientation * Math.PI / 180;

            Vector2d prey = new Vector2d(ad.getGPSLatLon().getLat(), ad.getGPSLatLon().getLon());
            Vector2d pred = new Vector2d(closest.getLat(), closest.getLon());
            Vector2d predToPrey = new Vector2d(prey);
            predToPrey.sub(pred);
            double predAngle = predToPrey.getAngle();

            double diff = radAngle - predAngle;
            if (diff > Math.PI) {
                diff -= Math.PI * 2;
            } else if (diff < -Math.PI) {
                diff += Math.PI * 2;
            }
            
            diff /= Math.PI; // [-1,1]
            diff = Math.min(1, Math.max(-1, diff + (Math.random() * 2 - 1) * headingRandom));
            ad.setRudder(-diff, speed);
        }
    }
    
}
