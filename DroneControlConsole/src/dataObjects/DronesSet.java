package dataObjects;

import java.util.ArrayList;
import java.util.HashMap;

public class DronesSet {	
		private HashMap<String, DroneData> dronesSet = new HashMap<>();
		
		public void addDrone(DroneData drone){
			dronesSet.put(drone.getName(), drone);
		}
		
		public void removeDrone(String droneName){
			dronesSet.remove(droneName);
		}
		
		public void removeDrone(DroneData drone){
			dronesSet.remove(drone.getName(), drone);
		}
		
		public DroneData getDrone(String name){
			return dronesSet.get(name);
		}
		
		public boolean existsDrone(String name){
			return dronesSet.containsKey(name);
		}
		
		public ArrayList<DroneData> getDronesSet(){
			return new ArrayList<DroneData>(dronesSet.values());
		}
}
