package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import simulation.Network;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.AquaticDroneCI;
import commoninterface.network.CommandConnectionListener;
import commoninterface.network.ConnectionListener;
import commoninterface.network.MotorConnectionListener;
import commoninterface.network.NetworkUtils;

public class MixedNetwork extends Network {
	
	protected int port = 8888;//port should be 8988 when running control console on the same computer
	protected int SEND_PORT = 8888;
	
	private MixedBroadcastReceiver receiver;
	private MixedBroadcastSender sender;
	private static MixedNetwork mixedNetwork;
	private boolean continueExecution = true;
	
	private ConnectionListener controllerListener;
	private CommandConnectionListener commandListener;
	private MotorConnectionListener motorListener;
	private GatewayRobot gatewayRobot;
	
	private String broadcastAddress = "";
	
	public MixedNetwork(Arguments args, Simulator sim) {
		super(args, sim);
		
		port = args.getArgumentAsIntOrSetDefault("port", port);

		broadcastAddress = NetworkUtils.getBroadcastAddress(NetworkUtils.getAddress());
		
		if(mixedNetwork != null) {
			mixedNetwork.shutdown();
		}
		
		mixedNetwork = this;
		
		try {
			receiver = new MixedBroadcastReceiver(InetAddress.getByName("0.0.0.0"), port);
			sender = new MixedBroadcastSender(InetAddress.getByName(NetworkUtils.getAddress()), port);
			receiver.start();
			
			gatewayRobot = new GatewayRobot(sim);
			
			controllerListener = new ConnectionListener(gatewayRobot);
			controllerListener.start();
			motorListener = new MotorConnectionListener(gatewayRobot);
			motorListener.start();
			commandListener = new CommandConnectionListener(gatewayRobot);
			commandListener.start();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void send(String senderAddress, String msg) {
		
		if(!continueExecution)
			return;
		
		for(int i = 0 ; i < sim.getRobots().size() ; i++) {
			Robot r = sim.getRobots().get(i);
			if(r instanceof AquaticDroneCI) {
				AquaticDroneCI aq = (AquaticDroneCI)r;
				aq.getBroadcastHandler().messageReceived(senderAddress, msg);
			}
		}
		
		for(int i = 0 ; i < sim.getRobots().size() ; i++) {
			Robot r = sim.getRobots().get(i);
			if(r instanceof AquaticDroneCI) {
				AquaticDroneCI aq = (AquaticDroneCI)r;
				aq.getBroadcastHandler().messageReceived(senderAddress, msg);
			}
		}
		sender.sendMessage(msg);
	}
	
	private void receive(String senderAddress, String msg) {
		
		if(!continueExecution)
			return;
		
		for(int i = 0 ; i < sim.getRobots().size() ; i++) {
			Robot r = sim.getRobots().get(i);
			if(r instanceof AquaticDroneCI) {
				AquaticDroneCI aq = (AquaticDroneCI)r;
				aq.getBroadcastHandler().messageReceived(senderAddress, msg);
			}
		}
	}

	@Override
	public void shutdown() {
		continueExecution = false;
		receiver.stopExecution();
		sender.stopExecution();
		controllerListener.closeConnections();
		controllerListener.shutdown();
		motorListener.closeConnections();
		motorListener.shutdown();
		commandListener.closeConnections();
		commandListener.shutdown();
	}
	
	class MixedBroadcastReceiver extends Thread{
		
		private int buffer_length = 15000;
		private DatagramSocket socket;
		private InetAddress ownAddress;

		public MixedBroadcastReceiver(InetAddress ownAddress, int port) {
			this.ownAddress = ownAddress;
			try {
				 socket = new DatagramSocket(port, ownAddress);
				 socket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {

			try {
				while (continueExecution) {
					byte[] recvBuf = new byte[buffer_length];
					DatagramPacket packet = new DatagramPacket(recvBuf,recvBuf.length);
					socket.receive(packet);

					String message = new String(packet.getData()).trim();
					if(!packet.getAddress().getHostAddress().equals(ownAddress)) {
						receive(packet.getAddress().getHostAddress(), message);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(socket != null)
					socket.close();
			}
		}
		
		public void stopExecution() {
			if(socket != null)
				socket.close();
		}
	}
	
	class MixedBroadcastSender {

		private DatagramSocket socket;

		public MixedBroadcastSender(InetAddress ownAddress, int port) {
			try {
				socket = new DatagramSocket(port+1, ownAddress);
				socket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String message) {
			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, InetAddress.getByName(broadcastAddress),
						SEND_PORT);
				socket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void stopExecution() {
			if(socket != null)
				socket.close();
		}
	}	
	
}