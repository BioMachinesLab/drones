package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JPanel;

import commoninterface.mathutils.Vector2d;

public class Graph extends JPanel {
	
	public static final int LEGEND_SIZE = 15;
	public static final int ORIGINAL_PAD_TOP = 20;
	
	private static final Color[] COLORS = {
			new Color(204,37,41),//Red
			new Color(58,106,177),//Blue
			new Color(62,150,81),//Green
			new Color(218,124,48),//Orange
			new Color(83,81,84),//Gray
			new Color(107,76,154),//Purple
			new Color(146,36,40),//Dark red
			new Color(148,139,61),//Lime
		};
	
	private static final int MAX = 4000;
	private int showLast = 2000;
	private String xLabel = "";
	private String yLabel = "";
	private double max = 0;
	private double min = 0;
	private double xInc = 0;
	private double scale = 0;
	private DecimalFormat df = new DecimalFormat("#.##");
	private int pad = 50;
	private int padTop = ORIGINAL_PAD_TOP;
	private Stroke stroke = new BasicStroke(1.2f);
	
	private Vector<Vector<Double>> listOfData = new Vector<Vector<Double>>();
	private Vector<Double> simpleData = new Vector<Double>();
	private Vector<String> legends = new Vector<String>();

	private Vector2d mousePosition = new Vector2d();
	
