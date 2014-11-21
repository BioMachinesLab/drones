package commoninterface;

public interface DroneCI {
	public void    start();
	public void    shutdown();
	public void    setMotorSpeeds(double leftMotor, double rightMotor);
	public double  getCompassOrientaiton();
	public double  getGPSLatitude();
	public double  getGPSLongitude();
	public double  getTimeSinceStart();
}
