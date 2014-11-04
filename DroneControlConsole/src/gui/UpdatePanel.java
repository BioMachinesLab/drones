package gui;

import threads.UpdateThread;

public interface UpdatePanel {
	
	public void registerThread(UpdateThread t);
	public int getSleepTime();

}
