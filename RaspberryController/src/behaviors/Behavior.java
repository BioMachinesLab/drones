package behaviors;

import main.Controller;

public abstract class Behavior extends Thread {
	
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
	
	public void startBehavior() {
		this.execute = true;
		notifyAll();
	}
	
	public void stopBehavior() {
		this.execute = false;
	}
	
	public abstract void setArgument(int index, double value);
	
	protected abstract void update();
}
