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
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import gui.panels.CommandPanel;
import gui.panels.map.MapPanel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import main.DroneControlConsole;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author jorge
 */
public class PredPreyScript extends FieldTestScript {

    public static final double MIN_DISTANCE = 5;
    public static final double BOX_SIDE = 10;
    public static final int NUM_ROBOTS = 3;
    private static String[] lastOptions = null;

    public PredPreyScript(DroneControlConsole console, CommandPanel commandPanel, MapPanel mapPanel) {
        super(console, commandPanel, mapPanel);
    }

    @Override
    public void start() {
        super.start();
        /*
         Get options
         */
        String[] options = getMultipleInputsDialog(
                new String[]{"Seed", "Prey distance", "IPs", "Predator behavior", "Prey behavior", "Placement box", "Safety distance"},
                lastOptions != null ? lastOptions : new String[]{"0", "30", "", "", "present_prey_10", "10", "3"});
        if (options == null) {
            return;
        }
        lastOptions = options;
        long seed = Long.parseLong(options[0]);
        Random rand = new Random(seed);
        double preyDist = Double.parseDouble(options[1]);
        String[] ips = options[2].split("[;,\\s]");
        String predBehav = options[3];
        String preyBehav = options[4];

        /*
         Generate starting positions
         */
        Waypoint center = getCentralPoint();
        super.removeEntityFromMap(center);
        GeoFence fence = super.defineGeoFence(center.getLatLon(), BOX_SIDE, BOX_SIDE);

        // Generate positions
        ArrayList<Waypoint> predatorsPos = super.generateWaypointsInGeoFence(fence, NUM_ROBOTS, MIN_DISTANCE, 40, seed);
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

        // Confirm positions
        int go = JOptionPane.showConfirmDialog(console.getGUI(), "Go with these positions?", "Position check", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (go != JOptionPane.YES_OPTION) {
            super.clearMapEntities();
            super.addEntityToMap(center);
            return;
        }
        
        super.clearMapEntities();

        /*
         Go to starting positions
         */
        super.clearMapEntities();
        for (int i = 0; i < NUM_ROBOTS; i++) {
            super.goToWaypoint(singletonList(ips[i]), predatorsPos.get(i));
        }
        super.goToWaypoint(singletonList(ips[ips.length - 1]), preyWp);

        /*
         Ask for permission to start
         */
        int confirm = JOptionPane.showConfirmDialog(console.getGUI(), "Yes to start, no to stop", "Last confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String description = super.startExperimentTimer(predBehav);
            for (int i = 0; i < NUM_ROBOTS; i++) {
                super.startControllers(singletonList(ips[i]), predBehav + "-" + i, description);
            }
            super.startControllers(singletonList(ips[ips.length - 1]), preyBehav, description);
        } else {
            List<String> asList = Arrays.asList(ips);
            super.stopControllers(new ArrayList<>(asList), "failed");
        }
    }

    protected Waypoint getCentralPoint() {
        ArrayList<Entity> entities = super.mapPanel.getEntities();
        Waypoint wp = null;
        for (Entity e : entities) {
            if (e instanceof Waypoint) {
                if (wp == null) {
                    wp = (Waypoint) e;
                } else {
                    fatalErrorDialog("More than one waypoint found for central point");
                    return null;
                }
            }
        }
        if (wp == null) {
            fatalErrorDialog("No waypoint is defined for central point");
            return null;
        } else {
            return wp;
        }
    }
}
