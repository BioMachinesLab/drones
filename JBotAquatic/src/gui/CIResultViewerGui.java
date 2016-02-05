package gui;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import gui.util.GraphViz;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;

public class CIResultViewerGui extends ResultViewerGui {

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

}
