package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import commoninterface.neat.core.NEATNeuralNet;
import commoninterface.neuralnetwork.CINEATNetwork;
import commoninterface.neuralnetwork.CINeuralNetwork;
import commoninterface.sensors.DroneCISensor;
import controllers.Controller;
import controllers.DroneNeuralNetworkController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import gui.renderer.CITwoDRenderer;
import gui.util.GraphViz;
import gui.util.SpringUtilities;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;

public class CIResultViewerGui extends ResultViewerGui {
	private static final long serialVersionUID = -8172879163076772847L;
	private JTextField droneIDTextField;
	private JTextField coneSensorIdTextField;
	private JTextField coneTransparenceTextField;
	private JCheckBox seeSensorJCheckBox;
	private JCheckBox seeEntitiesJCheckBox;

	private JCheckBox velocityVectorsCheckbox;
	private boolean displayVelocityVectors = true;

	public CIResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}

	@Override
	protected void launchGraphPlotter(JBotEvolver jbot, Simulator sim) {
		new CIGraphPlotter(jbot, sim);
	}

	@Override
	protected void displayNeuralNetwork() {
		if (showNeuralNetwork && graphViz == null) {
			NeuralNetworkController nn = (NeuralNetworkController) simulator.getEnvironment().getRobots().get(0)
					.getController();
			graphViz = new GraphViz(nn.getNeuralNetwork());
		}
		if (showNeuralNetwork)
			graphViz.show();
	}

	@Override
	protected void updateNeuralNetworkDisplay() {
		if (showNeuralNetwork) {
			if (graphViz != null)
				graphViz.changeNeuralNetwork(
						((NeuralNetworkController) simulator.getEnvironment().getRobots().get(0).getController())
								.getNeuralNetwork());
			else
				graphViz = new GraphViz(
						((NeuralNetworkController) simulator.getEnvironment().getRobots().get(0).getController())
								.getNeuralNetwork());

		}
	}

	@Override
	protected Container initRightWrapperPanel() {
		Container panel = super.initRightWrapperPanel();

		if (enableDebugOptions) {
			extraOptionsPanel.setLayout(new GridLayout(3, 1));
			velocityVectorsCheckbox = new JCheckBox("Show targets velocity vectors");
			velocityVectorsCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
			extraOptionsPanel.add(velocityVectorsCheckbox);

			// A Informations panel
			JPanel sensorsPanel = new JPanel(new GridLayout(2, 1));
			sensorsPanel.setBorder(BorderFactory.createTitledBorder("Sensors Options"));

			JPanel optionBoxesPanel = new JPanel(new GridLayout(2, 1));
			seeEntitiesJCheckBox = new JCheckBox("See entities");
			seeEntitiesJCheckBox.setHorizontalAlignment(JTextField.CENTER);
			seeEntitiesJCheckBox.setSelected(((CITwoDRenderer) renderer).isSeeEntitiesEnabled());
			optionBoxesPanel.add(seeEntitiesJCheckBox);

			seeSensorJCheckBox = new JCheckBox("See sensors");
			seeSensorJCheckBox.setHorizontalAlignment(JTextField.CENTER);
			seeSensorJCheckBox.setSelected(((CITwoDRenderer) renderer).isSeeSensorEnabled());
			optionBoxesPanel.add(seeSensorJCheckBox);
			sensorsPanel.add(optionBoxesPanel);

			JPanel textfieldsPanel = new JPanel(new GridLayout(3, 2));
			textfieldsPanel.add(new JLabel("Drone ID:"));
			droneIDTextField = new JTextField("0");
			droneIDTextField.setHorizontalAlignment(JTextField.CENTER);
			textfieldsPanel.add(droneIDTextField);

			textfieldsPanel.add(new JLabel("Sensor ID:"));
			coneSensorIdTextField = new JTextField("2");
			coneSensorIdTextField.setHorizontalAlignment(JTextField.CENTER);
			textfieldsPanel.add(coneSensorIdTextField);

			textfieldsPanel.add(new JLabel("Cone Transparence:"));
			coneTransparenceTextField = new JTextField("35");
			coneTransparenceTextField.setHorizontalAlignment(JTextField.CENTER);
			textfieldsPanel.add(coneTransparenceTextField);
			sensorsPanel.add(textfieldsPanel);

			debugOptions.add(sensorsPanel);

			SpringUtilities.makeGrid(debugOptions, 3, 1, // rows, cols
					0, 0, // initialX, initialY
					10, 10);
		}

		// @vasco: 1000 Pixels is a hardcoded value and 300 came from super
		// class
		panel.setPreferredSize(new Dimension(300, 1000));
		return new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}

	@Override
	protected void initListeners() {
		super.initListeners();

		if (enableDebugOptions) {
			seeSensorJCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox check = (JCheckBox) e.getSource();
					int droneID = -1, coneID = -1;
					double transparence = -1;

					try {
						droneID = Integer.parseInt(droneIDTextField.getText());
						coneID = Integer.parseInt(coneSensorIdTextField.getText());
						transparence = Double.parseDouble(coneTransparenceTextField.getText());

						transparence /= 100;
						transparence = (transparence > 1) ? 1 : transparence;
						transparence = (transparence < 0) ? 0 : transparence;

						((CITwoDRenderer) renderer).setDroneID(droneID);
						((CITwoDRenderer) renderer).setConeSensorID(coneID);
						((CITwoDRenderer) renderer).setConeTransparency(transparence);
						((CITwoDRenderer) renderer).setConeClass(DroneCISensor.class.getName());
						((CITwoDRenderer) renderer).seeSensors(check.isSelected());

						if (simulationState == STOPPED || simulationState == PAUSED) {
							renderer.drawFrame();
						}
					} catch (NumberFormatException exception) {

					}
				}
			});

			seeEntitiesJCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox check = (JCheckBox) e.getSource();
					((CITwoDRenderer) renderer).seeEntities(check.isSelected());

					if (simulationState == STOPPED || simulationState == PAUSED) {
						renderer.drawFrame();
					}
				}
			});

			velocityVectorsCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JCheckBox check = (JCheckBox) e.getSource();
					displayVelocityVectors = check.isSelected();
					((CITwoDRenderer) renderer).displayVelocityVectors(displayVelocityVectors);

					if (simulationState == STOPPED || simulationState == PAUSED) {
						renderer.drawFrame();
					}
				}
			});
		}
	}

	@Override
	public Simulator loadSimulator() {
		Simulator sim = super.loadSimulator();

		if (sim != null) {
			Controller cont = sim.getEnvironment().getRobots().get(0).getController();

			if (cont instanceof DroneNeuralNetworkController) {
				CINeuralNetwork network = ((DroneNeuralNetworkController) cont).getNeuralNetwork();

				inputNeuronsTextField.setText(Integer.toString(network.getNumberOfInputNeurons()));
				outputNeuronsTextField.setText(Integer.toString(network.getNumberOfOutputNeurons()));

				if (network instanceof CINEATNetwork) {
					NEATNeuralNet net = ((CINEATNetwork) network).getNetwork();
					totalNeuronsTextField.setText(Integer.toString(net.neurons().length));
					synapsesTextField.setText(Integer.toString(net.connections().length));
				} else {
					totalNeuronsTextField.setText("N/A");
					synapsesTextField.setText("N/A");
				}
			}
		}

		return sim;
	}
}
