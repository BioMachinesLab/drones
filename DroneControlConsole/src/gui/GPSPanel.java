package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.joda.time.LocalDateTime;

import threads.UpdateThread;
import dataObjects.GPSData;

public class GPSPanel extends JPanel implements UpdatePanel {
	private static final long serialVersionUID = 6535539451990270799L;
	
	private static int TEXTFIELD_SIZE = 90;
	
	private JTextField textFieldLatitude;
	private JTextField textFieldLongitude;
	private JTextField textFieldSatelittesView;
	private JTextField textFieldSatelittesUsed;
	private JTextField textFieldVelKmh;
	private JTextField textFieldOrientation;
	private JTextField textFieldHDOP;
	private JTextField textFieldPDOP;
	private JTextField textFieldVDOP;
	private JTextField textFieldTime;
	
	private JLabel labelHasFix;
	
	private JComboBox<String> comboBoxUpdateRate;

	private UpdateThread thread;
	
	private int sleepTime = 1000;

	public GPSPanel() {
		setBorder(BorderFactory.createTitledBorder("GPS Data"));
		setLayout(new BorderLayout());

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());
		left.add(buildCoordinatesPanel(),BorderLayout.NORTH);
		add(left,BorderLayout.WEST);
		
		add(buildGPSFixInformationPanel(), BorderLayout.EAST);
		add(buildRefreshPanel(), BorderLayout.SOUTH);
	}
	
	private JPanel buildRefreshPanel() {
		JPanel refresh = new JPanel();
		refresh.add(new JLabel("Refresh Rate"));

		comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(new String[] {
				"10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		add(comboBoxUpdateRate, BorderLayout.SOUTH);
		
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
				if(thread != null)
					thread.interrupt();
			}
		});

		refresh.add(comboBoxUpdateRate);
		return refresh;
	}

	private JPanel buildCoordinatesPanel() {
		JPanel coordinatesPanel = new JPanel();
		coordinatesPanel.setBorder(BorderFactory.createTitledBorder(""));
		coordinatesPanel.setLayout(new GridLayout(5,2));
		
		coordinatesPanel.add(new JLabel("Has Fix"));
		labelHasFix = new JLabel();
		labelHasFix.setBackground(Color.RED);
		labelHasFix.setBorder(new LineBorder(new Color(238,238,238), 4));
		labelHasFix.setOpaque(true);
		coordinatesPanel.add(labelHasFix);

		coordinatesPanel.add(new JLabel("Latitude"));
		textFieldLatitude = new JTextField();
		textFieldLatitude.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		
		textFieldLatitude.setEditable(false);
		coordinatesPanel.add(textFieldLatitude);
		
		coordinatesPanel.add(new JLabel("Longitude"));
		textFieldLongitude = new JTextField();
		textFieldLatitude.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldLongitude.setEditable(false);
		coordinatesPanel.add(textFieldLongitude);

		coordinatesPanel.add(new JLabel("Vel. (Km/h)"));
		textFieldVelKmh = new JTextField();
		textFieldVelKmh.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldVelKmh.setEditable(false);
		coordinatesPanel.add(textFieldVelKmh);
		
		coordinatesPanel.add(new JLabel("Time"));
		textFieldTime = new JTextField();
		textFieldTime.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldTime.setEditable(false);
		coordinatesPanel.add(textFieldTime);
		
		return coordinatesPanel;
	}

	private JPanel buildGPSFixInformationPanel() {
		JPanel fixInformationsPanel = new JPanel();
		fixInformationsPanel.setBorder(BorderFactory.createTitledBorder(""));
		fixInformationsPanel.setLayout(new GridLayout(5,2));

		JLabel lblNSatellites = new JLabel("Sat. View");
		fixInformationsPanel.add(lblNSatellites);
		textFieldSatelittesView = new JTextField();
		textFieldSatelittesView.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldSatelittesView.setEditable(false);
		fixInformationsPanel.add(textFieldSatelittesView);

		JLabel lblSatUsed = new JLabel("Sat. Used");
		fixInformationsPanel.add(lblSatUsed);
		textFieldSatelittesUsed = new JTextField();
		textFieldSatelittesUsed.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldSatelittesUsed.setEditable(false);
		fixInformationsPanel.add(textFieldSatelittesUsed);

		JLabel lblHdop = new JLabel("HDOP");
		fixInformationsPanel.add(lblHdop);
		textFieldHDOP = new JTextField();
		textFieldHDOP.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldHDOP.setEditable(false);
		fixInformationsPanel.add(textFieldHDOP);

		JLabel lblPdop = new JLabel("PDOP");
		fixInformationsPanel.add(lblPdop);
		textFieldPDOP = new JTextField();
		textFieldPDOP.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldPDOP.setEditable(false);
		fixInformationsPanel.add(textFieldPDOP);

		JLabel lblVdop = new JLabel("VDOP");
		fixInformationsPanel.add(lblVdop);
		textFieldVDOP = new JTextField();
		textFieldVDOP.setPreferredSize(new Dimension(TEXTFIELD_SIZE,1));
		textFieldVDOP.setEditable(false);
		fixInformationsPanel.add(textFieldVDOP);

		return fixInformationsPanel;
	}

	public void displayData(GPSData data) {
		textFieldLatitude.setText(data.getLatitude());
		textFieldLongitude.setText(data.getLongitude());

		textFieldSatelittesView.setText(Integer.toString(data
				.getNumberOfSatellitesInView()));
		textFieldSatelittesUsed.setText(Integer.toString(data
				.getNumberOfSatellitesInUse()));
		textFieldHDOP.setText(Double.toString(data.getHDOP()));
		textFieldPDOP.setText(Double.toString(data.getPDOP()));
		textFieldVDOP.setText(Double.toString(data.getVDOP()));

		switch (data.getGPSSourceType()) {
		case 0:
			labelHasFix.setBackground(Color.RED);
			break;
		default:
			labelHasFix.setBackground(Color.GREEN);
			break;
		}

		textFieldVelKmh.setText(Double.toString(data.getGroundSpeedKmh()));
		textFieldOrientation.setText(Double.toString(data.getOrientation()));

		LocalDateTime date = data.getDate();
		textFieldTime.setText(date.getHourOfDay() + ":"
				+ date.getMinuteOfHour() + ":" + date.getSecondOfMinute() + ","
				+ date.getMillisOfSecond());

		repaint();
	}
	
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}
	
	public int getSleepTime() {
		return sleepTime;
	}
}