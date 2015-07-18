package commoninterface.network.messages;

import java.util.ArrayList;

import commoninterface.entities.Entity;

public class EntitiesMessage extends Message {

	private static final long serialVersionUID = 7355614915881468297L;
	private ArrayList<Entity> entities;
	private int activeId = 0;

	public EntitiesMessage(ArrayList<Entity> entities, String senderHostname) {
		super(senderHostname);
		this.entities = entities;
	}
	
	public EntitiesMessage(ArrayList<Entity> entities, String senderHostname, int activeId) {
		this(entities, senderHostname);
		this.activeId = activeId;
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}
	
	public void setActiveId(int activeId) {
		this.activeId = activeId;
	}
	
	public int getActiveId() {
		return activeId;
	}
	
	@Override
	public Message getCopy() {
		return new EntitiesMessage(entities, getSenderHostname());
	}
	
	@Override
	public String toString() {
		String s = this.getClass().getSimpleName()+";";
		for(Entity e : entities)
			s+=e.toString()+";";
		return s;
	}
}