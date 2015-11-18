/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpletestbehaviors;

import commoninterface.AquaticDroneCI;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;

public class GoForwardCIBehavior extends CIBehavior {
    
    private double speed = 1;
    
    public GoForwardCIBehavior(CIArguments args, RobotCI robot) {
        super(args, robot);
        this.speed = args.getArgumentAsDoubleOrSetDefault("speed",speed);
    }

    @Override
    public void step(double timestep) {
        AquaticDroneCI ad = (AquaticDroneCI) robot;
        ad.setRudder(0, speed);
    }
    
}
