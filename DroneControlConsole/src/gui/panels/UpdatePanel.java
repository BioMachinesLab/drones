package gui.panels;

import javax.swing.JPanel;

import threads.UpdateThread;

public abstract class UpdatePanel extends JPanel{
	
	public abstract void registerThread(UpdateThread t);
	public abstract void threadWait();
	public abstract long getSleepTime();

}