	public Graph() {
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				mousePosition.setX(e.getX());
				mousePosition.setY(e.getY());
				repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) { }
		});
		
	}
	
	public void addData(Double value) {
		
		if(value > max)
			max = value;
		else if(value < min)
			min = value;
		
		simpleData.add(value);
		if (simpleData.size() > MAX) {
			simpleData.remove(0);
		}
		listOfData.clear();
		listOfData.add(0, simpleData);
		repaint();
	}
	
	public int getHeaderSize() {
		return legends.size()*LEGEND_SIZE+ORIGINAL_PAD_TOP;
	}
	
	public void addLegend(String s) {
		legends.add(s);
		padTop+=LEGEND_SIZE;
	}
	
	public void addDataList(Double[] dataList){
		Vector<Double> aux = new Vector<Double>();
		
		for (int i = 0; i < dataList.length; i++) {
			aux.add(dataList[i]);
			
			if(dataList[i] != null && dataList[i] > max)
				max = dataList[i];
			else if (dataList[i] != null && dataList[i] < min)
				min = dataList[i];
		}
		
		listOfData.add(aux);
	}

	public int getShowLast() {
		return showLast;
	}

	public void setShowLast(int showLast) {
		this.showLast = showLast;
		repaint();
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		drawBackground(g2);
		drawData(g2);
		drawLines(g2);
		drawLabels(g2);
		drawLegends(g2);
		drawMouseLine(g2);
	}
	
	private void drawMouseLine(Graphics2D g2) {
		int lh = getHeight()-pad;
		int lw = getWidth()-pad;
		
		if(mousePosition.getX() >= pad && mousePosition.getX() <= (getWidth()-pad) && mousePosition.getY() >= padTop && mousePosition.getY() <= (getHeight()-pad)){
			g2.setColor(Color.BLACK);
			Stroke originalStroke = g2.getStroke();
			
			Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
	        g2.setStroke(dashed);
	        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	        
			g2.draw(new Line2D.Double(mousePosition.getX(), padTop, mousePosition.getX(), lh));
			g2.draw(new Line2D.Double(pad, mousePosition.getY(), lw, mousePosition.getY()));
			
			g2.setStroke(originalStroke);
		}
	}

	private void drawLegends(Graphics2D g2) {
		
		FontMetrics metrics = g2.getFontMetrics(g2.getFont());
		
		Stroke originalStroke = g2.getStroke();
		 
		int index = 0;
		for(String s : legends) {
			
			g2.setColor(Color.BLACK);
			
			int width = metrics.stringWidth(s);
			int height = metrics.getHeight();
			
			int x = getWidth()-pad*2-width - 10;
			int y = ORIGINAL_PAD_TOP + index*height;
			
			g2.drawString(s,x,y);
			
			x = getWidth()-pad*2;
			y-=height/3;
			
			g2.setColor(COLORS[index%COLORS.length]);
			
			g2.setStroke(stroke);
			
			g2.draw(new Line2D.Double(x,y,x+pad,y));
			
			g2.setStroke(originalStroke);
			index++;
		}
		
	}
	
	private void drawBackground(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(Color.BLACK);
	}
	
	private void drawLines(Graphics2D g2) {
		
		g2.setColor(Color.BLACK);
		
		int w = getWidth();
		int h = getHeight();
		
		int lh = h-pad;
		int lw = w-pad;
		
		g2.draw(new Line2D.Double(pad, padTop, pad, lh));
		
		double divisions = (double) (h - pad - padTop) / (max-min);
		double yLine = h - pad - divisions * (0 - min);
		g2.draw(new Line2D.Double(pad, yLine, lw, yLine));
		
		g2.drawString("0", 5 + (6-"0".length())*7, (int) (yLine+5));
		
		lh = h-pad-padTop;
		lw = w-pad*2;
		
		double divs = 5;
		
		if(lw != 0) {
			double divider = lw/divs;
			
			if(divider > 0) {
				for(int i = 0 ; i <= divs ; i++) {
					int pos = pad+(int)(divider*i);
					g2.draw(new Line2D.Double(pos, lh+padTop-7, pos, lh+padTop));
					g2.draw(new Line2D.Double(pos, padTop+7, pos, padTop));

					if(min >= 0 || i != 0){
						
						
						double toAdd = !listOfData.isEmpty() && listOfData.get(0).size() > showLast ? listOfData.get(0).size() - showLast : 0; 
						
						String number = df.format((showLast/divs*i) + toAdd);
						g2.drawString(number, pos - (number.length()*4), lh+padTop+15);
					}
					
				}
			}
		}
		
		if(lh != 0) {
			double divider = lh/divs;
			if(divider > 0) {
				for(int i = 0 ; i <= divs ; i++) {
					int pos = padTop+(int)(divider*i);
					g2.draw(new Line2D.Double(lw+pad-7, pos, lw+pad, pos));
					g2.draw(new Line2D.Double(pad+7, pos, pad, pos));

					String number = df.format(((max-min)-(max-min)/divs*i) + min);
					g2.drawString(number, 5 + (6-number.length())*7, pos+5);
				}
			}
		}
	}
	
	private void drawLabels(Graphics2D g2) {
		
		g2.setColor(Color.BLACK);
		
		int w = getWidth();
		int h = getHeight();

		//Y AXIS
		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics("0", frc);
		float sh = lm.getAscent() + lm.getDescent();
		
		String s = yLabel;
		float sy = padTop + ((h - pad -padTop) - s.length() * sh) / 2 + lm.getAscent() + 15;
		
	    AffineTransform fontAT = new AffineTransform();
	    Font theFont = g2.getFont();
	    fontAT.rotate(Math.toRadians(-90));
	    Font theDerivedFont = theFont.deriveFont(fontAT);
	    g2.setFont(theDerivedFont);
		
		for(int i = 0; i < s.length(); i++) {
			String letter = String.valueOf(s.charAt(s.length() - i -1));
			float sw = (float)font.getStringBounds(letter, frc).getWidth();
			float sx = (pad - sw)/2 - 9;
			g2.drawString(letter, sx, sy);
			sy += sh - 5;
		}
		
		g2.setFont(theFont);
		
		//X AXIS
		s = xLabel;
		sy = h - pad + (pad - sh) / 2 + lm.getAscent() + 10;
		float sw = (float) font.getStringBounds(s, frc).getWidth();
		float sx = (w - sw) / 2 +10;
		g2.drawString(s, sx, sy);

		if(mousePosition.getX() >= pad && mousePosition.getX() <= (getWidth()-pad) && mousePosition.getY() >= padTop && mousePosition.getY() <= (getHeight()-pad)){
			double lw = getWidth() - pad * 2;
			double mouseXShift = mousePosition.getX() - pad;
			double mX = (mouseXShift * showLast) / lw;
			
			double lh = (getHeight() - pad) - padTop;
			double mouseYShift = lh - (mousePosition.getY() - padTop);
			double mY = mouseYShift*(max-min)/lh + min;
			
			String mousePoint = "X: " + df.format(mX) + ", Y: " + df.format(mY);
			float halfDistance = ((getWidth()-pad) - sx)/2;
			g2.drawString(mousePoint, sx+halfDistance, sy);
		}
		
	}
	
	private void drawData(Graphics2D g2) {
		
		int colorIndex = 0;
		int w = getWidth();
		int h = getHeight();

		xInc = (double) (w - 2 * pad) / (showLast - 1);
		scale = (double) (h - pad - padTop) / (max-min);
		
		Stroke originalStroke = g2.getStroke();
		
		g2.setStroke(stroke);
		
		for (Vector<Double> dataList : listOfData) {
			
			Vector<Double> data = new Vector<Double>();
			
			data.addAll(dataList);
			
			int currentColor = (colorIndex++)%COLORS.length;

			// Draw lines
			int init = Math.max(0, data.size() - showLast);
			g2.setPaint(COLORS[currentColor]);
			for (int i = init; i < data.size() - 1; i++) {
				if(i >= 0 && data.get(i) != null && data.get(i+1) != null) {
					double x1 = pad + (i - init) * xInc;
					double y1 = h - pad - scale * (data.get(i) - min);
					double x2 = pad + (i - init + 1) * xInc;
					double y2 = h - pad - scale * (data.get(i + 1) - min);
					g2.draw(new Line2D.Double(x1, y1, x2, y2));
				}
			}
		}
		g2.setStroke(originalStroke);
	}
	
	public void clear() {
		simpleData.clear();
		listOfData.clear();
		legends.clear();
		padTop = ORIGINAL_PAD_TOP;
		max = 0;
		min = 0;
		repaint();
	}

	public void setxLabel(String name) {
		xLabel = name;
	}
	
	public void setyLabel(String name) {
		yLabel = name;
	}
}