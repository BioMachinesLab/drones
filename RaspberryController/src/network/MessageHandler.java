package network;

import main.Controller;
import network.messages.BehaviorMessage;
import network.messages.Message;
import network.messages.MessageProvider;
import network.messages.SystemStatusMessage;
import behaviors.Behavior;

public class MessageHandler extends Thread {
	
	private static final int BUFFER_SIZE = 20;
	
	private Message[] pendingMessages = new Message[BUFFER_SIZE];
	private ConnectionHandler[] pendingConnections = new ConnectionHandler[BUFFER_SIZE];
	
	private int pendingIndex = 0;
	private int currentIndex = 0;
	
	private Controller controller;
	
	public MessageHandler(Controller c) {
		this.controller = c;
	}
	
	@Override
	public void run() {
		
		while(pendingIndex == currentIndex) {
			try {
				wait();
			} catch(Exception e){}
		}
		
		Message request = pendingMessages[currentIndex];
		Message response = null;
		
		if(request instanceof BehaviorMessage) {
			
			processBehaviorMessage(request);
			
		} else {
			for (MessageProvider p : controller.getMessageProviders()) {
				response = p.getMessage(request);
				if (response != null)
					break;
			}
	
			if (response == null)
				response = new SystemStatusMessage(
						"No message provider for the current request ("
								+ request.getClass().getSimpleName() + ")");
	
			pendingConnections[currentIndex].sendData(response);
		}
		
		pendingMessages[currentIndex] = null;
		pendingConnections[currentIndex] = null;
		
		currentIndex++;
		currentIndex%=BUFFER_SIZE;
	}
	
	private void processBehaviorMessage(Message request) {
		BehaviorMessage bm = (BehaviorMessage)request;

		Class<Behavior> chosenClass = bm.getSelectedBehavior();
		Behavior chosenBehavior = null;
		
		for(Behavior b : controller.getBehaviors()) {
			if(b.getClass().equals(chosenClass)) {
				chosenBehavior = b;
				break;
			}
		}
		
		if(chosenBehavior != null) {
			if(bm.changeStatusOrder()) {
				if(bm.getSelectedStatus()) {
					chosenBehavior.startBehavior();
				} else {
					chosenBehavior.stopBehavior();
				}
			} else if(bm.changeArgumentOrder()) {
				chosenBehavior.setArgument(bm.getArgumentIndex(), bm.getArgumentValue());
			}
		}
	}
	
	public synchronized void addMessage(Message m, ConnectionHandler c) {
		pendingMessages[currentIndex] = m;
		pendingConnections[currentIndex] = c;
		pendingIndex++;
		pendingIndex%=BUFFER_SIZE;
		notifyAll();
		System.out.println("[MessageHandler] Queue:"+Math.abs(pendingIndex-currentIndex));
	}

}
