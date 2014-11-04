package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.joda.time.LocalDateTime;

import threads.UpdateThread;
import dataObjects.GPSData;

public class GPSPanel extends JPanel implements UpdatePanel {
	private static final long serialVersionUID = 6535539451990270799L;
	
	private static int TEXTFIELD_SIZE = 7;
	
	private JTextField textFieldLatitude;
	private JTextField textFieldLongitude;
	private JTextField textFieldAltitude;
	private JTextField textFieldHasFix;
	private JTextField textFieldFixType;
	private JTextField textFieldSatelittesView;
	private JTextField textFieldSatelittesUsed;
	private JTextField textFieldVelKmh;
	private JTextField textFieldVelKnots;
	private JTextField textFieldOrientation;
	private JTextField textFieldHDOP;
	private JTextField textFieldPDOP;
	private JTextField textFieldVDOP;
	private JTextField textFieldGPSSource;
	private JTextField textFieldTime;
	private JTextField textFieldDate;
	private JComboBox<String> comboBoxUpdateRate;

	private UpdateThread thread;
	
	private int sleepTime = 1000;

	public GPSPanel() {
		setBorder(BorderFactory.createTitledBorder("GPS Data"));
		setLayout(new BorderLayout());

		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());
		left.add(buildCoordinatesPanel(),BorderLayout.NORTH);
		left.add(buildNavitationInformationsPanel(),BorderLayout.CENTER);
		left.add(buildTimePanel(), BorderLayout.SOUTH);
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
		coordinatesPanel.setBorder(BorderFactory.createTitledBorder("Coordinates"));
		coordinatesPanel.setLayout(new GridLayout(3,2));

		coordinatesPanel.add(new JLabel("Latitude"));
		textFieldLatitude = new JTextField(TEXTFIELD_SIZE);
		textFieldLatitude.setEditable(false);
		coordinatesPanel.add(textFieldLatitude);
		
		coordinatesPanel.add(new JLabel("Longitude"));
		textFieldLongitude = new JTextField(TEXTFIELD_SIZE);
		textFieldLongitude.setEditable(false);
		coordinatesPanel.add(textFieldLongitude);

		coordinatesPanel.add(new JLabel("Altitude"));
		textFieldAltitude = new JTextField(TEXTFIELD_SIZE);
		textFieldAltitude.setEditable(false);
		coordinatesPanel.add(textFieldAltitude);
		
