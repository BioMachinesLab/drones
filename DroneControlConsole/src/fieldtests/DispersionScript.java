/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fieldtests;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import gui.panels.CommandPanel;
import gui.panels.map.MapPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import main.DroneControlConsole;

public class DispersionScript extends FieldTestScript {

    private static String[] lastOptions = null;

    public DispersionScript(DroneControlConsole console, CommandPanel commandPanel) {
        super(console, commandPanel);
    }
    
    @Override
    public void run() {
    	
    	try {
	        super.run();
	        /*
	         Read options
	         */
	        String[] options = getMultipleInputsDialog(
	                new String[]{"Distance (meters)", "Ga dispersion (seconds)", "Gb gotowaypoint (seconds)", "All dispersion (seconds)", "IPs", "Seed"},
	                lastOptions != null ? lastOptions : new String[]{"60", "60", "120", "60", listToString(super.getSelectedIPs()), "1"});
	        if (options == null) {
	            return;
	        }
	        lastOptions = options;
	        int distance = Integer.parseInt(options[0]);
	        int time1 = Integer.parseInt(options[1]);
	        int time2 = Integer.parseInt(options[2]);
	        int time3 = Integer.parseInt(options[3]);
	        String[] ips = options[4].split("[;,\\-\\s]+");
	        long seed = Long.parseLong(options[5]);
	        
	        ArrayList<String> groupA = new ArrayList<String>();
	        
	        for(int i = 0 ; i < 4 ; i++)
	        	groupA.add(ips[i]);
	        
	        ArrayList<String> groupB = new ArrayList<String>();
	        
	        for(int i = 4 ; i < ips.length ; i++)
	        	groupB.add(ips[i]);
	        
	        /*
	         Generate starting positions
	         */
	        Waypoint startWP = getCentralPoint();
	        super.clearMapEntities();
	        
	        Vector2d vec = CoordinateUtilities.GPSToCartesian(startWP.getLatLon());
	        vec.y-=distance;
	        
	        Waypoint dispWP = new Waypoint("disp", CoordinateUtilities.cartesianToGPS(vec));
	        
	        GeoFence startFence = this.defineGeoFence(dispWP.getLatLon(), 20, 20);
	        ArrayList<Waypoint> startWPs = this.generateWaypointsInGeoFence(startFence, 4, 5, 40, seed);
	        
	        for(Waypoint wp : startWPs)
	        	super.addEntityToMap(wp);
	        super.addEntityToMap(startWP);
	        
	        super.addEntityToMap(startFence);
	        
	        /*
	         Confirm starting positions
	         */
	        int go = JOptionPane.showConfirmDialog(console.getGUI(), "Go with these positions?", "Position check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	        if (go != JOptionPane.YES_OPTION) {
	            super.clearMapEntities();
	            super.addEntityToMap(startWP);
	            return;
	        }
	        
	        /*
	         Go to starting positions
	         */
	        String controller = "dispersion2";
	        
	        for(int i = 0 ; i < groupA.size() ; i++)  {
	        	super.goToWaypoint(singletonList(groupA.get(i)), startWPs.get(i));
	        }
	        
	        super.goToWaypoint(groupB, startWP);
	        
	        /*
	         Ask for permission to start
	         */
	        int confirm = JOptionPane.showConfirmDialog(console.getGUI(), "Yes to start experiment, no to kill", "Confirm when ready", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
	        if (confirm == JOptionPane.YES_OPTION) {
	        	
	        	String description = super.startExperimentTimer(controller);
	        	
	        	startControllers(groupA, controller, description);
	        	
				Thread.sleep(time1*1000);
	        	
	        	System.out.println("Sending Group B");
	        	
	        	goToWaypoint(groupB, dispWP);
	        	
	        	Thread.sleep(time2*1000);
	           
	           System.out.println("Group B arrived, dispersing!");
	           
	           startControllers(groupB, controller, description);
	           
				Thread.sleep(time3*1000);
	           
	           System.out.println("Experiment finished!");
	           
	           super.stopControllers(groupA, description);
	           super.stopControllers(groupB, description);
	           
	        } else {
	            List<String> asList = Arrays.asList(ips);
	            super.stopControllers(new ArrayList<>(asList), "failed");
	        }
    	} catch(Exception e) {
    		System.out.println("Experiment aborted!");
    	}
    }
}
