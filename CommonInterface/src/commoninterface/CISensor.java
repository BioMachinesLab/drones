package commoninterface;

import java.io.Serializable;
import java.util.ArrayList;

import objects.Entity;

import commoninterface.utils.CIArguments;

public abstract class CISensor implements Serializable{
	
	protected int id = 0;
	protected AquaticDroneCI drone;
	
	public CISensor(int id, AquaticDroneCI drone, CIArguments args) {
		super();
		this.id = id;
		this.drone = drone;
	}
	
	public abstract double getSensorReading(int sensorNumber);
	
	public abstract void update(double time, ArrayList<Entity> entities);
	
	public static CISensor getSensor(AquaticDroneCI drone, String name, CIArguments arguments) {
		int id = arguments.getArgumentAsIntOrSetDefault("id",0);
		return (CISensor)CIFactory.getInstance(name, id, drone, arguments);
	}
	
	public int getId() {
		return id;
	}

}
