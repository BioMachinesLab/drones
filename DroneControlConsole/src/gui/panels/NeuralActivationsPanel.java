package gui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

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

	private HashMap<String, ArrayList<Double>> inputsMap;
	private HashMap<String, ArrayList<Double>> outputsMap;
	
	private CIGraph inputsGraph;;
	private CIGraph outputsGraph;
	
	private long sleepTime = 10000;

	private UpdateThread thread;
	
	public NeuralActivationsPanel() {
		setLayout(new BorderLayout());
		
		inputsMap = new HashMap<String, ArrayList<Double>>();
		outputsMap = new HashMap<String, ArrayList<Double>>();
		
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
		JPanel bottomPanel = new JPanel(new GridLayout(1,4));
		
		JPanel p = new JPanel(new FlowLayout());
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton button = (JButton) e.getSource();
				
				if(button.getText().equals("Start")){
					wakeUpThread();
					button.setText("Stop");
				}else{
					threadWait();
					button.setText("Start");
				}
			}
		});
		p.add(startButton);
		
		JPanel p2 = new JPanel(new FlowLayout());
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inputsGraph.clear();
				outputsGraph.clear();
			}
		});
		p2.add(clearButton);
		
		JPanel p3 = new JPanel(new FlowLayout());
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		p3.add(exportButton);
		
		bottomPanel.add(p);
		bottomPanel.add(p2);
		bottomPanel.add(p3);
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
		ArrayList<String> inputTitles = message.getInputsTitles();
		
		for (Double[] inputValues : message.getInputsValues()) {
			for (int i = 0; i < inputValues.length; i++) 
				addActivationValues(inputsMap, inputTitles.get(i), inputValues[i]);
		}
		
		ArrayList<String> outputTitles = message.getOutputsTitles();
		
		for (Double[] outputValues: message.getOutputsValues()){
			for (int i = 0; i < outputValues.length; i++) 
				addActivationValues(outputsMap, outputTitles.get(i), outputValues[i]);
		}
		
		inputsGraph.clear();
		outputsGraph.clear();
		
		drawActivations(inputsMap, inputsGraph);
		
		for (String title : message.getInputsTitles())
			inputsGraph.addLegend(title);
		
		drawActivations(outputsMap, outputsGraph);
		
		for (String title : message.getOutputsTitles())
			outputsGraph.addLegend(title);
		
		notifyAll();
	}

	private synchronized void drawActivations(HashMap<String, ArrayList<Double>> map, CIGraph graph) {
		boolean changeShowLast = true;
		
		for (String key : map.keySet()) {
			ArrayList<Double> aux = map.get(key);
			Double[] activations = new Double[aux.size()];
			
			for (int i = 0; i < aux.size(); i++)
				activations[i] = aux.get(i);			
			
			if(changeShowLast){
				graph.setShowLast(activations.length);
				changeShowLast = false;
			}
			
			graph.addDataList(activations);
		}
	}

	private void addActivationValues(HashMap<String, ArrayList<Double>> activations, String title, double value) {
		if(activations.containsKey(title)){
			ArrayList<Double> values = activations.get(title);
			values.add(value);
			activations.put(title, values);
		}else{
			ArrayList<Double> values = new ArrayList<Double>();
			values.add(value);
			activations.put(title, values);
		}
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
