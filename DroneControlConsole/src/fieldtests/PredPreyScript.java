/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fieldtests;

import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import gui.panels.CommandPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import main.DroneControlConsole;

/**
 *
 * @author jorge
 */
public class PredPreyScript extends FieldTestScript {

    private static String[] lastOptions = null;

    public PredPreyScript(DroneControlConsole console, CommandPanel commandPanel) {
        super(console, commandPanel);
    }
    
    @Override
    public void run() {
        super.run();
        /*
         Read options
         */
        String[] options = getMultipleInputsDialog(
                new String[]{"Seed", "Prey distance", "IPs (last will be prey)", "Predator behavior", "Prey behavior", "Placement box", "Safety distance", "Num. predators"},
                lastOptions != null ? lastOptions : new String[]{"0", "30", listToString(super.getSelectedIPs()), "", "prey10", "10", "3","3"});
        if (options == null) {
            return;
        }
        lastOptions = options;
        long seed = Long.parseLong(options[0]);
        Random rand = new Random(seed);
        double preyDist = Double.parseDouble(options[1]);
        String[] ips = options[2].split("[;,\\-\\s]+");
        String predBehav = options[3];
        String preyBehav = options[4];
        double boxSize = Double.parseDouble(options[5]);
        double safetyDist = Double.parseDouble(options[6]);
        int numPreds = Integer.parseInt(options[7]);

        /*
         Generate starting positions
         */
        Waypoint center = getCentralPoint();
        super.removeEntityFromMap(center);
        GeoFence fence = super.defineGeoFence(center.getLatLon(), boxSize, boxSize);
        ArrayList<Waypoint> predatorsPos = super.generateWaypointsInGeoFence(fence, numPreds, safetyDist, 40, seed);
        double a = rand.nextDouble() * Math.PI * 2;
        double x = Math.cos(a) * preyDist;
        double y = Math.sin(a) * preyDist;
        Vector2d cart = CoordinateUtilities.GPSToCartesian(center.getLatLon());
        cart.x += x;
        cart.y += y;
        Waypoint preyWp = new Waypoint("Prey", CoordinateUtilities.cartesianToGPS(cart));
        for (Waypoint wp : predatorsPos) {
            super.addEntityToMap(wp);
        }
        super.addEntityToMap(preyWp);

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
        for (int i = 0; i < numPreds; i++) {
            super.goToWaypoint(singletonList(ips[i]), predatorsPos.get(i));
        }
        super.goToWaypoint(singletonList(ips[ips.length - 1]), preyWp);

        /*
         Ask for permission to start
         */
        int confirm = JOptionPane.showConfirmDialog(console.getGUI(), "Yes to start experiment, no to kill", "Confirm when ready", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
        	super.clearMapEntities();
            String description = super.startExperimentTimer(predBehav);
            for (int i = 0; i < numPreds; i++) {
                super.startControllers(singletonList(ips[i]), predBehav + "-" + i, description);
            }
            super.startControllers(singletonList(ips[ips.length - 1]), preyBehav, description);
        } else {
            List<String> asList = Arrays.asList(ips);
            super.stopControllers(new ArrayList<>(asList), "failed");
        }
    }

}
