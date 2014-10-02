package network.messages;

import java.io.IOException;
import java.text.ParseException;

import dataObjects.SystemInformationsData;

public class SystemInformationsMessage extends Message {
	private static final long serialVersionUID = -8333006310686657108L;
	private SystemInformationsData sysInformations;

	public SystemInformationsMessage() throws IOException, InterruptedException, ParseException {
		super();
		this.sysInformations = new SystemInformationsData();
	}

	public SystemInformationsData getSysInformations() {
		return sysInformations;
	}

	public void update() {
		sysInformations.update();
	}
}
