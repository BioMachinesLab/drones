package gui.panels;

import gui.IPRequestToUserModal;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import main.DroneControlConsole;
import main.RobotControlConsole;
import threads.UpdateThread;

public class ConnectionPanel extends UpdatePanel {
	private static final long serialVersionUID = -4874186493593218098L;

	private static final long SLEEP_TIME = 10 * 1000;
	private static final long TIME_THRESHOLD = 10 * 1000;

	private JList<String> list;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private HashMap<String, Long> lastUpdate = new HashMap<String, Long>();
	private JLabel currentConnection;
	private RobotControlConsole console;

	public ConnectionPanel(RobotControlConsole console) {

		this.console = console;

		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder("Connection"));

		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(120, 80));
		add(listScroller);

		JButton connect = new JButton("Connect");

		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();

				if (index != -1) {
					connect(listModel.get(index));
				}
			}
		});

		JButton disconnect = new JButton("Disconnect");

		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});

		JButton connectTo = new JButton("Connect To");

		connectTo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				connectTo();
			}
		});

		currentConnection = new JLabel("");
		currentConnection.setHorizontalAlignment(JLabel.CENTER);

		JPanel buttonsPanel = new JPanel(new GridLayout(4, 1));
		buttonsPanel.add(connect);
		buttonsPanel.add(disconnect);
		buttonsPanel.add(connectTo);
		buttonsPanel.add(currentConnection);

		add(buttonsPanel);
	}

	private void connect(String address) {
		console.connect(address);
	}

	private void disconnect() {
		console.disconnect();
	}

	public void connectionOK(String address) {
		currentConnection.setText(address);
	}

	public void disconnected() {
		currentConnection.setText("");
	}

	public void connectTo() {
		new IPRequestToUserModal(console);
	}

	public synchronized void removeAddress(String address) {

		for (int i = 0; i < listModel.getSize(); i++) {
			if (address.equals(listModel.getElementAt(i))) {
				listModel.remove(i);
				lastUpdate.remove(address);

				if (console instanceof DroneControlConsole) {
					((DroneControlConsole) console).getDronesSet().removeDrone(
							address);
				}
			}
		}
	}

	public synchronized void newAddress(String address) {
		if (!listModel.contains(address)) {
			listModel.addElement(address);
		}
		lastUpdate.put(address, System.currentTimeMillis());
	}

	public String[] getCurrentAddresses() {
		String[] ips = new String[listModel.getSize()];
		int i = 0;
		for (Object o : listModel.toArray()) {
			ips[i++] = (String) o;
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

}