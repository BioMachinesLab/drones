package simulation.robot;

import java.util.ArrayList;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class CISensorWrapper extends Sensor{
	private static final long serialVersionUID = -8025272490973934599L;
	@ArgumentsAnnotation(name="ci", defaultValue="")
	private CISensor cisensor;
	
	public CISensorWrapper(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		String ciString = args.getArgumentValue("ci");
		
		CIArguments ciargs = new CIArguments(ciString);
		
		cisensor = CISensor.getSensor(((RobotCI)this.robot), ciargs.getArgumentAsString("classname"), ciargs);
		((RobotCI)this.robot).getCISensors().add(cisensor);
	}

    public CISensor getCisensor() {
        return cisensor;
    }
    
    public int getNumberOfSensors() {
    	return cisensor.getNumberOfSensors();
    }
        
	@Override
	public double getSensorReading(int sensorNumber) {
		return cisensor.getSensorReading(sensorNumber);
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		//We were having concurrency issues with the entities arraylist
		Object[] entities = ((RobotCI)this.robot).getEntities().toArray();
		
		cisensor.update(time, entities);
	}

}
