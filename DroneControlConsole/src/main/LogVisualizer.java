package main;

import gui.Graph;
import gui.panels.map.MapPanel;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import commoninterface.AquaticDroneCI;
import commoninterface.objects.RobotLocation;
import commoninterface.utils.jcoord.LatLon;

public class LogVisualizer extends JFrame {
	
	private String file = "../JBotAquatic/selected_logs/success_1drone_07_05_2015/logs/values_6-5-2015_15-51-27.6.log";
//	private String file = "../JBotAquatic/logs/22_04_2015/values_09-04-2015_01-17-56.log";
	private Graph graph;
	private MapPanel map;
	private JSlider slider;
	private ArrayList<LogData> data;
	
	public static void main(String[] args) {
		new LogVisualizer();
	}
	
	public LogVisualizer() {
		
		try {
			data = readFile();
		
			graph = new Graph();
			map = new MapPanel();
			
			setLayout(new BorderLayout());
			
			add(map,BorderLayout.WEST);
			add(graph, BorderLayout.CENTER);
			
			slider = new JSlider(0,data.size());
			slider.setValue(0);
			slider.setMajorTickSpacing(100);
			slider.setMinorTickSpacing(10);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					slider.setToolTipText(""+slider.getValue());
					moveTo(slider.getValue());
				}
			});
			
			add(slider, BorderLayout.SOUTH);
			
			setSize(1500, 800);
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
			leftData[i] = data.get(i).leftSpeed;
			rightData[i] = data.get(i).rightSpeed;
			map.displayData(new RobotLocation("robot", data.get(i).latLon, data.get(i).compassOrientation, data.get(i).droneType));
		}
		graph.addDataList(leftData);
		graph.addDataList(rightData);
		graph.addLegend("Left Motor");
		graph.addLegend("Right Motor");
		
	}
	
	private ArrayList<LogData> readFile() throws IOException {
		Scanner s = new Scanner(new File(file));
		
		String lastComment = "";
		ArrayList<LogData> data = new  ArrayList<LogData>();
		
		int step = 0;
		
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
					String date = sl.next();
					d.leftSpeed = sl.nextDouble();
					d.rightSpeed = sl.nextDouble();
					
					d.droneType = AquaticDroneCI.DroneType.valueOf(sl.next());
					
					d.lastComment = lastComment;
					
					d.timestep = step++;
					
					data.add(d);
				
				}catch(Exception e){}
				
				sl.close();
				
			} else if(l.startsWith("#")){
				lastComment = l.substring(1);
			}
		}
		
		s.close();
		
		System.out.println(data.size());
		
		return data;
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
		String lastComment;
		AquaticDroneCI.DroneType droneType;
	}

}
