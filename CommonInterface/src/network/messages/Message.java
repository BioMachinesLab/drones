package network.messages;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import org.joda.time.LocalDateTime;

import commoninterface.network.NetworkUtils;

public abstract class Message implements Serializable {
	private static final long serialVersionUID = 5764062121947005678L;
	private String senderIPAddr;
	private LocalDateTime timestamp;

	public Message() {
		try {
			timestamp = new LocalDateTime();
			if (System.getProperty("os.name").equals("Linux")
					&& System.getProperty("os.arch").equals("arm")) {

				senderIPAddr = NetworkUtils.getAddress();
			} else {
				senderIPAddr = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (IOException e) {
			System.err.println("Error fetching informations from system!");
			e.printStackTrace();
		}
	}

	public String getSenderIPAddr() {
		return senderIPAddr;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
