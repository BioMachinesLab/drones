package gui.panels.map;

import gui.DroneGUI;
import gui.panels.UpdatePanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import threads.UpdateThread;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.network.messages.EntityMessage;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;

public class MapPanel extends UpdatePanel {
	
	private static final long serialVersionUID = 1L;
	private static int POSITION_HISTORY = 1;
	public enum MapStatus { WAYPOINT, GEOFENCE, OBSTACLE }

    private JMapViewerTreeDrone treeMap = null;
    
    private int robotMarkerIndex = 0;
    
    private HashMap<String,LinkedList<MapMarker>> robotPositions = new HashMap<String, LinkedList<MapMarker>>();
    private HashMap<String,Long> robotPositionsUpdate = new HashMap<String, Long>();
    private UpdateThread thread = null;
    
    private GeoFence geoFence = new GeoFence("geofence");
    private LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
    private LinkedList<MapMarker> waypointMarkers = new LinkedList<MapMarker>();
    private LinkedList<ObstacleLocation> obstacles = new LinkedList<ObstacleLocation>();
    private LinkedList<MapMarker> obstacleMarkers = new LinkedList<MapMarker>();
    
    private Layer geoFenceLayer;
	private DroneGUI droneGUI;
	private MapStatus status;
	
	private JLabel mapStatusResultLabel;
	
	public MapPanel(DroneGUI droneGUI) {
		this();
		this.droneGUI = droneGUI;
	}
	
    public MapPanel() {
		treeMap = new JMapViewerTreeDrone("Zones");
        
        new RefreshDrones().start();
        
        setBorder(BorderFactory.createTitledBorder("Map"));
        setLayout(new BorderLayout());
        JPanel panelTop = new JPanel(new GridLayout(2,3));

        add(panelTop, BorderLayout.NORTH);
        
        JPanel mapStatusPanel = new JPanel(new GridLayout(1, 2));
        JLabel mapSatursLabel = new JLabel("Active Mode: ");
        mapStatusResultLabel = new JLabel("NONE");
        mapStatusPanel.add(mapSatursLabel);
        mapStatusPanel.add(mapStatusResultLabel);
        
        try {
        
	        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] { new OfflineOsmTileSource((new File("tiles").toURI().toURL()).toString(),1,19) ,
	        		new OsmTileSource.Mapnik(), new BingAerialTileSource(), new MapQuestOsmTileSource()
	        });
	        tileSourceSelector.addItemListener(new ItemListener() {
	            public void itemStateChanged(ItemEvent e) {
	                map().setTileSource((TileSource) e.getItem());
	            }
	        });
	        
	        map().setTileSource(tileSourceSelector.getItemAt(0));
	        
	        panelTop.add(tileSourceSelector);
	        
