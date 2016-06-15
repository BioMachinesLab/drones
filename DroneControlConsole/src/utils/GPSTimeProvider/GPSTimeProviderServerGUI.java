package utils.GPSTimeProvider;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.joda.time.LocalDateTime;

import commoninterface.dataobjects.GPSData;

public class GPSTimeProviderServerGUI extends JFrame implements GPSTimeProviderServerObserver {
	private static final long serialVersionUID = 1902399582928274879L;
	private JTextField serverPortTextField;
	private JTextField connectedClientsTextField;
	private JButton serverActionButton;

	private JTextField timeTextField;
	private JTextField hasFixTextField;
	private JTextField latitudeTextField;
	private JTextField longitudeTextField;
	private JTextField satelitesTextField;
	private JCheckBox serverUMTSummerCompensationCheckBox;
	private JComboBox<String> serialPortComboBox;
	private JButton gpsModuleActionButton;

	private JTextPane messagesAreaTextPane;

	private GPSTimeProviderServerObservated server;
	private GPSDataUpdater gpsDataUpdater = null;
	private SerialPortListUpdater serialPortListUpdater = null;

	private Document messageAreaDocument;

	public GPSTimeProviderServerGUI(GPSTimeProviderServerObservated server) {
		this.server = server;
		server.setObserver(this);
		buildPanel();

		pack();
		setTitle("GPS Time provider server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		gpsDataUpdater = new GPSDataUpdater();
		gpsDataUpdater.start();

		setVisible(true);
	}

	private void buildPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("GPS Time provider server"), BorderLayout.NORTH);

		JPanel serverPanel = new JPanel(new GridLayout(0, 2));
		serverPanel.setBorder(BorderFactory.createTitledBorder("Server"));

		serverPanel.add(new JLabel("Server Port:"));
		serverPortTextField = new JTextField(Integer.toString(server.getDefaultPort()));
		serverPortTextField.setHorizontalAlignment(JTextField.CENTER);
		serverPanel.add(serverPortTextField);

		serverPanel.add(new JLabel("Connected clients:"));
		connectedClientsTextField = new JTextField("0");
		connectedClientsTextField.setHorizontalAlignment(JTextField.CENTER);
		connectedClientsTextField.setEditable(false);
		serverPanel.add(connectedClientsTextField);

		JPanel gpsModulePanel = new JPanel(new BorderLayout());
		gpsModulePanel.setBorder(BorderFactory.createTitledBorder("GPS Module"));

		JPanel gpsModulePanelWrapper = new JPanel(new GridLayout(0, 2));
		gpsModulePanelWrapper.add(new JLabel("Time:"));
		timeTextField = new JTextField("N/A");
		timeTextField.setHorizontalAlignment(JTextField.CENTER);
		timeTextField.setEditable(false);
		gpsModulePanelWrapper.add(timeTextField);

		gpsModulePanelWrapper.add(new JLabel("Has fix:"));
		hasFixTextField = new JTextField("NO");
		hasFixTextField.setBackground(Color.RED);
		hasFixTextField.setHorizontalAlignment(JTextField.CENTER);
		hasFixTextField.setEditable(false);
		gpsModulePanelWrapper.add(hasFixTextField);

		gpsModulePanelWrapper.add(new JLabel("Latitude:"));
		latitudeTextField = new JTextField("N/A");
		latitudeTextField.setHorizontalAlignment(JTextField.CENTER);
		latitudeTextField.setEditable(false);
		gpsModulePanelWrapper.add(latitudeTextField);

		gpsModulePanelWrapper.add(new JLabel("Longitude:"));
		longitudeTextField = new JTextField("N/A");
		longitudeTextField.setHorizontalAlignment(JTextField.CENTER);
		longitudeTextField.setEditable(false);
		gpsModulePanelWrapper.add(longitudeTextField);

		gpsModulePanelWrapper.add(new JLabel("Satelites:"));
		satelitesTextField = new JTextField("N/A");
		satelitesTextField.setHorizontalAlignment(JTextField.CENTER);
		satelitesTextField.setEditable(false);
		gpsModulePanelWrapper.add(satelitesTextField);

		gpsModulePanelWrapper.add(new JLabel("Serial port:"));
		serialPortComboBox = new JComboBox<String>(server.getSerialPortIdentifiers());
		if (server.getSerialPortIdentifiers().length > 0) {
			serialPortComboBox.setSelectedItem(0);
		}
		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		serialPortComboBox.setRenderer(dlcr);
		gpsModulePanelWrapper.add(serialPortComboBox);
		serialPortListUpdater = new SerialPortListUpdater();
		serialPortListUpdater.start();

		serverUMTSummerCompensationCheckBox = new JCheckBox("Compensate local summer hour");
		gpsModulePanel.add(gpsModulePanelWrapper, BorderLayout.CENTER);
		gpsModulePanel.add(serverUMTSummerCompensationCheckBox, BorderLayout.SOUTH);

