package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicProgressBarUI;

import sun.swing.SwingUtilities2;
import threads.UpdateThread;

public class MotorsPanel extends JPanel implements UpdatePanel {
	private static final long serialVersionUID = 7086609300323189722L;

	private boolean locked = false;

	private JSlider leftSlider;
	private JProgressBar leftProgressBar;
	private int leftMotorPower = 0;

	private JSlider rightSlider;
	private JProgressBar rightProgressBar;
	private int rightMotorPower = 0;
	
	private UpdateThread thread;
	
	private int motorOffset = 0;
	private int motorLimit = 100;
	
	public MotorsPanel() {
		
		setBorder(BorderFactory.createTitledBorder("Motors Control"));
		setLayout(new BorderLayout());
		
		setPreferredSize(new Dimension(300,357));
		
		leftSlider = new JSlider(-100,100);
		rightSlider = new JSlider(-100,100);
		
		leftProgressBar = new JProgressBar(-100,100);
		rightProgressBar = new JProgressBar(-100,100);
		
		JPanel slidersPanel = new JPanel(new BorderLayout());

		slidersPanel.add(buildMotorSliderPanel(leftProgressBar, leftSlider, "Left"), BorderLayout.WEST);
		slidersPanel.add(buildMotorSliderPanel(rightProgressBar, rightSlider, "Right"), BorderLayout.EAST);
		
		add(slidersPanel, BorderLayout.WEST);
		
		add(buildAdjustmentPanel(), BorderLayout.EAST);
		
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
		
		add(soutPanel, BorderLayout.SOUTH);
	}
	
	private JPanel buildAdjustmentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JSlider limitSlider = new JSlider(0,100);
		limitSlider.setValue(100);
		limitSlider.setMinorTickSpacing(5);
		limitSlider.setPaintLabels(true);
		limitSlider.setPaintTicks(true);
		limitSlider.setSnapToTicks(true);
		limitSlider.setOrientation(SwingConstants.VERTICAL);
		limitSlider.setPreferredSize(new Dimension(150, 180));
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("0") );
		labelTable.put(new Integer(25), new JLabel("25") );
		labelTable.put(new Integer(50), new JLabel("50") );
		labelTable.put(new Integer(75), new JLabel("75") );
		labelTable.put(new Integer(100), new JLabel("100") );
		limitSlider.setLabelTable(labelTable);
		
		limitSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				motorLimit = limitSlider.getValue();
				if(thread != null) {
					thread.interrupt();
				}
			}
		});
		
		JPanel topSliderPanel = new JPanel(new BorderLayout());
		
		topSliderPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Speed Limit",
				TitledBorder.CENTER, TitledBorder.TOP));
		
		topSliderPanel.add(limitSlider, BorderLayout.NORTH);
		
		JSlider offsetSlider = new JSlider(-100,100);
		offsetSlider.setMinorTickSpacing(20);
		offsetSlider.setPaintLabels(true);
		offsetSlider.setPaintTicks(true);
		offsetSlider.setSnapToTicks(false);
		offsetSlider.setOrientation(SwingConstants.HORIZONTAL);
		offsetSlider.setPreferredSize(new Dimension(150, 70));
		
		offsetSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				motorOffset = offsetSlider.getValue();
				if(thread != null) {
					thread.interrupt();
				}
			}
		});
		
		offsetSlider.setValue(0);
		
		JPanel bottomSliderPanel = new JPanel(new BorderLayout());
		
		bottomSliderPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Motor Offset",
				TitledBorder.CENTER, TitledBorder.TOP));
		
		bottomSliderPanel.add(offsetSlider, BorderLayout.NORTH);
		
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(0), new JLabel("0") );
		labelTable.put(new Integer(-100), new JLabel("-100") );
		labelTable.put(new Integer(100), new JLabel("100") );
		offsetSlider.setLabelTable(labelTable);
		
		panel.add(topSliderPanel, BorderLayout.NORTH);
		panel.add(bottomSliderPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel buildMotorSliderPanel(JProgressBar progressBar, JSlider slider, String name) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), name,
				TitledBorder.CENTER, TitledBorder.TOP));

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
				if(thread != null) {
					thread.interrupt();
				}
			}
		});
		panel.add(slider, BorderLayout.WEST);

		progressBar.setValue(0);
		progressBar.setForeground(Color.GREEN);
		progressBar.setUI(new MyProgressUI());
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
		
		val = Math.max(val, -100);
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
	
	public double getLeftMotorPower() {
		return leftMotorPower/100.0;
	}
	
	public double getRightMotorPower() {
		return rightMotorPower/100.0;
	}
	
	public double getMotorOffset() {
		return motorOffset;
	}
	
	public double getMotorLimit() {
		return motorLimit/100.0;
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public int getSleepTime() {
		return 10;
	}
	
	private class MyProgressUI extends BasicProgressBarUI {

        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {
        	if (!(g instanceof Graphics2D)) {
                return;
            }

            Insets b = progressBar.getInsets(); // area for border
            int barRectWidth = progressBar.getWidth() - (b.right + b.left);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

            if (barRectWidth <= 0 || barRectHeight <= 0) {
                return;
            }

            int cellLength = getCellLength();
            int cellSpacing = getCellSpacing();
            // amount of progress to draw
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.BLUE);
            
            double percentage = amountFull/(double)barRectHeight;
            double drawingPercentage = ((int)(percentage*100)/100.0);
            int textPercentage = (int)Math.round(((amountFull/(double)barRectHeight-0.5)*200));
            
            barRectHeight-=25;
            int top = b.top + 10;
            
            // draw the cells
            if (cellSpacing == 0 && amountFull > 0) {
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            } else {
                // draw each individual cell
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                        0f, new float[] { cellLength, cellSpacing }, 0f));
            }
            if(percentage > 0.5) {
            	g2.setColor(Color.GREEN);
            	g2.drawLine(barRectWidth/2 + b.left, top + barRectHeight - (int)(barRectHeight*drawingPercentage),
                    barRectWidth/2 + b.left, top + barRectHeight/2);
            } else if(percentage < 0.5){
            	g2.setColor(Color.RED);
            	g2.drawLine(barRectWidth/2 + b.left, top + barRectHeight/2,
                        barRectWidth/2 + b.left, top + barRectHeight - (int)(barRectHeight*drawingPercentage));
            }

            g2.setColor(Color.BLACK);
            String s = textPercentage+"%";
            SwingUtilities2.drawString(progressBar, g2, textPercentage+"%",b.left+barRectWidth/3 - s.length()*1, b.top+barRectHeight/2 - 10);
            
        }
    }
}