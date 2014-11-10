package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

public class CompassRose extends JPanel {
	private Font font;
	private final int DIAMETER_MARGIN = 30;
	private final int BEZEL_MARKS_QNT = 8;
	private double diameter, idPointer;
	private float sx, sy;
	private String s;
	private NumberFormat nf;
	private List<Pointer> pointerList;

	public static void main(String[] args) {
		CompassRose compassPanel = new CompassRose();
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(compassPanel);
		// f.setSize(200, 200);
		f.setLocation(200, 200);
		f.setVisible(true);
	}

	public CompassRose() {
		setSize(200, 200);

		// font = new Font("lucida bright demibold italic", Font.PLAIN, 18);
		font = UIManager.getDefaults().getFont("TabbedPane.font");
		idPointer = 0;
		nf = NumberFormat.getIntegerInstance();
		pointerList = new ArrayList<Pointer>();
		PointerSelector selector = new PointerSelector(this);
		addMouseListener(selector);
		addMouseMotionListener(selector);
		setBackground(Color.white);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font);
		double center_x = getWidth() / 2;
		double center_y = getHeight() / 2;
		diameter = Math.min(getWidth(), getHeight()) - 2 * DIAMETER_MARGIN;
		double idRadius = diameter / 2 + 15;

		// circle
		g2.draw(new Ellipse2D.Double(center_x - diameter / 2, center_y
				- diameter / 2, diameter, diameter));

		// bezel marks - 8 points at each 45 degrees
		double theta = -Math.PI / 2;
		double thetaInc = 2 * Math.PI / BEZEL_MARKS_QNT;
		double x1, y1, x2, y2, psi;
		for (int j = 0; j < BEZEL_MARKS_QNT; j++) {
			x1 = center_x + (diameter / 2 - 1) * Math.cos(theta);
			y1 = center_y + (diameter / 2 - 1) * Math.sin(theta);
			
			// declive da recta
			if (j % 2 == 0)
				psi = diameter / 2 - 5;
			else
				psi = diameter / 2 - 3;
			
			
			x2 = center_x + psi * Math.cos(theta);
			y2 = center_y + psi * Math.sin(theta);
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
			g2.drawString("360", (int) x1, (int) y1);
			theta += thetaInc;
		}

		// selected bearings
		g2.setPaint(Color.blue);
		Pointer bearing;
		for (int j = 0; j < pointerList.size(); j++) {
			bearing = (Pointer) pointerList.get(j);
			g2.draw(bearing.radial);
			g2.drawString(bearing.angleLabel, bearing.sx, bearing.sy);
		}

		// dynamic bearings
		FontRenderContext frc = g2.getFontRenderContext();
		theta = idPointer + Math.PI / 2;
		if (theta < 0)
			theta += 2 * Math.PI;
		s = nf.format(Math.toDegrees(theta));
		float width = (float) font.getStringBounds(s, frc).getWidth();
		LineMetrics lm = font.getLineMetrics(s, frc);
		float ascent = lm.getAscent();
		float descent = lm.getDescent();
		float height = ascent - descent;
		double scx = center_x + (idRadius + width / 2) * Math.cos(idPointer);
		double scy = center_y + (idRadius + height / 2) * Math.sin(idPointer);
		sx = (float) scx - width / 2;
		sy = (float) scy + height / 2;
		g2.setPaint(Color.red);
		g2.drawString(s, sx, sy);

		// construction markers
		g2.setPaint(Color.orange);
		g2.draw(new Ellipse2D.Double(center_x - idRadius, center_y - idRadius,
				2 * idRadius, 2 * idRadius));
		g2.fill(new Ellipse2D.Double(scx - 2, scy - 2, 4, 4));
		g2.fill(new Ellipse2D.Double(sx - 2, sy - 2, 4, 4));
		g2.draw(new Rectangle2D.Double(sx, sy - height, width, height));

	}

	public void setBearing(double theta) {
		idPointer = theta;
		repaint();
	}

	public void addBearing(Pointer bearing) {
		pointerList.add(bearing);
		repaint();
	}

	public double getDia() {
		return diameter;
	}

	public String getS() {
		return s;
	}

	public float getSx() {
		return sx;
	}

	public float getSy() {
		return sy;
	}

	protected class PointerSelector extends MouseInputAdapter {
		CompassRose compassRose;

		public PointerSelector(CompassRose cp) {
			compassRose = cp;
		}

		public void mousePressed(MouseEvent e) {
			Point p = e.getPoint();
			double theta = getRadial(p);
			double cx = compassRose.getWidth() / 2;
			double cy = compassRose.getHeight() / 2;
			double x = cx + ((compassRose.getDia() - 1) / 2) * Math.cos(theta);
			double y = cy + ((compassRose.getDia() - 1) / 2) * Math.sin(theta);
			Line2D line = new Line2D.Double(cx, cy, x, y);
			Pointer bearing = new Pointer(line, compassRose.getS(),
					compassRose.getSx(), compassRose.getSy());
			compassRose.addBearing(bearing);
		}

		public void mouseMoved(MouseEvent e) {
			Point p = e.getPoint();
			double theta = getRadial(p);
			compassRose.setBearing(theta);
		}

		private double getRadial(Point p) {
			double cx = compassRose.getWidth() / 2;
			double cy = compassRose.getHeight() / 2;
			return Math.atan2(p.y - cy, p.x - cx);
		}

	}

	protected class Pointer {
		Line2D radial;
		String angleLabel;
		float sx, sy;

		public Pointer(Line2D line, String angleLabel, float x, float y) {
			radial = line;
			this.angleLabel = angleLabel;
			sx = x;
			sy = y;
		}
	}
}