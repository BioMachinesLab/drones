package network.broadcast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import commoninterface.network.broadcast.BroadcastHandler;

public class BroadcastReceiver extends Thread{
	
	private static int BUFFER_LENGTH = 15000;
	private DatagramSocket socket;
	private String ownAddress;
	private BroadcastHandler bh;
	private static boolean DEBUG = false;

	public BroadcastReceiver(BroadcastHandler bh, String ownAddress, int port) {
		this.ownAddress = ownAddress;
		this.bh = bh;
		try {
			 socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
			 socket.setBroadcast(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {
			while (true) {
				byte[] recvBuf = new byte[BUFFER_LENGTH];
				DatagramPacket packet = new DatagramPacket(recvBuf,recvBuf.length);
				socket.receive(packet);

				String message = new String(packet.getData()).trim();
				
				if(DEBUG)
					System.out.println("Receiving "+message);
				
				if(!packet.getAddress().getHostAddress().equals(ownAddress)) {
					bh.messageReceived(packet.getAddress().getHostAddress(), message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}