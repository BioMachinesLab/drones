package commoninterface.network.messages;

import commoninterface.entities.Entity;
import commoninterface.network.messages.Message;

public class EntityMessage extends Message {
	
	private static final long serialVersionUID = 7355614915881468297L;
	private Entity entity;
	
	public EntityMessage(Entity e, String senderHostname) {
		super(senderHostname);
		this.entity = e;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	@Override
	public Message getCopy() {
		return new EntityMessage(entity, senderHostname);
	}

}
