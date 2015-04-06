package network.messages;

import commoninterface.objects.Entity;

public class EntityMessage extends Message {
	
	private static final long serialVersionUID = 7355614915881468297L;
	private Entity entity;
	
	public EntityMessage(Entity e) {
		this.entity = e;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
