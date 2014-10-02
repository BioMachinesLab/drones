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
	private GamePad gamePad;
	private Thread gpsThread;
	private Thread messagesThread;
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
				}
			}
		});

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Not able to set LookAndFeel for the current OS");
		}

		connect();
	}
	
	//Make sure that everything is at the initial state. Useful for reconnections
	private void init() {
		if(frame != null)
			frame.dispose();
		
		if(gpsPanel != null)
			gpsPanel.stopExecuting();
		
		if(msgPanel != null)
			msgPanel.stopExecuting();
		
		frame = null;
		msgPanel = null;
		gpsPanel = null;
		sysInfoPanel = null;
		informationConnection = null;
		motorConnection = null;
	}
	
	public void connect() {
		
		init();
		
		do {
			try {
				IPandPortNumberRequestToUser form = new IPandPortNumberRequestToUser();
				if (form.getIpAddress() == null || form.getPortNumber() == -1) {
					continue;
				} else {

					informationConnection = new InformationConnection(this, form.getIpAddress());
					informationConnection.start();
					
					motorConnection = new MotorConnection(this, form.getIpAddress());
					motorConnection.start();

					motorMessageSender = new MotorMessageSender(motorConnection,motorSpeeds);
					motorMessageSender.start();

					buildGUI();

					gpsThread = new Thread(gpsPanel);
					gpsThread.start();
					messagesThread = new Thread(msgPanel);
					messagesThread.start();

					display();

					gamePad = new GamePad(this, GamePadType.GAMEPAD);
					
					if(gamePad.isAvailable())
						gamePad.start();
				}
			} catch (Exception | Error e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(frame,e.getMessage());
			}
		} while (informationConnection == null);
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setLocationRelativeTo(null);
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - "
				+ informationConnection.getDestInetAddress().getHostAddress());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setFocusable(true);

		frame.setLayout(new BorderLayout());

		JPanel centralPanel = new JPanel(new FlowLayout());
		motorsPanel = new MotorsPanel(this);
		centralPanel.add(motorsPanel);

		gpsPanel = new GPSPanel(this);
		centralPanel.add(gpsPanel);

		// sysInfoPanel = new SystemInfo_Panel(this);
		// connector.sendData(new InformationRequest(Message_Type.SYSTEM_INFO));
		// centralPanel.add(sysInfoPanel);

		frame.add(centralPanel, BorderLayout.CENTER);

		msgPanel = new MessagesPanel(this);
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
