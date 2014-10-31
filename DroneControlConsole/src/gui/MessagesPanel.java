package gui;

import java.awt.BorderLayout;
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
		setLayout(new BorderLayout());
		
//		setPreferredSize(new Dimension(100, 100));
//		setMinimumSize(new Dimension(100, 100));

		messageArea = new JTextArea(5,40);
		messageArea.setEditable(false);

		scrollPane = new JScrollPane(messageArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.NORTH);

		comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
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
		
		add(comboBoxUpdateRate, BorderLayout.EAST);

		JLabel lblRefreshRate = new JLabel("Refresh Rate");
		lblRefreshRate.setHorizontalAlignment(SwingConstants.RIGHT);
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