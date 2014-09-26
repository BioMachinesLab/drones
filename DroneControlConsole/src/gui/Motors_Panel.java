package gui;

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

public class Motors_Panel extends JPanel {
	private static final long serialVersionUID = 7086609300323189722L;
	// private static final int KEY_CONTROL_INCREMENT = 5;
	private GUI gui;
	private boolean locked = false;
	private JCheckBox chckbxLockControl;

	private JPanel leftPanel;
	private JSlider leftSlider;
	private JProgressBar leftProgressBar;
	private JButton btnStopLeft;
	private int leftMotorPower = 0;

	private JPanel rightPanel;
	private JSlider rightSlider;
	private JProgressBar rightProgressBar;
	private JButton btnStopRight;
	private int rightMotorPower = 0;
	private JCheckBox chckbxEnableGamepad;

	public Motors_Panel(GUI gui) {
		this.gui = gui;
		setBorder(BorderFactory.createTitledBorder("Motors Control"));
		setLayout(null);
		setMinimumSize(new Dimension(300, 300));
		setPreferredSize(new Dimension(300, 300));

		buildLeftPanel();
		buildRightPanel();

		chckbxLockControl = new JCheckBox("Lock Control");
		chckbxLockControl.setBounds(168, 270, 97, 23);
		chckbxLockControl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				locked = chckbxLockControl.isSelected();
				if (locked) {
					rightProgressBar.setValue(leftSlider.getValue());
					rightSlider.setValue(leftSlider.getValue());
				}
			}
		});
		add(chckbxLockControl);

		chckbxEnableGamepad = new JCheckBox("Enable Gamepad");
		chckbxEnableGamepad.setBounds(20, 270, 122, 23);
		chckbxEnableGamepad.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean enable = chckbxEnableGamepad.isSelected();
				setGamePadStatus(enable);
			}
		});
		add(chckbxEnableGamepad);
	}

	private void buildLeftPanel() {
		leftPanel = new JPanel();
		leftPanel.setBounds(10, 22, 132, 250);
		leftPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Left",
				TitledBorder.CENTER, TitledBorder.TOP));
		leftPanel.setLayout(null);

		leftSlider = new JSlider();
		leftSlider.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		leftSlider.setValue(0);
		leftSlider.setMinorTickSpacing(5);
		leftSlider.setPaintLabels(true);
		leftSlider.setPaintTicks(true);
		leftSlider.setSnapToTicks(true);
		leftSlider.setOrientation(SwingConstants.VERTICAL);
		leftSlider.setBounds(10, 21, 50, 185);
		leftSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				setLeftMotorPower(leftSlider.getValue());
			}
		});
		leftPanel.add(leftSlider);

		leftProgressBar = new JProgressBar();
		leftProgressBar.setForeground(Color.GREEN);
		leftProgressBar.setStringPainted(true);
		leftProgressBar.setOrientation(SwingConstants.VERTICAL);
		leftProgressBar.setBounds(70, 21, 52, 185);
		leftPanel.add(leftProgressBar);

		btnStopLeft = new JButton("Stop Motor");
		btnStopLeft.setBounds(20, 217, 89, 23);
		btnStopLeft.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (locked) {
					rightProgressBar.setValue(0);
					rightSlider.setValue(0);
				}

				leftProgressBar.setValue(0);
				leftSlider.setValue(0);
			}
		});
		leftPanel.add(btnStopLeft);

		add(leftPanel);
	}

	private void buildRightPanel() {
		rightPanel = new JPanel();
		rightPanel.setBounds(158, 22, 132, 250);
		rightPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Right",
				TitledBorder.CENTER, TitledBorder.TOP));
		rightPanel.setLayout(null);

		rightSlider = new JSlider();
		rightSlider.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		rightSlider.setValue(0);
		rightSlider.setMinorTickSpacing(5);
		rightSlider.setPaintLabels(true);
		rightSlider.setPaintTicks(true);
		rightSlider.setSnapToTicks(true);
		rightSlider.setOrientation(SwingConstants.VERTICAL);
		rightSlider.setBounds(10, 21, 50, 185);
		rightSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				setRightMotorPower(rightSlider.getValue());
			}
		});
		rightPanel.add(rightSlider);

		rightProgressBar = new JProgressBar();
		rightProgressBar.setForeground(Color.RED);
		rightProgressBar.setStringPainted(true);
		rightProgressBar.setOrientation(SwingConstants.VERTICAL);
		rightProgressBar.setBounds(70, 21, 52, 185);
		rightPanel.add(rightProgressBar);

		btnStopRight = new JButton("Stop Motor");
		btnStopRight.setBounds(20, 216, 89, 23);
		btnStopRight.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (locked) {
					leftProgressBar.setValue(0);
					leftSlider.setValue(0);
				}

				rightProgressBar.setValue(0);
				rightSlider.setValue(0);
			}
		});
		rightPanel.add(btnStopRight);

		add(rightPanel);
	}

	public void setLeftMotorPower(int value) {
		int val = value;
		if (val > 100) {
			val = 100;
		} else {
			if (val < 0) {
				val = 0;
			}
		}

		if (locked) {
			rightProgressBar.setValue(val);
			rightSlider.setValue(val);
		}

		leftProgressBar.setValue(val);
		leftMotorPower = val;
		sendMotorsStateMessage();
	}

	public void setRightMotorPower(int value) {
		int val = value;
		if (val > 100) {
			val = 100;
		} else {
			if (val < 0) {
				val = 0;
			}
		}

		if (locked) {
			leftProgressBar.setValue(val);
			leftSlider.setValue(val);
		}

		rightProgressBar.setValue(val);
		rightMotorPower = val;
		sendMotorsStateMessage();
	}

	public void sendMotorsStateMessage() {
		double leftPower = ((double) leftMotorPower) / 100;
		double rightPower = ((double) rightMotorPower) / 100;

		gui.getMotorSpeeds().setSpeeds(leftPower, rightPower);
	}

	private void setGamePadStatus(boolean status) {
		if (status)
			gui.getGamePad().enable();
		else
			gui.getGamePad().disable();
	}
}
