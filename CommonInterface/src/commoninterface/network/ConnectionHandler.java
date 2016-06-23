package commoninterface.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import commoninterface.RobotCI;
import commoninterface.network.messages.Message;
import commoninterface.network.messages.SystemStatusMessage;

public class ConnectionHandler extends Thread {

	private final static boolean DEBUG = false;

	protected Socket socket;
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected RobotCI robot;
	protected String clientName = null;
	protected ConnectionListener connectionListener;

	public ConnectionHandler(Socket socket, RobotCI robot, ConnectionListener connectionListener) {
		this.socket = socket;
		this.robot = robot;
		this.connectionListener = connectionListener;
	}

	@Override
	public void run() {
		try {

			initConnection();

			while (true) {
				try {
					Message message = (Message) in.readObject();
					processMessage(message);

				} catch (ClassNotFoundException e) {
					System.out.printf("[%s] Received class of unknown type from %s, so it was discarded%n",
							getClass().getName(), clientName);
				} catch (ClassCastException e) {
					System.out.printf(
							"[%s] Received class of different type (other than Message) from %s, so it was discarded%n",
							getClass().getName(), clientName);
				}
			}

		} catch (IOException e) {
			System.out.printf("[%s] Client %s (%s) disconnected%n", getClass().getName(),
					socket.getInetAddress().getHostAddress(), clientName);
		} catch (ClassNotFoundException e) {
			System.out.printf("[%s] I didn't reveived a correct name from %s%n", getClass().getName(),
					socket.getInetAddress().getHostAddress());
			e.printStackTrace();
		} finally {
			// always shutdown the handler when something goes wrong
			shutdownHandler();
		}
	}

	protected void shutdownHandler() {
		closeConnection();
		if (connectionListener.getConnections().isEmpty()) {
			robot.reset();
		}
	}

	protected void processMessage(Message message) {
		if (DEBUG)
			System.out.printf("[%s] Information Request Message (%s)%n", getClass().getName(),
					message.getClass().getSimpleName());
		robot.processInformationRequest(message, this);
	}

	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		out.reset();

		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		clientName = (String) in.readObject();

		System.out.printf("[%s] Client %s (%s) connected%n", getClass().getName(),
				socket.getInetAddress().getHostAddress(), clientName);

		// controller.processInformationRequest(new
		// InformationRequest(MessageType.SYSTEM_STATUS), this);
		if (robot.getNetworkAddress() != null) {
			sendData(new SystemStatusMessage(robot.getInitMessages(), robot.getNetworkAddress()));
		} else {
			sendData(new SystemStatusMessage(robot.getInitMessages(), "<virtual host>"));
		}
	}

	public synchronized void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				connectionListener.removeConnection(this);
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out.printf("[%s] Unable to close connection to %s... is there an open connection?%n",
					getClass().getName(), clientName);
		}
	}

	public synchronized void closeConnectionWthoutDiscardConnListener() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				out.close();
				in.close();
			}
		} catch (IOException e) {
			System.out.printf("[%s] Unable to close connection to %s... is there an open connection?%n",
					getClass().getName(), clientName);
		}
	}

	public synchronized void sendData(Object data) {
		try {
			out.reset();// clear the cache so that we don't send old data
			out.writeObject(data);
			out.flush();
			if (DEBUG)
				System.out.printf("[%s] Sent Data (%s)%n", getClass().getName(), data.getClass().getSimpleName());
		} catch (IOException e) {
			System.out.printf("[%s] Unable to close connection to %s... is there an open connection?%n",
					getClass().getName(), clientName);
		}
	}

	public Socket getSocket() {
		return socket;
	}
}
