package environment;

import java.util.Random;

import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.diagram.PowerDiagram;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class VoronoiEnvironment extends BoundaryEnvironment{
	
	//Arlind Nocaj, Ulrik Brandes, "Computing Voronoi Treemaps: Faster, Simpler, and Resolution-independent", Computer Graphics Forum, vol. 31, no. 3, June 2012, pp. 855-864
	
	protected PolygonSimple rootPolygon;
	protected PowerDiagram voronoiDiagram;
	protected OpenList sites;
	
	public VoronoiEnvironment(Simulator simulator, Arguments args) {
		super(simulator,args);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		// create a root polygon which limits the voronoi diagram.
		//  here it is just a rectangle.
		rootPolygon = new PolygonSimple();
		
		rootPolygon.add(-width/2, -height/2);
		rootPolygon.add(width/2, -height/2);
		rootPolygon.add(width/2, height/2);
		rootPolygon.add(-width/2, height/2);   
		
	}
	
	@Override
	public void update(double time) {
		
		voronoiDiagram = new PowerDiagram();

		// normal list based on an array
		sites = new OpenList();

	    for (Robot r : getRobots()) {
	        Site site = new Site(r.getPosition().x, r.getPosition().y);
	        // we could also set a different weighting to some sites
	        // site.setWeight(30)
	        sites.add(site);
	    }

		// set the list of points (sites), necessary for the power diagram
		voronoiDiagram.setSites(sites);
		// set the clipping polygon, which limits the power voronoi diagram
		voronoiDiagram.setClipPoly(rootPolygon);

		// do the computation
		voronoiDiagram.computeDiagram();   
		
	}
	
	public OpenList getSites() {
		return sites;
	}

}
