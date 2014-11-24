package main;

public class Test {
	
	public static void main(String[] args) {
		
		double lat1 = 38.74871;
		double lon1 = -9.15376;
		
		double lat2 = 38.748909;
		double lon2 = -9.153572;
		
		double angle = calculateCoordinatesAngle(lat1,lon1,lat2,lon2);
		double distance = calculateDistance(lat1,lon1,lat2,lon2);
		
		double orientation = 10;
		
		double difference = angle - orientation;
		
		if(difference > 360)
			difference-=360;
		if(difference < 0)
			difference+=360;
		
		System.out.println(distance);
		System.out.println(angle);
		System.out.println(difference);
	}
	
	private static double calculateCoordinatesAngle(double lat1, double lon1, double lat2, double lon2) {
		
		double angle = 0;
		
		double dy = lat2 - lat1;
		double dx = Math.cos(Math.PI/180*lat1)*(lon2 - lon1);
		angle = Math.atan2(dy, dx);
		
		return -(Math.toDegrees(angle) - 90);
	}
	
	private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
		
		double distance = 0;
		
		double R = 6371; // earth's radius in km
		double r1 = Math.toRadians(lat1);
		double r2 = Math.toRadians(lat2);
		double latDelta = Math.toRadians(lat2-lat1);
		double lonDelta = Math.toRadians(lon2-lon1);

		double a = Math.sin(latDelta/2) * Math.sin(latDelta/2) + Math.cos(r1) * Math.cos(r2) * Math.sin(lonDelta/2) * Math.sin(lonDelta/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		distance = R * c;
		
		return distance;
	}
}