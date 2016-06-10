package gui.panels.map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapObject;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Formation.FormationType;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.EntityMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import gui.DroneGUI;
import gui.panels.UpdatePanel;
import gui.utils.SortedListModel;
import threads.UpdateThread;

public class MapPanel extends UpdatePanel {
	private static final long serialVersionUID = 2367574368471038828L;
	private static int POSITION_HISTORY = 1;
	private static int SLEEP_TIME = 10;
	private static final int MAP_TOP_OFFSET = 75;
	protected static final int SHIFT_IDENTIFIER = 64;

	public enum MapStatus {
		NONE, WAYPOINT, GEOFENCE, OBSTACLE, FORMATION
	}

	private JMapViewerTreeDrone treeMap = null;

	private int robotMarkerIndex = 0;

	private HashMap<String, LinkedList<MapMarker>> robotPositions = new HashMap<String, LinkedList<MapMarker>>();
	private HashMap<String, Long> robotPositionsUpdate = new HashMap<String, Long>();

	private GeoFence geoFence = new GeoFence("geofence");
	private Formation formation = null;
	private LinkedList<Target> targets = new LinkedList<Target>();
	private LinkedList<MapMarker> targetMarkers = new LinkedList<MapMarker>();
	private LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
	private LinkedList<MapMarker> waypointMarkers = new LinkedList<MapMarker>();
	private LinkedList<ObstacleLocation> obstacles = new LinkedList<ObstacleLocation>();
	private LinkedList<MapMarker> obstacleMarkers = new LinkedList<MapMarker>();
	private LinkedList<String> selectedMarkerDrones = new LinkedList<String>();

	private Layer geoFenceLayer;
	private DroneGUI droneGUI;
	private MapStatus status = MapStatus.NONE;

	private JComboBox<MapStatus> mapStatusComboBox;
	private JButton helpButton;

	private HashMap<Integer, LinkedList<String>> groupsMap = new HashMap<Integer, LinkedList<String>>();

	private Point dragBoxStart;
	private Point dragBoxEnd;

	private UpdateThread thread = null;

	public MapPanel(DroneGUI droneGUI) {
		this();
		this.droneGUI = droneGUI;
	}

