package main;

import gui.panels.map.MapPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogData;

public class LogVisualizer extends JFrame {

	private static String FOLDER = "logs";
	private MapPanel map;
	private JSlider slider;
	private ArrayList<LogData> allData;
	private int currentStep = 0;
	private PlayThread playThread;
	private JLabel currentStepLabel;
	private DateTimeFormatter hourFormatter = DateTimeFormat
			.forPattern("HH:mm:ss.SS");
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	private String IPforEntities = "1";
	private int lastIncrementStep = 0;
	private JTextArea messageArea = new JTextArea();

	private boolean parserVersion = true;

	public static void main(String[] args) {
		new LogVisualizer();
	}

	public LogVisualizer() {

		try {
			if (!askParserVersion())
				System.exit(0);

			allData = readFile();
			Collections.sort(allData);
			
			for(LogData d : allData) {
				System.out.println(d.temperatures[1]+" "+d.GPSspeed+" "+d.temperatures[0]);
			}

			playThread = new PlayThread();
			playThread.start();

			buildGui();
			setVisible(true);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void buildGui() {
		map = new MapPanel();

		setLayout(new BorderLayout());

		add(map, BorderLayout.CENTER);

		slider = new JSlider(0, allData.size());
		slider.setValue(0);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				currentStep = slider.getValue();
				slider.setToolTipText("" + slider.getValue());
				if (!playThread.isPlaying())
					moveTo(slider.getValue());
			}
		});

