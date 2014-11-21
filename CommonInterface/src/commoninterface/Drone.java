package commoninterface;

public interface Drone {
	public void    start();
	public void    shutdown();
	public void    setSpeed(double leftMotor, double rightMotor);
	public double  getOrientaiton();
	public double  getGPSLatitude();
	public double  getGPSLongitude();
	public double  getTimeSinceStart();
}
