package network.broadcast;

import gui.DroneGUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import main.RobotControlConsole;
import commoninterface.entities.RobotLocation;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;

public class ConsoleBroadcastHandler {
	
	private static int PORT = 8888;
	private static int RETRANSMIT_PORT = 8888+100;
	private static int BUFFER_LENGTH = 15000;
	private BroadcastSender sender;
	private BroadcastReceiver receiver;
	private RobotControlConsole console;
	private String ownAddress;
	//TODO this has to be true for the mixed experiments to work
	private boolean retransmit = false;
	
	public ConsoleBroadcastHandler(RobotControlConsole console) {
		this.console = console;
		receiver = new BroadcastReceiver();
		sender = new BroadcastSender();
		receiver.start();
	}
	
	public void messageReceived(String address, String message) {
		newBroadcastMessage(address, message);
	}
	
	public void sendMessage(String message) {
		sender.sendMessage(message);
	}
	
	class BroadcastSender {
		
		private DatagramSocket socket;
		private DatagramSocket retransmitSocket;
		
		public BroadcastSender() {
			try {
				InetAddress ownInetAddress = InetAddress.getByName(NetworkUtils.getAddress());
				ownAddress = ownInetAddress.getHostAddress();
				System.out.println("SENDER "+ownInetAddress);
				socket = new DatagramSocket(PORT+1, ownInetAddress);
				socket.setBroadcast(true);
				
				if(retransmit) {
					retransmitSocket = new DatagramSocket(RETRANSMIT_PORT-1,ownInetAddress);
					retransmitSocket.setBroadcast(true);
				}
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
		
		public void retransmit(String message) {
			
			if(!retransmit)
				return;
			
			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), RETRANSMIT_PORT);
				retransmitSocket.send(sendPacket);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void newBroadcastMessage(String address, String message) {
		
		String[] split = message.split(BroadcastMessage.MESSAGE_SEPARATOR);
		switch(split[0]) {
			case "HEARTBEAT":
				if(!address.equals(ownAddress)) {
					long timeElapsed = HeartbeatBroadcastMessage.decode(message);
					console.getGUI().getConnectionPanel().newAddress(address);
				}
				break;
			case "GPS":
				RobotLocation di = PositionBroadcastMessage.decode(address, message);
				if(di != null) {
					if(console.getGUI() instanceof DroneGUI) {
						((DroneGUI)console.getGUI()).getMapPanel().displayData(di);
					}
					console.getGUI().getConnectionPanel().newAddress(address);
				}
				
				break;
			default:
				System.out.println("Uncategorized message > "+message+" < from "+address);
		}
		
		if(retransmit)
			sender.retransmit(message);
	}
	
	class BroadcastReceiver extends Thread {
		
		private DatagramSocket socket;

		public BroadcastReceiver() {
			try {
				System.out.println("RECEIVER " + InetAddress.getByName("0.0.0.0") + ", port: " + PORT);
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
					
//					if(!packet.getAddress().getHostAddress().equals(ownAddress))
					messageReceived(packet.getAddress().getHostAddress(), message);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}