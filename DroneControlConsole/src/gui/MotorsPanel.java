package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MotorsPanel extends JPanel {
	private static final long serialVersionUID = 7086609300323189722L;

	private GUI gui;
	private boolean locked = false;

	private JSlider leftSlider;
	private JProgressBar leftProgressBar;
	private int leftMotorPower = 0;

	private JSlider rightSlider;
	private JProgressBar rightProgressBar;
	private int rightMotorPower = 0;
	
	public MotorsPanel(GUI gui) {
		this.gui = gui;
		
		setBorder(BorderFactory.createTitledBorder("Motors Control"));
		setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(300,357));
		
		leftSlider = new JSlider();
		rightSlider = new JSlider();
		
		leftProgressBar = new JProgressBar();
		rightProgressBar = new JProgressBar();

		add(buildMotorSliderPanel(leftProgressBar, leftSlider, "Left"), BorderLayout.WEST);
		add(buildMotorSliderPanel(rightProgressBar, rightSlider, "Right"), BorderLayout.EAST);
		
		JCheckBox chckbxLockControl = new JCheckBox("Lock Control");
		chckbxLockControl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				locked = chckbxLockControl.isSelected();
				if (locked) {
					rightProgressBar.setValue(leftSlider.getValue());
					rightSlider.setValue(leftSlider.getValue());
				}
			}
		});
		
		JPanel soutPanel = new JPanel();
		
		soutPanel.add(chckbxLockControl);
		
		JCheckBox chckbxEnableGamepad = new JCheckBox("Enable Gamepad");
		chckbxEnableGamepad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean enable = chckbxEnableGamepad.isSelected();
				setGamePadStatus(enable);
			}
		});
		soutPanel.add(chckbxEnableGamepad);
		add(soutPanel, BorderLayout.SOUTH);
	}
	
	private JPanel buildMotorSliderPanel(JProgressBar progressBar, JSlider slider, String name) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), name,
				TitledBorder.CENTER, TitledBorder.TOP));
		panel.setLayout(new BorderLayout());

		slider.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		slider.setValue(0);
		slider.setMinorTickSpacing(5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.setOrientation(SwingConstants.VERTICAL);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (locked) {
					setLeftMotorPower(slider.getValue());
					setRightMotorPower(slider.getValue());
				} else {
					if(name.equals("Left")) {
						setLeftMotorPower(slider.getValue());
					} else {
						setRightMotorPower(slider.getValue());
					}
				}
				sendMotorsStateMessage();
			}
		});
		panel.add(slider, BorderLayout.WEST);

		progressBar.setForeground(Color.GREEN);
		progressBar.setStringPainted(true);
		progressBar.setOrientation(SwingConstants.VERTICAL);
		panel.add(progressBar, BorderLayout.CENTER);

		JButton btnStop = new JButton("Stop Motor");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (locked) {
					leftSlider.setValue(0);
					rightSlider.setValue(0);
				} else {
					slider.setValue(0);
				}
			}
		});
		panel.add(btnStop, BorderLayout.SOUTH);

		return panel;
	}
	
	private int boundValue(int val) {
		
		val = Math.max(val, 0);
		val = Math.min(val, 100);
		
		return val;
	}

	public void setLeftMotorPower(int val) {
		leftMotorPower = boundValue(val);
		leftProgressBar.setValue(leftMotorPower);
	}

	public void setRightMotorPower(int val) {
		rightMotorPower = boundValue(val);
		rightProgressBar.setValue(rightMotorPower);
	}

	public void sendMotorsStateMessage() {
		double leftPower = ((double) leftMotorPower) / 100;
		double rightPower = ((double) rightMotorPower) / 100;

		gui.getMotorSpeeds().setSpeeds(leftPower, rightPower);
	}

	private void setGamePadStatus(boolean status) {
		
		if(gui.getGamePad() != null) {
			if (status)
				gui.getGamePad().enable();
			else
				gui.getGamePad().disable();
		}
	}
}