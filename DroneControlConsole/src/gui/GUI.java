package gui;

import gamepad.GamePad;
import gamepad.GamePad.GamePadType;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import network.InformationConnection;
import network.MotorConnection;
import network.MotorMessageSender;
import network.messages.CompassMessage;
import network.messages.GPSMessage;
import network.messages.Message;
import network.messages.SystemInformationsMessage;
import network.messages.SystemStatusMessage;
import dataObjects.MotorSpeeds;

public class GUI {
	// Connections Objects
	private InformationConnection informationConnection;
	private MotorConnection motorConnection;

	// GUI Objects
	private JFrame frame;

	private MotorsPanel motorsPanel;
	private GPSPanel gpsPanel;
	private SystemInfoPanel sysInfoPanel;
	private MessagesPanel msgPanel;
//	private CompassPanel compassPanel;

	private GamePad gamePad;

	private Thread gpsThread;
	private Thread messagesThread;
	private Thread compassThread;

	private MotorSpeeds motorSpeeds;
	private MotorMessageSender motorMessageSender;

	public GUI() {
		motorSpeeds = new MotorSpeeds();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (informationConnection != null) {
					if (motorsPanel != null) {
						motorSpeeds.setSpeeds(0, 0);
					}
					if (informationConnection != null) {
						informationConnection.closeConnection();
					}
					if (gpsThread.isAlive()) {
						gpsThread.interrupt();
					}

					if (messagesThread.isAlive()) {
						messagesThread.interrupt();
					}

					if (compassThread.isAlive()) {
						compassThread.interrupt();
					}
				}
			}
		});

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err
					.println("Not able to set LookAndFeel for the current OS");
		}

		connect();
	}

	// Make sure that everything is at the initial state. Useful for
	// reconnections
	private void init() {
		if (frame != null)
			frame.dispose();

		if (gpsPanel != null)
			gpsPanel.stopExecuting();

		if (msgPanel != null)
			msgPanel.stopExecuting();

//		if (compassPanel != null)
//			compassPanel.stopExecuting();

		frame = null;
		msgPanel = null;
		gpsPanel = null;
//		compassPanel = null;
		sysInfoPanel = null;
		informationConnection = null;
		motorConnection = null;
	}

	public void connect() {
		init();

		do {
			try {
				IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
				if (form.getIpAddress() == null
						|| form.getInformationPortNumber() == -1
						|| form.getMotorPortNumber() == -1) {
					continue;
				} else {

					informationConnection = new InformationConnection(this,
							form.getIpAddress(),
							form.getInformationPortNumber());

					motorConnection = new MotorConnection(this,
							form.getIpAddress(), form.getMotorPortNumber());
					

					motorMessageSender = new MotorMessageSender(
							motorConnection, motorSpeeds);

					gpsThread = new Thread(gpsPanel);
					
					messagesThread = new Thread(msgPanel);

					// compassThread = new Thread(compassPanel);
					// compassThread.start();
					
					gamePad = new GamePad(this, GamePadType.GAMEPAD);
					
					buildGUI();
					display();

					informationConnection.start();
					motorConnection.start();
					motorMessageSender.start();
					gpsThread.start();
					messagesThread.start();
					gamePad.start();
				}
			} catch (Exception | Error e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame, e.getMessage());
			}
		} while (informationConnection == null);
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - "
				+ informationConnection.getDestInetAddress().getHostAddress());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setFocusable(true);

		frame.setLayout(new BorderLayout());

		// Central panel constructor and object addition to the frame
		JPanel centralPanel = new JPanel(new FlowLayout());
		motorsPanel = new MotorsPanel(this);
		centralPanel.add(motorsPanel);

		gpsPanel = new GPSPanel(this);
		centralPanel.add(gpsPanel);

		// sysInfoPanel = new SystemInfo_Panel(this);
		// connector.sendData(new InformationRequest(Message_Type.SYSTEM_INFO));
		// centralPanel.add(sysInfoPanel);

		// compassPanel=new CompassPanel(this);
		// centralPanel.add(compassPanel);

		frame.add(centralPanel, BorderLayout.CENTER);

		// South panel message area addition to the frame
		msgPanel = new MessagesPanel(this);
		frame.add(msgPanel, BorderLayout.PAGE_END);

		frame.pack();
		frame.setLocationRelativeTo(null);
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
					if(msgPanel == null)
						System.out.println("whaaa");
					msgPanel.addMessage((SystemStatusMessage) message);
				} else {
					if (message instanceof CompassMessage) {
						// compassPanel.displayData((CompassMessage) message);
					} else {
						System.out
								.println("Received non recognise message type: "
										+ message.getClass().toString());
					}
				}
			}
		}
	}

	protected InformationConnection getConnector() {
		return informationConnection;
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