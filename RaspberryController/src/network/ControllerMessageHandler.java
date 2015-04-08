package network;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import network.messages.BehaviorMessage;
import network.messages.EntitiesMessage;
import network.messages.EntityMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.NeuralActivationsMessage;
import network.messages.SystemStatusMessage;
import simpletestbehaviors.ControllerCIBehavior;
import commoninterface.CIBehavior;
import commoninterface.RealRobotCI;
import commoninterface.RobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.MessageHandler;
import commoninterface.objects.Entity;
import commoninterface.objects.GeoFence;
import commoninterface.objects.Waypoint;
import commoninterface.utils.CIArguments;
import commoninterface.utils.ClassLoadHelper;

public class ControllerMessageHandler extends MessageHandler {
	
	private RealRobotCI drone;
	
	public ControllerMessageHandler(RealRobotCI c) {
		this.drone = c;
	}
	
	@Override
	protected void processMessage(Message m, ConnectionHandler c) {
		Message request = pendingMessages[currentIndex];
		Message response = null;
		
		if(request instanceof InformationRequest && ((InformationRequest)request).getMessageTypeQuery().equals(InformationRequest.MessageType.NEURAL_ACTIVATIONS)){
			if (drone.getActiveBehavior() instanceof ControllerCIBehavior)
				response = createNeuralActivationMessage();
			
		}else{
			
			for (MessageProvider p : drone.getMessageProviders()) {
			response = p.getMessage(request);
			if (response != null)
				break;
			}
		
			if(response == null && m instanceof EntityMessage) {
				response = handleEntityMessage(m);
			}
		
			if(response == null && m instanceof EntitiesMessage) {
				response = handleEntitiesMessage(m);
			}
		
			if(response == null && m instanceof BehaviorMessage) {
				response = handleBehaviorMessage(m);
			}
			
			//TODO: Pass the FileLogger to the RobotCI 
//			if(response == null && m instanceof LogMessage) {
//				LogMessage lm = (LogMessage)m;
//				Logger logger = drone.getIOManager().getFileLogger();
//				
//				if(logger != null)
//					logger.addLog(lm.getLog());
//			}
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

	@SuppressWarnings("unchecked")
	private NeuralActivationsMessage createNeuralActivationMessage() {
		ArrayList<?>[] info = ((ControllerCIBehavior)drone.getActiveBehavior()).getNeuralNetworkActivations();
		
		ArrayList<String> inputsTitles = (ArrayList<String>) info[0];
		ArrayList<String> outputsTitles = (ArrayList<String>) info[1];
		ArrayList<Double[]> inputsValues = (ArrayList<Double[]>) info[2];
		ArrayList<Double[]> outputsValues = (ArrayList<Double[]>) info[3];
		
		return new NeuralActivationsMessage(inputsTitles, inputsValues, outputsTitles, outputsValues);
	}
	
	private Message handleEntityMessage(Message m) {
		EntityMessage wm = (EntityMessage)m;
		Entity e = wm.getEntity();
		
		if(drone.getEntities().contains(e)) {
			drone.getEntities().remove(e);
		}
		
		drone.getEntities().add(e);
		return m;
	}
	
	private Message handleEntitiesMessage(Message m) {
		EntitiesMessage wm = (EntitiesMessage)m;
		LinkedList<Entity> entities = wm.getEntities();
		
		Iterator<Entity> i = drone.getEntities().iterator();
		
		while(i.hasNext()) {
			Entity e = i.next();
			if(e instanceof GeoFence)
				i.remove();
			if(e instanceof Waypoint)
				i.remove();
		}
		
		drone.getEntities().addAll(entities);
		return m;
	}
	
	private Message handleBehaviorMessage(Message m) {
		BehaviorMessage bm = (BehaviorMessage)m;
		
     	 if(bm.getSelectedStatus()) {
     		try {
     			
     			ArrayList<Class<?>> classes = ClassLoadHelper.findRelatedClasses(bm.getSelectedBehavior());
     			
     			if(classes == null || classes.isEmpty()) {
     				return null;
     			}
     			
     			Class<CIBehavior> chosenClass = (Class<CIBehavior>)classes.get(0);
         		Constructor<CIBehavior> constructor = chosenClass.getConstructor(new Class[] { CIArguments.class, RobotCI.class});
				CIBehavior ctArgs = constructor.newInstance(new Object[] { new CIArguments(bm.getArguments()), drone });
				drone.startBehavior(ctArgs);
     		} catch(ReflectiveOperationException e) {
     			e.printStackTrace();
     		}
     	 } else {
     		 drone.stopActiveBehavior();
     	 }
     	 bm.setArguments("");
     	 return bm;
	}
}