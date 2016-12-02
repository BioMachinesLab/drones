package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import commoninterface.neuralnetwork.CICTRNN;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.neuralnetwork.inputs.CINNInput;
import commoninterface.neuralnetwork.outputs.CINNOutput;
import controllers.DroneNeuralNetworkController;
import evolutionaryrobotics.JBotEvolver;
import gui.util.GraphPlotter;
import simulation.Simulator;

public class CIGraphPlotter extends GraphPlotter {
	private static final long serialVersionUID = -726522646568520422L;
	protected CINeuralNetwork ciNN;
	protected Vector<CINNInput> ciInputs;
	protected Vector<CINNOutput> ciOutputs;
	protected boolean saveToFile=false;

	public CIGraphPlotter(JBotEvolver jBotEvolver, Simulator simulator) {
		super();
		try {
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			this.simulator = simulator;
			this.jBotEvolver = jBotEvolver;
			robots = simulator.getRobots();

			ciNN = ((DroneNeuralNetworkController) robots.get(0).getController()).getNeuralNetwork();
			ciInputs = ciNN.getInputs();
			ciOutputs = ciNN.getOutputs();

			mainPanel.add(initRobotPanel());
			mainPanel.add(initInputsPanel());

			JPanel hiddenPanel = initHiddenPanel();
			if (hiddenPanel != null)
				mainPanel.add(hiddenPanel);

			mainPanel.add(initOutputsPanel());

			JPanel buttonsPanel = new JPanel(new GridLayout(1, 4));

			JButton checkAllButton = new JButton("Check all");
			buttonsPanel.add(checkAllButton);

			JButton uncheckAllButton = new JButton("Uncheck all");
			buttonsPanel.add(uncheckAllButton);

			JButton saveToFileButton = new JButton("Save to file");
			buttonsPanel.add(saveToFileButton);

			JButton plotButton = new JButton("Plot!");
			buttonsPanel.add(plotButton);

			checkAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					changeAllCheckboxes(true);
				}
			});

			uncheckAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					changeAllCheckboxes(false);
				}
			});

			plotButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					plotGraph();
				}
			});

			saveToFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveToFile = true;
					plotGraph();
					saveToFile = false;
				}
			});

			mainPanel.add(buttonsPanel);

			mainPanel.add(new JScrollPane(console));

			add(mainPanel);

			pack();
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void plotGraph() {

		valuesList = new ArrayList<double[][]>();
		titlesList = new ArrayList<String>();

		simulator = jBotEvolver.createSimulator();
		simulator.addCallback(jBotEvolver.getEvaluationFunction()[0]);
		jBotEvolver.setupBestIndividual(simulator);

		simulator.addCallback(this);

		try {
			// Instantiate the needed arrays
			for (int i = 0; i < robotCheckboxes.size(); i++) {
				if (robotCheckboxes.get(i).isSelected()) {

					for (int j = 0; j < inputCheckboxes.size(); j++) {
						if (inputCheckboxes.get(j).isSelected()) {

							String[] chosenInputs = inputTextFields.get(j).getText().trim().split(",");
							int[] chosenInts = new int[chosenInputs.length];

							for (int z = 0; z < chosenInputs.length; z++)
								chosenInts[z] = Integer.parseInt(chosenInputs[z]);
							Arrays.sort(chosenInts);

							for (int z = 0; z < ciInputs.get(j).getNumberOfInputValues(); z++) {
								if (Arrays.binarySearch(chosenInts, z) >= 0) {
									titlesList.add(ciInputs.get(j).getLabel() + " " + z);
									valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
								}
							}
						}
					}
					if (!hiddenCheckboxes.isEmpty() && hiddenCheckboxes.get(0).isSelected()) {

						String[] chosenHidden = hiddenTextFields.get(0).getText().trim().split(",");
						int[] chosenInts = new int[chosenHidden.length];

						for (int z = 0; z < chosenHidden.length; z++)
							chosenInts[z] = Integer.parseInt(chosenHidden[z]);
						Arrays.sort(chosenInts);

						for (int z = 0; z < hidden.length; z++) {
							if (Arrays.binarySearch(chosenInts, z) >= 0) {
								titlesList.add("Hidden State " + z);
								valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
							}
						}
					}
					for (int j = 0; j < outputCheckboxes.size(); j++) {
						if (outputCheckboxes.get(j).isSelected()) {

							String[] chosenOutputs = outputTextFields.get(j).getText().trim().split(",");
							int[] chosenInts = new int[chosenOutputs.length];

							for (int z = 0; z < chosenOutputs.length; z++)
								chosenInts[z] = Integer.parseInt(chosenOutputs[z]);
							Arrays.sort(chosenInts);

							for (int z = 0; z < ciOutputs.get(j).getNumberOfOutputValues(); z++) {
								if (Arrays.binarySearch(chosenInts, z) >= 0) {
									titlesList.add(ciOutputs.get(j).getLabel() + " " + z);
									valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
								}
							}
						}
					}
				}
			}

			Thread worker = new Thread(new GraphSimulationRunner(simulator, saveToFile));
			worker.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected JPanel initOutputsPanel() {
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new GridLayout(ciOutputs.size(), 2));
		outputPanel.setBorder(BorderFactory.createTitledBorder("Outputs"));

		for (int i = 0; i < ciOutputs.size(); i++) {
			CINNOutput output = ciOutputs.get(i);
			String name = output.getLabel() + " (" + output.getNumberOfOutputValues() + ")";

			JCheckBox checkbox = new JCheckBox(name);
			checkbox.setSelected(true);

			String text = "";
			for (int j = 0; j < ciOutputs.get(i).getNumberOfOutputValues(); j++)
				text += j == ciOutputs.get(i).getNumberOfOutputValues() - 1 ? j : j + ",";

			outputCheckboxes.add(checkbox);
			outputTextFields.add(new JTextField(text));

			outputPanel.add(checkbox);
			outputPanel.add(outputTextFields.peekLast());
		}
		return outputPanel;
	}

	@Override
	protected JPanel initInputsPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(ciInputs.size(), 2));
		inputPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));

		for (int i = 0; i < ciInputs.size(); i++) {
			CINNInput input = ciInputs.get(i);
			String name = input.getLabel() + " (" + input.getNumberOfInputValues() + ")";

			JCheckBox checkbox = new JCheckBox(name);
			checkbox.setSelected(true);

			String text = "";
			for (int j = 0; j < input.getNumberOfInputValues(); j++)
				text += j == input.getNumberOfInputValues() - 1 ? j : j + ",";

			inputCheckboxes.add(checkbox);
			inputTextFields.add(new JTextField(text));

			inputPanel.add(checkbox);
			inputPanel.add(inputTextFields.peekLast());
		}
		return inputPanel;
	}

	@Override
	public void update(Simulator simulator) {

		currentStep = simulator.getTime();

		robots = simulator.getRobots();

		DroneNeuralNetworkController controller = (DroneNeuralNetworkController) robots.get(0).getController();
		CINeuralNetwork network = controller.getNeuralNetwork();
		ciInputs = network.getInputs();
		ciOutputs = network.getOutputs();

		int[] numberOfInputNeurons = new int[ciInputs.size()];
		for (int j = 0; j < ciInputs.size(); j++)
			numberOfInputNeurons[j] = ciInputs.get(j).getNumberOfInputValues();

		int[] numberOfHiddenInputs = new int[hidden != null && hidden.length > 0 ? hidden.length : 1];

		try {
			CICTRNN multilayer = (CICTRNN) network;
			hidden = multilayer.getHiddenStates();
			for (int j = 0; j < hidden.length; j++)
				numberOfHiddenInputs[j] = hidden.length;
		} catch (Exception e) {
		}

		int[] numberOfOutputNeurons = new int[ciOutputs.size()];
		for (int j = 0; j < ciOutputs.size(); j++)
			numberOfOutputNeurons[j] = ciOutputs.get(j).getNumberOfOutputValues();

		for (int i = 0; i < robotCheckboxes.size(); i++) {
			if (robotCheckboxes.get(i).isSelected()) {

				currentIndex = 0;

				controller = (DroneNeuralNetworkController) robots.get(i).getController();
				network = controller.getNeuralNetwork();

				ciInputs = network.getInputs();
				ciOutputs = network.getOutputs();

				processLayer(inputCheckboxes, inputTextFields, network.getInputNeuronStates(), numberOfInputNeurons);
				processLayer(outputCheckboxes, outputTextFields, network.getOutputNeuronStates(),
						numberOfOutputNeurons);
				processLayer(hiddenCheckboxes, hiddenTextFields, hidden, numberOfHiddenInputs);
			}
		}
	}
}
