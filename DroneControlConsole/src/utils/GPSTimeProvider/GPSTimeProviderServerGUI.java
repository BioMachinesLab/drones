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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.joda.time.LocalDateTime;

public class GPSTimeProviderServerGUI extends JFrame implements GPSTimeProviderServerObserver {
	private static final long serialVersionUID = 1902399582928274879L;
	private JTextField serverPortTextField;
	private JTextField connectedClientsTextField;
	private JButton serverActionButton;

	private JTextField timeTextField;
	private JTextField satelitesTextField;
	private JComboBox<String> serialPortComboBox;
	private JButton gpsModuleActionButton;

	private JTextPane messagesAreaTextPane;

	private GPSTimeProviderServerObservated server;
	private GPSDataUpdater updater = null;

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

		setVisible(true);
	}

	private void buildPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("GPS Time provider server"), BorderLayout.NORTH);

		JPanel commandPanel = new JPanel(new GridLayout(1, 2));
		JPanel serverPanel = new JPanel(new GridLayout(0, 1));
		serverPanel.setBorder(BorderFactory.createTitledBorder("Server"));
		JPanel serverTextFieldsPanel = new JPanel(new GridLayout(0, 2));

		serverTextFieldsPanel.add(new JLabel("Server Port:"));
		serverPortTextField = new JTextField(Integer.toString(server.getDefaultPort()));
		serverPortTextField.setHorizontalAlignment(JTextField.CENTER);
		serverTextFieldsPanel.add(serverPortTextField);

		serverTextFieldsPanel.add(new JLabel("Connected clients:"));
		connectedClientsTextField = new JTextField("0");
		connectedClientsTextField.setHorizontalAlignment(JTextField.CENTER);
		connectedClientsTextField.setEditable(false);
		serverTextFieldsPanel.add(connectedClientsTextField);

		serverActionButton = new JButton("Start server");
		serverActionButton.setBackground(Color.RED);
		serverActionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleServerButton();
			}
		});

		JPanel gpsModulePanel = new JPanel(new GridLayout(0, 1));
		gpsModulePanel.setBorder(BorderFactory.createTitledBorder("GPS Module"));
		JPanel gpsModuleTextFieldsPanel = new JPanel(new GridLayout(0, 2));

		gpsModuleTextFieldsPanel.add(new JLabel("Time:"));
		timeTextField = new JTextField("N/A");
		timeTextField.setHorizontalAlignment(JTextField.CENTER);
		gpsModuleTextFieldsPanel.add(timeTextField);

		gpsModuleTextFieldsPanel.add(new JLabel("Satelites:"));
		satelitesTextField = new JTextField("N/A");
		satelitesTextField.setHorizontalAlignment(JTextField.CENTER);
		gpsModuleTextFieldsPanel.add(satelitesTextField);

		gpsModuleTextFieldsPanel.add(new JLabel("Serial port:"));
		serialPortComboBox = new JComboBox<String>(server.getSerialPortIdentifiers());
		if (server.getSerialPortIdentifiers().length > 0) {
			serialPortComboBox.setSelectedItem(0);
		}
		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		serialPortComboBox.setRenderer(dlcr);
		gpsModuleTextFieldsPanel.add(serialPortComboBox);

		gpsModuleActionButton = new JButton("Start GPS module");
		gpsModuleActionButton.setBackground(Color.RED);
		gpsModuleActionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleGPSButton();
			}
		});

		serverPanel.add(serverTextFieldsPanel);
		serverPanel.add(serverActionButton);
		gpsModulePanel.add(gpsModuleTextFieldsPanel);
		gpsModulePanel.add(gpsModuleActionButton);
		commandPanel.add(serverPanel);
		commandPanel.add(gpsModulePanel);

		messagesAreaTextPane = new JTextPane();
		messagesAreaTextPane.setEditable(false);
		messagesAreaTextPane.setPreferredSize(new Dimension(400, 300));
		messageAreaDocument = messagesAreaTextPane.getDocument();
		JScrollPane messagesAreaScrollPanel = new JScrollPane(messagesAreaTextPane);
		messagesAreaScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		panel.add(commandPanel, BorderLayout.NORTH);
		panel.add(messagesAreaScrollPanel, BorderLayout.CENTER);
		getContentPane().add(panel);
		setPreferredSize(new Dimension(500, 500));
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
			server.startGPSModule((String)serialPortComboBox.getSelectedItem());
		}
	}

	private class GPSDataUpdater extends Thread {

		@Override
		public void run() {
			boolean exit = false;
			while (!exit) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					exit = true;
				}

				LocalDateTime data = server.getGPSData().getDate();

				if (data != null) {
					int sateliteCount = server.getGPSData().getNumberOfSatellitesInView();
					timeTextField.setText(data.getHourOfDay() + ":" + data.getMinuteOfHour() + ":"
							+ data.getSecondOfMinute() + "," + data.getSecondOfMinute());
					satelitesTextField.setText(Integer.toString(sateliteCount));
				} else {
					data = new LocalDateTime();
					timeTextField.setText(data.getHourOfDay() + ":" + data.getMinuteOfHour() + ":"
							+ data.getSecondOfMinute() + "," + data.getSecondOfMinute());
					satelitesTextField.setText("Local time");
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
		serverActionButton.setText("Start GPS module");
		serverActionButton.setBackground(Color.RED);

		serialPortComboBox.setEnabled(true);
		serialPortComboBox.removeAllItems();
		for (String port : server.getSerialPortIdentifiers()) {
			serialPortComboBox.addItem(port);
		}

		updater.interrupt();
	}

	@Override
	public void setOnlineGPSModule() {
		serverActionButton.setText("Stop GPS module");
		serverActionButton.setBackground(Color.GREEN);
		serialPortComboBox.setEnabled(false);
		updater = new GPSDataUpdater();
		updater.start();
	}

	@Override
	public void setMessage(String message) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.BLACK);
		StyleConstants.setBackground(set, Color.WHITE);

		try {
			LocalDateTime time = new LocalDateTime();
			String str = "[" + time.getHourOfDay() + ":" + time.getMinuteOfHour() + ":" + time.getSecondOfMinute()
					+ "] " + message + "\n";
			messageAreaDocument.insertString(messageAreaDocument.getLength(), str, set);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setErrorMessage(String message) {
		SimpleAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setForeground(set, Color.RED);
		StyleConstants.setBackground(set, Color.WHITE);

		try {
			LocalDateTime time = new LocalDateTime();
			String str = "[" + time.getHourOfDay() + ":" + time.getMinuteOfHour() + ":" + time.getSecondOfMinute()
					+ "] " + message + "\n";
			messageAreaDocument.insertString(messageAreaDocument.getLength(), str, set);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateStatus() {
		connectedClientsTextField.setText(Integer.toString(server.getConnectedClientsQuantity()));
	}
}
