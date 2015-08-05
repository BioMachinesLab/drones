/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private HashMap<Robot, List<IntPos>> points = null;
    private SVGGraphics2D gr;

    public PathTracer(Arguments args) {
        super(args);
        hideStart = args.getFlagIsTrue("hidestart");
        hideFinal = args.getFlagIsTrue("hidefinal");
    }

    @Override
    public void update(Simulator simulator) {
        if (points == null) { // first step -- setup
            width = simulator.getEnvironment().getWidth();
            height = simulator.getEnvironment().getHeight();
            gr = createCanvas();
            points = new HashMap<>();
        }

        if (simulator.getTime() >= timeStart) {
            // DRAW INITIAL POSITIONS
            if (!hideStart && simulator.getTime() == timeStart) {
                drawRobots(gr, simulator);
            }
            
            // RECORD PATHS
            for (Robot r : simulator.getRobots()) {
                if (!points.containsKey(r)) {
                    points.put(r, new ArrayList<IntPos>());
                }
                points.get(r).add(transform(r.getPosition().x, r.getPosition().y));
            }
        }
    }

    @Override
    public void terminate(Simulator simulator) {
        // DRAW PATHS
        for (Robot r : points.keySet()) {
            List<IntPos> pts = points.get(r);
            gr.setPaint(robotColor);

            int[] xs = new int[pts.size()];
            int[] ys = new int[pts.size()];
            for (int i = 0; i < pts.size(); i++) {
                xs[i] = pts.get(i).x;
                ys[i] = pts.get(i).y;
            }
            gr.drawPolyline(xs, ys, pts.size());
        }

        // DRAW FINAL POSITIONS
        if (!hideFinal) {
            drawRobots(gr, simulator);
        }

        writeGraphics(gr, simulator.hashCode() + "");
    }
}
