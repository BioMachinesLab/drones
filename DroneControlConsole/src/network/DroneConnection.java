package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import commoninterface.network.messages.Message;
import main.RobotControlConsole;

public abstract class DroneConnection extends Thread {

	private final static boolean DEBUG = false;
	protected int port;
	protected Socket socket;
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected InetAddress destHost;
	protected String destHostName;
	protected RobotControlConsole console;

	protected boolean ready = false;

	public DroneConnection(RobotControlConsole console, InetAddress destHost, int port) throws IOException {
		this.socket = null;
		this.in = null;
		this.out = null;
		this.destHostName = null;
		this.destHost = destHost;
		this.port = port;
		this.console = console;

		if (!checkIP(destHost)) {
			throw new UnknownHostException(destHost.getHostAddress() + " unreachable!");
		}

		connect();
	}

	private void connect() throws IOException {
		socket = new Socket(destHost, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	public synchronized void sendData(Object data) {

		if (!ready)
			return;

		try {
			if (socket != null && !socket.isClosed()) {
				out.writeObject(data);
				out.flush();
				if (DEBUG)
					System.out.printf("[%s] [SEND] Sent %s\n", this.getClass().getName(),
							data.getClass().getSimpleName());
			}
		} catch (IOException e) {
			System.err.printf("[%s] Unable to send data... is there an open connection?\n", this.getClass().getName());
			e.printStackTrace();
		}
	}

	private boolean checkIP(InetAddress destHost) throws IOException {
		return InetAddress.getByName(destHost.getHostAddress()).isReachable(5 * 1000);// 5
																						// sec
																						// timeout
	}

	@Override
	public void run() {
		try {

			initialization();

			while (true) {
				update();
			}

		} catch (IOException e) {
			System.out.printf("[%s] Drone Controller closed the connection with %s (%s:%d)\n", this.getClass().getName(), destHostName,
					destHost.getHostAddress(), port);
		} catch (ClassNotFoundException e) {
			System.err.printf("[%s] I didn't reveived a correct name from %s\n", this.getClass().getName(),
					socket.getInetAddress().getHostAddress());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			console.disconnect();
		}
	}

	protected void initialization() throws IOException, ClassNotFoundException {
		out.writeObject(InetAddress.getLocalHost().getHostName());
		out.flush();

		destHostName = (String) in.readObject();
		System.out.printf("[%s] Connected to %s (%s:%d)\n", this.getClass().getName(), destHostName,
				destHost.getHostAddress(), port);

		ready = true;
	}

	protected void update() throws IOException {
		try {
			Message message = (Message) in.readObject();
			if (DEBUG)
				System.out.printf("[%s] [RECEIVED] Received %s\n", this.getClass().getName(),
						message.getClass().getSimpleName());
			console.processMessage(message);
		} catch (ClassNotFoundException e) {
			System.err.printf("[%s] Received class of unknown type from %s, so it was discarded....\n",
					this.getClass().getName(), destHostName);
		}
	}

	public String getDestHostName() {
		return destHostName;
	}

	public InetAddress getDestInetAddress() {
		return destHost;
	}

	public synchronized void closeConnection() {
		if (socket != null && !socket.isClosed()) {
			System.out.printf("[%s] Closing Connection with %s (%s:%d)\n", this.getClass().getName(), destHostName,
					destHost.getHostAddress(), port);
			try {
				this.interrupt();
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				System.err.printf("[%s] Unable to close connection with %s (%s:%d)... is there an open connection?\n",
						this.getClass().getName(), destHostName, destHost.getHostAddress(), port);
			}
		}
	}

	public boolean connectionOK() {
		return socket != null && !socket.isClosed();
	}
}