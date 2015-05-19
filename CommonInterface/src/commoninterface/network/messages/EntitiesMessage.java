package commoninterface.network.messages;

import java.util.LinkedList;

import commoninterface.entities.Entity;
import commoninterface.network.messages.Message;

public class EntitiesMessage extends Message {
	
	private static final long serialVersionUID = 7355614915881468297L;
	private LinkedList<Entity> entities;
	
	public EntitiesMessage(LinkedList<Entity> entities) {
		this.entities = entities;
	}
	
	public LinkedList<Entity> getEntities() {
		return entities;
	}
}