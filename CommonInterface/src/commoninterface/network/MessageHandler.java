package commoninterface.network;

import commoninterface.network.messages.Message;

/**
 * This class implements a circular queue of messages. The objective is to
 * free the socket thread of the burden of finding the destination of a message
 * and sending out the response, hopefully keeping the socket buffer to a minimum
 * due to the low latency of this task.
 * 
 * It is used both in the Controller (RaspberryPi) and in the Console (desktop/laptop)
 * which can implement different versions of the handleMessage function.
 */
public abstract class MessageHandler extends Thread {
	
	protected static final int BUFFER_SIZE = 20;
	
	protected Message[] pendingMessages = new Message[BUFFER_SIZE];
	protected ConnectionHandler[] pendingConnections = new ConnectionHandler[BUFFER_SIZE];
	
	protected int pendingIndex = 0;
	protected int currentIndex = 0;
	
	private boolean keepExecuting = true;
	
	@Override
	public void run() {
		
		try {
			while(keepExecuting) {
				while(pendingIndex == currentIndex) {
					try {
						synchronized(this) {
							wait();
						}
					} catch(Exception e){}
				}
				
				Message response = processMessage(pendingMessages[currentIndex]);
				
				if(response != null)
					pendingConnections[currentIndex].sendData(response);
				
				pendingMessages[currentIndex] = null;
				pendingConnections[currentIndex] = null;
				
				currentIndex++;
				currentIndex%=BUFFER_SIZE;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract Message processMessage(Message m);
	
	/**
	 * Adds the message to the queue and moves the pointer to the next position.
	 * This function is called by the socket thread and notifies the MessageHandler
	 * thread so that it can start processing the message.
	 */
	public synchronized void addMessage(Message m, ConnectionHandler c) {
		pendingMessages[pendingIndex] = m;
		pendingConnections[pendingIndex] = c;
		pendingIndex++;
		pendingIndex%=BUFFER_SIZE;
		notifyAll();
	}
	
	public void stopExecuting() {
		keepExecuting = false;
	}

}
