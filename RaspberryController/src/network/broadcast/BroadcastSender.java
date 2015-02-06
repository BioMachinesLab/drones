package network.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastSender {

	private DatagramSocket socket;
	private int port;

	public BroadcastSender(String ownAddress, int port) {
		this.port = port;
		try {
			System.out.println("BroadcastSender own address is "+ownAddress);
			socket = new DatagramSocket(port+1, InetAddress.getByName(ownAddress));
			socket.setBroadcast(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		try {
			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, InetAddress.getByName("255.255.255.255"),
					port);
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}