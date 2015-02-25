package gui.panels.map;

import gui.panels.UpdatePanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
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
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import network.messages.CompassMessage;
import network.messages.EntityMessage;
import objects.DroneLocation;
import objects.Waypoint;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import threads.UpdateThread;
import dataObjects.GPSData;

public class MapPanel extends UpdatePanel {
	
	private static final long serialVersionUID = 1L;

    private JMapViewerTreeDrone treeMap = null;

    private JButton fitMarkersButton;
    
    private static int POSITION_HISTORY = 100;
    
    private int robotMarkerIndex = 0;
    
    private LinkedList<MapMarker> robotPositions = new LinkedList<MapMarker>();
    private LinkedList<MapMarker> waypoints = new LinkedList<MapMarker>();
    
    private Waypoint droneWaypoint = null;
    private UpdateThread thread = null;

    /**
     * Constructs the {@code Demo}.
     * @throws MalformedURLException 
     */
    public MapPanel() {

        treeMap = new JMapViewerTreeDrone("Zones");
        
        // final JMapViewer map = new JMapViewer(new MemoryTileCache(),4);
        // map.setTileLoader(new OsmFileCacheTileLoader(map));
        // new DefaultMapController(map);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        JPanel panelTop = new JPanel();
        JPanel helpPanel = new JPanel();

        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
        JLabel helpLabel = new JLabel("Left mouse: move // Double left click or mouse wheel: zoom // Right click: add markers");
        helpPanel.add(helpLabel);
        fitMarkersButton = new JButton("Fit Markers");
        fitMarkersButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                map().setDisplayToFitMapMarkers();
            }
        });
        
        try {
        
	        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] { new OsmTileSource.Mapnik(),
	                new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource(),
	                new OfflineOsmTileSource((new File("tiles").toURI().toURL()).toString(),1,20) });
	        tileSourceSelector.addItemListener(new ItemListener() {
	            public void itemStateChanged(ItemEvent e) {
	                map().setTileSource((TileSource) e.getItem());
	            }
	        });
	        
	        panelTop.add(tileSourceSelector);
	        
	        try {
	        	map().setTileLoader(new OsmFileCacheTileLoader(map()));
	        } catch (IOException e) {
	        	map().setTileLoader(new OsmTileLoader(map()));
	        }
        
        } catch(MalformedURLException e) {
        	e.printStackTrace();
        }
        
        final JCheckBox showTreeLayers = new JCheckBox("Tree Layers");
        showTreeLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                treeMap.setTreeVisible(showTreeLayers.isSelected());
            }
        });
        panelTop.add(showTreeLayers);
        
        final JCheckBox showMapMarker = new JCheckBox("Markers");
        showMapMarker.setSelected(map().getMapMarkersVisible());
        showMapMarker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setMapMarkerVisible(showMapMarker.isSelected());
            }
        });
        panelTop.add(showMapMarker);
        ///
        final JCheckBox showZoomControls = new JCheckBox("Zoom Controls");
        showZoomControls.setSelected(map().getZoomContolsVisible());
        showZoomControls.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                map().setZoomContolsVisible(showZoomControls.isSelected());
            }
        });
        panelTop.add(showZoomControls);
        panelTop.add(fitMarkersButton);

        add(treeMap, BorderLayout.CENTER);

        /*
        LayerGroup germanyGroup = new LayerGroup("Germany");
        Layer germanyWestLayer = germanyGroup.addLayer("Germany West");
        Layer germanyEastLayer = germanyGroup.addLayer("Germany East");
        MapMarkerDot eberstadt = new MapMarkerDot(germanyEastLayer, "Eberstadt", 49.814284999, 8.642065999);
        MapMarkerDot ebersheim = new MapMarkerDot(germanyWestLayer, "Ebersheim", 49.91, 8.24);
        MapMarkerDot empty = new MapMarkerDot(germanyEastLayer, 49.71, 8.64);
        MapMarkerDot darmstadt = new MapMarkerDot(germanyEastLayer, "Darmstadt", 49.8588, 8.643);
        map().addMapMarker(eberstadt);
        map().addMapMarker(ebersheim);
        map().addMapMarker(empty);
        Layer franceLayer = treeMap.addLayer("France");
        map().addMapMarker(new MapMarkerDot(franceLayer, "La Gallerie", 48.71, -1));
        map().addMapMarker(new MapMarkerDot(43.604, 1.444));
        map().addMapMarker(new MapMarkerCircle(53.343, -6.267, 0.666));
        map().addMapRectangle(new MapRectangleImpl(new Coordinate(53.343, -6.267), new Coordinate(43.604, 1.444)));
        map().addMapMarker(darmstadt);
        treeMap.addLayer(germanyWestLayer);
        treeMap.addLayer(germanyEastLayer);

        MapPolygon bermudas = new MapPolygonImpl(c(49,1), c(45,10), c(40,5));
        map().addMapPolygon( bermudas );
        map().addMapPolygon( new MapPolygonImpl(germanyEastLayer, "Riedstadt", ebersheim, darmstadt, eberstadt, empty));

        map().addMapMarker(new MapMarkerCircle(germanyWestLayer, "North of Suisse", new Coordinate(48, 7), .5));
        Layer spain = treeMap.addLayer("Spain");
        map().addMapMarker(new MapMarkerCircle(spain, "La Garena", new Coordinate(40.4838, -3.39), .002));
        spain.setVisible(false);

        Layer wales = treeMap.addLayer("UK");
        map().addMapRectangle(new MapRectangleImpl(wales, "Wales", c(53.35,-4.57), c(51.64,-2.63)));
         */
        // map.setDisplayPosition(new Coordinate(49.807, 8.6), 11);
