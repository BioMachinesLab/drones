package behaviors;

import main.Controller;
import network.messages.*;

public abstract class Behavior extends Thread implements MessageProvider{
	
	protected Controller c;
	protected long sleepTime = 100;
	protected boolean execute = false;
	
	public Behavior(Controller c) {
		this.c = c;
	}
	
	public Behavior(Controller c, long sleepTime) {
		this(c);
		this.sleepTime = sleepTime;
	}
	
	@Override
	public void run() {
		while(true) {
			
			try {
				while(!execute)
					wait();
			} catch(InterruptedException e) {}
			
			update();
			
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {}
		}
	}
	
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	public synchronized void startBehavior() {
		this.execute = true;
		notifyAll();
	}
	
	public void stopBehavior() {
		this.execute = false;
	}
	
	public abstract void setArgument(int index, double value);
	
	protected abstract void update();
	
	@Override
	public Message getMessage(Message request) {
		
		Message response = null;
		
		if(request instanceof BehaviorMessage) {
			
			BehaviorMessage bm = (BehaviorMessage)request;
			
			Class<Behavior> chosenClass = bm.getSelectedBehavior();
			
			if(chosenClass.isInstance(this)) {
				//send the same message back as an acknowledgement
				response = request;
				if(bm.changeStatusOrder()) {
					if(bm.getSelectedStatus()) {
						startBehavior();
					} else {
						stopBehavior();
					}
				} else if(bm.changeArgumentOrder()) {
					setArgument(bm.getArgumentIndex(), bm.getArgumentValue());
				}	
			}
		}
		return response;
	}
}