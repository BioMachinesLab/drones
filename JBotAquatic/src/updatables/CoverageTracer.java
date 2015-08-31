/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package updatables;

import java.awt.Color;
import java.awt.Graphics2D;
import simulation.Simulator;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class CoverageTracer extends Tracer {

    private double resolution;

    private float min = 0;
    private float max = 1;
    private boolean gradient = false;

    private double[][] coverage;
    private boolean sequence = false;
    private int count = 0;

    public CoverageTracer(Arguments args) {
        super(args);
        gradient = args.getFlagIsTrue("gradient");
        min = (float) args.getArgumentAsDoubleOrSetDefault("min", min);
        max = (float) args.getArgumentAsDoubleOrSetDefault("max", max);
        sequence = args.getFlagIsTrue("sequence");
    }

    public void setCoverage(double[][] coverage, double resolution) {
    	if(this.coverage == null) {
	        this.coverage = coverage;
	        this.resolution = resolution;
    	}
    }

    @Override
    public void snapshot(Simulator sim) {
        if (coverage == null) {
            return;
        }
        
        Graphics2D gr = createCanvas(sim);

        // draw heatmap
        for (int y = coverage.length - 1; y >= 0; y--) {
//        	if(color)
//        		System.out.print((y+1)+" ");
            for (int x = 0; x < coverage[y].length; x++) {
                IntPos lowerBB = transformNoShift(x * resolution, y * resolution);
                IntPos upperBB = transformNoShift((x + 1) * resolution, (y + 1) * resolution);
                int size = Math.abs(lowerBB.x - upperBB.x);
                
                if (coverage[y][x] >= min && coverage[y][x] <= max) {
                	
                    float cf = ((float) coverage[y][x] - min) / (max - min);
                    
                    Color c;

                    if (gradient) {
                        c = getColorForPercentage(cf);
                    } else {
                        c = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), (int) (cf * 255));
                    }

                    gr.setPaint(c);
                    gr.fillRect(Math.min(lowerBB.x, upperBB.x), Math.min(lowerBB.y, upperBB.y), size, size);
                }
            }
        }

        // draw robots
        drawRobots(gr, sim, true, altColor);

        // write file
        
        String fileName = ""+sim.getTime();
        while(fileName.length() < 6) {
        	fileName = "0"+fileName;
        }
        

        if (sequence) {
            writeGraphics(gr, sim, "frame_" + count++);
        } else {
            writeGraphics(gr, sim, "step_" + (int) (double) sim.getTime());
        }
    }

    public static Color getColorForPercentage(double percent) {

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

    public double[][] getCoverage() {
		return coverage;
	}

    @Override
    public void terminate(Simulator simulator) {
        snapshot(simulator);
    }

}