		JButton playButton = new JButton("Play/Pause");
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.toggle();
			}
		});

		currentStepLabel = new JLabel();
		updateCurrentStepLabel();

		JButton slower = new JButton("Speed --");
		slower.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.playSlower();
			}
		});

		JButton faster = new JButton("Speed ++");
		faster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playThread.playFaster();
			}
		});

		JPanel controlsPanel = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();

		buttonsPanel.add(slower);
		buttonsPanel.add(playButton);
		buttonsPanel.add(faster);
		
		messageArea = new JTextArea();
		messageArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(messageArea);
		//scrollPane.setBounds(10,60,780,500);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JPanel southPanel = new JPanel(new GridLayout(2, 1));
		southPanel.add(buttonsPanel);
		southPanel.add(scrollPane);

		controlsPanel.add(currentStepLabel, BorderLayout.NORTH);
		controlsPanel.add(slider, BorderLayout.CENTER);
		controlsPanel.add(southPanel, BorderLayout.SOUTH);

		add(controlsPanel, BorderLayout.SOUTH);

		setSize(800, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2
				- getSize().height / 2);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err
					.println("Not able to set LookAndFeel for the current OS");
		}
	}

	private void moveTo(int step) {

		if (step > allData.size()) {
			playThread.pause();
			return;
		}

		currentStep = step;
		map.clearHistory();

		for (int i = 0; i < step; i++) {
			LogData d = allData.get(i);

			map.displayData(new RobotLocation(d.ip, d.latLon,
					d.compassOrientation, d.droneType));
			
			if(i == step -1)
				messageArea.setText("WATER TEMP: "+d.temperatures[1]+"\tCPU TEMP: "+d.temperatures[0]);

			//TODO
//			if (d.entities != null && d.ip.equals(IPforEntities)) {
//				map.replaceEntities(d.entities);
//			}
		}
		
		updateCurrentStepLabel();

	}

	private void incrementPlay() {

		if (currentStep + 1 > allData.size()) {
			playThread.pause();
			return;
		}

		if (lastIncrementStep == currentStep) {
			currentStep++;
			slider.setValue(currentStep);
			LogData d = allData.get(currentStep);

			map.displayData(new RobotLocation(d.ip, d.latLon,
					d.compassOrientation, d.droneType));
			
			messageArea.setText("WATER TEMP: "+d.temperatures[1]+"\tCPU TEMP: "+d.temperatures[0]);

			//TODO
//			if (d.entities != null && d.ip.equals(IPforEntities)) {
//				map.replaceEntities(d.entities);
//			}
			updateCurrentStepLabel();
		} else {
			moveTo(currentStep);
		}

		lastIncrementStep = currentStep;
	}

	private ArrayList<LogData> readFile() throws IOException {

		File folder = new File(FOLDER);
		if (!folder.exists()) {
			throw new IOException("Log folder doesn't exist!");
		}

		// If true, use the new version of the parser
		if (!parserVersion) {
			return parseOldLog(folder);
		} else {
			ArrayList<LogData> result = new ArrayList<LogData>();

			for (String file : folder.list()) {
				if (!file.contains(".log"))
					continue;

				System.out.println("Parsing file: " + file);
				Scanner s = new Scanner(new File(FOLDER + "/" + file));

				ArrayList<Entity> currentEntities = new ArrayList<Entity>();

				while (s.hasNext()) {
					String l = s.nextLine();

					if (!l.startsWith(LogCodex.COMMENT_CHAR) && !l.isEmpty()) {
						DecodedLog decodedData = LogCodex.decodeLog(l);
						
						if(decodedData == null)
							continue;

						switch (decodedData.payloadType()) {
						case ENTITIES:
							currentEntities = (ArrayList<Entity>) LogCodex
									.decodeLog(l, currentEntities).getPayload();
							break;

						case LOGDATA:
							LogData d = (LogData) decodedData.getPayload();
							//TODO
//							d.entities = currentEntities;
							result.add(d);
							break;
							
						case ERROR:
							messageArea.setForeground(Color.RED);
							messageArea.setText((String) decodedData.getPayload());
							break;

						case MESSAGE:
							messageArea.setForeground(Color.BLACK);
							messageArea.setText((String) decodedData.getPayload());
							break;
						}
					}
				}

				s.close();
			}
			return result;
		}
	}

	private ArrayList<LogData> parseOldLog(File folder)
			throws FileNotFoundException {
		ArrayList<LogData> result = new ArrayList<LogData>();
		DateTimeFormatter dtf = DateTimeFormat
				.forPattern("dd-MM-yyyy_HH:mm:ss.SS");

		for (String file : folder.list()) {

			System.out.println(file);

			if (!file.contains(".log"))
				continue;

			Scanner s = new Scanner(new File(FOLDER + "/" + file));

			String lastComment = "";
			ArrayList<LogData> data = new ArrayList<LogData>();

			int step = 0;

			String ip = "";

			ArrayList<Entity> currentEntities = new ArrayList<Entity>();

			while (s.hasNext()) {
				String l = s.nextLine();

				if (!l.startsWith("[") && !l.startsWith("#")
						&& !l.trim().isEmpty()) {

					Scanner sl = new Scanner(l);

					try {

						LogData d = new LogData();

						d.systemTime = sl.next();

						double lat = sl.nextDouble();
						double lon = sl.nextDouble();

						d.latLon = new LatLon(lat, lon);

						d.GPSorientation = sl.nextDouble();
						d.compassOrientation = sl.nextDouble();
						d.GPSspeed = sl.nextDouble();

						try {

							d.GPSdate = sl.next();

							double left = sl.nextDouble();
							double right = sl.nextDouble();

							d.motorSpeeds[0] = left;
							d.motorSpeeds[1] = right;

						} catch (Exception e) {
						}

						d.droneType = AquaticDroneCI.DroneType.valueOf(sl.next());

						d.comment = lastComment;

						d.timestep = step++;

						//TODO
//						d.entities = new ArrayList<Entity>();
//						d.entities.addAll(currentEntities);

						d.file = file;
						data.add(d);

					} catch (Exception e) {
						System.out.println(l);
						e.printStackTrace();
					}

					sl.close();

				} else if (l.startsWith("#")) {

					if (l.startsWith("#entity")) {
						parseEntity(l, currentEntities);
					}
					if (l.startsWith("#IP")) {
						ip = l.replace("#IP ", "").trim();
					} else
						lastComment = l.substring(1);
				}
			}

			System.out.println(step);
			
			if(ip.isEmpty())
				ip = "192.168.3.1";
			
			if(!ip.isEmpty()) {
				
				for(LogData d : data)
					d.ip = ip;

				result.addAll(data);
			}

			s.close();
		}

		return result;
	}

	private void parseEntity(String line, ArrayList<Entity> entities) {
		Scanner s = new Scanner(line);
		s.next();// ignore first token

		String event = s.next();

		if (event.equals("added")) {

			String className = s.next();

			String name = s.next();

			if (className.equals(GeoFence.class.getSimpleName())) {

				GeoFence fence = new GeoFence(name);

				int number = s.nextInt();

				for (int i = 0; i < number; i++) {
					double lat = s.nextDouble();
					double lon = s.nextDouble();
					fence.addWaypoint(new LatLon(lat, lon));
				}
				entities.add(fence);
			} else if (className.equals(Waypoint.class.getSimpleName())) {

				double lat = s.nextDouble();
				double lon = s.nextDouble();
				Waypoint wp = new Waypoint(name, new LatLon(lat, lon));
				entities.remove(wp);
				entities.add(wp);

			} else if (className.equals(ObstacleLocation.class.getSimpleName())) {

				double lat = s.nextDouble();
				double lon = s.nextDouble();

				double radius = s.nextDouble();
				entities.add(new ObstacleLocation(name, new LatLon(lat, lon),
						radius));
			}

		} else if (event.equals("removed")) {

			String name = s.next();

			Iterator<Entity> i = entities.iterator();
			while (i.hasNext()) {
				if (i.next().getName().equals(name)) {
					i.remove();
					break;
				}
			}
		}

		s.close();
	}

	private void updateCurrentStepLabel() {

		if (currentStep < allData.size()) {
			LogData d = allData.get(currentStep);

			String text = "Step: " + currentStep + "/" + allData.size();
			text += "\t Time: " + d.systemTime + " ("
					+ (1 / playThread.getMultiplier()) + "x)";
			currentStepLabel.setText(text);
		}
	}

	private long compareTimeWithNextStep() {

		if (currentStep + 1 < allData.size()) {
			DateTime d1 = DateTime.parse(allData.get(currentStep).systemTime,dateFormatter);
			DateTime d2 = DateTime.parse(allData.get(currentStep + 1).systemTime,dateFormatter);
			return d2.getMillis() - d1.getMillis();
		}

		return 0;
	}

	public class PlayThread extends Thread {

		private boolean play = false;
		private double multiplier = 1.0;

		@Override
		public void run() {

			while (true) {

				try {
					synchronized (this) {
						if (!play)
							wait();
					}

					incrementPlay();

					long time = (long) (compareTimeWithNextStep() * multiplier);

					if (time > 1000)
						time = 1000;

					Thread.sleep(time);

				} catch (Exception e) {
				}
			}
		}

		public synchronized void play() {
			play = true;
			notify();
		}

		public void toggle() {
			if (play)
				pause();
			else
				play();
		}

		public void pause() {
			play = false;
		}

		public void playFaster() {
			multiplier *= 0.5;
			updateCurrentStepLabel();
			interrupt();
		}

		public void playSlower() {
			if (multiplier < Math.pow(2, 4)) {
				multiplier *= 2;
				updateCurrentStepLabel();
				interrupt();
			}
		}

		public boolean isPlaying() {
			return play;
		}

		public double getMultiplier() {
			return multiplier;
		}
	}
	
	/*
	public static LogData convert(commoninterface.utils.logger.LogData data) {
		
		LogData log = new LogData();
		log.time = data.systemTime;
		log.date = DateTime.parse(data.systemTime,DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS"));
		log.file = data.file;
		log.compassOrientation = data.compassOrientation;
		log.ip = data.ip;
		if(data.temperatures != null) {
			log.cpuTemp = data.temperatures[0];
			log.waterTemp = data.temperatures[1];
		}
		log.timestep = data.timestep;
		log.latLon = data.latLon;
		log.GPSorientation = data.GPSorientation;
		log.GPSspeed = data.GPSspeed;
		log.leftSpeed = data.motorSpeeds[0];
		log.rightSpeed = data.motorSpeeds[1];
		log.lastComment = data.comment;
		log.droneType = data.droneType;
		log.inputs = data.inputNeuronStates;
		log.outputs = data.outputNeuronStates;
		
		return log;
	}

	public static class LogData implements Comparable<LogData> {
		String time;
		String file;
		String ip;
		int timestep;
		LatLon latLon;
		double GPSorientation;
		double compassOrientation;
		double GPSspeed;
		double leftSpeed;
		double rightSpeed;
		double cpuTemp;
		double waterTemp;
		double[] outputs;
		double[] inputs;
		DateTime date;
		String lastComment;
		AquaticDroneCI.DroneType droneType;
		ArrayList<Entity> entities = null;

		@Override
		public int compareTo(LogData o) {
			return date.compareTo(o.date);
		}
	}
*/
	private boolean askParserVersion() {
		JPanel dialogJPane = new JPanel(new GridLayout(2, 1));

		JRadioButton radioButton1 = new JRadioButton("Old (Mike's)");
		radioButton1.setSelected(true);

		JRadioButton radioButton2 = new JRadioButton("Big boss new");
		radioButton2.setSelected(false);

		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(radioButton1);
		radioButtonGroup.add(radioButton2);

		dialogJPane.add(radioButton1);
		dialogJPane.add(radioButton2);

		Object[] options = { "Ok" };
		int result = JOptionPane.showOptionDialog(null, dialogJPane,
				"Please select log parser", JOptionPane.PLAIN_MESSAGE,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		parserVersion = radioButton2.isSelected();

		return result == JOptionPane.OK_OPTION;
	}
}
