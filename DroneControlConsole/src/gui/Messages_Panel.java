package gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import network.messages.InformationRequest;
import network.messages.InformationRequest.Message_Type;
import network.messages.SystemStatusMessage;

public class Messages_Panel extends JPanel implements Runnable {
	private static final long serialVersionUID = 5958293256864880036L;
	private GUI gui;
	private JTextArea messageArea;
	private JScrollPane scrollPane;
	private JComboBox<String> comboBoxUpdateRate;

	public Messages_Panel(GUI gui) {
		this.gui = gui;
		setBorder(BorderFactory.createTitledBorder("Drone Messages"));
		setLayout(null);
		setMinimumSize(new Dimension(300, 300));
		setPreferredSize(new Dimension(600, 165));

		messageArea = new JTextArea();
		messageArea.setEditable(false);

		scrollPane = new JScrollPane(messageArea);
		scrollPane.setSize(580, 96);
		scrollPane.setLocation(10, 23);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);

		comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(0);
		comboBoxUpdateRate.setBounds(504, 130, 86, 20);
		add(comboBoxUpdateRate);

		JLabel lblRefreshRate = new JLabel("Refresh Rate");
		lblRefreshRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRefreshRate.setBounds(414, 133, 80, 14);
		add(lblRefreshRate);
	}

	public void addMessage(SystemStatusMessage message) {
		String str=message.getMessage();
		if (!str.endsWith("\n") && !str.endsWith("\r\n"))
			str += "\n";
		
		str=message.getTimestamp()+str;
		messageArea.append(str);
	}

	private void requestSystemStatus() {
		gui.getConnector().sendData(
				new InformationRequest(Message_Type.SYSTEM_STATUS));
	}

	@Override
	public void run() {
		while (true) {
			requestSystemStatus();

			int sleepTime = 0;
			switch (comboBoxUpdateRate.getSelectedIndex()) {
			case 0:
				sleepTime = 100;
				break;
			case 1:
				sleepTime = 200;
				break;
			case 2:
				sleepTime = 1000;
				break;
			case 3:
				sleepTime = 10000;
				break;
			default:
				sleepTime = 1000;
				break;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				System.out.println("Messages Panel thread was interrupted....");
				e.printStackTrace();
			}
		}
	}
}
