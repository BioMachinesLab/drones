package fieldtests.data;

import java.awt.GridLayout;
import gui.renderer.Renderer;
import javax.swing.JFrame;

public class DoubleFitnessViewer extends JFrame{
	
	private Renderer renderer1;
	private Renderer renderer2;
//	private CompareFitnessField plot;
	
	public DoubleFitnessViewer() {
		super("Position Plot");
		setLayout(new GridLayout(1,2));
		
		final DoubleFitnessViewer t = this;
		
//		JButton button = new JButton("Replay");
//		
//		button.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if(plot != null)
//					plot.interrupt();
//				plot = new CompareFitnessField(t);
//				plot.start();
//			}
//		});
		
//		JButton pause = new JButton("Pause/Play");
//		pause.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if(plot != null)
//					plot.pause();
//			}
//		});
		
//		JPanel south = new JPanel();
//		south.add(button);
//		south.add(pause);
		
//		add(south, BorderLayout.SOUTH);
		
		setSize(700, 400);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		button.doClick();
	}
	
	public void setRenderer1(Renderer renderer) {
		if(this.renderer1 != null)
			remove(this.renderer1);
		add(renderer);
		this.renderer1 = renderer;
	}
	
	public void setRenderer2(Renderer renderer) {
		if(this.renderer2 != null)
			remove(this.renderer2);
		add(renderer);
		this.renderer2 = renderer;
	}
	
}
