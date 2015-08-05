/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private int timeStart = 0;
    private File folder = new File("traces");
    private Color robotColor = Color.BLUE;
    private Color bgColor = Color.WHITE;

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
        scale = args.getArgumentAsDoubleOrSetDefault("scale", scale);
        if (args.getArgumentIsDefined("robotcolor")) {
            robotColor = parseColor(args.getArgumentAsString("robotcolor"));
        }
        if (args.getArgumentIsDefined("bgcolor")) {
            bgColor = parseColor(args.getArgumentAsString("bgcolor"));
        }
        if (args.getArgumentIsDefined("folder")) {
            folder = new File(args.getArgumentAsString("folder"));
        }
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static Color parseColor(String str) {
        String[] split = str.split("-|\\.|,|;");
        if (split.length == 1) {
            try {
                Field field = Color.class.getField(split[0]);
                return (Color) field.get(null);
            } catch (Exception ex) {
                return Color.decode(split[0]);
            }
        } else if (split.length == 3) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } else if (split.length == 4) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }
        return null;
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
            points.get(r).add(new IntPos(margin + (int) Math.round((r.getPosition().x + width / 2) * scale),
                    margin + (int) Math.round((r.getPosition().y + height / 2) * scale)));
        }
    }

    @Override
    public void terminate(Simulator simulator) {
        // INIT GRAPHICS
        int w = (int) (width * scale) + margin * 2;
        int h = (int) (height * scale) + margin * 2;
        SVGGraphics2D gr = new SVGGraphics2D(w, h);
        gr.setPaint(bgColor);
        gr.fillRect(0, 0, w, h);

        // DRAW INITIAL POSITIONS
        if (!hideStart) {
            for (Robot r : points.keySet()) {
                IntPos p = points.get(r).get(0);
                int s = (int) Math.round(r.getRadius() * 2 * scale);
                gr.setPaint(robotColor);
                gr.fillRect(p.x - (int) (r.getRadius() * scale), p.y - (int) (r.getRadius() * scale), s, s);
            }
        }

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
            for (Robot r : simulator.getRobots()) {
                IntPos p = points.get(r).get(points.get(r).size() - 1);
                int s = (int) Math.round(r.getRadius() * 2 * scale);
                gr.setPaint(robotColor);
                gr.fillOval(p.x - (int) (r.getRadius() * scale), p.y - (int) (r.getRadius() * scale), s, s);
            }
        }
        

        // WRITE FILE
        File out = new File(folder, simulator.hashCode() + ".svg");
        try {
            SVGUtils.writeToSVG(out, gr.getSVGElement());

        } catch (IOException ex) {
            Logger.getLogger(Tracer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}
