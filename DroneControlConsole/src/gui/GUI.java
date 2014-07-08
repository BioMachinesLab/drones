package gui;

import java.awt.Container;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import network.ConnectionToDrone;
import network.messages.GPSMessage;
import network.messages.Message;

public class GUI {
	// Connections Objects
	private InetAddress ip;
	private int portNumber = -1;
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

		// try {
		// for (int i = 0; i < 5; i++) {
		// connector.sendData(new InformationRequest(Message_Type.GPS));
		// Thread.sleep(500);
		// }
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		buildGUI();
		display();
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - ");
		frame.setBounds(100, 100, 800, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		contentPane = frame.getContentPane();
		contentPane.setLayout(null);

		motorsPanel = new Motors_Panel(this);
		motorsPanel.setBounds(0, 318, 385, -318);
		contentPane.add(motorsPanel);
		frame.repaint();
	}

	public void display() {
		frame.setVisible(true);
	}

	public void processMessage(Message message) {
		if (message instanceof GPSMessage) {
			System.out.println("GPS MESSAGE:"
					+ ((GPSMessage) message).getGPSData().toString());
		} else {
			System.out.println("Received Message: "
					+ message.getClass().toString());
		}
	}
}
