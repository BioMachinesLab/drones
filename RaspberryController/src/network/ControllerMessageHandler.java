package network;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import network.messages.BehaviorMessage;
import network.messages.EntityMessage;
import network.messages.InformationRequest;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.NeuralActivationsMessage;
import network.messages.SystemStatusMessage;
import objects.Entity;
import simpletestbehaviors.ControllerCIBehavior;
import commoninterface.CIBehavior;
import commoninterface.RobotCI;
import commoninterface.utils.CIArguments;
import commoninterfaceimpl.RealRobotCI;

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
				EntityMessage wm = (EntityMessage)m;
				Entity e = wm.getEntity();
				
				if(drone.getEntities().contains(e)) {
					drone.getEntities().remove(e);
				}
				
				drone.getEntities().add(e);
				response = m;
			}
			
			if(response == null && m instanceof BehaviorMessage) {
				BehaviorMessage bm = (BehaviorMessage)m;
				
	          	 if(bm.getSelectedStatus()) {
	          		try {
	              		Constructor<CIBehavior> constructor = bm.getSelectedBehavior().getConstructor(new Class[] { CIArguments.class, RobotCI.class});
	    				CIBehavior ctArgs = constructor.newInstance(new Object[] { new CIArguments(bm.getArguments()), drone });
	    				drone.startBehavior(ctArgs);
	          		} catch(ReflectiveOperationException e) {
	          			e.printStackTrace();
	          		}
	          	 } else {
	          		 drone.stopActiveBehavior();
	          	 }
	          	 bm.setArguments("");
	          	 response = bm;
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
}