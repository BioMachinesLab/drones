package network.messages;

import dataObjects.SystemInformationsData;


public class SystemInformationsMessage extends Message {
	private static final long serialVersionUID = -8333006310686657108L;
	private SystemInformationsData sysInformations;

	public SystemInformationsMessage(SystemInformationsData sysInformations) {
		super();
		this.sysInformations = sysInformations;
	}

	public SystemInformationsData getSysInformations() {
		return sysInformations;
	}
}
