package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import network.messages.SystemStatusMessage;

public class MessagesPanel extends JPanel implements Runnable {
	private static final long serialVersionUID = 5958293256864880036L;
	private GUI gui;

	private JTextArea messageArea;
	private JScrollPane scrollPane;
	private JComboBox<String> comboBoxUpdateRate;

	private boolean keepGoing = true;
	private Thread threadRef;
	private int sleepTime = 1000;

	public MessagesPanel(GUI gui) {
		this.gui = gui;
		setBorder(BorderFactory.createTitledBorder("Drone Messages"));
		setLayout(null);
		setMinimumSize(new Dimension(300, 300));
		setPreferredSize(new Dimension(750, 165));

		messageArea = new JTextArea();
		messageArea.setEditable(false);

		scrollPane = new JScrollPane(messageArea);
		scrollPane.setSize(730, 96);
		scrollPane.setLocation(10, 23);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);

		comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		comboBoxUpdateRate.setBounds(654, 130, 86, 20);
		comboBoxUpdateRate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
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
				threadRef.interrupt();
			}
		});
		add(comboBoxUpdateRate);

		JLabel lblRefreshRate = new JLabel("Refresh Rate");
		lblRefreshRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRefreshRate.setBounds(564, 133, 80, 14);
		add(lblRefreshRate);
	}

	public void addMessage(SystemStatusMessage message) {
		if (message.getMessage() != null) {
			String str = message.getMessage();
			if (!str.endsWith("\n") && !str.endsWith("\r\n"))
				str += "\n";

			str = message.getTimestamp() + " - " + str;
			messageArea.append(str);
		}
	}

	private void requestSystemStatus() {
		gui.getConnector().sendData(
				new InformationRequest(MessageType.SYSTEM_STATUS));
	}

	@Override
	public void run() {
		this.threadRef = Thread.currentThread();

		while (keepGoing) {
			requestSystemStatus();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// we expect interruptions when we change the refresh rate
			}
		}
	}

	public void stopExecuting() {
		keepGoing = false;
	}
}
