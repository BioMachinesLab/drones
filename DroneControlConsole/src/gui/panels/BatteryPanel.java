package gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicProgressBarUI;

import commoninterface.dataobjects.BatteryStatus;
import commoninterface.network.messages.BatteryMessage;
import threads.UpdateThread;

public class BatteryPanel extends UpdatePanel {
	private final static int CELL_QUANTITY = 3;
	private final static double MIN_VOLTAGE = 3.0;
	private final static double MAX_VOLTAGE = 4.2;

	private final static double GOOD_VOLTAGE_THRESHOLD = 3.75;
	private final static double WARNING_VOLTAGE_THRESHOLD = 3.3;
	private final static double DANGEROUS_VOLTAGE_THRESHOLD = 3.1;

	private final static double TEMPERATURE_NORMAL = 30;
	private final static double TEMPERATURE_WARNING = 40;
	private final static double TEMPERATURE_DANGER = 50;
	
	public final static int VOLTAGE_MULTIPLIER = 1000;
	public final static int TEMPERATURE_MULTIPLIER = 100;

	private UpdateThread thread;
	private long sleepTime = 10000;
	private BatteryStatus batteryStatus = null;
	private JProgressBar[] voltageMeters = new JProgressBar[CELL_QUANTITY];
	private JTextField temperature;

	public BatteryPanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Battery Status"));

		JPanel refreshPanel = new JPanel(new BorderLayout());
		refreshPanel.add(new JLabel("Refresh Rate"), BorderLayout.WEST);

		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(3);
		refreshPanel.add(comboBoxUpdateRate, BorderLayout.EAST);
		
		JPanel temperaturePanel = new JPanel(new BorderLayout());
		temperaturePanel.add(new JLabel("Temperature"), BorderLayout.WEST);
		temperature=new JTextField("N/A     ");
		temperature.setEditable(false);
		temperaturePanel.add(temperature,BorderLayout.EAST);

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

		JPanel jProgressBarsPanel = new JPanel(new GridLayout(1, CELL_QUANTITY));
		for (int i = 0; i < CELL_QUANTITY; i++) {
			voltageMeters[i] = new JProgressBar(
					(int) (MIN_VOLTAGE * VOLTAGE_MULTIPLIER),
			     		(int) (MAX_VOLTAGE * VOLTAGE_MULTIPLIER));
			voltageMeters[i].setUI(new MyProgressUI());

			voltageMeters[i].setPreferredSize(new Dimension(50, 10));
			voltageMeters[i].setValue(0);
			voltageMeters[i].setForeground(Color.GREEN);
			voltageMeters[i].setUI(new MyProgressUI());
			voltageMeters[i].setStringPainted(true);
			voltageMeters[i].setOrientation(SwingConstants.VERTICAL);

			jProgressBarsPanel.add(voltageMeters[i]);
		}

		JPanel southPanel = new JPanel(new GridLayout(2,1));
		southPanel.add(temperaturePanel);
		southPanel.add(refreshPanel);
		
		this.add(jProgressBarsPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
	}

	private class MyProgressUI extends BasicProgressBarUI {

		@Override
		protected void paintDeterminate(Graphics g, JComponent c) {
			if (!(g instanceof Graphics2D)) {
				return;
			}

			Rectangle vr = new Rectangle();
			SwingUtilities.calculateInnerArea(c, vr);

			//Insets are not working! I think it's because the
			//panel is a child of another panel.
			Rectangle or = c.getBounds();
			Insets insets = c.getInsets();

			if (vr.width <= 0 || vr.height <= 0) {
				return;
			}
			int amountFull = c.getHeight();

			g.setColor(c.getBackground());
			g.fill3DRect(vr.x, vr.y, vr.width + 1, vr.height, false);

			// Set Correct color according to voltage
			double voltage = ((double) progressBar.getValue() / VOLTAGE_MULTIPLIER);
			
//			System.out.println("Voltage: " + voltage);
			if (voltage > GOOD_VOLTAGE_THRESHOLD)
				g.setColor(Color.GREEN);
			else if (voltage >= WARNING_VOLTAGE_THRESHOLD)
				g.setColor(Color.YELLOW);
			else if (voltage >= DANGEROUS_VOLTAGE_THRESHOLD)
				g.setColor(Color.RED);
			else if (voltage < DANGEROUS_VOLTAGE_THRESHOLD)
				g.setColor(Color.BLACK);

			g.fill3DRect(vr.x, vr.y + vr.height - amountFull, vr.width,
					amountFull, true);

			if (progressBar.isStringPainted()) {
				String voltageStr = voltage+"";
				
				if(voltageStr.length() > 4)
					voltageStr = voltageStr.substring(0,4);
				
				voltageStr+= " V";
				String percentStr = "(" + progressBar.getString() + ")";

				Point placement = getStringPlacement(g, voltageStr,
						0, 30, c.getWidth()/2 , getHeight()/2);

				if(g.getColor() == Color.YELLOW || g.getColor() == Color.GREEN)
					g.setColor(Color.BLACK);
				else
					g.setColor(Color.WHITE);

				FontMetrics fm = g.getFontMetrics(progressBar.getFont());

				g.drawString(voltageStr, placement.x, placement.y
						+ fm.getAscent() - 8);
				g.drawString(percentStr, placement.x + 5,
						placement.y + fm.getAscent() + 8);
			}
		}
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

	public synchronized void displayData(BatteryMessage message) {
		this.batteryStatus = message.getBatteryStatus();

		for (int i = 0; i < CELL_QUANTITY; i++) {
			voltageMeters[i]
					.setValue((int) (batteryStatus.getCellsVoltages()[i] * VOLTAGE_MULTIPLIER));
		}
		
		double temperatureVal = batteryStatus.getBatteryTemperature();
		if(temperatureVal<TEMPERATURE_WARNING)
			temperature.setBackground(Color.GREEN);
		else if(temperatureVal>=TEMPERATURE_WARNING && temperatureVal<TEMPERATURE_DANGER)
			temperature.setBackground(Color.YELLOW);
		else if(temperatureVal>=TEMPERATURE_DANGER)
			temperature.setBackground(Color.RED);
		
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("##.##", decimalFormatSymbols);
		temperature.setText(decimalFormat.format(temperatureVal)+" C");
		
		notifyAll();
	}
}
