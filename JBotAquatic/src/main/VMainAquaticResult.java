package main;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import evolutionaryrobotics.JBotEvolver;
import gui.Gui;

public class VMainAquaticResult extends JFrame {
	public static void main(String[] args) {
		new VMainAquaticResult(
				new String[] { "--gui",
						"classname=CIResultViewerGui,renderer=(classname=CITwoDRenderer))" });
	}

	public VMainAquaticResult(String[] args) {
		super("JBotEvolver");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		JBotEvolver jbot = null;
		try {
			jbot = new JBotEvolver(args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		add(Gui.getGui(jbot, jbot.getArguments().get("--gui")));

		setSize(1100, 680);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	public VMainAquaticResult() {
		this(
				new String[] { "--gui",
						"classname=CIResultViewerGui,renderer=(classname=CITwoDRenderer))" });
	}
}
