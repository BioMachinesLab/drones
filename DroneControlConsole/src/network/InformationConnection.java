package network;

import gui.GUI;
import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JOptionPane;

public class InformationConnection extends DroneConnection {
	
	private static int INFORMATION_PORT = 10101;
	
	public InformationConnection(GUI gui, InetAddress destHost) throws IOException {
		super(gui, destHost, INFORMATION_PORT);
	}
	
	@Override
	protected void shutdownConnection() {
		JOptionPane.showMessageDialog(gui.getFrame(), "Connection to drone was lost!");
		System.exit(0);
		//TODO fix this! when this happens, the old threads are still in the background sending stuff
		gui.connect();
	}
	
}
