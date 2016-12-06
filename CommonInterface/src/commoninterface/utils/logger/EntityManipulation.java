package commoninterface.utils.logger;

import java.util.ArrayList;

import commoninterface.entities.Entity;

public class EntityManipulation {

	public static enum Operation {
		ADD, REMOVE, MOVE
	}

	private ArrayList<Entity> entities;
	private Operation operation;
	private String entitiesClass;
	private double timestep;

	public EntityManipulation(Operation op, ArrayList<Entity> entities, String entitiesClass) {
		this(op, entities, entitiesClass, -1);
	}

	public EntityManipulation(Operation op, ArrayList<Entity> entities, String entitiesClass, double timestep) {
		this.entities = entities;
		this.operation = op;
		this.entitiesClass = entitiesClass;
		this.timestep = timestep;
	}

	public void setTimestep(double timestep) {
		this.timestep = timestep;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Operation getOperation() {
		return operation;
	}

	public String getEntitiesClass() {
		return entitiesClass;
	}

	public double getTimestep() {
		return timestep;
	}
}