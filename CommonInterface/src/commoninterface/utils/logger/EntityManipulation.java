package commoninterface.utils.logger;

import java.util.ArrayList;
import commoninterface.entities.Entity;

public class EntityManipulation {

	public static enum Operation {
		ADD, REMOVE
	}

	ArrayList<Entity> entities;
	Operation op;
	String entitiesClass;

	public EntityManipulation(Operation op, ArrayList<Entity> entities,
			String entitiesClass) {
		this.entities = entities;
		this.op = op;
		this.entitiesClass = entitiesClass;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Operation operation() {
		return op;
	}

	public String getEntitiesClass() {
		return entitiesClass;
	}
}