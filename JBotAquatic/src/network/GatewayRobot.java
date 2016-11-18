package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commoninterface.CIBehavior;
import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.MessageProvider;
import commoninterface.utils.CIArguments;
import commoninterface.utils.RobotLogger;
import simulation.Simulator;
import simulation.robot.Robot;

public class GatewayRobot implements RobotCI {

	private int chosenIndex = 0;
	private Simulator sim;

	public GatewayRobot(Simulator sim) {
		this.sim = sim;
	}

	@Override
	public void begin(HashMap<String, CIArguments> args) {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void processInformationRequest(Message request, ConnectionHandler conn) {
		for (int i = 0; i < sim.getRobots().size(); i++) {
			Robot r = sim.getRobots().get(i);
			if (r instanceof RobotCI) {
				RobotCI robot = (RobotCI) r;

				// if(robot instanceof AquaticDroneCI)
				// if(((AquaticDroneCI)robot).getDroneType() !=
				// AquaticDroneCI.DroneType.DRONE)
				// continue;

				robot.processInformationRequest(request, i == chosenIndex ? conn : null);
			}
		}
	}

	@Override
	public void setMotorSpeeds(double leftMotor, double rightMotor) {
		if (sim.getRobots().size() > chosenIndex) {
			RobotCI robot = (RobotCI) sim.getRobots().get(chosenIndex);
			robot.setMotorSpeeds(leftMotor, rightMotor);
		}
	}

	@Override
	public double getTimeSinceStart() {
		return 0;
	}

	@Override
	public ArrayList<Entity> getEntities() {
		return null;
	}

	@Override
	public void setEntities(ArrayList<Entity> entities) {
	}

	@Override
	public ArrayList<CISensor> getCISensors() {
		return null;
	}

	@Override
	public CISensor getCISensorByType(Class<? extends CISensor> c) {
		return null;
	}

	@Override
	public String getNetworkAddress() {
		return null;
	}

	@Override
	public BroadcastHandler getBroadcastHandler() {
		return null;
	}

	@Override
	public String getInitMessages() {
		return null;
	}

	@Override
	public void reset() {
	}

	@Override
	public RobotLogger getLogger() {
		return null;
	}

	@Override
	public List<MessageProvider> getMessageProviders() {
		return null;
	}

	@Override
	public String getStatus() {
		return null;
	}

	@Override
	public void startBehavior(CIBehavior b) {
		for (int i = 0; i < sim.getRobots().size(); i++) {
			Robot r = sim.getRobots().get(i);
			if (r instanceof RobotCI) {
				RobotCI robot = (RobotCI) r;

				// if(robot instanceof AquaticDroneCI)
				// if(((AquaticDroneCI)robot).getDroneType() !=
				// AquaticDroneCI.DroneType.DRONE)
				// continue;

				robot.startBehavior(b);
			}
		}
	}

	@Override
	public void stopActiveBehavior() {
		for (int i = 0; i < sim.getRobots().size(); i++) {
			Robot r = sim.getRobots().get(i);
			if (r instanceof RobotCI) {
				RobotCI robot = (RobotCI) r;

				// if(robot instanceof AquaticDroneCI)
				// if(((AquaticDroneCI)robot).getDroneType() !=
				// AquaticDroneCI.DroneType.DRONE)
				// continue;

				robot.stopActiveBehavior();
			}
		}
	}

	@Override
	public CIBehavior getActiveBehavior() {
		return null;
	}

	@Override
	public double getLeftMotorSpeed() {
		Robot r = sim.getRobots().get(chosenIndex);
		if (r instanceof RobotCI) {
			return ((RobotCI) r).getLeftMotorSpeed();
		}
		return 0;
	}

	@Override
	public double getRightMotorSpeed() {
		Robot r = sim.getRobots().get(chosenIndex);
		if (r instanceof RobotCI) {
			return ((RobotCI) r).getRightMotorSpeed();
		}
		return 0;
	}

	@Override
	public void replaceEntity(Entity e) {
	}

	@Override
	public void setProperty(String name, String value) {

	}

	@Override
	public RobotLogger getEntityLogger() {
		return null;
	}

}
