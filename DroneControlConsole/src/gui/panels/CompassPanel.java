package gui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import network.messages.CompassMessage;
import threads.UpdateThread;

public class CompassPanel extends UpdatePanel {
	
	private UpdateThread thread;
	private JTextField heading;
	private long sleepTime = 1000;
	private int headingValue = 0;
	private CompassDrawingPanel compassDrawing;
	
	public CompassPanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Compass Data"));
		
		JPanel valuePanel = new JPanel(new BorderLayout());
		
		valuePanel.add(new JLabel("Heading"), BorderLayout.WEST);
		
		heading = new JTextField(6);
		heading.setEditable(false);
		valuePanel.add(heading, BorderLayout.EAST);
		
		JPanel refreshPanel = new JPanel(new BorderLayout());
		refreshPanel.add(new JLabel("Refresh Rate"), BorderLayout.WEST);
		
		JComboBox<String> comboBoxUpdateRate = new JComboBox<String>();
		comboBoxUpdateRate.setModel(new DefaultComboBoxModel<String>(new String[] {
				"10 Hz", "5 Hz", "1 Hz", "0.1Hz" }));
		comboBoxUpdateRate.setSelectedIndex(2);
		refreshPanel.add(comboBoxUpdateRate, BorderLayout.EAST);
		
		JPanel right = new JPanel(new BorderLayout());
		
		right.add(valuePanel, BorderLayout.NORTH);
		right.add(refreshPanel, BorderLayout.SOUTH);
		
		add(right, BorderLayout.EAST);
		compassDrawing = new CompassDrawingPanel();
		add(compassDrawing, BorderLayout.WEST);
		
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
			synchronized(this){
				wait();
			}
		}catch(Exception e) {}
	}
	
	@Override
	public long getSleepTime() {
		return sleepTime;
	}

	public synchronized void displayData(CompassMessage message) {
		this.headingValue = message.getHeading();
		heading.setText(""+headingValue);
		notifyAll();
		compassDrawing.repaint();
	}
	
	public class CompassDrawingPanel extends JPanel {
        Image bufImage;
        Graphics bufG;
        private int circleX, circleY, circleRadius;
        private int[] xPoints, yPoints;

        public CompassDrawingPanel() {
            setVisible(true);
            setPreferredSize(new Dimension(150,150));
        }
        
        @Override
        public void paintComponent(Graphics g) {
        	
			 int w = this.getSize().width;
			 int h = this.getSize().height;
			
			 if (bufImage == null) {
			     bufImage = this.createImage(w, h);
			     bufG = bufImage.getGraphics();
			 }
			
			 bufG.setColor(this.getBackground());
			 bufG.fillRect(0, 0, w, h);
			 bufG.setColor(this.getForeground());

            Graphics2D g2d = (Graphics2D) bufG;
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            circleRadius = (int) (getWidth() * 0.7);
            circleX = 25;
            circleY = 25;

            g2d.setColor(Color.BLACK);
            for (int angle = 0; angle <= 360; angle += 5) {
                double sin = Math.sin(Math.toRadians(angle));
                double cos = Math.cos(Math.toRadians(angle));
                int x1 = (int) ((circleX + circleRadius / 2) - cos  * (circleRadius * 0.37) - sin * (circleRadius * 0.37));
                int y1 = (int) ((circleY + circleRadius / 2) + sin  * (circleRadius * 0.375) - cos * (circleRadius * 0.375));
                g2d.setColor(Color.BLACK);
                g2d.drawLine(x1, y1, (circleX + circleRadius / 2), (circleY + circleRadius / 2));
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("N", circleX + circleRadius / 2 - 5, circleY - 10);
            g2d.drawString("E", circleX + circleRadius + 5, circleY + circleRadius / 2 + 4);
            g2d.drawString("S", circleX + circleRadius / 2 - 5, circleY + circleRadius + 15);
            g2d.drawString("W", circleX -20, circleY + circleRadius / 2 + 4);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(circleX, circleY, circleRadius, circleRadius);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(circleX, circleY, circleRadius, circleRadius);

            xPoints = new int[] { (int) (circleX + circleRadius / 2),
                    (int) (circleX + circleRadius * 0.25),
                    (int) (circleX + circleRadius / 2),
                    (int) (circleX + circleRadius * 0.75) };

            yPoints = new int[] { (int) (circleY + 10),
                    (int) (circleY + circleRadius * 0.85),
                    (int) (circleY + circleRadius * 0.6),
                    (int) (circleY + circleRadius * 0.85) };

            Polygon fillPoly = new Polygon(xPoints, yPoints, 4);
            Polygon outerPoly = new Polygon(xPoints, yPoints, 4);

            int rotationX = circleX + (circleRadius / 2);
            int rotationY = circleX + (circleRadius / 2);
            g2d.setColor(Color.green);
            int ovalRadius = 2;
            g2d.fillOval((int)(circleX + (circleRadius / 2) + Math.cos(Math.toRadians(headingValue-90))*55) - ovalRadius, (int)(circleX + (circleRadius / 2) + Math.sin(Math.toRadians(headingValue-90))*55) - ovalRadius, ovalRadius*2, ovalRadius*2);
            AffineTransform old = g2d.getTransform();
            
            AffineTransform at = g2d.getTransform().getRotateInstance(Math.toRadians(headingValue), rotationX, rotationY);
            
            g2d.setTransform(at);

            g2d.setColor(Color.RED);
            g2d.fillPolygon(fillPoly);
            
            g2d.setColor(Color.black);
            g2d.draw(outerPoly);
            
            g2d.setTransform(old);
            
            g.drawImage(bufImage, 0, 0, this);
        }

    }
}