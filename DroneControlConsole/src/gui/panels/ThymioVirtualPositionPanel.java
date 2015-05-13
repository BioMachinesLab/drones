package gui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import threads.UpdateThread;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.ThymioVirtualPositionMessage;

public class ThymioVirtualPositionPanel extends UpdatePanel {

	private UpdateThread thread;
	private JTextField thymioPosition;
	private JTextField thymioOrientation;
	
	private long sleepTime = 1000;
	private DecimalFormat df = new DecimalFormat("#.00");
	
	public ThymioVirtualPositionPanel() {
		setBorder(BorderFactory.createTitledBorder("Virtual Positions"));
		setLayout(new BorderLayout());
		
		add(createPositionsPanel());
		add(createRefreshRatePanel(), BorderLayout.SOUTH);
	}
	
	private JPanel createPositionsPanel(){
		JPanel panel = new JPanel(new GridLayout(3, 1));
		
		JLabel label = new JLabel("Thymio:");
		thymioPosition = new JTextField("N/A");
		
		JLabel label2 = new JLabel("Orientation:");
		thymioOrientation = new JTextField("N/A");
		
		panel.add(label);
		panel.add(thymioPosition);
		panel.add(label2);
		panel.add(thymioOrientation);
		
		return panel;
	}
	
	private JPanel createRefreshRatePanel() {
		JPanel refreshPanel = new JPanel(new BorderLayout());
		refreshPanel.add(new JLabel("Refresh Rate"), BorderLayout.WEST);
		
		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(new String[] {
				"10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		refreshPanel.add(comboBoxUpdateRate, BorderLayout.EAST);
		
		comboBoxUpdateRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		
		JPanel bottom = new JPanel(new GridLayout(1,1));
		bottom.add(refreshPanel);
		
		return bottom;
	}
	
	public synchronized void displayData(ThymioVirtualPositionMessage message) {
		Vector2d tPosition = message.getVirtualThymioPosition();
		double orientation = message.getVirtualOrientation();

		if(thymioPosition != null){
			thymioPosition.setText("X: " + df.format(tPosition.x) +", Y: " + df.format(tPosition.y));
			thymioOrientation.setText(String.valueOf(df.format(Math.toDegrees(orientation))) + "º");
			notifyAll();
		}
		
	}
	
	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	private synchronized void wakeUpThread() {
		notifyAll();
		thread.interrupt();
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

}
