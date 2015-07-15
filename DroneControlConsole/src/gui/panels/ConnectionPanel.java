package gui.panels;

import gui.IPRequestToUserModal;
import gui.utils.DroneIP;
import gui.utils.DroneIP.DroneStatus;
import gui.utils.SortedListModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import main.DroneControlConsole;
import main.RobotControlConsole;
import threads.UpdateThread;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConnectionPanel extends UpdatePanel {
	private static final long serialVersionUID = -4874186493593218098L;
	private static final String START_COMMAND = "killall screen run.sh; cd RaspberryController; screen -d -m -S controller ./run.sh;";
	private static final String STOP_COMMAND = "screen -S controller -p 0 -X stuff \"q$(printf \\\\r)\"";
	private static final String DRONE_HOME_FOLDER = "/home/pi/";
	
	private static final long SLEEP_TIME = 10 * 1000;
	private static final long TIME_THRESHOLD = 10 * 1000;
	private static final String CONNECTIONS_STRING = "N Connections: ";

	private JList<DroneIP> list;
	private SortedListModel listModel = new SortedListModel();
	private HashMap<String, Long> lastUpdate = new HashMap<String, Long>();
	private JLabel currentConnection;
	private RobotControlConsole console;
	
	private JButton disconnect;
	private JLabel connectionCountLabel;
	private int numberOfConnections = 0;
	
	private boolean droneConnected = false;
	private JSch jsch = new JSch();

	public ConnectionPanel(RobotControlConsole console) {

		this.console = console;

		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder("Connection"));
		
		list = new JList<DroneIP>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if(droneConnected){
						disconnect.doClick();
					
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) { }
					}
					
					int index = list.getSelectedIndex();

					if (index != -1) {
						connect(listModel.get(index).getIp());
					}
				}
			}
		});
		
		list.setCellRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
				
				if(isSelected)
					c.setBackground(Color.LIGHT_GRAY);
				
				if(value instanceof DroneIP){
					DroneIP droneIP = (DroneIP)value;
					if(droneIP.getStatus() == DroneStatus.RUNNING)
						setForeground(Color.GREEN.darker());
					else if(droneIP.getStatus() == DroneStatus.DETECTED)
						setForeground(Color.ORANGE.darker());
					else
						setForeground(Color.RED.darker());
				}
				
				return c;
			}
		});
		
		JScrollPane listScroller = new JScrollPane(list);
		add(listScroller);

		try {
			importIpsFromFile();
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		PingThread pingThread = new PingThread();
		pingThread.start();
		
		JButton connect = new JButton("Connect");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(droneConnected){
					disconnect.doClick();
				
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) { }
				}
				
				int index = list.getSelectedIndex();

				if (index != -1) {
					connect(listModel.get(index).getIp());
				}
			}
		});

		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});

