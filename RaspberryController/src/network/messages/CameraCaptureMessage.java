package network.messages;


public class CameraCaptureMessage extends Message {

	private static final long serialVersionUID = -2683957128513511562L;
	private byte[] frameBytes;


	public CameraCaptureMessage(byte[] frameBytes) {
		super();
		this.frameBytes = frameBytes;
	}

	public byte[] getFrameBytes() {
		return frameBytes;
	}
	
}