	public MapPanel() {
		treeMap = new JMapViewerTreeDrone("Zones");

		for (int i = 1; i < 4; i++)
			groupsMap.put(i, new LinkedList<String>());

		new RefreshDrones().start();

		setBorder(BorderFactory.createTitledBorder("Map"));
		setLayout(new BorderLayout());
		JPanel panelTop = new JPanel(new BorderLayout());

		add(panelTop, BorderLayout.NORTH);

		JPanel panelTopInformation = new JPanel(new GridLayout(2, 3));

		try {

			JComboBox<TileSource> tileSourceSelector = new JComboBox<>(
					new TileSource[] { new OfflineOsmTileSource((new File("tiles").toURI().toURL()).toString(), 1, 19),
							new OsmTileSource.Mapnik(), new BingAerialTileSource(), new MapQuestOsmTileSource() });
			tileSourceSelector.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					getMap().setTileSource((TileSource) e.getItem());
				}
			});

			getMap().setTileSource(tileSourceSelector.getItemAt(0));

			panelTopInformation.add(tileSourceSelector);

			try {
				getMap().setTileLoader(new OsmFileCacheTileLoader(getMap()));
			} catch (IOException e) {
				getMap().setTileLoader(new OsmTileLoader(getMap()));
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		helpButton = new JButton("Help");
		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
						"<html><strong>W</strong>:  Add Waypoints <br>" + "<strong>G</strong>: Add GeoFence <br>"
								+ "<strong>O</strong>: Add Obstacles <br>" + "<strong>T</strong>: Add Target <br>"
								+ "<strong>Ctrl+OBJECT_KEY</strong>: Clear Objects <br>"
								+ "<strong>F</strong>: Fit Markers <br>" + "<strong>+</strong>: Zoom In <br>"
								+ "<strong>-</strong>: Zoom Out <br>" + "<strong>Ctrl+C</strong>: Cancel Option <br>"
								+ "<strong>Ctrl+E</strong>: Clear All Objects</html>");
			}
		});
		panelTopInformation.add(helpButton);

		JLabel mapStatusLabel = new JLabel("Active Mode: ");
		panelTopInformation.add(mapStatusLabel);

		mapStatusComboBox = new JComboBox<MapStatus>(MapStatus.values());
		mapStatusComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					status = (MapStatus) e.getItem();
				}
			}
		});

		mapStatusComboBox.setPreferredSize(new Dimension(200, 10));
		panelTopInformation.add(mapStatusComboBox);

		panelTop.add(panelTopInformation);

		JButton hidePanelButton = new JButton(">>");
		hidePanelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton pressedButton = (JButton) e.getSource();

				if (pressedButton.getText().equals(">>")) {
					pressedButton.setText("<<");
					droneGUI.hideRightPanel();
				} else {
					pressedButton.setText(">>");
					droneGUI.showRightPanel();
				}
			}
		});

		Font font = hidePanelButton.getFont();
		int width = hidePanelButton.getFontMetrics(font).stringWidth(hidePanelButton.getText());
		hidePanelButton.setPreferredSize(new Dimension(width + 40, 30));
		panelTop.add(hidePanelButton, BorderLayout.EAST);

		add(treeMap, BorderLayout.CENTER);

		geoFenceLayer = treeMap.addLayer("_GeoFence");
		initActions();

		// Lisbon
		getMap().setDisplayPosition(new Coordinate(38.7166700, -9.1333300), 13);

		getMap().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (status.equals(MapStatus.NONE)) {
						JTextPane selectedTextField = droneGUI.getCommandPanel().getSelectedDronesTextField();

						if (e.getModifiersEx() != SHIFT_IDENTIFIER) {
							cleanSelectedMarkersList();
							selectedTextField.setText("");
						}

						Coordinate clickCoord = getMap().getPosition(e.getPoint());
						Vector2d clickPosition = CoordinateUtilities
								.GPSToCartesian(new LatLon(clickCoord.getLat(), clickCoord.getLon()));

						MapMarkerDrone closestMarker = getClosestMarker(clickPosition);

						if (droneGUI != null && closestMarker != null
								&& !selectedMarkerDrones.contains(closestMarker.getName()))
							selectDroneMarker(selectedTextField, closestMarker);

						repaint();
					} else if (status.equals(MapStatus.OBSTACLE)) {
						addObstacle(getMap().getPosition(e.getPoint()));
					} else if (status.equals(MapStatus.GEOFENCE)) {
						addToGeoFence(getMap().getPosition(e.getPoint()));
					} else if (status.equals(MapStatus.FORMATION)) {
						addFormation(getMap().getPosition(e.getPoint()));
					} else {
						addWaypoint(getMap().getPosition(e.getPoint()));
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (status.equals(MapStatus.NONE) && e.getButton() == MouseEvent.BUTTON1) {
					JTextPane selectedTextField = droneGUI.getCommandPanel().getSelectedDronesTextField();

					if (e.getModifiersEx() != SHIFT_IDENTIFIER) {
						cleanSelectedMarkersList();
						selectedTextField.setText("");
					}

					if (dragBoxStart != null && dragBoxEnd != null) {
						Coordinate mapStartPosition = getMap().getPosition(dragBoxStart);
						Vector2d start = CoordinateUtilities
								.GPSToCartesian(new LatLon(mapStartPosition.getLat(), mapStartPosition.getLon()));
						Coordinate mapEndPosition = getMap().getPosition(dragBoxEnd);
						Vector2d end = CoordinateUtilities
								.GPSToCartesian(new LatLon(mapEndPosition.getLat(), mapEndPosition.getLon()));

						Vector2d min = new Vector2d(Math.min(start.x, end.x), Math.min(start.y, end.y));
						Vector2d max = new Vector2d(Math.max(start.x, end.x), Math.max(start.y, end.y));

						ArrayList<MapMarkerDrone> selectedMarkers = getMarkersBetween(min, max);
						if (droneGUI != null) {
							if (!selectedMarkers.isEmpty()) {
								for (MapMarkerDrone m : selectedMarkers) {
									selectDroneMarker(selectedTextField, m);
								}
							}
						}

						dragBoxStart = null;
						dragBoxEnd = null;
						repaint();
					}

				}
			}
		});

		getMap().addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				boolean cursorHand = getMap().getAttribution().handleAttributionCursor(p);
				if (cursorHand) {
					getMap().setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					getMap().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (status.equals(MapStatus.NONE) && e.getButton() == MouseEvent.BUTTON1) {
					if (dragBoxStart == null)
						dragBoxStart = new Point(e.getPoint().x, e.getPoint().y);
					else
						dragBoxEnd = new Point(e.getPoint().x, e.getPoint().y);

					repaint();
				}
			};
		});

	}

	private class RefreshDrones extends Thread {
		long timeToDelete = 1000 * 5;// 5 sec

		@Override
		public void run() {
			while (true) {

				for (String s : robotPositionsUpdate.keySet()) {
					if (System.currentTimeMillis() - robotPositionsUpdate.get(s) > timeToDelete)
						clearRobot(s);
				}

				try {
					Thread.sleep(timeToDelete);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/*
	 * GUI stuff
	 */
	protected void initActions() {
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");
		this.getActionMap().put("+", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().zoomIn();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");
		this.getActionMap().put("-", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().zoomOut();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control E"), "control E");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta E"), "control E");
		this.getActionMap().put("control E", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearWaypoints();
				clearGeoFence();
				clearObstacles();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "control C");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta C"), "control C");
		this.getActionMap().put("control C", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F"), "F");
		this.getActionMap().put("F", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().setDisplayToFitMapMarkers();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "W");
		this.getActionMap().put("W", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				status = MapStatus.WAYPOINT;
				mapStatusComboBox.setSelectedItem(status);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control W"), "control W");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta W"), "control W");
		this.getActionMap().put("control W", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearWaypoints();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("G"), "G");
		this.getActionMap().put("G", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				status = MapStatus.GEOFENCE;
				mapStatusComboBox.setSelectedItem(status);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control G"), "control G");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta G"), "control G");
		this.getActionMap().put("control G", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearGeoFence();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("O"), "O");
		this.getActionMap().put("O", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				status = MapStatus.OBSTACLE;
				mapStatusComboBox.setSelectedItem(status);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control O"), "control O");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta O"), "control O");
		this.getActionMap().put("control O", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearObstacles();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F"), "F");
		this.getActionMap().put("F", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				status = MapStatus.FORMATION;
				mapStatusComboBox.setSelectedItem(status);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "control F");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta F"), "control F");
		this.getActionMap().put("control F", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				clearFormation();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("1"), "1");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("1"), "1");
		this.getActionMap().put("1", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				selectDronesFromGroup(1);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control 1"), "control 1");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta 1"), "control 1");
		this.getActionMap().put("control 1", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				saveDronesGroup(1);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("2"), "2");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("2"), "2");
		this.getActionMap().put("2", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				selectDronesFromGroup(2);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control 2"), "control 2");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta 2"), "control 2");
		this.getActionMap().put("control 2", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				saveDronesGroup(2);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("3"), "3");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("3"), "3");
		this.getActionMap().put("3", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				selectDronesFromGroup(3);
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control 3"), "control 3");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta 3"), "control 3");
		this.getActionMap().put("control 3", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				saveDronesGroup(3);
			}
		});
	}

	public synchronized void updateRobotPosition(RobotLocation di) {

		LatLon latLon = di.getLatLon();

		double lat = latLon.getLat();
		double lon = latLon.getLon();

		if (lat == 0 && lon == 0)
			return;

		double orientation = di.getOrientation();

		String name = di.getName().isEmpty() ? "drone" : di.getName();

		Layer l = null;

		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);

		if (robotMarkers == null) {
			robotMarkers = new LinkedList<MapMarker>();
			robotPositions.put(name, robotMarkers);
			robotPositionsUpdate.put(name, System.currentTimeMillis());
		}

		Iterator<MapMarker> i = getMap().getMapMarkerList().iterator();

		while (i.hasNext()) {
			MapMarker m = i.next();
			if (m.getLayer() != null && m.getLayer().getName().equals(name)) {
				l = m.getLayer();
				break;
			}
		}

		if (l == null) {
			l = treeMap.addLayer(name);
		}

		boolean markerSelected = false;

		if (!robotMarkers.isEmpty()) {

			Style styleOld = new Style(Color.BLACK, Color.LIGHT_GRAY, new BasicStroke(1),
					new Font("Dialog", Font.PLAIN, 0));

			// remove last value from previous iteration
			MapMarker last = robotMarkers.pollLast();
			treeMap.removeFromLayer(last);
			getMap().removeMapMarker(last);
			if (((MapMarkerDrone) last).isSelected())
				markerSelected = true;

			// add that same one with a different style
			MapMarker old = new MapMarkerDot(l, "" + robotMarkerIndex++, last.getCoordinate(), styleOld);
			robotMarkers.add(old);
			l.add(old);
			getMap().addMapMarker(old);
			robotPositionsUpdate.put(name, System.currentTimeMillis());

		}

		Style styleNew = null;

		switch (di.getDroneType()) {
		case DRONE:
			styleNew = new Style(Color.RED, Color.GREEN, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
			break;
		case ENEMY:
			styleNew = new Style(Color.RED, Color.RED, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
			break;
		default:
			styleNew = new Style(Color.RED, Color.GREEN, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
		}

		MapMarker m = new MapMarkerDrone(l, name, latLonToCoord(lat, lon), styleNew, orientation);
		((MapMarkerDrone) m).setSelected(markerSelected);
		l.add(m);
		getMap().addMapMarker(m);
		robotMarkers.add(m);

		while (robotMarkers.size() > POSITION_HISTORY) {
			MapMarker old = robotMarkers.pollFirst();
			treeMap.removeFromLayer(old);
			getMap().removeMapMarker(old);
		}

		if (robotMarkers.size() == 1 && robotPositions.size() == 1) {
			// map().setDisplayToFitMapMarkers();
		}
	}

	public void displayData(EntityMessage message) {
		// TODO
		System.out.println("TODO MapPanel");
	}

	public void displayData(RobotLocation di) {
		LatLon latLon = di.getLatLon();

		if (latLon != null) {
			double lat = latLon.getLat();
			double lon = latLon.getLon();

			if (lat == 0 && lon == 0)
				return;

			if (usefulRobotCoordinate(di.getName(), latLonToCoord(lat, lon))) {
				updateRobotPosition(di);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g.create();

		if (dragBoxStart != null && dragBoxEnd != null) {
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0));

			int width = Math.abs(dragBoxEnd.x - dragBoxStart.x);
			int height = Math.abs((dragBoxEnd.y + MAP_TOP_OFFSET) - (dragBoxStart.y + MAP_TOP_OFFSET));

			g2d.drawRect(Math.min(dragBoxStart.x, dragBoxEnd.x),
					Math.min((dragBoxStart.y + MAP_TOP_OFFSET), (dragBoxEnd.y + MAP_TOP_OFFSET)), width, height);
		}
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	public UpdateThread getThread() {
		return thread;
	}

	@Override
	public synchronized void threadWait() {
		while (true) {
			try {
				wait();
			} catch (Exception e) {
			}
		}
	}

	private void updateCommandPanel() {
		if (droneGUI != null)
			droneGUI.getCommandPanel().updateMapInfo(getEntities());
	}

	private void selectDroneMarker(JTextPane selectedTextField, MapMarkerDrone m) {
		m.setSelected(true);
		selectedMarkerDrones.add(m.getName());
		String[] split = m.getName().split("\\.");
		String id = split[split.length - 1];

		if (Integer.valueOf(id) > 99) {
			SortedListModel listModel = droneGUI.getConnectionPanel().getListModel();

			for (int i = 0; i < listModel.getSize(); i++) {
				String[] tmp = listModel.get(i).getIp().split("\\.");
				String newID = tmp[tmp.length - 1];

				if (Integer.valueOf(newID) > 99)
					id = newID;
			}
		}

		String rsl = selectedTextField.getText();
		if (!selectedTextField.getText().contains(id)) {
			if (rsl.isEmpty())
				selectedTextField.setText(id);
			else
				selectedTextField.setText(rsl + "," + id);
		}
	}

	/*
	 * Getters and setters
	 */
	@Override
	public long getSleepTime() {
		return SLEEP_TIME;
	}

	public ArrayList<Entity> getEntities() {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		if (!geoFence.getWaypoints().isEmpty())
			entities.add(geoFence);

		if (waypoints != null)
			entities.addAll(waypoints);

		if (obstacles != null)
			entities.addAll(obstacles);

		if (formation != null) {
			entities.add(formation);
		}

		return entities;
	}

	private JMapViewer getMap() {
		return treeMap.getViewer();
	}

	private MapMarkerDrone getClosestMarker(Vector2d clickPosition) {
		MapMarkerDrone closestMarker = null;
		double closestDistance = 5;

		for (MapMarker marker : getMap().getMapMarkerList()) {
			if (marker instanceof MapMarkerDrone) {
				MapMarkerDrone markerDrone = (MapMarkerDrone) marker;
				Vector2d robotPosition = CoordinateUtilities
						.GPSToCartesian(new LatLon(markerDrone.getLat(), markerDrone.getLon()));
				double dist = robotPosition.distanceTo(clickPosition);

				if (dist < closestDistance) {
					closestMarker = markerDrone;
					closestDistance = dist;
				}
			}
		}

		return closestMarker;
	}

	private ArrayList<MapMarkerDrone> getMarkersBetween(Vector2d start, Vector2d end) {
		ArrayList<MapMarkerDrone> mrks = new ArrayList<MapMarkerDrone>();

		for (MapMarker m : getMap().getMapMarkerList()) {
			if (m instanceof MapMarkerDrone) {
				MapMarkerDrone mD = (MapMarkerDrone) m;
				Vector2d dP = CoordinateUtilities.GPSToCartesian(new LatLon(mD.getLat(), mD.getLon()));

				if (dP.x > start.x && dP.x < end.x && dP.y > start.y && dP.y < end.y)
					mrks.add(mD);
			}
		}

		return mrks;
	}

	/*
	 * Add/ replace entities
	 */
	public synchronized void addWaypoint(Waypoint wp) {
		addWaypoint(latLonToCoord(wp.getLatLon().getLat(), wp.getLatLon().getLon()));
	}

	public synchronized void addWaypoint(Coordinate c) {
		String layerName = "waypoints";
		Layer l = null;

		for (Layer layer : treeMap.getLayers()) {
			if (layer.getName().equals(layerName)) {
				l = layer;
			}
		}
		if (l == null) {
			l = treeMap.addLayer(layerName);
		}

		String markerName = "waypoint" + waypoints.size();

		MapMarker m = new MapMarkerWaypoint(l, markerName, c);

		l.add(m);
		waypointMarkers.add(m);

		getMap().addMapMarker(m);

		synchronized (this) {
			Waypoint waypoint = new Waypoint(markerName, new LatLon(c.getLat(), c.getLon()));
			waypoints.add(waypoint);
			updateCommandPanel();
			// notifyAll();
		}
	}

	private synchronized void addToGeoFence(Coordinate coord) {
		MapMarker marker = new MapMarkerDot(coord);
		geoFence.addWaypoint(new LatLon(coord.getLat(), coord.getLon()));
		geoFenceLayer.add(marker);
		getMap().addMapMarker(marker);

		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

		for (Waypoint wp : geoFence.getWaypoints())
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));

		getMap().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		getMap().addMapPolygon(po);
		updateCommandPanel();
	}

	public synchronized void addGeoFence(GeoFence geo) {
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

		for (Waypoint wp : geo.getWaypoints()) {
			System.out.println("GEOFENCE " + wp);
			Coordinate coord = latLonToCoord(wp.getLatLon().getLat(), wp.getLatLon().getLon());

			MapMarker marker = new MapMarkerDot(coord);
			geoFence.addWaypoint(new LatLon(coord.getLat(), coord.getLon()));
			geoFenceLayer.add(marker);
			getMap().addMapMarker(marker);
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));
		}

		getMap().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		getMap().addMapPolygon(po);
		updateCommandPanel();
	}

	public synchronized void addFormation(Coordinate c) {
		Formation formation = buildFormation(c);
		if (formation != null) {
			clearFormation();
			String layerName = "formation";
			Layer layer = null;

			for (Layer l : treeMap.getLayers()) {
				if (l.getName().equals(layerName)) {
					layer = l;
					break;
				}
			}

			if (layer == null) {
				layer = treeMap.addLayer(layerName);
			}

			clearFormation();

			for (Target t : formation.getTargets()) {
				Coordinate position = new Coordinate(t.getLatLon().getLat(), t.getLatLon().getLon());
				String name = t.getName().replace("formation_target_", "");

				MapMarker marker = new MapMarkerWaypoint(layer, name, position, Color.BLUE);
				layer.add(marker);
				targetMarkers.add(marker);
				getMap().addMapMarker(marker);
			}

			synchronized (this) {
				targets.addAll(formation.getTargets());
				updateCommandPanel();
			}
		}
	}

	private void addObstacle(Coordinate c) {
		String layerName = "obstacles";

		Layer l = null;

		for (Layer layer : treeMap.getLayers())
			if (layer.getName().equals("obstacles"))
				l = layer;

		if (l == null) {
			l = treeMap.addLayer(layerName);
		}

		String markerName = "obstacle" + obstacles.size();

		MapMarker m = new MapMarkerObstacle(l, markerName, c);

		l.add(m);
		obstacleMarkers.add(m);

		getMap().addMapMarker(m);

		synchronized (this) {
			ObstacleLocation ol = new ObstacleLocation(markerName, new LatLon(c.getLat(), c.getLon()));
			obstacles.add(ol);
			updateCommandPanel();
		}
	}

	public void replaceEntities(ArrayList<Entity> entities) {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		clearFormation();

		for (Entity e : entities) {
			if (e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity) e;
				if (ge instanceof Waypoint)
					addWaypoint(latLonToCoord(ge.getLatLon().getLat(), ge.getLatLon().getLon()));
				if (ge instanceof ObstacleLocation)
					addObstacle(latLonToCoord(ge.getLatLon().getLat(), ge.getLatLon().getLon()));
				if (ge instanceof Formation)
					addFormation(latLonToCoord(ge.getLatLon().getLat(), ge.getLatLon().getLon()));
			}
			if (e instanceof GeoFence) {
				addGeoFence((GeoFence) e);
			}
		}

		updateCommandPanel();
	}

	/*
	 * Clears
	 */
	private synchronized void clearRobot(String name) {
		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
		Iterator<MapMarker> i = getMap().getMapMarkerList().iterator();
		Layer l = null;

		while (i.hasNext()) {
			MapMarker m = i.next();
			if (m.getLayer() != null && m.getLayer().getName().equals(name)) {
				l = m.getLayer();
				break;
			}
		}

		if (robotMarkers != null & l != null && !robotMarkers.isEmpty()) {

			i = robotMarkers.iterator();

			while (i.hasNext()) {
				MapMarker m = i.next();
				treeMap.removeFromLayer(m);
				getMap().removeMapMarker(m);
				i.remove();
			}
		}
	}

	public void clearEntities() {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		clearFormation();
		updateCommandPanel();
	}

	public void clearWaypoints() {
		for (MapMarker m : waypointMarkers) {
			treeMap.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		waypoints.clear();
		waypointMarkers.clear();

		updateCommandPanel();
	}

	public void clearFormation() {
		for (MapMarker m : targetMarkers) {
			treeMap.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		targets.clear();
		targetMarkers.clear();
		formation = null;

		updateCommandPanel();
	}

	private synchronized void clearObstacles() {
		for (MapMarker m : obstacleMarkers) {
			treeMap.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		obstacles.clear();
		obstacleMarkers.clear();
		updateCommandPanel();
	}

	public synchronized void clearGeoFence() {
		LinkedList<MapMarker> list = new LinkedList<MapMarker>();

		if (geoFenceLayer.getElements() != null) {
			for (MapObject mo : geoFenceLayer.getElements()) {
				if (mo instanceof MapMarker) {
					list.add((MapMarker) mo);
					getMap().removeMapMarker((MapMarker) mo);
				}
			}

			for (MapMarker m : list)
				treeMap.removeFromLayer(m);

			geoFenceLayer.getElements().clear();
			geoFence.clear();
		}
		getMap().removeAllMapPolygons();
		updateCommandPanel();
	}

	private void clearStatus() {
		status = MapStatus.NONE;
		mapStatusComboBox.setSelectedItem(status);
	}

	public void clearHistory() {
		for (String s : robotPositions.keySet()) {
			LinkedList<MapMarker> robotMarkers = robotPositions.get(s);

			while (!robotMarkers.isEmpty()) {
				MapMarker old = robotMarkers.pollFirst();
				treeMap.removeFromLayer(old);
				getMap().removeMapMarker(old);
			}
		}
	}

	private synchronized void cleanSelectedMarkersList() {
		for (String selectedDroneIP : selectedMarkerDrones) {
			MapMarkerDrone marker = (MapMarkerDrone) robotPositions.get(selectedDroneIP).peek();
			marker.setSelected(false);
		}

		selectedMarkerDrones.clear();
		repaint();
	}

	/*
	 * Utils
	 */
	private void saveDronesGroup(int id) {
		LinkedList<String> group = groupsMap.get(id);
		group.addAll(selectedMarkerDrones);
		groupsMap.put(id, group);
	}

	private synchronized void selectDronesFromGroup(int id) {
		/*
		 * //THIS WAS CAUSING PROBLEMS WHEN TYPING NUMBERS IN THE TEXT FIELDS!
		 * cleanSelectedMarkersList(); JTextArea selectedTextField =
		 * droneGUI.getCommandPanel().getSelectedDronesTextField();
		 * selectedTextField.setText("");
		 * 
		 * LinkedList<String> group = groupsMap.get(id);
		 * 
		 * for (String droneIP : group) { MapMarkerDrone m =
		 * (MapMarkerDrone)robotPositions.get(droneIP).peek();
		 * if(isDroneConnected(m.getName()) || isVirtualDrone(m.getName()))
		 * selectDroneMarker(selectedTextField, m); }
		 */
	}

	public static Coordinate latLonToCoord(double lat, double lon) {
		return new Coordinate(lat, lon);
	}

	private boolean usefulRobotCoordinate(String name, Coordinate n) {
		if (n.getLat() == -1 && n.getLon() == -1)
			return false;

		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
		if (robotMarkers == null || robotMarkers.isEmpty()) {
			return true;
		}

		robotPositionsUpdate.put(name, System.currentTimeMillis());
		Coordinate c = robotMarkers.peekLast().getCoordinate();
		if (c.getLat() == n.getLat() && c.getLon() == n.getLon())
			return false;

		return true;
	}

	private Formation buildFormation(Coordinate c) {
		HashMap<String, String> args = readArgsFromFile("formationParameters.conf");

		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Insert formation parameters");
		Font font = label.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		label.setFont(boldFont);
		label.setHorizontalAlignment(JLabel.CENTER);
		panel.add(label, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

		JPanel topPanel = new JPanel(new GridLayout(0, 2));
		topPanel.add(new JLabel("Targets quantity: "));
		JTextField targetsQuantityTextField = new JTextField(10);
		targetsQuantityTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetsQuantity") != null) {
			targetsQuantityTextField.setText(args.get("targetsQuantity"));
		}
		topPanel.add(targetsQuantityTextField);

		topPanel.add(new JLabel("Formation shape"));
		JComboBox<FormationType> formationTypeComboBox = new JComboBox<FormationType>(FormationType.values());
		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		formationTypeComboBox.setRenderer(dlcr);
		int index = 0;
		if (args.get("formationShape") != null) {
			index = FormationType.valueOf(args.get("formationShape")).ordinal();
		}
		formationTypeComboBox.setSelectedIndex(index);
		topPanel.add(formationTypeComboBox);

		topPanel.add(new JLabel("Line formation delta: "));
		JTextField lineFormationDeltaTextField = new JTextField(10);
		lineFormationDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("lineFormationDelta") != null) {
			lineFormationDeltaTextField.setText(args.get("lineFormationDelta"));
		}
		topPanel.add(lineFormationDeltaTextField);

		topPanel.add(new JLabel("Arrow formation delta: "));
		JPanel deltasPanel = new JPanel(new GridLayout(1, 4));
		JLabel xLabel_1 = new JLabel("X=");
		xLabel_1.setHorizontalAlignment(JLabel.CENTER);
		deltasPanel.add(xLabel_1);
		JTextField arrowFormationXDeltaTextField = new JTextField(10);
		arrowFormationXDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("arrowFormation_xDelta") != null) {
			arrowFormationXDeltaTextField.setText(args.get("arrowFormation_xDelta"));
		}
		deltasPanel.add(arrowFormationXDeltaTextField);
		JLabel yLabel_1 = new JLabel("Y=");
		yLabel_1.setHorizontalAlignment(JLabel.CENTER);
		deltasPanel.add(yLabel_1);
		JTextField arrowFormationYDeltaTextField = new JTextField(10);
		arrowFormationYDeltaTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("arrowFormation_yDelta") != null) {
			arrowFormationYDeltaTextField.setText(args.get("arrowFormation_yDelta"));
		}
		deltasPanel.add(arrowFormationYDeltaTextField);
		topPanel.add(deltasPanel);

		topPanel.add(new JLabel("Circle formation radius: "));
		JTextField circleFormationRadiusTextField = new JTextField(10);
		circleFormationRadiusTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("circleFormation_radius") != null) {
			circleFormationRadiusTextField.setText(args.get("circleFormation_radius"));
		}
		topPanel.add(circleFormationRadiusTextField);
		optionsPanel.add(topPanel);

		JPanel dummyPanel_2 = new JPanel(new GridLayout(1, 1));
		JCheckBox variateFormationParametersCheckBox = new JCheckBox("Add noise to formation parameters");
		variateFormationParametersCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		if (args.get("noiseInParameters") != null) {
			variateFormationParametersCheckBox.setSelected((args.get("noiseInParameters").equals("1")));
		} else {
			variateFormationParametersCheckBox.setSelected(false);
		}
		dummyPanel_2.add(variateFormationParametersCheckBox);
		optionsPanel.add(dummyPanel_2);

		JPanel bottomPanel = new JPanel(new GridLayout(0, 2));
		bottomPanel.add(new JLabel("Initial rotation (º): "));
		JTextField initialRotationTextField = new JTextField(10);
		initialRotationTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("initialRotation") != null) {
			initialRotationTextField.setText(args.get("initialRotation"));
		}
		bottomPanel.add(initialRotationTextField);

		bottomPanel.add(new JLabel("Target radius: "));
		JTextField targetRadiusTextField = new JTextField(10);
		targetRadiusTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("targetRadius") != null) {
			targetRadiusTextField.setText(args.get("targetRadius"));
		}
		bottomPanel.add(targetRadiusTextField);

		bottomPanel.add(new JLabel("Random seed: "));
		JTextField randomSeedTextField = new JTextField(10);
		randomSeedTextField.setHorizontalAlignment(JTextField.CENTER);
		if (args.get("randomSeed") != null) {
			randomSeedTextField.setText(args.get("randomSeed"));
		}
		bottomPanel.add(randomSeedTextField);

		optionsPanel.add(bottomPanel);
		panel.add(optionsPanel, BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(360, 270));

		if (JOptionPane.showConfirmDialog(null, panel, "Formation parameters", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			try {
				int targetQuantity = Integer.parseInt(targetsQuantityTextField.getText());
				double lineFormationDelta = Double.parseDouble(lineFormationDeltaTextField.getText());
				double arrowFormationXDelta = Double.parseDouble(arrowFormationXDeltaTextField.getText());
				double arrowFormationYDelta = Double.parseDouble(arrowFormationYDeltaTextField.getText());
				double circleFormationRadius = Double.parseDouble(circleFormationRadiusTextField.getText());
				boolean variateFormationParameters = variateFormationParametersCheckBox.isSelected();
				double initialRotation = Double.parseDouble(initialRotationTextField.getText());
				double targetRadius = Double.parseDouble(targetRadiusTextField.getText());
				Long randomSeed = Long.parseLong(randomSeedTextField.getText());
				FormationType formationType = (FormationType) formationTypeComboBox.getSelectedItem();

				formation = new Formation("formation", new LatLon(c.getLat(), c.getLon()));
				formation.setLineFormationDelta(lineFormationDelta);
				formation.setArrowFormationDeltas(new Vector2d(arrowFormationXDelta, arrowFormationYDelta));
				formation.setCircleFormationRadius(circleFormationRadius);
				formation.setVariateFormationParameters(variateFormationParameters);
				formation.setInitialRotation(initialRotation * Math.PI / 180);
				formation.setRandom(new Random(randomSeed));
				formation.buildFormation(targetQuantity, formationType, targetRadius);

				return formation;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Illegal argument(s)!", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		} else {
			return null;
		}
	}

	private HashMap<String, String> readArgsFromFile(String fileName) {
		HashMap<String, String> args = new HashMap<String, String>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

			String line = br.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					String[] elements = line.trim().split("=");
					if (elements.length >= 2) {
						args.put(elements[0].trim(), elements[1].trim());
					}
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File %s not found!\n", getClass().getName(), fileName);
		} catch (IOException e) {
			System.err.printf("[%s] Error reading file %s!\n", getClass().getName(), fileName);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing file %s\n%s\n", getClass().getName(), fileName,
							e.getMessage());
				}
			}
		}

		return args;
	}
}