		return coordinatesPanel;
	}

	private JPanel buildNavitationInformationsPanel() {
		JPanel navigationPanel = new JPanel();
		navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));
		navigationPanel.setLayout(new GridLayout(3,2));

		JLabel lblVelkmh = new JLabel("Vel. (Km/h)");
		navigationPanel.add(lblVelkmh);
		textFieldVelKmh = new JTextField(TEXTFIELD_SIZE);
		textFieldVelKmh.setEditable(false);
		navigationPanel.add(textFieldVelKmh);

		JLabel lblVelknots = new JLabel("Vel. (Knots)");
		navigationPanel.add(lblVelknots);
		textFieldVelKnots = new JTextField(TEXTFIELD_SIZE);
		textFieldVelKnots.setEditable(false);
		navigationPanel.add(textFieldVelKnots);

		JLabel lblOrientation = new JLabel("Orientation");
		navigationPanel.add(lblOrientation);
		textFieldOrientation = new JTextField(TEXTFIELD_SIZE);
		textFieldOrientation.setEditable(false);
		navigationPanel.add(textFieldOrientation);
		
		return navigationPanel;
	}

	private JPanel buildGPSFixInformationPanel() {
		JPanel fixInformationsPanel = new JPanel();
		fixInformationsPanel.setBorder(BorderFactory
				.createTitledBorder("Fix Informations"));
		fixInformationsPanel.setLayout(new GridLayout(8,2));

		JLabel lblHasFix = new JLabel("Has Fix");
		fixInformationsPanel.add(lblHasFix);
		textFieldHasFix = new JTextField(TEXTFIELD_SIZE);
		textFieldHasFix.setEditable(false);
		fixInformationsPanel.add(textFieldHasFix);

		JLabel lblFixType = new JLabel("Fix Type");
		fixInformationsPanel.add(lblFixType);
		textFieldFixType = new JTextField(TEXTFIELD_SIZE);
		textFieldFixType.setEditable(false);
		fixInformationsPanel.add(textFieldFixType);

		JLabel lblNSatellites = new JLabel("Sat. View");
		fixInformationsPanel.add(lblNSatellites);
		textFieldSatelittesView = new JTextField(TEXTFIELD_SIZE);
		textFieldSatelittesView.setEditable(false);
		fixInformationsPanel.add(textFieldSatelittesView);

		JLabel lblSatUsed = new JLabel("Sat. Used");
		fixInformationsPanel.add(lblSatUsed);
		textFieldSatelittesUsed = new JTextField(TEXTFIELD_SIZE);
		textFieldSatelittesUsed.setEditable(false);
		fixInformationsPanel.add(textFieldSatelittesUsed);

		JLabel lblHdop = new JLabel("HDOP");
		fixInformationsPanel.add(lblHdop);
		textFieldHDOP = new JTextField(TEXTFIELD_SIZE);
		textFieldHDOP.setEditable(false);
		fixInformationsPanel.add(textFieldHDOP);

		JLabel lblPdop = new JLabel("PDOP");
		fixInformationsPanel.add(lblPdop);
		textFieldPDOP = new JTextField(TEXTFIELD_SIZE);
		textFieldPDOP.setEditable(false);
		fixInformationsPanel.add(textFieldPDOP);

		JLabel lblVdop = new JLabel("VDOP");
		fixInformationsPanel.add(lblVdop);
		textFieldVDOP = new JTextField(TEXTFIELD_SIZE);
		textFieldVDOP.setEditable(false);
		fixInformationsPanel.add(textFieldVDOP);

		JLabel lblGpsSource = new JLabel("GPS Source");
		fixInformationsPanel.add(lblGpsSource);
		textFieldGPSSource = new JTextField(TEXTFIELD_SIZE);
		textFieldGPSSource.setEditable(false);
		fixInformationsPanel.add(textFieldGPSSource);
		
		return fixInformationsPanel;
	}

	private JPanel buildTimePanel() {
		JPanel timePanel = new JPanel();
		timePanel.setBorder(BorderFactory.createTitledBorder("Time & Date"));
		timePanel.setLayout(new GridLayout(2,2));

		JLabel lblTime = new JLabel("Time");
		timePanel.add(lblTime);
		textFieldTime = new JTextField();
		textFieldTime.setEditable(false);
		timePanel.add(textFieldTime);

		JLabel lblDate = new JLabel("Date");
		timePanel.add(lblDate);
		textFieldDate = new JTextField();
		textFieldDate.setEditable(false);
		timePanel.add(textFieldDate);
		
		return timePanel;
	}

	public void displayData(GPSData data) {
		textFieldLatitude.setText(data.getLatitude());
		textFieldLongitude.setText(data.getLongitude());

		textFieldAltitude.setText(Double.toString(data.getAltitude()));

		textFieldHasFix.setText(Boolean.toString(data.isFix()));
		textFieldSatelittesView.setText(Integer.toString(data
				.getNumberOfSatellitesInView()));
		textFieldSatelittesUsed.setText(Integer.toString(data
				.getNumberOfSatellitesInUse()));
		textFieldHDOP.setText(Double.toString(data.getHDOP()));
		textFieldPDOP.setText(Double.toString(data.getPDOP()));
		textFieldVDOP.setText(Double.toString(data.getVDOP()));
		switch (data.getFixType()) {
		case 0:
			textFieldFixType.setText("Not Available");
			break;
		case 1:
			textFieldFixType.setText("2D");
			break;
		case 2:
			textFieldFixType.setText("3D");
			break;
		}

		switch (data.getGPSSourceType()) {
		case 0:
			textFieldGPSSource.setText("No fix");
			break;
		case 1:
			textFieldGPSSource.setText("GPS");
			break;
		case 2:
			textFieldGPSSource.setText("Diff. GPS");
			break;
		case 3:
			textFieldGPSSource.setText("Code " + data.getGPSSourceType() + "?");
			break;
		}

		textFieldVelKmh.setText(Double.toString(data.getGroundSpeedKmh()));
		textFieldVelKnots.setText(Double.toString(data.getGroundSpeedKnts()));
		textFieldOrientation.setText(Double.toString(data.getOrientation()));

		LocalDateTime date = data.getDate();
		textFieldTime.setText(date.getHourOfDay() + ":"
				+ date.getMinuteOfHour() + ":" + date.getSecondOfMinute() + ","
				+ date.getMillisOfSecond());
		textFieldDate.setText(date.getDayOfMonth() + "/"
				+ date.getMonthOfYear() + "/" + date.getYear());

		repaint();
	}
	
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}
	
	public int getSleepTime() {
		return sleepTime;
	}

}
