package gui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import network.messages.NeuralActivationsMessage;
import threads.UpdateThread;
import utils.CIGraph;

public class NeuralActivationsPanel extends UpdatePanel {

	private CIGraph inputsGraph;;
	private CIGraph outputsGraph;
	
	private long sleepTime = 10000;

	private UpdateThread thread;
	
	public NeuralActivationsPanel() {
		setLayout(new BorderLayout());
		add(createGraphsPanel(), BorderLayout.CENTER);
		add(createBottomPanel(), BorderLayout.SOUTH);
	}
	
	private JPanel createGraphsPanel(){
		JPanel panel = new JPanel(new GridLayout(1,1));
		
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputsGraph = new CIGraph();
		
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputsGraph = new CIGraph();
		
		inputPanel.setBorder(BorderFactory.createTitledBorder("Inputs Graph"));
		inputsGraph.setxLabel("Steps");
		inputsGraph.setyLabel("Activations");
		
		inputPanel.add(inputsGraph);
		panel.add(inputPanel);
		
		outputPanel.setBorder(BorderFactory.createTitledBorder("Outputs Graph"));
		outputsGraph.setxLabel("Steps");
		outputsGraph.setyLabel("Activations");
		
		outputPanel.add(outputsGraph);
		panel.add(outputPanel);
		
		return panel;
	}
	
	private JPanel createBottomPanel(){
		JPanel bottomPanel = new JPanel(new GridLayout(1,3));
		
		JPanel p = new JPanel(new FlowLayout());
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		p.add(clearButton);
		
		JPanel p2 = new JPanel(new FlowLayout());
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		p2.add(exportButton);
		
		bottomPanel.add(p);
		bottomPanel.add(p2);
		bottomPanel.add(createRefreshRatePanel());
		
		return bottomPanel;
	}
	
	private JPanel createRefreshRatePanel() {
		JPanel refreshPanel = new JPanel(new BorderLayout());
		refreshPanel.add(new JLabel("Refresh Rate"), BorderLayout.WEST);
		
		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(new String[] {
				"10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		refreshPanel.add(comboBoxUpdateRate, BorderLayout.EAST);
		
		comboBoxUpdateRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (comboBoxUpdateRate.getSelectedIndex()) {
				case 0:
					sleepTime = 100;
					break;
				case 1:
					sleepTime = 200;
					break;
				case 2:
					sleepTime = 1000;
					break;
				case 3:
					sleepTime = 10000;
					break;
				default:
					sleepTime = 1000;
					break;
				}
				wakeUpThread();
			}
		});
		
		JPanel bottom = new JPanel(new GridLayout(1,1));
		bottom.add(refreshPanel);
		
		return bottom;
	}
	
	public synchronized void displayData(NeuralActivationsMessage message) {
		//?? =  message.getReadings();
		
		//Receber a mensagem e trata-la
		
		notifyAll();
	}
	
	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	private synchronized void wakeUpThread() {
		notifyAll();
		thread.interrupt();
	}
	
	@Override
	public void threadWait() {
		try {
			synchronized(this){
				wait();
			}
		}catch(Exception e) {}
	}

	@Override
	public long getSleepTime() {
		return sleepTime;
	}

}
