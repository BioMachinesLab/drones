package gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import network.ConnectionToDrone;
import network.messages.Message;
import network.messages.MotorMessage;

public class GUI {
	private JFrame frame;
	private Container contentPane;
	private InetAddress ip;
	private int portNumber = -1;
	private ConnectionToDrone connector;

	public static void main(String[] args) {
		new GUI();
	}

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

		IPandPortNumberRequest form = new IPandPortNumberRequest();
		if (form.getIpAddress() == null || form.getPortNumber() == -1) {
			System.exit(0);
		}

		connector = new ConnectionToDrone(this, form.getIpAddress(),
				form.getPortNumber());
		connector.start();

		// buildGUI();
		// display();
	}

	private void buildGUI() {
		frame = new JFrame();
		frame.setTitle("HANCAD/ CORATAM Project - Drone Remote Console - ");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		contentPane = frame.getContentPane();
		contentPane.setLayout(null);
	}

	public void display() {
		frame.setVisible(true);
	}

	public void processMessage(Message message) {
		// TODO Auto-generated method stub
		System.out
				.println("Received Message: " + message.getClass().toString());
	}
}