		serverActionButton = new JButton("Start server");
		serverActionButton.setBackground(Color.RED);
		serverActionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleServerButton();
			}
		});

		gpsModuleActionButton = new JButton("Start GPS module");
		gpsModuleActionButton.setBackground(Color.RED);
		gpsModuleActionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleGPSButton();
			}
		});

		messagesAreaTextPane = new JTextPane();
		messagesAreaTextPane.setEditable(false);
		messageAreaDocument = messagesAreaTextPane.getDocument();
		messagesAreaTextPane.setPreferredSize(new Dimension(450, 250));
		JScrollPane messagesAreaScrollPanel = new JScrollPane(messagesAreaTextPane);
		messagesAreaScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		DefaultCaret caret = (DefaultCaret) messagesAreaTextPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		topPanel.add(serverPanel);
		topPanel.add(gpsModulePanel);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		buttonsPanel.add(serverActionButton);
		buttonsPanel.add(gpsModuleActionButton);

		JPanel allStuff = new JPanel(new BorderLayout());
		allStuff.add(topPanel, BorderLayout.CENTER);
		allStuff.add(buttonsPanel, BorderLayout.SOUTH);

		panel.add(allStuff, BorderLayout.NORTH);
		panel.add(messagesAreaScrollPanel, BorderLayout.CENTER);
		getContentPane().add(panel);
	}

	private void toggleServerButton() {
		if (server.isServerRunning()) {
			server.stopServer();
		} else {
			int port = server.getDefaultPort();
			try {
				port = Integer.parseInt(serverPortTextField.getText());
			} catch (NumberFormatException e) {
				setErrorMessage("Invalid server port. Using default one (" + port + ")");
			} finally {
				server.startServer(port);
			}
		}
	}

	private void toggleGPSButton() {
		if (server.isGPSModuleRunning()) {
			server.stopGPSModule();
		} else {
			server.startGPSModule((String) serialPortComboBox.getSelectedItem());
		}
	}

	private class SerialPortListUpdater extends Thread {
		private boolean stop = false;

		public synchronized void stop(boolean stop) {
			this.stop = stop;
			notify();
		}

		@Override
		public void run() {
			boolean exit = false;
			while (!exit) {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					exit = true;
				}

				synchronized (this) {
					while (stop) {
						try {
							wait();
						} catch (InterruptedException e) {
							exit = true;
						}
					}
				}

				serialPortComboBox.removeAllItems();
				for (String port : server.getSerialPortIdentifiers()) {
					serialPortComboBox.addItem(port);
				}
			}

		}
	}

	private class GPSDataUpdater extends Thread {

		@Override
		public void run() {
			boolean exit = false;
			boolean printedAcquired = false;
			boolean printedLost = false;

			while (!exit) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					exit = true;
				}

				if (server.getGPSData() != null) {
					GPSData data = server.getGPSData();
					LocalDateTime date = data.getDate();

					String str = "";
					if (serverUMTSummerCompensationCheckBox.isSelected() && data.getFixType() == -10000) {
						str = "(-1h) " + date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":"
								+ date.getSecondOfMinute() + "," + date.getSecondOfMinute();
					} else {
						str = date.getHourOfDay() + ":" + date.getMinuteOfHour() + ":" + date.getSecondOfMinute() + ","
								+ date.getSecondOfMinute();
					}

					int sateliteCount = data.getNumberOfSatellitesInView();
					timeTextField.setText(str);

					if (!data.isFix() && data.getFixType() == -10000) {
						satelitesTextField.setText("Server time");
					} else {
						satelitesTextField.setText(Integer.toString(sateliteCount));
					}

					if (data.isFix()) {
						latitudeTextField.setText(Double.toString(data.getLatitudeDecimal()));
						longitudeTextField.setText(Double.toString(data.getLongitudeDecimal()));
						hasFixTextField.setText("YES");
						hasFixTextField.setBackground(Color.GREEN);

						if (!printedAcquired) {
							setMessage("Acquired GPS fix");
							printedAcquired = true;
							printedLost = false;
						}
					} else {
						latitudeTextField.setText("N/A");
						longitudeTextField.setText("N/A");
						hasFixTextField.setText("NO");
						hasFixTextField.setBackground(Color.RED);

						if (!printedLost) {
							setMessage("Waiting for GPS fix");
							printedLost = true;
							printedAcquired = false;
						}
					}
				}
			}
		}
	}

	@Override
	public void setOfflineServer() {
		serverActionButton.setText("Start server");
		serverActionButton.setBackground(Color.RED);
		serverPortTextField.setEditable(true);
	}

	@Override
	public void setOnlineServer() {
		serverActionButton.setText("Stop server");
		serverActionButton.setBackground(Color.GREEN);
		serverPortTextField.setEditable(false);
	}

	@Override
	public void setOfflineGPSModule() {
		timeTextField.setText("N/A");
		satelitesTextField.setText("N/A");
		gpsModuleActionButton.setText("Start GPS module");
		gpsModuleActionButton.setBackground(Color.RED);

		serialPortComboBox.setEnabled(true);
		serialPortComboBox.removeAllItems();
		serialPortListUpdater.stop(false);
	}

	@Override
	public void setOnlineGPSModule() {
		gpsModuleActionButton.setText("Stop GPS module");
		gpsModuleActionButton.setBackground(Color.GREEN);
		serialPortComboBox.setEnabled(false);
		serialPortListUpdater.stop(true);
	}

	@Override
	public synchronized void setMessage(String message) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.BLACK);
		StyleConstants.setBackground(set, Color.WHITE);

		try {
			LocalDateTime time = new LocalDateTime();
			String str = "[" + time.getHourOfDay() + ":" + time.getMinuteOfHour() + ":" + time.getSecondOfMinute()
					+ "] " + message + "\n";
			synchronized (messageAreaDocument) {
				messageAreaDocument.insertString(messageAreaDocument.getLength(), str, set);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void setErrorMessage(String message) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.RED);
		StyleConstants.setBackground(set, Color.WHITE);

		try {
			LocalDateTime time = new LocalDateTime();
			String str = "[" + time.getHourOfDay() + ":" + time.getMinuteOfHour() + ":" + time.getSecondOfMinute()
					+ "] " + message + "\n";

			synchronized (messageAreaDocument) {
				messageAreaDocument.insertString(messageAreaDocument.getLength(), str, set);
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateStatus() {
		connectedClientsTextField.setText(Integer.toString(server.getConnectedClientsQuantity()));
	}

	public boolean serverUMTSummerCompensationActive() {
		return serverUMTSummerCompensationCheckBox.isSelected();
	}
}
