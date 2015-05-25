package gui.panels;

import java.awt.BorderLayout;
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

import org.joda.time.LocalDateTime;

import commoninterface.network.messages.SystemStatusMessage;

import threads.UpdateThread;
import java.awt.Font;

public class MessagesPanel extends UpdatePanel {
	private static final long serialVersionUID = 5958293256864880036L;

	private JTextArea messageArea;
	private JScrollPane scrollPane;
	private JComboBox<String> comboBoxUpdateRate;

	private long sleepTime = 10000;
	
	private UpdateThread thread;

	public MessagesPanel() {
		setBorder(BorderFactory.createTitledBorder("Drone Messages"));
		setLayout(new BorderLayout());
		
		messageArea = new JTextArea(5,1);
		messageArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
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
		comboBoxUpdateRate.setSelectedIndex(3);
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
				wakeUpThread();
			}
		});
		
		refresh.add(comboBoxUpdateRate);

		
		add(refresh, BorderLayout.SOUTH);
	}
	
	public void clear() {
		messageArea.setText("");
	}
	
	private synchronized void wakeUpThread() {
		notifyAll();
		thread.interrupt();
	}

	public synchronized void displayData(SystemStatusMessage message) {
		if (message.getMessage() != null) {
			String str = message.getMessage();
			if (!str.endsWith("\n") && !str.endsWith("\r\n"))
				str += "\n";
			
			LocalDateTime time = message.getTimestamp();

			str = time.toString("HH:MM:ss.SS") + " - " + str;
			messageArea.append(str);
			messageArea.setCaretPosition(messageArea.getDocument().getLength());
			
			notifyAll();
		}
	}
	
	@Override
	public void threadWait() {
		try {
			synchronized(this){
				wait();
			}
		}catch(Exception e) {}
	}
	
	@Override
	public long getSleepTime() {
		return sleepTime;
	}

	public void registerThread(UpdateThread t) {
		this.thread = t;
	}
}