package gui.panels;

import javax.swing.JPanel;

import threads.UpdateThread;

public abstract class UpdatePanel extends JPanel{
	private static final long serialVersionUID = -6024147826485066415L;
	public abstract void registerThread(UpdateThread t);
	public abstract void threadWait();
	public abstract long getSleepTime();

}
