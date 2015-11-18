/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.jcoord.LatLon;
import java.util.ArrayList;
import java.util.Random;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class PreyController extends Controller {

    private final double speed;
    private final double headingRandom;
    private final Random random;
    
    public PreyController(Simulator simulator, Robot robot, Arguments args, double speed, double headingRandom) {
        super(simulator, robot, args);
        this.random = simulator.getRandom();
        this.speed = speed;
        this.headingRandom = headingRandom;
    }

    @Override
    public void controlStep(double time) {
        AquaticDrone ad = (AquaticDrone) robot;
        ArrayList<Entity> entities = ad.getEntities();

        LatLon closest = null;
        double closestDist = Double.POSITIVE_INFINITY;
        for (Entity e : entities) {
            if (e instanceof RobotLocation) {
                RobotLocation rl = (RobotLocation) e;
                double d = ad.getGPSLatLon().distanceInKM(rl.getLatLon());
                if (d < closestDist) {
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
            diff = Math.min(1, Math.max(-1, diff + (random.nextDouble() * 2 - 1) * headingRandom));
            ad.setRudder(-diff, speed);
        }
    }
}
