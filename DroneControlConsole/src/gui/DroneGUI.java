package gui;

import gui.panels.BatteryPanel;
import gui.panels.CompassPanel;
import gui.panels.GPSPanel;
import gui.panels.LogsPanel;
import gui.panels.SystemInfoPanel;
import gui.panels.TemperaturePanel;
import gui.panels.map.MapPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import main.DroneControlConsole;

public class DroneGUI extends RobotGUI {
	private static final long serialVersionUID = 1835233364751980805L;

	private GPSPanel gpsPanel;
	private SystemInfoPanel sysInfoPanel;
	private CompassPanel compassPanel;
	private BatteryPanel batteryPanel;
	private MapPanel mapPanel;
	private TemperaturePanel temperaturePanel;
	private LogsPanel logsPanel;

	private JPanel rightPanel;

	public DroneGUI(DroneControlConsole console) {
		this.console = console;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err
			.println("Not able to set LookAndFeel for the current OS");
		}

		enableOSXFullscreen(this);
		buildGUI();
	}

	@Override
	protected void buildGUI() {
		setTitle("HANCAD/ CORATAM Project - Drone Remote Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setFocusable(true);

		setLayout(new BorderLayout());

		createPanels();
		createInfoPanel();
		createMapPanel();
		// createSysInfoPanel();

		pack();
		setLocationRelativeTo(null);
	}

	private void createMapPanel() {
		mapPanel = new MapPanel(this);
		add(mapPanel, BorderLayout.CENTER);
	}

	private void createInfoPanel() {
		rightPanel = new JPanel(new BorderLayout());

		JPanel hidePanel = new JPanel(new BorderLayout());
		hidePanel.setPreferredSize(new Dimension(30, this.getHeight()));

		// Motors
		rightPanel.add(motorsPanel, BorderLayout.NORTH);

		JPanel GPSCompassBatteriesPanel = new JPanel(new BorderLayout());

		// GPS
		gpsPanel = new GPSPanel();
		GPSCompassBatteriesPanel.add(gpsPanel, BorderLayout.NORTH);

		// Compass and batteries
		JPanel compassAndBatteriesPanel = new JPanel(new GridLayout(1, 2));
		compassPanel = new CompassPanel();
		temperaturePanel = new TemperaturePanel();
		//		batteryPanel = new BatteryPanel();

		compassAndBatteriesPanel.add(compassPanel);
		//		compassAndBatteriesPanel.add(batteryPanel);
		compassAndBatteriesPanel.add(temperaturePanel);
		GPSCompassBatteriesPanel.add(compassAndBatteriesPanel, BorderLayout.CENTER);

		rightPanel.add(GPSCompassBatteriesPanel, BorderLayout.CENTER);

		add(rightPanel, BorderLayout.EAST);

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel leftTopPanel = new JPanel();
		leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
		leftTopPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);

		// Connection
		leftTopPanel.add(connectionPanel);

		// Behaviors
		leftTopPanel.add(commandPanel);
		leftPanel.add(leftTopPanel, BorderLayout.NORTH);

		// Messages
		//leftPanel.add(msgPanel, BorderLayout.CENTER);

		// Mobile Application Server
		//serverPanel = new ServerPanel(this);
		//leftPanel.add(serverPanel, BorderLayout.SOUTH);

		logsPanel = new LogsPanel(this);
		leftPanel.add(logsPanel, BorderLayout.CENTER);

		add(leftPanel, BorderLayout.WEST);
	}

	public void hideRightPanel(){
		rightPanel.setVisible(false);
	}

	public void showRightPanel(){
		rightPanel.setVisible(true);
	}

	public GPSPanel getGPSPanel() {
		return gpsPanel;
	}

	public CompassPanel getCompassPanel() {
		return compassPanel;
	}

	public SystemInfoPanel getSysInfoPanel() {
		return sysInfoPanel;
	}

	public MapPanel getMapPanel() {
		return mapPanel;
	}

	public BatteryPanel getBatteryPanel() {
		return batteryPanel;
	}

	public TemperaturePanel getTemperaturesPanel() {
		return temperaturePanel;
	}

	public LogsPanel getLogsPanel() {
		return logsPanel;
	}
}