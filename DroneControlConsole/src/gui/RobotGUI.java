package gui;

import gui.panels.CommandPanel;
import gui.panels.ConnectionPanel;
import gui.panels.MessagesPanel;
import gui.panels.MotorsPanel;
import gui.panels.NeuralActivationsPanel;

import java.awt.BorderLayout;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import main.RobotControlConsole;

public abstract class RobotGUI extends JFrame {

	protected MotorsPanel motorsPanel;
	protected ConnectionPanel connectionPanel;
	protected CommandPanel commandPanel;
	protected MessagesPanel msgPanel;
	protected RobotControlConsole console;
	protected NeuralActivationsPanel neuralActivationsPanel;

	protected void createPanels() {
		motorsPanel = new MotorsPanel();
		connectionPanel = new ConnectionPanel(console);
		commandPanel = new CommandPanel(console, this);
		msgPanel = new MessagesPanel();

		neuralActivationsPanel = new NeuralActivationsPanel();
		commandPanel.getNeuralActivationsWindow().add(neuralActivationsPanel,
				BorderLayout.CENTER);
	}

	public MotorsPanel getMotorsPanel() {
		return motorsPanel;
	}

	public ConnectionPanel getConnectionPanel() {
		return connectionPanel;
	}

	public CommandPanel getCommandPanel() {
		return commandPanel;
	}

	public MessagesPanel getMessagesPanel() {
		return msgPanel;
	}

	public NeuralActivationsPanel getNeuralActivationsPanel() {
		return neuralActivationsPanel;
	}

	public RobotControlConsole getConsole() {
		return console;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void enableOSXFullscreen(Window window) {
		try {
			Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
			Class params[] = new Class[] { Window.class, Boolean.TYPE };
			Method method = util.getMethod("setWindowCanFullScreen", params);
			method.invoke(util, window, true);
		} catch (ClassNotFoundException e1) {
		} catch (Exception e) {
		}
	}

	protected abstract void buildGUI();

}
