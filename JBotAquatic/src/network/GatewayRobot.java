package network;

import java.util.ArrayList;

import commoninterface.CISensor;
import commoninterface.RobotCI;
import commoninterface.network.ConnectionHandler;
import commoninterface.network.broadcast.BroadcastHandler;
import commoninterface.network.messages.Message;
import commoninterface.objects.Entity;
import commoninterface.utils.CIArguments;

public class GatewayRobot implements RobotCI {
	
	private MixedNetwork mixedNetwork;
	
	public GatewayRobot(MixedNetwork mixedNetwork) {
		this.mixedNetwork = mixedNetwork;
	}

	@Override
	public void begin(CIArguments args) {}

	@Override
	public void shutdown() {}

	@Override
	public void setMotorSpeeds(double leftMotor, double rightMotor) {
		mixedNetwork.setMotorSpeeds(leftMotor, rightMotor);
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
	public ArrayList<CISensor> getCISensors() {
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
	public void processInformationRequest(Message request,
			ConnectionHandler conn) {
		mixedNetwork.processInformationRequest(request, conn);
	}

	@Override
	public String getInitMessages() {
		return null;
	}

	@Override
	public void reset() {}

}
