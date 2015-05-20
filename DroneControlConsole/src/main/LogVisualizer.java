package main;

import gui.Graph;
import gui.panels.map.MapPanel;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.utils.jcoord.LatLon;

public class LogVisualizer extends JFrame {
	
	private static String FOLDER = "logs";
	private Graph graph;
	private MapPanel map;
	private JSlider slider;
	private HashMap<String,ArrayList<LogData>> allData;
	private int maxSize = 0;
	
	public static void main(String[] args) {
		new LogVisualizer();
	}
	
	public LogVisualizer() {
		
		try {
			allData = readFile();
		
			graph = new Graph();
			map = new MapPanel();
			
			setLayout(new BorderLayout());
			
			add(map,BorderLayout.CENTER);
//			add(graph, BorderLayout.EAST);
			
			slider = new JSlider(0,maxSize);
			slider.setValue(0);
//			slider.setMajorTickSpacing(100);
//			slider.setMinorTickSpacing(10);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					slider.setToolTipText(""+slider.getValue());
					moveTo(slider.getValue());
				}
			});
			
			add(slider, BorderLayout.SOUTH);
			
			setSize(800, 800);
			setVisible(true);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void moveTo(int step) {
		graph.clear();
		map.clearHistory();
		
		graph.setShowLast(1000);
		
		Double[] leftData = new Double[step];
		Double[] rightData = new Double[step];
		
		for(int i = 0 ; i < step ; i++) {
			
			for(String s : allData.keySet()) {
				
				if(i < allData.get(s).size()) {
					
					LogData d = allData.get(s).get(i);
					
					leftData[i] = d.leftSpeed;
					rightData[i] = d.rightSpeed;
					map.displayData(new RobotLocation(s, d.latLon, d.compassOrientation, d.droneType));
					
					if(d.entities != null) {
						map.replaceEntities(d.entities);
					}
				}
			}
		}
		graph.addDataList(leftData);
		graph.addDataList(rightData);
		graph.addLegend("Left Motor");
		graph.addLegend("Right Motor");
		
	}
	
	private HashMap<String,ArrayList<LogData>> readFile() throws IOException {
		
		File folder = new File(FOLDER);
		
		HashMap<String,ArrayList<LogData>> result = new HashMap<String, ArrayList<LogData>>();
		
		for(String file : folder.list()) {
		
			Scanner s = new Scanner(new File(FOLDER+"/"+file));
			
			String lastComment = "";
			ArrayList<LogData> data = new  ArrayList<LogData>();
			
			int step = 0;
			
			String ip = "";
			
			ArrayList<Entity> currentEntities = new ArrayList<Entity>();
			
			while(s.hasNext()) {
				String l = s.nextLine();
				
				if(!l.startsWith("[") && !l.startsWith("#") && !l.trim().isEmpty()) {
					
					Scanner sl = new Scanner(l);
					
					try {
					
						LogData d = new LogData();
						
						d.time = sl.next();
						
						double lat = sl.nextDouble();
						double lon = sl.nextDouble();
						
						d.latLon = new LatLon(lat,lon);
						
						d.GPSorientation = sl.nextDouble();
						d.compassOrientation = sl.nextDouble();
						d.GPSspeed = sl.nextDouble();
						
						d.date = sl.next();
						
						DateTime date = new DateTime(d.date);		
						System.out.println(date);
						System.exit(0);
						
						d.leftSpeed = sl.nextDouble();
						d.rightSpeed = sl.nextDouble();
						
						d.droneType = AquaticDroneCI.DroneType.valueOf(sl.next());
						
						d.lastComment = lastComment;
						
						d.timestep = step++;
						
						d.entities = new ArrayList<Entity>();
						d.entities.addAll(currentEntities);
						data.add(d);
					
					}catch(Exception e){}
					
					sl.close();
					
				} else if(l.startsWith("#")){
					
					if(l.startsWith("#entity")) {
						handleEntity(l,currentEntities);
					} if(l.startsWith("#IP")) {
						ip = l.replace("#IP ", "").trim();
					} else
						lastComment = l.substring(1);
				}
			}
			
			if(!ip.isEmpty()) {
				maxSize = Math.max(maxSize, step);
				
				s.close();
				
				if(result.get(ip) != null) {
					result.get(ip).addAll(data);
				} else {
					result.put(ip, data);
				}
			}
		
		}
		
		return result;
	}
	
	private void handleEntity(String line, ArrayList<Entity> entities) {
		Scanner s = new Scanner(line);
		s.next();//ignore first token
		
		String event = s.next();
		
		if(event.equals("added")) {
			
			String className = s.next();
			
			String name = s.next();
			
			if(className.equals(GeoFence.class.getSimpleName())) {
				
				GeoFence fence = new GeoFence(name);
				
				int number = s.nextInt();
				
				for(int i = 0 ; i < number ; i++) {
					double lat = s.nextDouble();
					double lon = s.nextDouble();
					fence.addWaypoint(new LatLon(lat,lon));					
				}
				entities.add(fence);
			} else if(className.equals(Waypoint.class.getSimpleName())) {
				
				double lat = s.nextDouble();
				double lon = s.nextDouble();
				Waypoint wp = new Waypoint(name, new LatLon(lat,lon));
				entities.remove(wp);
				entities.add(wp);
				
			} else if(className.equals(ObstacleLocation.class.getSimpleName())) {
				
				double lat = s.nextDouble();
				double lon = s.nextDouble();
				
				double radius = s.nextDouble();
				entities.add(new ObstacleLocation(name, new LatLon(lat,lon),radius));
			}
			
		} else if(event.equals("removed")) {
			
			String name = s.next();
			
			Iterator<Entity> i = entities.iterator();
			while(i.hasNext()) {
				if(i.next().getName().equals(name)) {
					i.remove();
					break;
				}
			}
		}
		
		s.close();
	}
	
	public class LogData {
		String time;
		int timestep;
		LatLon latLon;
		double GPSorientation;
		double compassOrientation;
		double GPSspeed;
		double leftSpeed;
		double rightSpeed;
		String date;
		String lastComment;
		AquaticDroneCI.DroneType droneType;
		ArrayList<Entity> entities = null;
	}

}
