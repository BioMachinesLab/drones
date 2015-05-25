package commoninterface.network.messages;

import java.util.ArrayList;
import java.util.LinkedList;

import commoninterface.entities.Entity;
import commoninterface.network.messages.Message;

public class EntitiesMessage extends Message {
	
	private static final long serialVersionUID = 7355614915881468297L;
	private ArrayList<Entity> entities;
	
	public EntitiesMessage(ArrayList<Entity> entities) {
		this.entities = entities;
	}
	
	public ArrayList<Entity> getEntities() {
		return entities;
	}
}