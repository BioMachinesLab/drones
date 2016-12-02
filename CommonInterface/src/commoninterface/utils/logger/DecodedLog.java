package commoninterface.utils.logger;

import commoninterface.utils.logger.LogCodex.LogType;

public class DecodedLog {

	LogType payloadType;
	Object[] payload;
	double timeStep;

	public DecodedLog(LogType payloadType, Object... payload) {
		this.payload = payload;
		this.payloadType = payloadType;
	}

	public Object[] getPayload() {
		return payload;
	}

	public LogType getPayloadType() {
		return payloadType;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
}