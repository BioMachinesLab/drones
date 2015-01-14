package utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtils {

	private static String ADDRESS = null;

	// In order to find our own IP address among all possible NICs,
	// we should at least know how it starts.
	private static final String ADDRESS_START = "192.168.3";

	public static String getAddress() {

		if (ADDRESS == null) {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();

					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						String next = enumIpAddr.nextElement().toString()
								.replace("/", "");
						if (next.startsWith(ADDRESS_START)) {
							ADDRESS = next;
							return next;
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return ADDRESS;
		}
	}

	public static String getAddress(String intfName) {

		if (ADDRESS == null) {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();

					if (intf.getName().equals(intfName)) {

						for (Enumeration<InetAddress> enumIpAddr = intf
								.getInetAddresses(); enumIpAddr
								.hasMoreElements();) {
							String next = enumIpAddr.nextElement().toString()
									.replace("/", "");
							if (next.startsWith(ADDRESS_START)) {
								ADDRESS = next;
								return next;
							}
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return ADDRESS;
		}
	}

}
