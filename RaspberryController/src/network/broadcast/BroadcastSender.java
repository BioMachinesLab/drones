package network.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import commoninterface.network.NetworkUtils;

public class BroadcastSender {

	private static boolean DEBUG = false;
	
	private DatagramSocket socket;
	private int port;

	private String broacastAddress;
	
	public BroadcastSender(String ownAddress, int port) {
		this.port = port;
		broacastAddress = NetworkUtils.getBroadcastAddress(ownAddress);
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
			if(DEBUG)
				System.out.println("Broadcasting "+message);
			byte[] sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, InetAddress.getByName(broacastAddress),
					port);
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}