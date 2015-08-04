/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import simulation.Simulator;
import simulation.Stoppable;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class Tracer implements Stoppable {

    private int margin = 50;
    private double scale = 5;
    private boolean hideStart = false;
    private boolean hideFinal = false;
    private boolean fillBG = false;
    private int timeStart = 0;
    private File folder = new File("traces");

    private double width, height;
    private HashMap<Robot, List<IntPos>> points = null;

    private class IntPos {

        int x;
        int y;

        IntPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Tracer(Arguments args) {
        margin = args.getArgumentAsIntOrSetDefault("imagemargin", margin);
        timeStart = args.getArgumentAsIntOrSetDefault("timestart", timeStart);
        hideStart = args.getFlagIsTrue("hidestart");
        hideFinal = args.getFlagIsTrue("hidefinal");
        fillBG = args.getFlagIsTrue("fillbg");
        scale = args.getArgumentAsDoubleOrSetDefault("scale", scale);
        if (args.getArgumentIsDefined("folder")) {
            folder = new File(args.getArgumentAsString("folder"));
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public void update(Simulator simulator) {
        if (points == null) {
            width = simulator.getEnvironment().getWidth();
            height = simulator.getEnvironment().getHeight();
            points = new HashMap<>();
        }

        // RECORD PATHS
        for (Robot r : simulator.getRobots()) {
            if (!points.containsKey(r)) {
                points.put(r, new ArrayList<IntPos>());
            }
            points.get(r).add(new IntPos((int) Math.round((r.getPosition().x + width / 2) * scale),
                    (int) Math.round((r.getPosition().y + height / 2) * scale)));
        }
    }

    @Override
    public void terminate(Simulator simulator) {
        // INIT GRAPHICS
        int w = (int) (width * scale);
        int h = (int) (height * scale);
        SVGGraphics2D gr = new SVGGraphics2D(w, h);
        if (fillBG) {
            gr.setPaint(Color.WHITE);
            gr.fillRect(0, 0, w, h);
        }

        // DRAW INITIAL POSITIONS
        if (!hideStart) {
            for (Robot r : points.keySet()) {
                IntPos p = points.get(r).get(0);
                int s = (int) Math.round(r.getRadius() * 2 * scale);
                Color c = r.getBodyColor();
                gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
                gr.fillRect(p.x - (int) (r.getRadius() * scale), p.y - (int) (r.getRadius() * scale), s, s);
            }
        }
        
        // DRAW PATHS
        for (Robot r : points.keySet()) {
            List<IntPos> pts = points.get(r);
            Color c = (Color) r.getBodyColor();
            gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));

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
            for (Robot r : simulator.getRobots()) {
                IntPos p = points.get(r).get(points.get(r).size() - 1);
                int s = (int) Math.round(r.getRadius() * 2 * scale);
                Color c = r.getBodyColor();
                gr.setPaint(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
                gr.fillOval(p.x - (int) (r.getRadius() * scale), p.y - (int) (r.getRadius() * scale), s, s);
            }
        }
        
        // WRITE FILE
        File out = new File(folder, simulator.hashCode() + ".svg");
        try {
            SVGUtils.writeToSVG(out, gr.getSVGElement());
        } catch (IOException ex) {
            Logger.getLogger(Tracer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
