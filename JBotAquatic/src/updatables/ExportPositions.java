package updatables;

import java.io.File;
import java.io.FileWriter;

import simulation.Simulator;
import simulation.Stoppable;
import simulation.Updatable;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ExportPositions implements Updatable,Stoppable{
	
	private int time = 0;
	private StringBuffer b = new StringBuffer();
	private String file = "exportpositions";
	
	public ExportPositions(Arguments args) {
		time = args.getArgumentAsIntOrSetDefault("time", time);
		file = args.getArgumentAsStringOrSetDefault("file", file);
	}
	
	@Override
	public void update(Simulator simulator) {

		if(simulator.getTime() % time == 0) {
			for(Robot r : simulator.getRobots()) {
				b.append(simulator.getTime().intValue()+" "+r.getId()+" "+r.getPosition().getX()+" "+r.getPosition().getY()+"\n");
			}
		}
	}
	
	@Override
	public void terminate(Simulator simulator) {
		try {
			FileWriter fw = new FileWriter(new File("exportpositions/"+file+".txt"));
			fw.write(b.toString());
			fw.close();
		}catch(Exception e){e.printStackTrace();}
	}

}
