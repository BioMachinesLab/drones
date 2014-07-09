package gui;

import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import network.ConnectionToDrone;
import network.messages.GPSMessage;
import network.messages.Message;
import network.messages.MotorMessage;

public class GUI {
	// Connections Objects
	private ConnectionToDrone connector;

	// GUI Objects
	private JFrame frame;
	private Container contentPane;
	private Motors_Panel motorsPanel;
	private GPS_Panel gpsPanel;
	private SystemInfo_Panel sysInfoPanel;

	public GUI() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (connector != null) {
					if (motorsPanel != null) {
						connector.sendData(new MotorMessage(0, 0));
					}
					connector.closeConnection();
				}
			}
		});

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out
					.println("Not able to set LookAndFeel for the current OS");
			e.printStackTrace();
		}

		IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
		if (form.getIpAddress() == null || form.getPortNumber() == -1) {
			System.exit(0);
		}

		connector = new ConnectionToDrone(this, form.getIpAddress(),
				form.getPortNumber());
		connector.start();

		buildGUI();
		display();

		Thread gpsThread = new Thread(gpsPanel);
		gpsThread.start();
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - "
				+ connector.getDestInetAddress().getHostAddress());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);

		contentPane = frame.getContentPane();
		contentPane.setLayout(new FlowLayout());

		motorsPanel = new Motors_Panel(this);
		contentPane.add(motorsPanel);

		gpsPanel = new GPS_Panel(this);
		contentPane.add(gpsPanel);

		frame.pack();
	}

	public void display() {
		frame.setVisible(true);
	}

	public void processMessage(Message message) {
		if (message instanceof GPSMessage) {
			System.out.println("Received GPS data");
			gpsPanel.displayData(((GPSMessage) message).getGPSData());
		} else {
			System.out.println("Received Message: "
					+ message.getClass().toString());
		}
	}

	protected ConnectionToDrone getConnector() {
		return connector;
	}

	public JFrame getFrame() {
		return frame;
	}
}
