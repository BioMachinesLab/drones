package gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;

public class Motors_Panel extends JPanel {
	private static final long serialVersionUID = 7086609300323189722L;
	private boolean locked = false;
	private JCheckBox chckbxLockControl;

	private JPanel leftPanel;
	private JSlider leftSlider;
	private JProgressBar leftProgressBar;
	private JButton btnStopLeft;

	private JPanel rightPanel;
	private JSlider rightSlider;
	private JProgressBar rightProgressBar;
	private JButton btnStopRight;

	public Motors_Panel() {
		setBorder(BorderFactory.createTitledBorder("Motors Control"));
		setLayout(null);

		buildLeftPanel();
		buildRightPanel();

		chckbxLockControl = new JCheckBox("Lock Control");
		chckbxLockControl.setBounds(107, 270, 97, 23);
		// chckbxLockControl.is
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
				if (locked) {
					rightProgressBar.setValue(leftSlider.getValue());
					rightSlider.setValue(leftSlider.getValue());
				}

				leftProgressBar.setValue(leftSlider.getValue());
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
				if (locked) {
					leftProgressBar.setValue(rightSlider.getValue());
					leftSlider.setValue(rightSlider.getValue());
				}

				rightProgressBar.setValue(rightSlider.getValue());
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

	public void changeLeftMotorPower(int value) {
		leftSlider.setValue(value);
		leftProgressBar.setValue(value);
	}

	public void changeRightMotorPower(int value) {
		rightSlider.setValue(value);
		rightProgressBar.setValue(value);
	}
}
