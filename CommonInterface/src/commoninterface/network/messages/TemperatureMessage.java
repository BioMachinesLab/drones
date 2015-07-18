package commoninterface.network.messages;

import commoninterface.network.messages.Message;

public class TemperatureMessage extends Message {
	private static final long serialVersionUID = 7925900141790370630L;
	private double[] temperature;

	public TemperatureMessage(double[] temperature, String senderHostname) {
		super(senderHostname);
		this.temperature = temperature;
	}

	public double[] getTemperature() {
		return temperature;
	}
	
	@Override
	public Message getCopy() {
		return new TemperatureMessage(temperature, senderHostname);
	}

}
