package network.messages;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import org.joda.time.LocalDateTime;

import com.pi4j.system.NetworkInfo;

public abstract class Message implements Serializable {
	private static final long serialVersionUID = 5764062121947005678L;
	private String senderName;
	private String senderIPAddr;
	private LocalDateTime timestamp;

	public Message() {
		try {
			timestamp = new LocalDateTime();
			if (System.getProperty("os.name").equals("Linux")
					&& System.getProperty("os.arch").equals("arm")) {

				senderName = NetworkInfo.getHostname();

				senderIPAddr = NetworkInfo.getIPAddress();
			} else {
				senderName = InetAddress.getLocalHost().getHostName();
				senderIPAddr = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Error fetching informations from system!");
			e.printStackTrace();
		}
	}

	public String getSenderName() {
		return senderName;
	}

	public String getSenderIPAddr() {
		return senderIPAddr;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
