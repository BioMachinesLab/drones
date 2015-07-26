package gui.panels;

import gui.DroneGUI;
import gui.utils.BroadcastLogger;
import gui.utils.DroneIP;
import gui.utils.DroneIP.DroneStatus;
import gui.utils.IncidentLogger;
import gui.utils.ObtainLogsThread;
import gui.utils.SortedListModel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogsPanel extends JPanel {
	private static final long serialVersionUID = -9161997728619121089L;

	private JCheckBox loggerCheckBox;
	private JTextArea logsTextArea;
	private JButton textAreaButton;
	private JButton getLogsButton;
	
	private IncidentLogger incidentLogger;
	private BroadcastLogger broadcastLogger;
	
	public LogsPanel(DroneGUI droneGui) {
		setBorder(BorderFactory.createTitledBorder("Logs"));
		setLayout(new BorderLayout());
		
		incidentLogger = new IncidentLogger();
		broadcastLogger = new BroadcastLogger();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if(incidentLogger != null)
						incidentLogger.close();
					
					if(broadcastLogger != null)
						broadcastLogger.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		JPanel broacastLoggerPanel = new JPanel(new GridLayout(1, 2));
		JLabel loggerLabel = new JLabel("Broadcast Logger");
		loggerCheckBox = new JCheckBox();
		loggerCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cBox = (JCheckBox) e.getSource();
				
				if(cBox.isSelected())
					broadcastLogger.open();
				else
					try {
						broadcastLogger.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
		});
		broacastLoggerPanel.add(loggerLabel);
		broacastLoggerPanel.add(loggerCheckBox);
		add(broacastLoggerPanel, BorderLayout.NORTH);
		
		getLogsButton = new JButton("Get Logs From Drones");
		getLogsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SortedListModel ipsList = droneGui.getConnectionPanel().getListModel();
				for (int i = 0; i < ipsList.getSize(); i++) {
					DroneIP droneIP = ipsList.get(i);
					if(!droneIP.getStatus().equals(DroneStatus.NOT_RUNNING)){
						ObtainLogsThread t = new ObtainLogsThread(droneIP.getIp());
						t.start();
					}
				}
			}
		});
		add(getLogsButton, BorderLayout.SOUTH);
		
		JPanel logsTextAreaPanel = new JPanel(new BorderLayout());
		logsTextArea = new JTextArea(7,2);
		logsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
		logsTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) || (e.getKeyCode() == KeyEvent.VK_ENTER) && (e.getModifiersEx() == 256)){
					incidentLogger.log(logsTextArea.getText());
					logsTextArea.setText("");
				}
			}
		});
		JScrollPane scroll = new JScrollPane(logsTextArea);
		logsTextAreaPanel.add(scroll);
		
		textAreaButton = new JButton("Save Log");
		textAreaButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				incidentLogger.log(logsTextArea.getText());
				logsTextArea.setText("");
			}
		});
		logsTextAreaPanel.add(textAreaButton, BorderLayout.SOUTH);
		
		add(logsTextAreaPanel);
	}
	
	public JCheckBox getLoggerCheckBox() {
		return loggerCheckBox;
	}
	
	public JTextArea getLogsTextArea() {
		return logsTextArea;
	}
	
	public IncidentLogger getIncidentLogger() {
		return incidentLogger;
	}
	
	public BroadcastLogger getBroadcastLogger() {
		return broadcastLogger;
	}
	
}
