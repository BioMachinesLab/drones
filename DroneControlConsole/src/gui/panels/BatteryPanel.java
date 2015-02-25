package gui.panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicProgressBarUI;

import network.messages.BatteryMessage;
import sun.swing.SwingUtilities2;
import threads.UpdateThread;
import dataObjects.BatteryStatus;

public class BatteryPanel extends UpdatePanel {
	private final static int CELL_QUANTITY = 3;
	private final static double MIN_VOLTAGE = 3.0;
	private final static double MAX_VOLTAGE = 4.2;
	
	private UpdateThread thread;
	private long sleepTime = 1000;
	private BatteryStatus batteryStatus = null;
	private JProgressBar[] voltageMeters= new JProgressBar[CELL_QUANTITY];

	public BatteryPanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Battery Status"));

		JPanel refreshPanel = new JPanel(new BorderLayout());
		refreshPanel.add(new JLabel("Refresh Rate"), BorderLayout.WEST);

		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(
				new String[] { "10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		refreshPanel.add(comboBoxUpdateRate, BorderLayout.EAST);

		comboBoxUpdateRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (comboBoxUpdateRate.getSelectedIndex()) {
				case 0:
					sleepTime = 100;
					break;
				case 1:
					sleepTime = 200;
					break;
				case 2:
					sleepTime = 1000;
					break;
				case 3:
					sleepTime = 10000;
					break;
				default:
					sleepTime = 1000;
					break;
				}
				wakeUpThread();
			}
		});

		JPanel jProgressBarsPanel = new JPanel(new GridLayout(1,CELL_QUANTITY));
		for(int i =0;i<CELL_QUANTITY;i++){
			//voltageMeters[i]=new JProgressBar(MIN_VOLTAGE,MAX_VOLTAGE);
			voltageMeters[i].setUI(new MyProgressUI());
			
			voltageMeters[i].setPreferredSize(new Dimension(50,10));
			voltageMeters[i].setValue(0);
			voltageMeters[i].setForeground(Color.GREEN);
			voltageMeters[i].setUI(new MyProgressUI());
			voltageMeters[i].setStringPainted(true);
			voltageMeters[i].setOrientation(SwingConstants.VERTICAL);
			
			jProgressBarsPanel.add(voltageMeters[i]);
		}
		
		
		this.add(refreshPanel, BorderLayout.SOUTH);
		this.add(jProgressBarsPanel, BorderLayout.CENTER);
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
            	g2.drawLine(barRectWidth/2 + b.left, top + barRectHeight - (int)(barRectHeight*drawingPercentage), barRectWidth/2 + b.left, top + barRectHeight/2);
            } else if(percentage < 0.5){
            	g2.setColor(Color.RED);
            	g2.drawLine(barRectWidth/2 + b.left, top + barRectHeight/2, barRectWidth/2 + b.left, top + barRectHeight - (int)(barRectHeight*drawingPercentage));
            }

            g2.setColor(Color.BLACK);
            String s = textPercentage+"%";
            SwingUtilities2.drawString(progressBar, g2, textPercentage+"%",b.left+barRectWidth/3 - s.length()*1, b.top+barRectHeight/2 - 10);
            
        }
    }
	
	private synchronized void wakeUpThread() {
		notifyAll();
		thread.interrupt();
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}

	@Override
	public void threadWait() {
		try {
			synchronized (this) {
				wait();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public long getSleepTime() {
		return sleepTime;
	}

	public synchronized void displayData(BatteryMessage message) {
		this.batteryStatus = message.getBatteryStatus();
		notifyAll();
	}
}
