package gui.utils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class GetIPFromUser {
	private static final String IP_ADDRESS_REGEX = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
	private static final String HOSTNAME_REGEX = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$";
	private static final String IP_ELEMENT_REGEX = "^(([0-1]?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-4]))$";

	private InetAddress ip = null;
	private String ip_asString = "";
	private int portNumber = -1;
	private String username = "";
	private String defaultPort = "";
	private String defaultIPAddress = "";
	private String windowTitle = "";

	private JRadioButton firstLineRadioButton;
	private JRadioButton secondLineRadioButton;

	private JTextField address1JTextField, address2JTextField, address3JTextField, address4JTextField;
	private JTextField hostnameJTextField;
	private JTextField portNumberJTextField;
	private int pressedButton;

	public GetIPFromUser(final String windowTitle, final String defaultPort, final String defaultIPAddress) {
		this.windowTitle = windowTitle;
		this.defaultPort = defaultPort;
		this.defaultIPAddress = defaultIPAddress;

		JPanel panel = buildGUI();

		boolean exit = false;
		while (!exit) {
			pressedButton = JOptionPane.showConfirmDialog(null, panel, windowTitle, JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);

			if (pressedButton == JOptionPane.CANCEL_OPTION) {
				exit = true;
			} else if (parseUserInformations() == 0) {
				exit = true;
			}
		}
	}

	private JPanel buildGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("Not able to set LookAndFeel for the current OS");
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JLabel title = new JLabel(windowTitle);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(title, BorderLayout.NORTH);
		mainPanel.add(buildCentralPane(), BorderLayout.CENTER);

		return mainPanel;
	}

	private JPanel buildCentralPane() {
		JPanel mainPanel = new JPanel(new GridLayout(1, 2));
		JPanel leftPanel = new JPanel(new GridLayout(3, 1));
		JPanel rightPanel = new JPanel(new GridLayout(3, 1));

		/*
		 * First Line Construction (IP address introduction)
		 */
		// Text Fields Construction
		String[] ipAddrStr = defaultIPAddress.split("\\.");
		address1JTextField = new JTextField(3);
		address1JTextField.setText(ipAddrStr[0]);
		address2JTextField = new JTextField(3);
		address2JTextField.setText(ipAddrStr[1]);
		address3JTextField = new JTextField(3);
		address3JTextField.setText(ipAddrStr[2]);
		address4JTextField = new JTextField(3);
		address4JTextField.setText(ipAddrStr[3]);

		// Panel Construction
		JPanel addressAndLabelJPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		addressAndLabelJPane.add(address1JTextField);
		addressAndLabelJPane.add(new JLabel("."));
		addressAndLabelJPane.add(address2JTextField);
		addressAndLabelJPane.add(new JLabel("."));
		addressAndLabelJPane.add(address3JTextField);
		addressAndLabelJPane.add(new JLabel("."));
		addressAndLabelJPane.add(address4JTextField);
		rightPanel.add(addressAndLabelJPane);

		/*
		 * Hostname introduction
		 */
		hostnameJTextField = new JTextField(15);
		hostnameJTextField.setText(defaultIPAddress);
		hostnameJTextField.setEditable(false);
		hostnameJTextField.setEnabled(false);
		rightPanel.add(hostnameJTextField);

		/*
		 * JRadio Buttons Management
		 */
		firstLineRadioButton = new JRadioButton("IP Address");
		firstLineRadioButton.addActionListener(new RadioButtonActionListener());
		firstLineRadioButton.setSelected(true);

		secondLineRadioButton = new JRadioButton("Hostname");
		secondLineRadioButton.addActionListener(new RadioButtonActionListener());
		secondLineRadioButton.setSelected(false);

		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(firstLineRadioButton);
		radioButtonGroup.add(secondLineRadioButton);

		leftPanel.add(firstLineRadioButton);
		leftPanel.add(secondLineRadioButton);

		/*
		 * Port introduction
		 */
		JLabel portLabel = new JLabel("Port");
		portLabel.setHorizontalAlignment(SwingConstants.CENTER);
		leftPanel.add(portLabel);

		portNumberJTextField = new JTextField(5);
		portNumberJTextField.setText(defaultPort);
		rightPanel.add(portNumberJTextField);

		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		return mainPanel;
	}

	private int parseUserInformations() {
		/*
		 * Parse port number
		 */
		String portNumberText = portNumberJTextField.getText();
		boolean portNumberInError = false;
		if (portNumberText.matches("^[0-9]{1,5}$")) {
			int portNumberFromUser = Integer.parseInt(portNumberText);
			if (portNumberFromUser < 0 || portNumberFromUser > 65535) {
				portNumberInError = true;
			} else {
				portNumber = portNumberFromUser;
			}
		} else {
			portNumber = -1;
			portNumberInError = true;
		}

		/*
		 * Parse remaining stuff
		 */
		boolean ipAddressInError = false;
		boolean hostnameInError = false;
		if (firstLineRadioButton.isSelected()) {
			String address1JTextFieldText = address1JTextField.getText();
			String address2JTextFieldText = address2JTextField.getText();
			String address3JTextFieldText = address3JTextField.getText();
			String address4JTextFieldText = address4JTextField.getText();

			if (address1JTextFieldText.matches(IP_ELEMENT_REGEX) && address2JTextFieldText.matches(IP_ELEMENT_REGEX)
					&& address3JTextFieldText.matches(IP_ELEMENT_REGEX)
					&& address4JTextFieldText.matches(IP_ELEMENT_REGEX)) {

				try {
					ip_asString = address1JTextFieldText + "." + address2JTextFieldText + "." + address3JTextFieldText
							+ "." + address4JTextFieldText;
					ip = InetAddress.getByName(ip_asString);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} else {
				ipAddressInError = true;
				ip_asString = null;
				ip = null;
			}
		} else {
			String hostnameJTextFieldText = hostnameJTextField.getText();
			if (hostnameJTextFieldText.matches(IP_ADDRESS_REGEX) || hostnameJTextFieldText.matches(HOSTNAME_REGEX)) {
				try {
					ip_asString = hostnameJTextFieldText;
					ip = InetAddress.getByName(ip_asString);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} else {
				ip_asString = null;
				ip = null;
				hostnameInError = true;
			}
		}

		if (portNumberInError || ipAddressInError || hostnameInError) {
			String message = "The ";

			if (portNumberInError) {
				message += "port number";

				int val = (ipAddressInError ? 1 : 0) + (hostnameInError ? 1 : 0);
				if (val > 1) {
					message += ", ";
				} else {
					if (val == 1) {
						message += " and ";
					}
				}
			}

			if (ipAddressInError) {
				message += "IP address";
			}

			if (hostnameInError) {
				message += "hostname";
			}

			int val = (portNumberInError ? 1 : 0) + (ipAddressInError ? 1 : 0) + (hostnameInError ? 1 : 0);
			if (val > 1) {
				message += " are ";
			} else {
				message += " is ";
			}
			message += "invalid. Please Try Again!";
			JOptionPane.showMessageDialog(null, message, "Input Error", JOptionPane.ERROR_MESSAGE);

			return 1;
		} else {
			return 0;
		}
	}

	public InetAddress getIpAddress() {
		return ip;
	}

	public String getIpAddressAsString() {
		return ip_asString;
	}

	public String getPortNumberAsString() {
		return Integer.toString(portNumber);
	}

	public int getPortNumber() {
		return portNumber;
	}

	public String getUsername() {
		return username;
	}

	public int getPressedButton() {
		return pressedButton;
	}

	private class RadioButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean veredict = false;
			if (e.getActionCommand().equals(firstLineRadioButton.getActionCommand())) {
				veredict = true;
			} else {
				veredict = false;
			}

			address1JTextField.setEditable(veredict);
			address1JTextField.setEnabled(veredict);

			address2JTextField.setEditable(veredict);
			address2JTextField.setEnabled(veredict);

			address3JTextField.setEditable(veredict);
			address3JTextField.setEnabled(veredict);

			address4JTextField.setEditable(veredict);
			address4JTextField.setEnabled(veredict);

			hostnameJTextField.setEditable(!veredict);
			hostnameJTextField.setEnabled(!veredict);
		}

	}

	public void lockIPHostnameInput() {
		address1JTextField.setEditable(false);
		address1JTextField.setEnabled(false);
		address2JTextField.setEditable(false);
		address2JTextField.setEnabled(false);
		address3JTextField.setEditable(false);
		address3JTextField.setEnabled(false);
		address4JTextField.setEditable(false);
		address4JTextField.setEnabled(false);

		hostnameJTextField.setEditable(false);
		hostnameJTextField.setEnabled(false);

		portNumberJTextField.setEditable(false);
		portNumberJTextField.setEnabled(false);

		firstLineRadioButton.setEnabled(false);
		secondLineRadioButton.setEnabled(false);
	}

	public void unlockIPHostnameInput() {
		boolean veredict = false;
		if (firstLineRadioButton.isSelected()) {
			veredict = true;
		} else {
			veredict = false;
		}

		address1JTextField.setEditable(veredict);
		address1JTextField.setEnabled(veredict);
		address2JTextField.setEditable(veredict);
		address2JTextField.setEnabled(veredict);
		address3JTextField.setEditable(veredict);
		address3JTextField.setEnabled(veredict);
		address4JTextField.setEditable(veredict);
		address4JTextField.setEnabled(veredict);

		hostnameJTextField.setEditable(!veredict);
		hostnameJTextField.setEnabled(!veredict);

		portNumberJTextField.setEditable(true);
		portNumberJTextField.setEnabled(true);

		firstLineRadioButton.setEnabled(true);
		secondLineRadioButton.setEnabled(true);
	}

	public static void main(String[] args) {
		new GetIPFromUser("teste", "1234", "127.0.0.1");
	}
}