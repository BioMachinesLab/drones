package commoninterface.utils.logger;

import java.io.Serializable;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import commoninterface.AquaticDroneCI;
import commoninterface.entities.Entity;
import commoninterface.utils.jcoord.LatLon;

public class LogData implements Comparable<LogData>,Serializable {
	
	static DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-YY_HH:mm:ss.SS");
	
	public String file = null;
	public int timestep = -1;
	public String comment = null;
	public AquaticDroneCI.DroneType droneType = null;

	// System informations
	public String systemTime = null;
	public String ip = null;

	// Inputs/ Telemetry
	public LatLon latLon = null;
	public double GPSorientation = -1;
	public double GPSspeed = -1;
	public String GPSdate = null;
	public double compassOrientation = -1;
	public double[] temperatures = null;

	// Outputs
	public double[] motorSpeeds = null;

	// Controller
	public double[] inputNeuronStates = null;
	public double[] outputNeuronStates = null;
	
	//Entities
	public ArrayList<Entity> entities;

	@Override
	public int compareTo(LogData o) {
		return DateTime.parse(GPSdate,formatter).compareTo(DateTime.parse(o.GPSdate,formatter));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogData) {
			LogData object = (LogData) obj;

			boolean a = (file == null && object.file == null) || file.equals(object.file);
			boolean b = timestep == object.timestep;
			boolean c = (comment == null && object.comment == null)	|| comment.equals(object.comment);
			boolean d = droneType.equals(object.droneType);
			boolean e = (systemTime == null && object.systemTime == null) || systemTime.equals(object.systemTime);
			boolean f = (ip == null && object.ip == null) || ip.equals(object.ip);
			boolean g = (latLon == null && object.latLon == null) || latLon.equals(object.latLon);
			boolean h = GPSorientation == object.GPSorientation;
			boolean i = GPSspeed == object.GPSspeed;
			boolean j = (GPSdate == null && object.GPSdate == null) || GPSdate.equals(object.GPSdate);
			boolean k = compassOrientation == object.compassOrientation;
			boolean l = (temperatures == null && object.temperatures == null) || temperatures.equals(object.temperatures);
			boolean m = (motorSpeeds == null && object.motorSpeeds == null) || motorSpeeds.equals(object.motorSpeeds);
			boolean n = (inputNeuronStates == null && object.inputNeuronStates == null) || inputNeuronStates.equals(object.inputNeuronStates);
			boolean o = (outputNeuronStates == null && object.outputNeuronStates == null) || outputNeuronStates.equals(object.outputNeuronStates);

			return a && b && c && d && e && f && g && h && i && j && k && l	&& m && n && o;
		} else {
			return false;
		}
	}
}