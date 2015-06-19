package gui.panels;

import gui.DroneGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.DroneControlConsole;
import network.server.ServerConnectionListener;
import network.server.ServerObserver;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ServerPanel extends JPanel implements ServerObserver {

	private DroneGUI gui;
	private ServerConnectionListener mobileAppServer;

	private JButton actionButton;
	private JTextField portNumberTextfield;

	private JTextField displayMessageArea;

	public ServerPanel(DroneGUI gui) {
		this.gui = gui;
		this.mobileAppServer = ((DroneControlConsole) (gui.getConsole()))
				.getMobileAppServer();
		mobileAppServer.setObserver(this);

		buildGUI();
	}

	private void buildGUI() {
		setBorder(BorderFactory.createTitledBorder("Mobile App Server"));

		setLayout(new BorderLayout());

		JPanel inputPanel = new JPanel();
		actionButton = new JButton("Start");
		actionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleButton();
			}
		});

		portNumberTextfield = new JTextField("" + ServerConnectionListener.SERVER_PORT);
		portNumberTextfield.setColumns(15);
		inputPanel.setLayout(new FlowLayout());
		inputPanel.add(actionButton);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setHorizontalAlignment(SwingConstants.RIGHT);
		inputPanel.add(lblPort);
		inputPanel.add(portNumberTextfield);
		
		displayMessageArea = new JTextField("Offline...");
		displayMessageArea.setColumns(15);
		displayMessageArea.setBackground(Color.RED);

		add(inputPanel, BorderLayout.CENTER);
		add(displayMessageArea, BorderLayout.SOUTH);
	}

	private void toggleButton() {
		if (mobileAppServer.isRunning()) {
			mobileAppServer.stopServer();
		} else {
			if (portNumberTextfield.getText().matches("^[0-9]{1,5}$")
					&& Integer.parseInt(portNumberTextfield.getText()) > 1023
					&& Integer.parseInt(portNumberTextfield.getText()) < 65535) {
				mobileAppServer.startServer(Integer.parseInt(portNumberTextfield
						.getText()));
			} else {
				JOptionPane.showMessageDialog(null, "Invalid port number!",
						"Input Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public synchronized void updateStatus(){
		if (mobileAppServer.isRunning()) {
			setOnlineServer();
		}else{
			setOfflineServer();
		}
	}

	public synchronized void setOfflineServer() {
		displayMessageArea.setBackground(Color.RED);
		displayMessageArea.setText("Offline...");
		actionButton.setText("Start");
	}

	public synchronized void setOnlineServer() {
		try {
			displayMessageArea.setBackground(Color.GREEN);

			displayMessageArea.setText("Running on "
					+ InetAddress.getLocalHost().getHostAddress() + ":"
					+ mobileAppServer.getPort() + " ("+mobileAppServer.getClientQuantity()+" clients)");
			actionButton.setText("Stop");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public synchronized void setMessage(String message) {
		displayMessageArea.setText(message);
	}
}
