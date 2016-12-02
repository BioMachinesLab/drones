package fieldtests.data;

import javax.swing.JFrame;

import gui.renderer.Renderer;

public class FitnessViewer extends JFrame {
	private static final long serialVersionUID = -5020833094480400375L;

	public FitnessViewer(Renderer renderer) {
		super("Fitness Viewer");
		add(renderer);

		setSize(400, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
