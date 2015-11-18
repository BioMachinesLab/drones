package fieldtests.updatables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.Waypoint;
import commoninterface.utils.CoordinateUtilities;
import simulation.Simulator;
import simulation.Updatable;
import simulation.physicalobjects.Line;
import simulation.robot.AquaticDrone;
import simulation.util.Arguments;

public class Coverage implements Updatable{
	
	private double[][] coverage;
    private double resolution = 1;
    private double width = 5, height = 5;
    private double decrease = 0.001; //1000 steps to go from 1.0 to 0.0
    private double distance = 10;
    private double min = 0;
    private double max = 0;
    private List<Line> lines;
    
    public Coverage(Arguments args) {
    	resolution = args.getArgumentAsDoubleOrSetDefault("resolution", resolution);
        distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
        decrease = args.getArgumentAsDoubleOrSetDefault("decrease", decrease);
        min = args.getArgumentAsDoubleOrSetDefault("min", min);
        max = args.getArgumentAsDoubleOrSetDefault("max", max);
	}
    
    public void setup(Simulator simulator){
    	// SET GEOFENCE
        AquaticDrone ad = (AquaticDrone) simulator.getRobots().get(0);
        
        ArrayList<GeoFence> fences = GeoFence.getGeoFences(ad);
        
        if(fences.isEmpty())
        	return;
        
        GeoFence fence = fences.get(0);
        
        lines = getLines(fence.getWaypoints(), simulator);

        double maxXAbs = 0, maxYAbs = 0;

        for (Line l : lines) {
            maxXAbs = Math.max(maxXAbs, Math.abs(l.getPointA().x));
            maxYAbs = Math.max(maxYAbs, Math.abs(l.getPointA().y));
        }

        width = Math.max(maxXAbs, maxYAbs) * 2;
        height = width;
        coverage = new double[(int) (height / resolution)][(int) (width / resolution)];
        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {
                double coordX = (x - coverage[y].length / 2) * resolution;
                double coordY = (y - coverage.length / 2) * resolution;
                if (!insideLines(new Vector2d(coordX, coordY))) {
                    coverage[y][x] = -1;
                }
            }
        }
    }
    
    @Override
    public void update(Simulator simulator) {
    	if(coverage == null)
    		setup(simulator);
    	
    	if(coverage == null)
    		return;
    	
        for (int y = 0; y < coverage.length; y++) {
            for (int x = 0; x < coverage[y].length; x++) {

                if (coverage[y][x] == -1) {
                    continue;
                }

                if (coverage[y][x] > 0) {
                    if (coverage[y][x] <= max) {
                        coverage[y][x] -= decrease;
                        if (coverage[y][x] < min) {
                            coverage[y][x] = min;
                        }
                    }
                }

            }
        }
    }
    
    public void addPoint(Simulator sim, Vector2d pos, double value) {
    	
    	if(coverage == null)
    		setup(sim);
    	
    	if(coverage == null)
    		return;
    	
    	if (insideLines(pos)) {
    		
            double rX = pos.getX();
            double rY = pos.getY();

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
                	
                	double distX = Math.abs((pMinX+pMaxX)/2 - x);
                	double distY = Math.abs((pMinY+pMaxY)/2 - y);
                	
                	if(Math.sqrt(Math.pow(distX, 2)+Math.pow(distY,2)) > Math.abs((pMaxX-pMinX))/2) {
                		continue;
                	}

                    if (x >= coverage[y].length || x < 0) {
                        continue;
                    }

                    if (coverage[y][x] == -1) {
                        continue;
                    }
                    coverage[y][x] = value;
                }
            }
        }
    }
    
    public double[][] getCoverage() {
		return coverage;
	}
    
    public boolean insideLines(Vector2d v) {
        int count = 0;
        for (Line l : lines) {
            if (l.intersectsWithLineSegment(v, new Vector2d(0, -Integer.MAX_VALUE)) != null) {
                count++;
            }
        }
        return count % 2 != 0;
    }
    
    protected List<Line> getLines(LinkedList<Waypoint> waypoints, Simulator simulator) {
        List<Line> linesList = new ArrayList<Line>();
        for (int i = 1; i < waypoints.size(); i++) {

            Waypoint wa = waypoints.get(i - 1);
            Waypoint wb = waypoints.get(i);
            commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
            commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

            simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(
            		simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
            linesList.add(l);
        }

        Waypoint wa = waypoints.get(waypoints.size() - 1);
        Waypoint wb = waypoints.get(0);
        commoninterface.mathutils.Vector2d va = CoordinateUtilities.GPSToCartesian(wa.getLatLon());
        commoninterface.mathutils.Vector2d vb = CoordinateUtilities.GPSToCartesian(wb.getLatLon());

        simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(
        		simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
        linesList.add(l);
        return linesList;
    }

}
