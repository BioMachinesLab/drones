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
import java.util.logging.Level;
import java.util.logging.Logger;
import mathutils.Vector2d;
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
public abstract class Tracer implements Stoppable {

    protected File folder = new File("traces");
    protected Color robotColor = Color.BLUE;
    protected Color bgColor = Color.WHITE;
    protected int margin = 0;
    protected double scale = 5;
    protected int timeStart = 0;
    protected double width, height;

    public Tracer(Arguments args) {
        margin = args.getArgumentAsIntOrSetDefault("imagemargin", margin);
        timeStart = args.getArgumentAsIntOrSetDefault("timestart", timeStart);
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

    protected class IntPos {

        int x;
        int y;

        IntPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    protected IntPos transform(double x, double y) {
        return new IntPos(margin + (int) Math.round((x + width / 2) * scale),
                margin + (int) Math.round((height / 2 - y) * scale));
    }
    
    protected IntPos transformNoShift(double x, double y) {
        return new IntPos(margin + (int) Math.round(x * scale),
                margin + (int) Math.round((height - y) * scale));
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

    protected SVGGraphics2D createCanvas() {
        int w = (int) (width * scale) + margin * 2;
        int h = (int) (height * scale) + margin * 2;
        SVGGraphics2D gr = new SVGGraphics2D(w, h);
        gr.setPaint(bgColor);
        gr.fillRect(0, 0, w, h);
        return gr;
    }

    protected void writeGraphics(SVGGraphics2D gr, String filePrefix) {
        File out = new File(folder, filePrefix + ".svg");
        try {
            SVGUtils.writeToSVG(out, gr.getSVGElement());
        } catch (IOException ex) {
            Logger.getLogger(PathTracer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void drawRobots(SVGGraphics2D gr, Simulator sim) {
        gr.setPaint(robotColor);
        for (Robot r : sim.getRobots()) {
            Vector2d pos = r.getPosition();
            int size = (int) Math.round(r.getRadius() * 2 * scale);
            IntPos iPos = transform(pos.x, pos.y);
            /*int x = (int) Math.round((pos.x + width / 2) * scale - size / 2d);
            int y = (int) Math.round((pos.y + height / 2) * scale - size / 2d);*/
            gr.fillOval(iPos.x, iPos.y, size, size);
        }
    }

}