	        try {
	        	map().setTileLoader(new OsmFileCacheTileLoader(map()));
	        } catch (IOException e) {
	        	map().setTileLoader(new OsmTileLoader(map()));
	        }
        
        } catch(MalformedURLException e) {
        	e.printStackTrace();
        }
        
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "<html><strong>W</strong>:  Add Waypoints <br><br>"
						+ "<strong>G</strong>: Add GeoFence <br><br>"
						+ "<strong>O</strong>: Add Obstacles <br><br>"
						+ "<strong>Ctrl+OBJECT_KEY</strong>: Clear Objects <br><br>"
						+ "<strong>F</strong>: Fit Markers  <br><br>"
						+ "<strong>+</strong>: Zoom In  <br><br>"
						+ "<strong>-</strong>: Zoom Out  <br><br>"
						+ "<strong>Ctrl+E</strong>: Clear All Objects</html>");
			}
		});
        panelTop.add(helpButton);
        
        panelTop.add(mapStatusPanel);
        
        add(treeMap, BorderLayout.CENTER);
        
        geoFenceLayer = treeMap.addLayer("_GeoFence");
        initActions();
        
        //Lisbon
        map().setDisplayPosition(new Coordinate(38.7166700,-9.1333300), 13);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	
               if(e.getButton() == MouseEvent.BUTTON1) {
            	   if (status == null){
            		   
            		   Coordinate clickCoord = map().getPosition(e.getPoint());
            		   Vector2d clickPosition = CoordinateUtilities.GPSToCartesian(new LatLon(clickCoord.getLat(), clickCoord.getLon()));
            		   
            		   for (MapMarker marker : map().getMapMarkerList()) {
            			   MapMarkerDrone robotMarker = (MapMarkerDrone)marker;
            			   Vector2d robotPosition = CoordinateUtilities.GPSToCartesian(new LatLon(robotMarker.getLat(), robotMarker.getLon()));
            			   
            			   if(robotPosition.distanceTo(clickPosition) < 5){
            				   robotMarker.setSelected(true);
            				   
            				   if(droneGUI != null){
            					   droneGUI.getCommandPanel().getSelectedDronesTextField().setText(robotMarker.getName());
            				   }
            				   
            				   break;
            			   }else{
            				   robotMarker.setSelected(false);
            				   droneGUI.getCommandPanel().getSelectedDronesTextField().setText("");;
            			   }
            		   }
            		   
            		   
            	   }else if(status.equals(MapStatus.OBSTACLE)){
            		   addObstacle(map().getPosition(e.getPoint()));
            	   }else if(status.equals(MapStatus.GEOFENCE)){
            		   addToGeoFence(map().getPosition(e.getPoint()));
            	   }else{
            		   addWaypoint(map().getPosition(e.getPoint()));
            	   }
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
    }
    
    protected void initActions() {
    	this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");
		this.getActionMap().put("+", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				map().zoomIn();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");
		this.getActionMap().put("-", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				map().zoomOut();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control E"), "control E");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta E"), "control E");
		this.getActionMap().put("control E", new AbstractAction(){   
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				clearWaypoints();
				clearGeoFence();
				clearObstacles();
			}
		});
    	this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "control C");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta C"), "control C");
		this.getActionMap().put("control C", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F"), "F");
		this.getActionMap().put("F", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				map().setDisplayToFitMapMarkers();
			}
		});
    	this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "W");
		this.getActionMap().put("W", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {
				status = MapStatus.WAYPOINT;
				mapStatusResultLabel.setText(status.toString());
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control W"), "control W");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta W"), "control W");
		this.getActionMap().put("control W", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				clearWaypoints();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("G"), "G");
		this.getActionMap().put("G", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				status = MapStatus.GEOFENCE;
				mapStatusResultLabel.setText(status.toString());
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control G"), "control G");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta G"), "control G");
		this.getActionMap().put("control G", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				clearGeoFence();
				clearStatus();
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("O"), "O");
		this.getActionMap().put("O", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				status = MapStatus.OBSTACLE;
				mapStatusResultLabel.setText(status.toString());
			}
		});
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control O"), "control O");
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta O"), "control O");
		this.getActionMap().put("control O", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				clearObstacles();
				clearStatus();
			}
		});
	}
    
    private void clearStatus() {
		status = null;
		mapStatusResultLabel.setText("NONE");
	}
    
    private JMapViewer map(){
        return treeMap.getViewer();
    }
    
    public static Coordinate c(double lat, double lon){
        return new Coordinate(lat, lon);
    }

    public synchronized void addWaypoint(Coordinate c) {
    	
    	String layerName = "waypoints";
    	
    	Layer l = null;
    	
    	for(Layer layer : treeMap.getLayers())
    		if(layer.getName().equals("waypoints"))
    			l = layer;

    	if(l == null) {
    		l = treeMap.addLayer(layerName);
    	}
    	
    	String markerName = "waypoint"+waypoints.size();
    	
    	MapMarker m = new MapMarkerWaypoint(l, markerName , c);
    	
    	l.add(m);
    	waypointMarkers.add(m);
    	
    	map().addMapMarker(m);
    	
    	synchronized(this) {
    		Waypoint waypoint = new Waypoint(markerName, new LatLon(c.getLat(),c.getLon()));
    		waypoints.add(waypoint);
    		updateCommandPanel();
//    		notifyAll();
    	}
    }
    
    public synchronized void updateRobotPosition(RobotLocation di) {
    	
    	LatLon latLon = di.getLatLon();
    	
		double lat = latLon.getLat();
		double lon = latLon.getLon();
		
		if(lat == 0 && lon == 0)
			return;
		
		double orientation = di.getOrientation();
		
		String name = di.getName().isEmpty() ? "drone" : di.getName();
    	
    	Layer l = null;
    	
    	LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
    	
    	if(robotMarkers == null) {
    		robotMarkers = new LinkedList<MapMarker>();
    		robotPositions.put(name, robotMarkers);
    		robotPositionsUpdate.put(name, System.currentTimeMillis());
    	}
    	
    	
    	Iterator<MapMarker> i = map().getMapMarkerList().iterator();
    	
    	while(i.hasNext()) {
    		MapMarker m = i.next();
    		if(m.getLayer() != null && m.getLayer().getName().equals(name)) {
    			l = m.getLayer();
    			break;
    		}
    	}

    	if(l == null) {
    		l = treeMap.addLayer(name);
    	}
    	
    	boolean markerSelected = false;
    	
    	if(!robotMarkers.isEmpty()) {

    		Style styleOld = new Style(Color.black, Color.LIGHT_GRAY, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 0));
	    	
	    	//remove last value from previous iteration
	    	MapMarker last = robotMarkers.pollLast();
	    	treeMap.removeFromLayer(last);
	    	map().removeMapMarker(last);
	    	if(((MapMarkerDrone)last).isSelected())
	    		markerSelected = true;

	    	//add that same one with a different style
	    	MapMarker old = new MapMarkerDot(l,""+robotMarkerIndex++,last.getCoordinate(), styleOld);
	    	robotMarkers.add(old);
	    	l.add(old);
	    	map().addMapMarker(old);
	    	robotPositionsUpdate.put(name, System.currentTimeMillis());
	    	
    	}
    	
    	Style styleNew = null;
    	
    	switch(di.getDroneType()) {
    		case DRONE:
    			styleNew = new Style(Color.RED, Color.green, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    			break;
    		case ENEMY:
    			styleNew = new Style(Color.RED, Color.RED, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    			break;
    		default:
    			styleNew = new Style(Color.RED, Color.green, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    	}
    	
    	MapMarker m = new MapMarkerDrone(l, name , c(lat,lon), styleNew, orientation);
    	((MapMarkerDrone)m).setSelected(markerSelected);
    	l.add(m);
    	map().addMapMarker(m);
    	robotMarkers.add(m);
    	
    	while(robotMarkers.size() > POSITION_HISTORY) {
    		MapMarker old = robotMarkers.pollFirst();
    		treeMap.removeFromLayer(old);
        	map().removeMapMarker(old);
    	}
    	
    	if(robotMarkers.size() == 1 && robotPositions.size() == 1) {
//    		map().setDisplayToFitMapMarkers();
    	}
    }
    
    public void displayData(RobotLocation di) {
		
    	LatLon latLon = di.getLatLon();
    	
		double lat = latLon.getLat();
		double lon = latLon.getLon();
		
		if(lat == 0 && lon == 0)
			return;
		
		if(usefulRobotCoordinate(di.getName(), c(lat,lon))) {
			updateRobotPosition(di);
		}
	}
	
	private boolean usefulRobotCoordinate(String name, Coordinate n) {
		
		if(n.getLat() == -1 && n.getLon() == -1)
			return false;
		
		LinkedList<MapMarker> robotMarkers = robotPositions.get(name); 
		
		if(robotMarkers == null || robotMarkers.isEmpty())
			return true;
		
		robotPositionsUpdate.put(name, System.currentTimeMillis());
		
		Coordinate c = robotMarkers.peekLast().getCoordinate();
		
		if(c.getLat() == n.getLat() && c.getLon() == n.getLon())
			return false;
		
		return true;
	}
	
	@Override
	public long getSleepTime() {
		return 10;
	}
	
	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;		
	}
	
	@Override
	public synchronized void threadWait() {
		while(true) {
			try {
				wait();
			} catch(Exception e){}
		}
	}

	public void displayData(EntityMessage message) {
		System.out.println("TODO MapPanel");
	}
	
	private synchronized void addToGeoFence(Coordinate coord) {
		MapMarker marker = new MapMarkerDot(coord);
		geoFence.addWaypoint(new LatLon(coord.getLat(),coord.getLon()));
		geoFenceLayer.add(marker);
		map().addMapMarker(marker);
		
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		
		for(Waypoint wp : geoFence.getWaypoints())
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));

		map().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		map().addMapPolygon(po);
		updateCommandPanel();
	}
	
	private synchronized void addGeoFence(GeoFence geo) {
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		
		for(Waypoint wp : geo.getWaypoints()) {
			Coordinate coord = c(wp.getLatLon().getLat(), wp.getLatLon().getLon());
			
			MapMarker marker = new MapMarkerDot(coord);
			geoFence.addWaypoint(new LatLon(coord.getLat(),coord.getLon()));
			geoFenceLayer.add(marker);
			map().addMapMarker(marker);
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));
		}

		map().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		map().addMapPolygon(po);
		updateCommandPanel();
	}

	
	public ArrayList<Entity> getEntities() {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		if(!geoFence.getWaypoints().isEmpty())
			entities.add(geoFence);
		
		if(waypoints != null)
			entities.addAll(waypoints);
		
		if(obstacles != null)
			entities.addAll(obstacles);
		
		return entities;
	}
	
	private void addObstacle(Coordinate c) {
		String layerName = "obstacles";
    	
    	Layer l = null;
    	
    	for(Layer layer : treeMap.getLayers())
    		if(layer.getName().equals("obstacles"))
    			l = layer;

    	if(l == null) {
    		l = treeMap.addLayer(layerName);
    	}
    	
    	String markerName = "obstacle"+obstacles.size();
    	
    	MapMarker m = new MapMarkerObstacle(l, markerName , c);
    	
    	l.add(m);
    	obstacleMarkers.add(m);
    	
    	map().addMapMarker(m);
    	
    	synchronized(this) {
    		ObstacleLocation ol = new ObstacleLocation(markerName, new LatLon(c.getLat(),c.getLon()));
    		obstacles.add(ol);
    		updateCommandPanel();
    	}
	}
	
	private synchronized void clearRobot(String name) {
		
		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
    	
    	Iterator<MapMarker> i = map().getMapMarkerList().iterator();
    	
    	Layer l = null;
    	
    	while(i.hasNext()) {
    		MapMarker m = i.next();
    		if(m.getLayer() != null && m.getLayer().getName().equals(name)) {
    			l = m.getLayer();
    			break;
    		}
    	}

    	if(robotMarkers != null & l != null && !robotMarkers.isEmpty()) {
    		
    		i = robotMarkers.iterator();
    		
    		while(i.hasNext()) {
    			MapMarker m = i.next();
    			treeMap.removeFromLayer(m);
    			map().removeMapMarker(m);
    			i.remove();
    		}
    	}
	}
	
	private void updateCommandPanel(){
		if(droneGUI != null)
			droneGUI.getCommandPanel().updateMapInfo(getEntities());
	}
	
	private synchronized void clearObstacles() {
		
		for(MapMarker m : obstacleMarkers) {
	    	treeMap.removeFromLayer(m);
	    	map().removeMapMarker(m);
		}
    	
    	obstacles.clear();
    	obstacleMarkers.clear();
    	updateCommandPanel();
	}
	
	private synchronized void clearGeoFence() {
		
		LinkedList<MapMarker> list = new LinkedList<MapMarker>();
		
		if(geoFenceLayer.getElements() != null) {
			for(MapObject mo : geoFenceLayer.getElements()) {
				if(mo instanceof MapMarker) {
					list.add((MapMarker)mo);
					map().removeMapMarker((MapMarker)mo);
				}
			}
			
			for(MapMarker m : list)
				treeMap.removeFromLayer(m);
			
			geoFenceLayer.getElements().clear();
			geoFence.clear();
		}
		map().removeAllMapPolygons();
		updateCommandPanel();
	}
	
	private void clearWaypoints() {
		
		for(MapMarker m : waypointMarkers) {
	    	treeMap.removeFromLayer(m);
	    	map().removeMapMarker(m);
		}
    	
    	waypoints.clear();
    	waypointMarkers.clear();
    	
    	updateCommandPanel();
	}
	
	public void clearHistory() {
		
		for(String s : robotPositions.keySet()) {
		
			LinkedList<MapMarker> robotMarkers = robotPositions.get(s);
		
			while(!robotMarkers.isEmpty()) {
	    		MapMarker old = robotMarkers.pollFirst();
	    		treeMap.removeFromLayer(old);
	        	map().removeMapMarker(old);
	    	}
		}
	}
	
	public void replaceEntities(ArrayList<Entity> entities) {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		
		for(Entity e : entities) {
			if(e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity)e;
				if(ge instanceof Waypoint)
					addWaypoint(c(ge.getLatLon().getLat(),ge.getLatLon().getLon()));
				if(ge instanceof ObstacleLocation)
					addObstacle(c(ge.getLatLon().getLat(),ge.getLatLon().getLon()));
			}
			if(e instanceof GeoFence)
				addGeoFence((GeoFence)e);
		}
		
		updateCommandPanel();
	}
	
	class RefreshDrones extends Thread {
		
		long timeToDelete = 1000*5;//5 sec
		
		@Override
		public void run() {
			while(true) {
				
				for(String s : robotPositionsUpdate.keySet()) {
					if(System.currentTimeMillis() - robotPositionsUpdate.get(s) > timeToDelete)
						clearRobot(s);
				}
				
				try {
					Thread.sleep(timeToDelete);
				} catch (InterruptedException e) {}
			}
		}
	}
}