package gui.panels;

import java.awt.BorderLayout;
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

import commoninterface.dataobjects.GPSData;
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
	private JTextField hasFixTextField;
	private JTextField latitudeTextField;
	private JTextField longitudeTextField;
	private JTextField satelitesTextField;

	private Updater updater = null;

	public GPSTimePanel(DroneGUI gui) {
		this.gui = gui;
		buildPanel();

		try {
			if (InetAddress.getByName(SERVER_IP).isReachable(5 * 1000)) {
				gpsTimeProviderClient = new GPSTimeProviderClient(InetAddress.getByName(SERVER_IP), SERVER_PORT);
			} else {
				System.err.printf("[%s] Unreachable GPS time provider server%n", getClass().getName());
			}
		} catch (IOException e) {
			System.err.printf("[%s] Error resolving server address -> %s%n", getClass().getName(), e.getMessage());
		}
	}

	private void buildPanel() {
		JPanel inputPanel = new JPanel(new GridLayout(5, 2));
		inputPanel.add(new JLabel("Time:"));
		timeTextField = new JTextField("N/A");
		timeTextField.setHorizontalAlignment(JTextField.CENTER);
		timeTextField.setEditable(false);
		inputPanel.add(timeTextField);

		inputPanel.add(new JLabel("Has fix:"));
		hasFixTextField = new JTextField("NO");
		hasFixTextField.setBackground(Color.RED);
		hasFixTextField.setHorizontalAlignment(JTextField.CENTER);
		hasFixTextField.setEditable(false);
		inputPanel.add(hasFixTextField);

		inputPanel.add(new JLabel("Latitude:"));
		latitudeTextField = new JTextField("N/A");
		latitudeTextField.setHorizontalAlignment(JTextField.CENTER);
		latitudeTextField.setEditable(false);
		inputPanel.add(latitudeTextField);

		inputPanel.add(new JLabel("Longitude:"));
		longitudeTextField = new JTextField("N/A");
		longitudeTextField.setHorizontalAlignment(JTextField.CENTER);
		longitudeTextField.setEditable(false);
		inputPanel.add(longitudeTextField);

		inputPanel.add(new JLabel("Satelites:"));
		satelitesTextField = new JTextField("N/A");
		satelitesTextField.setHorizontalAlignment(JTextField.CENTER);
		satelitesTextField.setEditable(false);
		inputPanel.add(satelitesTextField);

		actionButton = new JButton("Start");
		actionButton.setBackground(Color.RED);
		actionButton.setSize(new Dimension(100, 20));
		actionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleButton();
			}
		});

		setBorder(BorderFactory.createTitledBorder("GPS Time Client"));
		setLayout(new BorderLayout());
		add(inputPanel, BorderLayout.CENTER);
		add(actionButton, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(100, 180));
	}

	private void toggleButton() {
		if (gpsTimeProviderClient!=null && gpsTimeProviderClient.connectionOK()) {
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
						System.err.printf("[%s] Unreachable GPS time provider server%n", getClass().getName());
					}
				} catch (IOException e) {
					System.err.printf("[%s] Error resolving server address -> %s%n", getClass().getName(),
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
				System.err.printf("[%s] Error initializing GPS time provider client%n", getClass().getName());
				JOptionPane.showMessageDialog(this, "Error initializing GPS time provider client!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e) {
			System.err.printf("[%s] Error initializing GPS time provider client -> %s%n", getClass().getName(),
					e.getMessage());
			JOptionPane.showMessageDialog(this, "Error initializing GPS time provider client!\n " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void stopClient() {
		gpsTimeProviderClient.closeConnection();

		timeTextField.setText("N/A");
		satelitesTextField.setText("N/A");
		latitudeTextField.setText("N/A");
		longitudeTextField.setText("N/A");
		actionButton.setText("Start");
		actionButton.setBackground(Color.RED);
		hasFixTextField.setText("NO");
		hasFixTextField.setBackground(Color.RED);

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
						System.err.printf("[%s] GPS time provider server is down!%n", getClass().getName());
					}
				}

				if (gpsTimeProviderClient.getGPSData() != null) {
					GPSData data = gpsTimeProviderClient.getGPSData();
					LocalDateTime date = data.getDate();
					int sateliteCount = data.getNumberOfSatellitesInView();
					timeTextField.setText(date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":"
							+ date.getSecondOfMinute() + "," + date.getSecondOfMinute());

					if (data.getFixType() == -10000) {
						satelitesTextField.setText("Server time");
					} else {
						satelitesTextField.setText(Integer.toString(sateliteCount));
					}

					if (data.isFix()) {
						LatLon latLon = new LatLon(data.getLatitudeDecimal(), data.getLongitudeDecimal());
						gui.getMapPanel().setBaseStation(latLon);

						latitudeTextField.setText(Double.toString(data.getLatitudeDecimal()));
						longitudeTextField.setText(Double.toString(data.getLongitudeDecimal()));
						hasFixTextField.setText("YES");
						hasFixTextField.setBackground(Color.GREEN);
					} else {
						gui.getMapPanel().clearBaseStation();
						latitudeTextField.setText("N/A");
						longitudeTextField.setText("N/A");
						hasFixTextField.setText("NO");
						hasFixTextField.setBackground(Color.RED);
					}
				} else {
					LocalDateTime date = new LocalDateTime();
					timeTextField.setText(date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":"
							+ date.getSecondOfMinute() + "," + date.getSecondOfMinute());
					satelitesTextField.setText("Local time");
					gui.getMapPanel().clearBaseStation();
					latitudeTextField.setText("N/A");
					longitudeTextField.setText("N/A");
					hasFixTextField.setText("NO");
					hasFixTextField.setBackground(Color.RED);
				}
			}
			stopClient();
		}
	}

	public LocalDateTime getDate() {
		return gpsTimeProviderClient.getGPSData().getDate();
	}
}
