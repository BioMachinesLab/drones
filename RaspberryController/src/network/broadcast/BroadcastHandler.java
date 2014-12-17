package network.broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import utils.NetworkUtils;
import commoninterfaceimpl.RealAquaticDroneCI;

/**
 * This class handles everything related to receiving and sending broadcast messages.
 * New types of messages can be periodically sent through BroadcastStatusThreads.
 * 
 * @author miguelduarte42
 */
public class BroadcastHandler {
	
	private static int PORT = 8888;
	private static int BUFFER_LENGTH = 15000;
	private BroadcastSender sender;
	private BroadcastReceiver receiver;
	private RealAquaticDroneCI drone;
	private String ownAddress;
	private ArrayList<BroadcastStatusThread> statusThreads = new ArrayList<BroadcastStatusThread>();
	
	public BroadcastHandler(RealAquaticDroneCI drone) {
		this.drone = drone;
		receiver = new BroadcastReceiver();
		sender = new BroadcastSender();
		receiver.start();
		
		initStatusThreads();
	}
	
	private void initStatusThreads() {
		statusThreads.add(new HeartbeatStatusThread(this));
		statusThreads.add(new PositionStatusThread(this));
		
		for(BroadcastStatusThread bst : statusThreads)
			bst.start();
	}
	
	public void messageReceived(String address, String message) {
		//TODO possibly retransmit. We also have to react to certain messages,
		//such as starting behaviors, for instance.
		
		//System.out.println("Received "+message+" from "+address);
	}
	
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}
	
	public RealAquaticDroneCI getDrone() {
		return drone;
	}
	
	class BroadcastSender {
		
		private DatagramSocket socket;
		
		public BroadcastSender() {
			try {
				InetAddress ownInetAddress = InetAddress.getByName(NetworkUtils.getAddress());
				ownAddress = ownInetAddress.getHostAddress();
				socket = new DatagramSocket(PORT+1, ownInetAddress);
				socket.setBroadcast(true);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void sendMessage(String message) {
			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PORT);
				socket.send(sendPacket);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class BroadcastReceiver extends Thread {
		
		private DatagramSocket socket;

		public BroadcastReceiver() {
			try {
				 socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
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
					
					if(!packet.getAddress().getHostAddress().equals(ownAddress))
						messageReceived(packet.getAddress().getHostAddress(), message);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}