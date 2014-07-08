package gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import org.joda.time.LocalDateTime;

import dataObjects.GPSData;

public class GPS_Panel extends JPanel {
	private static final long serialVersionUID = 6535539451990270799L;
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

	public GPS_Panel() {
		setBorder(BorderFactory.createTitledBorder("GPS Data"));
		setLayout(null);

		buildCoordinatesPanel();
		buildNavitationInformationsPanel();
		buildGPSFixInformationPanel();
		buildTimePanel();

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBounds(246, 316, 89, 23);
		add(btnRefresh);

		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {
				"10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBox.setSelectedIndex(2);
		comboBox.setBounds(95, 317, 86, 20);
		add(comboBox);

		JLabel lblNewLabelRefreshRate = new JLabel("Refresh Rate");
		lblNewLabelRefreshRate.setBounds(20, 320, 72, 14);
		add(lblNewLabelRefreshRate);
	}

	private void buildCoordinatesPanel() {
		JPanel coordinatesPanel = new JPanel();
		coordinatesPanel.setBounds(10, 16, 181, 104);
		coordinatesPanel.setBorder(BorderFactory
				.createTitledBorder("Coordinates"));
		coordinatesPanel.setLayout(null);
		add(coordinatesPanel);

		JLabel lblLatitude = new JLabel("Latitude");
		lblLatitude.setBounds(10, 25, 65, 14);
		coordinatesPanel.add(lblLatitude);

		JLabel lblLongitude = new JLabel("Longitude");
		lblLongitude.setBounds(10, 50, 65, 14);
		coordinatesPanel.add(lblLongitude);

		JLabel lblAltitude = new JLabel("Altitude");
		lblAltitude.setBounds(10, 75, 65, 14);
		coordinatesPanel.add(lblAltitude);

		textFieldLatitude = new JTextField();
		textFieldLatitude.setEditable(false);
		textFieldLatitude.setBounds(85, 22, 86, 20);
		textFieldLatitude.setColumns(10);
		coordinatesPanel.add(textFieldLatitude);

		textFieldLongitude = new JTextField();
		textFieldLongitude.setEditable(false);
		textFieldLongitude.setBounds(85, 47, 86, 20);
		textFieldLongitude.setColumns(10);
		coordinatesPanel.add(textFieldLongitude);

		textFieldAltitude = new JTextField();
		textFieldAltitude.setEditable(false);
		textFieldAltitude.setBounds(85, 72, 86, 20);
		textFieldAltitude.setColumns(10);
		coordinatesPanel.add(textFieldAltitude);
	}

	private void buildNavitationInformationsPanel() {
		JPanel navigationPanel = new JPanel();
		navigationPanel.setBounds(10, 142, 181, 104);
		add(navigationPanel);
		navigationPanel.setBorder(BorderFactory
				.createTitledBorder("Navigation"));
		navigationPanel.setLayout(null);

		JLabel lblVelkmh = new JLabel("Vel. (Km/h)");
		lblVelkmh.setBounds(10, 25, 65, 14);
		navigationPanel.add(lblVelkmh);

		JLabel lblVelknots = new JLabel("Vel. (Knots)");
		lblVelknots.setBounds(10, 50, 65, 14);
		navigationPanel.add(lblVelknots);

		JLabel lblOrientation = new JLabel("Orientation");
		lblOrientation.setBounds(10, 75, 65, 14);
		navigationPanel.add(lblOrientation);

		textFieldVelKmh = new JTextField();
		textFieldVelKmh.setEditable(false);
		textFieldVelKmh.setBounds(85, 22, 86, 20);
		navigationPanel.add(textFieldVelKmh);
		textFieldVelKmh.setColumns(10);

		textFieldVelKnots = new JTextField();
		textFieldVelKnots.setEditable(false);
		textFieldVelKnots.setBounds(85, 47, 86, 20);
		navigationPanel.add(textFieldVelKnots);
		textFieldVelKnots.setColumns(10);

		textFieldOrientation = new JTextField();
		textFieldOrientation.setEditable(false);
		textFieldOrientation.setBounds(85, 72, 86, 20);
		navigationPanel.add(textFieldOrientation);
		textFieldOrientation.setColumns(10);
	}

	private void buildGPSFixInformationPanel() {
		JPanel fixInformationsPanel = new JPanel();
		fixInformationsPanel.setBounds(201, 16, 181, 230);
		fixInformationsPanel.setBorder(BorderFactory
				.createTitledBorder("Fix Informations"));
		fixInformationsPanel.setLayout(null);
		add(fixInformationsPanel);

		JLabel lblHasFix = new JLabel("Has Fix");
		lblHasFix.setBounds(10, 25, 46, 14);
		fixInformationsPanel.add(lblHasFix);

		JLabel lblFixType = new JLabel("Fix Type");
		lblFixType.setBounds(10, 50, 46, 14);
		fixInformationsPanel.add(lblFixType);

		JLabel lblNSatellites = new JLabel("Sat. View");
		lblNSatellites.setBounds(10, 75, 65, 14);
		fixInformationsPanel.add(lblNSatellites);

		JLabel lblSatUsed = new JLabel("Sat. Used");
		lblSatUsed.setBounds(10, 100, 65, 14);
		fixInformationsPanel.add(lblSatUsed);

		JLabel lblHdop = new JLabel("HDOP");
		lblHdop.setBounds(10, 125, 46, 14);
		fixInformationsPanel.add(lblHdop);

		JLabel lblPdop = new JLabel("PDOP");
		lblPdop.setBounds(10, 150, 46, 14);
		fixInformationsPanel.add(lblPdop);

		JLabel lblVdop = new JLabel("VDOP");
		lblVdop.setBounds(10, 175, 46, 14);
		fixInformationsPanel.add(lblVdop);

		JLabel lblGpsSource = new JLabel("GPS Source");
		lblGpsSource.setBounds(10, 200, 65, 14);
		fixInformationsPanel.add(lblGpsSource);

		textFieldHasFix = new JTextField();
		textFieldHasFix.setEditable(false);
		textFieldHasFix.setBounds(85, 22, 86, 20);
		textFieldHasFix.setColumns(10);
		fixInformationsPanel.add(textFieldHasFix);

		textFieldFixType = new JTextField();
		textFieldFixType.setEditable(false);
		textFieldFixType.setBounds(85, 47, 86, 20);
		textFieldFixType.setColumns(10);
		fixInformationsPanel.add(textFieldFixType);

		textFieldSatelittesView = new JTextField();
		textFieldSatelittesView.setEditable(false);
		textFieldSatelittesView.setBounds(85, 72, 86, 20);
		textFieldSatelittesView.setColumns(10);
		fixInformationsPanel.add(textFieldSatelittesView);

		textFieldSatelittesUsed = new JTextField();
		textFieldSatelittesUsed.setEditable(false);
		textFieldSatelittesUsed.setBounds(85, 97, 86, 20);
		textFieldSatelittesUsed.setColumns(10);
		fixInformationsPanel.add(textFieldSatelittesUsed);

		textFieldHDOP = new JTextField();
		textFieldHDOP.setEditable(false);
		textFieldHDOP.setBounds(85, 122, 86, 20);
		fixInformationsPanel.add(textFieldHDOP);
		textFieldHDOP.setColumns(10);

		textFieldPDOP = new JTextField();
		textFieldPDOP.setEditable(false);
		textFieldPDOP.setBounds(85, 147, 86, 20);
		fixInformationsPanel.add(textFieldPDOP);
		textFieldPDOP.setColumns(10);

		textFieldVDOP = new JTextField();
		textFieldVDOP.setEditable(false);
		textFieldVDOP.setBounds(85, 172, 86, 20);
		fixInformationsPanel.add(textFieldVDOP);
		textFieldVDOP.setColumns(10);

		textFieldGPSSource = new JTextField();
		textFieldGPSSource.setEditable(false);
		textFieldGPSSource.setBounds(85, 197, 86, 20);
		fixInformationsPanel.add(textFieldGPSSource);
		textFieldGPSSource.setColumns(10);
	}

	private void buildTimePanel() {
		JPanel timePanel = new JPanel();
		timePanel.setBounds(10, 257, 372, 57);
		timePanel.setBorder(BorderFactory.createTitledBorder("Time & Date"));
		timePanel.setLayout(null);
		add(timePanel);

		JLabel lblTime = new JLabel("Time");
		lblTime.setBounds(10, 25, 46, 14);
		timePanel.add(lblTime);

		JLabel lblDate = new JLabel("Date");
		lblDate.setBounds(200, 25, 46, 14);
		timePanel.add(lblDate);

		textFieldTime = new JTextField();
		textFieldTime.setEditable(false);
		textFieldTime.setBounds(85, 22, 86, 20);
		timePanel.add(textFieldTime);
		textFieldTime.setColumns(10);

		textFieldDate = new JTextField();
		textFieldDate.setEditable(false);
		textFieldDate.setBounds(276, 22, 86, 20);
		timePanel.add(textFieldDate);
		textFieldDate.setColumns(10);
	}

	public void displayData(GPSData data) {
		textFieldLatitude.setText(data.getLatitude());
		textFieldLongitude.setText(data.getLongitude());
		textFieldAltitude.setText(Double.toString(data.getAltitude()));

		textFieldHasFix.setText(Boolean.toString(data.isFix()));
		textFieldSatelittesView.setText(Integer.toString(data
				.getNumberOfSatellitesInView()));
		;
		textFieldSatelittesUsed.setText(Integer.toString(data
				.getNumberOfSatellitesInUse()));
		;
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
		;
		textFieldOrientation.setText(Double.toString(data.getOrientation()));
		;

		LocalDateTime date = data.getDate();
		textFieldTime.setText(date.getHourOfDay() + ":"
				+ date.getMinuteOfHour() + ":" + date.getSecondOfMinute() + ","
				+ date.getMillisOfSecond());
		textFieldDate.setText(date.getDayOfMonth() + "/"
				+ date.getMonthOfYear() + "/" + date.getYear());
	}
}
