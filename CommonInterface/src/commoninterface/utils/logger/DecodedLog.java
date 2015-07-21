package commoninterface.utils.logger;

import commoninterface.utils.logger.LogCodex.LogType;

public class DecodedLog {

	LogType payloadType;
	Object payload;

	public DecodedLog(LogType payloadType, Object payload) {
		this.payload = payload;
		this.payloadType = payloadType;
	}

	public Object getPayload() {
		return payload;
	}

	public LogType payloadType() {
		return payloadType;
	}
}