/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package commoninterface.sensors;

import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CIArguments;

/**
 *
 * @author jorge
 */
public class WaypointConeCISensor extends ConeTypeCISensor {

    public WaypointConeCISensor(int id, RobotCI robot, CIArguments args) {
        super(id, robot, args);
    }

    @Override
    public boolean validEntity(Entity e) {
        return e instanceof Waypoint;
    }
    
}
