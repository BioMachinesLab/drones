package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class CameraCaptureMessage extends Message {

	private static final long serialVersionUID = -2683957128513511562L;
	private byte[] frameBytes;

	public CameraCaptureMessage(byte[] frameBytes, String senderHostname) {
		super(senderHostname);
		this.frameBytes = frameBytes;
	}

	public byte[] getFrameBytes() {
		return frameBytes;
	}
	
	@Override
	public Message getCopy() {
		return new CameraCaptureMessage(frameBytes, getSenderHostname());
	}

}
