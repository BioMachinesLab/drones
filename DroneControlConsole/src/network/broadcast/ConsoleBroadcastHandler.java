package network.broadcast;

import gui.DroneGUI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import main.DroneControlConsole;
import main.RobotControlConsole;
import commoninterface.entities.Entity;
import commoninterface.entities.RobotLocation;
import commoninterface.network.NetworkUtils;
import commoninterface.network.broadcast.BroadcastMessage;
import commoninterface.network.broadcast.EntitiesBroadcastMessage;
import commoninterface.network.broadcast.HeartbeatBroadcastMessage;
import commoninterface.network.broadcast.PositionBroadcastMessage;
import dataObjects.DroneData;
import dataObjects.DronesSet;

public class ConsoleBroadcastHandler {

	public static int PORT = 8888;
	public static int RETRANSMIT_PORT = 8888 + 100;
	public static int BUFFER_LENGTH = 15000;

	private BroadcastSender sender;
	private BroadcastReceiver receiver;
	private RobotControlConsole console;
	private String ownAddress;

	// TODO this has to be true for the mixed experiments to work
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

	public void closeConnections() {
		receiver.shutdown();
		sender.shutdown();
	}

	class BroadcastSender {

		private DatagramSocket socket;
		private DatagramSocket retransmitSocket;

		public BroadcastSender() {
			try {
				InetAddress ownInetAddress = InetAddress.getByName(NetworkUtils
						.getAddress());
				ownAddress = ownInetAddress.getHostAddress();
				System.out.println("SENDER " + ownInetAddress);
				socket = new DatagramSocket(PORT + 1, ownInetAddress);
				socket.setBroadcast(true);

				if (retransmit) {
					retransmitSocket = new DatagramSocket(RETRANSMIT_PORT - 1,
							ownInetAddress);
					retransmitSocket.setBroadcast(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String message) {
			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length,
						InetAddress.getByName("255.255.255.255"), PORT);
				socket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void retransmit(String message) {

			if (!retransmit)
				return;

			try {
				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length,
						InetAddress.getByName("255.255.255.255"),
						RETRANSMIT_PORT);
				retransmitSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void shutdown() {
			if (socket != null)
				socket.close();
			if (retransmitSocket != null)
				retransmitSocket.close();
		}
	}

	public void newBroadcastMessage(String address, String message) {

		String[] split = message.split(BroadcastMessage.MESSAGE_SEPARATOR);
		switch (split[0]) {
		case "HEARTBEAT":
			if (!address.equals(ownAddress)) {
				long timeElapsed = HeartbeatBroadcastMessage.decode(message);
				console.getGUI().getConnectionPanel().newAddress(address);
				updateDroneData(address, split[0], timeElapsed);
			}
			break;
		case "GPS":
			RobotLocation di = PositionBroadcastMessage
					.decode(address, message);
			if (di != null) {
				if (console.getGUI() instanceof DroneGUI) {
					((DroneGUI) console.getGUI()).getMapPanel().displayData(di);
				}
				console.getGUI().getConnectionPanel().newAddress(address);
				updateDroneData(address, split[0], di);
			}
			break;
		case "ENTITIES":
			if (!address.equals(ownAddress)) {
				ArrayList<Entity> entities = EntitiesBroadcastMessage.decode(
						address, message);
				if (console instanceof DroneControlConsole) {
					((DroneGUI) ((DroneControlConsole) console).getGUI())
							.getMapPanel().replaceEntities(entities);
				}
			}
			break;
		default:
			System.out.println("Uncategorized message > " + message
					+ " < from " + address);
		}

		if (retransmit)
			sender.retransmit(message);

	}

	private void updateDroneData(String address, String msgType, Object obj) {
		if (console instanceof DroneControlConsole) {
			try {
				DronesSet dronesSet = ((DroneControlConsole) console)
						.getDronesSet();
				DroneData drone;
				boolean exists = false;

				if (!dronesSet.existsDrone(address)) {
					drone = new DroneData();
					drone.setIpAddr(InetAddress.getByName(address));
					drone.setName("<no name>");
				} else {
					drone = dronesSet.getDrone(address);
					exists = true;
				}

				switch (msgType) {
				case "HEARTBEAT":
					drone.setTimeSinceLastHeartbeat((long) obj);
					break;
				case "GPS":
					drone.setRobotLocation((RobotLocation) obj);
					break;
				default:
					System.out
							.println("Uncategorized message type to update on drone data, from "
									+ address);
				}

				if (!exists) {
					dronesSet.addDrone(drone);
				}
			} catch (UnknownHostException e) {
				System.err
						.println("UnknownHostException on ConsoleBroadcastHandler....\n"
								+ e.getMessage());
			}

		}
	}

	class BroadcastReceiver extends Thread {

		private DatagramSocket socket;
		private boolean execute = true;

		public BroadcastReceiver() {
			try {
				System.out.println("RECEIVER "
						+ InetAddress.getByName("0.0.0.0") + ", port: " + PORT);
				socket = new DatagramSocket(PORT,
						InetAddress.getByName("0.0.0.0"));
				socket.setBroadcast(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (execute) {
					byte[] recvBuf = new byte[BUFFER_LENGTH];
					DatagramPacket packet = new DatagramPacket(recvBuf,
							recvBuf.length);
					socket.receive(packet);

					String message = new String(packet.getData()).trim();

					// if(!packet.getAddress().getHostAddress().equals(ownAddress))
					messageReceived(packet.getAddress().getHostAddress(),
							message);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		public void shutdown() {
			execute = false;
		}
	}
}
