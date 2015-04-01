package gui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import network.messages.ThymioReadingsMessage;
import threads.UpdateThread;

public class ThymioSensorsPanel extends UpdatePanel {
	private static final int FRONT_SENSORS_NUMBER = 5;
	private static final int BACK_SENSORS_NUMBER = 2;
	
	private ArrayList<JTextField> frontSensorsReadings;
	private ArrayList<JTextField> backSensorsReadings;
	private ArrayList<JTextField> groundSensorsReadings;
	
	private long sleepTime = 10000;
	
	private UpdateThread thread;
	
	public ThymioSensorsPanel() {
		setBorder(BorderFactory.createTitledBorder("Sensors Readings"));
		setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(300,350));
		
		frontSensorsReadings = new ArrayList<JTextField>();
		backSensorsReadings = new ArrayList<JTextField>();
		groundSensorsReadings = new ArrayList<JTextField>();
		
		
		for (int i = 0; i < FRONT_SENSORS_NUMBER; i++) {
			JTextField t = new JTextField("N/A");
			t.setEditable(false);
			frontSensorsReadings.add(t);
		}
		
		for (int i = 0; i < BACK_SENSORS_NUMBER; i++) {
			JTextField t = new JTextField("N/A");
			JTextField t2 = new JTextField("N/A");
			t.setEditable(false);
			t2.setEditable(false);
			
			backSensorsReadings.add(t);
			groundSensorsReadings.add(t2);
		}
		
		JPanel readingsPanel = new JPanel(new BorderLayout());
		readingsPanel.add(frontSensorsReadings());
		
		JPanel southPanel = new JPanel(new GridLayout(2,1));
		southPanel.add(backSensorsReadings());
		southPanel.add(groundSensorsReadings());
		
		readingsPanel.add(southPanel, BorderLayout.SOUTH);
		
		add(readingsPanel);
		add(createRefreshRatePanel(), BorderLayout.SOUTH);
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
	
	private JPanel frontSensorsReadings(){
		JPanel frontSensorsPanel = new JPanel(new GridLayout(5, 1));
		frontSensorsPanel.setBorder(BorderFactory.createTitledBorder("Front Sensors"));
		
		createReadingsComponent(frontSensorsPanel, frontSensorsReadings, FRONT_SENSORS_NUMBER);
		
		return frontSensorsPanel;
	}
	
	private JPanel backSensorsReadings(){
		JPanel backSensorsPanel = new JPanel(new GridLayout(2, 1));
		backSensorsPanel.setBorder(BorderFactory.createTitledBorder("Back Sensors"));
		
		createReadingsComponent(backSensorsPanel, backSensorsReadings, BACK_SENSORS_NUMBER);
		
		return backSensorsPanel;
	}
	
	private JPanel groundSensorsReadings(){
		JPanel groundSensorsPanel = new JPanel(new GridLayout(2, 1));
		groundSensorsPanel.setBorder(BorderFactory.createTitledBorder("Ground Sensors"));
		
		createReadingsComponent(groundSensorsPanel, groundSensorsReadings, BACK_SENSORS_NUMBER);
		
		return groundSensorsPanel;
	}
	
	private void createReadingsComponent(JPanel panel, ArrayList<JTextField> sensorsList, int numberOfSensors){
		for (int i = 0; i < numberOfSensors; i++) {
			panel.add(new JLabel("Sensor " + i + ":"));
			panel.add(sensorsList.get(i));
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

	public synchronized void displayData(ThymioReadingsMessage message) {
		List<Short> readings = message.getReadings();
		
		for (int i = 0; i < readings.size(); i++) {
			if(i < FRONT_SENSORS_NUMBER)
				frontSensorsReadings.get(i).setText(String.valueOf(readings.get(i)));
			else
				backSensorsReadings.get(i - FRONT_SENSORS_NUMBER).setText(String.valueOf(readings.get(i)));
		}
		
		notifyAll();
	}
	
	public ArrayList<JTextField> getFrontSensorsReadingsTextFields() {
		return frontSensorsReadings;
	}
	
	public ArrayList<JTextField> getBackSensorsReadingsTextFields() {
		return backSensorsReadings;
	}
	
	public ArrayList<JTextField> getGroundSensorsReadingsTextFields() {
		return groundSensorsReadings;
	}
	
}
