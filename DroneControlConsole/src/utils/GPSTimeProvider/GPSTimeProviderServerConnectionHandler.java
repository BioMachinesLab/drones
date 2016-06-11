package utils.GPSTimeProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import commoninterface.network.messages.GPSMessage;
import commoninterface.network.messages.InformationRequest;
import commoninterface.network.messages.InformationRequest.MessageType;
import commoninterface.network.messages.Message;

public class GPSTimeProviderServerConnectionHandler extends Thread {
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String clientName = null;
	private String myHostname = null;
	private GPSTimeProviderServer server;

	public GPSTimeProviderServerConnectionHandler(Socket socket, GPSTimeProviderServer server) {
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			initConnection();

			while (true) {
				Message message = (Message) in.readObject();

				if (message instanceof InformationRequest) {
					processData((InformationRequest) message);
				}
			}
		} catch (IOException e) {
			server.setMessage(
					"Client " + socket.getInetAddress().getHostAddress() + " (" + clientName + ") disconnected");
		} catch (ClassNotFoundException e) {
			server.setErrorMessage("I didn't reveived correct data from " + socket.getInetAddress().getHostAddress()
					+ " -> " + e.getMessage());
		} finally {
			// Always shutdown the handler when something goes wrong
			closeConnection();
		}
	}

	public Socket getSocket() {
		return socket;
	}

	/*
	 * Manage connection
	 */
	protected void initConnection() throws IOException, ClassNotFoundException {
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());

		clientName = (String) in.readObject();
		myHostname = InetAddress.getLocalHost().getHostName();
		out.writeObject(myHostname);
		out.flush();

		server.setMessage("Client " + socket.getInetAddress().getHostAddress() + " (" + clientName + ") connected");
	}

	public synchronized void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				server.removeConnection(this);
				out.close();
				in.close();
			}
		} catch (IOException e) {
			server.setErrorMessage("Unable to close connection to " + clientName + ". Is there an open connection?");
		}
	}

	public synchronized void closeConnectionWhitoutRemove() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				out.close();
				in.close();
			}
		} catch (IOException e) {
			server.setErrorMessage("Unable to close connection to " + clientName + ". Is there an open connection?");
		}
	}

	private void processData(InformationRequest message) throws ClassNotFoundException {
		if (message.getMessageTypeQuery() == MessageType.GPS) {
			GPSMessage toSend = new GPSMessage(server.getGPSData(), myHostname);

			try {
				out.writeObject(toSend);
				out.flush();
			} catch (IOException e) {
				server.setErrorMessage("Unable to write object to socket ->" + e.getMessage());
			}
		} else {
			server.setErrorMessage("Error: " + message.getSenderIPAddr() + " (" + message.getSenderHostname()
					+ ") requested " + message.getMessageTypeQuery() + " information");
		}
	}
}
