package gui;

import java.awt.GridLayout;
import java.awt.Panel;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class IPandPortNumberRequestToUser extends Panel {
	private InetAddress ip = null;
	private int portNumber = -1;

	public IPandPortNumberRequestToUser() {
		JPanel dialogJPane = new JPanel(new GridLayout(2, 1));

		// JTextFields Construction
		JTextField address1 = new JTextField(3);
		address1.setText("10");

		JTextField address2 = new JTextField(3);
		address2.setText("40");

		JTextField address3 = new JTextField(3);
		address3.setText("50");

		JTextField address4 = new JTextField(3);
		address4.setText("242");

		JTextField portTextField = new JTextField(5);
		portTextField.setText("10101");

		// Panel Construction
		final JPanel addressPanel = new JPanel();
		addressPanel.add(new JLabel("IP Address:"));
		addressPanel.add(address1);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address2);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address3);
		addressPanel.add(new JLabel("."));
		addressPanel.add(address4);
		dialogJPane.add(addressPanel);

		JPanel portPanel = new JPanel();
		portPanel.add(new JLabel("Port:"));
		portPanel.add(portTextField);
		dialogJPane.add(portPanel);

		int result = JOptionPane.showConfirmDialog(null, dialogJPane,
				"Please Enter Drone's Address and Port",
				JOptionPane.OK_CANCEL_OPTION);

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
				int portNumber = Integer.parseInt(portTextField.getText());
				if (portNumber < 1024 || portNumber > 65535) {
					JOptionPane
							.showMessageDialog(
									null,
									"The given Port number is not valid! Please try again",
									"Port Number Error",
									JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						ip = InetAddress.getByName(address1.getText() + "."
								+ address2.getText() + "." + address3.getText()
								+ "." + address4.getText());
						this.portNumber = portNumber;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	public InetAddress getIpAddress() {
		return ip;
	}

	public int getPortNumber() {
		return portNumber;
	}
}
