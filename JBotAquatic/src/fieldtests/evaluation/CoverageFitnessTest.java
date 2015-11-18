package fieldtests.evaluation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.Line;
import simulation.robot.AquaticDrone;
import simulation.robot.Robot;
import simulation.util.Arguments;
import updatables.CoverageTracer;
import commoninterface.AquaticDroneCI.DroneType;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import evaluation.AvoidCollisionsFunction;

public class CoverageFitnessTest extends AvoidCollisionsFunction {

    private boolean isSetup = false;
    private double[][] coverage;
    private double resolution = 1;
    private double width = 5, height = 5;
    private double decrease = 1000; //1000 steps to go from 1.0 to 0.0
    private double accum = 0;
    private List<Line> lines;

    private double max = 0;
    private double distance = 10;
    private double steps = 0;

    private boolean instant = false;
    private boolean useGPS = false;

    public CoverageFitnessTest(Arguments args) {
        super(args);
        resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        instant = args.getFlagIsTrue("instant");
        decrease = 1.0 / args.getArgumentAsDoubleOrSetDefault("decrease",decrease);
        useGPS = args.getFlagIsTrue("usegps");
    }

    public void setup(Simulator simulator) {
        // SET GEOFENCE
        AquaticDrone ad = (AquaticDrone) simulator.getRobots().get(0);
        GeoFence fence = null;
        for (Entity e : ad.getEntities()) {
            if (e instanceof GeoFence) {
                fence = (GeoFence) e;
                break;
            }
        }
        lines = getLines(fence.getWaypoints(), simulator);

        double maxXAbs = 0, maxYAbs = 0;

        for (Line l : lines) {
            maxXAbs = Math.max(maxXAbs, Math.abs(l.getPointA().x));
            maxYAbs = Math.max(maxYAbs, Math.abs(l.getPointA().y));
        }

        width = Math.max(maxXAbs, maxYAbs) * 2;
        height = width;
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
        int count = 0;
        for (Line l : lines) {
            if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
                count++;
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
                        coverage[y][x] -= decrease;
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
                
                if (ad.getDroneType()== DroneType.DRONE && insideLines(r.getPosition(), simulator)) {
                	
                    double rX = ad.getPosition().getX();
                    double rY = ad.getPosition().getY();
                    
                    if(useGPS) {
                    	commoninterface.mathutils.Vector2d vpos = CoordinateUtilities.GPSToCartesian(ad.getGPSLatLon());
                    	rX = vpos.x;
                    	rY = vpos.y;
                    }

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

                            double distX = Math.abs((pMinX + pMaxX) / 2 - x);
                            double distY = Math.abs((pMinY + pMaxY) / 2 - y);

                            if (Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2)) > Math.abs((pMaxX - pMinX)) / 2) {
                                continue;
                            }

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
//                    r.setEnabled(false);
                }
            }
        }
        
        if (instant) {
            fitness = sum / max;
        } else {
            accum += ((sum / max) / steps);
            fitness = accum;
        }

        if (simulator.getTime() == 0) {
            for (Updatable u : simulator.getCallbacks()) {
                if (u instanceof CoverageTracer) {
                    CoverageTracer ct = (CoverageTracer) u;
                    ct.setCoverage(coverage, resolution);
                    ct.update(simulator);
                }
            }
        }

    }
    
    public double[][] getCoverage() {
		return coverage;
	}

    @Override
    public double getFitness() {
        return fitness;
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
