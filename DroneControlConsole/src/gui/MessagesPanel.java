package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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

import network.messages.SystemStatusMessage;
import threads.UpdateThread;

public class MessagesPanel extends JPanel implements UpdatePanel {
	private static final long serialVersionUID = 5958293256864880036L;

	private JTextArea messageArea;
	private JScrollPane scrollPane;
	private JComboBox<String> comboBoxUpdateRate;

	private int sleepTime = 1000;
	
	private UpdateThread thread;

	public MessagesPanel() {
		setBorder(BorderFactory.createTitledBorder("Drone Messages"));
		setLayout(new BorderLayout());
		
//		setPreferredSize(new Dimension(100, 100));
//		setMinimumSize(new Dimension(100, 100));

		messageArea = new JTextArea(10,1);
		messageArea.setEditable(false);

		scrollPane = new JScrollPane(messageArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel refresh = new JPanel();
		
		JLabel lblRefreshRate = new JLabel("Refresh Rate");
		refresh.add(lblRefreshRate);

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
				if(thread != null)
					thread.interrupt();
			}
		});
		
		refresh.add(comboBoxUpdateRate);

		
		add(refresh, BorderLayout.SOUTH);
	}

	public void addMessage(SystemStatusMessage message) {
		if (message.getMessage() != null) {
			String str = message.getMessage();
			if (!str.endsWith("\n") && !str.endsWith("\r\n"))
				str += "\n";

			str = message.getTimestamp() + " - " + str;
			messageArea.append(str);
			messageArea.setCaretPosition(messageArea.getDocument().getLength());
		}
	}
	
	public int getSleepTime() {
		return sleepTime;
	}

	public void registerThread(UpdateThread t) {
		this.thread = t;
	}
}