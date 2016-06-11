package gui.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.joda.time.LocalDateTime;

import commoninterface.utils.jcoord.LatLon;
import gui.DroneGUI;
import network.GPSTimeProviderClient;

public class GPSTimePanel extends JPanel {
	private static final long serialVersionUID = 3310464759362205768L;
	private final static String SERVER_IP = "127.0.0.1";
	private final static int SERVER_PORT = 9190;
	private GPSTimeProviderClient gpsTimeProviderClient = null;

	private DroneGUI gui;
	private JButton actionButton;
	private JTextField timeTextField;
	private JTextField satelitesTextField;

	private Updater updater = null;

	public GPSTimePanel(DroneGUI gui) {
		this.gui = gui;
		buildPanel();

		try {
			if (InetAddress.getByName(SERVER_IP).isReachable(5 * 1000)) {
				gpsTimeProviderClient = new GPSTimeProviderClient(InetAddress.getByName(SERVER_IP), SERVER_PORT);
			} else {
				System.err.printf("[%s] Unreachable GPS time provider server\n", getClass().getName());
			}
		} catch (IOException e) {
			System.err.printf("[%s] Error resolving server address -> %s\n", getClass().getName(), e.getMessage());
		}
	}

	private void buildPanel() {
		setBorder(BorderFactory.createTitledBorder("GPS Time Client"));
		setLayout(new GridLayout(3, 1));

		JPanel inputPanel = new JPanel(new GridLayout(2, 2));
		inputPanel.add(new JLabel("Time:"));
		timeTextField = new JTextField("N/A");
		timeTextField.setEditable(false);
		timeTextField.setHorizontalAlignment(JTextField.CENTER);
		inputPanel.add(timeTextField);

		inputPanel.add(new JLabel("Satelites:"));
		satelitesTextField = new JTextField("N/A");
		satelitesTextField.setEditable(false);
		satelitesTextField.setHorizontalAlignment(JTextField.CENTER);
		inputPanel.add(satelitesTextField);

		actionButton = new JButton("Start");
		actionButton.setBackground(Color.RED);
		actionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleButton();
			}
		});

		add(inputPanel);
		add(actionButton);
		setPreferredSize(new Dimension(100, 150));
	}

	private void toggleButton() {
		if (gpsTimeProviderClient.connectionOK()) {
			stopClient();
		} else {
			startClient();
		}
	}

	public void startClient() {
		try {
			if (gpsTimeProviderClient.connectionOK()) {
				stopClient();
			}

			if (!gpsTimeProviderClient.isAlive() && gpsTimeProviderClient.exited()) {
				try {
					if (InetAddress.getByName(SERVER_IP).isReachable(5 * 1000)) {
						gpsTimeProviderClient = new GPSTimeProviderClient(InetAddress.getByName(SERVER_IP),
								SERVER_PORT);
					} else {
						System.err.printf("[%s] Unreachable GPS time provider server\n", getClass().getName());
					}
				} catch (IOException e) {
					System.err.printf("[%s] Error resolving server address -> %s\n", getClass().getName(),
							e.getMessage());
				}
			}

			gpsTimeProviderClient.connect();
			gpsTimeProviderClient.start();

			if (gpsTimeProviderClient.connectionOK()) {
				actionButton.setText("Stop");
				actionButton.setBackground(Color.GREEN);

				updater = new Updater();
				updater.start();
			} else {
				System.err.printf("[%s] Error initializing GPS time provider client\n", getClass().getName());
				JOptionPane.showMessageDialog(this, "Error initializing GPS time provider client!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			System.err.printf("[%s] Error initializing GPS time provider client %s\n", getClass().getName(),
					e.getMessage());
			JOptionPane.showMessageDialog(this, "Error initializing GPS time provider client!\n " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void stopClient() {
		gpsTimeProviderClient.closeConnection();

		timeTextField = new JTextField("N/A");
		satelitesTextField = new JTextField("N/A");
		actionButton.setText("Start");
		actionButton.setBackground(Color.RED);

		updater.interrupt();

		gui.getMapPanel().clearBaseStation();
	}

	private class Updater extends Thread {

		@Override
		public void run() {
			boolean exit = false;
			int errors = 0;

			while (gpsTimeProviderClient.connectionOK() && !exit) {
				try {
					gpsTimeProviderClient.requestUpdate();
					sleep(500);
				} catch (InterruptedException e) {
				} catch (IOException e) {
					// System.err.printf("[%s] Unable to send data... is there
					// an open connection?\n",
					// getClass().getName());
					errors++;

					if (errors == 5) {
						exit = true;
						System.err.printf("[%s] GPS time provider server is down!\n", getClass().getName());
					}
				}

				if (gpsTimeProviderClient.getGPSData() != null) {
					LocalDateTime date = gpsTimeProviderClient.getGPSData().getDate();
					int sateliteCount = gpsTimeProviderClient.getGPSData().getNumberOfSatellitesInView();
					timeTextField.setText(date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":"
							+ date.getSecondOfMinute() + "," + date.getSecondOfMinute());
					satelitesTextField.setText(Integer.toString(sateliteCount));

					if (gpsTimeProviderClient.getGPSData().isFix()) {
						LatLon latLon = new LatLon(gpsTimeProviderClient.getGPSData().getLatitudeDecimal(),
								gpsTimeProviderClient.getGPSData().getLongitudeDecimal());
						gui.getMapPanel().setBaseStation(latLon);
					} else {
						gui.getMapPanel().clearBaseStation();
					}
				} else {
					LocalDateTime date = new LocalDateTime();
					timeTextField.setText(date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":"
							+ date.getSecondOfMinute() + "," + date.getSecondOfMinute());
					satelitesTextField.setText("Local time");
					gui.getMapPanel().clearBaseStation();
				}
			}
			stopClient();
		}
	}

	public LocalDateTime getDate() {
		return gpsTimeProviderClient.getGPSData().getDate();
	}
}
