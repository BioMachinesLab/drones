/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.util.Arguments;


/**
 *
 * @author jorge
 */
public class CoverageTracer extends Tracer {

    private double resolution;

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
        if (sim.getTime() % snapshotFrequency == 0 && insideTimeframe(sim)) {
            drawHeatmap(sim);
        }
    }

    protected void setup(Simulator sim) {
        double maxXAbs = 0, maxYAbs = 0;

        super.setupGeofence(sim);
        
        if(lines != null) {
        
	        for (Line l : lines) {
	            maxXAbs = Math.max(maxXAbs, Math.abs(l.getPointA().x));
	            maxYAbs = Math.max(maxYAbs, Math.abs(l.getPointA().y));
	        }
	        
	        width = Math.max(maxXAbs, maxYAbs) * 2;
	        height = width;
        } else {
        	width = sim.getEnvironment().getWidth();
        	height = width;
        }

    }

    public void setCoverage(double[][] coverage, double resolution) {
        this.coverage = coverage;
        this.resolution = resolution;
    }

    public void drawHeatmap(Simulator sim) {
        if (coverage == null) {
            return;
        }
        
        SVGGraphics2D gr = createCanvas(sim);

        // draw heatmap
        for (int y = coverage.length - 1; y >= 0; y--) {
            for (int x = 0; x < coverage[y].length; x++) {
                IntPos lowerBB = transformNoShift(x * resolution, y * resolution);
                IntPos upperBB = transformNoShift((x + 1) * resolution, (y + 1) * resolution);
                int size = Math.abs(lowerBB.x - upperBB.x);

                if (coverage[y][x] >= min && coverage[y][x] <= max) {
                    float cf = ((float) coverage[y][x] - min) / (max - min);
                    Color c;

                    if (color) {
                        c = getColorForPercentage(cf);
                    } else {
                        c = new Color(1 - cf, 1 - cf, 1 - cf);
                    }

                    gr.setPaint(c);
                    gr.fillRect(Math.min(lowerBB.x, upperBB.x), Math.min(lowerBB.y, upperBB.y), size, size);
                }
            }
        }

        // draw robots
        drawRobots(gr, sim, true);

        // write file
        writeGraphics(gr, sim, sim.hashCode() + "_" + sim.getTime());
    }

    public Color getColorForPercentage(double percent) {

        Color[] percentColors = new Color[]{new Color(0, 255, 0), new Color(255, 255, 0), new Color(255, 0, 0)};
        double[] perentage = {0.0, 0.5, 1.0};

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



    @Override
    public void terminate(Simulator simulator) {
        drawHeatmap(simulator);
    }

}
