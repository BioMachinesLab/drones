package gui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import commoninterface.network.messages.NeuralActivationsMessage;
import threads.UpdateThread;
import utils.CIGraph;

public class NeuralActivationsPanel extends UpdatePanel {
	private static final long serialVersionUID = -1007986325782353224L;
	private HashMap<String, ArrayList<Double>> inputsMap;
	private HashMap<String, ArrayList<Double>> outputsMap;
	
	private CIGraph inputsGraph;;
	private CIGraph outputsGraph;
	
	private JPanel inputsCheckBoxesPanel = new JPanel();
	private JPanel outputsCheckBoxesPanel = new JPanel();
	
	private long sleepTime = 10000;

	private UpdateThread thread;
	
	private boolean notifyThread;
	private boolean createCheckBoxes;
	
	private ArrayList<String> selectedInputsTitles;
	private ArrayList<String> selectedOutputsTitles;
	
	public NeuralActivationsPanel() {
		setLayout(new BorderLayout());
		
		inputsMap = new HashMap<String, ArrayList<Double>>();
		outputsMap = new HashMap<String, ArrayList<Double>>();
		selectedInputsTitles = new ArrayList<String>();
		selectedOutputsTitles = new ArrayList<String>();
		notifyThread = true;
		createCheckBoxes = true;
		
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
		inputPanel.add(inputsCheckBoxesPanel, BorderLayout.NORTH);
		inputsGraph.setxLabel("Steps");
		inputsGraph.setyLabel("Activations");
		
		inputPanel.add(inputsGraph);
		panel.add(inputPanel);
		
		outputPanel.setBorder(BorderFactory.createTitledBorder("Outputs Graph"));
		outputPanel.add(outputsCheckBoxesPanel, BorderLayout.NORTH);
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
					notifyThread = true;
					wakeUpThread();
					button.setText("Stop");
				}else{
					notifyThread = false;
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
				inputsMap.clear();
				outputsMap.clear();
			}
		});
		p2.add(clearButton);
		
		JPanel p3 = new JPanel(new FlowLayout());
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FileWriter writer = new FileWriter(new File("neural_activations.txt"));
					
					int recordSteps = -1;
					writer.write("#");
					for (String sensorName : inputsMap.keySet()) {
						writer.write(sensorName + "\t");
						
						if(recordSteps < 0)
							recordSteps = inputsMap.get(sensorName).size();
					}
					
					for (String actuatorName : outputsMap.keySet()) 
						writer.write(actuatorName + "\t");
					
					writer.write("\n");
					
					for (int i = 0; i < recordSteps; i++) {
						writer.write((i+1) + "\t");
						
						for (String key : inputsMap.keySet()) 
							writer.write(inputsMap.get(key).get(i) + "\t");
						
						for (String key : outputsMap.keySet()) 
							writer.write(outputsMap.get(key).get(i) + "\t");
						
						writer.write("\n");
					}
					
					writer.write("\n");
					
					writer.close();
					
					JOptionPane.showMessageDialog(null, "Neural Network Activations Exported");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
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
			@Override
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
		
		if(createCheckBoxes){
			createCheckBoxes(inputTitles, inputsCheckBoxesPanel, selectedInputsTitles);
		}
		
		for (Double[] inputValues : message.getInputsValues()) {
			for (int i = 0; i < inputValues.length; i++) 
				addActivationValues(inputsMap, inputTitles.get(i), inputValues[i]);
		}
		
		ArrayList<String> outputTitles = message.getOutputsTitles();
		
		if(createCheckBoxes){
			createCheckBoxes(outputTitles, outputsCheckBoxesPanel, selectedOutputsTitles);
			validate();
			createCheckBoxes = false;
		}
		
		for (Double[] outputValues: message.getOutputsValues()){
			for (int i = 0; i < outputValues.length; i++) 
				addActivationValues(outputsMap, outputTitles.get(i), outputValues[i]);
		}
		
		inputsGraph.clear();
		outputsGraph.clear();
		
		drawActivations(inputsMap, inputsGraph, selectedInputsTitles);
		
		for (String title : message.getInputsTitles())
			inputsGraph.addLegend(title);
		
		drawActivations(outputsMap, outputsGraph, selectedOutputsTitles);
		
		for (String title : message.getOutputsTitles())
			outputsGraph.addLegend(title);
		
		if(notifyThread)
			notifyAll();
	}

	private synchronized void drawActivations(HashMap<String, ArrayList<Double>> map, CIGraph graph, ArrayList<String> selectedTitles) {
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
			
			if(selectedTitles.isEmpty()){
				graph.addDataList(activations);
			}else if(selectedTitles.contains(key)){
				graph.addDataList(activations);
			}
			
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
	
	private void createCheckBoxes(ArrayList<String> titles, JPanel checkBoxesPanel, ArrayList<String> selectedTitles) {
		if(titles.size() > 0){
			int rows = (int) Math.round(titles.size() / 4.0 + 0.25);
			int cols = (titles.size() < 4) ? titles.size() : 4;
			
			checkBoxesPanel.setLayout(new GridLayout(rows, cols));
			
			for (String title : titles) {
				JLabel label = new JLabel(title);
				JCheckBox checkBox = new JCheckBox();
				
				checkBox.addItemListener(new CheckBoxListener(checkBox, title, selectedTitles));
				
				checkBoxesPanel.add(label);
				checkBoxesPanel.add(checkBox);
			}
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

	class CheckBoxListener implements ItemListener {
		private JCheckBox checkBox;
		private String title;
		private ArrayList<String> selectedTitles;

		public CheckBoxListener(JCheckBox checkBox, String title, ArrayList<String> selectedTitles) {
			this.checkBox = checkBox;
			this.title = title;
			this.selectedTitles = selectedTitles;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(checkBox.isSelected())
				selectedTitles.add(title);
			else
				selectedTitles.remove(title);
		}
		
	}
	
}
