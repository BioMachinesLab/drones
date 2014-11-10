package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import network.messages.CompassMessage;
import network.messages.InformationRequest;
import network.messages.InformationRequest.MessageType;
import javax.swing.JLabel;

public class CompassPanel extends JPanel implements Runnable {
	private static final long serialVersionUID = -7954389075369447129L;
	private GUI gui;

	private JComboBox<String> comboBoxUpdateRate;

	private boolean keepGoing = true;
	private Thread threadRef;
	private int sleepTime;

	public CompassPanel(GUI gui) {
		this.gui = gui;
		setBorder(BorderFactory.createTitledBorder("Compass Data"));
		setLayout(null);
		setMinimumSize(new Dimension(300, 300));
		setPreferredSize(new Dimension(300, 300));

		comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(0);
		comboBoxUpdateRate.setBounds(204, 269, 86, 20);
		comboBoxUpdateRate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch (comboBoxUpdateRate.getSelectedIndex()) {
				case 0:
					sleepTime = 10000;
					break;
				case 1:
					sleepTime = 2000;
					break;
				case 2:
					sleepTime = 1000;
					break;
				case 3:
					sleepTime = 100;
					break;
				default:
					sleepTime = 1000;
					break;
				}
				threadRef.interrupt();
			}
		});
		add(comboBoxUpdateRate);
		
		JLabel labelRefreshRate = new JLabel("Refresh Rate");
		labelRefreshRate.setBounds(122, 272, 72, 14);
		add(labelRefreshRate);
	}

	public void displayData(CompassMessage message) {

	}

	private void requestCompassInformation() {
		gui.getConnector()
				.sendData(new InformationRequest(MessageType.COMPASS));
	}

	@Override
	public void run() {
		this.threadRef = Thread.currentThread();

		while (keepGoing) {
			requestCompassInformation();
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
