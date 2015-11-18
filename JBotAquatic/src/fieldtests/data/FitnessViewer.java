package fieldtests.data;

import gui.renderer.Renderer;
import javax.swing.JFrame;

public class FitnessViewer extends JFrame{
	
	private Renderer renderer;
	
	public FitnessViewer(Renderer renderer) {
		super("Fitness Viewer");
		add(renderer);
		
		setSize(400, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
