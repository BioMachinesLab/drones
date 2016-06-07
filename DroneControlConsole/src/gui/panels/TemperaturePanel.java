package gui.panels;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import commoninterface.network.messages.TemperatureMessage;
import threads.UpdateThread;

public class TemperaturePanel extends UpdatePanel {
	private static final long serialVersionUID = -834153200645748947L;
	private static final String TEMP_UNIT = "ºC";

	private UpdateThread thread;
	private long sleepTime = 10000;
	private JTextField systemTemperature;
	private JTextField sensorTemperature;

	public TemperaturePanel() {
		setLayout(new GridLayout(2, 1));
		setBorder(BorderFactory.createTitledBorder("Temperatures"));

		add(buildTemperaturesPanel());
		add(buildRefreshPanel());
	}

	private JPanel buildRefreshPanel() {
		JPanel refresh = new JPanel();
		refresh.add(new JLabel("Refresh Rate"));

		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(3);
		comboBoxUpdateRate.addActionListener(new ActionListener() {
			@Override
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

		refresh.add(comboBoxUpdateRate);
		return refresh;
	}

	private JPanel buildTemperaturesPanel() {
		JPanel temperaturesPanel = new JPanel(new GridLayout(2, 2));

		temperaturesPanel.add(new JLabel("System Temp: "));
		systemTemperature = new JTextField("N/A");
		systemTemperature.setEditable(false);
		temperaturesPanel.add(systemTemperature);

		temperaturesPanel.add(new JLabel("Sensor Temp: "));
		sensorTemperature = new JTextField("N/A");
		sensorTemperature.setEditable(false);
		temperaturesPanel.add(sensorTemperature);

		return temperaturesPanel;
	}

	private synchronized void wakeUpThread() {
		notifyAll();
		thread.interrupt();
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public void threadWait() {
		try {
			synchronized (this) {
				wait();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public long getSleepTime() {
		return sleepTime;
	}

	public synchronized void displayData(TemperatureMessage message) {
		double[] temps = message.getTemperature();

		systemTemperature.setText(String.format("%.3f",temps[0]) + " "
				+ TEMP_UNIT);
		sensorTemperature.setText(String.format("%.3f",temps[1]) + " "
				+ TEMP_UNIT);

		notifyAll();
	}
}
