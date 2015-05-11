package dataObjects;

import java.net.Inet4Address;

public class DroneData {
	private String name="";
	private Inet4Address ipAddr;
	
	public DroneData(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	// TO-DO put more information!
}
