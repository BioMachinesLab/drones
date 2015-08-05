/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.Line;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;

/**
 *
 * @author jorge
 */
public class CoverageTracer implements Updatable {

    private int margin = 50;
    private double scale = 5;
    private int timeStart = 0;
    private String folder = "heatmaps";
    
    private double resolution = 1;
    private List<Line> lines;

    private double distance = 10;
    
    private float min = 0;
    private float max = 1;
    private float diff = 1;
    private boolean color = false;

    private int snapshotFrequency = 300;

    private double width, height;

    public CoverageTracer(Arguments args) {
        margin = args.getArgumentAsIntOrSetDefault("imagemargin", margin);
        timeStart = args.getArgumentAsIntOrSetDefault("timestart", timeStart);
        scale = args.getArgumentAsDoubleOrSetDefault("scale", scale);
        
        color = args.getFlagIsTrue("color");
        
        min = (float)args.getArgumentAsDoubleOrSetDefault("min", min);
        max = (float)args.getArgumentAsDoubleOrSetDefault("max", max);
        
        diff = max-min;
        
        resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        snapshotFrequency = args.getArgumentAsIntOrSetDefault("snapshotfrequency", snapshotFrequency);

        scale = args.getArgumentAsDoubleOrSetDefault("scale", scale);
        margin = args.getArgumentAsIntOrSetDefault("imagemargin", margin);
        
        folder = args.getArgumentAsStringOrSetDefault("folder",folder);
        
        try {
	        File f = new File(folder);
	        
	        if (!f.exists()) {
	            f.mkdirs();
	        }
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public void update(Simulator sim) {}
    
    public void update(Simulator sim, double[][] coverage) {
    	
    	if(sim.getTime() % snapshotFrequency != 0)
    		return;
    	
    	AquaticDrone ad = (AquaticDrone) sim.getRobots().get(0);
        GeoFence fence = null;
        for (Entity e : ad.getEntities()) {
            if (e instanceof GeoFence) {
                fence = (GeoFence) e;
                break;
            }
        }
        lines = getLines(fence.getWaypoints(), sim);
        
        double maxXAbs = 0, maxYAbs = 0;

        for (Line l : lines) {
            maxXAbs = Math.max(maxXAbs, Math.abs(l.getPointA().x));
            maxYAbs = Math.max(maxYAbs, Math.abs(l.getPointA().y));
        }

        width = Math.max(maxXAbs, maxYAbs) * 2;
        height = width;
    	
    	int w = (int) (width * scale) + margin * 2;
        int h = (int) (height * scale) + margin * 2;
        SVGGraphics2D gr = new SVGGraphics2D(w, h);
        gr.setPaint(Color.WHITE);
        gr.fillRect(0, 0, w, h);

        // draw heatmap
        for (int y = coverage.length - 1; y >= 0; y--) {
            for (int x = 0; x < coverage[y].length; x++) {
                int minX = (int) Math.round((x * resolution) * scale);
                int minY = (int) Math.round((y * resolution) * scale);
                int maxX = (int) Math.round(((x + 1) * resolution) * scale);
                int maxY = (int) Math.round(((y + 1) * resolution) * scale);

                if (coverage[y][x] >= min && coverage[y][x] <= max) {
                	
                	float cf = ((float)coverage[y][x]-min)/diff;
                    Color c;
                    
                    if(color)
                    	c = getColorForPercentage(cf);
                    else
                    	c = new Color(cf, cf, cf);
                    
                    gr.setPaint(c);
                    gr.fillRect(minX + margin, minY + margin, maxX - minX, maxY - minY);
                }
            }
        }

        // draw robots
        gr.setPaint(Color.RED);
        for (Robot r : sim.getRobots()) {
            Vector2d pos = r.getPosition();
            int size = (int) Math.round(r.getRadius() * 2 * scale);
            int x = (int) Math.round((pos.x + width / 2) * scale - size / 2d);
            int y = (int) Math.round((pos.y + height / 2) * scale - size / 2d);
            gr.fillOval(x + margin, y + margin, size, size);
        }

        // draw bounds
        gr.setPaint(Color.BLUE);
        for (Line l : lines) {
            Vector2d pointA = l.getPointA();
            Vector2d pointB = l.getPointB();
            int xa = (int) ((pointA.x + width / 2) * scale);
            int xb = (int) ((pointB.x + width / 2) * scale);
            int ya = (int) ((pointA.y + height / 2) * scale);
            int yb = (int) ((pointB.y + height / 2) * scale);
            gr.drawLine(xa + margin, ya + margin, xb + margin, yb + margin);
        }
        
        
        // write file
        try {
        	SVGUtils.writeToSVG(new File(folder, sim.hashCode() + "_" + sim.getTime() + ".svg"), gr.getSVGElement());
        } catch(Exception e) {
        	e.printStackTrace();
        }
    }
    
    

    public Color getColorForPercentage(double percent) {
    	
    	Color[] percentColors = new Color[]{new Color(255,255,0), new Color(255,0,0)}; 
    	double[] perentage = {0.0,1.0};
    	
    	int i;
    	 
        for (i = 1; i < percentColors.length - 1; i++) {
            if (percent < perentage[i]) {
                break;
            }
        }
        
        double lower = perentage[i - 1];
        double upper = perentage[i];
        double range = upper - lower;
        double rangePct = (percent - lower) / range;
        double pctLower = 1 - rangePct;
        double pctUpper = rangePct;
        
        int r = (int)Math.floor(percentColors[i-1].getRed() * pctLower + percentColors[i].getRed() * pctUpper);
        int g = (int)Math.floor(percentColors[i-1].getGreen() * pctLower + percentColors[i].getGreen() * pctUpper);
        int b = (int)Math.floor(percentColors[i-1].getBlue() * pctLower + percentColors[i].getBlue() * pctUpper);

        return new Color(r,g,b);
    }  
    
    protected List<Line> getLines(LinkedList<Waypoint> waypoints, Simulator simulator) {
        List<Line> linesList = new ArrayList<Line>();
        for (int i = 1; i < waypoints.size(); i++) {

            Waypoint wa = waypoints.get(i - 1);
            Waypoint wb = waypoints.get(i);
            commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
            commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

            simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
            linesList.add(l);
        }

        Waypoint wa = waypoints.get(waypoints.size() - 1);
        Waypoint wb = waypoints.get(0);
        commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
        commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

        simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
        linesList.add(l);
        return linesList;
    }

}
