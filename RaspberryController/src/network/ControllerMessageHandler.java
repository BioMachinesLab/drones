package network;

import network.messages.BehaviorMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;
import network.messages.WaypointMessage;
import objects.Waypoint;

import commoninterface.CIBehavior;
import commoninterfaceimpl.RealAquaticDroneCI;

public class ControllerMessageHandler extends MessageHandler {
	
	private RealAquaticDroneCI drone;
	
	public ControllerMessageHandler(RealAquaticDroneCI c) {
		this.drone = c;
	}
	
	@Override
	protected void processMessage(Message m, ConnectionHandler c) {
		Message request = pendingMessages[currentIndex];
		Message response = null;
		
		for (MessageProvider p : drone.getMessageProviders()) {
			response = p.getMessage(request);
			if (response != null)
				break;
		}
		
		if(response == null && m instanceof WaypointMessage) {
			WaypointMessage wm = (WaypointMessage)m;
			Waypoint p = wm.getWaypoint();
			drone.getWaypoints().clear();
			drone.getWaypoints().add(p);
			response = m;
		}
		
		if(response == null && m instanceof BehaviorMessage) {
			BehaviorMessage bm = (BehaviorMessage)m;
			
			for(CIBehavior b : drone.getBehaviors()) {
				if(bm.getSelectedBehavior().equals(b.getClass())) {
                    //send the same message back as an acknowledgement
                    response = request;
                    if(bm.changeStatusOrder()) {
                        if(bm.getSelectedStatus()) {
                        	drone.executeBehavior(b,true);
                        } else {
                        	drone.executeBehavior(b,false);
                        }
                    } else if(bm.changeArgumentOrder()) {
//                    	b.setArgument(bm.getArgumentIndex(), bm.getArgumentValue());
                    }       
				}
			}
		}
		
		if (response == null) {
			
			String sResponse = "No message provider for the current request (";
			
			if(m instanceof InformationRequest) {
				InformationRequest ir = (InformationRequest)m;
				sResponse+=ir.getMessageTypeQuery() + ")";
			} else {
				sResponse+=request.getClass().getSimpleName() + ")";
			}
			
			response = new SystemStatusMessage(sResponse);
		}

		pendingConnections[currentIndex].sendData(response);
	}
}