package gui;

import java.awt.GridLayout;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.RobotControlConsole;

public class IPRequestToUserModal extends JFrame {
	private static final long serialVersionUID = 1919821842077609484L;
	private String DEFAULT_IP = "192.168.3.11";
	private InetAddress ip = null;
	private String ip_asString = "";

	public IPRequestToUserModal(RobotControlConsole console) {
		JPanel dialogJPane = new JPanel(new GridLayout(1, 1));

		// JTextFields Construction
		String[] ipAddrStr = DEFAULT_IP.split("\\.");
		JTextField address1 = new JTextField(3);
		address1.setText(ipAddrStr[0]);

		JTextField address2 = new JTextField(3);
		address2.setText(ipAddrStr[1]);

		JTextField address3 = new JTextField(3);
		address3.setText(ipAddrStr[2]);

		JTextField address4 = new JTextField(3);
		address4.setText(ipAddrStr[3]);

		// Panel Construction
		JPanel addressPanel = new JPanel();
		addressPanel.add(new JLabel("IP Address:"));
		addressPanel.add(address1);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address2);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address3);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address4);
		dialogJPane.add(addressPanel);

		int result = JOptionPane.showConfirmDialog(null, dialogJPane,
				"Please Enter Drone's Address", JOptionPane.OK_CANCEL_OPTION);

		if (result == JOptionPane.OK_OPTION) {
			int add1 = Integer.parseInt(address1.getText());
			int add2 = Integer.parseInt(address2.getText());
			int add3 = Integer.parseInt(address3.getText());
			int add4 = Integer.parseInt(address4.getText());

			if (add1 < 0 || add1 > 255 || add2 < 0 || add2 > 255 || add3 < 0
					|| add3 > 255 || add4 < 0 || add4 > 255) {
				JOptionPane.showMessageDialog(null,
						"The given IP address is not valid! Please try again",
						"IP Address Error", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					ip_asString = address1.getText() + "." + address2.getText() + "." + address3.getText() + "." + address4.getText();
					ip = InetAddress.getByName(address1.getText() + "."
							+ address2.getText() + "." + address3.getText()
							+ "." + address4.getText());
					console.connect(ip_asString);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}

//		} else {
//			System.exit(0);
		}
	}

	public InetAddress getIpAddress() {
		return ip;
	}

	public String getIpAddressAsString() {
		return ip_asString;
	}

	// public int getInformationPortNumber() {
	// return informationPortNumber;
	// }

	// public int getMotorPortNumber() {
	// return motorPortNumber;
	// }
}
