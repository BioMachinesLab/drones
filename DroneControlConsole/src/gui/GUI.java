package gui;

import gamepad.GamePad;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dataObjects.MotorSpeeds;
import network.ConnectionToDrone;
import network.MotorMessageSender;
import network.messages.GPSMessage;
import network.messages.Message;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;

public class GUI {
	// Connections Objects
	private ConnectionToDrone connector;

	// GUI Objects
	private JFrame frame;
	private Motors_Panel motorsPanel;
	private GPS_Panel gpsPanel;
	private SystemInfo_Panel sysInfoPanel;
	private Messages_Panel msgPanel;
	private GamePad gamePad;
	private Thread gpsThread;
	private Thread messagesThread;
	private MotorSpeeds motorSpeeds;
	private MotorMessageSender motorMessageSender;

	public GUI() {

		motorSpeeds = new MotorSpeeds();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (connector != null) {
					if (motorsPanel != null) {
						motorSpeeds.setSpeeds(0, 0);
						// connector.sendData(new MotorMessage(0, 0));
					}
					if (connector != null) {
						connector.closeConnection();
					}
					if (gpsThread.isAlive()) {
						gpsThread.interrupt();
					}
					if (messagesThread.isAlive()) {
						messagesThread.interrupt();
					}
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

		do {
			try {
				IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
				if (form.getIpAddress() == null || form.getPortNumber() == -1) {
					continue;
				} else {

					connector = new ConnectionToDrone(this,
							form.getIpAddress(), form.getPortNumber());
					connector.start();

					motorMessageSender = new MotorMessageSender(connector,
							motorSpeeds);
					motorMessageSender.start();

					buildGUI();

					gpsThread = new Thread(gpsPanel);
					gpsThread.start();
					messagesThread = new Thread(msgPanel);
					messagesThread.start();

					display();

					gamePad = new GamePad(this);
					gamePad.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (connector == null);
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - "
				+ connector.getDestInetAddress().getHostAddress());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setFocusable(true);

		frame.setLayout(new BorderLayout());

		JPanel centralPanel = new JPanel(new FlowLayout());
		motorsPanel = new Motors_Panel(this);
		centralPanel.add(motorsPanel);

		gpsPanel = new GPS_Panel(this);
		centralPanel.add(gpsPanel);

		// sysInfoPanel = new SystemInfo_Panel(this);
		// connector.sendData(new InformationRequest(Message_Type.SYSTEM_INFO));
		// centralPanel.add(sysInfoPanel);

		frame.add(centralPanel, BorderLayout.CENTER);

		msgPanel = new Messages_Panel(this);
		frame.add(msgPanel, BorderLayout.PAGE_END);

		frame.pack();
	}

	public void display() {
		frame.setVisible(true);
	}

	public void processMessage(Message message) {
		if (message instanceof GPSMessage) {
			gpsPanel.displayData(((GPSMessage) message).getGPSData());
		} else {
			if (message instanceof SystemInformationsMessage) {
				sysInfoPanel.displayData(((SystemInformationsMessage) message)
						.getSysInformations());
			} else {
				if (message instanceof SystemStatusMessage) {
					msgPanel.addMessage((SystemStatusMessage) message);
				} else {
					System.out.println("Received Message: "
							+ message.getClass().toString());
				}
			}
		}
	}

	protected ConnectionToDrone getConnector() {
		return connector;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setMotorsValue(int left, int right) {
		motorsPanel.setLeftMotorPower(left);
		motorsPanel.setRightMotorPower(right);
	}

	public MotorSpeeds getMotorSpeeds() {
		return motorSpeeds;
	}

	public GamePad getGamePad() {
		return gamePad;
	}
}
