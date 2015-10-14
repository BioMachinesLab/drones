package evaluation;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CoverageFitness extends AvoidCollisionsFunction {

    private boolean isSetup = false;
    private double[][] coverage;
    private double resolution = 1;
    private double width = 5, height = 5;
    private double decay = 0.001;//1000 steps to go from 1.0 to 0.0
    private double accum = 0;

    private double v = 0;
    private double max = 0;
    private double distance = 10;  
    private double steps = 0;

    public CoverageFitness(Arguments args) {
        super(args);
        resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        decay = args.getArgumentAsDoubleOrSetDefault("decay", decay);
    }

    public void setup(Simulator simulator) {
        width = simulator.getEnvironment().getWidth();
        height = simulator.getEnvironment().getHeight();
        coverage = new double[(int) (height / resolution)][(int) (width / resolution)];
        steps = simulator.getEnvironment().getSteps();
        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {
                double coordX = (x - coverage[y].length / 2) * resolution;
                double coordY = (y - coverage.length / 2) * resolution;
                if (!insideLines(new Vector2d(coordX, coordY), simulator)) {
                    coverage[y][x] = -1;
                } else {
                    max++;
                }
            }
        }
    }

    public boolean insideLines(Vector2d v, Simulator sim) {
        //http://en.wikipedia.org/wiki/Point_in_polygon
        int count = 0;

        for (PhysicalObject p : sim.getEnvironment().getAllObjects()) {
            if (p.getType() == PhysicalObjectType.LINE) {
                Line l = (Line) p;
                if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
                    count++;
                }
            }
        }
        return count % 2 != 0;
    }

    @Override
    public void update(Simulator simulator) {
        if (!isSetup) {
            setup(simulator);
            isSetup = true;
        }

        ArrayList<Robot> robots = simulator.getRobots();

        double sum = 0;

        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {

                if (coverage[y][x] == -1) {
                    continue;
                }

                if (coverage[y][x] > 0) {
                    if (coverage[y][x] <= 1) {
                        coverage[y][x] -= decay;
                        if (coverage[y][x] < 0) {
                            coverage[y][x] = 0;
                        }
                    }
                }

                if (coverage[y][x] > 0) {
                    sum += coverage[y][x];
                }
            }
        }

        for (Robot r : robots) {
            if (r.isEnabled()) {
                AquaticDrone ad = (AquaticDrone) r;
                if (insideLines(r.getPosition(), simulator)) {

                    double rX = ad.getPosition().getX();
                    double rY = ad.getPosition().getY();

                    double minX = rX - distance;
                    double minY = rY - distance;

                    double maxX = rX + distance;
                    double maxY = rY + distance;

                    int pMinX = (int) ((minX / resolution) + coverage.length / 2);
                    int pMinY = (int) ((minY / resolution) + coverage[0].length / 2);

                    double pMaxX = (maxX / resolution) + coverage.length / 2;
                    double pMaxY = (maxY / resolution) + coverage[0].length / 2;

                    for (int y = pMinY; y < pMaxY; y++) {

                        if (y >= coverage.length || y < 0) {
                            continue;
                        }

                        for (int x = pMinX; x < pMaxX; x++) {

                            if (x >= coverage[y].length || x < 0) {
                                continue;
                            }

                            if (coverage[y][x] == -1) {
                                continue;
                            }
                            coverage[y][x] = 1.0;
                        }
                    }
                } else if (ad.isInvolvedInCollison()) {
                    r.setEnabled(false);
                }
            }
        }

        accum += ((sum / max) / steps / robots.size() * 10);
        fitness = accum;
        
        super.update(simulator);
    }
    
    private void printGrid() {
		System.out.println();
		System.out.println();
		for(int y = coverage.length-1 ; y >= 0 ; y--) {
			for(int x = 0 ; x < coverage[y].length ; x++) {
				if(coverage[y][x] == -1) {
					System.out.print(" ");
				} else if(coverage[y][x] == 0) {
					System.out.print("_");
				} else if(coverage[y][x] == 1) {
					System.out.print("#");
				} else {
					System.out.print("<");
				}
			}
			System.out.println();
		}
	}

    @Override
    public double getFitness() {
        return 10 + fitness;
    }
}
