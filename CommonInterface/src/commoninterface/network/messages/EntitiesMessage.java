package commoninterface.network.messages;

import java.util.ArrayList;

import commoninterface.entities.Entity;

public class EntitiesMessage extends Message {

	private static final long serialVersionUID = 7355614915881468297L;
	private ArrayList<Entity> entities;

	public EntitiesMessage(ArrayList<Entity> entities, String senderHostname) {
		super(senderHostname);
		this.entities = entities;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}
}