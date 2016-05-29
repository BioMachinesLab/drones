import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkInterfaces {
	public static void main(String[] args) throws SocketException {

		for (Enumeration<NetworkInterface> enumInterfaces = NetworkInterface
				.getNetworkInterfaces(); enumInterfaces.hasMoreElements();) {
			NetworkInterface intf = enumInterfaces.nextElement();
			if (intf.getName().equals("wlan0")) {
				System.out.println("Encontrei!!!!!");
				break;
			}
		}
	}
}
