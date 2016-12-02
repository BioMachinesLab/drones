package fieldtests.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.joda.time.DateTime;

import commoninterface.utils.logger.ToLogData;

public class Experiment implements Serializable {
	private static final long serialVersionUID = 5213253883943586641L;
	public DateTime start;
	public DateTime end;
	public int timeSteps;
	public String controllerName;
	public int controllerNumber;
	public ArrayList<Integer> robots = new ArrayList<Integer>();
	public int sample;
	public ArrayList<ToLogData> logs = new ArrayList<ToLogData>();
	public int activeRobot = -1;

	@Override
	public String toString() {
		return controllerName + "" + controllerNumber + "_" + sample + "_" + robots.size();
	}

}