//		JButton connectTo = new JButton("Connect To");
//		connectTo.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				connectTo();
//			}
//		});

		JButton startController = new JButton("Start Controller");
		startController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();

				if (index != -1) {
					try {
						executeSSHCommand(listModel.get(index).getIp(), START_COMMAND);
					} catch (JSchException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		JButton stopController = new JButton("Stop Controller");
		stopController.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();

				if (index != -1) {
					try {
						executeSSHCommand(listModel.get(index).getIp(), STOP_COMMAND);
					} catch (JSchException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		JButton uploadCodeButton = new JButton("Upload Code");
		uploadCodeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();

				if (index != -1) {
					DroneIP droneIP = listModel.get(index);
					
					switch (droneIP.getStatus()) {
					case RUNNING:
						JOptionPane.showMessageDialog(null, "Disconnect from drone " + droneIP.getIp() + " before upload new code");
						break;
					case DETECTED:
						uploadCode(droneIP.getIp(), true);
						break;
					case NOT_RUNNING:
						JOptionPane.showMessageDialog(null, "Couldn't reach drone " + droneIP.getIp());
						break;
					default:
						System.err.println("Unknown status: " + droneIP.getStatus());
						break;
					}
					
				}
				
			}
		});
		
		currentConnection = new JLabel("");
		currentConnection.setHorizontalAlignment(JLabel.CENTER);

		connectionCountLabel = new JLabel(CONNECTIONS_STRING + numberOfConnections);
		connectionCountLabel.setHorizontalAlignment(0);
		
		JPanel buttonsPanel = new JPanel(new GridLayout(7, 1));
		buttonsPanel.add(connect);
		buttonsPanel.add(disconnect);
		buttonsPanel.add(startController);
		buttonsPanel.add(stopController);
		buttonsPanel.add(uploadCodeButton);
//		buttonsPanel.add(connectTo);
		buttonsPanel.add(connectionCountLabel);
		buttonsPanel.add(currentConnection);

		add(buttonsPanel);
	}
	
	private void importIpsFromFile() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("drones_ips.txt"));
		
		while (scanner.hasNext()) {
			String ip = scanner.nextLine();
			if (!ip.contains("#")) {
				listModel.addElement(new DroneIP(ip));
			}
		}
		
		scanner.close();
	}
	
	private void executeSSHCommand(String hostIP, String command) throws JSchException {
		Session session = jsch.getSession("pi", hostIP, 22);
		session.setPassword("raspberry");
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		
		ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
		channelExec.setCommand(command);
		channelExec.connect();
		
		int exitStatus = channelExec.getExitStatus();
		channelExec.disconnect();
		session.disconnect();

		if (exitStatus > 0) {
		    System.out.println("Done, but with error!");
		}
		
		session.disconnect();
	}

	private void uploadCode(String ip, boolean showOutput) {
		File f = new File("../RaspberryController/utils");
		
		String[] split = ip.split("\\.");
		
		String[] cmd =  {
		        "/bin/sh",
		        "-c",
		        "cd " + f.getAbsolutePath() + " && ./upload.sh " + split[split.length-1]};
		
		try {
			ProcessBuilder pb = new ProcessBuilder(cmd);
			Process p = pb.start();
			p.waitFor();
			
			if(showOutput)
				showConsoleCommandOutput(p);
		} catch (IOException | InterruptedException e) { }
		
	}
	
	private void showConsoleCommandOutput(Process p) throws IOException{
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		String standard = "";
		String errors = "";
		
		// read the output from the command
		String s = null;
		while ((s = stdInput.readLine()) != null)
			standard+=s + "<p>";
		
		// read any errors from the attempted command
		while ((s = stdError.readLine()) != null)
			errors+=s + "<p>";
		
		
		String result = "<html>";
		
		if(!standard.isEmpty()){
			result += "<b>Output:</b><p>" + standard;
			result += "<p>";
		}
		
		if(!errors.isEmpty())
			result += "<b>Errors:</b><p>" + errors;
		
		result += "</html>";
		
//		JOptionPane.showMessageDialog(null, result);
		JOptionPane.showMessageDialog(null, result, "Upload Console Output", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void connect(String address) {
		console.connect(address);
	}

	private void disconnect() {
		console.disconnect();
	}
	
	public void connectionOK(String address) {
		currentConnection.setText(address);
		
		if(console instanceof DroneControlConsole){
			((DroneControlConsole) console).getDronesSet().setConnectedTo(address);
		}
	}

	public void disconnected() {
		currentConnection.setText("");
		
		if(console instanceof DroneControlConsole){
			((DroneControlConsole) console).getDronesSet().setConnectedTo("");
		}
	}

	public void connectTo() {
		new IPRequestToUserModal(console);
	}

	public synchronized void removeAddress(String address) {

		for (int i = 0; i < listModel.getSize(); i++) {
			DroneIP droneIP = listModel.getElementAt(i);
			if (address.equals(droneIP.getIp())) {
//				listModel.remove(i);
				lastUpdate.remove(address);
				droneIP.setStatus(DroneStatus.NOT_RUNNING);
				numberOfConnections--;
				list.repaint();
				
				connectionCountLabel.setText(CONNECTIONS_STRING + numberOfConnections);
				
				if (console instanceof DroneControlConsole) {
					((DroneControlConsole) console).getDronesSet().removeDrone(
							address);
				}
			}
		}
	}

	public synchronized void newAddress(String address) {
//		if (!listModel.contains(address)) {
//			listModel.addElement(address);
//			numberOfConnections++;
//		}
		
		DroneIP droneIP = listModel.getDroneWithIP(address);
		if(droneIP != null && droneIP.getStatus() != DroneStatus.RUNNING){
			droneIP.setStatus(DroneStatus.RUNNING);
			numberOfConnections++;
			list.repaint();
		} else if(droneIP == null) {
			listModel.addElement(new DroneIP(address));
			droneIP = listModel.getDroneWithIP(address);
			droneIP.setStatus(DroneStatus.RUNNING);
			numberOfConnections++;
			list.repaint();
		}
		
		lastUpdate.put(address, System.currentTimeMillis());
		connectionCountLabel.setText(CONNECTIONS_STRING + numberOfConnections);
	}

	public String[] getCurrentAddresses() {
		String[] ips = new String[listModel.getSize()];
		int i = 0;
		for (Object o : listModel.toArray()) {
			DroneIP ip = (DroneIP) o;
			if(ip.getStatus() == DroneStatus.RUNNING)
				ips[i++] = ip.getIp();
		}
		return ips;
	}

	public synchronized void cleanupAddresses() {

		Set<String> keys = lastUpdate.keySet();

		LinkedList<String> toRemove = new LinkedList<String>();

		for (String s : keys) {
			long updateTime = lastUpdate.get(s);
			if (System.currentTimeMillis() - updateTime > TIME_THRESHOLD) {
				toRemove.add(s);
			}
		}

		for (String s : toRemove) {
			removeAddress(s);
			// remove droneData from droneSet
		}
	}

	private void executeShellCommand(String[] pCommand, boolean showOutput){
		try {
			Runtime run = Runtime.getRuntime();
			Process pr = run.exec(pCommand);
			pr.waitFor();
			
			if(showOutput){
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

				// read the output from the command
				System.out.println("Here is the standard output of the command:");
				String s = null;
				while ((s = stdInput.readLine()) != null)
					System.out.println(s);

				System.out.println("\n");
				// read any errors from the attempted command
				System.out.println("Here is the standard error of the command (if any):");
				while ((s = stdError.readLine()) != null)
					System.out.println(s);
			}
			
		} catch (IOException | InterruptedException e) {
			System.out.println("Something went wrong when executing command in the commnad line");
			e.printStackTrace();
		}
	}
	
	public void setDroneConnected(boolean droneConnected) {
		this.droneConnected = droneConnected;
	}
	
	@Override
	public void registerThread(UpdateThread t) {
	}

	@Override
	public void threadWait() {
	}

	@Override
	public long getSleepTime() {
		return SLEEP_TIME;
	}
	
	public SortedListModel getListModel() {
		return listModel;
	}
	
	class PingThread extends Thread {
		
		private long timeBetweenPings = 1000*10; //10 sec
		private int pingTimeout = 2500;
		
		@Override
		public void run() {
			while(true) {
				for (int i = 0; i < listModel.getSize(); i++) {
					DroneIP droneIP = listModel.get(i);
					if(droneIP.getStatus() != DroneStatus.RUNNING){
						try {
							if(InetAddress.getByName(droneIP.getIp()).isReachable(pingTimeout)){
								droneIP.setStatus(DroneStatus.DETECTED);
								list.repaint();
							}else
								droneIP.setStatus(DroneStatus.NOT_RUNNING);
						} catch (IOException e) {
//							System.err.println(droneIP.getIp() + ": " + e.getMessage());
							droneIP.setStatus(DroneStatus.NOT_RUNNING);
						}
					}
				}
				
				try {
					Thread.sleep(timeBetweenPings);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
