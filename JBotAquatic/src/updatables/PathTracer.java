/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mathutils.Vector2d;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class PathTracer extends Tracer {

    private boolean hideStart = false;
    private boolean hideFinal = false;
    private boolean fade = false;
    private HashMap<Robot, List<Vector2d>> points = null;

    public PathTracer(Arguments args) {
        super(args);
        hideStart = args.getFlagIsTrue("hidestart");
        hideFinal = args.getFlagIsTrue("hidefinal");
        fade = args.getFlagIsTrue("fade");
    }

    @Override
    public void update(Simulator simulator) {
        if (points == null) { // first step -- setup
            width = simulator.getEnvironment().getWidth();
            height = simulator.getEnvironment().getHeight();
            points = new HashMap<>();
        }

        if (insideTimeframe(simulator)) {
            // RECORD PATHS
            for (Robot r : simulator.getRobots()) {
                if (!points.containsKey(r)) {
                    points.put(r, new ArrayList<Vector2d>());
                }
                points.get(r).add(new Vector2d(r.getPosition()));
            }
        }
    }

    @Override
    public void terminate(Simulator simulator) {
        // FIND BOUNDARIES
        double maxAbsX = 0, maxAbsY = 0;
        for (List<Vector2d> l : points.values()) {
            for (Vector2d v : l) {
                maxAbsX = Math.max(maxAbsX, Math.abs(v.x));
                maxAbsY = Math.max(maxAbsY, Math.abs(v.y));
            }
        }
        width = Math.max(maxAbsX, maxAbsY) * 2;
        height = width;

        SVGGraphics2D gr = createCanvas(simulator);
        
        // DRAW PATHS
        for (Robot r : points.keySet()) {
            List<Vector2d> pts = points.get(r);
            gr.setPaint(robotColor);
            if (!fade) {
                int[] xs = new int[pts.size()];
                int[] ys = new int[pts.size()];
                for (int i = 0; i < pts.size(); i++) {
                    IntPos t = transform(pts.get(i).x, pts.get(i).y);
                    xs[i] = t.x;
                    ys[i] = t.y;
                }
                gr.drawPolyline(xs, ys, pts.size());
            } else {
            	
            	
                for(int i = 1 ; i < pts.size() ; i++) {
                    int alpha = Math.max(25,(int) Math.round((double) i / pts.size() * 255));
                    Color c = new Color(robotColor.getRed(), robotColor.getGreen(), robotColor.getBlue(), alpha);
                    gr.setPaint(c);
                    IntPos old = transform(pts.get(i - 1).x, pts.get(i-1).y);
                    IntPos cur = transform(pts.get(i).x, pts.get(i).y);
                    if(old.x != cur.x || old.y != cur.y) {
                        gr.drawLine(old.x, old.y, cur.x, cur.y);
                    }
                }
            }
        }

        // DRAW INITIAL POSITIONS
        if (!hideStart) {
            for (Robot r : points.keySet()) {
                drawRobot(gr, r, points.get(r).get(0), true);
            }
        }

        // DRAW FINAL POSITIONS
        if (!hideFinal) {
        	for (Robot r : points.keySet()) {
                drawRobot(gr, r, points.get(r).get(points.get(r).size()-1), false);
            }
        }

        writeGraphics(gr, simulator, name.isEmpty() ? simulator.hashCode() + "" : name);
    }
}
