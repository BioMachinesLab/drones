package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import commoninterface.dataobjects.GPSData;
import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;
import main.RobotControlConsole;

public class GPSTimeProviderClient extends Thread {

	private final static boolean DEBUG = false;
	private int port;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private InetAddress destHost;
	private String destHostName;
	private String myHostName;
	private RobotControlConsole console;

	private boolean ready = false;
	private boolean exit = false;

	private GPSData gpsData = null;

	public GPSTimeProviderClient(InetAddress destHost, int port) throws IOException {
		this.socket = null;
		this.in = null;
		this.out = null;
		this.destHostName = null;
		this.destHost = destHost;
		this.port = port;

		if (!checkIP(destHost)) {
			throw new UnknownHostException(destHost.getHostAddress() + " unreachable!");
		}
	}

	@Override
	public void run() {
		try {
			initializeCommunications();

			while (!exit) {
				receiveData();
			}
		} catch (IOException e) {
			System.out.printf("[%s] GPS Time server closed the connection with %s (%s:%d)\n", getClass().getName(),
					destHostName, destHost.getHostAddress(), port);
		} catch (ClassNotFoundException e) {
			System.err.printf("[%s] I didn't reveived a correct name from %s\n", getClass().getName(),
					socket.getInetAddress().getHostAddress());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			console.disconnect();
		}
	}

	private void receiveData() throws IOException {
		try {
			GPSMessage message = (GPSMessage) in.readObject();
			if (DEBUG)
				System.out.printf("[%s] Received %s\n", this.getClass().getName(), message.getClass().getSimpleName());

			gpsData = message.getGPSData();
		} catch (ClassNotFoundException e) {
			System.err.printf("[%s] Received class of unknown type from %s, so it was discarded....\n",
					getClass().getName(), destHostName);
		}
	}

	public synchronized void requestUpdate() {
		if (!ready)
			return;

		try {
			if (socket != null && !socket.isClosed()) {
				InformationRequest request = new InformationRequest(MessageType.GPS, myHostName);
				out.writeObject(request);
				out.flush();
				if (DEBUG)
					System.out.printf("[%s] Sent %s\n", getClass().getName(), request.getClass().getSimpleName());
			}
		} catch (IOException e) {
			System.err.printf("[%s] Unable to send data... is there an open connection?\n", getClass().getName());
			e.printStackTrace();
		}
	}

	/*
	 * Rise and kill connections
	 */
	private void initializeCommunications() throws IOException, ClassNotFoundException {
		myHostName = InetAddress.getLocalHost().getHostName();
		out.writeObject(myHostName);
		out.flush();

		destHostName = (String) in.readObject();
		System.out.printf("[%s] Connected to %s (%s:%d)\n", this.getClass().getName(), destHostName,
				destHost.getHostAddress(), port);

		ready = true;
	}

	public void connect() throws IOException {
		socket = new Socket(destHost, port);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	public synchronized void closeConnection() {
		if (socket != null && !socket.isClosed()) {
			System.out.printf("[%s] Closing connection with %s (%s:%d)\n", getClass().getName(), destHostName,
					destHost.getHostAddress(), port);
			try {
				exit = true;
				this.interrupt();
				socket.close();
				in.close();
				out.close();
			} catch (IOException e) {
				System.err.printf("[%s] Unable to close connection with %s (%s:%d)... is there an open connection?\n",
						this.getClass().getName(), destHostName, destHost.getHostAddress(), port);
			} finally {
				gpsData = null;
			}
		}
	}

	/*
	 * Getters, setters and status report
	 */
	public boolean connectionOK() {
		return socket != null && !socket.isClosed();
	}

	public String getDestHostName() {
		return destHostName;
	}

	public InetAddress getDestInetAddress() {
		return destHost;
	}

	private boolean checkIP(InetAddress destHost) throws IOException {
		// Check IP address with a 5 seconds timeout
		return InetAddress.getByName(destHost.getHostAddress()).isReachable(5 * 1000);
	}

	public GPSData getGPSData() {
		return gpsData;
	}
}
