package fieldtests.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.joda.time.DateTime;

import commoninterface.utils.logger.LogData;

public class Experiment implements Serializable{
	
	public DateTime start;
	public DateTime end;
	public int timeSteps;
	public String controllerName;
	public int controllerNumber;
	public ArrayList<Integer> robots = new ArrayList<Integer>();
	public int sample;
	public ArrayList<LogData> logs = new ArrayList<LogData>();
	public int activeRobot = -1;
	
	@Override
	public String toString() {
		return controllerName+""+controllerNumber+"_"+sample+"_"+robots.size();
	}

}
