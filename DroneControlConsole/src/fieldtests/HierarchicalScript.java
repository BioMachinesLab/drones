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

public class HierarchicalScript extends FieldTestScript {

    private static String[] lastOptions = null;

    public HierarchicalScript(DroneControlConsole console, CommandPanel commandPanel) {
        super(console, commandPanel);
    }
    
    @Override
    public void run() {
        super.run();
        /*
         Read options
         */
        String[] options = getMultipleInputsDialog(
                new String[]{"Number Drones (+1 intruder)", "IPs (last will be intruder)", "Width (meters)", "Height (meters)"},
                lastOptions != null ? lastOptions : new String[]{"0", listToString(super.getSelectedIPs()), "100", "100"});
        if (options == null) {
            return;
        }
        lastOptions = options;
        int nDrones = Integer.parseInt(options[0]);
        String[] ips = options[1].split("[;,\\-\\s]+");
        int w = Integer.parseInt(options[2]);
        int h = Integer.parseInt(options[3]);
        
        if(ips.length != nDrones + 1) {
        	JOptionPane.showMessageDialog(commandPanel,"Number of drones + 1 != ips.length");
        	return;
        }
        
        /*
         Generate starting positions
         */
        Waypoint center = getCentralPoint();
        
        Waypoint baseWp = new Waypoint("base", center.getLatLon());
        
        Vector2d vec = CoordinateUtilities.GPSToCartesian(center.getLatLon());
        vec.y-=(h/2+h/5);
        
        super.removeEntityFromMap(center);
        GeoFence fence = super.defineGeoFence(CoordinateUtilities.cartesianToGPS(vec), w, h);
        
        super.addEntityToMap(baseWp);
        super.addEntityToMap(fence);

        /*
         Confirm starting positions
         */
        int go = JOptionPane.showConfirmDialog(console.getGUI(), "Go with these positions?", "Position check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (go != JOptionPane.YES_OPTION) {
            super.clearMapEntities();
            super.addEntityToMap(center);
            return;
        }
        
        /*
         Go to starting positions
         */
        for (int i = 0; i < nDrones + 1; i++) {
            super.goToWaypoint(singletonList(ips[i]), baseWp);
        }
        
        String droneController = "hierarchical";
        String intruderController = "preprog_waypoint";
        
        /*
         Ask for permission to start
         */
        int confirm = JOptionPane.showConfirmDialog(console.getGUI(), "Yes to start experiment, no to kill", "Confirm when ready", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
        	
        	ArrayList<String> deployDroneIPs = new ArrayList<String>();
        	for(int i = 0 ; i < nDrones ; i++)
        		deployDroneIPs.add(ips[i]);
        	
        	deployMapEntities(deployDroneIPs);
        	deployMapEntities(singletonList(ips[ips.length - 1]));
        	
        	try {
        		Thread.sleep(3000);
        	} catch(Exception e) {
        		
        	}
        	
            String description = super.startExperimentTimer(droneController);
            
            try {
                CIArguments args = readConfigurationFile(droneController);
                args.setArgument("description", description);
                for(int i = 0 ; i < nDrones ; i++) {
                	String ip = deployDroneIPs.get(i);
                	args.setArgument("ip"+ip, (0.5+0.5/(nDrones-1)*i));
                }
                
                startControllers(deployDroneIPs, args);
                
                super.startControllers(singletonList(ips[ips.length - 1]), intruderController, description);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        } else {
            List<String> asList = Arrays.asList(ips);
            super.stopControllers(new ArrayList<>(asList), "failed");
        }
    }

}