//        map.setTileGridVisible(true);
        
        //Lisbon
        map().setDisplayPosition(new Coordinate(38.7166700,-9.1333300), 11);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	
               if(e.getButton() == MouseEvent.BUTTON3) {
            	   addMarker(map().getPosition(e.getPoint()),"waypoint");
//            	   updateRobotPosition("drone", map().getPosition(e.getPoint()));
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
    
    private JMapViewer map(){
        return treeMap.getViewer();
    }
    
    public static Coordinate c(double lat, double lon){
        return new Coordinate(lat, lon);
    }

    public void addMarker(Coordinate c, String name) {
    	Layer l = null;
    	
    	for(Layer layer : treeMap.getLayers())
    		if(layer.getName().equals(name))
    			l = layer;

    	if(l == null) {
    		l = treeMap.addLayer(name);
    	} else {
    		MapMarker old = waypoints.peekFirst();
    		treeMap.removeFromLayer(old);
    		map().removeMapMarker(old);
    	}
    	
    	waypoints.clear();
    	
    	MapMarker m = new MapMarkerDot(l, name , c);
    	l.add(m);
    	waypoints.add(m);
    	
    	map().addMapMarker(m);
    	
    	synchronized(this) {
    		droneWaypoint = new Waypoint(c.getLat(),c.getLon(),"waypoint");
    		notifyAll();
    	}
    }
    
    public void updateRobotPosition(String name, Coordinate c, double orientation) {
    	
    	Layer l = null;
    	
    	Iterator<MapMarker> i = map().getMapMarkerList().iterator();
    	
    	while(i.hasNext()) {
    		MapMarker m = i.next();
    		if(m.getLayer().getName().equals(name)) {
    			l = m.getLayer();
    			break;
    		}
    	}

    	if(l == null) {
    		l = treeMap.addLayer(name);
    	}
    	
    	if(!robotPositions.isEmpty()) {
    	
	    	Style styleOld = new Style(Color.black, Color.LIGHT_GRAY, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 0));
	    	
	    	//remove last value from previous iteration
	    	MapMarker last = robotPositions.pollLast();
	    	treeMap.removeFromLayer(last);
	    	map().removeMapMarker(last);
	    	
	    	//add that same one with a different style
	    	MapMarker old = new MapMarkerDot(l,""+robotMarkerIndex++,last.getCoordinate(), styleOld);
	    	robotPositions.add(old);
	    	l.add(old);
	    	map().addMapMarker(old);
	    	
    	}
    	
    	//add the new one with the new style
    	Style styleNew = new Style(Color.RED, Color.green, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    	MapMarker m = new MapMarkerDrone(l, name , c, styleNew, orientation);
    	l.add(m);
    	map().addMapMarker(m);
    	robotPositions.add(m);
    	
    	while(robotPositions.size() > POSITION_HISTORY) {
    		MapMarker old = robotPositions.pollFirst();
    		treeMap.removeFromLayer(old);
        	map().removeMapMarker(old);
    	}
    	
    	if(robotPositions.size() == 1) {
    		fitMarkersButton.doClick();
    	}
    }
    
    public void displayData(DroneLocation di) {
		
		double lat = di.getLatitude();
		double lon = di.getLongitude();
		
		if(lat == 0 && lon == 0)
			return;
		
		double orientation = di.getOrientation();
		
		String droneName = di.getName().isEmpty() ? "drone" : di.getName();
		
		if(usefulRobotCoordinate(c(lat,lon))) {
			updateRobotPosition(droneName, c(lat,lon), orientation);
		}
	}
	
	private boolean usefulRobotCoordinate(Coordinate n) {
		
		if(n.getLat() == -1 && n.getLon() == -1)
			return false;
		
		if(robotPositions.isEmpty())
			return true;
		
		Coordinate c = robotPositions.peekLast().getCoordinate();
		
		if(c.getLat() == n.getLat() && c.getLon() == n.getLon())
			return false;
		
		return true;
	}
	
	public synchronized EntityMessage getCurrentMessage() {
		EntityMessage m = new EntityMessage(droneWaypoint);
		droneWaypoint = null;
		return m;
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
		while(droneWaypoint == null) {
			try {
				wait();
			} catch(Exception e){}
		}
	}

	public void displayData(EntityMessage message) {
		System.out.println("TODO MapPanel");
	}
	
}