package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GUI extends JFrame{
	

	private MotorsPanel motorsPanel;
	private GPSPanel gpsPanel;
	private SystemInfoPanel sysInfoPanel;
	private MessagesPanel msgPanel;
//	private CompassPanel compassPanel;
	private MapPanel mapPanel;

	public GUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err
					.println("Not able to set LookAndFeel for the current OS");
		}

		buildGUI();
	}

	// Make sure that everything is at the initial state. Useful for
	// reconnections

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
		JPanel infoPanel = new JPanel(new BorderLayout());

		//Motors
		JPanel motorsGPSPanel = new JPanel(new BorderLayout());
		motorsPanel = new MotorsPanel();
		motorsGPSPanel.add(motorsPanel, BorderLayout.NORTH);

		//GPS
		gpsPanel = new GPSPanel();
		motorsGPSPanel.add(gpsPanel, BorderLayout.CENTER);

		infoPanel.add(motorsGPSPanel, BorderLayout.NORTH);

		//Messages
		msgPanel = new MessagesPanel();
		infoPanel.add(msgPanel, BorderLayout.CENTER);
		
		add(infoPanel, BorderLayout.EAST);
	}
	
	private void createSysInfoPanel() {
		sysInfoPanel = new SystemInfoPanel(this);
		add(sysInfoPanel, BorderLayout.SOUTH);
	}
	
	public GPSPanel getGPSPanel() {
		return gpsPanel;
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
}