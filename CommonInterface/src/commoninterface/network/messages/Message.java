package commoninterface.network.messages;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.joda.time.LocalDateTime;

import commoninterface.network.NetworkUtils;

/**
 * Class to be optimized: getting the name of the interfaces an comparing them
 * as strings......
 * 
 * @author Vasco Craveiro Costa
 *
 */
public abstract class Message implements Serializable {
	protected static final long serialVersionUID = 5764062121947005678L;
	protected String senderIPAddr;
	protected LocalDateTime timestamp;
	protected String senderHostname;

	private String[] cabledIntefaces = { "Atheros AR8151" };
	private String[] wirelessIntefaces = { "Atheros AR9002WB-1NG" };

	private boolean interfaceType = false; // false for wireless, true for cable

	public Message(String senderHostname) {
		this.senderHostname = senderHostname;

		try {
			timestamp = new LocalDateTime();
			if (System.getProperty("os.name").toLowerCase().indexOf("Linux") >= 0
					&& System.getProperty("os.arch").toLowerCase()
							.indexOf("arm") >= 0) {

				senderIPAddr = NetworkUtils.getAddress();
			} else {
				if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
					senderIPAddr = getInterfaceIPAddress();
				} else {
					senderIPAddr = InetAddress.getLocalHost().getHostAddress();
				}
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

	private String getInterfaceIPAddress() {
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();

			String[] toCompareInterfaces;
			if (interfaceType) {
				toCompareInterfaces = cabledIntefaces;
			} else {
				toCompareInterfaces = wirelessIntefaces;
			}

			for (NetworkInterface netint : Collections.list(nets)) {
				for (String str : toCompareInterfaces) {
					String name = netint.getDisplayName();
					if (name != null && name.indexOf(str) >= 0) {
						Enumeration<InetAddress> inetAddresses = netint
								.getInetAddresses();
						return Collections.list(inetAddresses).get(0)
								.toString().substring(1);
					}
				}
			}

			return null;
		} catch (SocketException e) {
			System.err.println("Unable to get intefaces informations");
			System.err.println(e.getMessage());

			return null;
		}
	}

	public String getSenderHostname() {
		return senderHostname;
	}
	
	public abstract Message getCopy();
}
