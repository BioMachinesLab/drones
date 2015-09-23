/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package environment;

import commoninterface.AquaticDroneCI;
import controllers.PreyController;
import simulation.Simulator;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class PredatorPreyEnvironment extends OpenEnvironment {

    protected double minPreyDistance;
    protected double maxPreyDistance;
    protected double minPreySpeed = 0.5;
    protected double maxPreySpeed = 1;
    protected double escapeDistance;
    protected double headingRandom = 0;
    protected AquaticDrone preyDrone;

    public PredatorPreyEnvironment(Simulator simulator, Arguments args) {
        super(simulator, args);
        minPreyDistance = args.getArgumentAsDouble("minpreydistance");
        maxPreyDistance = args.getArgumentAsDouble("maxpreydistance");
        escapeDistance = args.getArgumentAsDouble("escapedistance");
        minPreySpeed = args.getArgumentAsDoubleOrSetDefault("minpreyspeed", minPreySpeed);
        maxPreySpeed = args.getArgumentAsDoubleOrSetDefault("maxpreyspeed", maxPreySpeed);
        headingRandom = args.getArgumentAsDoubleOrSetDefault("headingrandom", headingRandom);
    }

    @Override
    public void setup(Simulator simulator) {
        super.setup(simulator);

        preyDrone = new AquaticDrone(simulator, new Arguments("commrange=" + escapeDistance + ",radius=0.5,diameter=1,maxspeed=0.3,gpserror=1.8,avoiddrones=0,rudder=1,distancewheels=0.2,compassoffset=0,compasserror=10,headingoffset=0.05,speedoffset=0.1"));
        preyDrone.setDroneType(AquaticDroneCI.DroneType.ENEMY);
        double speed = simulator.getRandom().nextDouble() * (maxPreySpeed - minPreySpeed) + minPreySpeed;
        PreyController controller = new PreyController(simulator, preyDrone, new Arguments(""), speed, headingRandom);
        preyDrone.setController(controller);

        double r = minPreyDistance + simulator.getRandom().nextDouble() * (maxPreyDistance - minPreyDistance);
        double a = simulator.getRandom().nextDouble() * Math.PI * 2;
        double x = Math.cos(a) * r;
        double y = Math.sin(a) * r;
        preyDrone.setPosition(x, y);
        preyDrone.setOrientation(simulator.getRandom().nextDouble() * Math.PI * 2);
        addRobot(preyDrone);
    }
    
    public AquaticDrone getPreyDrone() {
        return preyDrone;
    }
    
}
