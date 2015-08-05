/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import java.awt.BasicStroke;

/**
 *
 * @author jorge
 */
public class CoverageTracer extends Tracer {

    private double resolution;
    private List<Line> lines;

    private float min = 0;
    private float max = 1;
    private boolean color = false;

    private int snapshotFrequency = 300;
    private double[][] coverage;
    private boolean isSetup = false;

    public CoverageTracer(Arguments args) {
        super(args);
        color = args.getFlagIsTrue("color");
        min = (float) args.getArgumentAsDoubleOrSetDefault("min", min);
        max = (float) args.getArgumentAsDoubleOrSetDefault("max", max);
        snapshotFrequency = args.getArgumentAsIntOrSetDefault("snapshotfrequency", snapshotFrequency);
    }

    @Override
    public void update(Simulator sim) {
        if (!isSetup) {
            setup(sim);
            isSetup = true;
        }
        if (sim.getTime() % snapshotFrequency == 0 && sim.getTime() >= timeStart) {
            drawHeatmap(sim);
        }
    }

    protected void setup(Simulator sim) {
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
        /*width = 100;
        height = 100;*/
    }

    public void setCoverage(double[][] coverage, double resolution) {
        this.coverage = coverage;
        this.resolution = resolution;
    }

    public void drawHeatmap(Simulator sim) {
        if (coverage == null) {
            return;
        }

        SVGGraphics2D gr = createCanvas();

        // draw heatmap
        for (int y = coverage.length - 1; y >= 0; y--) {
            for (int x = 0; x < coverage[y].length; x++) {
                IntPos lowerBB = transformNoShift(x * resolution, y * resolution);
                IntPos upperBB = transformNoShift((x + 1) * resolution, (y + 1) * resolution);
                System.out.println(x+","+y+":"+lowerBB.x + "," + lowerBB.y + "|" + upperBB.x + "," + upperBB.y);

                if (coverage[y][x] >= min && coverage[y][x] <= max) {
                    float cf = ((float) coverage[y][x] - min) / (max - min);
                    Color c;

                    if (color) {
                        c = getColorForPercentage(cf);
                    } else {
                        c = new Color(1 - cf, 1 - cf, 1 - cf);
                    }

                    gr.setPaint(c);
                    gr.fillRect(upperBB.x, upperBB.y, lowerBB.x - upperBB.x, lowerBB.y - upperBB.y);
                }
            }
        }

        // draw robots
        drawRobots(gr, sim);
        gr.fillRect(transformNoShift(5,95).x, transformNoShift(5,95).y, 10, 10);
        gr.fillRect(transformNoShift(5,5).x, transformNoShift(5,5).y, 10, 10);
        gr.fillRect(transformNoShift(95,5).x, transformNoShift(95,5).y, 10, 10);
        
                

        // draw geofence
        gr.setPaint(Color.BLUE);
        BasicStroke dashed = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, new float[]{10f}, 0.0f);
        gr.setStroke(dashed);
        for (Line l : lines) {
            Vector2d pointA = l.getPointA();
            Vector2d pointB = l.getPointB();
            IntPos pa = transform(pointA.x, pointA.y);
            IntPos pb = transform(pointB.x, pointB.y);
            gr.drawLine(pa.x, pa.y, pb.x, pb.y);
        }

        // write file
        writeGraphics(gr, sim.hashCode() + "_" + sim.getTime());
    }

    public Color getColorForPercentage(double percent) {

        Color[] percentColors = new Color[]{new Color(255, 255, 0), new Color(255, 0, 0)};
        double[] perentage = {0.0, 1.0};

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

        int r = (int) Math.floor(percentColors[i - 1].getRed() * pctLower + percentColors[i].getRed() * pctUpper);
        int g = (int) Math.floor(percentColors[i - 1].getGreen() * pctLower + percentColors[i].getGreen() * pctUpper);
        int b = (int) Math.floor(percentColors[i - 1].getBlue() * pctLower + percentColors[i].getBlue() * pctUpper);

        return new Color(r, g, b);
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

    @Override
    public void terminate(Simulator simulator) {
        drawHeatmap(simulator);
    }

}
