package gui;

import gui.map.MapPanel;

import java.awt.BorderLayout;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import main.DroneControlConsole;

public class GUI extends JFrame{
	

	private MotorsPanel motorsPanel;
	private GPSPanel gpsPanel;
	private SystemInfoPanel sysInfoPanel;
	private MessagesPanel msgPanel;
	private CompassPanel compassPanel;
	private MapPanel mapPanel;
	private BehaviorsPanel behaviorsPanel;
	private ConnectionPanel connectionPanel;
	
	private DroneControlConsole console;

	public GUI(DroneControlConsole console) {
		
		this.console = console;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Not able to set LookAndFeel for the current OS");
		}
		
		enableOSXFullscreen(this);
		buildGUI();
	}

	private void buildGUI() {
		setTitle("HANCAD/ CORATAM Project - Drone Remote Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setFocusable(true);
		
		setLayout(new BorderLayout());
		
		createInfoPanel();
		createMapPanel();
//		createSysInfoPanel();

		pack();
		setLocationRelativeTo(null);
	}
	
	private void createMapPanel() {
		mapPanel = new MapPanel();
		add(mapPanel, BorderLayout.CENTER);
	}

	private void createInfoPanel() {
		JPanel rightPanel = new JPanel(new BorderLayout());

		//Motors
		JPanel motorsGPSPanel = new JPanel(new BorderLayout());
		motorsPanel = new MotorsPanel();
		motorsGPSPanel.add(motorsPanel, BorderLayout.NORTH);

		//GPS
		gpsPanel = new GPSPanel();
		motorsGPSPanel.add(gpsPanel, BorderLayout.CENTER);
		
		//Compass
		compassPanel = new CompassPanel();
		motorsGPSPanel.add(compassPanel, BorderLayout.SOUTH);

		rightPanel.add(motorsGPSPanel, BorderLayout.NORTH);
		
		add(rightPanel, BorderLayout.EAST);
		
		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel leftTopPanel = new JPanel();
		leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
		leftTopPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		
		//Connection
		connectionPanel = new ConnectionPanel(console);
		leftTopPanel.add(connectionPanel);
		
		//Behaviors
		behaviorsPanel = new BehaviorsPanel();
		leftTopPanel.add(behaviorsPanel);
		leftPanel.add(leftTopPanel, BorderLayout.NORTH);
		
		//Messages
		msgPanel = new MessagesPanel();
		leftPanel.add(msgPanel, BorderLayout.CENTER);
		
		add(leftPanel, BorderLayout.WEST);
	}
	
	private void createSysInfoPanel() {
		sysInfoPanel = new SystemInfoPanel(this);
		add(sysInfoPanel, BorderLayout.SOUTH);
	}
	
	public GPSPanel getGPSPanel() {
		return gpsPanel;
	}
	
	public CompassPanel getCompassPanel() {
		return compassPanel;
	}
	
	public MessagesPanel getMessagesPanel() {
		return msgPanel;
	}
	
	public SystemInfoPanel getSysInfoPanel() {
		return sysInfoPanel;
	}
	
	public MotorsPanel getMotorsPanel() {
		return motorsPanel;
	}
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public BehaviorsPanel getBehaviorsPanel() {
		return behaviorsPanel;
	}
	
	public ConnectionPanel getConnectionPanel() {
		return connectionPanel;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void enableOSXFullscreen(Window window) {
	    try {
	        Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
	        Class params[] = new Class[]{Window.class, Boolean.TYPE};
	        Method method = util.getMethod("setWindowCanFullScreen", params);
	        method.invoke(util, window, true);
	    } catch (ClassNotFoundException e1) {
	    } catch (Exception e) {}
	}
}